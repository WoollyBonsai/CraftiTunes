package com.woollybonsai.craftitunes.client.audio;

import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import com.woollybonsai.craftitunes.CraftiTunes;
import org.lwjgl.openal.AL10;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class OpenALAudioOutput {

    private final AudioPlayer player;
    private boolean initialized = false;
    
    // OpenAL IDs
    private int sourceId = -1;
    private final int[] buffers = new int[10]; // 10 buffers for 200ms queue, plenty for 20 TPS ticking
    
    // 48000Hz, 16-bit, stereo
    private static final int SAMPLE_RATE = 48000;
    private static final int FORMAT = AL10.AL_FORMAT_STEREO16;

    public OpenALAudioOutput(AudioPlayer player) {
        this.player = player;
    }

    public void init() {
        if (initialized) return;
        CraftiTunes.LOGGER.info("Initializing OpenAL PCM Bridge on Render Thread...");
        
        try {
            // Generate Source
            sourceId = AL10.alGenSources();
            
            // Set source properties (Not spatialized, 100% volume)
            AL10.alSourcei(sourceId, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
            AL10.alSourcef(sourceId, AL10.AL_GAIN, 1.0f);
            
            // Generate Buffers
            AL10.alGenBuffers(buffers);
            
            // Prime the buffers
            for (int i = 0; i < buffers.length; i++) {
                fillBuffer(buffers[i]);
            }
            
            AL10.alSourceQueueBuffers(sourceId, buffers);
            AL10.alSourcePlay(sourceId);
            initialized = true;
        } catch (Exception e) {
            CraftiTunes.LOGGER.error("OpenAL Bridge init failed", e);
        }
    }

    public void tick() {
        if (!initialized) {
            init();
            if (!initialized) return;
        }

        try {
            int processed = AL10.alGetSourcei(sourceId, AL10.AL_BUFFERS_PROCESSED);
            
            while (processed > 0) {
                int unqueuedBuffer = AL10.alSourceUnqueueBuffers(sourceId);
                fillBuffer(unqueuedBuffer);
                AL10.alSourceQueueBuffers(sourceId, unqueuedBuffer);
                processed--;
            }
            
            int state = AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE);
            if (state != AL10.AL_PLAYING) {
                // Buffer underrun recovery
                AL10.alSourcePlay(sourceId);
            }
        } catch (Exception e) {
            CraftiTunes.LOGGER.error("OpenAL Bridge tick error", e);
        }
    }

    private void fillBuffer(int bufferId) {
        AudioFrame frame = player.provide();
        if (frame != null) {
            byte[] data = frame.getData();
            ByteBuffer byteBuffer = MemoryUtil.memAlloc(data.length);
            byteBuffer.put(data).flip();
            
            AL10.alBufferData(bufferId, FORMAT, byteBuffer, SAMPLE_RATE);
            MemoryUtil.memFree(byteBuffer);
        } else {
            // If no audio is playing, feed silence to keep the stream alive
            // Frame size for 20ms at 48kHz stereo 16-bit is 3840 bytes.
            ByteBuffer silence = MemoryUtil.memAlloc(3840);
            // MemoryAlloc zeroes out, or we can just leave it empty if we want, but OpenAL needs data.
            // Actually memCalloc zeroes it out.
            for(int i=0; i<3840; i++) silence.put((byte)0);
            silence.flip();
            
            AL10.alBufferData(bufferId, FORMAT, silence, SAMPLE_RATE);
            MemoryUtil.memFree(silence);
        }
    }

    public void cleanup() {
        if (!initialized) return;
        if (sourceId != -1) {
            AL10.alSourceStop(sourceId);
            AL10.alDeleteSources(sourceId);
            sourceId = -1;
        }
        for (int buffer : buffers) {
            if (buffer != 0) {
                AL10.alDeleteBuffers(buffer);
            }
        }
        CraftiTunes.LOGGER.info("OpenAL PCM Bridge shut down successfully.");
    }
}
