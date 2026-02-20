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

    public static boolean autoShieldBreakEnabled = true;
    public static boolean smartCrystalBreakEnabled = true;
    public static boolean smartAnchorBreakEnabled = true;
    public static boolean wTapEnabled = true;

    // =========================
    // KEYBINDS
    // =========================
    private static KeyBinding toggleHitKey;
    private static KeyBinding toggleAnchorKey;
    private static KeyBinding toggleTotemKey;
    private static KeyBinding toggleAnchorSafeKey;
    private static KeyBinding openGuiKey;

    private static KeyBinding autoShieldBreakKey;
    private static KeyBinding smartCrystalBreakKey;
    private static KeyBinding smartAnchorBreakKey;
    private static KeyBinding wTapKey;

    @Override
    public void onInitializeClient() {

        toggleHitKey = registerKey("togglehit", GLFW.GLFW_KEY_R);
        toggleAnchorKey = registerKey("toggleanchor", GLFW.GLFW_KEY_G);
        toggleTotemKey = registerKey("toggletotem", GLFW.GLFW_KEY_T);
        toggleAnchorSafeKey = registerKey("anchorsafe", GLFW.GLFW_KEY_Y);
        openGuiKey = registerKey("opengui", GLFW.GLFW_KEY_RIGHT_SHIFT);

        autoShieldBreakKey = registerKey("autoshieldbreak", GLFW.GLFW_KEY_Z);
        smartCrystalBreakKey = registerKey("smartcrystalbreak", GLFW.GLFW_KEY_X);
        smartAnchorBreakKey = registerKey("smartanchorbreak", GLFW.GLFW_KEY_C);
        wTapKey = registerKey("wtap", GLFW.GLFW_KEY_V);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null || client.world == null) return;

            handleToggles(client);

            // Feature Calls
            if (autoHitEnabled) handleAutoHit(client);
            if (autoAnchorEnabled) handleAutoAnchor(client);
            if (anchorSafeEnabled) handleAnchorSafe(client);
            if (fastTotemEnabled) handleFastTotem(client);

            if (autoShieldBreakEnabled) handleAutoShieldBreak(client);
            if (smartCrystalBreakEnabled) handleSmartCrystalBreak(client);
            if (smartAnchorBreakEnabled) handleSmartAnchorBreak(client);

            if (wTapEnabled && client.player.handSwinging) {
                client.player.setSprinting(false);
                client.player.setSprinting(true);
            }
        });
    }

    private KeyBinding registerKey(String name, int key) {
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

        while (autoShieldBreakKey.wasPressed())
            autoShieldBreakEnabled = toggle(client, autoShieldBreakEnabled, "Auto Shield Break");

        while (smartCrystalBreakKey.wasPressed())
            smartCrystalBreakEnabled = toggle(client, smartCrystalBreakEnabled, "Smart Crystal Break");

        while (smartAnchorBreakKey.wasPressed())
            smartAnchorBreakEnabled = toggle(client, smartAnchorBreakEnabled, "Smart Anchor Break");

        while (wTapKey.wasPressed())
            wTapEnabled = toggle(client, wTapEnabled, "W-Tap Assist");

        while (openGuiKey.wasPressed())
            client.setScreen(new AnchorAssistScreen());
    }

    private boolean toggle(MinecraftClient client, boolean value, String name) {
        boolean newValue = !value;
        client.player.sendMessage(Text.literal(name + ": " + (newValue ? "ON" : "OFF")), true);
        return newValue;
    }

    // =========================
    // AUTO HIT
    // =========================
    private void handleAutoHit(MinecraftClient client) {
        if (client.crosshairTarget instanceof EntityHitResult hit) {
            if (hit.getEntity() instanceof PlayerEntity target) {
                if (client.player.distanceTo(target) <= 3.1D &&
                        client.player.getAttackCooldownProgress(0.5f) >= 1f) {

                    client.interactionManager.attackEntity(client.player, target);
                    client.player.swingHand(Hand.MAIN_HAND);
                }
            }
        }
    }

    // =========================
    // AUTO SHIELD BREAK
    // =========================
    private void handleAutoShieldBreak(MinecraftClient client) {
        if (!(client.crosshairTarget instanceof EntityHitResult hit)) return;
        if (!(hit.getEntity() instanceof PlayerEntity target)) return;
        if (!target.isBlocking()) return;

        int axeSlot = findItem(Items.NETHERITE_AXE, client);
        int swordSlot = findItem(Items.NETHERITE_SWORD, client);

        if (axeSlot == -1 || swordSlot == -1) return;

        client.player.getInventory().selectedSlot = axeSlot;
        client.interactionManager.attackEntity(client.player, target);
        client.player.swingHand(Hand.MAIN_HAND);

        client.player.getInventory().selectedSlot = swordSlot;
    }

    // =========================
    // SMART CRYSTAL BREAK
    // =========================
    private void handleSmartCrystalBreak(MinecraftClient client) {
        if (!(client.crosshairTarget instanceof EntityHitResult hit)) return;
        if (!(hit.getEntity() instanceof EndCrystalEntity crystal)) return;

        if (client.player.distanceTo(crystal) <= 4.5f &&
                client.player.getAttackCooldownProgress(0.5f) >= 1f) {

            client.interactionManager.attackEntity(client.player, crystal);
            client.player.swingHand(Hand.MAIN_HAND);
        }
    }

    // =========================
    // SMART ANCHOR BREAK
    // =========================
    private void handleSmartAnchorBreak(MinecraftClient client) {
        if (!(client.crosshairTarget instanceof BlockHitResult hit)) return;

        BlockState state = client.world.getBlockState(hit.getBlockPos());

        if (state.getBlock() instanceof RespawnAnchorBlock &&
                state.get(RespawnAnchorBlock.CHARGES) > 0) {

            client.interactionManager.interactBlock(
                    client.player,
                    Hand.MAIN_HAND,
                    hit
            );
        }
    }

    // =========================
    // AUTO ANCHOR (1 CHARGE)
    // =========================
    private void handleAutoAnchor(MinecraftClient client) {

        if (!(client.crosshairTarget instanceof BlockHitResult blockHit)) return;

        BlockState state = client.world.getBlockState(blockHit.getBlockPos());
        if (!(state.getBlock() instanceof RespawnAnchorBlock)) return;

        if (state.get(RespawnAnchorBlock.CHARGES) != 0) return;

        int glowSlot = findItem(Items.GLOWSTONE, client);
        if (glowSlot == -1) return;

        client.player.getInventory().selectedSlot = glowSlot;

        client.interactionManager.interactBlock(
                client.player,
                Hand.MAIN_HAND,
                blockHit
        );

        client.player.swingHand(Hand.MAIN_HAND);
    }

    // =========================
    // ANCHOR SAFE
    // =========================
    private void handleAnchorSafe(MinecraftClient mc) {

        if (!(mc.crosshairTarget instanceof BlockHitResult blockHit)) return;

        BlockPos anchorPos = blockHit.getBlockPos();

        if (!(mc.world.getBlockState(anchorPos).getBlock() instanceof RespawnAnchorBlock))
            return;

        if (mc.world.getBlockState(anchorPos).get(RespawnAnchorBlock.CHARGES) < 1)
            return;

        Direction dir = mc.player.getHorizontalFacing();
        BlockPos safePos = anchorPos.offset(dir);

        if (!mc.world.getBlockState(safePos).isAir()) return;
        if (!mc.world.getBlockState(safePos.down()).isSolidBlock(mc.world, safePos.down()))
            return;

        int glowSlot = findItem(Items.GLOWSTONE, mc);
        if (glowSlot == -1) return;

        mc.player.getInventory().selectedSlot = glowSlot;

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
    // FAST TOTEM
    // =========================
    private void handleFastTotem(MinecraftClient client) {

        if (!(client.currentScreen instanceof InventoryScreen)) return;
        if (client.player.currentScreenHandler == null) return;

        int syncId = client.player.currentScreenHandler.syncId;

        for (int i = 9; i <= 44; i++) {
            if (client.player.currentScreenHandler.getSlot(i).getStack().getItem() ==
                    Items.TOTEM_OF_UNDYING) {

                client.interactionManager.clickSlot(
                        syncId,
                        i,
                        40,
                        SlotActionType.SWAP,
                        client.player
                );
                break;
            }
        }
    }

    private int findItem(net.minecraft.item.Item item, MinecraftClient client) {
        for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getStack(i).getItem() == item)
                return i;
        }
        return -1;
    }
}
