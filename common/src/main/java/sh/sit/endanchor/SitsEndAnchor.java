package sh.sit.endanchor;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.RegistryKeys;

import java.util.Set;

public class SitsEndAnchor {
    public static final String MOD_ID = "endanchor";

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(MOD_ID, RegistryKeys.BLOCK);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(MOD_ID, RegistryKeys.BLOCK_ENTITY_TYPE);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(MOD_ID, RegistryKeys.ITEM);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(MOD_ID, RegistryKeys.RECIPE_SERIALIZER);

    public static final RegistrySupplier<Block> END_ANCHOR_BLOCK =
            BLOCKS.register(
                    "end_anchor",
                    EndAnchorBlock::new
            );
    public static final RegistrySupplier<BlockEntityType<EndAnchorBlockEntity>> END_ANCHOR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register(
                    "end_anchor_block_entity",
                    () -> new BlockEntityType<>(EndAnchorBlockEntity::new, Set.of(END_ANCHOR_BLOCK.get()))
            );
    public static final RegistrySupplier<Item> END_ANCHOR_BLOCK_ITEM =
            ITEMS.register(
                    "end_anchor",
                    EndAnchorBlockItem::new
            );
    public static final RegistrySupplier<EndAnchorBlockCraftingRecipe.Serializer> END_ANCHOR_BLOCK_CRAFTING_RECIPE =
            RECIPE_SERIALIZERS.register(
                    "end_anchor_recipe",
                    EndAnchorBlockCraftingRecipe.Serializer::new
            );

    public static void onInitialize() {
        EndAnchorBlock.registerDispenserBehaviour();

        BLOCKS.register();
        BLOCK_ENTITIES.register();
        ITEMS.register();
        RECIPE_SERIALIZERS.register();
    }
}
