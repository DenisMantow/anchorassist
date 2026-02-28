package com.anchorassist;

import com.anchorassist.visual.FakeMouseRenderer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

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

        // Reset kalau inventory ditutup
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
        // FIND TOTEM (9–35)
        // =========================
        List<Integer> totemSlots = new ArrayList<>();

        for (int i = 9; i <= 35; i++) {
            if (client.player.currentScreenHandler
                    .getSlot(i).getStack().getItem() == Items.TOTEM_OF_UNDYING) {
                totemSlots.add(i);
            }
        }

        if (totemSlots.isEmpty()) return;

        int slot7 = 36 + 7; // hotbar index 7
        int offhand = 45;

        boolean slot7Empty = client.player.currentScreenHandler
                .getSlot(slot7).getStack().isEmpty();

        boolean offhandEmpty = client.player.currentScreenHandler
                .getSlot(offhand).getStack().isEmpty();

        if (!slot7Empty && !offhandEmpty) return;

        // =========================
        // PHASE 1: MOVE FAKE MOUSE
        // =========================
        if (!waitingForMouse) {

            targetSlot = totemSlots.get(
                    ThreadLocalRandom.current().nextInt(totemSlots.size())
            );

            targetHotbar = slot7Empty ? 7 : 40;

            moveMouseToSlot(client, screen, targetSlot);

            waitingForMouse = true;
            delay = 4; // tunggu 4 tick sebelum swap
            return;
        }

        // =========================
        // PHASE 2: SWAP AFTER DELAY
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
    // MOVE VISUAL FAKE MOUSE
    // =========================
    private static void moveMouseToSlot(MinecraftClient client,
                                        InventoryScreen screen,
                                        int slotIndex) {

        Slot slot = client.player.currentScreenHandler.getSlot(slotIndex);

        double mouseX = screen.width / 2.0 - 90 + slot.x;
        double mouseY = screen.height / 2.0 - 90 + slot.y;

        FakeMouseRenderer.moveTo(mouseX, mouseY);
    }
}
