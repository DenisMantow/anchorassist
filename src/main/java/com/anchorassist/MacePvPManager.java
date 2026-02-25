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
    public static boolean pearlComboEnabled = true;

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

            if (pearlComboEnabled)
                handlePearlCombo(client);
        });
    }

    // =========================
    // AUTO FLYING
    // =========================
    private static void handleAutoFlying(MinecraftClient client) {

        if (!client.player.getInventory().getArmorStack(2).isEmpty()) {

            if (client.options.useKey.isPressed() &&
                    client.player.getMainHandStack().isOf(Items.FIREWORK_ROCKET)) {

                if (!client.player.isFallFlying()) {

                    if (!client.player.isOnGround()) {
                        client.player.startFallFlying();
                    } else {
                        client.player.jump();
                    }

                } else {
                    if (ThreadLocalRandom.current().nextInt(100) < 25) {
                        client.player.jump();
                    }
                }
            }
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

        int maceSlot = 0;   // Slot 1
        int axeSlot = 6;    // Slot 7

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

        if (returningToMace && switchDelay == 0) {
            client.player.getInventory().selectedSlot = maceSlot;
            returningToMace = false;
            switchDelay = random(2,3);
            return;
        }

        if (client.player.getInventory().selectedSlot != maceSlot)
            client.player.getInventory().selectedSlot = maceSlot;

        if (attackDelay == 0 &&
                client.player.getAttackCooldownProgress(0.5f) >= 1.0f) {

            client.interactionManager.attackEntity(client.player, target);
            client.player.swingHand(Hand.MAIN_HAND);
            attackDelay = random(3,6);
        }
    }

    // =========================
    // PEARL → SLOT 8 INSTANT
    // =========================
    private static void handlePearlCombo(MinecraftClient client) {

        if (client.options.useKey.wasPressed() &&
                client.player.getMainHandStack().isOf(Items.ENDER_PEARL)) {

            int previousSlot = client.player.getInventory().selectedSlot;
            int slot8 = 8; // Slot terakhir hotbar

            if (client.player.getInventory().getStack(slot8).isEmpty())
                return;

            // Switch ke slot 8
            client.player.getInventory().selectedSlot = slot8;

            // Instant right click
            client.interactionManager.interactItem(
                    client.player,
                    Hand.MAIN_HAND
            );

            // Balik ke slot sebelumnya
            client.player.getInventory().selectedSlot = previousSlot;
        }
    }

    private static int random(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
