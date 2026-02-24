package com.anchorassist;

import com.anchorassist.assist.RotationAssist;

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

        int buttonWidth = 150;
        int buttonHeight = 20;
        int spacing = 24;

        // ⬅ KIRI
        int leftX = centerX - 170;

        // ➡ KANAN
        int rightX = centerX + 20;

        int yLeft = startY;
        int yRight = startY;

        // =========================
        // KIRI – CORE COMBAT
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
        yLeft += spacing;

        // =========================
        // 🔥 HITBOX MODE (3 STATE)
        // =========================

        this.addDrawableChild(ButtonWidget.builder(
                getHitboxModeText(),
                button -> {

                    switch (AnchorAssist.hitboxMode) {
                        case OFF -> AnchorAssist.hitboxMode = AnchorAssist.HitboxMode.FULL;
                        case FULL -> AnchorAssist.hitboxMode = AnchorAssist.HitboxMode.PITCH;
                        case PITCH -> AnchorAssist.hitboxMode = AnchorAssist.HitboxMode.OFF;
                    }

                    button.setMessage(getHitboxModeText());
                })
                .dimensions(leftX, yLeft, buttonWidth, buttonHeight)
                .build()
        );
        yLeft += spacing;

        // =========================
        // KANAN – ASSIST & OPTIMIZER
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
        yRight += spacing;

        // =========================
        // 🧭 ROTATION ASSIST
        // =========================

        addToggle(rightX, yRight, buttonWidth, buttonHeight,
                "Rotation Assist",
                () -> RotationAssist.enabled,
                v -> RotationAssist.enabled = v);
        yRight += spacing;

        addToggle(rightX, yRight, buttonWidth, buttonHeight,
                "Micro Snap",
                () -> RotationAssist.microSnapEnabled,
                v -> RotationAssist.microSnapEnabled = v);
    }

    // =========================
    // HITBOX MODE TEXT
    // =========================
    private Text getHitboxModeText() {

        return switch (AnchorAssist.hitboxMode) {

            case FULL -> Text.literal("Hitbox Stop: ")
                    .append(Text.literal("FULL")
                            .formatted(Formatting.RED));

            case PITCH -> Text.literal("Hitbox Stop: ")
                    .append(Text.literal("PITCH")
                            .formatted(Formatting.GREEN));

            default -> Text.literal("Hitbox Stop: ")
                    .append(Text.literal("OFF")
                            .formatted(Formatting.GRAY));
        };
    }

    // =========================
    // TOGGLE HELPER
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
                Text.literal("BNDTxDen MOD - Crystal PvP GUI")
                        .formatted(Formatting.AQUA, Formatting.BOLD),
                centerX,
                35,
                0xFFFFFF
        );

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Manual Keybind Config In Minecraft Setting")
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
