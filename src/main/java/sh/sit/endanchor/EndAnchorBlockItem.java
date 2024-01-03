package sh.sit.endanchor;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class EndAnchorBlockItem extends BlockItem {
    public static final String LODESTONE_POS_KEY = "LodestonePos";

    public EndAnchorBlockItem() {
        super(SitsEndAnchor.END_ANCHOR_BLOCK, new FabricItemSettings());
    }

    @Override
    protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
        super.postPlacement(pos, world, player, stack, state);

        if (world.getServer() == null) {
            return false;
        }

        final NbtCompound stackNbt = Objects.requireNonNull(stack.getNbt());
        final BlockPos lodestonePos = NbtHelper.toBlockPos(stackNbt.getCompound(LODESTONE_POS_KEY));

        final EndAnchorBlockEntity blockEntity = (EndAnchorBlockEntity) world.getBlockEntity(pos);
        Objects.requireNonNull(blockEntity).setLodestonePos(lodestonePos);

        return true;
    }
}
