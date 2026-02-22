package com.anchorassist;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class FastTotem {

    // =========================
    // KEYBIND
    // =========================
    public static KeyBinding fastTotemKey;

    // =========================
    // STATE MACHINE
    // =========================
    private enum State {
        IDLE,
        WAIT_FIRST,
        WAIT_SECOND
    }

    private static State state = State.IDLE;

    // =========================
    // INTERNAL DATA
    // =========================
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
    // TICK LOGIC
    // =========================
    private static void onTick(MinecraftClient client) {
        if (client.player == null) return;

        if (state == State.IDLE) {
            handleKeyPress(client);
            return;
        }

        if (delay > 0) {
            delay--;
            return;
        }

        int syncId = client.player.currentScreenHandler.syncId;

        if (state == State.WAIT_FIRST) {
            clickSlot(client, syncId, sourceSlot, firstTarget);
            delay = randomDelay();
            state = (secondTarget != -1) ? State.WAIT_SECOND : State.IDLE;
            return;
        }

        if (state == State.WAIT_SECOND) {
            clickSlot(client, syncId, sourceSlot, secondTarget);
            reset();
        }
    }

    // =========================
    // KEY PRESS HANDLER
    // =========================
    private static void handleKeyPress(MinecraftClient client) {
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

        // =========================
        // DECIDE TARGET ORDER
        // =========================
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

        delay = randomDelay();
        state = State.WAIT_FIRST;

        client.player.sendMessage(
                Text.literal("Fast Totem activated"),
                true
        );
    }

    // =========================
    // CLICK SLOT
    // =========================
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

    // =========================
    // UTILS
    // =========================
    private static int randomDelay() {
        return 1 + random.nextInt(4); // 1–4 tick
    }

    private static void reset() {
        state = State.IDLE;
        delay = 0;
        sourceSlot = -1;
        firstTarget = -1;
        secondTarget = -1;
    }
          }
