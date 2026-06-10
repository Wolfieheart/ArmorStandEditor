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

import java.util.Objects;


@SuppressWarnings("UnstableApiUsage")

public class EquipmentMenu {
    // Equipment slot indices in the inventory
    private static final int EQUIP_SLOT_HELMET = 9;
    private static final int EQUIP_SLOT_CHEST = 10;
    private static final int EQUIP_SLOT_PANTS = 11;
    private static final int EQUIP_SLOT_BOOTS = 12;
    private static final int EQUIP_SLOT_RIGHT_HAND = 13;
    private static final int EQUIP_SLOT_LEFT_HAND = 14;

    Inventory menuInv;
    private final Debug debug;
    private final PlayerEditor pe;
    private final ArmorStand armorstand;
    private ItemStack currentHelmet;
    private ItemStack currentChest;
    private ItemStack currentPants;
    private ItemStack currentBoots;
    private ItemStack currentRightHand;
    private ItemStack currentLeftHand = ItemStack.of(Material.AIR);
    private ItemStack oldHelmet;
    private ItemStack oldChest;
    private ItemStack oldPants;
    private ItemStack oldBoots;
    private ItemStack oldRightHand;
    private ItemStack oldLeftHand = ItemStack.of(Material.AIR);
    private final CoreProtectExtension coreProtectExtension;

    /**
     * Constructs an EquipmentMenu for editing armor stand equipment.
     *
     * @param pe the PlayerEditor managing this menu
     * @param as the ArmorStand to edit
     */
    public EquipmentMenu(PlayerEditor pe, ArmorStand as) {
        this.pe = pe;
        this.armorstand = as;
        this.debug = pe.plugin.debug;

        //noinspection ConstantConditions
        coreProtectExtension = pe.plugin.getCoreProtectExtension();

        Component name = pe.plugin.getLang().getMessage("equiptitle", "menutitle");
        menuInv = Bukkit.createInventory(pe.getManager().getEquipmentHolder(), 18, name);
    }

    private void fillInventory() {
        menuInv.clear();
        EntityEquipment equipment = armorstand.getEquipment();

        ItemStack itemHelmet = equipment.getHelmet();
        ItemStack itemChestplate = equipment.getChestplate();
        ItemStack itemLeggings = equipment.getLeggings();
        ItemStack itemBoots = equipment.getBoots();
        ItemStack itemMainHandItem = equipment.getItemInMainHand();
        ItemStack itemOffHandItem = equipment.getItemInOffHand();

        oldHelmet = itemHelmet;
        oldChest = itemChestplate;
        oldPants = itemLeggings;
        oldBoots = itemBoots;
        oldRightHand = itemMainHandItem;
        oldLeftHand = itemOffHandItem;
        equipment.clear();

        ItemStack disabledIcon = ItemStack.of(Material.BARRIER);
        disabledIcon.setData(DataComponentTypes.CUSTOM_NAME,
            pe.plugin.getLang().getMessage("disabled", "warn")); //equipslot.msg <option>
        disabledIcon.editPersistentDataContainer(
            pdc -> pdc.set(pe.plugin.getIconKey(), PersistentDataType.STRING, "ase icon")); // mark as icon


        ItemStack helmetIcon = createIcon(Material.LEATHER_HELMET, "helm");
        ItemStack chestIcon = createIcon(Material.LEATHER_CHESTPLATE, "currentChestplate");
        ItemStack pantsIcon = createIcon(Material.LEATHER_LEGGINGS, "pants");
        ItemStack bootsIcon = createIcon(Material.LEATHER_BOOTS, "boots");
        ItemStack rightHandIcon = createIcon(Material.WOODEN_SWORD, "rhand");
        ItemStack leftHandIcon = createIcon(Material.SHIELD, "lhand");
        ItemStack[] items =
            {
                helmetIcon, chestIcon, pantsIcon, bootsIcon, rightHandIcon, leftHandIcon, disabledIcon, disabledIcon, disabledIcon,
                    itemHelmet, itemChestplate, itemLeggings, itemBoots, itemMainHandItem, itemOffHandItem, disabledIcon, disabledIcon, disabledIcon
            };
        menuInv.setContents(items);
    }

