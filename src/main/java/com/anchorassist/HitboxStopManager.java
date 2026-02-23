package com.anchorassist.assist;

import com.anchorassist.AnchorAssist;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class HitboxStopManager {

    private static int lockTicks = 0;
    private static float lockedYaw;
    private static float lockedPitch;

    private static final int MAX_LOCK_TICKS = 3; // berhenti 3 tick saja

    public static void register() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (!AnchorAssist.hitboxStopEnabled) return;
            if (client.player == null || client.world == null) return;

            // Jika sedang lock → tahan rotation
            if (lockTicks > 0) {
                client.player.setYaw(lockedYaw);
                client.player.setPitch(lockedPitch);
                lockTicks--;
                return;
            }

            HitResult result = client.crosshairTarget;

            if (result instanceof EntityHitResult entityHit) {

                Entity entity = entityHit.getEntity();

                // Target harus player atau crystal
                if (!(entity instanceof PlayerEntity) &&
                    !(entity instanceof EndCrystalEntity)) {
                    return;
                }

                // Cek range hit
                double distance = client.player.distanceTo(entity);

                double maxRange = entity instanceof EndCrystalEntity ? 4.5 : 3.2;

                if (distance > maxRange) return;

                // DI SINI ARTINYA:
                // Crosshair sudah tepat kena hitbox
                // Maka kita lock sebentar

                lockedYaw = client.player.getYaw();
                lockedPitch = client.player.getPitch();
                lockTicks = MAX_LOCK_TICKS;
            }
        });
    }
}
