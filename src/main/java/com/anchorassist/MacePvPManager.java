package com.anchorassist.assist;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

import java.util.concurrent.ThreadLocalRandom;

public class MacePvPManager {

    // =========================
    // TOGGLES
    // =========================
    public static boolean macePvPEnabled = true;
    public static boolean pearlComboEnabled = true;

    // =========================
    // INTERNAL DELAYS
    // =========================
    private static int attackDelay = 0;
    private static int switchDelay = 0;
    private static boolean returningToMace = false;

    // Pearl combo state
    private static boolean pearlThrown = false;
    private static int pearlDetectDelay = 0;

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
                handlePearlWindCombo(client);
        });
    }

    // =========================
    // MACE PVP (AUTO SLOT DETECT)
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

        int maceSlot = findHotbarItem(client, Items.MACE);
        int axeSlot = findHotbarItem(client, Items.NETHERITE_AXE);

        if (maceSlot == -1)
            return; // Tidak ada mace

        // =========================
        // TARGET BLOCKING → SWITCH AXE
        // =========================
        if (target.isBlocking() && axeSlot != -1) {

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
    // PEARL → WIND CHARGE COMBO
    // =========================
    private static void handlePearlWindCombo(MinecraftClient client) {

        if (pearlDetectDelay > 0)
            pearlDetectDelay--;

        // Detect pearl usage
        if (client.options.useKey.isPressed() &&
                client.player.getMainHandStack().isOf(Items.ENDER_PEARL)) {

            pearlThrown = true;
            pearlDetectDelay = 3;
        }

        if (!pearlThrown || pearlDetectDelay > 0)
            return;

        EnderPearlEntity pearl = null;

        for (var entity : client.world.getEntities()) {
            if (entity instanceof EnderPearlEntity e) {
                if (e.getY() > client.player.getY()) {
                    pearl = e;
                    break;
                }
            }
        }

        if (pearl == null)
            return;

        int windSlot = findHotbarItem(client, Items.WIND_CHARGE);
        if (windSlot == -1)
            return;

        client.player.getInventory().selectedSlot = windSlot;

        double dx = pearl.getX() - client.player.getX();
        double dy = pearl.getY() - client.player.getEyeY();
        double dz = pearl.getZ() - client.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90F);
        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, dist)));

        client.player.setYaw(yaw);
        client.player.setPitch(pitch);

        client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);

        pearlThrown = false;
    }

    // =========================
    // HOTBAR SCAN
    // =========================
    private static int findHotbarItem(MinecraftClient client, Item item) {

        for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getStack(i).isOf(item))
                return i;
        }

        return -1;
    }

    // =========================
    // RANDOM DELAY
    // =========================
    private static int random(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