    private ItemStack createIcon(Material mat, String slot) {
        ItemStack icon = ItemStack.of(mat);

        // 1. Get the friendly name (e.g., "Helmet") from the config
        // This looks at 'equipslot.helm'
        String friendlyName = pe.plugin.getLang().getString("equipslot." + slot);

        // 2. Get the description name (e.g., "Helmet" or "boots")
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

    /**
     * Opens the equipment menu for the player if they have permission.
     */
    public void openMenu() {
        pe.getPlayer().closeInventory();
        if (pe.getPlayer().hasPermission("asedit.equipment")) {
            fillInventory();
            debug.log(String.format("Player '%s' has opened the Equipment Menu.", pe.getPlayer().getName()));
            pe.getPlayer().openInventory(menuInv);
        }
    }

    /**
     * Equips the armor stand with items from the menu and checks for changes.
     */
    public void equipArmorstand() {
        currentHelmet = notNull(menuInv.getItem(EQUIP_SLOT_HELMET));
        currentChest = notNull(menuInv.getItem(EQUIP_SLOT_CHEST));
        currentPants = notNull(menuInv.getItem(EQUIP_SLOT_PANTS));
        currentBoots = notNull(menuInv.getItem(EQUIP_SLOT_BOOTS));
        currentRightHand = notNull(menuInv.getItem(EQUIP_SLOT_RIGHT_HAND));
        currentLeftHand = notNull(menuInv.getItem(EQUIP_SLOT_LEFT_HAND));

        EntityEquipment equipment = armorstand.getEquipment();
        equipment.setHelmet(currentHelmet);
        equipment.setChestplate(currentChest);
        equipment.setLeggings(currentPants);
        equipment.setBoots(currentBoots);
        equipment.setItemInMainHand(currentRightHand);
        equipment.setItemInOffHand(currentLeftHand);

        checkForChanges();
    }

    /**
     * Checks if equipment has changed and logs the change.
     */
    @SuppressWarnings("java:S2209")
    private void checkForChanges() {
        debug.log("Equipping ArmorStand and checking changes.");
        Player player = pe.getPlayer();
        ItemStack[] oldArray = new ItemStack[]{oldHelmet, oldChest, oldPants, oldBoots, oldRightHand, oldLeftHand};
        ItemStack[] newArray = new ItemStack[]{currentHelmet, currentChest, currentPants, currentBoots, currentRightHand, currentLeftHand};

        if (hasChanged(oldHelmet, currentHelmet)) {
            debug.log(String.format("Helmet changed from %s to %s", oldHelmet, currentHelmet));
            oldHelmet = currentHelmet;
        }
        if (hasChanged(oldChest, currentChest)) {
            debug.log(String.format("Chest changed from %s to %s", oldChest, currentChest));
            oldChest = currentChest;
        }
        if (hasChanged(oldPants, currentPants)) {
            debug.log(String.format("Pants changed from %s to %s", oldPants, currentPants));
            oldPants = currentPants;
        }
        if (hasChanged(oldBoots, currentBoots)) {
            debug.log(String.format("Boots changed from %s to %s", oldBoots, currentBoots));
            oldBoots = currentBoots;
        }
        if (hasChanged(oldRightHand, currentRightHand)) {
            debug.log(String.format("R-Hand changed from %s to %s", oldRightHand, currentRightHand));
            oldRightHand = currentRightHand;
        }
        if (hasChanged(oldLeftHand, currentLeftHand)) {
            debug.log(String.format("L-Hand changed from %s to %s", oldLeftHand, currentLeftHand));
            oldLeftHand = currentLeftHand;
        }

        coreProtectExtension.logChange(player, armorstand, oldArray, newArray);
    }

    /**
     * Checks if an item has changed between two states.
     * Considers items equal if both are null or if their item stacks are equal.
     *
     * @param before the item before the change
     * @param after  the item after the change
     * @return true if the items are different, false otherwise
     */
    private boolean hasChanged(@Nullable ItemStack before, @Nullable ItemStack after) {
        if (before == null && after == null) {
            return false;
        }
        return !Objects.equals(before, after);
    }

    /**
     * Returns the provided item stack, or an empty air stack if it is null.
     *
     * @param item the item stack to check
     * @return the item stack, or an empty air stack if null
     */
    private ItemStack notNull(@Nullable ItemStack item) {
        return item != null ? item : ItemStack.of(Material.AIR);
    }
}
