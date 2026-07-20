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
            slider.margins(Insets.horizontal(15));
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
        
        FlowLayout contentContainer = rootComponent.childById(FlowLayout.class, "content-container");
        
        ButtonComponent queueBtn = rootComponent.childById(ButtonComponent.class, "btn-queue");
        if (queueBtn != null && contentContainer != null) {
            queueBtn.onPress(button -> {
                contentContainer.clearChildren();
                contentContainer.child(Components.label(Component.literal("QUEUE")).margins(Insets.bottom(10)));
                for (AudioTrack t : AudioEngine.getScheduler().getQueue()) {
                    LabelComponent lbl = Components.label(Component.literal(t.getInfo().title));
                    lbl.margins(Insets.bottom(2));
                    contentContainer.child(lbl);
                }
                if (AudioEngine.getScheduler().getQueue().isEmpty()) {
                    contentContainer.child(Components.label(Component.literal("Queue is empty.")));
                }
            });
        }

        if (contentContainer != null) {
            setupTabs(rootComponent, contentContainer);
            loadTab(rootComponent, contentContainer, "Local Files");
        }
    }

    private void setupTabs(FlowLayout rootComponent, FlowLayout contentContainer) {
        String[] tabs = {"tab-local", "tab-spotify", "tab-yt-music", "tab-apple", "tab-prime", "tab-settings"};
        String[] names = {"Local Files", "Spotify", "YT Music", "Apple Music", "Prime Music", "Settings"};
        for (int i = 0; i < tabs.length; i++) {
            final String tabTitle = names[i];
            FlowLayout tabBtn = rootComponent.childById(FlowLayout.class, tabs[i]);
            if (tabBtn != null) {
                tabBtn.mouseDown().subscribe((mouseX, mouseY, button) -> {
                    if (button == 0) {
                        loadTab(rootComponent, contentContainer, tabTitle);
                        return true;
                    }
                    return false;
                });
            }
        }
    }

    private void loadTab(FlowLayout root, FlowLayout contentContainer, String tabName) {
        contentContainer.clearChildren();
        
        int themeColor = 0xFFFFFF; // Default white
        if (tabName.equals("Spotify")) themeColor = 0x1DB954;
        else if (tabName.equals("YT Music")) themeColor = 0xFF0000;
        else if (tabName.equals("Apple Music")) themeColor = 0xFA243C;
        else if (tabName.equals("Prime Music")) themeColor = 0x00A8E1;
        else if (tabName.equals("Settings")) themeColor = 0xAAAAAA;

        if (tabName.equals("Local Files")) {
            LabelComponent header = Components.label(Component.literal(tabName.toUpperCase()));
            header.color(io.wispforest.owo.ui.core.Color.ofArgb(0xFF000000 | themeColor)).shadow(true).margins(Insets.bottom(10));
            contentContainer.child(header);

            FlowLayout splitView = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
            
            // Left Sidebar (Folders)
            FlowLayout sidebar = Containers.verticalFlow(Sizing.fill(28), Sizing.content());
            sidebar.padding(Insets.of(10));
            sidebar.margins(Insets.right(5));
            sidebar.surface(Surface.flat(0xFF1E1E1E));
            sidebar.child(Components.label(Component.literal("Folders")).shadow(true).margins(Insets.bottom(5)));
            
            FlowLayout folders = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
            String[] mockFolders = {"Downloads", "Music", "Minecraft"};
            for (String f : mockFolders) {
                FlowLayout fCard = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(30));
                fCard.surface(Surface.flat(0xFF333333));
                fCard.margins(Insets.bottom(5)).padding(Insets.of(5));
                fCard.verticalAlignment(io.wispforest.owo.ui.core.VerticalAlignment.CENTER);
                fCard.child(Components.label(Component.literal("📁 " + f)).color(io.wispforest.owo.ui.core.Color.ofArgb(0xFFBBBBBB)));
                folders.child(fCard);
            }
            sidebar.child(folders);

            sidebar.child(Components.label(Component.literal("Playlists")).shadow(true).margins(Insets.bottom(5).top(10)));
            FlowLayout playlists = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
            ButtonComponent newPlaylistBtn = Components.button(Component.literal("+ New Playlist"), b -> {});
            newPlaylistBtn.sizing(Sizing.fill(100), Sizing.fixed(20)).margins(Insets.bottom(5));
            playlists.child(newPlaylistBtn);
            
            String[] mockPlaylists = {"Chill Vibes", "Gaming", "Workout"};
            for (String p : mockPlaylists) {
                FlowLayout pCard = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(30));
                pCard.surface(Surface.flat(0xFF333333));
                pCard.margins(Insets.bottom(5)).padding(Insets.of(5));
                pCard.verticalAlignment(io.wispforest.owo.ui.core.VerticalAlignment.CENTER);
                pCard.child(Components.label(Component.literal("🎵 " + p)).color(io.wispforest.owo.ui.core.Color.ofArgb(0xFFBBBBBB)));
                playlists.child(pCard);
            }
            sidebar.child(playlists);

            // Right Main Area (Tracks)
            FlowLayout trackList = Containers.verticalFlow(Sizing.fill(68), Sizing.content());
            trackList.padding(Insets.left(5));
            
            FlowLayout tableHeader = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(20));
            tableHeader.surface(Surface.flat(0xFF2B2B2B));
            tableHeader.child(Components.label(Component.literal("Track Name")).color(io.wispforest.owo.ui.core.Color.ofArgb(0xFFAAAAAA)).sizing(Sizing.fill(45), Sizing.content()));
            tableHeader.child(Components.label(Component.literal("Artist")).color(io.wispforest.owo.ui.core.Color.ofArgb(0xFFAAAAAA)).sizing(Sizing.fill(20), Sizing.content()));
            tableHeader.child(Components.label(Component.literal("Duration")).color(io.wispforest.owo.ui.core.Color.ofArgb(0xFFAAAAAA)).sizing(Sizing.fill(15), Sizing.content()));
            trackList.child(tableHeader);
            
            trackList.child(Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(1)).surface(Surface.flat(0xFF444444)).margins(Insets.bottom(10)));

            File musicDir = new File("/home/woolly/Work/Minecraft_Mods/Craftitunes/test_music");
            if (musicDir.exists() && musicDir.isDirectory()) {
                File[] files = musicDir.listFiles((d, name) -> name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".ogg"));
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(30));
                        row.verticalAlignment(io.wispforest.owo.ui.core.VerticalAlignment.CENTER);
                        row.padding(Insets.of(5));
                        row.surface(Surface.flat(0xFF333333));
                        row.margins(Insets.bottom(2));
                        
                        // Hover effect
                        row.mouseEnter().subscribe(() -> row.surface(Surface.flat(0xFF444444)));
                        row.mouseLeave().subscribe(() -> row.surface(Surface.flat(0xFF333333)));
                        
                        row.mouseDown().subscribe((mouseX, mouseY, button) -> {
                            if (button == 0) {
                                AudioEngine.getScheduler().clearQueue();
                                AudioEngine.getPlayer().stopTrack();
                                AudioEngine.loadAndPlay(file.getAbsolutePath());
                                return true;
                            } else if (button == 1) { // Right click -> Queue
                                AudioEngine.loadAndPlay(file.getAbsolutePath());
                                return true;
                            }
                            return false;
                        });
                                           FlowLayout titleArea = Containers.horizontalFlow(Sizing.fill(40), Sizing.content());
                        titleArea.child(Components.texture(net.minecraft.resources.ResourceLocation.parse("craftitunes:textures/gui/music_icon.png"), 0, 0, 16, 16, 16, 16).sizing(Sizing.fixed(16)));
                        titleArea.child(Components.label(Component.literal(formatTrackName(file.getName()))).color(io.wispforest.owo.ui.core.Color.ofArgb(0xFFFFFFFF)).margins(Insets.left(5)));
                        row.child(titleArea);
                        
                        LabelComponent artistLbl = Components.label(Component.literal("Unknown Artist"));
                        artistLbl.sizing(Sizing.fill(20), Sizing.content());
                        row.child(artistLbl);
                        
                        LabelComponent durLbl = Components.label(Component.literal("3:00"));
                        durLbl.sizing(Sizing.fill(15), Sizing.content());
                        row.child(durLbl);
                        
                        FlowLayout actionArea = Containers.horizontalFlow(Sizing.fill(20), Sizing.content());
                        actionArea.horizontalAlignment(io.wispforest.owo.ui.core.HorizontalAlignment.RIGHT);
                        ButtonComponent playBtn = Components.button(Component.literal(">"), b -> {
                            com.woollybonsai.craftitunes.audio.AudioEngine.getScheduler().clearQueue();
                            com.woollybonsai.craftitunes.audio.AudioEngine.getPlayer().stopTrack();
                            com.woollybonsai.craftitunes.audio.AudioEngine.loadAndPlay(file.getAbsolutePath());
                        });
                        playBtn.sizing(Sizing.fixed(20), Sizing.fixed(20)).margins(Insets.right(5));
                        
                        ButtonComponent qBtn = Components.button(Component.literal("+"), b -> {
                            com.woollybonsai.craftitunes.audio.AudioEngine.loadAndPlay(file.getAbsolutePath());
                        });
                        qBtn.sizing(Sizing.fixed(20), Sizing.fixed(20));
                        actionArea.child(playBtn);
                        actionArea.child(qBtn);
                        row.child(actionArea);
                        
                        trackList.child(row);
                    }
                } else {
                    trackList.child(Components.label(Component.literal("No tracks found.")).color(io.wispforest.owo.ui.core.Color.ofArgb(0xFFAAAAAA)));
                }
            } else {
                trackList.child(Components.label(Component.literal("test_music folder not found.")).color(io.wispforest.owo.ui.core.Color.ofArgb(0xFFAAAAAA)));
            }

            splitView.child(sidebar);
            splitView.child(trackList);
            contentContainer.child(splitView);
        } else if (tabName.equals("Settings")) {
            LabelComponent header = Components.label(Component.literal("CraftiTunes Settings"));
            header.shadow(true).margins(Insets.bottom(15));
            contentContainer.child(header);

            contentContainer.child(Components.button(Component.literal("Aesthetics: Default Theme"), b -> {}).sizing(Sizing.fill(100)).margins(Insets.bottom(10)));
            contentContainer.child(Components.button(Component.literal("Notifications: ON"), b -> {}).sizing(Sizing.fill(100)).margins(Insets.bottom(10)));
            contentContainer.child(Components.button(Component.literal("Hotkeys: Configure..."), b -> {}).sizing(Sizing.fill(100)).margins(Insets.bottom(10)));
        } else if (tabName.equals("Spotify")) {
            LabelComponent header = Components.label(Component.literal(tabName.toUpperCase()));
            header.color(io.wispforest.owo.ui.core.Color.ofArgb(0xFF000000 | themeColor)).shadow(true).margins(Insets.bottom(10));
            contentContainer.child(header);

            if (com.woollybonsai.craftitunes.auth.SpotifyAuthManager.accessToken == null) {
                ButtonComponent loginBtn = Components.button(Component.literal("Login to Spotify (Web)"), b -> {
                    com.woollybonsai.craftitunes.auth.SpotifyAuthManager.startAuthFlow();
                });
                loginBtn.sizing(Sizing.fixed(150), Sizing.fixed(20)).margins(Insets.bottom(15));
                contentContainer.child(loginBtn);
                
                contentContainer.child(Components.label(Component.literal("Once you click login, your browser will open. Agree to link your account, and the token will be fetched automatically!")).color(io.wispforest.owo.ui.core.Color.ofArgb(0xFFAAAAAA)).margins(Insets.bottom(10)));
            } else {
                contentContainer.child(Components.label(Component.literal("Loading Playlists...")).color(io.wispforest.owo.ui.core.Color.ofArgb(0xFFAAAAAA)).id("spotify-loading"));
                
                com.woollybonsai.craftitunes.api.SpotifyApiClient.getUserPlaylists().thenAccept(playlists -> {
                    net.minecraft.client.Minecraft.getInstance().execute(() -> {
                        var loading = contentContainer.childById(io.wispforest.owo.ui.core.Component.class, "spotify-loading");
                        if (loading != null) contentContainer.removeChild(loading);
                        
                        if (playlists.isEmpty()) {
                            contentContainer.child(Components.label(Component.literal("No playlists found or failed to load.")).color(io.wispforest.owo.ui.core.Color.ofArgb(0xFFAAAAAA)));
                            return;
                        }
                        
                        FlowLayout grid = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
                        for (var playlist : playlists) {
                            ButtonComponent pBtn = Components.button(Component.literal(playlist.name()), b -> {
                                loadSpotifyPlaylist(root, contentContainer, playlist.id(), playlist.name());
                            });
                            pBtn.sizing(Sizing.fill(48), Sizing.fixed(30)).margins(Insets.of(0, 5, 5, 0));
                            grid.child(pBtn);
                        }
                        contentContainer.child(grid);
                    });
                });
            }
        } else {
            // Streaming Services (YT, Apple, etc) Mockups
            LabelComponent header = Components.label(Component.literal(tabName.toUpperCase()));
            header.color(io.wispforest.owo.ui.core.Color.ofArgb(0xFF000000 | themeColor)).shadow(true).margins(Insets.bottom(10));
            contentContainer.child(header);
            
            contentContainer.child(Components.label(Component.literal("GOOD EVENING, Steve")).shadow(true).margins(Insets.bottom(10)));
            
            contentContainer.child(Components.label(Component.literal("Quick Access")).margins(Insets.bottom(5)));
            FlowLayout quickGrid = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
            quickGrid.child(Components.button(Component.literal("Liked Songs"), b -> {}).sizing(Sizing.fill(32), Sizing.fixed(40)).margins(Insets.of(0, 5, 0, 5)));
            quickGrid.child(Components.button(Component.literal("Daily Mix 1"), b -> {}).sizing(Sizing.fill(32), Sizing.fixed(40)).margins(Insets.of(0, 5, 0, 5)));
            quickGrid.child(Components.button(Component.literal("LoFi Beats"), b -> {}).sizing(Sizing.fill(32), Sizing.fixed(40)).margins(Insets.of(0, 5, 0, 5)));
            contentContainer.child(quickGrid);
            
            contentContainer.child(Components.label(Component.literal("Recommendations")).margins(Insets.top(15).bottom(5)));
            FlowLayout recGrid = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
            recGrid.child(Components.button(Component.literal("Discover Weekly"), b -> {}).sizing(Sizing.fill(48), Sizing.fixed(40)).margins(Insets.of(0, 5, 0, 5)));
            recGrid.child(Components.button(Component.literal("New Releases"), b -> {}).sizing(Sizing.fill(48), Sizing.fixed(40)).margins(Insets.of(0, 5, 0, 5)));
            contentContainer.child(recGrid);
        }
    }

    private void loadSpotifyPlaylist(FlowLayout root, FlowLayout contentContainer, String playlistId, String playlistName) {
        contentContainer.clearChildren();
        
        ButtonComponent backBtn = Components.button(Component.literal("< Back to Playlists"), b -> {
            loadTab(root, contentContainer, "Spotify");
        });
        backBtn.sizing(Sizing.fixed(150), Sizing.fixed(20)).margins(Insets.bottom(10));
        contentContainer.child(backBtn);
        
        LabelComponent header = Components.label(Component.literal(playlistName));
        header.color(io.wispforest.owo.ui.core.Color.ofArgb(0xFF1DB954)).shadow(true).margins(Insets.bottom(10));
        contentContainer.child(header);
        
        contentContainer.child(Components.label(Component.literal("Loading Tracks...")).color(io.wispforest.owo.ui.core.Color.ofArgb(0xFFAAAAAA)).id("spotify-loading-tracks"));
        
        com.woollybonsai.craftitunes.api.SpotifyApiClient.getPlaylistTracks(playlistId).thenAccept(tracks -> {
            net.minecraft.client.Minecraft.getInstance().execute(() -> {
                var loading = contentContainer.childById(io.wispforest.owo.ui.core.Component.class, "spotify-loading-tracks");
                if (loading != null) contentContainer.removeChild(loading);
                
                if (tracks.isEmpty()) {
                    contentContainer.child(Components.label(Component.literal("No tracks found.")).color(io.wispforest.owo.ui.core.Color.ofArgb(0xFFAAAAAA)));
                    return;
                }
                
                for (var trackItem : tracks) {
                    var track = trackItem.track();
                    if (track == null) continue;
                    
                    FlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.fixed(30));
                    row.verticalAlignment(io.wispforest.owo.ui.core.VerticalAlignment.CENTER);
                    row.padding(Insets.of(5));
                    row.surface(Surface.flat(0xFF333333));
                    row.margins(Insets.bottom(2));
                    
                    row.mouseEnter().subscribe(() -> row.surface(Surface.flat(0xFF444444)));
                    row.mouseLeave().subscribe(() -> row.surface(Surface.flat(0xFF333333)));
                    
                    row.mouseDown().subscribe((mouseX, mouseY, button) -> {
                        if (button == 0) {
                            if (net.minecraft.client.Minecraft.getInstance().player != null) {
                                net.minecraft.client.Minecraft.getInstance().player.displayClientMessage(Component.literal("§eSpotify playback (via YT) coming soon! §7Selected: " + track.name()), false);
                            }
                            return true;
                        }
                        return false;
                    });
                    
                    LabelComponent titleLbl = Components.label(Component.literal(track.name()));
                    titleLbl.sizing(Sizing.fill(40), Sizing.content()).margins(Insets.left(5));
                    row.child(titleLbl);
                    
                    LabelComponent artistLbl = Components.label(Component.literal(track.getArtistNames()));
                    artistLbl.color(io.wispforest.owo.ui.core.Color.ofArgb(0xFFAAAAAA));
                    artistLbl.sizing(Sizing.fill(40), Sizing.content());
                    row.child(artistLbl);
                    
                    contentContainer.child(row);
                }
            });
        });
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
                        // Always update slider if the music is paused OR if enough time passed to sync
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
        String name = filename.replaceAll("\\.mp3$", "").replaceAll("\\.wav$", "").replaceAll("\\.ogg$", "");
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
