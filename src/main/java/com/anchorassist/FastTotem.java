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

    private static boolean moveMouse = false;
    private static double targetX, targetY;

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(FastTotem::onTick);
    }

    public static boolean shouldMoveMouse() {
        return moveMouse;
    }

    public static double getTargetX() {
        return targetX;
    }

    public static double getTargetY() {
        return targetY;
    }

    private static void onTick(MinecraftClient client) {

        if (!AnchorAssist.fastTotemEnabled) return;
        if (client.player == null) return;

        handle(client);
    }

    private static void handle(MinecraftClient client) {

        if (!(client.currentScreen instanceof InventoryScreen screen)) {
            moveMouse = false;
            return;
        }

        if (client.player.currentScreenHandler == null) return;

        if (delay > 0) {
            delay--;
            return;
        }

        int syncId = client.player.currentScreenHandler.syncId;

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

        int randomSlot = totemSlots.get(
                ThreadLocalRandom.current().nextInt(totemSlots.size())
        );

        Slot slot = client.player.currentScreenHandler.getSlot(randomSlot);

        targetX = screen.width / 2.0 - 90 + slot.x;
        targetY = screen.height / 2.0 - 90 + slot.y;

        moveMouse = true;

        if (slot7Empty) {
            swap(client, syncId, randomSlot, 7);
        }

        if (offhandEmpty) {
            swap(client, syncId, randomSlot, 40);
        }

        delay = ThreadLocalRandom.current().nextInt(3, 6);
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
