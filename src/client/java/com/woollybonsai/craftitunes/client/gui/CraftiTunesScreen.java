package com.woollybonsai.craftitunes.client.gui;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.resources.ResourceLocation;

public class CraftiTunesScreen extends BaseUIModelScreen<FlowLayout> {

    public CraftiTunesScreen() {
        super(FlowLayout.class, DataSource.asset(ResourceLocation.fromNamespaceAndPath("craftitunes", "main_screen")));
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent.surface(Surface.VANILLA_TRANSLUCENT);
    }
}
