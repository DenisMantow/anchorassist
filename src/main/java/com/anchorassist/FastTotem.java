package com.anchorassist;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.Items;
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
        if (client.player == null) return;
        handle(client);
    }

    private static void handle(MinecraftClient client) {

        if (!(client.currentScreen instanceof InventoryScreen)) return;
        if (client.player.currentScreenHandler == null) return;

        if (delay > 0) {
            delay--;
            return;
        }

        int syncId = client.player.currentScreenHandler.syncId;

        List<Integer> totemSlots = new ArrayList<>();

        for (int i = 9; i <= 35; i++) {
            if (client.player.currentScreenHandler.getSlot(i)
                    .getStack().getItem() == Items.TOTEM_OF_UNDYING) {
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

        int randomSlot = totemSlots.get(
                ThreadLocalRandom.current().nextInt(totemSlots.size())
        );

        if (slot7Empty) {
            swap(client, syncId, randomSlot, 7);
        }

        if (offhandEmpty) {
            swap(client, syncId, randomSlot, 40);
        }

        delay = ThreadLocalRandom.current().nextInt(1, 4);
    }

    private static void swap(MinecraftClient client, int syncId, int from, int to) {
        client.interactionManager.clickSlot(
                syncId,
                from,
                to,
                SlotActionType.SWAP,
                client.player
        );
    }
}
