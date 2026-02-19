package com.anchorassist;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class AnchorAssistScreen extends Screen {

    private final int PANEL_WIDTH = 230;
    private final int PANEL_HEIGHT = 210;

    private int startX;
    private int startY;

    public AnchorAssistScreen() {
        super(Text.literal("Anchor Assist Settings"));
    }

    @Override
    protected void init() {

        startX = (this.width - PANEL_WIDTH) / 2;
        startY = (this.height - PANEL_HEIGHT) / 2;

        int y = startY + 25;
        int spacing = 26;

        // =========================
        // AUTO HIT
        // =========================
        this.addDrawableChild(createToggleButton(
                "Auto Hit",
                AnchorAssist.autoHitEnabled,
                value -> AnchorAssist.autoHitEnabled = value,
                y
        ));

        y += spacing;

        // =========================
        // AUTO ANCHOR
        // =========================
        this.addDrawableChild(createToggleButton(
                "Auto Anchor",
                AnchorAssist.autoAnchorEnabled,
                value -> AnchorAssist.autoAnchorEnabled = value,
                y
        ));

        y += spacing;

        // =========================
        // ANCHOR SAFE
        // =========================
        this.addDrawableChild(createToggleButton(
                "Anchor Safe",
                AnchorAssist.anchorSafeEnabled,
                value -> AnchorAssist.anchorSafeEnabled = value,
                y
        ));

        y += spacing;

        // =========================
        // FAST TOTEM
        // =========================
        this.addDrawableChild(createToggleButton(
                "Fast Totem",
                AnchorAssist.fastTotemEnabled,
                value -> AnchorAssist.fastTotemEnabled = value,
                y
        ));

        y += spacing;

        // =========================
        // ROTATION ASSIST
        // =========================
        this.addDrawableChild(createToggleButton(
                "Rotation Assist",
                AnchorAssist.rotationAssistEnabled,
                value -> AnchorAssist.rotationAssistEnabled = value,
                y
        ));

        y += spacing + 8;

        // =========================
        // CLOSE BUTTON
        // =========================
        this.addDrawableChild(ButtonWidget.builder(
                Text.literal("Close"),
                button -> this.close()
        ).dimensions(startX + 65, y, 100, 20).build());
    }

    // =========================
    // TOGGLE BUTTON BUILDER
    // =========================
    private ButtonWidget createToggleButton(
            String name,
            boolean currentState,
            java.util.function.Consumer<Boolean> setter,
            int yPosition
    ) {

        return ButtonWidget.builder(
                getToggleText(name, currentState),
                button -> {
                    boolean newState = !getCurrentState(name);
                    setter.accept(newState);
                    button.setMessage(getToggleText(name, newState));
                }
        ).dimensions(startX + 25, yPosition, 180, 20).build();
    }

    // =========================
    // STATE HELPER
    // =========================
    private boolean getCurrentState(String name) {
        return switch (name) {
            case "Auto Hit" -> AnchorAssist.autoHitEnabled;
            case "Auto Anchor" -> AnchorAssist.autoAnchorEnabled;
            case "Anchor Safe" -> AnchorAssist.anchorSafeEnabled;
            case "Fast Totem" -> AnchorAssist.fastTotemEnabled;
            case "Rotation Assist" -> AnchorAssist.rotationAssistEnabled;
            default -> false;
        };
    }

    private Text getToggleText(String name, boolean state) {
        return Text.literal(name + ": " + (state ? "ON" : "OFF"));
    }

    // =========================
    // RENDER
    // =========================
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        this.renderBackground(context);

        // Panel Background
        context.fill(
                startX,
                startY,
                startX + PANEL_WIDTH,
                startY + PANEL_HEIGHT,
                0xCC1E1E1E
        );

        // Title
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                this.width / 2,
                startY + 8,
                0xFFFFFF
        );

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
