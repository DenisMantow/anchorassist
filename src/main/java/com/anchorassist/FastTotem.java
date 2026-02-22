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
    // KEYBIND
    // ==================================================
    public static KeyBinding fastTotemKey;

    // ==================================================
    // CLICK STATE
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
    // AUTO STATE
    // ==================================================
    private static int autoDelay = 0;
    private static boolean autoSecondPending = false;
    private static int autoSecondTarget = -1;

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

        handleClickFastTotem(client);
        handleAutoFastTotem(client);
    }

    // ==================================================
    // CLICK FAST TOTEM (KEYBIND)
    // ==================================================
    private static void handleClickFastTotem(MinecraftClient client) {
        if (!AnchorAssist.clickFastTotemEnabled) return;

        if (clickState == ClickState.IDLE) {

            if (!fastTotemKey.wasPressed()) return;
            if (!(client.currentScreen instanceof HandledScreen<?> screen)) return;

            // ✅ MC 1.20.1 SAFE WAY
            Slot hovered = screen.getSlotAt(
                    client.mouse.getX(),
                    client.mouse.getY()
            );

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

            client.player.sendMessage(Text.literal("Click Fast Totem"), true);
            return;
        }

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
    // AUTO FAST TOTEM
    // ==================================================
    private static void handleAutoFastTotem(MinecraftClient client) {
        if (!AnchorAssist.fastTotemEnabled) return;
        if (!(client.currentScreen instanceof InventoryScreen)) return;
        if (clickState != ClickState.IDLE) return;

        if (autoDelay > 0) {
            autoDelay--;
            return;
        }

        int syncId = client.player.currentScreenHandler.syncId;

        int slot7 = 36 + 7;
        int offhand = 45;

        boolean slot7Empty = client.player.currentScreenHandler
                .getSlot(slot7).getStack().isEmpty();
        boolean offhandEmpty = client.player.currentScreenHandler
                .getSlot(offhand).getStack().isEmpty();

        if (!slot7Empty && !offhandEmpty) {
            autoSecondPending = false;
            return;
        }

        List<Integer> totems = new ArrayList<>();
        for (int i = 9; i <= 35; i++) {
            if (client.player.currentScreenHandler
                    .getSlot(i).getStack().getItem() == Items.TOTEM_OF_UNDYING) {
                totems.add(i);
            }
        }

        if (totems.isEmpty()) return;

        int source = totems.get(ThreadLocalRandom.current().nextInt(totems.size()));

        if (slot7Empty && offhandEmpty && !autoSecondPending) {
            if (random.nextBoolean()) {
                clickSlot(client, syncId, source, 7);
                autoSecondTarget = 40;
            } else {
                clickSlot(client, syncId, source, 40);
                autoSecondTarget = 7;
            }
            autoSecondPending = true;
            autoDelay = randomDelay();
            return;
        }

        if (autoSecondPending) {
            clickSlot(client, syncId, source, autoSecondTarget);
            autoSecondPending = false;
            autoDelay = randomDelay();
            return;
        }

        if (slot7Empty) {
            clickSlot(client, syncId, source, 7);
            autoDelay = randomDelay();
        } else if (offhandEmpty) {
            clickSlot(client, syncId, source, 40);
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
        return 1 + random.nextInt(4);
    }

    private static void resetClick() {
        clickState = ClickState.IDLE;
        clickDelay = 0;
        sourceSlot = -1;
        firstTarget = -1;
        secondTarget = -1;
    }
}
