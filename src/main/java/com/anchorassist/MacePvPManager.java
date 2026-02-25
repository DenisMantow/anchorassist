package com.anchorassist.assist;

import com.anchorassist.AnchorAssist;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

import java.util.concurrent.ThreadLocalRandom;

public class MacePvPManager {

    public static boolean macePvPEnabled = true;
    public static boolean autoFlyingEnabled = true;

    private static int attackDelay = 0;
    private static int switchDelay = 0;
    private static boolean returningToMace = false;

    public static void register() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null || client.world == null) return;

            if (autoFlyingEnabled)
                handleAutoFlying(client);

            if (macePvPEnabled)
                handleMacePvP(client);
        });
    }

    // =========================
    // AUTO FLYING
    // =========================
    private static void handleAutoFlying(MinecraftClient client) {

        if (!client.player.isFallFlying()) return;

        if (client.options.useKey.isPressed() &&
                client.player.getMainHandStack().isOf(Items.FIREWORK_ROCKET)) {

            client.player.jump();
        }
    }

    // =========================
    // MACE PVP (Slot Based)
    // =========================
    private static void handleMacePvP(MinecraftClient client) {

        if (attackDelay > 0) attackDelay--;
        if (switchDelay > 0) switchDelay--;

        if (!(client.crosshairTarget instanceof EntityHitResult hit)) return;
        if (!(hit.getEntity() instanceof PlayerEntity target)) return;

        double distance = client.player.distanceTo(target);
        if (distance > 3.1D) return;

        // Slot 0 = Mace
        int maceSlot = 0;

        // Slot 1 = Axe
        int axeSlot = 6;

        // Kalau target blocking → pindah ke axe dulu
        if (target.isBlocking()) {

            if (switchDelay == 0) {
                client.player.getInventory().selectedSlot = axeSlot;
                switchDelay = random(2,4);
                attackDelay = random(2,4);
                returningToMace = true;
            }

            if (attackDelay == 0 &&
                    client.player.getInventory().selectedSlot == axeSlot) {

                client.interactionManager.attackEntity(client.player, target);
                client.player.swingHand(Hand.MAIN_HAND);
                attackDelay = random(3,5);
            }

            return;
        }

        // Balik ke mace setelah axe
        if (returningToMace && switchDelay == 0) {
            client.player.getInventory().selectedSlot = maceSlot;
            returningToMace = false;
            switchDelay = random(2,3);
            return;
        }

        // Attack pakai mace (slot 0)
        if (client.player.getInventory().selectedSlot != maceSlot)
            client.player.getInventory().selectedSlot = maceSlot;

        if (attackDelay == 0 &&
                client.player.getAttackCooldownProgress(0.5f) >= 1.0f) {

            client.interactionManager.attackEntity(client.player, target);
            client.player.swingHand(Hand.MAIN_HAND);
            attackDelay = random(3,6);
        }
    }

    private static int random(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
