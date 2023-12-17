/*
 * ArmorStandEditor: Bukkit plugin to allow editing armor stand attributes
 * Copyright (C) 2016-2023RypoFalem
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA02110-1301, USA.
 */

package io.github.rypofalem.armorstandeditor.menu;

import io.github.rypofalem.armorstandeditor.ArmorStandEditorPlugin;
import io.github.rypofalem.armorstandeditor.PlayerEditor;
import io.github.rypofalem.armorstandeditor.utils.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collections;

public class Menu implements ItemFactory {
    private final Inventory menuInv;
    private final PlayerEditor pe;

    public Menu(PlayerEditor pe) {
        this.pe = pe;
        menuInv = Bukkit.createInventory(pe.getManager().getMenuHolder(), Configuration.getGUI().getInt("menu.size"), Configuration.color(Configuration.getGUI().getString("menu.title")));
        fillInventory();
    }

    private void fillInventory() {
        menuInv.clear();
        ConfigurationSection section = Configuration.getGUI().getConfigurationSection("menu.items");
        for (String keys : section == null ? Collections.<String>emptyList() : section.getKeys(false)) {
            ConfigurationSection itemSection = section.getConfigurationSection(keys);
            if (itemSection == null) continue;

            this.createItem(itemSection, menuInv, x -> createIcon(x, null));
        }
        this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.xaxis"), menuInv, x -> createIcon(x, "axis x"));
        this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.yaxis"), menuInv, x -> createIcon(x, "axis y"));
        this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.zaxis"), menuInv, x -> createIcon(x, "axis z"));
        this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.coarseadj"), menuInv, x -> createIcon(x, "adj coarse"));
        this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.fineadj"), menuInv, x -> createIcon(x, "adj fine"));
        this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.reset"), menuInv, x -> createIcon(x, "mode reset"));
        this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.head"), menuInv, x -> createIcon(x, "mode head"));
        this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.body"), menuInv, x -> createIcon(x, "mode body"));
        this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.leftleg"), menuInv, x -> createIcon(x, "mode leftleg"));
        this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.rightleg"), menuInv, x -> createIcon(x, "mode rightleg"));
        this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.leftarm"), menuInv, x -> createIcon(x, "mode leftarm"));
        this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.rightarm"), menuInv, x -> createIcon(x, "mode rightarm"));
        this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.showarms"), menuInv, x -> createIcon(x, "mode showarms"));

        if (pe.getPlayer().hasPermission("asedit.togglearmorstandvisibility") || pe.plugin.getArmorStandVisibility()) {
            this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.visibility"), menuInv, x -> createIcon(x, "mode invisible"));
        }
        if (pe.getPlayer().hasPermission("asedit.toggleitemframevisibility") || pe.plugin.getItemFrameVisibility()) {
            this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.itemFrameVisible"), menuInv, x -> createIcon(x, "mode itemframe"));
        }
        if (pe.getPlayer().hasPermission("asedit.toggleInvulnerability")) {
            this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.vulnerability"), menuInv, x -> createIcon(x, "mode vulnerability"));
        }
        if (pe.getPlayer().hasPermission("asedit.togglesize")) {
            this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.size"), menuInv, x -> createIcon(x, "mode size"));
        }
        if (pe.getPlayer().hasPermission("asedit.disableslots")) {
            this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.disableSlots"), menuInv, x -> createIcon(x, "mode disableslots"));
        }
        if (pe.getPlayer().hasPermission("asedit.togglegravity")) {
            this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.gravity"), menuInv, x -> createIcon(x, "mode gravity"));
        }
        if (pe.getPlayer().hasPermission("asedit.togglebaseplate")) {
            this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.plate"), menuInv, x -> createIcon(x, "mode baseplate"));
        }
        if (pe.getPlayer().hasPermission("asedit.movement")) {
            this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.placement"), menuInv, x -> createIcon(x, "mode placement"));
        }
        if (pe.getPlayer().hasPermission("asedit.rotation")) {
            this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.rotate"), menuInv, x -> createIcon(x, "mode rotate"));
        }
        if (pe.getPlayer().hasPermission("asedit.equipment")) {
            this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.equipment"), menuInv, x -> createIcon(x, "mode equipment"));
        }
        if (pe.getPlayer().hasPermission("asedit.copy")) {
            this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.copy"), menuInv, x -> createIcon(x, "mode copy"));
            this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.slot1"), menuInv, x -> createIcon(x, "slot 1"));
            this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.slot2"), menuInv, x -> createIcon(x, "slot 2"));
            this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.slot3"), menuInv, x -> createIcon(x, "slot 3"));
            this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.slot4"), menuInv, x -> createIcon(x, "slot 4"));
        }
        if (pe.getPlayer().hasPermission("asedit.paste")) {
            this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.paste"), menuInv, x -> createIcon(x, "mode paste"));
        }
        if (pe.getPlayer().hasPermission("asedit.head") && pe.plugin.getAllowedToRetrievePlayerHead()) {
            this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.playerHead"), menuInv, x -> createIcon(x, "playerhead"));
        }
        this.createItem(Configuration.getGUI().getConfigurationSection("menu.clickable-items.help"), menuInv, x -> createIcon(x, "help"));
    }

    private ItemStack createIcon(ItemStack icon, String command) {
        if (icon == null) return null;

        ItemMeta meta = icon.getItemMeta();
        assert meta != null;
        meta.getPersistentDataContainer().set(ArmorStandEditorPlugin.instance().getIconKey(), PersistentDataType.STRING, command == null ? "" : "ase " + command);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        icon.setItemMeta(meta);
        return icon;
    }

    public void openMenu() {
        if (!pe.getPlayer().hasPermission("asedit.basic")) return;

        fillInventory();
        pe.getPlayer().openInventory(menuInv);
    }
}