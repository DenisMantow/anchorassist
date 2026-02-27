package com.anchorassist;

import com.anchorassist.assist.HitboxStopManager;
import com.anchorassist.assist.MacePvPManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
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

import org.lwjgl.glfw.GLFW;

import walksy.optimizer.CrystalOptimizer;

import java.util.concurrent.ThreadLocalRandom;

public class AnchorAssist implements ClientModInitializer {

    // =========================
    // HITBOX STOP MODE
    // =========================
    public enum HitboxMode { OFF, FULL, PITCH }
    public static HitboxMode hitboxMode = HitboxMode.OFF;

    // =========================
    // TOGGLES
    // =========================
    public static boolean autoHitEnabled = true;
    public static boolean autoAnchorEnabled = true;
    public static boolean fastTotemEnabled = true;
    public static boolean anchorSafeEnabled = true;
    public static boolean smartCrystalBreakEnabled = true;
    public static boolean autoShieldBreakEnabled = true;
    public static boolean macePvPEnabled = true;
    public static boolean pearlComboEnabled = true;

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
    private static KeyBinding macePvPKey;
    private static KeyBinding pearlComboKey;

    // =========================
    // AUTO ANCHOR SYSTEM
    // =========================
    private int anchorDelay = 0;
    private int anchorStage = 0;
    private int previousSlot = 0;

    @Override
    public void onInitializeClient() {

        FastTotem.init();
        HitboxStopManager.register();
        MacePvPManager.register();

        toggleHitKey = register("Auto HIT", GLFW.GLFW_KEY_UNKNOWN);
        toggleAnchorKey = register("Anchor Charge", GLFW.GLFW_KEY_UNKNOWN);
        toggleTotemKey = register("Fast Totem", GLFW.GLFW_KEY_UNKNOWN);
        toggleAnchorSafeKey = register("Anchor Safe", GLFW.GLFW_KEY_UNKNOWN);
        openGuiKey = register("Open GUI", GLFW.GLFW_KEY_UNKNOWN);
        smartCrystalKey = register("Crystal Break", GLFW.GLFW_KEY_UNKNOWN);
        autoShieldKey = register("Break Shield", GLFW.GLFW_KEY_UNKNOWN);
        crystalOptimizerKey = register("Crystal Optimizer", GLFW.GLFW_KEY_UNKNOWN);
        macePvPKey = register("Mace PvP", GLFW.GLFW_KEY_UNKNOWN);
        pearlComboKey = register("Pearl Combo", GLFW.GLFW_KEY_UNKNOWN);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null || client.world == null) return;

            handleToggles(client);

            if (autoHitEnabled) handleAutoHit(client);
            if (autoAnchorEnabled) handleAutoAnchor(client);
            if (anchorSafeEnabled) handleAnchorSafe(client);
            if (smartCrystalBreakEnabled) handleSmartCrystalBreak(client);
            if (autoShieldBreakEnabled) handleAutoShieldBreak(client);

            if (CrystalOptimizer.enabled) CrystalOptimizer.onTick();
        });
    }

    // =========================
    // KEY REGISTER
    // =========================
    private KeyBinding register(String name, int key) {
        return KeyBindingHelper.registerKeyBinding(
                new KeyBinding(name, InputUtil.Type.KEYSYM, key, "BNDTxDen MOD")
        );
    }

    // =========================
    // TOGGLES
    // =========================
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

        while (macePvPKey.wasPressed()) {
            macePvPEnabled = toggle(client, macePvPEnabled, "Mace PvP");
            MacePvPManager.macePvPEnabled = macePvPEnabled;
        }
        
        while (pearlComboKey.wasPressed()) {
            pearlComboEnabled = toggle(client, pearlComboEnabled, "Pearl Combo");
             MacePvPManager.pearlComboEnabled = pearlComboEnabled;
        }

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
    // AUTO HIT (FIXED SAFE VERSION)
    // =========================
    private void handleAutoHit(MinecraftClient client) {

        // Jangan jalan kalau GUI terbuka (inventory, chest, dll)
        if (client.currentScreen != null) return;

        if (!(client.crosshairTarget instanceof EntityHitResult hit)) return;
        if (!(hit.getEntity() instanceof PlayerEntity target)) return;

        if (target == client.player) return;
        if (client.player.distanceTo(target) > 3.1D) return;
        if (client.player.getAttackCooldownProgress(0.5f) < 1.0f) return;

        client.interactionManager.attackEntity(client.player, target);
        client.player.swingHand(Hand.MAIN_HAND);
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
    // AUTO ANCHOR (1 TICK DELAY)
    // =========================
    private void handleAutoAnchor(MinecraftClient client) {

        if (anchorDelayTick > 0) return;

        if (!(client.crosshairTarget instanceof BlockHitResult hit)) return;

        BlockState state = client.world.getBlockState(hit.getBlockPos());
        if (!(state.getBlock() instanceof RespawnAnchorBlock)) return;
        if (state.get(RespawnAnchorBlock.CHARGES) != 0) return;

        int glowSlot = findHotbarItem(Items.GLOWSTONE, client);
        if (glowSlot == -1) return;

        client.player.getInventory().selectedSlot = glowSlot;
        client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hit);
        client.player.swingHand(Hand.MAIN_HAND);

        anchorDelayTick = 1; // ~0.3 detik
    }

    // =========================
    // ANCHOR SAFE (1 TICK DELAY)
    // =========================
    private void handleAnchorSafe(MinecraftClient mc) {

        if (safeDelayTick > 0) return;

        if (!(mc.crosshairTarget instanceof BlockHitResult hit)) return;

        BlockPos anchorPos = hit.getBlockPos();
        BlockState state = mc.world.getBlockState(anchorPos);

        if (!(state.getBlock() instanceof RespawnAnchorBlock)) return;
        if (state.get(RespawnAnchorBlock.CHARGES) < 1) return;

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

        safeDelayTick = 1; //
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
