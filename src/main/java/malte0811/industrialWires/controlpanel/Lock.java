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

package malte0811.industrialWires.controlpanel;

import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.controlpanel.TileEntityPanel;
import malte0811.industrialWires.client.RawQuad;
import malte0811.industrialWires.client.gui.GuiPanelCreator;
import malte0811.industrialWires.items.ItemKey;
import malte0811.industrialWires.util.TriConsumer;
import net.minecraft.block.Block;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Lock extends PanelComponent implements IConfigurableComponent {
	private final static Random rand = new Random();
	@Nullable
	private NBTTagCompound keyNBT;
	private boolean turned;
	private boolean latching = false;
	private int rsOutputId;
	private int rsOutputChannel;
	private int ticksTillOff;
	private int lockID;

	public Lock() {
		super("lock");
		while (lockID==0) {
			lockID = rand.nextInt();
		}
	}

	public Lock(boolean latching, int rsOutputId, int rsOutputChannel) {
		this();
		this.latching = latching;
		this.rsOutputChannel = rsOutputChannel;
		this.rsOutputId = rsOutputId;
	}

	@Override
	protected void writeCustomNBT(NBTTagCompound nbt, boolean toItem) {
		if (!toItem) {
			nbt.setInteger("timeout", ticksTillOff);
			nbt.setBoolean("turned", turned);
			if (keyNBT != null) {
				nbt.setTag("key", keyNBT);
			}
		}
		nbt.setInteger("lockId", lockID);
		nbt.setBoolean(LATCHING, latching);
		nbt.setInteger(RS_CHANNEL, rsOutputChannel);
		nbt.setInteger(RS_ID, rsOutputId);
	}

	@Override
	protected void readCustomNBT(NBTTagCompound nbt) {
		ticksTillOff = nbt.getInteger("timeout");
		if (nbt.hasKey("key", 10)) {
			keyNBT = nbt.getCompoundTag("key");
		} else {
			keyNBT = null;
		}
		turned = nbt.getBoolean("turned");
		if (nbt.hasKey("lockId")) {
			lockID = nbt.getInteger("lockId");
		}
		latching = nbt.getBoolean(LATCHING);
		rsOutputChannel = nbt.getInteger(RS_CHANNEL);
		rsOutputId = nbt.getInteger(RS_ID);
	}

	private final static float size = .0625F;
	private final static float keyWidth = .125F * size;
	private final static float yOffset = size / 2 + .0001F;
	private final static float xOffset = (size - keyWidth) / 2;
	private final static float[] DARK_GRAY = {.4F, .4F, .4F};
	private final static int DARK_GRAY_INT = 0xFF686868;
	private final static float zOffset = keyWidth / 2;
	private final static float keyOffset = keyWidth;
	private final static float zOffsetLowerKey = size / 4;


	@Override
	public List<RawQuad> getQuads() {
		List<RawQuad> ret = new ArrayList<>(5);
		PanelUtils.addColoredBox(GRAY, GRAY, null, new Vector3f(0, 0, 0), new Vector3f(size, size / 2, size), ret, false);
		if (keyNBT != null) {
			Matrix4 mat = null;
			if (turned) {
				mat = new Matrix4();
				mat.translate(size / 2, 0, size / 2);
				mat.rotate(Math.PI / 2, 0, 1, 0);
				mat.translate(-size / 2, 0, -size / 2);
			}
			addKey(ret, mat);
		} else {
			PanelUtils.addColoredQuad(ret, new Vector3f(xOffset + keyWidth, yOffset, zOffsetLowerKey), new Vector3f(xOffset, yOffset, zOffsetLowerKey),
					new Vector3f(xOffset, yOffset, size - zOffsetLowerKey), new Vector3f(xOffset + keyWidth, yOffset, size - zOffsetLowerKey),
					EnumFacing.UP, DARK_GRAY);
		}
		return ret;
	}

	private void addKey(List<RawQuad> out, Matrix4 mat) {
		PanelUtils.addColoredBox(DARK_GRAY, DARK_GRAY, null, new Vector3f(xOffset, size / 2, zOffsetLowerKey), new Vector3f(keyWidth, keyOffset, size / 2), out, false, mat);
		PanelUtils.addColoredBox(DARK_GRAY, DARK_GRAY, null, new Vector3f(xOffset, size / 2 + keyOffset, zOffset), new Vector3f(keyWidth, size, size - 2 * zOffset), out, false, mat);
	}

	@Override
	@Nonnull
	public PanelComponent copyOf() {
		Lock ret = new Lock(latching, rsOutputId, rsOutputChannel);
		ret.turned = turned;
		ret.keyNBT = keyNBT == null ? null : keyNBT.copy();
		ret.ticksTillOff = ticksTillOff;
		ret.setX(x);
		ret.setY(y);
		ret.panelHeight = panelHeight;
		return ret;
	}

	@Nonnull
	@Override
	public AxisAlignedBB getBlockRelativeAABB() {
		if (aabb == null) {
			aabb = new AxisAlignedBB(x, 0, y, x + size, getHeight(), y + size);
		}
		return aabb;
	}

	@Override
	public void interactWith(Vec3d hitRel, TileEntityPanel tile, EntityPlayerMP player) {
		boolean update = false;
		if (keyNBT == null) {
			for (EnumHand hand : EnumHand.values()) {
				ItemStack held = player.getHeldItem(hand);
				if (held!=null && held.getItem() == IndustrialWires.key && ItemKey.idForKey(held) == lockID) {
					keyNBT = held.serializeNBT();
					player.setHeldItem(hand, null);
					break;
				}
			}
		} else if (!turned) {
			if (player.isSneaking() && player.getHeldItemMainhand()== null) {
				player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.loadItemStackFromNBT(keyNBT));
				keyNBT = null;
			} else {
				turned = true;
			}
			update = true;
		} else {
			if (latching) {
				turned = false;
				update = true;
			} else {
				ticksTillOff = 10;
			}
		}
		if (update) {
			setOut(tile);
			if (!latching && turned) {
				ticksTillOff = 10;
			}
		}
		tile.triggerRenderUpdate();
	}

	@Override
	public void update(TileEntityPanel tile) {
		if (!latching && ticksTillOff > 0) {
			ticksTillOff--;
			tile.markDirty();
			if (ticksTillOff == 0) {
				turned = false;
				tile.triggerRenderUpdate();
				setOut(tile);
			}
		}
	}

	@Override
	public void registerRSOutput(int id, @Nonnull TriConsumer<Integer, Byte, PanelComponent> out) {
		if (id == rsOutputId) {
			super.registerRSOutput(id, out);
			out.accept(rsOutputChannel, (byte) (turned ? 15 : 0), this);
		}
	}

	@Override
	public float getHeight() {
		return size / 2;
	}

	@Override
	public void renderInGUI(GuiPanelCreator gui) {
		renderInGUIDefault(gui, GRAY_INT);
		AxisAlignedBB aabb = getBlockRelativeAABB();
		int left = (int) (gui.getX0() + (aabb.minX+xOffset) * gui.panelSize);
		int top = (int) (gui.getY0() + (aabb.minZ+zOffsetLowerKey) * gui.panelSize);
		int right = (int) (gui.getX0() + (aabb.maxX-xOffset) * gui.panelSize);
		int bottom = (int) (gui.getY0() + (aabb.maxZ-zOffsetLowerKey) * gui.panelSize);
		Gui.drawRect(left, top, right, bottom, DARK_GRAY_INT);
	}

	@Override
	public void invalidate(TileEntityPanel te) {
		setOut(rsOutputChannel, 0);
	}

	private void setOut(TileEntityPanel tile) {
		tile.triggerRenderUpdate();
		setOut(rsOutputChannel, turned ? 15 : 0);
	}

	@Override
	public void dropItems(TileEntityPanel te) {
		super.dropItems(te);
		if (keyNBT!=null) {
			Block.spawnAsEntity(te.getWorld(), te.getPos(), ItemStack.loadItemStackFromNBT(keyNBT));
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		Lock lock = (Lock) o;

		if (turned != lock.turned) return false;
		if (latching != lock.latching) return false;
		if (rsOutputId != lock.rsOutputId) return false;
		if (rsOutputChannel != lock.rsOutputChannel) return false;
		if (ticksTillOff != lock.ticksTillOff) return false;
		if (lockID != lock.lockID) return false;
		return keyNBT != null ? keyNBT.equals(lock.keyNBT) : lock.keyNBT == null;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (keyNBT != null ? keyNBT.hashCode() : 0);
		result = 31 * result + (turned ? 1 : 0);
		result = 31 * result + (latching ? 1 : 0);
		result = 31 * result + rsOutputId;
		result = 31 * result + rsOutputChannel;
		result = 31 * result + ticksTillOff;
		result = 31 * result + lockID;
		return result;
	}

	@Override
	public void applyConfigOption(ConfigType type, int id, NBTBase value) {
		switch (type) {
		case BOOL:
			if (id == 0) {
				latching = ((NBTTagByte) value).getByte() != 0;
			}
			break;
		case RS_CHANNEL:
			if (id == 0) {
				rsOutputChannel = ((NBTTagByte) value).getByte();
			}
			break;
		case INT:
			if (id == 0) {
				rsOutputId = ((NBTTagInt) value).getInt();
			}
			break;
		}
	}

	@Override
	public String fomatConfigName(ConfigType type, int id) {
		switch (type) {
		case BOOL:
			return I18n.format(IndustrialWires.MODID + ".desc.latching");
		case RS_CHANNEL:
		case INT:
			return null;
		default:
			return "INVALID";
		}
	}

	@Override
	public String fomatConfigDescription(ConfigType type, int id) {
		switch (type) {
		case BOOL:
			return I18n.format(IndustrialWires.MODID + ".desc.latching_info");
		case RS_CHANNEL:
			return I18n.format(IndustrialWires.MODID + ".desc.rschannel_info");
		case INT:
			return I18n.format(IndustrialWires.MODID + ".desc.rsid_info");
		default:
			return "INVALID?";
		}
	}

	@Override
	public RSChannelConfig[] getRSChannelOptions() {
		return new RSChannelConfig[]{new RSChannelConfig("channel", 0, 0, (byte) rsOutputChannel)};
	}

	@Override
	public IntConfig[] getIntegerOptions() {
		return new IntConfig[]{new IntConfig("rsId", 0, 50, rsOutputId, 2, false)};
	}

	@Override
	public BoolConfig[] getBooleanOptions() {
		return new BoolConfig[]{new BoolConfig("latching", 0, 70, latching)};
	}

	@Override
	public int getColor() {
		return GRAY_INT;
	}

	public int getLockID() {
		return lockID;
	}
}
