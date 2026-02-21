package com.anchorassist;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AnchorAssistScreen extends Screen {

    protected AnchorAssistScreen() {
        super(Text.literal("Denis MOD"));
    }

    @Override
    protected void init() {

        int centerX = this.width / 2;
        int leftX = centerX - 110;
        int rightX = centerX + 10;

        int startY = 80;
        int spacing = 24;

        int buttonWidth = 100;
        int buttonHeight = 20;

        int leftY = startY;
        int rightY = startY;

        // =========================
        // LEFT COLUMN (4)
        // =========================

        addToggle(leftX, leftY, buttonWidth, buttonHeight,
                "Auto HIT",
                () -> AnchorAssist.autoHitEnabled,
                v -> AnchorAssist.autoHitEnabled = v);
        leftY += spacing;

        addToggle(leftX, leftY, buttonWidth, buttonHeight,
                "Anchor Charge",
                () -> AnchorAssist.autoAnchorEnabled,
                v -> AnchorAssist.autoAnchorEnabled = v);
        leftY += spacing;

        addToggle(leftX, leftY, buttonWidth, buttonHeight,
                "Anchor Safe",
                () -> AnchorAssist.anchorSafeEnabled,
                v -> AnchorAssist.anchorSafeEnabled = v);
        leftY += spacing;

        addToggle(leftX, leftY, buttonWidth, buttonHeight,
                "Fast Totem",
                () -> AnchorAssist.fastTotemEnabled,
                v -> AnchorAssist.fastTotemEnabled = v);

        // =========================
        // RIGHT COLUMN (3)
        // =========================

        addToggle(rightX, rightY, buttonWidth, buttonHeight,
                "Break Shield",
                () -> AnchorAssist.autoShieldBreakEnabled,
                v -> AnchorAssist.autoShieldBreakEnabled = v);
        rightY += spacing;

        addToggle(rightX, rightY, buttonWidth, buttonHeight,
                "Crystal Break",
                () -> AnchorAssist.smartCrystalBreakEnabled,
                v -> AnchorAssist.smartCrystalBreakEnabled = v);
        rightY += spacing;

        addToggle(rightX, rightY, buttonWidth, buttonHeight,
                "Crystal Optimizer",
                () -> walksy.optimizer.CrystalOptimizer.enabled,
                v -> walksy.optimizer.CrystalOptimizer.enabled = v);
    }

    // =========================
    // TOGGLE BUILDER
    // =========================
    private void addToggle(int x, int y, int width, int height,
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
                .dimensions(x, y, width, height)
                .build()
        );
    }

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
                Text.literal("Denis MOD - PvP Module")
                        .formatted(Formatting.AQUA, Formatting.BOLD),
                centerX,
                35,
                0xFFFFFF
        );

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Manual Keybind Config in Minecraft Settings")
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
