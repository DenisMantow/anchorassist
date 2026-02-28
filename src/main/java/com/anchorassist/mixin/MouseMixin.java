package com.anchorassist.mixin;

import com.anchorassist.FastTotem;
import net.minecraft.client.Mouse;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Shadow private MinecraftClient client;

    @Unique
    private double currentX, currentY;

    @Inject(method = "onCursorPos", at = @At("HEAD"), cancellable = true)
    private void anchorassist$injectCursor(long window, double x, double y, CallbackInfo ci) {

        if (!FastTotem.shouldMoveMouse()) return;

        double targetX = FastTotem.getTargetX();
        double targetY = FastTotem.getTargetY();

        // Smooth movement
        double speed = 0.25; // semakin kecil semakin smooth
        currentX += (targetX - currentX) * speed;
        currentY += (targetY - currentY) * speed;

        GLFW.glfwSetCursorPos(window, currentX, currentY);

        ci.cancel(); // blok default movement
    }
}
