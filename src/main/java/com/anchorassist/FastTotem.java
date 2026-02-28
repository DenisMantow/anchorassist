package com.anchorassist;

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

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(FastTotem::onTick);
    }

    private static void onTick(MinecraftClient client) {

        if (!AnchorAssist.fastTotemEnabled) return;
        if (client.player == null) return;

        handle(client);
    }

    private static void handle(MinecraftClient client) {

        // Hanya aktif saat inventory terbuka
        if (!(client.currentScreen instanceof InventoryScreen screen)) return;
        if (client.player.currentScreenHandler == null) return;

        if (delay > 0) {
            delay--;
            return;
        }

        int syncId = client.player.currentScreenHandler.syncId;

        List<Integer> totemSlots = new ArrayList<>();

        // Scan inventory utama (9–35)
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

        int randomSlot = totemSlots.get(
                ThreadLocalRandom.current().nextInt(totemSlots.size())
        );

        // =========================
        // HUMAN VISUAL MOUSE MOVE
        // =========================
        moveMouseToSlot(client, screen, randomSlot);

        // =========================
        // SWAP (TIDAK GERAK KE SLOT TUJUAN)
        // =========================
        if (slot7Empty) {
            swap(client, syncId, randomSlot, 7);
        }

        if (offhandEmpty) {
            swap(client, syncId, randomSlot, 40);
        }

        // Delay random biar natural
        delay = ThreadLocalRandom.current().nextInt(2, 6);
    }

    // =========================
    // MOVE MOUSE VISUAL KE SLOT TOTEM (1.21.4 FIX)
    // =========================
    private static void moveMouseToSlot(MinecraftClient client,
                                        InventoryScreen screen,
                                        int slotIndex) {

        if (client.getWindow() == null) return;

        Slot slot = client.player.currentScreenHandler.getSlot(slotIndex);

        // Posisi tengah slot
        double mouseX = screen.width / 2.0 - 90 + slot.x + 8;
        double mouseY = screen.height / 2.0 - 90 + slot.y + 8;

        long windowHandle = client.getWindow().getHandle();

        client.mouse.onCursorPos(windowHandle, mouseX, mouseY);
    }

    private static void swap(MinecraftClient client,
                             int syncId,
                             int from,
                             int hotbarSlot) {

        client.interactionManager.clickSlot(
                syncId,
                from,
                hotbarSlot,
                SlotActionType.SWAP,
                client.player
        );
    }
}
