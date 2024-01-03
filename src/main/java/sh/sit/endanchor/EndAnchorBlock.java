package sh.sit.endanchor;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CompassItem;
import net.minecraft.item.EndCrystalItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class EndAnchorBlock extends Block implements BlockEntityProvider {
    public static final BooleanProperty CHARGED = BooleanProperty.of("charged");

    public EndAnchorBlock() {
        super(FabricBlockSettings.create()
                .strength(4f)
                .luminance(blockState -> blockState.get(CHARGED) ? 10 : 0));

        setDefaultState(getDefaultState().with(CHARGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(CHARGED);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        // charge the anchor if holding end crystal
        final ItemStack handContents = player.getStackInHand(hand);
        if (handContents.getItem() instanceof EndCrystalItem && !state.get(CHARGED)) {
            world.setBlockState(pos, state.with(CHARGED, true));
            player.playSound(SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1);
            if (!player.getAbilities().creativeMode) {
                handContents.decrement(1);
            }
            return ActionResult.success(world.isClient);
        }

        // if not the end, explode
        if (!world.getDimensionEntry().matchesKey(DimensionTypes.THE_END)) {
            if (!world.isClient) {
                explode(state, world, pos);
            }

            return ActionResult.success(world.isClient);
        }

        // if charged and not holding end crystal
        if (state.get(CHARGED)) {
            if (world.isClient) {
                return ActionResult.SUCCESS;
            }

            final EndAnchorBlockEntity blockEntity = (EndAnchorBlockEntity) world.getBlockEntity(pos);
            final BlockPos lodestonePos = Objects.requireNonNull(blockEntity).getLodestonePos();

            if (world.getBlockState(lodestonePos).getBlock() != Blocks.LODESTONE) {
                playErrorSound(world, pos);
                return ActionResult.SUCCESS;
            }

            final BlockPos spawnPos = lodestonePos.add(0, 1, 0);
            final Vec3d teleportTarget = Dismounting.findRespawnPos(EntityType.PLAYER, world, spawnPos, false);

            if (teleportTarget == null) {
                playErrorSound(world, pos);
            } else {
                world.setBlockState(pos, state.with(CHARGED, false));

                player.teleport(teleportTarget.x, teleportTarget.y, teleportTarget.z, true);

                final MinecraftServer server = world.getServer();
                if (server != null) {
                    final Vec3d centerBlockPos = pos.toCenterPos();

                    // fixme: this doesn't look right
                    server.getPlayerManager().sendToAround(
                            null,
                            centerBlockPos.x,
                            centerBlockPos.y,
                            centerBlockPos.z,
                            30f,
                            world.getRegistryKey(),
                            new PlaySoundS2CPacket(
                                    Registries.SOUND_EVENT.getEntry(SoundEvents.ENTITY_ENDERMAN_TELEPORT),
                                    SoundCategory.PLAYERS,
                                    centerBlockPos.x,
                                    centerBlockPos.y,
                                    centerBlockPos.z,
                                    1,
                                    1,
                                    world.random.nextLong()
                            )
                    );
                    server.getPlayerManager().sendToAround(
                            null,
                            teleportTarget.x,
                            teleportTarget.y,
                            teleportTarget.z,
                            30f,
                            world.getRegistryKey(),
                            new PlaySoundS2CPacket(
                                    Registries.SOUND_EVENT.getEntry(SoundEvents.ENTITY_ENDERMAN_TELEPORT),
                                    SoundCategory.PLAYERS,
                                    teleportTarget.x,
                                    teleportTarget.y,
                                    teleportTarget.z,
                                    1,
                                    1,
                                    world.random.nextLong()
                            )
                    );
                }
            }

            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    private static void explode(BlockState state, World world, BlockPos pos) {
        world.removeBlock(pos, false);
        world.createExplosion(
                null,
                world.getDamageSources().badRespawnPoint(pos.toCenterPos()),
                null,
                pos.toCenterPos(),
                // make the explosion two times as big if charged with crystal
                5.0F * (state.get(CHARGED) ? 2 : 1),
                true,
                World.ExplosionSourceType.BLOCK
        );
    }

    private static void playErrorSound(World world, BlockPos pos) {
        world.playSound(
                null,
                pos,
                SoundEvents.ENTITY_ENDERMAN_TELEPORT,
                SoundCategory.PLAYERS,
                1,
                -2f
        );
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContextParameterSet.Builder builder) {
        final EndAnchorBlockEntity blockEntity = (EndAnchorBlockEntity) builder.getOptional(LootContextParameters.BLOCK_ENTITY);
        if (blockEntity == null) return super.getDroppedStacks(state, builder);

        final ItemStack tool = builder.getOptional(LootContextParameters.TOOL);
        if (tool == null) return super.getDroppedStacks(state, builder);

        final NbtCompound lodestonePos = NbtHelper.fromBlockPos(blockEntity.getLodestonePos());

        if (EnchantmentHelper.hasSilkTouch(tool)) {
            final ItemStack droppedStack = new ItemStack(SitsEndAnchor.END_ANCHOR_BLOCK_ITEM);
            final NbtCompound droppedStackNbt = droppedStack.getOrCreateNbt();
            droppedStackNbt.put(EndAnchorBlockItem.LODESTONE_POS_KEY, lodestonePos);

            return Collections.singletonList(droppedStack);
        } else {
            final ItemStack droppedStack = new ItemStack(Items.COMPASS);
            final NbtCompound droppedStackNbt = droppedStack.getOrCreateNbt();
            droppedStackNbt.putBoolean(CompassItem.LODESTONE_TRACKED_KEY, true);
            droppedStackNbt.put(CompassItem.LODESTONE_POS_KEY, lodestonePos);
            // dramatic!
            // (simply writes the world name)
            World.CODEC
                    // .getWorld() only returns null during world gen, so it's never null in this case
                    .encodeStart(NbtOps.INSTANCE, Objects.requireNonNull(blockEntity.getWorld()).getRegistryKey())
                    .result()
                    .ifPresent((nbtElement) -> {
                        droppedStackNbt.put(CompassItem.LODESTONE_DIMENSION_KEY, nbtElement);
                    });

            return Collections.singletonList(droppedStack);
        }
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return state.get(CHARGED) ? 15 : 0;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EndAnchorBlockEntity(pos, state);
    }

    public static void registerDispenserBehaviour() {
        DispenserBlock.registerBehavior(Items.END_CRYSTAL, (pointer, stack) -> {
            final BlockPos targetBlock = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
            final BlockState targetState = pointer.getWorld().getBlockState(targetBlock);

            if (targetState.isOf(SitsEndAnchor.END_ANCHOR_BLOCK) && !targetState.get(CHARGED)) {
                pointer.getWorld().setBlockState(targetBlock, targetState.with(CHARGED, true));
                stack.decrement(1);
            }
            return stack;
        });
    }
}
