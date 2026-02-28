package com.anchorassist;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FastTotem {

    private static int delay = 0;
    private static boolean waitingForMouse = false;
    private static int targetSlot = -1;
    private static int targetHotbar = -1;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(FastTotem::onTick);
    }

    private static void onTick(MinecraftClient client) {

        if (!AnchorAssist.fastTotemEnabled) return;
        if (client.player == null) return;

        handle(client);
    }

    private static void handle(MinecraftClient client) {

        if (!(client.currentScreen instanceof InventoryScreen screen)) {
            waitingForMouse = false;
            return;
        }

        if (client.player.currentScreenHandler == null) return;

        if (delay > 0) {
            delay--;
            return;
        }

        int syncId = client.player.currentScreenHandler.syncId;

        // =========================
        // FIND TOTEM
        // =========================
        List<Integer> totemSlots = new ArrayList<>();

        for (int i = 9; i <= 35; i++) {
            if (client.player.currentScreenHandler
                    .getSlot(i).getStack().getItem() == Items.TOTEM_OF_UNDYING) {
                totemSlots.add(i);
            }
        }

        if (totemSlots.isEmpty()) return;

        int slot7 = 36 + 7;
        int offhand = 45;

        boolean slot7Empty = client.player.currentScreenHandler
                .getSlot(slot7).getStack().isEmpty();

        boolean offhandEmpty = client.player.currentScreenHandler
                .getSlot(offhand).getStack().isEmpty();

        if (!slot7Empty && !offhandEmpty) return;

        // =========================
        // PHASE 1: MOVE MOUSE
        // =========================
        if (!waitingForMouse) {

            targetSlot = totemSlots.get(
                    ThreadLocalRandom.current().nextInt(totemSlots.size())
            );

            if (slot7Empty) targetHotbar = 7;
            else targetHotbar = 40;

            moveMouseToSlot(client, screen, targetSlot);

            waitingForMouse = true;
            delay = 4; // tunggu beberapa tick sebelum swap
            return;
        }

        // =========================
        // PHASE 2: SWAP AFTER MOUSE ARRIVED
        // =========================
        if (waitingForMouse) {

            client.interactionManager.clickSlot(
                    syncId,
                    targetSlot,
                    targetHotbar,
                    SlotActionType.SWAP,
                    client.player
            );

            waitingForMouse = false;
            delay = ThreadLocalRandom.current().nextInt(4, 8);
        }
    }

    // =========================
    // REAL MOUSE MOVE (GLFW)
    // =========================
    private static void moveMouseToSlot(MinecraftClient client,
                                        InventoryScreen screen,
                                        int slotIndex) {

        Slot slot = client.player.currentScreenHandler.getSlot(slotIndex);

        double guiX = screen.getX() + slot.x + 8;
        double guiY = screen.getY() + slot.y + 8;

        long window = client.getWindow().getHandle();

        double scaleFactor = client.getWindow().getScaleFactor();

        double realX = guiX * scaleFactor;
        double realY = guiY * scaleFactor;

        GLFW.glfwSetCursorPos(window, realX, realY);
    }
}
