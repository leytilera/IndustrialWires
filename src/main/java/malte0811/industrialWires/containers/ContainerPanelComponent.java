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

package malte0811.industrialWires.containers;

import malte0811.industrialWires.IndustrialWires;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class ContainerPanelComponent extends Container {
	public EnumHand hand;

	public ContainerPanelComponent(EnumHand h) {
		hand = h;
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		ItemStack held = playerIn.getHeldItem(hand);
		return held!=null&&held.getItem()== IndustrialWires.panelComponent;
	}
}
