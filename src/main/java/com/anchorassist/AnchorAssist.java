package com.anchorassist;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

import net.minecraft.text.Text;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;

import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction;

import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.BlockState;

import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import org.lwjgl.glfw.GLFW;

import java.util.concurrent.ThreadLocalRandom;

import walksy.optimizer.CrystalOptimizer;

public class AnchorAssist implements ClientModInitializer {

    // =========================
    // TOGGLES
    // =========================
    public static boolean autoHitEnabled = true;
    public static boolean autoAnchorEnabled = true;
    public static boolean fastTotemEnabled = true;
    public static boolean anchorSafeEnabled = true;
    public static boolean smartCrystalBreakEnabled = true;
    public static boolean autoShieldBreakEnabled = true;
    public static boolean crystalOptimizerEnabled = true;

    // =========================
    // FAST TOTEM SYSTEM
    // =========================
    private int fastTotemDelay = 0;

    private int getRandomDelay() {
        return ThreadLocalRandom.current().nextInt(1, 4); // 1–3 tick
    }

    // =========================
    // KEYBINDS
    // =========================
    private static KeyBinding toggleHitKey;
    private static KeyBinding toggleAnchorKey;
    private static KeyBinding toggleTotemKey;
    private static KeyBinding toggleAnchorSafeKey;
    private static KeyBinding openGuiKey;
    private static KeyBinding smartCrystalKey;
    private static KeyBinding autoShieldKey;
    private static KeyBinding crystalOptimizerKey;

    @Override
    public void onInitializeClient() {

        toggleHitKey = register("Auto HIT", GLFW.GLFW_KEY_UNKNOWN);
        toggleAnchorKey = register("Anchor Charge", GLFW.GLFW_KEY_UNKNOWN);
        toggleTotemKey = register("Fast Totem", GLFW.GLFW_KEY_UNKNOWN);
        toggleAnchorSafeKey = register("Anchor Safe", GLFW.GLFW_KEY_UNKNOWN);
        openGuiKey = register("Open GUI", GLFW.GLFW_KEY_UNKNOWN);
        smartCrystalKey = register("Crystal Break", GLFW.GLFW_KEY_UNKNOWN);
        autoShieldKey = register("Break Shield", GLFW.GLFW_KEY_UNKNOWN);
        crystalOptimizerKey = register("Crystal Optimizer", GLFW.GLFW_KEY_UNKNOWN);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null || client.world == null) return;

            handleToggles(client);

            if (autoHitEnabled) handleAutoHit(client);
            if (autoAnchorEnabled) handleAutoAnchor(client);
            if (anchorSafeEnabled) handleAnchorSafe(client);
            if (fastTotemEnabled) handleFastTotem(client);
            if (smartCrystalBreakEnabled) handleSmartCrystalBreak(client);
            if (autoShieldBreakEnabled) handleAutoShieldBreak(client);

