package com.anchorassist.assist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class RotationAssist {

    // =========================
    // TOGGLE
    // =========================
    public static boolean enabled = false;          // Rotation Assist
    public static boolean microSnapEnabled = true;  // Micro Snap

    // =========================
    // ROTATION CLAMP
    // =========================
    public static float maxYawPerTick = 18.0f;
    public static float maxPitchPerTick = 12.0f;

    // =========================
    // MICRO SNAP SETTINGS
    // =========================
    public static float microSnapYawMin = 4.0f;
    public static float microSnapYawMax = 4.0f;
    public static float microSnapPitchMin = 3.0f;
    public static float microSnapPitchMax = 5.0f;
    public static float microSnapChance = 1.0f;
    public static int microSnapDelayMax = 0;

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
    // MAIN APPLY
    // =========================
    public static void apply(MinecraftClient mc, Vec3d target) {

        if (mc.player == null || target == null) return;
        if (!enabled && !microSnapEnabled) return;

        float[] targetRot = getRotations(mc, target);

        float currentYaw = mc.player.getYaw();
        float currentPitch = mc.player.getPitch();

        float yawDiff = MathHelper.wrapDegrees(targetRot[0] - currentYaw);
        float pitchDiff = targetRot[1] - currentPitch;

        // =========================
        // ROTATION ASSIST
        // =========================
        if (enabled) {
            float yawChange = MathHelper.clamp(yawDiff, -maxYawPerTick, maxYawPerTick);
            float pitchChange = MathHelper.clamp(pitchDiff, -maxPitchPerTick, maxPitchPerTick);

            mc.player.setYaw(currentYaw + yawChange);
            mc.player.setPitch(MathHelper.clamp(currentPitch + pitchChange, -90f, 90f));

            isWorking = true;
            hudTimer = 10;
        }

        // =========================
        // MICRO SNAP
        // =========================
        if (!microSnapEnabled) return;

        if (snapDelay > 0) {
            snapDelay--;
            return;
        }

        if (random.nextFloat() > microSnapChance) return;
        System.out.println("Micro snap jalan");

        // Recalculate diff setelah perubahan yaw
        float newYawDiff = MathHelper.wrapDegrees(targetRot[0] - mc.player.getYaw());
        float newPitchDiff = targetRot[1] - mc.player.getPitch();

        float snapYaw = Math.signum(newYawDiff) *
                randomRange(microSnapYawMin, microSnapYawMax);

        float snapPitch = Math.signum(newPitchDiff) *
                randomRange(microSnapPitchMin, microSnapPitchMax);

        mc.player.setYaw(mc.player.getYaw() + snapYaw);
        mc.player.setPitch(MathHelper.clamp(
                mc.player.getPitch() + snapPitch,
                -90f, 90f
        ));

        microSnapTriggered = true;
        hudTimer = 20;

        snapDelay = microSnapDelayMax == 0
                ? 0
                : random.nextInt(microSnapDelayMax + 1);
    }

    // =========================
    // HUD TICK
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
