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

import io.github.rypofalem.armorstandeditor.ArmorStandEditorPlugin;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

public class ArmorStandData {
    public EulerAngle headPos, leftArmPos, rightArmPos, bodyPos, leftLegPos, rightLegPos;
    public boolean visible, size, basePlate, gravity, showArms;
    public ItemStack head, body, legs, feetsies, rightHand, leftHand;
    public double attributeScale;
    private ArmorStandEditorPlugin plugin = ArmorStandEditorPlugin.instance();

    ArmorStandData(ArmorStand as) {
        this.headPos = as.getHeadPose();
        this.leftArmPos = as.getLeftArmPose();
        this.rightArmPos = as.getRightArmPose();
        this.bodyPos = as.getBodyPose();
        this.leftLegPos = as.getLeftLegPose();
        this.rightLegPos = as.getRightLegPose();
        this.size = as.isSmall();
        if (plugin.getNmsVersion().compareTo("1.21.4") >= 0 || plugin.getNmsVersion().compareTo("v1_21_R3") >= 0) {
            this.attributeScale = as.getAttribute(Attribute.SCALE).getValue();
        }
        this.basePlate = as.hasBasePlate();
        this.gravity = as.hasGravity();
        this.showArms = as.hasArms();
        this.visible = as.isVisible();

        //Fix for Depreciate Armor Stand STuff
        this.head = as.getEquipment().getHelmet();
        this.body = as.getEquipment().getChestplate();
        this.legs = as.getEquipment().getLeggings();
        this.feetsies = as.getEquipment().getBoots();
        this.rightHand = as.getEquipment().getItemInMainHand();
        this.leftHand = as.getEquipment().getItemInOffHand();
    }
}
