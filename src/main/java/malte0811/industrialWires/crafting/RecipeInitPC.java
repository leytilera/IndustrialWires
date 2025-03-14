/*
 * This file is part of Industrial Wires.
 * Copyright (C) 2016-2017 malte0811
 *
 * Industrial Wires is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Industrial Wires is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Industrial Wires.  If not, see <http://www.gnu.org/licenses/>.
 */

package malte0811.industrialWires.crafting;

import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.items.ItemPanelComponent;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nonnull;

public class RecipeInitPC extends ShapedOreRecipe {

	public RecipeInitPC(ItemStack result, Object... recipe) {
		super(result, recipe);
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(@Nonnull InventoryCrafting var1) {
		ItemStack ret = super.getCraftingResult(var1);
		if (ret.getItem()== IndustrialWires.panelComponent) {
			//implicitely initialize the component, relevant for locks with random ID's
			ItemPanelComponent.getTagCompound(ret);
		}
		return ret;
	}

	@Override
	public boolean matches(InventoryCrafting inv, World world) {
		return super.matches(inv, world);
	}
}
