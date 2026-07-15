package com.woollybonsai.craftitunes.client.gui;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Insets;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.woollybonsai.craftitunes.audio.AudioEngine;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.io.File;

public class CraftiTunesScreen extends BaseUIModelScreen<FlowLayout> {

    public CraftiTunesScreen() {
        super(FlowLayout.class, DataSource.asset(ResourceLocation.fromNamespaceAndPath("craftitunes", "main_screen")));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT);
        
        ButtonComponent playBtn = rootComponent.childById(ButtonComponent.class, "btn-play-pause");
        if (playBtn != null) {
            playBtn.onPress(button -> {
                AudioPlayer player = AudioEngine.getPlayer();
                if (player.getPlayingTrack() == null) {
                    AudioEngine.loadAndPlay("/home/woolly/Work/Minecraft_Mods/Craftitunes/test_music/sample_beat.mp3");
                    button.setMessage(Component.literal("Pause"));
                } else {
                    player.setPaused(!player.isPaused());
                    button.setMessage(Component.literal(player.isPaused() ? "Play" : "Pause"));
                }
            });
        }

        ButtonComponent nextBtn = rootComponent.childById(ButtonComponent.class, "btn-next");
        if (nextBtn != null) {
            nextBtn.onPress(button -> {
                AudioEngine.getScheduler().nextTrack();
            });
        }
        
        FlowLayout trackList = rootComponent.childById(FlowLayout.class, "track-list");
        if (trackList != null) {
            trackList.clearChildren();
            
            File musicDir = new File("/home/woolly/Work/Minecraft_Mods/Craftitunes/test_music");
            if (musicDir.exists() && musicDir.isDirectory()) {
                File[] files = musicDir.listFiles((d, name) -> name.endsWith(".mp3") || name.endsWith(".wav"));
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        ButtonComponent trackBtn = Components.button(
                            Component.literal(file.getName()), 
                            button -> {
                                AudioEngine.loadAndPlay(file.getAbsolutePath());
                            }
                        );
                        trackBtn.sizing(Sizing.fill(100), Sizing.fixed(20));
                        trackBtn.margins(Insets.bottom(2));
                        trackList.child(trackBtn);
                    }
                } else {
                    trackList.child(Components.label(Component.literal("No tracks found.")));
                }
            } else {
                trackList.child(Components.label(Component.literal("test_music folder not found.")));
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.uiAdapter != null && this.uiAdapter.rootComponent != null) {
            LabelComponent label = this.uiAdapter.rootComponent.childById(LabelComponent.class, "lbl-now-playing");
            ButtonComponent playBtn = this.uiAdapter.rootComponent.childById(ButtonComponent.class, "btn-play-pause");
            
            if (label != null) {
                AudioPlayer player = AudioEngine.getPlayer();
                AudioTrack playing = player.getPlayingTrack();
                if (playing != null) {
                    label.text(Component.literal("Now Playing: " + playing.getInfo().title));
                    if (playBtn != null) {
                        playBtn.setMessage(Component.literal(player.isPaused() ? "Play" : "Pause"));
                    }
                } else {
                    label.text(Component.literal("Now Playing: None"));
                    if (playBtn != null) {
                        playBtn.setMessage(Component.literal("Play"));
                    }
                }
            }
        }
    }
}
