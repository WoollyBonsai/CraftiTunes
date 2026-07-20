# CraftiTunes 🎵

CraftiTunes is a feature-intensive Minecraft mod designed to seamlessly integrate local and live streaming audio (Spotify, YT Music, Apple Music, Amazon Prime Music) directly into the game client. It allows players to access their playlists, libraries, and algorithmic suggestions without needing to alt-tab, maintaining total immersion.

## Features

- **In-Game Music Player:** A fully-featured UI heavily inspired by modern music streaming apps.
- **Local Files Playback:** Load your own `.mp3` and `.wav` files directly from the `test_music` folder.
- **Audio Engine:** Powered by the robust LavaPlayer, supporting queue management, seek controls, and flawless track transitions.
- **Queueing System:** Right-click tracks to add them to your queue seamlessly while playing existing music.
- **Streaming Support (WIP):** Upcoming integration with Spotify, YT Music, Apple Music, and Amazon Music.
- **Minecraft Aesthetic:** Beautiful UI utilizing `owo-ui` to match the exact vibe of modern apps but styled within Minecraft's aesthetic limitations.

## How to Use

1. **Install Fabric:** Ensure you are running the Fabric Loader for Minecraft 1.21.
2. **Add the Mod:** Drop the compiled `craftitunes.jar` into your `.minecraft/mods` folder.
3. **Local Music:** Create a folder named `test_music` inside your Minecraft working directory (or where you run the client). Drop some `.mp3` or `.wav` files inside!
4. **Open UI:** Press the designated hotkey (default is `V`) in-game to open the CraftiTunes Master UI.
5. **Playback:** 
   - Click the play button `>` to play a track immediately.
   - Click the add button `+` (or right-click anywhere on the track row) to add it to the queue.
   - Use the slider to skip around the song.

## Building from Source

```bash
# Clone the repo
git clone https://github.com/WoollyBonsai/CraftiTunes.git

# Build the mod
./gradlew build

# Run the Minecraft client
./gradlew runClient
```

## Credits

- **LavaPlayer:** For providing the backbone audio decoding and playback logic.
- **owo-lib:** For providing the declarative XML-based UI framework used to build the player interface.
