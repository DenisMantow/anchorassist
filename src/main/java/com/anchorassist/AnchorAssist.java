package com.anchorassist;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.BlockState;

import org.lwjgl.glfw.GLFW;

public class AnchorAssist implements ClientModInitializer {

    public static boolean autoHitEnabled = true;
    public static boolean autoAnchorEnabled = true;

    private static KeyBinding toggleHitKey;
    private static KeyBinding toggleAnchorKey;
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

            while (openGuiKey.wasPressed()) {
                client.setScreen(new AnchorAssistScreen());
            }

            if (autoHitEnabled) {
                handleAutoHit(client);
            }

            if (autoAnchorEnabled) {
                handleAutoAnchor(client);
            }

        });
    }

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

    private void handleAutoAnchor(MinecraftClient client) {

        if (!(client.crosshairTarget instanceof BlockHitResult blockHit)) return;

        BlockState state = client.world.getBlockState(blockHit.getBlockPos());

        if (!(state.getBlock() instanceof RespawnAnchorBlock)) return;

        int charges = state.get(RespawnAnchorBlock.CHARGES);

        // HANYA isi jika kosong
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
}
