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

package malte0811.industrialWires.blocks.controlpanel;

import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public enum BlockTypes_Panel implements IStringSerializable {
	TOP,
	RS_WIRE,
	DUMMY,
	CREATOR;

	@Override
	public String getName() {
		return toString().toLowerCase(Locale.ENGLISH);
	}

	public boolean isPanelConnector() {
		return this != CREATOR;
	}
}
