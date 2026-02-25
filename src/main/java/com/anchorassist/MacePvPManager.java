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
    // TOGGLES (diakses dari AnchorAssist)
    // =========================
    public static boolean macePvPEnabled = true;
    public static boolean autoFlyingEnabled = true;

    // =========================
    // INTERNAL STATE
    // =========================
    private static int maceStage = 0;
    private static int maceDelay = 0;
    private static int previousSlot = -1;

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
    // AUTO FLYING (ELYTRA BOOST ASSIST)
    // =========================
    private static void handleAutoFlying(MinecraftClient client) {

        if (!client.player.isFallFlying()) return;

        if (!client.options.useKey.isPressed()) return;

        if (!client.player.getMainHandStack().isOf(Items.FIREWORK_ROCKET))
            return;

        // Jump kecil supaya firework aktif smooth
        if (client.player.isOnGround()) {
            client.player.jump();
        }
    }

    // =========================
    // MACE PVP SYSTEM
    // =========================
    private static void handleMacePvP(MinecraftClient client) {

        if (maceDelay > 0) {
            maceDelay--;
            return;
        }

        if (!(client.crosshairTarget instanceof EntityHitResult hit)) return;
        if (!(hit.getEntity() instanceof PlayerEntity target)) return;

        // Harus pegang mace
        if (!client.player.getMainHandStack().isOf(Items.MACE)) return;

        // Harus sedang jatuh / dive
        if (client.player.isOnGround()) return;

        double distance = client.player.distanceTo(target);
        if (distance > 3.1D) return;

        switch (maceStage) {

            // =========================
            // STAGE 0 - NORMAL MACE HIT
            // =========================
            case 0 -> {

                if (target.isBlocking()) {

                    int axeSlot = findHotbarItem(Items.NETHERITE_AXE, client);
                    if (axeSlot == -1) return;

                    previousSlot = client.player.getInventory().selectedSlot;
                    client.player.getInventory().selectedSlot = axeSlot;

                    maceDelay = randomDelay(1, 2);
                    maceStage = 1;
                    return;
                }

                if (client.player.getAttackCooldownProgress(0.5f) >= 1.0f) {
                    client.interactionManager.attackEntity(client.player, target);
                    client.player.swingHand(Hand.MAIN_HAND);

                    maceDelay = randomDelay(2, 3);
                }
            }

            // =========================
            // STAGE 1 - BREAK SHIELD
            // =========================
            case 1 -> {

                client.interactionManager.attackEntity(client.player, target);
                client.player.swingHand(Hand.MAIN_HAND);

                maceDelay = randomDelay(2, 3);
                maceStage = 2;
            }

            // =========================
            // STAGE 2 - SWAP BACK
            // =========================
            case 2 -> {

                client.player.getInventory().selectedSlot = previousSlot;

                maceDelay = randomDelay(1, 2);
                maceStage = 3;
            }

            // =========================
            // STAGE 3 - FINAL MACE HIT
            // =========================
            case 3 -> {

                if (client.player.getAttackCooldownProgress(0.5f) >= 1.0f) {
                    client.interactionManager.attackEntity(client.player, target);
                    client.player.swingHand(Hand.MAIN_HAND);
                }

                maceStage = 0;
            }
        }
    }

    // =========================
    // HOTBAR FINDER
    // =========================
    private static int findHotbarItem(net.minecraft.item.Item item, MinecraftClient client) {
        for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getStack(i).getItem() == item)
                return i;
        }
        return -1;
    }

    // =========================
    // RANDOM DELAY (ANTI ROBOTIC)
    // =========================
    private static int randomDelay(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
