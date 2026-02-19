package com.anchorassist;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class AnchorAssistScreen extends Screen {

    protected AnchorAssistScreen() {
        super(Text.literal("Anchor Assist Settings"));
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // =========================
        // AUTO HIT
        // =========================
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Auto Hit: " + (AnchorAssist.autoHitEnabled ? "ON" : "OFF")),
                button -> {
                    AnchorAssist.autoHitEnabled = !AnchorAssist.autoHitEnabled;
                    button.setMessage(Text.literal("Auto Hit: " + (AnchorAssist.autoHitEnabled ? "ON" : "OFF")));
                })
                .dimensions(centerX - 75, centerY - 40, 150, 20)
                .build()
        );

        // =========================
        // AUTO ANCHOR
        // =========================
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Auto Anchor: " + (AnchorAssist.autoAnchorEnabled ? "ON" : "OFF")),
                button -> {
                    AnchorAssist.autoAnchorEnabled = !AnchorAssist.autoAnchorEnabled;
                    button.setMessage(Text.literal("Auto Anchor: " + (AnchorAssist.autoAnchorEnabled ? "ON" : "OFF")));
                })
                .dimensions(centerX - 75, centerY - 10, 150, 20)
                .build()
        );

        // =========================
        // FAST TOTEM
        // =========================
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Fast Totem: " + (AnchorAssist.fastTotemEnabled ? "ON" : "OFF")),
                button -> {
                    AnchorAssist.fastTotemEnabled = !AnchorAssist.fastTotemEnabled;
                    button.setMessage(Text.literal("Fast Totem: " + (AnchorAssist.fastTotemEnabled ? "ON" : "OFF")));
                })
                .dimensions(centerX - 75, centerY + 20, 150, 20)
                .build()
        );
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
