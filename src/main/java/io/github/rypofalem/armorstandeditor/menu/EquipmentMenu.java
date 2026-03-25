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

package io.github.rypofalem.armorstandeditor.menu;

import io.github.rypofalem.armorstandeditor.Debug;
import io.github.rypofalem.armorstandeditor.PlayerEditor;
import io.github.rypofalem.armorstandeditor.coreprotect.CoreProtectExtension;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;


@SuppressWarnings("UnstableApiUsage")
public class EquipmentMenu {
    Inventory menuInv;
    private Debug debug;
    private PlayerEditor pe;
    private ArmorStand armorstand;
    ItemStack helmet, chest, pants, feetsies, rightHand, leftHand = ItemStack.of(Material.AIR);
    ItemStack oldHelmet, oldChest, oldPants, oldFeetsies, oldRightHand, oldLeftHand = ItemStack.of(Material.AIR);

    public EquipmentMenu(PlayerEditor pe, ArmorStand as) {
        this.pe = pe;
        this.armorstand = as;
        this.debug = pe.plugin.debug;
        Component name = pe.plugin.getLang().getMessage("equiptitle", "menutitle");
        menuInv = Bukkit.createInventory(pe.getManager().getEquipmentHolder(), 18, name);
    }

    private void fillInventory() {
        menuInv.clear();
        EntityEquipment equipment = armorstand.getEquipment();
        ItemStack helmet = equipment.getHelmet();
        ItemStack chest = equipment.getChestplate();
        ItemStack pants = equipment.getLeggings();
        ItemStack feetsies = equipment.getBoots();
        ItemStack rightHand = equipment.getItemInMainHand();
        ItemStack leftHand = equipment.getItemInOffHand();
        oldHelmet = helmet;
        oldChest = chest;
        oldPants = pants;
        oldFeetsies = feetsies;
        oldRightHand = rightHand;
        oldLeftHand = leftHand;
        equipment.clear();

        ItemStack disabledIcon = ItemStack.of(Material.BARRIER);
        disabledIcon.setData(DataComponentTypes.CUSTOM_NAME,
            pe.plugin.getLang().getMessage("disabled", "warn")); //equipslot.msg <option>
        disabledIcon.editPersistentDataContainer(
            pdc -> pdc.set(pe.plugin.getIconKey(), PersistentDataType.STRING, "ase icon")); // mark as icon)


        ItemStack helmetIcon = createIcon(Material.LEATHER_HELMET, "helm");
        ItemStack chestIcon = createIcon(Material.LEATHER_CHESTPLATE, "chest");
        ItemStack pantsIcon = createIcon(Material.LEATHER_LEGGINGS, "pants");
        ItemStack feetsiesIcon = createIcon(Material.LEATHER_BOOTS, "boots");
        ItemStack rightHandIcon = createIcon(Material.WOODEN_SWORD, "rhand");
        ItemStack leftHandIcon = createIcon(Material.SHIELD, "lhand");
        ItemStack[] items =
            {
                helmetIcon, chestIcon, pantsIcon, feetsiesIcon, rightHandIcon, leftHandIcon, disabledIcon, disabledIcon, disabledIcon,
                helmet, chest, pants, feetsies, rightHand, leftHand, disabledIcon, disabledIcon, disabledIcon
            };
        menuInv.setContents(items);
    }

    private ItemStack createIcon(Material mat, String slot) {
        ItemStack icon = ItemStack.of(mat);

        // 1. Get the friendly name (e.g., "Helmet") from the config
        // This looks at 'equipslot.helm'
        String friendlyName = pe.plugin.getLang().getString("equipslot." + slot);

        // 2. Get the description name (e.g., "Helmet" or "Feetsies")
        // This looks at 'equipslot.description.helm'
        String friendlyDesc = pe.plugin.getLang().getString("equipslot.description." + slot);

        icon.editPersistentDataContainer(
            pdc -> pdc.set(pe.plugin.getIconKey(), PersistentDataType.STRING, "ase icon"));

        // 3. Pass the friendly name into the <x> placeholder
        icon.setData(DataComponentTypes.CUSTOM_NAME,
            pe.plugin.getLang().getMessage("equipslot", "iconname", friendlyName));

        // 4. Pass the description-friendly name into the <x> placeholder
        icon.setData(DataComponentTypes.LORE, ItemLore.lore()
            .addLine(pe.plugin.getLang().getMessage("equipslot.description", "icondescription", friendlyDesc)));

        icon.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay()
            .addHiddenComponents(DataComponentTypes.ATTRIBUTE_MODIFIERS).build());

        return icon;
    }

    public void openMenu() {
        pe.getPlayer().closeInventory();
        if (pe.getPlayer().hasPermission("asedit.equipment")) {
            fillInventory();
            debug.log("Player '" + pe.getPlayer().getName() + "' has opened the Equipment Menu.");
            pe.getPlayer().openInventory(menuInv);
        }
    }

    public void equipArmorstand() {
        helmet = notNull(menuInv.getItem(9));
        chest = notNull(menuInv.getItem(10));
        pants = notNull(menuInv.getItem(11));
        feetsies = notNull(menuInv.getItem(12));
        rightHand = notNull(menuInv.getItem(13));
        leftHand = notNull(menuInv.getItem(14));

        EntityEquipment equipment = armorstand.getEquipment();
        equipment.setHelmet(helmet);
        equipment.setChestplate(chest);
        equipment.setLeggings(pants);
        equipment.setBoots(feetsies);
        equipment.setItemInMainHand(rightHand);
        equipment.setItemInOffHand(leftHand);

        checkForChanges();
    }

    private void checkForChanges() {
        debug.log("Equipping ArmorStand and checking changes.");
        Player player = pe.getPlayer();
        ItemStack[] oldArray = new ItemStack[]{oldHelmet, oldChest, oldPants, oldFeetsies, oldRightHand, oldLeftHand};
        ItemStack[] newArray = new ItemStack[]{helmet, chest, pants, feetsies, rightHand, leftHand};

        boolean change = false;
        if (hasChanged(oldHelmet, helmet)) {
            debug.log("Helmet changed from " + oldHelmet + " to " + helmet);
            oldHelmet = helmet;
            change = true;
        }
        if (hasChanged(oldChest, chest)) {
            debug.log("Chest changed from " + oldChest + " to " + chest);
            oldChest = chest;
            change = true;
        }
        if (hasChanged(oldPants, pants)) {
            debug.log("Pants changed from " + oldPants + " to " + pants);
            oldPants = pants;
            change = true;
        }
        if (hasChanged(oldFeetsies, feetsies)) {
            debug.log("Boots changed from " + oldFeetsies + " to " + feetsies);
            oldFeetsies = feetsies;
            change = true;
        }
        if (hasChanged(oldRightHand, rightHand)) {
            debug.log("R-Hand changed from " + oldRightHand + " to " + rightHand);
            oldRightHand = rightHand;
            change = true;
        }
        if (hasChanged(oldLeftHand, leftHand)) {
            debug.log("L-Hand changed from " + oldLeftHand + " to " + leftHand);
            oldLeftHand = leftHand;
            change = true;
        }

        if (change) {
            CoreProtectExtension.logChange(player, armorstand, oldArray, newArray);
        }
    }

    private boolean hasChanged(@Nullable ItemStack before, @Nullable ItemStack after) {
        if (before == null && after == null) return false;
        return before == null || !before.equals(after);
    }

    private ItemStack notNull(@Nullable ItemStack item) {
        return item != null ? item : ItemStack.of(Material.AIR);
    }
}
