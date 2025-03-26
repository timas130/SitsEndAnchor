package sh.sit.endanchor;

import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class EndAnchorBlockItem extends BlockItem {
    public EndAnchorBlockItem() {
        super(SitsEndAnchor.END_ANCHOR_BLOCK, new BlockItem.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of("endanchor", "end_anchor")))
                .component(DataComponentTypes.LODESTONE_TRACKER, new LodestoneTrackerComponent(Optional.empty(), false)));
    }

    @Override
    protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
        super.postPlacement(pos, world, player, stack, state);

        if (world.getServer() == null) {
            return false;
        }

        final LodestoneTrackerComponent lodestoneTrackerComponent =
                Objects.requireNonNull(stack.get(DataComponentTypes.LODESTONE_TRACKER));
        final GlobalPos lodestonePos = lodestoneTrackerComponent.target().orElse(null);

        if (lodestonePos != null) {
            final EndAnchorBlockEntity blockEntity = (EndAnchorBlockEntity) world.getBlockEntity(pos);
            Objects.requireNonNull(blockEntity).setLodestonePos(lodestonePos.pos());
        }

        return true;
    }
}
