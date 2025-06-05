package sh.sit.endanchor;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NbtCompatHelper {
    @NotNull
    public static NbtElement fromBlockPos(@NotNull BlockPos pos) {
        return NbtHelper.fromBlockPos(pos);
    }

    @NotNull
    public static BlockPos toBlockPos(@Nullable NbtElement nbt) {
        if (nbt instanceof NbtCompound tag) {
            return new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
        } else if (nbt instanceof NbtIntArray) {
            final int[] intArray = ((NbtIntArray) nbt).getIntArray();
            if (intArray.length != 3) {
                return BlockPos.ORIGIN;
            }
            return new BlockPos(intArray[0], intArray[1], intArray[2]);
        } else {
            return BlockPos.ORIGIN;
        }
    }
}
