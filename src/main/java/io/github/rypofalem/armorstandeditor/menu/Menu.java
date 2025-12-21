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

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.datacomponent.item.PotionContents;
import io.papermc.paper.datacomponent.item.TooltipDisplay;

import net.kyori.adventure.text.Component;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionType;

public class Menu {
    private final Inventory menuInv;
    private final PlayerEditor pe;
    Component name;
    private Debug debug;

    public Menu(PlayerEditor pe) {
        this.pe = pe;
        this.debug = pe.plugin.debug;
        name = pe.plugin.getLang().getMessage("mainmenutitle", "menutitle");
        menuInv = Bukkit.createInventory(pe.getManager().getMenuHolder(), 54, name);
        fillInventory();
    }

    private void fillInventory() {

        menuInv.clear();

        ItemStack xAxis;
        ItemStack yAxis;
        ItemStack zAxis;
        ItemStack coarseAdj;
        ItemStack fineAdj;
        ItemStack rotate = null;
        ItemStack headPos;
        ItemStack rightArmPos;
        ItemStack bodyPos;
        ItemStack leftArmPos;
        ItemStack reset;
        ItemStack showArms;
        ItemStack visibility;
        ItemStack size = null;
        ItemStack rightLegPos;
        ItemStack glowing;
        ItemStack leftLegPos;
        ItemStack plate = null;
        ItemStack copy = null;
        ItemStack paste = null;
        ItemStack slot1 = null;
        ItemStack slot2 = null;
        ItemStack slot3 = null;
        ItemStack slot4 = null;
        ItemStack help;
        ItemStack itemFrameVisible;
        ItemStack blankSlot;
        ItemStack presetItem = null;

        //Variables that need to be Initialized
        ItemStack place = null;
        ItemStack equipment = null;
        ItemStack disableSlots = null;
        ItemStack gravity = null;
        ItemStack playerHead = null;
        ItemStack toggleVulnerabilty = null;

        //Slots with No Value
        blankSlot = createIcon(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE),
            "blankslot", "");

        //Axis - X, Y, Z for Movement
        xAxis = createIcon(ItemStack.of(Material.RED_CONCRETE),
            "xaxis", "axis x");

        yAxis = createIcon(ItemStack.of(Material.GREEN_CONCRETE),
            "yaxis", "axis y");

        zAxis = createIcon(ItemStack.of(Material.BLUE_CONCRETE),
            "zaxis", "axis z");

        //Movement Speed
        coarseAdj = createIcon(ItemStack.of(Material.COARSE_DIRT),
            "coarseadj", "adj coarse");

        fineAdj = createIcon(ItemStack.of(Material.SMOOTH_SANDSTONE),
            "fineadj", "adj fine");

        //Reset Changes
        reset = createIcon(ItemStack.of(Material.WATER_BUCKET),
            "reset", "mode reset");

        //Which Part to Move
        headPos = createIcon(ItemStack.of(Material.IRON_HELMET),
            "head", "mode head");

        bodyPos = createIcon(ItemStack.of(Material.IRON_CHESTPLATE),
            "body", "mode body");

        leftLegPos = createIcon(ItemStack.of(Material.IRON_LEGGINGS),
            "leftleg", "mode leftleg");

        rightLegPos = createIcon(ItemStack.of(Material.IRON_LEGGINGS),
            "rightleg", "mode rightleg");

        leftArmPos = createIcon(ItemStack.of(Material.STICK),
            "leftarm", "mode leftarm");

        rightArmPos = createIcon(ItemStack.of(Material.STICK),
            "rightarm", "mode rightarm");

        showArms = createIcon(ItemStack.of(Material.STICK),
            "showarms", "mode showarms");

        presetItem = createIcon(ItemStack.of(Material.BOOKSHELF), "presetmenu", "mode preset");

        //Praise Start - Sikatsu and cowgod, Nicely spotted this being broken
        if (pe.getPlayer().hasPermission("asedit.togglearmorstandvisibility") ||
            pe.plugin.getArmorStandVisibility()) {
            visibility = ItemStack.of(Material.POTION);
            visibility.setData(DataComponentTypes.POTION_CONTENTS, PotionContents.potionContents().potion(PotionType.INVISIBILITY).build());
            createIcon(visibility, "invisible", "mode invisible");
        } else {
            visibility = blankSlot;
        }
        if (pe.getPlayer().hasPermission("asedit.toggleitemframevisibility") ||
            pe.plugin.getItemFrameVisibility()) {
            itemFrameVisible = ItemStack.of(Material.ITEM_FRAME);
            createIcon(itemFrameVisible, "itemframevisible", "mode itemframe");
        } else {
            itemFrameVisible = blankSlot;
        }

        //Praise end

        if (pe.getPlayer().hasPermission("asedit.toggleInvulnerability")) {
            toggleVulnerabilty = createIcon(ItemStack.of(Material.TOTEM_OF_UNDYING),
                "vulnerability", "mode vulnerability");
        } else {
            toggleVulnerabilty = blankSlot;
        }

        if (pe.getPlayer().hasPermission("asedit.togglesize")) {
            size = createIcon(ItemStack.of(Material.PUFFERFISH),
                "size", "mode size");
        } else {
            size = blankSlot;
        }

