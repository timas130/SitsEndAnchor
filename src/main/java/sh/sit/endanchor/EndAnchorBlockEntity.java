package sh.sit.endanchor;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class EndAnchorBlockEntity extends BlockEntity {
    public static final String LODESTONE_POS_KEY = "LodestonePos";

    private BlockPos lodestonePos;

    public EndAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(SitsEndAnchor.END_ANCHOR_BLOCK_ENTITY, pos, state);
    }

    public BlockPos getLodestonePos() {
        return lodestonePos != null ? lodestonePos : BlockPos.ORIGIN;
    }

    public void setLodestonePos(BlockPos pos) {
        lodestonePos = pos;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.put(LODESTONE_POS_KEY, NbtCompatHelper.fromBlockPos(getLodestonePos()));
        super.writeNbt(nbt, registryLookup);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        setLodestonePos(NbtCompatHelper.toBlockPos(nbt.get(LODESTONE_POS_KEY)));
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }
}