            if (CrystalOptimizer.enabled)
                CrystalOptimizer.onTick();
        });
    }

    private KeyBinding register(String name, int key) {
        return KeyBindingHelper.registerKeyBinding(
                new KeyBinding(name, InputUtil.Type.KEYSYM, key, "BNDTxDen MOD")
        );
    }

    private void handleToggles(MinecraftClient client) {

        while (toggleHitKey.wasPressed())
            autoHitEnabled = toggle(client, autoHitEnabled, "Auto HIT");

        while (toggleAnchorKey.wasPressed())
            autoAnchorEnabled = toggle(client, autoAnchorEnabled, "Anchor Charge");

        while (toggleTotemKey.wasPressed())
            fastTotemEnabled = toggle(client, fastTotemEnabled, "Fast Totem");

        while (toggleAnchorSafeKey.wasPressed())
            anchorSafeEnabled = toggle(client, anchorSafeEnabled, "Anchor Safe");

        while (smartCrystalKey.wasPressed())
            smartCrystalBreakEnabled = toggle(client, smartCrystalBreakEnabled, "Crystal Break");

        while (autoShieldKey.wasPressed())
            autoShieldBreakEnabled = toggle(client, autoShieldBreakEnabled, "Break Shield");

        while (crystalOptimizerKey.wasPressed()) {
            CrystalOptimizer.enabled = !CrystalOptimizer.enabled;
            client.player.sendMessage(
                    Text.literal("Crystal Optimizer: " +
                            (CrystalOptimizer.enabled ? "ON" : "OFF")), true);
        }

        while (openGuiKey.wasPressed())
            client.setScreen(new AnchorAssistScreen());
    }

    private boolean toggle(MinecraftClient client, boolean value, String name) {
        boolean newValue = !value;
        client.player.sendMessage(
                Text.literal(name + ": " + (newValue ? "ON" : "OFF")), true);
        return newValue;
    }

    // =========================
    // AUTO HIT
    // =========================
    private void handleAutoHit(MinecraftClient client) {
        if (client.crosshairTarget instanceof EntityHitResult hit &&
                hit.getEntity() instanceof PlayerEntity target) {

            if (client.player.distanceTo(target) <= 3.1D &&
                    client.player.getAttackCooldownProgress(0.5f) >= 1.0f) {

                client.interactionManager.attackEntity(client.player, target);
                client.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }

    // =========================
    // SMART CRYSTAL BREAK
    // =========================
    private void handleSmartCrystalBreak(MinecraftClient client) {
        if (!(client.crosshairTarget instanceof EntityHitResult hit)) return;
        if (!(hit.getEntity() instanceof EndCrystalEntity crystal)) return;

        if (client.player.distanceTo(crystal) <= 4.5D &&
                client.player.getAttackCooldownProgress(0.5f) >= 1.0f) {

            client.interactionManager.attackEntity(client.player, crystal);
            client.player.swingHand(Hand.MAIN_HAND);
        }
    }

    // =========================
    // AUTO SHIELD BREAK
    // =========================
    private void handleAutoShieldBreak(MinecraftClient client) {

        if (!(client.crosshairTarget instanceof EntityHitResult hit)) return;
        if (!(hit.getEntity() instanceof PlayerEntity target)) return;
        if (!target.isBlocking()) return;

        int axeSlot = findHotbarItem(Items.NETHERITE_AXE, client);
        int swordSlot = findHotbarItem(Items.NETHERITE_SWORD, client);

        if (axeSlot == -1 || swordSlot == -1) return;

        client.player.getInventory().selectedSlot = axeSlot;
        client.interactionManager.attackEntity(client.player, target);
        client.player.swingHand(Hand.MAIN_HAND);
        client.player.getInventory().selectedSlot = swordSlot;
    }

    // =========================
    // AUTO ANCHOR
    // =========================
    private void handleAutoAnchor(MinecraftClient client) {

        if (!(client.crosshairTarget instanceof BlockHitResult hit)) return;

        BlockState state = client.world.getBlockState(hit.getBlockPos());
        if (!(state.getBlock() instanceof RespawnAnchorBlock)) return;
        if (state.get(RespawnAnchorBlock.CHARGES) != 0) return;

        int glowSlot = findHotbarItem(Items.GLOWSTONE, client);
        if (glowSlot == -1) return;

        client.player.getInventory().selectedSlot = glowSlot;
        client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hit);
        client.player.swingHand(Hand.MAIN_HAND);
    }

    // =========================
    // ANCHOR SAFE
    // =========================
    private void handleAnchorSafe(MinecraftClient mc) {

        if (!(mc.crosshairTarget instanceof BlockHitResult hit)) return;

        BlockPos anchorPos = hit.getBlockPos();
        if (!(mc.world.getBlockState(anchorPos).getBlock() instanceof RespawnAnchorBlock))
            return;

        int charges = mc.world.getBlockState(anchorPos)
                .get(RespawnAnchorBlock.CHARGES);
        if (charges < 1) return;

        Vec3d playerPos = mc.player.getPos();
        Vec3d anchorCenter = Vec3d.ofCenter(anchorPos);

        Direction placeDir = Direction.getFacing(
                playerPos.x - anchorCenter.x, 0, playerPos.z - anchorCenter.z);

        if (placeDir.getAxis().isVertical()) return;

        BlockPos safePos = anchorPos.offset(placeDir);
        if (!mc.world.getBlockState(safePos).isAir()) return;
        if (!mc.world.getBlockState(safePos.down()).isSolidBlock(mc.world, safePos.down())) return;

        mc.interactionManager.interactBlock(
                mc.player,
                Hand.MAIN_HAND,
                new BlockHitResult(
                        Vec3d.ofCenter(safePos.down()),
                        Direction.UP,
                        safePos.down(),
                        false
                )
        );

        mc.player.swingHand(Hand.MAIN_HAND);
    }

    // =========================
    // FAST TOTEM (MOUSE PRIORITY – 1.20.1)
    // =========================
    private void handleFastTotem(MinecraftClient client) {

        if (!(client.currentScreen instanceof InventoryScreen)) return;
        if (client.player.currentScreenHandler == null) return;

        if (fastTotemDelay > 0) {
            fastTotemDelay--;
            return;
        }

        int syncId = client.player.currentScreenHandler.syncId;

        int slot7 = 36 + 7;
        int offhand = 45;

        boolean slot7Empty = client.player.currentScreenHandler.getSlot(slot7).getStack().isEmpty();
        boolean offhandEmpty = client.player.currentScreenHandler.getSlot(offhand).getStack().isEmpty();

        if (!slot7Empty && !offhandEmpty) return;

        int nearestTotemSlot = findNearestTotemSlot(client);
        if (nearestTotemSlot == -1) return;

        int targetSlot = slot7Empty ? 7 : 40;

        client.interactionManager.clickSlot(
                syncId,
                nearestTotemSlot,
                targetSlot,
                SlotActionType.SWAP,
                client.player
        );

        fastTotemDelay = getRandomDelay();
    }

    // =========================
    // FIND NEAREST TOTEM SLOT (1.20.1 SAFE)
    // =========================
    private int findNearestTotemSlot(MinecraftClient client) {

        if (!(client.currentScreen instanceof HandledScreen<?> screen)) return -1;

        double mouseX = screen.getMouseX();
        double mouseY = screen.getMouseY();

        int guiLeft = screen.x;
        int guiTop = screen.y;

        double closest = Double.MAX_VALUE;
        int closestSlot = -1;

        for (int i = 9; i <= 35; i++) {
            Slot slot = screen.getScreenHandler().getSlot(i);
            if (slot.getStack().getItem() != Items.TOTEM_OF_UNDYING) continue;

            double x = guiLeft + slot.x + 8;
            double y = guiTop + slot.y + 8;

            double dist = Math.hypot(mouseX - x, mouseY - y);
            if (dist < closest) {
                closest = dist;
                closestSlot = i;
            }
        }
        return closestSlot;
    }

    // =========================
    // HOTBAR FINDER
    // =========================
    private int findHotbarItem(net.minecraft.item.Item item, MinecraftClient client) {
        for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getStack(i).getItem() == item)
                return i;
        }
        return -1;
    }
}
