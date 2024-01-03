package sh.sit.endanchor;

import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.CompassItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.registry.DynamicRegistryManager;
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
    public boolean matches(RecipeInputInventory inventory, World world) {
        // require full-sized crafting table
        if (inventory.size() != 9) {
            return false;
        }

        // check ingredients
        for (int i = 0; i < 9; i++) {
            if (!inventory.getStack(i).isOf(INGREDIENTS[i])) {
                return false;
            }
        }

        // require compass to be a lodestone compass
        final ItemStack lodestoneCompassStack = inventory.getStack(4);
        final NbtCompound lodestoneCompassNbt = lodestoneCompassStack.getNbt();
        if (lodestoneCompassNbt == null) {
            return false;
        }
        final GlobalPos lodestonePos = CompassItem.createLodestonePos(lodestoneCompassNbt);
        if (lodestonePos == null) {
            return false;
        }
        // ensure compass points to the end
        return lodestonePos.getDimension() == World.END;
    }

    @Override
    public ItemStack craft(RecipeInputInventory inventory, DynamicRegistryManager registryManager) {
        // just in case?
        if (!matches(inventory, null)) {
            return ItemStack.EMPTY;
        }

        final ItemStack lodestoneCompassStack = inventory.getStack(4);
        final NbtCompound lodestoneCompassNbt = lodestoneCompassStack.getNbt();
        assert lodestoneCompassNbt != null; // checked in matches()
        final GlobalPos lodestonePos = CompassItem.createLodestonePos(lodestoneCompassNbt);
        assert lodestonePos != null; // checked in matches()

        final ItemStack result = new ItemStack(SitsEndAnchor.END_ANCHOR_BLOCK_ITEM);
        final NbtCompound resultNbt = result.getOrCreateNbt();
        resultNbt.put(EndAnchorBlockItem.LODESTONE_POS_KEY, NbtHelper.fromBlockPos(lodestonePos.getPos()));

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
