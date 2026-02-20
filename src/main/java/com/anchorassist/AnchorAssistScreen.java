package com.anchorassist;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AnchorAssistScreen extends Screen {

    protected AnchorAssistScreen() {
        super(Text.literal("Anchor Assist Settings"));
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 26;

        int startY = centerY - spacing * 3;

        // =========================
        // AUTO HIT
        // =========================
        this.addDrawableChild(ButtonWidget.builder(
                getToggleText("Auto Hit", AnchorAssist.autoHitEnabled),
                button -> {
                    AnchorAssist.autoHitEnabled = !AnchorAssist.autoHitEnabled;
                    button.setMessage(getToggleText("Auto Hit", AnchorAssist.autoHitEnabled));
                })
                .dimensions(centerX - buttonWidth / 2, startY, buttonWidth, buttonHeight)
                .build()
        );

        // =========================
        // AUTO ANCHOR
        // =========================
        this.addDrawableChild(ButtonWidget.builder(
                getToggleText("Auto Anchor", AnchorAssist.autoAnchorEnabled),
                button -> {
                    AnchorAssist.autoAnchorEnabled = !AnchorAssist.autoAnchorEnabled;
                    button.setMessage(getToggleText("Auto Anchor", AnchorAssist.autoAnchorEnabled));
                })
                .dimensions(centerX - buttonWidth / 2, startY + spacing, buttonWidth, buttonHeight)
                .build()
        );

        // =========================
        // ANCHOR SAFE
        // =========================
        this.addDrawableChild(ButtonWidget.builder(
                getToggleText("Anchor Safe", AnchorAssist.anchorSafeEnabled),
                button -> {
                    AnchorAssist.anchorSafeEnabled = !AnchorAssist.anchorSafeEnabled;
                    button.setMessage(getToggleText("Anchor Safe", AnchorAssist.anchorSafeEnabled));
                })
                .dimensions(centerX - buttonWidth / 2, startY + spacing * 2, buttonWidth, buttonHeight)
                .build()
        );

        // =========================
        // FAST TOTEM
        // =========================
        this.addDrawableChild(ButtonWidget.builder(
                getToggleText("Fast Totem", AnchorAssist.fastTotemEnabled),
                button -> {
                    AnchorAssist.fastTotemEnabled = !AnchorAssist.fastTotemEnabled;
                    button.setMessage(getToggleText("Fast Totem", AnchorAssist.fastTotemEnabled));
                })
                .dimensions(centerX - buttonWidth / 2, startY + spacing * 3, buttonWidth, buttonHeight)
                .build()
        );

        // =========================
        // D-TAP CRYSTAL
        // =========================
        this.addDrawableChild(ButtonWidget.builder(
                getToggleText("D-Tap Crystal", AnchorAssist.dTapEnabled),
                button -> {
                    AnchorAssist.dTapEnabled = !AnchorAssist.dTapEnabled;
                    button.setMessage(getToggleText("D-Tap Crystal", AnchorAssist.dTapEnabled));
                })
                .dimensions(centerX - buttonWidth / 2, startY + spacing * 4, buttonWidth, buttonHeight)
                .build()
        );

        // =========================
        // CLOSE BUTTON
        // =========================
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Close").formatted(Formatting.GRAY),
                button -> this.close())
                .dimensions(centerX - 50, startY + spacing * 5 + 5, 100, buttonHeight)
                .build()
        );
    }

    // =========================
    // TEXT STYLE
    // =========================
    private Text getToggleText(String name, boolean enabled) {
        return Text.literal(name + ": ")
                .append(enabled
                        ? Text.literal("ON").formatted(Formatting.GREEN)
                        : Text.literal("OFF").formatted(Formatting.RED)
                );
    }

    // =========================
    // RENDER
    // =========================
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Anchor Assist").formatted(Formatting.AQUA, Formatting.BOLD),
                centerX,
                25,
                0xFFFFFF
        );

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("R=Hit | G=Anchor | Y=Safe | T=Totem | U=D-Tap Toggle | V=D-Tap Trigger | Shift=GUI")
                        .formatted(Formatting.GRAY),
                centerX,
                40,
                0xFFFFFF
        );
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
