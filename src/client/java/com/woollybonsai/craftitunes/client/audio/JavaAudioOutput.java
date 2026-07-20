package com.woollybonsai.craftitunes.client.audio;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.woollybonsai.craftitunes.CraftiTunes;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class JavaAudioOutput {

    private final AudioPlayer player;
    private SourceDataLine line;
    private boolean initialized = false;
    private AudioTrack lastTrack = null;

    public JavaAudioOutput(AudioPlayer player) {
        this.player = player;
    }

    public void init() {
        if (initialized) return;
        CraftiTunes.LOGGER.info("Initializing Java Sound PCM Bridge...");
        
        try {
            // Lavaplayer usually defaults to 48000Hz, 16-bit, Stereo, Big Endian.
            // If it sounds like crackling, it's usually either Endianness mismatch or buffer underrun.
            // Let's set it to Big Endian first, but with a MASSIVE 1-second buffer (192,000 bytes)
            // to survive Minecraft's 50ms tick rate.
            AudioFormat format = new AudioFormat(48000f, 16, 2, true, true);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format, 192000);
            line.start();
            initialized = true;
        } catch (Exception e) {
            CraftiTunes.LOGGER.error("JavaAudio Bridge init failed", e);
        }
    }

    public void tick() {
        if (!initialized) {
            init();
            if (!initialized) return;
        }

        AudioTrack currentTrack = player.getPlayingTrack();
        if (currentTrack != lastTrack) {
            line.flush();
            lastTrack = currentTrack;
        }

        if (player.isPaused() || currentTrack == null) {
            if (line.isActive()) {
                line.stop();
            }
            return;
        } else {
            if (!line.isActive() && line.isOpen()) {
                line.start();
            }
        }

        try {
            // Check if we need to feed more data to the sound card
            // SourceDataLine buffers audio internally. We should keep it fed if it has space.
            // 48000Hz * 2 bytes * 2 channels = 192000 bytes per second.
            // A 20ms frame from Lavaplayer is exactly 3840 bytes.
            while (line.available() > 3840) {
                AudioFrame frame = player.provide();
                if (frame != null) {
                    byte[] data = frame.getData();
                    line.write(data, 0, data.length);
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            CraftiTunes.LOGGER.error("JavaAudio Bridge tick error", e);
        }
    }

    public void cleanup() {
        if (!initialized || line == null) return;
        line.drain();
        line.stop();
        line.close();
        initialized = false;
    }
}
