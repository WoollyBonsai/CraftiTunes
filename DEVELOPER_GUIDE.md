# CraftiTunes Developer Guide

This document explains the architecture, theory, and logic used in the CraftiTunes mod. It is designed to help contributors modify the project and understand how the different components (UI, Audio, Logic) interact.

## Architecture Overview

CraftiTunes is split into two primary domains:
1. **The Audio Engine (Backend):** Handles loading, decoding, queueing, and playing audio bytes.
2. **The GUI (Frontend):** Handles rendering the player UI, intercepting user inputs, and updating state based on the audio engine.

### 1. The Audio Engine (LavaPlayer + Java Sound Bridge)

Minecraft's native sound engine (OpenAL via SoundSystem) is optimized for short 3D positional audio (like footsteps or explosions). It is notoriously bad at streaming large, long-format music files seamlessly without severe memory overhead or stutters. 

To solve this, we bypassed Minecraft's sound engine entirely for music playback.
- **LavaPlayer:** We integrated `LavaPlayer` (typically used in Discord bots). LavaPlayer is an extremely robust library capable of streaming remote HTTP sources (YouTube, Spotify wrappers) and local files. It handles all the heavy lifting of decoding `.mp3`, `.wav`, `.flac`, etc., into raw PCM audio frames.
- **JavaAudioOutput Bridge:** LavaPlayer generates `AudioFrame` objects (usually 20ms of audio). Minecraft doesn't know what to do with these. We created a bridge using `javax.sound.sampled.SourceDataLine` (Java's native sound API). 
- **Theory of Operation:** In the Minecraft client tick (`ClientTickEvents`), we check if the `SourceDataLine` buffer has space. If it does, we ask LavaPlayer for the next frame (`player.provide()`) and write the bytes directly to the sound card.
- **Managing Latency:** Because `SourceDataLine` has an internal buffer (we allocate a massive 1-second buffer to survive Minecraft lag spikes), pausing or skipping a track would normally result in a delay while the buffer empties. We fixed this by manually controlling the line (`line.stop()` on pause, `line.flush()` on track transitions).

### 2. The GUI Framework (owo-ui)

Building complex UIs in standard Minecraft screens (using `DrawContext` and absolute pixel coordinates) is a nightmare for a music app. 

We used **owo-lib (owo-ui)**, which allows us to declare UI layouts using XML (like HTML/CSS) with features like flexboxes (`flow-layout`), percentages, padding, margins, and vertical/horizontal alignments.

- **`main_screen.xml`:** The layout is defined here. It heavily uses `horizontal` and `vertical` flow layouts to split the screen into the Sidebar, the Main Content Area, the Track List table, and the Bottom Playback Controls.
- **`CraftiTunesScreen.java`:** This class inflates the XML and binds logic to the components. 
  - **State Syncing:** In the `tick()` method (called 20 times a second), the screen queries the `AudioEngine` for the current track position and updates the `SliderComponent` and the `now-playing-label`.
  - **Slider Conflict Resolution:** If the UI updates the slider while the user is actively dragging it, the slider glitches. To fix this, we track `lastSliderUpdate`. When the user changes the slider, we pause UI updates for 500ms to allow the audio engine to catch up asynchronously.

### 3. Track Management & Queueing

- **TrackScheduler:** Attached to the `AudioPlayer` as an event listener. It maintains a `LinkedBlockingQueue<AudioTrack>`.
- When a track finishes, `onTrackEnd` is fired, and it polls the queue for the next track.
- The UI binds to these functions, clearing the queue and stopping the player when "Play" is clicked, but simply `queue()`ing the track when "Add to Queue" (or Right-Click) is used.

## How to Modify

- **To change the UI layout:** Edit `src/main/resources/assets/craftitunes/owo_ui/main_screen.xml`. You don't need to recompile the Java code for layout tweaks if you use hot-reloading!
- **To add a new Streaming Service:** You will need to implement the API calls in a new manager class, and then map those results to LavaPlayer's `AudioReference` or local cached files. See `AUTH_PLAN.md` for our upcoming architecture on this.
