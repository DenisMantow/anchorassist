package walksy.optimizer;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

import java.util.List;

public class CrystalOptimizer {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean enabled = false;

    private static int hitCount;
    private static int breakingBlockTick;

    public static void onTick() {

        if (!enabled) return;
        if (mc.player == null || mc.world == null) return;

        ItemStack mainHandStack = mc.player.getMainHandStack();

        if (mc.options.attackKey.isPressed()) {
            breakingBlockTick++;
        } else breakingBlockTick = 0;

        if (breakingBlockTick > 2)
            return;

        if (!mc.options.useKey.isPressed()) {
            hitCount = 0;
        }

        if (hitCount >= limitPackets())
            return;

        // FAST BREAK
        if (lookingAtCrystalLike()) {
            if (mc.options.attackKey.isPressed()) {
                Entity entity = getLookEntity();
                if (entity != null) {
                    mc.interactionManager.attackEntity(mc.player, entity);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    hitCount++;
                }
            }
        }

        // FAST PLACE
        if (!mainHandStack.isOf(Items.END_CRYSTAL))
            return;

        BlockHitResult hit = raycast();

        if (hit == null) return;

        BlockPos pos = hit.getBlockPos();

        if (mc.options.useKey.isPressed()
                && canPlaceCrystal(pos)) {

            mc.interactionManager.interactBlock(
                    mc.player,
                    Hand.MAIN_HAND,
                    hit
            );

            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private static BlockHitResult raycast() {
        Vec3d camPos = mc.player.getEyePos();
        Vec3d look = mc.player.getRotationVec(1.0f);
        return mc.world.raycast(new RaycastContext(
                camPos,
                camPos.add(look.multiply(4.5)),
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                mc.player
        ));
    }

    private static boolean lookingAtCrystalLike() {
        return mc.crosshairTarget instanceof EntityHitResult hit &&
                (hit.getEntity() instanceof EndCrystalEntity
                        || hit.getEntity() instanceof SlimeEntity
                        || hit.getEntity() instanceof MagmaCubeEntity);
    }

    private static Entity getLookEntity() {
        if (mc.crosshairTarget instanceof EntityHitResult hit)
            return hit.getEntity();
        return null;
    }

    private static boolean canPlaceCrystal(BlockPos block) {
        BlockState state = mc.world.getBlockState(block);
        if (!state.isOf(Blocks.OBSIDIAN) && !state.isOf(Blocks.BEDROCK))
            return false;

        BlockPos up = block.up();
        if (!mc.world.isAir(up))
            return false;

        List<Entity> list = mc.world.getOtherEntities(
                null,
                new Box(up)
        );

        return list.isEmpty();
    }

    private static int limitPackets() {
        return getPing() > 50 ? 2 : 1;
    }

    private static int getPing() {
        if (mc.getNetworkHandler() == null) return 0;
        PlayerListEntry entry =
                mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        if (entry == null) return 0;
        return entry.getLatency();
    }
  }
