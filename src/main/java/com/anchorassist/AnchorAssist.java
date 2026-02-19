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
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.BlockState;

import net.minecraft.screen.slot.SlotActionType;

import org.lwjgl.glfw.GLFW;

public class AnchorAssist implements ClientModInitializer {

    public static boolean autoHitEnabled = true;
    public static boolean autoAnchorEnabled = true;
    public static boolean fastTotemEnabled = true;

    private static KeyBinding toggleHitKey;
    private static KeyBinding toggleAnchorKey;
    private static KeyBinding toggleTotemKey;
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

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.anchorassist.opengui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.anchorassist"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null || client.world == null) return;

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

            while (openGuiKey.wasPressed()) {
                client.setScreen(new AnchorAssistScreen());
            }

            if (autoHitEnabled) handleAutoHit(client);
            if (autoAnchorEnabled) handleAutoAnchor(client);
            if (fastTotemEnabled) handleFastTotem(client);
        });
    }

    // =========================
    // AUTO HIT
    // =========================
    private void handleAutoHit(MinecraftClient client) {

        if (client.crosshairTarget instanceof EntityHitResult entityHit) {
            if (entityHit.getEntity() instanceof PlayerEntity target) {

                double distance = client.player.distanceTo(target);

                if (distance <= 3.1D) {
                    if (client.player.getAttackCooldownProgress(0.5f) >= 1.0f) {
                        client.interactionManager.attackEntity(client.player, target);
                        client.player.swingHand(Hand.MAIN_HAND);
                    }
                }
            }
        }
    }

    // =========================
    // AUTO ANCHOR (1 CHARGE ONLY)
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
    // FAST TOTEM
    // Minecraft 1.20.1 Container Index
    //
    // 9–35  = Main Inventory
    // 36–44 = Hotbar (0–8)
    // 45    = Offhand
    //
    // Slot 7 (hotbar) = 36 + 7 = 43
    // =========================
    private void handleFastTotem(MinecraftClient client) {

        if (!(client.currentScreen instanceof InventoryScreen))
            return;

        if (client.player == null)
            return;

        if (client.player.currentScreenHandler == null)
            return;

        int syncId = client.player.currentScreenHandler.syncId;
        int containerTotemSlot = -1;

        // Scan main inventory + hotbar only
        for (int i = 9; i <= 44; i++) {
            if (client.player.currentScreenHandler
                    .getSlot(i)
                    .getStack()
                    .getItem() == Items.TOTEM_OF_UNDYING) {

                containerTotemSlot = i;
                break;
            }
        }

        if (containerTotemSlot == -1)
            return;

        // =====================
        // PRIORITAS 1 → SLOT 7
        // =====================
        int slot7Container = 43;

        if (client.player.currentScreenHandler
                .getSlot(slot7Container)
                .getStack()
                .isEmpty()) {

            client.interactionManager.clickSlot(
                    syncId,
                    containerTotemSlot,
                    7, // hotbar index
                    SlotActionType.SWAP,
                    client.player
            );

            return;
        }

        // =====================
        // PRIORITAS 2 → OFFHAND
        // =====================
        int offhandContainer = 45;

        if (client.player.currentScreenHandler
                .getSlot(offhandContainer)
                .getStack()
                .isEmpty()) {

            client.interactionManager.clickSlot(
                    syncId,
                    containerTotemSlot,
                    40, // offhand button index
                    SlotActionType.SWAP,
                    client.player
            );
        }
    }
}
