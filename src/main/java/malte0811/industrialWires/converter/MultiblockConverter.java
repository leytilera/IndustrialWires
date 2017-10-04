/*
 * This file is part of Industrial Wires.
 * Copyright (C) 2016-2017 malte0811
 * Industrial Wires is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Industrial Wires is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Industrial Wires.  If not, see <http://www.gnu.org/licenses/>.
 */

package malte0811.industrialWires.converter;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.converter.MechanicalMBBlockType;
import malte0811.industrialWires.blocks.converter.TileEntityMultiblockConverter;
import malte0811.industrialWires.util.LocalSidedWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static malte0811.industrialWires.blocks.converter.MechanicalMBBlockType.*;

public class MultiblockConverter implements MultiblockHandler.IMultiblock {
	public static MultiblockConverter INSTANCE;
	@Override
	public String getUniqueName() {
		return "iw:converter";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state) {
		return MechMBPart.isValidCenter(state);
	}

	private boolean checkEnd(LocalSidedWorld w, BlockPos.PooledMutableBlockPos p) {
		p.setPos(0, 0, 0);
		if (!MechMBPart.isValidCenter(w.getBlockState(p))) {
			return false;
		}
		p.setPos(0, -1, 0);
		return MechMBPart.isValidCenter(w.getBlockState(p));
	}

	private void formEnd(LocalSidedWorld w, BlockPos.PooledMutableBlockPos p, MechanicalMBBlockType type,
			BiConsumer<TileEntityMultiblockConverter, Boolean> init) {
		p.setPos(0, 0, 0);
		w.setBlockState(p, IndustrialWires.mechanicalMB.getStateFromMeta(type.ordinal()));
		TileEntity te = w.getTileEntity(p);
		if (te instanceof TileEntityMultiblockConverter) {
			init.accept((TileEntityMultiblockConverter) te, true);
		}
		p.setPos(0, -1, 0);
		w.setBlockState(p, IndustrialWires.mechanicalMB.getStateFromMeta(NO_MODEL.ordinal()));
		te = w.getTileEntity(p);
		if (te instanceof TileEntityMultiblockConverter) {
			init.accept((TileEntityMultiblockConverter) te, false);
		}
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
		boolean b = true;
		BlockPos.PooledMutableBlockPos mutPos = BlockPos.PooledMutableBlockPos.retain();
		Minecraft.getMinecraft().mouseHelper.ungrabMouseCursor();
		try {
			LocalSidedWorld w = new LocalSidedWorld(world, pos, side.getOpposite(), false);
			if (!checkEnd(w, mutPos)) {
				return false;
			}
			mirrorLoop:do {
				b = !b;
				w.setMirror(b);
				boolean done = false;
				List<MechMBPart> parts = new ArrayList<>();
				int lastLength = 1;
				double weight = 0;
				while (!done) {
					mutPos.setPos(0, 0, lastLength);
					w.setOrigin(w.getRealPos(mutPos));
					MechMBPart next = null;
					for (String key:MechMBPart.INSTANCES.keySet()) {
						MechMBPart it = MechMBPart.INSTANCES.get(key);
						if (it.canForm(w)) {
							next = it;
							MechMBPart.cacheNewInstance(key);
							break;
						}
					}
					if (next!=null) {
						parts.add(next);
						lastLength = next.getLength();
						weight += next.getWeight();
					} else {
						if (parts.size()>0&&checkEnd(w, mutPos)) {
							done = true;
						} else {
							continue mirrorLoop;
						}
					}
				}
				double finalWeight = weight;
				w.setOrigin(pos);
				formEnd(w, mutPos, END, (te, master)->{
					if (master) {
						te.offset = BlockPos.ORIGIN;
						te.setMechanical(parts.toArray(new MechMBPart[parts.size()]), 0);
						te.energyState = new MechEnergy(finalWeight, 0);
					} else {
						te.offset = new BlockPos(0, -1, 0);
					}
					te.facing = side;
				});
				lastLength = 1;
				Consumer<TileEntityMultiblockConverter> init = (te)-> {
					te.offset = te.getPos().subtract(pos);
					te.facing = side;
				};
				for (MechMBPart part:parts) {
					mutPos.setPos(0, 0, lastLength);
					w.setOrigin(w.getRealPos(mutPos));
					part.form(w, init);
					lastLength = part.getLength();
				}
				mutPos.setPos(0, 0, lastLength);
				w.setOrigin(w.getRealPos(mutPos));
				formEnd(w, mutPos, OTHER_END, (te, __)->init.accept(te));
				break;
			} while (!b);
			return false;
		} finally {
			mutPos.release();
		}
	}

	@Override
	public ItemStack[][][] getStructureManual() {
		return new ItemStack[0][][];
	}

	@Override
	public IngredientStack[] getTotalMaterials() {
		return new IngredientStack[0];
	}

	@Override
	public boolean overwriteBlockRender(ItemStack stack, int iterator) {
		return false;
	}

	@Override
	public float getManualScale() {
		return 1;
	}

	@Override
	public boolean canRenderFormedStructure() {
		return false;
	}

	@Override
	public void renderFormedStructure() {

	}
}
