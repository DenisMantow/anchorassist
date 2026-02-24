package com.anchorassist.assist;

import com.anchorassist.AnchorAssist;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;

public class HitboxStopManager {

    private static int lockTicks = 0;
    private static float lockedYaw;
    private static float lockedPitch;

    // 🔥 Lebih ringan & legit
    private static final int MAX_LOCK_TICKS = 1;
    private static int cooldown = 0;

    // 🎯 Smooth factor (atur kalau mau lebih lembut)
    private static final float SMOOTH_FACTOR = 0.35f;

    public static void register() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (AnchorAssist.hitboxMode == AnchorAssist.HitboxMode.OFF) return;
            if (client.player == null || client.world == null) return;

            // Cooldown tick down
            if (cooldown > 0) cooldown--;

            // ❌ Jangan aktif saat klik kanan (place block)
            if (client.options.useKey.isPressed()) return;

            // 🔒 Jika sedang lock → smooth dampener (NO SNAP)
            if (lockTicks > 0) {

                float currentYaw = client.player.getYaw();
                float currentPitch = client.player.getPitch();

                if (AnchorAssist.hitboxMode == AnchorAssist.HitboxMode.FULL) {

                    float newYaw = currentYaw + (lockedYaw - currentYaw) * SMOOTH_FACTOR;
                    float newPitch = currentPitch + (lockedPitch - currentPitch) * SMOOTH_FACTOR;

                    client.player.setYaw(newYaw);
                    client.player.setPitch(newPitch);
                }

                if (AnchorAssist.hitboxMode == AnchorAssist.HitboxMode.PITCH) {

                    float newPitch = currentPitch + (lockedPitch - currentPitch) * SMOOTH_FACTOR;
                    client.player.setPitch(newPitch);
                }

                lockTicks--;
                return;
            }

            // ⛔ Jangan aktif kalau masih cooldown
            if (cooldown > 0) return;

            // Harus benar-benar kena entity hitbox
            if (!(client.crosshairTarget instanceof EntityHitResult hit)) return;

            Entity entity = hit.getEntity();

            // Target hanya Player atau Crystal
            if (!(entity instanceof PlayerEntity) &&
                !(entity instanceof EndCrystalEntity)) return;

            // Cek range legal hit
            double distance = client.player.distanceTo(entity);
            double maxRange = entity instanceof EndCrystalEntity ? 4.5 : 3.1;

            if (distance > maxRange) return;

            // 🎯 Crosshair sudah kena hitbox → aktifkan smooth stop
            lockedYaw = client.player.getYaw();
            lockedPitch = client.player.getPitch();
            lockTicks = MAX_LOCK_TICKS;

            // 🔄 Beri jeda supaya tidak spam magnet
            cooldown = 4;
        });
    }
}
