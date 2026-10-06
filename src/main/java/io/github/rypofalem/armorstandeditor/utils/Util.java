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

package io.github.rypofalem.armorstandeditor.utils;

import io.github.rypofalem.armorstandeditor.ArmorStandEditorPlugin;
import io.github.rypofalem.armorstandeditor.modes.InvisibleEmptyMode;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;


public abstract class Util {

    public static final double FULL_CIRCLE = Math.PI * 2;

    private static final EquipmentSlot[] ARMORSTAND_SLOTS = {
            EquipmentSlot.HAND, EquipmentSlot.OFF_HAND,
            EquipmentSlot.HEAD, EquipmentSlot.CHEST,
            EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public static <T extends Enum<?>> String getEnumList(Class<T> enumType) {
        return getEnumList(enumType, " | ");
    }

    public static <T extends Enum<?>> String getEnumList(Class<T> enumType, String delimiter) {
        StringBuilder list = new StringBuilder();
        boolean put = false;
        for (Enum<?> e : enumType.getEnumConstants()) {
            list.append(e.toString()).append(delimiter);
            put = true;
        }
        if (put) list = new StringBuilder(list.substring(0, list.length() - delimiter.length()));
        return list.toString();
    }

    public static double addAngle(double current, double angleChange) {
        current += angleChange;
        current = fixAngle(current, angleChange);
        return current;
    }

    public static double subAngle(double current, double angleChange) {
        current -= angleChange;
        current = fixAngle(current, angleChange);
        return current;
    }

    //clamps angle to 0 if it exceeds 2PI rad (360 degrees), is closer to 0 than angleChange value, or is closer to 2PI rad than 2PI rad - angleChange value.
    private static double fixAngle(double angle, double angleChange) {
        if (angle > FULL_CIRCLE) {
            return 0;
        }

        if (angle > 0 && angle < angleChange && angle < angleChange / 2) {
            return 0;
        }

        if (angle > FULL_CIRCLE - angle && angle > FULL_CIRCLE - (angleChange / 2)) {
            return 0;
        }

        return angle;
    }

    public static void applyEmptyStandMode(ArmorStand as, InvisibleEmptyMode mode, NamespacedKey autoGlowKey) {
        PersistentDataContainer pdc = as.getPersistentDataContainer();
        boolean autoGlow = pdc.has(autoGlowKey, PersistentDataType.BYTE);

        EntityEquipment eq = as.getEquipment();
        boolean empty = Arrays.stream(ARMORSTAND_SLOTS).allMatch(s -> eq.getItem(s).getType().isAir());
        boolean orphan = !as.isVisible() && !as.isCustomNameVisible() && empty;
        ArmorStandEditorPlugin.instance().debug.log("[DEBUG] empty=" + empty
                + " nameVisible=" + as.isCustomNameVisible() + " orphan=" + orphan + " mode=" + mode);


        if (orphan && mode == InvisibleEmptyMode.VISIBLE) {
            as.setVisible(true);
        } else if (orphan && mode == InvisibleEmptyMode.GLOW) {
            if (!as.isGlowing()) {
                as.setGlowing(true);
                pdc.set(autoGlowKey, PersistentDataType.BYTE, (byte) 1);
            }
        } else if (autoGlow) {
            // Only undo glow we applied; never touch ASE's own targeting/lock glow
            as.setGlowing(false);
            pdc.remove(autoGlowKey);
        }
    }
}
