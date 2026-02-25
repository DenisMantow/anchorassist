package com.anchorassist.assist;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

import java.util.concurrent.ThreadLocalRandom;

public class MacePvPManager {

    // =========================
    // TOGGLES (dikontrol dari AnchorAssist)
    // =========================
    public static boolean macePvPEnabled = true;
    public static boolean pearlComboEnabled = true;

    // =========================
    // INTERNAL DELAY SYSTEM
    // =========================
    private static int attackDelay = 0;
    private static int switchDelay = 0;
    private static boolean returningToMace = false;

    // =========================
    // REGISTER
    // =========================
    public static void register() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null || client.world == null)
                return;

            if (macePvPEnabled)
                handleMacePvP(client);

            if (pearlComboEnabled)
                handlePearlCombo(client);
        });
    }

    // =========================
    // MACE PVP (Slot Based)
    // =========================
    private static void handleMacePvP(MinecraftClient client) {

        if (attackDelay > 0) attackDelay--;
        if (switchDelay > 0) switchDelay--;

        if (!(client.crosshairTarget instanceof EntityHitResult hit))
            return;

        if (!(hit.getEntity() instanceof PlayerEntity target))
            return;

        double distance = client.player.distanceTo(target);
        if (distance > 3.1D)
            return;

        int maceSlot = 0; // Slot 1
        int axeSlot = 6;  // Slot 7

        // =========================
        // TARGET BLOCKING → SWITCH AXE
        // =========================
        if (target.isBlocking()) {

            if (switchDelay == 0) {
                client.player.getInventory().selectedSlot = axeSlot;
                switchDelay = random(2, 4);
                attackDelay = random(2, 4);
                returningToMace = true;
            }

            if (attackDelay == 0 &&
                    client.player.getInventory().selectedSlot == axeSlot) {

                client.interactionManager.attackEntity(client.player, target);
                client.player.swingHand(Hand.MAIN_HAND);
                attackDelay = random(3, 5);
            }

            return;
        }

        // =========================
        // RETURN TO MACE
        // =========================
        if (returningToMace && switchDelay == 0) {
            client.player.getInventory().selectedSlot = maceSlot;
            returningToMace = false;
            switchDelay = random(2, 3);
            return;
        }

        // =========================
        // NORMAL MACE HIT
        // =========================
        if (client.player.getInventory().selectedSlot != maceSlot)
            client.player.getInventory().selectedSlot = maceSlot;

        if (attackDelay == 0 &&
                client.player.getAttackCooldownProgress(0.5f) >= 1.0f) {

            client.interactionManager.attackEntity(client.player, target);
            client.player.swingHand(Hand.MAIN_HAND);
            attackDelay = random(3, 6);
        }
    }

    // =========================
    // PEARL → SLOT 8 INSTANT
    // =========================
    private static void handlePearlCombo(MinecraftClient client) {

        // Detect pearl throw
        if (client.options.useKey.wasPressed() &&
                client.player.getMainHandStack().isOf(Items.ENDER_PEARL)) {

            int previousSlot = client.player.getInventory().selectedSlot;
            int slot8 = 8; // slot terakhir hotbar (index 8)

            if (client.player.getInventory().getStack(slot8).isEmpty())
                return;

            // Switch ke slot 8
            client.player.getInventory().selectedSlot = slot8;

            // Right click item slot 8
            client.interactionManager.interactItem(
                    client.player,
                    Hand.MAIN_HAND
            );

            // Balik ke slot sebelumnya
            client.player.getInventory().selectedSlot = previousSlot;
        }
    }

    // =========================
    // RANDOM DELAY
    // =========================
    private static int random(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
