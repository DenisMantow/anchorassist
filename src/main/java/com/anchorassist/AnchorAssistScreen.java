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

        int buttonWidth = 150;   // ⬅ diperbesar
        int buttonHeight = 20;
        int spacing = 24;

        // ⬅⬅ Geser kiri
        int leftX = centerX - 240;

        // ➡➡ Geser kanan
        int rightX = centerX + 20;

        int yLeft = startY;
        int yRight = startY;

        // =========================
        // 4 KIRI
        // =========================

        addToggle(leftX, yLeft, buttonWidth, buttonHeight,
                "Auto Hit",
                () -> AnchorAssist.autoHitEnabled,
                v -> AnchorAssist.autoHitEnabled = v);
        yLeft += spacing;

        addToggle(leftX, yLeft, buttonWidth, buttonHeight,
                "Auto Anchor",
                () -> AnchorAssist.autoAnchorEnabled,
                v -> AnchorAssist.autoAnchorEnabled = v);
        yLeft += spacing;

        addToggle(leftX, yLeft, buttonWidth, buttonHeight,
                "Anchor Safe",
                () -> AnchorAssist.anchorSafeEnabled,
                v -> AnchorAssist.anchorSafeEnabled = v);
        yLeft += spacing;

        addToggle(leftX, yLeft, buttonWidth, buttonHeight,
                "Fast Totem",
                () -> AnchorAssist.fastTotemEnabled,
                v -> AnchorAssist.fastTotemEnabled = v);

        // =========================
        // 3 KANAN
        // =========================

        addToggle(rightX, yRight, buttonWidth, buttonHeight,
                "Auto Shield Break",
                () -> AnchorAssist.autoShieldBreakEnabled,
                v -> AnchorAssist.autoShieldBreakEnabled = v);
        yRight += spacing;

        addToggle(rightX, yRight, buttonWidth, buttonHeight,
                "Smart Crystal Break",
                () -> AnchorAssist.smartCrystalBreakEnabled,
                v -> AnchorAssist.smartCrystalBreakEnabled = v);
        yRight += spacing;

        addToggle(rightX, yRight, buttonWidth, buttonHeight,
                "Crystal Optimizer",
                () -> walksy.optimizer.CrystalOptimizer.enabled,
                v -> walksy.optimizer.CrystalOptimizer.enabled = v);
    }

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
