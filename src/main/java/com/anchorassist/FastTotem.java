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

    // ==================================================
    // KEYBIND (CLICK FAST TOTEM)
    // ==================================================
    public static KeyBinding fastTotemKey;

    // ==================================================
    // STATE MACHINE (CLICK FAST TOTEM)
    // ==================================================
    private enum ClickState {
        IDLE,
        WAIT_FIRST,
        WAIT_SECOND
    }

    private static ClickState clickState = ClickState.IDLE;

    private static int clickDelay = 0;
    private static int sourceSlot = -1;
    private static int firstTarget = -1;
    private static int secondTarget = -1;

    // ==================================================
    // AUTO FAST TOTEM (INVENTORY)
    // ==================================================
    private static int autoDelay = 0;
    private static int autoStage = 0;

    private static final Random random = new Random();

    // ==================================================
    // INIT
    // ==================================================
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

    // ==================================================
    // MAIN TICK
    // ==================================================
    private static void onTick(MinecraftClient client) {
        if (client.player == null) return;

        // ===============================
        // FEATURE 1 — CLICK FAST TOTEM
        // ===============================
        if (clickState == ClickState.IDLE) {
            handleClickFastTotem(client);
        } else {
            handleClickState(client);
        }

        // ===============================
        // FEATURE 2 — AUTO FAST TOTEM
        // ===============================
        handleAutoFastTotem(client);
    }

    // ==================================================
    // CLICK FAST TOTEM (KEYBIND)
    // ==================================================
    private static void handleClickFastTotem(MinecraftClient client) {
        if (!AnchorAssist.clickFastTotemEnabled) return;
        if (!fastTotemKey.wasPressed()) return;

        if (!(client.currentScreen instanceof HandledScreen<?> screen)) return;

        Slot hovered = screen.getFocusedSlot();
        if (hovered == null) return;
        if (hovered.getStack().getItem() != Items.TOTEM_OF_UNDYING) return;

        int slot7 = 36 + 7;
        int offhand = 45;

        boolean slot7Empty = client.player.currentScreenHandler
                .getSlot(slot7).getStack().isEmpty();
        boolean offhandEmpty = client.player.currentScreenHandler
                .getSlot(offhand).getStack().isEmpty();

        if (!slot7Empty && !offhandEmpty) return;

        sourceSlot = hovered.id;

        if (slot7Empty && offhandEmpty) {
            if (random.nextBoolean()) {
                firstTarget = 7;
                secondTarget = 40;
            } else {
                firstTarget = 40;
                secondTarget = 7;
            }
        } else if (slot7Empty) {
            firstTarget = 7;
            secondTarget = -1;
        } else {
            firstTarget = 40;
            secondTarget = -1;
        }

        clickDelay = randomDelay();
        clickState = ClickState.WAIT_FIRST;

        client.player.sendMessage(
                Text.literal("Click Fast Totem"),
                true
        );
    }

    private static void handleClickState(MinecraftClient client) {
        if (clickDelay > 0) {
            clickDelay--;
            return;
        }

        int syncId = client.player.currentScreenHandler.syncId;

        if (clickState == ClickState.WAIT_FIRST) {
            clickSlot(client, syncId, sourceSlot, firstTarget);
            clickDelay = randomDelay();
            clickState = (secondTarget != -1) ? ClickState.WAIT_SECOND : ClickState.IDLE;
            return;
        }

        if (clickState == ClickState.WAIT_SECOND) {
            clickSlot(client, syncId, sourceSlot, secondTarget);
            resetClick();
        }
    }

    // ==================================================
    // AUTO FAST TOTEM (INVENTORY)
    // ==================================================
    private static void handleAutoFastTotem(MinecraftClient client) {
        if (!AnchorAssist.fastTotemEnabled) return;
        if (!(client.currentScreen instanceof InventoryScreen)) return;
        if (client.player.currentScreenHandler == null) return;

        if (autoDelay > 0) {
            autoDelay--;
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

        if (totemSlots.isEmpty()) {
            autoStage = 0;
            return;
        }

        int slot7 = 36 + 7;
        int offhand = 45;

        boolean slot7Empty = client.player.currentScreenHandler
                .getSlot(slot7).getStack().isEmpty();
        boolean offhandEmpty = client.player.currentScreenHandler
                .getSlot(offhand).getStack().isEmpty();

        if (!slot7Empty && !offhandEmpty) {
            autoStage = 0;
            return;
        }

        int randomTotem = totemSlots.get(
                ThreadLocalRandom.current().nextInt(totemSlots.size())
        );

        if (slot7Empty && offhandEmpty) {
            if (ThreadLocalRandom.current().nextBoolean()) {
                clickSlot(client, syncId, randomTotem, 7);
                autoStage = 1;
            } else {
                clickSlot(client, syncId, randomTotem, 40);
                autoStage = 2;
            }
            autoDelay = randomDelay();
            return;
        }

        if (slot7Empty) {
            clickSlot(client, syncId, randomTotem, 7);
            autoDelay = randomDelay();
        } else if (offhandEmpty) {
            clickSlot(client, syncId, randomTotem, 40);
            autoDelay = randomDelay();
        }
    }

    // ==================================================
    // CLICK SLOT
    // ==================================================
    private static void clickSlot(MinecraftClient client, int syncId, int from, int to) {
        client.interactionManager.clickSlot(
                syncId,
                from,
                to,
                SlotActionType.SWAP,
                client.player
        );
        client.player.swingHand(Hand.MAIN_HAND);
    }

    // ==================================================
    // UTILS
    // ==================================================
    private static int randomDelay() {
        return 1 + random.nextInt(4); // 1–4 tick
    }

    private static void resetClick() {
        clickState = ClickState.IDLE;
        clickDelay = 0;
        sourceSlot = -1;
        firstTarget = -1;
        secondTarget = -1;
    }
}
