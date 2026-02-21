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
        int startY = 95;

        int buttonWidth = 170;
        int buttonHeight = 20;
        int spacing = 26;

        int leftX = centerX - 190;
        int rightX = centerX + 20;

        // =========================
        // LEFT SIDE (4)
        // =========================

        addToggle(leftX, startY + spacing * 0, buttonWidth, buttonHeight,
                "Auto Hit",
                () -> AnchorAssist.autoHitEnabled,
                v -> AnchorAssist.autoHitEnabled = v);

        addToggle(leftX, startY + spacing * 1, buttonWidth, buttonHeight,
                "Auto Anchor",
                () -> AnchorAssist.autoAnchorEnabled,
                v -> AnchorAssist.autoAnchorEnabled = v);

        addToggle(leftX, startY + spacing * 2, buttonWidth, buttonHeight,
                "Anchor Safe",
                () -> AnchorAssist.anchorSafeEnabled,
                v -> AnchorAssist.anchorSafeEnabled = v);

        addToggle(leftX, startY + spacing * 3, buttonWidth, buttonHeight,
                "Fast Totem",
                () -> AnchorAssist.fastTotemEnabled,
                v -> AnchorAssist.fastTotemEnabled = v);

        // =========================
        // RIGHT SIDE (3)
        // =========================

        addToggle(rightX, startY + spacing * 0, buttonWidth, buttonHeight,
                "Auto Shield Break",
                () -> AnchorAssist.autoShieldBreakEnabled,
                v -> AnchorAssist.autoShieldBreakEnabled = v);

        addToggle(rightX, startY + spacing * 1, buttonWidth, buttonHeight,
                "Smart Crystal Break",
                () -> AnchorAssist.smartCrystalBreakEnabled,
                v -> AnchorAssist.smartCrystalBreakEnabled = v);

        addToggle(rightX, startY + spacing * 2, buttonWidth, buttonHeight,
                "Crystal Optimizer v2",
                () -> AnchorAssist.crystalOptimizerV2Enabled,
                v -> AnchorAssist.crystalOptimizerV2Enabled = v);
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

    // =========================
    // ON/OFF STYLE
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
                30,
                0xFFFFFF
        );

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("KEYBINDS")
                        .formatted(Formatting.YELLOW, Formatting.BOLD),
                centerX,
                48,
                0xFFFFFF
        );

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("R = Hit | G = Anchor | Y = Safe | T = Totem")
                        .formatted(Formatting.GRAY),
                centerX,
                62,
                0xFFFFFF
        );

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Z = Shield | X = Crystal Break | B = Optimizer")
                        .formatted(Formatting.GRAY),
                centerX,
                72,
                0xFFFFFF
        );

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("Right Shift = Open GUI")
                        .formatted(Formatting.DARK_GRAY),
                centerX,
                84,
                0xFFFFFF
        );
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}    return false;
    }
}
