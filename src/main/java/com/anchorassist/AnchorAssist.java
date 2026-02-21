package com.anchorassist;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.DrawContext;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;

import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;

import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.BlockState;

import net.minecraft.screen.slot.SlotActionType;

import org.lwjgl.glfw.GLFW;

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
    // KEYBINDS (Denis MOD)
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
                new KeyBinding(
                        name,
                        InputUtil.Type.KEYSYM,
                        key,
                        "Denis MOD"
                )
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
            client.setScreen(new DenisScreen());
    }

    private boolean toggle(MinecraftClient client, boolean value, String name) {
        boolean newValue = !value;
        client.player.sendMessage(
                Text.literal(name + ": " + (newValue ? "ON" : "OFF")), true);
        return newValue;
    }

    // =========================
    // GUI 4 KIRI 3 KANAN
    // =========================
    public static class DenisScreen extends Screen {

        protected DenisScreen() {
            super(Text.literal("Denis MOD Settings"));
        }

        @Override
        protected void init() {

            int centerX = width / 2;
            int startY = 80;
            int spacing = 25;

            int leftX = centerX - 110;
            int rightX = centerX + 10;

            int yLeft = startY;
            int yRight = startY;

            // LEFT SIDE (4)
            addToggle(leftX, yLeft, "Auto HIT", () -> autoHitEnabled, v -> autoHitEnabled = v);
            yLeft += spacing;

            addToggle(leftX, yLeft, "Anchor Charge", () -> autoAnchorEnabled, v -> autoAnchorEnabled = v);
            yLeft += spacing;

            addToggle(leftX, yLeft, "Anchor Safe", () -> anchorSafeEnabled, v -> anchorSafeEnabled = v);
            yLeft += spacing;

            addToggle(leftX, yLeft, "Fast Totem", () -> fastTotemEnabled, v -> fastTotemEnabled = v);

            // RIGHT SIDE (3)
            addToggle(rightX, yRight, "Break Shield", () -> autoShieldBreakEnabled, v -> autoShieldBreakEnabled = v);
            yRight += spacing;

            addToggle(rightX, yRight, "Crystal Break", () -> smartCrystalBreakEnabled, v -> smartCrystalBreakEnabled = v);
            yRight += spacing;

            addToggle(rightX, yRight, "Crystal Optimizer",
                    () -> CrystalOptimizer.enabled,
                    v -> CrystalOptimizer.enabled = v);
        }

        private void addToggle(int x, int y, String name,
                               java.util.function.Supplier<Boolean> getter,
                               java.util.function.Consumer<Boolean> setter) {

            addDrawableChild(ButtonWidget.builder(
                    getText(name, getter.get()),
                    b -> {
                        boolean val = !getter.get();
                        setter.accept(val);
                        b.setMessage(getText(name, val));
                    })
                    .dimensions(x, y, 200, 20)
                    .build()
            );
        }

        private Text getText(String name, boolean state) {
            return Text.literal(name + ": ")
                    .append(state
                            ? Text.literal("ON").formatted(Formatting.GREEN)
                            : Text.literal("OFF").formatted(Formatting.RED));
        }

        @Override
        public boolean shouldPause() {
            return false;
        }
    }

    // =========================
    // FEATURES (SAMA SEMUA)
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

    private void handleSmartCrystalBreak(MinecraftClient client) {
        if (!(client.crosshairTarget instanceof EntityHitResult hit)) return;
        if (!(hit.getEntity() instanceof EndCrystalEntity crystal)) return;

        if (client.player.distanceTo(crystal) <= 4.5D &&
                client.player.getAttackCooldownProgress(0.5f) >= 1.0f) {

            client.interactionManager.attackEntity(client.player, crystal);
            client.player.swingHand(Hand.MAIN_HAND);
        }
    }

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

    private void handleAnchorSafe(MinecraftClient mc) {
        if (!(mc.crosshairTarget instanceof BlockHitResult hit)) return;

        BlockPos anchorPos = hit.getBlockPos();
        if (!(mc.world.getBlockState(anchorPos).getBlock() instanceof RespawnAnchorBlock))
            return;

        int charges = mc.world.getBlockState(anchorPos)
                .get(RespawnAnchorBlock.CHARGES);

        if (charges < 1) return;

        int totemSlot = findHotbarItem(Items.TOTEM_OF_UNDYING, mc);
        if (totemSlot != -1)
            mc.player.getInventory().selectedSlot = totemSlot;
    }

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

        int offhandContainer = 45;

        if (client.player.currentScreenHandler.getSlot(offhandContainer)
                .getStack().isEmpty()) {

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
