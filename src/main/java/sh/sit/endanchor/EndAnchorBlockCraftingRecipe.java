package sh.sit.endanchor;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

public class EndAnchorBlockCraftingRecipe extends SpecialCraftingRecipe {
    private static final Item[] INGREDIENTS = {
            Items.OBSIDIAN, Items.POPPED_CHORUS_FRUIT, Items.OBSIDIAN,
            Items.POPPED_CHORUS_FRUIT, Items.COMPASS, Items.POPPED_CHORUS_FRUIT,
            Items.OBSIDIAN, Items.POPPED_CHORUS_FRUIT, Items.OBSIDIAN
    };

    public EndAnchorBlockCraftingRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        // require full-sized crafting table
        if (input.getStackCount() != 9) {
            return false;
        }

        // check ingredients
        for (int i = 0; i < 9; i++) {
            if (!input.getStackInSlot(i).isOf(INGREDIENTS[i])) {
                return false;
            }
        }

        // require compass to be a lodestone compass
        final ItemStack lodestoneCompassStack = input.getStackInSlot(4);
        final LodestoneTrackerComponent lodestoneTrackerComponent = lodestoneCompassStack.get(DataComponentTypes.LODESTONE_TRACKER);
        if (lodestoneTrackerComponent == null) {
            return false;
        }

        final GlobalPos lodestonePos = lodestoneTrackerComponent.target().orElse(null);
        if (lodestonePos == null) {
            return false;
        }
        // ensure compass points to the end
        return lodestonePos.dimension() == World.END;
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        // just in case?
        if (!matches(input, null)) {
            return ItemStack.EMPTY;
        }

        final ItemStack lodestoneCompassStack = input.getStackInSlot(4);
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
        return width == 3 && height == 3;
    }

    @Override
    public RecipeSerializer<EndAnchorBlockCraftingRecipe> getSerializer() {
        return SitsEndAnchor.END_ANCHOR_BLOCK_CRAFTING_RECIPE;
    }
}
