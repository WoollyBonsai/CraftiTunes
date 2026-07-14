package com.woollybonsai.craftitunes.client.audio;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import com.woollybonsai.craftitunes.CraftiTunes;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class JavaAudioOutput {

    private final AudioPlayer player;
    private SourceDataLine line;
    private boolean initialized = false;

    public JavaAudioOutput(AudioPlayer player) {
        this.player = player;
    }

    public void init() {
        if (initialized) return;
        CraftiTunes.LOGGER.info("Initializing Java Sound PCM Bridge...");
        
        try {
            // Lavaplayer outputs 48000Hz, 16-bit, Stereo, Big Endian PCM
            AudioFormat format = new AudioFormat(48000f, 16, 2, true, true);
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(format);
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
