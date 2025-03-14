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
package malte0811.industrialWires.blocks.wire;

import blusunrize.immersiveengineering.api.IEProperties;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.BlockIWBase;
import malte0811.industrialWires.blocks.IMetaEnum;
import malte0811.industrialWires.wires.IC2Wiretype;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class BlockIC2Connector extends BlockIWBase implements IMetaEnum {
	private static PropertyEnum<BlockTypes_IC2_Connector> type = PropertyEnum.create("type", BlockTypes_IC2_Connector.class);

	public BlockIC2Connector() {
		super(Material.IRON, "ic2Connector");
		setHardness(3.0F);
		setResistance(15.0F);
		lightOpacity = 0;
		this.setCreativeTab(IndustrialWires.creativeTab);
	}
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityIC2ConnectorTin) {
			TileEntityIC2ConnectorTin connector = (TileEntityIC2ConnectorTin) te;
			if (world.isAirBlock(pos.offset(connector.f))) {
				this.dropBlockAsItem(connector.getWorld(), pos, world.getBlockState(pos), 0);
				connector.getWorld().setBlockToAir(pos);
			}
		}
	}

	@Override
	public void getSubBlocks(@Nonnull Item itemIn, CreativeTabs tab, List<ItemStack> list) {
		for (int i = 0;i<type.getAllowedValues().size();i++) {
			list.add(new ItemStack(itemIn, 1, i));
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {
		BlockStateContainer base = super.createBlockState();
		IUnlistedProperty<?>[] unlisted = (base instanceof ExtendedBlockState) ? ((ExtendedBlockState) base).getUnlistedProperties().toArray(new IUnlistedProperty[0]) : new IUnlistedProperty[0];
		unlisted = Arrays.copyOf(unlisted, unlisted.length + 1);
		unlisted[unlisted.length - 1] = IEProperties.CONNECTIONS;
		return new ExtendedBlockState(this, base.getProperties().toArray(new IProperty[0]), unlisted);
	}

	@Override
	protected IProperty<?>[] getProperties() {
		return new IProperty[]{type, IEProperties.FACING_ALL};
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		state = super.getActualState(state, worldIn, pos);
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityIC2ConnectorTin) {
			state.withProperty(IEProperties.FACING_ALL, ((TileEntityIC2ConnectorTin) te).getFacing());
		}
		return state;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return super.getStateFromMeta(meta).withProperty(type, BlockTypes_IC2_Connector.values()[meta]);
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return false;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		switch (state.getValue(type)) {
		case TIN_CONN:
			return new TileEntityIC2ConnectorTin(false);
		case TIN_RELAY:
			return new TileEntityIC2ConnectorTin(true);
		case COPPER_CONN:
			return new TileEntityIC2ConnectorCopper(false);
		case COPPER_RELAY:
			return new TileEntityIC2ConnectorCopper(true);
		case GOLD_CONN:
			return new TileEntityIC2ConnectorGold(false);
		case GOLD_RELAY:
			return new TileEntityIC2ConnectorGold(true);
		case HV_CONN:
			return new TileEntityIC2ConnectorHV(false);
		case HV_RELAY:
			return new TileEntityIC2ConnectorHV(true);
		case GLASS_CONN:
			return new TileEntityIC2ConnectorGlass(false);
		case GLASS_RELAY:
			return new TileEntityIC2ConnectorGlass(true);
		}
		return null;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced) {
		super.addInformation(stack, player, tooltip, advanced);
		if (stack!=null && stack.getMetadata() % 2 == 0) {
			int type = stack.getMetadata() / 2;
			tooltip.add(I18n.format(IndustrialWires.MODID + ".tooltip.power_tier", type + 1));
			tooltip.add(I18n.format(IndustrialWires.MODID + ".tooltip.eu_per_tick", IC2Wiretype.IC2_TYPES[type].getTransferRate() / 8));
		}
	}

	@Override
	public boolean canRenderInLayer(IBlockState state, @Nonnull BlockRenderLayer layer) {
		return layer == BlockRenderLayer.TRANSLUCENT || layer == BlockRenderLayer.SOLID;
	}

	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	@Override
	public boolean isVisuallyOpaque() {
		return false;
	}
	@Override
	public Object[] getValues() {
		return BlockTypes_IC2_Connector.values();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(type).ordinal();
	}
}
