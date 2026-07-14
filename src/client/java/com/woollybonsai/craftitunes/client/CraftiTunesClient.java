package com.woollybonsai.craftitunes.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.woollybonsai.craftitunes.CraftiTunes;
import com.woollybonsai.craftitunes.audio.AudioEngine;
import com.woollybonsai.craftitunes.client.audio.JavaAudioOutput;
import com.woollybonsai.craftitunes.client.gui.CraftiTunesScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class CraftiTunesClient implements ClientModInitializer {

    private JavaAudioOutput audioBridge;
    private static KeyMapping openUiKey;

    @Override
    public void onInitializeClient() {
        CraftiTunes.LOGGER.info("Registering client events...");
        
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            CraftiTunes.LOGGER.info("Minecraft fully booted. Registering Audio Bridge...");
            audioBridge = new JavaAudioOutput(AudioEngine.getPlayer());
        });

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (audioBridge != null) {
                audioBridge.tick();
            }
        });

        openUiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.craftitunes.open_ui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            "category.craftitunes.main"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openUiKey.consumeClick()) {
                client.setScreen(new CraftiTunesScreen());
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("craftitunes")
                    .then(ClientCommandManager.literal("play")
                            .then(ClientCommandManager.argument("url", StringArgumentType.greedyString())
                                    .executes(context -> {
                                        String url = StringArgumentType.getString(context, "url");
                                        context.getSource().sendFeedback(Component.literal("Loading track: " + url));
                                        AudioEngine.loadAndPlay(url);
                                        return 1;
                                    })
                            )
                    )
                    .then(ClientCommandManager.literal("stop")
                            .executes(context -> {
                                AudioEngine.getPlayer().stopTrack();
                                AudioEngine.getScheduler().clearQueue();
                                context.getSource().sendFeedback(Component.literal("Stopped playback and cleared queue."));
                                return 1;
                            })
                    )
                    .then(ClientCommandManager.literal("skip")
                            .executes(context -> {
                                AudioEngine.getScheduler().nextTrack();
                                context.getSource().sendFeedback(Component.literal("Skipped to next track."));
                                return 1;
                            })
                    )
            );
        });
    }
}
