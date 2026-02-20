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

    // =========================
    // INTERNAL
    // =========================
    private boolean lastTickHadTotem = true;

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

    @Override
    public void onInitializeClient() {

        toggleHitKey = registerKey("togglehit", GLFW.GLFW_KEY_R);
        toggleAnchorKey = registerKey("toggleanchor", GLFW.GLFW_KEY_G);
        toggleTotemKey = registerKey("toggletotem", GLFW.GLFW_KEY_T);
        toggleAnchorSafeKey = registerKey("anchorsafe", GLFW.GLFW_KEY_Y);
        openGuiKey = registerKey("opengui", GLFW.GLFW_KEY_RIGHT_SHIFT);

        smartCrystalKey = registerKey("smartcrystal", GLFW.GLFW_KEY_X);
        autoShieldKey = registerKey("autoshield", GLFW.GLFW_KEY_Z);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null || client.world == null) return;

            handleToggles(client);
            handleTotemPopAutoOpen(client);
            handleInventoryTotemHold(client);

            if (autoHitEnabled) handleAutoHit(client);
            if (autoAnchorEnabled) handleAutoAnchor(client);
            if (anchorSafeEnabled) handleAnchorSafe(client);
            if (fastTotemEnabled) handleFastTotem(client);

            if (smartCrystalBreakEnabled) handleSmartCrystalBreak(client);
            if (autoShieldBreakEnabled) handleAutoShieldBreak(client);
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

        while (smartCrystalKey.wasPressed())
            smartCrystalBreakEnabled = toggle(client, smartCrystalBreakEnabled, "Smart Crystal Break");

        while (autoShieldKey.wasPressed())
            autoShieldBreakEnabled = toggle(client, autoShieldBreakEnabled, "Auto Shield Break");

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

        int axeSlot = findItem(Items.NETHERITE_AXE, client);
        int swordSlot = findItem(Items.NETHERITE_SWORD, client);

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

        if (!(client.crosshairTarget instanceof BlockHitResult blockHit)) return;

        BlockState state = client.world.getBlockState(blockHit.getBlockPos());
        if (!(state.getBlock() instanceof RespawnAnchorBlock)) return;
        if (state.get(RespawnAnchorBlock.CHARGES) != 0) return;

        for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getStack(i).getItem() == Items.GLOWSTONE) {

                client.player.getInventory().selectedSlot = i;
                client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, blockHit);
                client.player.swingHand(Hand.MAIN_HAND);
                break;
            }
        }
    }

    // =========================
    // ANCHOR SAFE (FIXED)
    // =========================
    private void handleAnchorSafe(MinecraftClient mc) {

        if (!(mc.crosshairTarget instanceof BlockHitResult blockHit)) return;

        BlockPos anchorPos = blockHit.getBlockPos();

        if (!(mc.world.getBlockState(anchorPos).getBlock() instanceof RespawnAnchorBlock))
            return;

        if (mc.world.getBlockState(anchorPos).get(RespawnAnchorBlock.CHARGES) < 1)
            return;

        Direction dir = Direction.getFacing(
                anchorPos.getX() - mc.player.getBlockPos().getX(),
                0,
                anchorPos.getZ() - mc.player.getBlockPos().getZ()
        );

        BlockPos safePos = anchorPos.offset(dir);

        if (!mc.world.getBlockState(safePos).isAir()) return;
        if (!mc.world.getBlockState(safePos.down()).isSolidBlock(mc.world, safePos.down()))
            return;

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

        switchToTotem(mc);
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
        int offhandSlot = 45;

        if (client.player.currentScreenHandler.getSlot(slot7Container).getStack().isEmpty()) {
            client.interactionManager.clickSlot(syncId, totemSlot, 7, SlotActionType.SWAP, client.player);
            return;
        }

        if (client.player.currentScreenHandler.getSlot(offhandSlot).getStack().isEmpty()) {
            client.interactionManager.clickSlot(syncId, totemSlot, 40, SlotActionType.SWAP, client.player);
        }
    }

    // =========================
    // AUTO HOLD TOTEM WHEN INVENTORY OPEN
    // =========================
    private void handleInventoryTotemHold(MinecraftClient client) {

        if (!(client.currentScreen instanceof InventoryScreen)) return;
        switchToTotem(client);
    }

    // =========================
    // AUTO OPEN INVENTORY ON TOTEM POP
    // =========================
    private void handleTotemPopAutoOpen(MinecraftClient client) {

        boolean hasTotemNow =
                client.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING;

        if (lastTickHadTotem && !hasTotemNow) {
            client.setScreen(new InventoryScreen(client.player));
        }

        lastTickHadTotem = hasTotemNow;
    }

    // =========================
    // SWITCH TO TOTEM
    // =========================
    private void switchToTotem(MinecraftClient client) {

        for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getStack(i).getItem()
                    == Items.TOTEM_OF_UNDYING) {

                client.player.getInventory().selectedSlot = i;
                return;
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
