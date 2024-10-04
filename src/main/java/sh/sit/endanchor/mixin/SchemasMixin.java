package sh.sit.endanchor.mixin;

import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.datafixer.Schemas;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sh.sit.endanchor.EndAnchorBlockPosFormatFix;

import java.util.function.BiFunction;

@Mixin(Schemas.class)
public class SchemasMixin {
    @Shadow @Final private static BiFunction<Integer, Schema, Schema> EMPTY_IDENTIFIER_NORMALIZE;

    @Inject(at = @At(value = "NEW", shift = At.Shift.BY, by = 5, target = "(Lcom/mojang/datafixers/schemas/Schema;)Lnet/minecraft/datafixer/fix/BlockPosFormatFix;"), method = "build")
    private static void build(DataFixerBuilder builder, CallbackInfo ci) {
        Schema schema = builder.addSchema(3813, 1, EMPTY_IDENTIFIER_NORMALIZE);
        builder.addFixer(new EndAnchorBlockPosFormatFix(schema));
    }
}
