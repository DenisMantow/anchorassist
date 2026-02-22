package com.anchorassist;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class FastTotem {

    // =========================
    // KEYBIND
    // =========================
    public static KeyBinding fastTotemKey;

    // =========================
    // CLICK STATE
    // =========================
    private enum State { IDLE, WAIT_FIRST, WAIT_SECOND }

    private static State state = State.IDLE;
    private static int delay = 0;

    private static int sourceSlot = -1;
    private static int firstTarget = -1;
    private static int secondTarget = -1;

    private static final Random random = new Random();

    // =========================
    // INIT
    // =========================
    public static void init() {
        fastTotemKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "Click Fast Totem",
                        GLFW.GLFW_KEY_UNKNOWN,
                        "BNDTxDen MOD"
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(FastTotem::onTick);
    }

    // =========================
    // MAIN TICK
    // =========================
    private static void onTick(MinecraftClient client) {
        if (client.player == null) return;

        handleClickFastTotem(client);
        handleAutoFastTotem(client);
    }

    // =========================
    // CLICK FAST TOTEM
    // =========================
    private static void handleClickFastTotem(MinecraftClient client) {
        if (!AnchorAssist.clickFastTotemEnabled) return;
        if (!fastTotemKey.wasPressed()) return;
        if (!(client.currentScreen instanceof InventoryScreen)) return;

        // SLOT YANG DIPEGANG CURSOR
        if (client.player.currentScreenHandler.getCursorStack().getItem()
                != Items.TOTEM_OF_UNDYING) return;

        // Cari slot inventory yang berisi totem
        sourceSlot = findAnyTotemSlot(client);
        if (sourceSlot == -1) return;

        int slot7 = 7;
        int offhand = 40;

        boolean slot7Empty = client.player.getInventory().getStack(slot7).isEmpty();
        boolean offhandEmpty = client.player.getOffHandStack().isEmpty();

        if (!slot7Empty && !offhandEmpty) return;

        if (slot7Empty && offhandEmpty) {
            if (random.nextBoolean()) {
                firstTarget = slot7;
                secondTarget = offhand;
            } else {
                firstTarget = offhand;
                secondTarget = slot7;
            }
        } else if (slot7Empty) {
            firstTarget = slot7;
            secondTarget = -1;
        } else {
            firstTarget = offhand;
            secondTarget = -1;
        }

        delay = randomDelay();
        state = State.WAIT_FIRST;

        client.player.sendMessage(Text.literal("Click Fast Totem"), true);
    }

    // =========================
    // AUTO FAST TOTEM
    // =========================
    private static void handleAutoFastTotem(MinecraftClient client) {
        if (!AnchorAssist.fastTotemEnabled) return;
        if (!(client.currentScreen instanceof InventoryScreen)) return;
        if (state != State.IDLE) return;

        int slot7 = 7;
        int offhand = 40;

        boolean slot7Empty = client.player.getInventory().getStack(slot7).isEmpty();
        boolean offhandEmpty = client.player.getOffHandStack().isEmpty();

        if (!slot7Empty && !offhandEmpty) return;

        int source = findAnyTotemSlot(client);
        if (source == -1) return;

        int syncId = client.player.currentScreenHandler.syncId;

        if (slot7Empty) {
            click(client, syncId, source, slot7);
        } else {
            click(client, syncId, source, offhand);
        }
    }

    // =========================
    // CLICK SLOT
    // =========================
    private static void click(MinecraftClient client, int syncId, int from, int to) {
        client.interactionManager.clickSlot(
                syncId,
                from,
                to,
                SlotActionType.SWAP,
                client.player
        );
        client.player.swingHand(Hand.MAIN_HAND);
    }

    // =========================
    // UTILS
    // =========================
    private static int findAnyTotemSlot(MinecraftClient client) {
        for (int i = 9; i <= 35; i++) {
            if (client.player.currentScreenHandler
                    .getSlot(i).getStack().getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        return -1;
    }

    private static int randomDelay() {
        return 1 + random.nextInt(4);
    }
}
