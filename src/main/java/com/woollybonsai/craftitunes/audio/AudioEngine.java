package com.woollybonsai.craftitunes.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.woollybonsai.craftitunes.CraftiTunes;

public class AudioEngine {
    private static AudioPlayerManager playerManager;
    private static AudioPlayer player;
    private static TrackScheduler scheduler;

    public static void init() {
        CraftiTunes.LOGGER.info("Initializing CraftiTunes Audio Engine...");
        
        playerManager = new DefaultAudioPlayerManager();
        
        // Registers local file audio source and standard HTTP sources
        AudioSourceManagers.registerLocalSource(playerManager);
        AudioSourceManagers.registerRemoteSources(playerManager);
        
        player = playerManager.createPlayer();
        scheduler = new TrackScheduler(player);
        player.addListener(scheduler);
        
        // TODO: We will need a bridge to pass PCM audio frames from LavaPlayer 
        // to Minecraft's OpenAL audio system in the client tick event.
    }

    public static void loadAndPlay(String trackUrl) {
        playerManager.loadItem(trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                CraftiTunes.LOGGER.info("Adding to queue: " + track.getInfo().title);
                scheduler.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();
                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }
                CraftiTunes.LOGGER.info("Adding to queue: " + firstTrack.getInfo().title + " (from playlist " + playlist.getName() + ")");
                scheduler.queue(firstTrack);
            }

            @Override
            public void noMatches() {
                CraftiTunes.LOGGER.warn("Nothing found by " + trackUrl);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                CraftiTunes.LOGGER.error("Could not play: " + exception.getMessage());
            }
        });
    }

    public static AudioPlayer getPlayer() {
        return player;
    }

    public static TrackScheduler getScheduler() {
        return scheduler;
    }
}
