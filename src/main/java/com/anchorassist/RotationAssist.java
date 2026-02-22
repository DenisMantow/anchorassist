package com.anchorassist.assist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class RotationAssist {

    // =========================
    // TOGGLE
    // =========================
    public static boolean enabled = false;         // Rotation Assist On/Off
    public static boolean microSnapEnabled = true; // Micro Snap On/Off

    // =========================
    // ROTATION CLAMP (AMAN)
    // =========================
    public static float maxYawPerTick = 18.0f;
    public static float maxPitchPerTick = 12.0f;

    // =========================
    // MICRO SNAP (TERASA & MASUK AKAL)
    // =========================
    public static float microSnapYawMin = 4.0f;
    public static float microSnapYawMax = 4.0f;
    public static float microSnapPitchMin = 3.0f;
    public static float microSnapPitchMax = 5.0f;
    public static float microSnapChance = 1.0f; // 90%
    public static int microSnapDelayMax = 0;     // 0 tick

    // =========================
    // HUD STATUS
    // =========================
    public static boolean isWorking = false;
    public static boolean microSnapTriggered = false;
    public static int hudTimer = 0;

    // =========================
    // INTERNAL
    // =========================
    private static int snapDelay = 0;
    private static final Random random = new Random();

    // =========================
    // MAIN APPLY METHOD
    // =========================
    public static void apply(MinecraftClient mc, Vec3d target) {
        if (!enabled || mc.player == null || target == null) return;

        float[] targetRot = getRotations(mc, target);

        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();

        float yawDiff = MathHelper.wrapDegrees(targetRot[0] - yaw);
        float pitchDiff = targetRot[1] - pitch;

        // =========================
        // ROTATION CLAMP
        // =========================
        float yawChange = clamp(yawDiff, maxYawPerTick);
        float pitchChange = clamp(pitchDiff, maxPitchPerTick);

        mc.player.setYaw(yaw + yawChange);
        mc.player.setPitch(MathHelper.clamp(pitch + pitchChange, -90f, 90f));

        // Set HUD status untuk rotation
        isWorking = Math.abs(yawDiff) > 0.5f || Math.abs(pitchDiff) > 0.5f;
        hudTimer = 10;

        // =========================
        // MICRO SNAP
        // =========================
        if (!microSnapEnabled) return;

        if (snapDelay > 0) {
            snapDelay--;
            return;
        }

        if (random.nextFloat() > microSnapChance) return;

        // Micro snap relatif ke target (bukan acak total)
        float snapYaw = MathHelper.clamp(
                yawDiff * randomRange(0.6f, 1.0f),
                -randomRange(microSnapYawMin, microSnapYawMax),
                 randomRange(microSnapYawMin, microSnapYawMax)
        );

        float snapPitch = MathHelper.clamp(
                pitchDiff * randomRange(0.6f, 1.0f),
                -randomRange(microSnapPitchMin, microSnapPitchMax),
                 randomRange(microSnapPitchMin, microSnapPitchMax)
        );

        mc.player.setYaw(mc.player.getYaw() + snapYaw);
        mc.player.setPitch(MathHelper.clamp(mc.player.getPitch() + snapPitch, -90f, 90f));

        // Set HUD status untuk micro snap
        microSnapTriggered = true;
        hudTimer = 40;

        snapDelay = (microSnapDelayMax == 0)
                ? 0
                : random.nextInt(microSnapDelayMax + 1);
    }

    // =========================
    // HUD TICK (RESET STATUS)
    // =========================
    public static void tickHUD() {
        if (hudTimer > 0) {
            hudTimer--;
        } else {
            isWorking = false;
            microSnapTriggered = false;
        }
    }

    // =========================
    // UTILS
    // =========================
    private static float clamp(float value, float max) {
        return MathHelper.clamp(value, -max, max);
    }

    private static float randomRange(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    private static float[] getRotations(MinecraftClient mc, Vec3d target) {
        Vec3d eyes = mc.player.getEyePos();
        Vec3d diff = target.subtract(eyes);

        double distXZ = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

        float yaw = (float) (Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90f);
        float pitch = (float) (-Math.toDegrees(Math.atan2(diff.y, distXZ)));

        return new float[]{yaw, pitch};
    }
}
