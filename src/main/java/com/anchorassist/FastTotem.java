package com.anchorassist;

import com.anchorassist.mixin.HandledScreenAccessor;

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

public class FastTotem {

    // =========================
    // KEYBIND
    // =========================
    public static KeyBinding fastTotemKey;

    // =========================
    // STATE MACHINE
    // =========================
    private enum State { IDLE, WAIT_FIRST, WAIT_SECOND }
    private static State state = State.IDLE;

    private static int delay = 0;
    private static int sourceSlot = -1;
    private static int firstTarget = -1;
    private static int secondTarget = -1;

    private static final Random random = new Random();

    // =========================
    // INIT (PANGGIL DI AnchorAssist.onInitializeClient)
    // =========================
    public static void init() {
        fastTotemKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "Fast Totem (Hover)",
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

        if (state != State.IDLE) {
            processState(client);
            return;
        }

        handleClickFastTotem(client);
        handleAutoFastTotem(client);
    }

    // =========================
    // CLICK FAST TOTEM (HOVER + KEYBIND)
    // =========================
    private static void handleClickFastTotem(MinecraftClient client) {
        if (!AnchorAssist.clickFastTotemEnabled) return;
        if (!fastTotemKey.wasPressed()) return;
        if (!(client.currentScreen instanceof HandledScreen<?> screen)) return;

        // 🔥 AMBIL SLOT DI BAWAH MOUSE (BENAR)
        HandledScreenAccessor accessor = (HandledScreenAccessor) screen;

        double mouseX = client.mouse.getX()
                * client.getWindow().getScaledWidth()
                / client.getWindow().getWidth();

        double mouseY = client.mouse.getY()
                * client.getWindow().getScaledHeight()
                / client.getWindow().getHeight();

        Slot hovered = accessor.invokeGetSlotAt(mouseX, mouseY);
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

        client.player.sendMessage(Text.literal("Fast Totem ✔"), true);
    }

    // =========================
    // AUTO FAST TOTEM (RANDOM INVENTORY)
    // =========================
    private static void handleAutoFastTotem(MinecraftClient client) {
        if (!AnchorAssist.fastTotemEnabled) return;
        if (!(client.currentScreen instanceof InventoryScreen)) return;

        int slot7 = 36 + 7;
        int offhand = 45;

        boolean slot7Empty = client.player.currentScreenHandler
                .getSlot(slot7).getStack().isEmpty();
        boolean offhandEmpty = client.player.currentScreenHandler
                .getSlot(offhand).getStack().isEmpty();

        if (!slot7Empty && !offhandEmpty) return;

        List<Integer> totems = new ArrayList<>();
        for (int i = 9; i <= 35; i++) {
            if (client.player.currentScreenHandler
                    .getSlot(i).getStack().getItem() == Items.TOTEM_OF_UNDYING) {
                totems.add(i);
            }
        }

        if (totems.isEmpty()) return;

        sourceSlot = totems.get(random.nextInt(totems.size()));
        firstTarget = slot7Empty ? slot7 : offhand;
        secondTarget = -1;

        delay = randomDelay();
        state = State.WAIT_FIRST;
    }

    // =========================
    // STATE PROCESS
    // =========================
    private static void processState(MinecraftClient client) {
        if (delay-- > 0) return;

        int syncId = client.player.currentScreenHandler.syncId;

        if (state == State.WAIT_FIRST) {
            click(client, syncId, sourceSlot, firstTarget);
            delay = randomDelay();
            state = (secondTarget != -1) ? State.WAIT_SECOND : State.IDLE;
            return;
        }

        if (state == State.WAIT_SECOND) {
            click(client, syncId, sourceSlot, secondTarget);
            reset();
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
    private static int randomDelay() {
        return 1 + random.nextInt(3); // 1–3 tick
    }

    private static void reset() {
        state = State.IDLE;
        delay = 0;
        sourceSlot = -1;
        firstTarget = -1;
        secondTarget = -1;
    }
}
