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
import io.wispforest.owo.ui.container.Containers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.woollybonsai.craftitunes.audio.AudioEngine;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.io.File;
import java.nio.file.Paths;

public class CraftiTunesScreen extends BaseUIModelScreen<FlowLayout> {

    private long lastSliderUpdate = 0;

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
                    button.setMessage(Component.literal("||"));
                } else {
                    player.setPaused(!player.isPaused());
                    button.setMessage(Component.literal(player.isPaused() ? ">" : "||"));
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
                        lastSliderUpdate = System.currentTimeMillis();
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
            loadTab(rootComponent, trackList, "Local Files");
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
                    loadTab(rootComponent, trackList, tabTitle);
                });
            }
        }
    }

    private void loadTab(FlowLayout root, FlowLayout trackList, String tabName) {
        trackList.clearChildren();
        FlowLayout sidebar = root.childById(FlowLayout.class, "left-sidebar");
        LabelComponent header = root.childById(LabelComponent.class, "header-label");
        
        if (sidebar != null) sidebar.clearChildren();
        
        int themeColor = 0xFFFFFF; // Default white
        if (tabName.equals("Spotify")) themeColor = 0x1DB954;
        else if (tabName.equals("YT Music")) themeColor = 0xFF0000;
        else if (tabName.equals("Apple Music")) themeColor = 0xFA243C;
        else if (tabName.equals("Prime Music")) themeColor = 0x00A8E1;
        
        if (header != null) {
            header.text(Component.literal(tabName.toUpperCase()));
            header.color(io.wispforest.owo.ui.core.Color.ofArgb(0xFF000000 | themeColor));
        }

        if (tabName.equals("Local Files")) {
            if (sidebar != null) {
                sidebar.child(Components.label(Component.literal("Folders")).margins(Insets.bottom(5)));
                FlowLayout folders = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
                folders.child(Components.button(Component.literal("My Playlists"), b -> {}).sizing(Sizing.fixed(80)).margins(Insets.of(0, 5, 0, 5)));
                folders.child(Components.button(Component.literal("Chill Vibes"), b -> {}).sizing(Sizing.fixed(80)).margins(Insets.bottom(5)));
                sidebar.child(folders);
            }
            File musicDir = new File("/home/woolly/Work/Minecraft_Mods/Craftitunes/test_music");
            if (musicDir.exists() && musicDir.isDirectory()) {
                File[] files = musicDir.listFiles((d, name) -> name.endsWith(".mp3") || name.endsWith(".wav"));
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(22));
                        row.margins(Insets.bottom(2));
                        
                        // Right-click logic on the row background
                        row.mouseDown().subscribe((mouseX, mouseY, button) -> {
                            if (button == 1) { // Right click
                                AudioEngine.loadAndPlay(file.getAbsolutePath());
                                return true;
                            }
                            return false;
                        });
                        
                        LabelComponent nameLbl = Components.label(Component.literal(formatTrackName(file.getName())));
                        nameLbl.sizing(Sizing.fill(45), Sizing.content());
                        row.child(nameLbl);
                        
                        LabelComponent artistLbl = Components.label(Component.literal("Unknown Artist"));
                        artistLbl.sizing(Sizing.fill(20), Sizing.content());
                        row.child(artistLbl);
                        
                        LabelComponent durLbl = Components.label(Component.literal("--:--"));
                        durLbl.sizing(Sizing.fill(10), Sizing.content());
                        row.child(durLbl);
                        
                        ButtonComponent rowPlayBtn = Components.button(Component.literal(">"), btn -> {
                            AudioEngine.getScheduler().clearQueue();
                            AudioEngine.getPlayer().stopTrack();
                            AudioEngine.loadAndPlay(file.getAbsolutePath());
                        });
                        rowPlayBtn.sizing(Sizing.fixed(20), Sizing.fixed(20));
                        rowPlayBtn.margins(Insets.right(2));
                        
                        ButtonComponent rowQueueBtn = Components.button(Component.literal("+"), btn -> {
                            AudioEngine.loadAndPlay(file.getAbsolutePath());
                        });
                        rowQueueBtn.sizing(Sizing.fixed(20), Sizing.fixed(20));
                        rowQueueBtn.margins(Insets.right(2));
                        
                        row.child(rowPlayBtn);
                        row.child(rowQueueBtn);
                        
                        trackList.child(row);
                    }
                } else {
                    trackList.child(Components.label(Component.literal("No tracks found.")));
                }
            } else {
                trackList.child(Components.label(Component.literal("test_music folder not found.")));
            }
        } else {
            // Dummy content for Streaming Services
            if (sidebar != null) {
                if (tabName.equals("Spotify")) {
                    sidebar.child(Components.label(Component.literal("Your Library")).margins(Insets.bottom(5)));
                    sidebar.child(Components.button(Component.literal("Liked Songs"), b -> {}).sizing(Sizing.fill(100)).margins(Insets.bottom(5)));
                    sidebar.child(Components.button(Component.literal("Made For You"), b -> {}).sizing(Sizing.fill(100)).margins(Insets.bottom(5)));
                } else if (tabName.equals("YT Music")) {
                    sidebar.child(Components.label(Component.literal("Library")).margins(Insets.bottom(5)));
                    sidebar.child(Components.button(Component.literal("Downloads"), b -> {}).sizing(Sizing.fill(100)).margins(Insets.bottom(5)));
                    sidebar.child(Components.button(Component.literal("Supermix"), b -> {}).sizing(Sizing.fill(100)).margins(Insets.bottom(5)));
                } else {
                    sidebar.child(Components.label(Component.literal("Playlists")).margins(Insets.bottom(5)));
                    sidebar.child(Components.button(Component.literal("Favorites"), b -> {}).sizing(Sizing.fill(100)).margins(Insets.bottom(5)));
                }
            }

            LabelComponent lbl = Components.label(Component.literal(tabName + " API Integration Coming Soon!"));
            lbl.margins(Insets.top(20));
            trackList.child(lbl);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.uiAdapter != null && this.uiAdapter.rootComponent != null) {
            LabelComponent label = this.uiAdapter.rootComponent.childById(LabelComponent.class, "now-playing-label");
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
                        playBtn.setMessage(Component.literal(player.isPaused() ? ">" : "||"));
                    }
                    
                    SliderComponent slider = this.uiAdapter.rootComponent.childById(SliderComponent.class, "track-slider");
                    if (slider != null && playing.getDuration() > 0) {
                        if (System.currentTimeMillis() - lastSliderUpdate > 500) {
                            double expected = (double) playing.getPosition() / playing.getDuration();
                            if (Math.abs(slider.value() - expected) > 0.005) {
                                slider.value(expected);
                            }
                        }
                    }
                } else {
                    label.text(Component.literal("Now Playing: None"));
                    if (playBtn != null) {
                        playBtn.setMessage(Component.literal(">"));
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
