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

    // =========================
    // KEYBINDS
    // =========================
    private static KeyBinding toggleHitKey;
    private static KeyBinding toggleAnchorKey;
    private static KeyBinding toggleTotemKey;
    private static KeyBinding toggleAnchorSafeKey;
    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {

        toggleHitKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.anchorassist.togglehit",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.anchorassist"
        ));

        toggleAnchorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.anchorassist.toggleanchor",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.anchorassist"
        ));

        toggleTotemKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.anchorassist.toggletotem",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_T,
                "category.anchorassist"
        ));

        toggleAnchorSafeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.anchorassist.anchorsafe",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
                "category.anchorassist"
        ));

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.anchorassist.opengui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.anchorassist"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null || client.world == null) return;

            // =========================
            // TOGGLE KEYS
            // =========================
            while (toggleHitKey.wasPressed()) {
                autoHitEnabled = !autoHitEnabled;
                client.player.sendMessage(Text.literal("Auto Hit: " + (autoHitEnabled ? "ON" : "OFF")), true);
            }

            while (toggleAnchorKey.wasPressed()) {
                autoAnchorEnabled = !autoAnchorEnabled;
                client.player.sendMessage(Text.literal("Auto Anchor: " + (autoAnchorEnabled ? "ON" : "OFF")), true);
            }

            while (toggleTotemKey.wasPressed()) {
                fastTotemEnabled = !fastTotemEnabled;
                client.player.sendMessage(Text.literal("Fast Totem: " + (fastTotemEnabled ? "ON" : "OFF")), true);
            }

            while (toggleAnchorSafeKey.wasPressed()) {
                anchorSafeEnabled = !anchorSafeEnabled;
                client.player.sendMessage(Text.literal("Anchor Safe: " + (anchorSafeEnabled ? "ON" : "OFF")), true);
            }

            while (openGuiKey.wasPressed()) {
                client.setScreen(new AnchorAssistScreen());
            }

            // =========================
            // FEATURE CALLS
            // =========================
            if (autoHitEnabled) handleAutoHit(client);
            if (autoAnchorEnabled) handleAutoAnchor(client);
            if (anchorSafeEnabled) handleAnchorSafe(client);
            if (fastTotemEnabled) handleFastTotem(client);
        });
    }

    // =========================
    // AUTO HIT
    // =========================
    private void handleAutoHit(MinecraftClient client) {

        if (client.crosshairTarget instanceof EntityHitResult entityHit) {
            if (entityHit.getEntity() instanceof PlayerEntity target) {

                if (client.player.distanceTo(target) <= 3.1D) {
                    if (client.player.getAttackCooldownProgress(0.5f) >= 1.0f) {
                        client.interactionManager.attackEntity(client.player, target);
                        client.player.swingHand(Hand.MAIN_HAND);
                    }
                }
            }
        }
    }

    // =========================
    // AUTO ANCHOR (1 CHARGE)
    // =========================
    private void handleAutoAnchor(MinecraftClient client) {

        if (!(client.crosshairTarget instanceof BlockHitResult blockHit)) return;

        BlockState state = client.world.getBlockState(blockHit.getBlockPos());

        if (!(state.getBlock() instanceof RespawnAnchorBlock)) return;

        int charges = state.get(RespawnAnchorBlock.CHARGES);

        if (charges != 0) return;

        for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getStack(i).getItem() == Items.GLOWSTONE) {

                client.player.getInventory().selectedSlot = i;

                client.interactionManager.interactBlock(
                        client.player,
                        Hand.MAIN_HAND,
                        blockHit
                );

                client.player.swingHand(Hand.MAIN_HAND);
                break;
            }
        }
    }

    // =========================
    // ANCHOR SAFE + AUTO TOTEM
    // =========================
    private void handleAnchorSafe(MinecraftClient mc) {

        if (!(mc.crosshairTarget instanceof BlockHitResult blockHit)) return;

        BlockPos anchorPos = blockHit.getBlockPos();

        if (!(mc.world.getBlockState(anchorPos).getBlock() instanceof RespawnAnchorBlock))
            return;

        int charges = mc.world.getBlockState(anchorPos)
                .get(RespawnAnchorBlock.CHARGES);

        if (charges < 1)
            return;

        if (mc.player.getMainHandStack().getItem() != Items.GLOWSTONE)
            return;

        Vec3d playerPos = mc.player.getPos();
        Vec3d anchorCenter = Vec3d.ofCenter(anchorPos);

        double dx = playerPos.x - anchorCenter.x;
        double dz = playerPos.z - anchorCenter.z;

        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance < 1.2 || distance > 3.0)
            return;

        Direction dir = Direction.getFacing(dx, 0, dz);
        if (dir.getAxis().isVertical()) return;

        BlockPos safePos = anchorPos.offset(dir);

        if (!mc.world.getBlockState(safePos).isAir())
            return;

        BlockPos floor = safePos.down();

        if (!mc.world.getBlockState(floor).isSolidBlock(mc.world, floor))
            return;

        // PLACE SAFE BLOCK
        mc.interactionManager.interactBlock(
                mc.player,
                Hand.MAIN_HAND,
                new BlockHitResult(
                        Vec3d.ofCenter(floor),
                        Direction.UP,
                        floor,
                        false
                )
        );

        mc.player.swingHand(Hand.MAIN_HAND);

        // SWITCH TO TOTEM (PRIORITAS SLOT 7)
        if (mc.player.getInventory().getStack(7).getItem() == Items.TOTEM_OF_UNDYING) {
            mc.player.getInventory().selectedSlot = 7;
            return;
        }

        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                mc.player.getInventory().selectedSlot = i;
                break;
            }
        }
    }

    // =========================
    // FAST TOTEM (1.20.1)
    // =========================
    private void handleFastTotem(MinecraftClient client) {

        if (!(client.currentScreen instanceof InventoryScreen)) return;
        if (client.player.currentScreenHandler == null) return;

        int syncId = client.player.currentScreenHandler.syncId;
        int containerTotemSlot = -1;

        for (int i = 9; i <= 44; i++) {
            if (client.player.currentScreenHandler
                    .getSlot(i)
                    .getStack()
                    .getItem() == Items.TOTEM_OF_UNDYING) {

                containerTotemSlot = i;
                break;
            }
        }

        if (containerTotemSlot == -1) return;

        int slot7Container = 43;

        if (client.player.currentScreenHandler
                .getSlot(slot7Container)
                .getStack()
                .isEmpty()) {

            client.interactionManager.clickSlot(
                    syncId,
                    containerTotemSlot,
                    7,
                    SlotActionType.SWAP,
                    client.player
            );
            return;
        }

        int offhandContainer = 45;

        if (client.player.currentScreenHandler
                .getSlot(offhandContainer)
                .getStack()
                .isEmpty()) {

            client.interactionManager.clickSlot(
                    syncId,
                    containerTotemSlot,
                    40,
                    SlotActionType.SWAP,
                    client.player
            );
        }
    }
}
