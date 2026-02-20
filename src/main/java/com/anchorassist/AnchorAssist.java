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

    // NEW
    public static boolean crystalOptimizerV2Enabled = true;

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
    private static KeyBinding crystalOptimizerV2Key; // NEW

    @Override
    public void onInitializeClient() {

        toggleHitKey = register("togglehit", GLFW.GLFW_KEY_R);
        toggleAnchorKey = register("toggleanchor", GLFW.GLFW_KEY_G);
        toggleTotemKey = register("toggletotem", GLFW.GLFW_KEY_T);
        toggleAnchorSafeKey = register("anchorsafe", GLFW.GLFW_KEY_Y);
        openGuiKey = register("opengui", GLFW.GLFW_KEY_RIGHT_SHIFT);
        smartCrystalKey = register("smartcrystal", GLFW.GLFW_KEY_X);
        autoShieldKey = register("autoshield", GLFW.GLFW_KEY_Z);
        crystalOptimizerV2Key = register("crystaloptv2", GLFW.GLFW_KEY_B);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null || client.world == null) return;

            handleToggles(client);

            if (autoHitEnabled) handleAutoHit(client);
            if (autoAnchorEnabled) handleAutoAnchor(client);
            if (anchorSafeEnabled) handleAnchorSafe(client);
            if (fastTotemEnabled) handleFastTotem(client);
            if (smartCrystalBreakEnabled) handleSmartCrystalBreak(client);
            if (autoShieldBreakEnabled) handleAutoShieldBreak(client);
            if (crystalOptimizerV2Enabled) handleCrystalOptimizerV2(client);
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

        while (crystalOptimizerV2Key.wasPressed())
            crystalOptimizerV2Enabled = toggle(client, crystalOptimizerV2Enabled, "Crystal Optimizer V2");

        while (openGuiKey.wasPressed())
            client.setScreen(new AnchorAssistScreen());
    }

    private boolean toggle(MinecraftClient client, boolean value, String name) {
        boolean newValue = !value;
        client.player.sendMessage(Text.literal(name + ": " + (newValue ? "ON" : "OFF")), true);
        return newValue;
    }

    // =========================
    // CRYSTAL OPTIMIZER V2 (MANUAL SMOOTH)
    // =========================
    private void handleCrystalOptimizerV2(MinecraftClient client) {

        if (client.player.getMainHandStack().getItem() != Items.END_CRYSTAL)
            return;

        // Remove ghost crystals instantly
        client.world.getEntities().forEach(entity -> {
            if (entity instanceof EndCrystalEntity crystal) {
                if (!crystal.isAlive()) {
                    crystal.discard();
                }
            }
        });

        // Ignore crystal hitbox blocking placement
        if (client.crosshairTarget instanceof EntityHitResult hit &&
                hit.getEntity() instanceof EndCrystalEntity) {
            client.crosshairTarget = null;
        }

        // Smooth swing sync
        if (client.options.useKey.isPressed()) {
            client.player.swingHand(Hand.MAIN_HAND);
        }
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

        double dx = playerPos.x - anchorCenter.x;
        double dz = playerPos.z - anchorCenter.z;

        Direction placeDir = Direction.getFacing(dx, 0, dz);
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

        int totemSlot = findHotbarItem(Items.TOTEM_OF_UNDYING, mc);
        if (totemSlot != -1) {
            mc.player.getInventory().selectedSlot = totemSlot;
        }
    }

    // =========================
    // FAST TOTEM
    // =========================
    private void handleFastTotem(MinecraftClient client) {

        if (!(client.currentScreen instanceof InventoryScreen)) return;
        if (client.player.currentScreenHandler == null) return;

        int syncId = client.player.currentScreenHandler.syncId;
        int totemSlot = -1;

        for (int i = 9; i <= 35; i++) {
            if (client.player.currentScreenHandler.getSlot(i).getStack().getItem()
                    == Items.TOTEM_OF_UNDYING) {
                totemSlot = i;
                break;
            }
        }

        if (totemSlot == -1) return;

        int slot7Container = 36 + 7;
        int offhandContainer = 45;

        if (client.player.currentScreenHandler.getSlot(slot7Container).getStack().isEmpty()) {

            client.interactionManager.clickSlot(
                    syncId,
                    totemSlot,
                    7,
                    SlotActionType.SWAP,
                    client.player
            );
            return;
        }

        if (client.player.currentScreenHandler.getSlot(offhandContainer).getStack().isEmpty()) {

            client.interactionManager.clickSlot(
                    syncId,
                    totemSlot,
                    40,
                    SlotActionType.SWAP,
                    client.player
            );
        }
    }

    private int findHotbarItem(net.minecraft.item.Item item, MinecraftClient client) {
        for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getStack(i).getItem() == item)
                return i;
        }
        return -1;
    }
}
