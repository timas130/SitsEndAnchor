package sh.sit.endanchor;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class SitsEndAnchor implements ModInitializer {
    public static final EndAnchorBlock END_ANCHOR_BLOCK = Registry.register(Registries.BLOCK, Identifier.of("endanchor", "end_anchor"), new EndAnchorBlock());
    public static final BlockEntityType<EndAnchorBlockEntity> END_ANCHOR_BLOCK_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of("endanchor", "end_anchor_block_entity"), BlockEntityType.Builder.create(EndAnchorBlockEntity::new, END_ANCHOR_BLOCK).build());
    public static final EndAnchorBlockItem END_ANCHOR_BLOCK_ITEM = Registry.register(Registries.ITEM, Identifier.of("endanchor", "end_anchor"), new EndAnchorBlockItem());
    public static final EndAnchorBlockCraftingRecipe.Serializer END_ANCHOR_BLOCK_CRAFTING_RECIPE = Registry.register(Registries.RECIPE_SERIALIZER, Identifier.of("endanchor", "end_anchor_recipe"), new EndAnchorBlockCraftingRecipe.Serializer());

    @Override
    public void onInitialize() {
        EndAnchorBlock.registerDispenserBehaviour();
    }
}
