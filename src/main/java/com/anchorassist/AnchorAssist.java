package com.anchorassist;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

import net.minecraft.text.Text;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;

import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import net.minecraft.screen.slot.SlotActionType;

import org.lwjgl.glfw.GLFW;

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
    // CRYSTAL OPTIMIZER SETTINGS
    // =========================
    private int crystalPlaceDelay = 0;
    private final int MAX_CRYSTAL_DELAY = 4; // 4 ticks (anti cheat safe)

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

        toggleHitKey = register("togglehit", GLFW.GLFW_KEY_R);
        toggleAnchorKey = register("toggleanchor", GLFW.GLFW_KEY_G);
        toggleTotemKey = register("toggletotem", GLFW.GLFW_KEY_T);
        toggleAnchorSafeKey = register("anchorsafe", GLFW.GLFW_KEY_Y);
        openGuiKey = register("opengui", GLFW.GLFW_KEY_RIGHT_SHIFT);

        smartCrystalKey = register("smartcrystal", GLFW.GLFW_KEY_X);
        autoShieldKey = register("autoshield", GLFW.GLFW_KEY_Z);
        crystalOptimizerKey = register("crystaloptimizer", GLFW.GLFW_KEY_C);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null || client.world == null) return;

            handleToggles(client);

            if (autoHitEnabled) handleAutoHit(client);
            if (autoAnchorEnabled) handleAutoAnchor(client);
            if (anchorSafeEnabled) handleAnchorSafe(client);
            if (fastTotemEnabled) handleFastTotem(client);
            if (smartCrystalBreakEnabled) handleSmartCrystalBreak(client);
            if (autoShieldBreakEnabled) handleAutoShieldBreak(client);
            if (crystalOptimizerEnabled) handleCrystalOptimizer(client);
        });
    }

    // =========================
    // KEY REGISTER
    // =========================
    private KeyBinding register(String name, int key) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.anchorassist." + name,
                InputUtil.Type.KEYSYM,
                key,
                "category.anchorassist"
        ));
    }

    private void handleToggles(MinecraftClient client) {

        while (toggleHitKey.wasPressed())
            autoHitEnabled = toggle(client, autoHitEnabled, "Auto Hit");

        while (toggleAnchorKey.wasPressed())
            autoAnchorEnabled = toggle(client, autoAnchorEnabled, "Auto Anchor");

        while (toggleTotemKey.wasPressed())
            fastTotemEnabled = toggle(client, fastTotemEnabled, "Fast Totem");

        while (toggleAnchorSafeKey.wasPressed())
            anchorSafeEnabled = toggle(client, anchorSafeEnabled, "Anchor Safe");

        while (smartCrystalKey.wasPressed())
            smartCrystalBreakEnabled = toggle(client, smartCrystalBreakEnabled, "Smart Crystal Break");

        while (autoShieldKey.wasPressed())
            autoShieldBreakEnabled = toggle(client, autoShieldBreakEnabled, "Auto Shield Break");

        while (crystalOptimizerKey.wasPressed())
            crystalOptimizerEnabled = toggle(client, crystalOptimizerEnabled, "Crystal Optimizer");

        while (openGuiKey.wasPressed())
            client.setScreen(new AnchorAssistScreen());
    }

    private boolean toggle(MinecraftClient client, boolean value, String name) {
        boolean newValue = !value;
        client.player.sendMessage(Text.literal(name + ": " + (newValue ? "ON" : "OFF")), true);
        return newValue;
    }

    // =========================
    // CRYSTAL OPTIMIZER
    // =========================
    private void handleCrystalOptimizer(MinecraftClient client) {

        if (!(client.crosshairTarget instanceof BlockHitResult hit)) return;

        BlockPos pos = hit.getBlockPos();
        BlockState state = client.world.getBlockState(pos);

        if (!state.isOf(Blocks.OBSIDIAN) && !state.isOf(Blocks.BEDROCK))
            return;

        if (!client.world.getBlockState(pos.up()).isAir()) return;
        if (!client.world.getBlockState(pos.up(2)).isAir()) return;

        if (client.player.squaredDistanceTo(Vec3d.ofCenter(pos)) > 25)
            return;

        if (crystalPlaceDelay > 0) {
            crystalPlaceDelay--;
            return;
        }

        int crystalSlot = findHotbarItem(Items.END_CRYSTAL, client);
        if (crystalSlot == -1) return;

        int oldSlot = client.player.getInventory().selectedSlot;

        client.player.getInventory().selectedSlot = crystalSlot;

        client.interactionManager.interactBlock(
                client.player,
                Hand.MAIN_HAND,
                hit
        );

        client.player.swingHand(Hand.MAIN_HAND);

        crystalPlaceDelay = MAX_CRYSTAL_DELAY;

        client.player.getInventory().selectedSlot = oldSlot;
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
    // UTIL
    // =========================
    private int findHotbarItem(net.minecraft.item.Item item, MinecraftClient client) {
        for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getStack(i).getItem() == item)
                return i;
        }
        return -1;
    }

    // AnchorSafe / FastTotem tetap seperti versi kamu sebelumnya
}
