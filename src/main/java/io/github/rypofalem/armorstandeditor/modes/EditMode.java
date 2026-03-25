/*
 * ArmorStandEditor: Bukkit plugin to allow editing armor stand attributes
 * Copyright (C) 2016-2023  RypoFalem
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package io.github.rypofalem.armorstandeditor.modes;

public enum EditMode {
    NONE("None"), INVISIBLE("Invisible"), SHOWARMS("ShowArms"), GRAVITY("Gravity"), BASEPLATE("BasePlate"), SIZE("Size"),
    COPY("Copy"), PASTE("Paste"), HEAD("Head"), BODY("Body"), LEFTARM("LeftArm"), RIGHTARM("RightArm"), LEFTLEG("LeftLeg"),
    RIGHTLEG("RightLeg"), PLACEMENT("Placement"), DISABLESLOTS("DisableSlots"), ROTATE("Rotate"), EQUIPMENT("Equipment"), PRESET("Preset"),
    RESET("Reset"), ITEMFRAME("ItemFrame"), ITEMFRAMEGLOW("ItemFrameGlow"), VULNERABILITY("Vulnerability"), PLAYERHEAD("playerheadmenu"),
    GLOWING("armorstandglow");

    private String name;

    EditMode(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
