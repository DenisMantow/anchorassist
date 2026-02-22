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
    // SETTINGS (AMAN)
    // =========================
    public static float maxYawPerTick = 18.0f;
    public static float maxPitchPerTick = 12.0f;

    public static float microSnapMax = 2.0f;
    public static float microSnapChance = 0.7f; // 70%
    public static int microSnapDelayMax = 3;

    // =========================
    private static int snapDelay = 0;
    private static final Random random = new Random();

    // =========================
    // MAIN APPLY
    // =========================
    public static void apply(MinecraftClient mc, Vec3d target) {
        if (!enabled || mc.player == null) return;

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

        // =========================
        // MICRO SNAP
        // =========================
        if (!microSnapEnabled) return;

        if (snapDelay > 0) {
            snapDelay--;
            return;
        }

        if (random.nextFloat() > microSnapChance) return;

        float snapYaw = clamp(
                randomRange(-microSnapMax, microSnapMax),
                microSnapMax
        );

        float snapPitch = clamp(
                randomRange(-microSnapMax, microSnapMax),
                microSnapMax
        );

        mc.player.setYaw(mc.player.getYaw() + snapYaw);
        mc.player.setPitch(
                MathHelper.clamp(mc.player.getPitch() + snapPitch, -90f, 90f)
        );

        snapDelay = random.nextInt(microSnapDelayMax + 1);
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

        float yaw = (float)(Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90f);
        float pitch = (float)(-Math.toDegrees(Math.atan2(diff.y, distXZ)));

        return new float[]{yaw, pitch};
    }
}
