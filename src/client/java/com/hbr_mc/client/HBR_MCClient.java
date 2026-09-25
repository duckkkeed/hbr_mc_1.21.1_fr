package com.hbr_mc.client;

import com.hbr_mc.HBR_MC;
import net.fabricmc.api.ClientModInitializer;

public class HBR_MCClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Client-only initialization (renderers, client events, keybinds, etc.)
        HBR_MC.LOGGER.info("Hello Fabric client!");
    }
}
