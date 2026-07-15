package com.woollybonsai.craftitunes.client.gui;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.woollybonsai.craftitunes.audio.AudioEngine;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

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
