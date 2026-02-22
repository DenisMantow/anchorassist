package com.anchorassist.assist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class RotationAssist {

    // =========================
    // TOGGLE
    // =========================
    public static boolean enabled = false;
    public static boolean microSnapEnabled = true;

    // =========================
    // ROTATION CLAMP (AMAN)
    // =========================
    public static float maxYawPerTick = 18.0f;
    public static float maxPitchPerTick = 12.0f;

    // =========================
    // MICRO SNAP (DEMO / TERASA)
    // =========================
    public static float microSnapYawMin = 2.0f;
    public static float microSnapYawMax = 3.0f;

    public static float microSnapPitchMin = 1.5f;
    public static float microSnapPitchMax = 2.5f;

    public static float microSnapChance = 0.90f; // 90%
    public static int microSnapDelayMax = 0;     // 0 tick (biar kerasa)

    // =========================
    private static int snapDelay = 0;
    private static final Random random = new Random();

    // =========================
    // MAIN APPLY
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
        mc.player.setPitch(
                MathHelper.clamp(pitch + pitchChange, -90f, 90f)
        );

        // =========================
        // MICRO SNAP (TERASA & MASUK AKAL)
        // =========================
        if (!microSnapEnabled) return;

        if (snapDelay > 0) {
            snapDelay--;
            return;
        }

        if (random.nextFloat() > microSnapChance) return;

        // Snap ke arah target (bukan acak)
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
        mc.player.setPitch(
                MathHelper.clamp(mc.player.getPitch() + snapPitch, -90f, 90f)
        );

        snapDelay = (microSnapDelayMax == 0)
                ? 0
                : random.nextInt(microSnapDelayMax + 1);
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
