package com.anchorassist;

import net.fabricmc.api.ClientModInitializer;

public class AnchorAssist implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("[AnchorAssist] Loaded");
    }
}
