package com.anchorassist.visual;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class FakeMouseRenderer {

    private static double x;
    private static double y;
    private static double targetX;
    private static double targetY;
    private static boolean active = false;

    private static final Identifier CURSOR =
            new Identifier("minecraft", "textures/gui/sprites/container/slot.png");

    public static void init() {
        HudRenderCallback.EVENT.register(FakeMouseRenderer::render);
    }

    public static void moveTo(double tx, double ty) {
        targetX = tx;
        targetY = ty;
        active = true;
    }

    public static void stop() {
        active = false;
    }

    private static void render(DrawContext context, float tickDelta) {

        if (!active) return;

        // Smooth movement
        x += (targetX - x) * 0.25;
        y += (targetY - y) * 0.25;

        context.drawTexture(
                CURSOR,
                (int) x,
                (int) y,
                0,
                0,
                16,
                16,
                16,
                16
        );
    }
}
