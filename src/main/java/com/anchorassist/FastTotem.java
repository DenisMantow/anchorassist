package com.anchorassist;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class FastTotem {
    
// =========================
// FAST TOTEM (RANDOM DELAY 1-3 TICK)
// =========================
private void handleFastTotem(MinecraftClient client) {

if (!(client.currentScreen instanceof InventoryScreen)) return;
if (client.player.currentScreenHandler == null) return;

if (fastTotemDelay > 0) {
fastTotemDelay--;
return;
}

int syncId = client.player.currentScreenHandler.syncId;

// =========================
// Cari semua slot inventory yang berisi totem
// =========================
java.util.List<Integer> totemSlots = new java.util.ArrayList<>();

for (int i = 9; i <= 35; i++) {
if (client.player.currentScreenHandler.getSlot(i).getStack().getItem()
== Items.TOTEM_OF_UNDYING) {
totemSlots.add(i);
}
}

if (totemSlots.isEmpty()) {
fastTotemStage = 0;
return;
}

int slot7Container = 36 + 7;
int offhandContainer = 45;

boolean slot7Empty = client.player.currentScreenHandler
.getSlot(slot7Container).getStack().isEmpty();

boolean offhandEmpty = client.player.currentScreenHandler
.getSlot(offhandContainer).getStack().isEmpty();

// Kalau dua-duanya sudah ada totem → reset
if (!slot7Empty && !offhandEmpty) {
fastTotemStage = 0;
return;
}

// Ambil slot totem random
int randomIndex = ThreadLocalRandom.current().nextInt(totemSlots.size());
int randomTotemSlot = totemSlots.get(randomIndex);

// =========================
// Isi salah satu dulu
// =========================
if (slot7Empty && offhandEmpty) {

// Random pilih mana dulu    
if (ThreadLocalRandom.current().nextBoolean()) {    

    // Slot 7 dulu    
    client.interactionManager.clickSlot(    
            syncId,    
            randomTotemSlot,    
            7,    
            SlotActionType.SWAP,    
            client.player    
    );    

    fastTotemStage = 1;    
} else {    

    // Offhand dulu    
    client.interactionManager.clickSlot(    
            syncId,    
            randomTotemSlot,    
            40,    
            SlotActionType.SWAP,    
            client.player    
    );    

    fastTotemStage = 2;    
}    

fastTotemDelay = getRandomDelay();    
return;

}

// =========================
// Jika salah satu kosong
// =========================
if (slot7Empty) {

client.interactionManager.clickSlot(    
        syncId,    
        randomTotemSlot,    
        7,    
        SlotActionType.SWAP,    
        client.player    
);    

fastTotemDelay = getRandomDelay();    
return;

}

if (offhandEmpty) {

client.interactionManager.clickSlot(    
        syncId,    
        randomTotemSlot,    
        40,    
        SlotActionType.SWAP,    
        client.player    
);    

fastTotemDelay = getRandomDelay();    
return;

}

}

// =========================
// HOTBAR FINDER
// =========================
private int findHotbarItem(net.minecraft.item.Item item, MinecraftClient client) {
for (int i = 0; i < 9; i++) {
if (client.player.getInventory().getStack(i).getItem() == item) {
return i;
}
}
return -1;
}
}
