package sh.sit.endanchor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EndAnchorBlockCraftingRecipe implements CraftingRecipe {
    private static final Logger LOGGER = LoggerFactory.getLogger(EndAnchorBlockCraftingRecipe.class);

    final String group;
    final CraftingRecipeCategory category;
    final RawShapedRecipe raw;
    final boolean showNotification;

    public EndAnchorBlockCraftingRecipe(String group,
                                        CraftingRecipeCategory category,
                                        RawShapedRecipe raw,
                                        boolean showNotification) {
        this.group = group;
        this.category = category;
        this.raw = raw;
        this.showNotification = showNotification;
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        if (!raw.matches(input)) return false;

        // find the lodestone compass in the recipe
        ItemStack lodestoneCompassStack = findLodestoneCompass(input);
        if (lodestoneCompassStack == null) return false;

        // check the position the compass is pointing to
        LodestoneTrackerComponent lodestoneTrackerComponent = lodestoneCompassStack.get(DataComponentTypes.LODESTONE_TRACKER);
        assert lodestoneTrackerComponent != null;

        final GlobalPos lodestonePos = lodestoneTrackerComponent.target().orElse(null);
        if (lodestonePos == null) {
            return false;
        }

        // ensure compass points to the end
        return lodestonePos.dimension() == World.END;
    }

    private static @Nullable ItemStack findLodestoneCompass(CraftingRecipeInput input) {
        ItemStack lodestoneCompassStack = null;
        for (int i = 0; i < input.getSize(); i++) {
            final ItemStack stack = input.getStackInSlot(i);
            if (stack.isOf(Items.COMPASS) && stack.get(DataComponentTypes.LODESTONE_TRACKER) != null) {
                if (lodestoneCompassStack != null) {
                    LOGGER.info("Invalid end anchor recipe: multiple lodestone compasses");
                    return null;
                }
                lodestoneCompassStack = stack;
            }
        }

        if (lodestoneCompassStack == null) {
            LOGGER.info("Invalid end anchor recipe: no lodestone compass");
            return null;
        }

        return lodestoneCompassStack;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        // just in case?
        if (!matches(input, null)) {
            return ItemStack.EMPTY;
        }

        final ItemStack lodestoneCompassStack = findLodestoneCompass(input);
        assert lodestoneCompassStack != null; // checked in matches()
        final LodestoneTrackerComponent lodestoneTrackerComponent = lodestoneCompassStack.get(DataComponentTypes.LODESTONE_TRACKER);
        assert lodestoneTrackerComponent != null; // checked in matches()

        final GlobalPos lodestonePos = lodestoneTrackerComponent.target().orElse(null);
        assert lodestonePos != null; // checked in matches()

        final ItemStack result = new ItemStack(SitsEndAnchor.END_ANCHOR_BLOCK_ITEM);
        result.set(DataComponentTypes.LODESTONE_TRACKER, lodestoneTrackerComponent);

        return result;
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= raw.getHeight() && height >= raw.getHeight();
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return new ItemStack(SitsEndAnchor.END_ANCHOR_BLOCK_ITEM);
    }

    @Override
    public RecipeSerializer<EndAnchorBlockCraftingRecipe> getSerializer() {
        return SitsEndAnchor.END_ANCHOR_BLOCK_CRAFTING_RECIPE;
    }

    @Override
    public CraftingRecipeCategory getCategory() {
        return category;
    }

    @Override
    public boolean showNotification() {
        return showNotification;
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        return raw.getIngredients();
    }

    public static class Serializer implements RecipeSerializer<EndAnchorBlockCraftingRecipe> {
        public static final MapCodec<EndAnchorBlockCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                                Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.group),
                                CraftingRecipeCategory.CODEC.fieldOf("category").orElse(CraftingRecipeCategory.MISC).forGetter(recipe -> recipe.category),
                                RawShapedRecipe.CODEC.forGetter(recipe -> recipe.raw),
                                Codec.BOOL.optionalFieldOf("show_notification", Boolean.TRUE).forGetter(recipe -> recipe.showNotification)
                        )
                        .apply(instance, EndAnchorBlockCraftingRecipe::new)
        );
        public static final PacketCodec<RegistryByteBuf, EndAnchorBlockCraftingRecipe> PACKET_CODEC = PacketCodec.ofStatic(
                Serializer::write, Serializer::read
        );

        @Override
        public MapCodec<EndAnchorBlockCraftingRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, EndAnchorBlockCraftingRecipe> packetCodec() {
            return PACKET_CODEC;
        }

        private static EndAnchorBlockCraftingRecipe read(RegistryByteBuf buf) {
            String string = buf.readString();
            CraftingRecipeCategory craftingRecipeCategory = buf.readEnumConstant(CraftingRecipeCategory.class);
            RawShapedRecipe rawShapedRecipe = RawShapedRecipe.PACKET_CODEC.decode(buf);
            boolean bl = buf.readBoolean();
            return new EndAnchorBlockCraftingRecipe(string, craftingRecipeCategory, rawShapedRecipe, bl);
        }

        private static void write(RegistryByteBuf buf, EndAnchorBlockCraftingRecipe recipe) {
            buf.writeString(recipe.group);
            buf.writeEnumConstant(recipe.category);
            RawShapedRecipe.PACKET_CODEC.encode(buf, recipe.raw);
            buf.writeBoolean(recipe.showNotification);
        }
    }
}
