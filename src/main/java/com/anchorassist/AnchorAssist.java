package com.anchorassist;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.BlockState;

public class AnchorAssist implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null || client.world == null) return;

            // =========================
            // AUTO HIT (FULL COOLDOWN)
            // =========================
            if (client.crosshairTarget instanceof EntityHitResult entityHit) {
                if (entityHit.getEntity() instanceof PlayerEntity target) {

                    double distance = client.player.distanceTo(target);

                    if (distance <= 3.1D) {

                        if (client.player.getAttackCooldownProgress(0.5f) >= 1.0f) {

                            client.interactionManager.attackEntity(client.player, target);
                            client.player.swingHand(Hand.MAIN_HAND);

                        }
                    }
                }
            }

            // =========================
            // AUTO FILL RESPAWN ANCHOR
            // =========================
            if (client.crosshairTarget instanceof BlockHitResult blockHit) {

                BlockState state = client.world.getBlockState(blockHit.getBlockPos());

                if (state.getBlock() instanceof RespawnAnchorBlock) {

                    for (int i = 0; i < 9; i++) {

                        if (client.player.getInventory().getStack(i).getItem() == Items.GLOWSTONE) {

                            int previousSlot = client.player.getInventory().selectedSlot;

                            client.player.getInventory().selectedSlot = i;

                            client.interactionManager.interactBlock(
                                    client.player,
                                    Hand.MAIN_HAND,
                                    blockHit
                            );

                            client.player.getInventory().selectedSlot = previousSlot;

                            break;
                        }
                    }
                }
            }

        });
    }
}
