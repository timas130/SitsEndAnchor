package sh.sit.endanchor.mixin;

import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.fix.ItemStackComponentizationFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStackComponentizationFix.class)
public abstract class ItemStackComponentizationFixMixin {
    @Shadow
    private static void fixLodestoneTarget(ItemStackComponentizationFix.StackData data, Dynamic<?> dynamic) {
    }

    @Inject(at = @At("TAIL"), method = "fixStack")
    private static void fixStack(ItemStackComponentizationFix.StackData data, Dynamic<?> dynamic, CallbackInfo ci) {
        if (data.itemEquals("endanchor:end_anchor")) {
            fixLodestoneTarget(data, dynamic);
        }
    }
}
