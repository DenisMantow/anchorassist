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
        int startY = 80;

        int buttonWidth = 200;
        int buttonHeight = 20;
        int spacing = 24;

        int y = startY;

        // =========================
        // CORE FEATURES
        // =========================

        addToggle(centerX, y, buttonWidth, buttonHeight,
                "Auto Hit",
                () -> AnchorAssist.autoHitEnabled,
                v -> AnchorAssist.autoHitEnabled = v);
        y += spacing;

        addToggle(centerX, y, buttonWidth, buttonHeight,
                "Auto Anchor",
                () -> AnchorAssist.autoAnchorEnabled,
                v -> AnchorAssist.autoAnchorEnabled = v);
        y += spacing;

        addToggle(centerX, y, buttonWidth, buttonHeight,
                "Anchor Safe",
                () -> AnchorAssist.anchorSafeEnabled,
                v -> AnchorAssist.anchorSafeEnabled = v);
        y += spacing;

        addToggle(centerX, y, buttonWidth, buttonHeight,
                "Fast Totem",
                () -> AnchorAssist.fastTotemEnabled,
                v -> AnchorAssist.fastTotemEnabled = v);
        y += spacing;

        // =========================
        // PVP FEATURES
        // =========================

        addToggle(centerX, y, buttonWidth, buttonHeight,
                "Auto Shield Break",
                () -> AnchorAssist.autoShieldBreakEnabled,
                v -> AnchorAssist.autoShieldBreakEnabled = v);
        y += spacing;

        addToggle(centerX, y, buttonWidth, buttonHeight,
                "Smart Crystal Break",
                () -> AnchorAssist.smartCrystalBreakEnabled,
                v -> AnchorAssist.smartCrystalBreakEnabled = v);

        addToggle(centerX, y, buttonWidth, buttonHeight,
                "Crystal Optimizer",
                () -> walksy.optimizer.CrystalOptimizer.enabled,
                v -> walksy.optimizer.CrystalOptimizer.enabled = v);
    }

    // =========================
    // TOGGLE BUILDER
    // =========================
    private void addToggle(int centerX, int y, int width, int height,
                           String name,
                           java.util.function.Supplier<Boolean> getter,
                           java.util.function.Consumer<Boolean> setter) {

        this.addDrawableChild(ButtonWidget.builder(
                getToggleText(name, getter.get()),
                button -> {
                    boolean newValue = !getter.get();
                    setter.accept(newValue);
                    button.setMessage(getToggleText(name, newValue));
                })
                .dimensions(centerX - width / 2, y, width, height)
                .build()
        );
    }

    // =========================
    // MODERN ON/OFF STYLE
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
                Text.literal("Anchor Assist PvP Module")
                        .formatted(Formatting.AQUA, Formatting.BOLD),
                centerX,
                35,
                0xFFFFFF
        );

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("R=Hit | G=Anchor | Y=Safe | T=Totem | Z=Shield | X=Crystal | Shift=GUI")
                        .formatted(Formatting.GRAY),
                centerX,
                50,
                0xFFFFFF
        );
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