        if (pe.getPlayer().hasPermission("asedit.disableslots")) {
            disableSlots = createIcon(ItemStack.of(Material.BARRIER), "disableslots", "mode disableslots");
        } else {
            disableSlots = blankSlot;
        }

        if (pe.getPlayer().hasPermission("asedit.togglegravity")) {
            gravity = createIcon(ItemStack.of(Material.SAND), "gravity", "mode gravity");
        } else {
            gravity = blankSlot;
        }

        if (pe.getPlayer().hasPermission("asedit.togglebaseplate")) {
            plate = createIcon(ItemStack.of(Material.SMOOTH_STONE_SLAB),
                "baseplate", "mode baseplate");
        } else {
            plate = blankSlot;
        }

        if (pe.getPlayer().hasPermission("asedit.movement")) {
            place = createIcon(ItemStack.of(Material.RAIL),
                "placement", "mode placement");
        } else {
            place = blankSlot;
        }

        if (pe.getPlayer().hasPermission("asedit.rotation")) {
            rotate = createIcon(ItemStack.of(Material.COMPASS),
                "rotate", "mode rotate");
        } else {
            rotate = blankSlot;
        }

        if (pe.getPlayer().hasPermission("asedit.equipment")) {
            equipment = createIcon(ItemStack.of(Material.CHEST),
                "equipment", "mode equipment");
        } else {
            equipment = blankSlot;
        }

        if (pe.getPlayer().hasPermission("asedit.copy")) {
            copy = createIcon(ItemStack.of(Material.FLOWER_BANNER_PATTERN),
                "copy", "mode copy");

            slot1 = createIcon(ItemStack.of(Material.BOOK),
                "copyslot", "slot 1", "1");

            slot2 = createIcon(ItemStack.of(Material.BOOK, 2),
                "copyslot", "slot 2", "2");

            slot3 = createIcon(ItemStack.of(Material.BOOK, 3),
                "copyslot", "slot 3", "3");

            slot4 = createIcon(ItemStack.of(Material.BOOK, 4),
                "copyslot", "slot 4", "4");
        }

        if (pe.getPlayer().hasPermission("asedit.paste")) {
            paste = createIcon(ItemStack.of(Material.FEATHER),
                "paste", "mode paste");
        }

        if (pe.getPlayer().hasPermission("asedit.head") || pe.plugin.getallowedToRetrieveOwnPlayerHead()) {
            playerHead = createIcon(ItemStack.of(Material.PLAYER_HEAD),
                "playerheadmenu",
                "playerhead");
        } else {
            playerHead = blankSlot;
        }

        if (pe.getPlayer().hasPermission("asedit.togglearmorstandglow")) {
            glowing = createIcon(ItemStack.of(Material.GLOW_INK_SAC, 1),
                "armorstandglow",
                "mode armorstandglow");
        } else {
            glowing = blankSlot;
        }

        help = createIcon(new ItemStack(Material.NETHER_STAR), "helpgui", "help");

        ItemStack[] items = {

            blankSlot, blankSlot, blankSlot, xAxis, yAxis, zAxis, blankSlot, blankSlot, help,
            copy, paste, blankSlot, playerHead, headPos, reset, blankSlot, itemFrameVisible, glowing,
            slot1, slot2, blankSlot, rightArmPos, bodyPos, leftArmPos, blankSlot, rotate, place,
            slot3, slot4, blankSlot, rightLegPos, equipment, leftLegPos, blankSlot, coarseAdj, fineAdj,
            presetItem, blankSlot, blankSlot, blankSlot, blankSlot, blankSlot, blankSlot, blankSlot, disableSlots,
            blankSlot, showArms, visibility, size, blankSlot, plate, toggleVulnerabilty, gravity, blankSlot
        };

        menuInv.setContents(items);
    }

    private ItemStack createIcon(ItemStack icon, String path, String command) {
        return createIcon(icon, path, command, null);
    }

    private ItemStack createIcon(ItemStack icon, String path, String command, String option) {

        if (!command.isEmpty()) {
            icon.editPersistentDataContainer(pdc -> pdc.set(pe.plugin.getIconKey(), PersistentDataType.STRING, "ase " + command));
            icon.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay()
                .addHiddenComponents(DataComponentTypes.POTION_CONTENTS)
                .addHiddenComponents(DataComponentTypes.ATTRIBUTE_MODIFIERS)
                .build());
        } else {
            icon.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true).build());
        }

        icon.setData(DataComponentTypes.CUSTOM_NAME, getIconName(path, option));
        icon.setData(DataComponentTypes.LORE, ItemLore.lore().addLine(getIconDescription(path, option)).build());

        return icon;
    }


    private Component getIconName(String path, String option) {
        return pe.plugin.getLang().getMessage(path, "iconname", option);
    }


    private Component getIconDescription(String path, String option) {
        return pe.plugin.getLang().getMessage(path + ".description", "icondescription", option);
    }

    public void openMenu() {
        if (pe.getPlayer().hasPermission("asedit.basic")) {
            fillInventory();
            debug.log("Player '" + pe.getPlayer().getName() + "' has opened the Main ASE Menu");
            pe.getPlayer().openInventory(menuInv);
        }
    }
}