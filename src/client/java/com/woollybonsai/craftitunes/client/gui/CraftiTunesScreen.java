package com.woollybonsai.craftitunes.client.gui;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.SliderComponent;
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
import java.nio.file.Paths;

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
        
        ButtonComponent prevBtn = rootComponent.childById(ButtonComponent.class, "btn-prev");
        if (prevBtn != null) {
            prevBtn.onPress(button -> {
                AudioTrack track = AudioEngine.getPlayer().getPlayingTrack();
                if (track != null) {
                    track.setPosition(0);
                }
            });
        }

        SliderComponent slider = rootComponent.childById(SliderComponent.class, "track-slider");
        if (slider != null) {
            slider.onChanged().subscribe(value -> {
                AudioTrack track = AudioEngine.getPlayer().getPlayingTrack();
                if (track != null) {
                    double expected = (double) track.getPosition() / track.getDuration();
                    if (Math.abs(value - expected) > 0.02) {
                        track.setPosition((long)(value * track.getDuration()));
                    }
                }
            });
        }
        
        FlowLayout trackList = rootComponent.childById(FlowLayout.class, "track-list");
        
        ButtonComponent queueBtn = rootComponent.childById(ButtonComponent.class, "btn-queue");
        if (queueBtn != null && trackList != null) {
            queueBtn.onPress(button -> {
                trackList.clearChildren();
                for (AudioTrack t : AudioEngine.getScheduler().getQueue()) {
                    LabelComponent lbl = Components.label(Component.literal(t.getInfo().title));
                    lbl.margins(Insets.bottom(2));
                    trackList.child(lbl);
                }
                if (AudioEngine.getScheduler().getQueue().isEmpty()) {
                    trackList.child(Components.label(Component.literal("Queue is empty.")));
                }
            });
        }

        if (trackList != null) {
            setupTabs(rootComponent, trackList);
            loadTab(trackList, "Local Files");
        }
    }

    private void setupTabs(FlowLayout rootComponent, FlowLayout trackList) {
        String[] tabs = {"tab-local", "tab-spotify", "tab-yt-music", "tab-apple", "tab-prime"};
        String[] names = {"Local Files", "Spotify", "YT Music", "Apple Music", "Prime Music"};
        for (int i = 0; i < tabs.length; i++) {
            final String tabTitle = names[i];
            ButtonComponent tabBtn = rootComponent.childById(ButtonComponent.class, tabs[i]);
            if (tabBtn != null) {
                tabBtn.onPress(button -> {
                    loadTab(trackList, tabTitle);
                });
            }
        }
    }

    private void loadTab(FlowLayout trackList, String tabName) {
        trackList.clearChildren();
        if (tabName.equals("Local Files")) {
            File musicDir = new File("/home/woolly/Work/Minecraft_Mods/Craftitunes/test_music");
            if (musicDir.exists() && musicDir.isDirectory()) {
                File[] files = musicDir.listFiles((d, name) -> name.endsWith(".mp3") || name.endsWith(".wav"));
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        ButtonComponent trackBtn = Components.button(
                            Component.literal(formatTrackName(file.getName()) + " (R-Click to Queue)"), 
                            button -> {
                                AudioEngine.getScheduler().clearQueue();
                                AudioEngine.getPlayer().stopTrack();
                                AudioEngine.loadAndPlay(file.getAbsolutePath());
                            }
                        );
                        
                        trackBtn.mouseDown().subscribe((mouseX, mouseY, button) -> {
                            if (button == 1) { // Right click
                                AudioEngine.loadAndPlay(file.getAbsolutePath());
                                return true;
                            }
                            return false;
                        });

                        trackBtn.sizing(Sizing.fill(100), Sizing.fixed(20));
                        trackBtn.margins(Insets.bottom(2));
                        trackBtn.renderer(ButtonComponent.Renderer.flat(0x00000000, 0x331DB954, 0x00000000));
                        trackList.child(trackBtn);
                    }
                } else {
                    trackList.child(Components.label(Component.literal("No tracks found.")));
                }
            } else {
                trackList.child(Components.label(Component.literal("test_music folder not found.")));
            }
        } else {
            LabelComponent lbl = Components.label(Component.literal(tabName + " API Integration Coming Soon!"));
            lbl.margins(Insets.top(20));
            trackList.child(lbl);
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
                    String title = playing.getInfo().title;
                    if (title == null || title.equalsIgnoreCase("Unknown track") || title.equalsIgnoreCase("Unknown title")) {
                        String uri = playing.getInfo().uri;
                        if (uri != null) {
                            title = formatTrackName(Paths.get(uri).getFileName().toString());
                        } else {
                            title = "Unknown Track";
                        }
                    }
                    
                    label.text(Component.literal("Now Playing: " + title));
                    if (playBtn != null) {
                        playBtn.setMessage(Component.literal(player.isPaused() ? "Play" : "Pause"));
                    }
                    
                    SliderComponent slider = this.uiAdapter.rootComponent.childById(SliderComponent.class, "track-slider");
                    if (slider != null && playing.getDuration() > 0) {
                        double expected = (double) playing.getPosition() / playing.getDuration();
                        if (Math.abs(slider.value() - expected) > 0.02) {
                            slider.value(expected);
                        }
                    }
                } else {
                    label.text(Component.literal("Now Playing: None"));
                    if (playBtn != null) {
                        playBtn.setMessage(Component.literal("Play"));
                    }
                    SliderComponent slider = this.uiAdapter.rootComponent.childById(SliderComponent.class, "track-slider");
                    if (slider != null) {
                        slider.value(0.0);
                    }
                }
            }
        }
    }

    private String formatTrackName(String filename) {
        if (filename == null) return "";
        String name = filename.replaceAll("\\.mp3$", "").replaceAll("\\.wav$", "");
        name = name.replace("_", " ").replace("-", " ");
        String[] words = name.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
