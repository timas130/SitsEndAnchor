package sh.sit.endanchor;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.datafixer.FixUtil;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.datafixer.fix.ItemNbtFix;

public class EndAnchorBlockPosFormatFix extends DataFix {
    public EndAnchorBlockPosFormatFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(TypeReferences.ITEM_STACK);
        return this.fixTypeEverywhereTyped(
                "BlockPos format for end anchor target",
                type,
                ItemNbtFix.fixNbt(type,
                        "endanchor:end_anchor"::equals,
                        tagDynamic -> tagDynamic
                                .update("LodestonePos", FixUtil::fixBlockPos)
                                // dimension is ignored, so it's probably ok to just hardcode it?
                                // this is such a hack
                                .set("LodestoneDimension", tagDynamic.createString("minecraft:the_end"))
                )
        );
    }
}
