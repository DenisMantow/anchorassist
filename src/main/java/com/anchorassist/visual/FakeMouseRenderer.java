package com.anchorassist.visual;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class FakeMouseRenderer {

    private static double x;
    private static double y;
    private static boolean visible = false;

    public static void init() {
        HudRenderCallback.EVENT.register(FakeMouseRenderer::onRender);
    }

    public static void moveTo(double newX, double newY) {
        x = newX;
        y = newY;
        visible = true;
    }

    public static void hide() {
        visible = false;
    }

    private static void onRender(DrawContext context, float tickDelta) {

        if (!visible) return;

        // Gambar kotak kecil putih sebagai fake cursor
        context.fill(
                (int)x,
                (int)y,
                (int)x + 4,
                (int)y + 4,
                0xFFFFFFFF
        );
    }
}
