/*
 * armorstandeditor: Bukkit plugin to allow editing armor stand attributes
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

import io.github.rypofalem.armorstandeditor.ArmorStandEditorPlugin;
import io.github.rypofalem.armorstandeditor.Debug;
import io.github.rypofalem.armorstandeditor.PlayerEditor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;

public class PresetArmorPosesMenu {

    Inventory menuInv;
    private Debug debug;
    private final PlayerEditor pe;
    public ArmorStandEditorPlugin plugin = ArmorStandEditorPlugin.instance();
    private ArmorStand armorStand;
    static String name;

    public PresetArmorPosesMenu(PlayerEditor pe, ArmorStand as) {
        this.pe = pe;
        this.armorStand = as;
        this.debug = new Debug(pe.plugin);
        name = plugin.getLang().getMessage("presettitle", "menutitle");
        menuInv = Bukkit.createInventory(pe.getManager().getPresetHolder(), 36, name);
    }

    //PRESET NAMES
    final String VALUETOREPLACE = "§"+plugin.getLang().getFormat("info");
    final String SITTING = plugin.getLang().getMessage("sitting").replace(VALUETOREPLACE, "§2§n");
    final String WAVING = plugin.getLang().getMessage("waving").replace(VALUETOREPLACE, "§2§n");
    final String GREETING_1 = plugin.getLang().getMessage("greeting 1").replace(VALUETOREPLACE, "§2§n");
    final String GREETING_2 = plugin.getLang().getMessage("greeting 2").replace(VALUETOREPLACE, "§2§n");
    final String CHEERS = plugin.getLang().getMessage("cheers").replace(VALUETOREPLACE, "§2§n");
    final String ARCHER = plugin.getLang().getMessage("archer").replace(VALUETOREPLACE, "§2§n");
    final String DANCING = plugin.getLang().getMessage("dancing").replace(VALUETOREPLACE, "§2§n");
    final String HANGING = plugin.getLang().getMessage("hanging").replace(VALUETOREPLACE, "§2§n");
    final String PRESENTING = plugin.getLang().getMessage("present").replace(VALUETOREPLACE, "§2§n");
    final String FISHING = plugin.getLang().getMessage("fishing").replace(VALUETOREPLACE, "§2§n");

    //Menu Stuff
    final String BACKTOMENU = plugin.getLang().getMessage("backtomenu").replace(VALUETOREPLACE, "§2§n");
    final String HOWTO = plugin.getLang().getMessage("howtopreset").replace(VALUETOREPLACE, "§2§n");

    private void fillInventory() {
        menuInv.clear();

        /*
          Menu Set up in a similar way as to how we do it for
          the actual armorStand menu
         */

        //Blank Slots
        ItemStack blank = createIcon(new ItemStack(Material.BLACK_STAINED_GLASS_PANE, 1), "blankslot");

        //Presets -- Here to test things out, will get better names soon TM
        ItemStack sitting = createIcon(new ItemStack(Material.ARMOR_STAND, 1), "sitting");
        ItemStack waving = createIcon(new ItemStack(Material.ARMOR_STAND, 2), "waving");
        ItemStack greet1 = createIcon(new ItemStack(Material.ARMOR_STAND, 3), "greeting 1");
        ItemStack greet2 = createIcon(new ItemStack(Material.ARMOR_STAND, 4), "greeting 2");
        ItemStack cheer = createIcon(new ItemStack(Material.ARMOR_STAND, 5), "cheers");
        ItemStack archer = createIcon(new ItemStack(Material.ARMOR_STAND, 6), "archer");
        ItemStack dancing = createIcon(new ItemStack(Material.ARMOR_STAND, 7), "dancing");
        ItemStack hanging = createIcon(new ItemStack(Material.ARMOR_STAND, 8), "hanging");
        ItemStack present = createIcon(new ItemStack(Material.ARMOR_STAND, 9), "present");
        ItemStack fishing = createIcon(new ItemStack(Material.ARMOR_STAND, 10), "fishing");

        //Utilities
        ItemStack backToMenu = createIcon(new ItemStack(Material.RED_WOOL, 1), "backtomenu");
        ItemStack howToPreset = createIcon(new ItemStack(Material.BOOK, 1), "howtopreset");

        //Build for the Menu ---- DO NOT MODIFY THIS UNLESS YOU KNOW WHAT YOU ARE DOING!
        ItemStack[] items = {
            blank, blank, blank, blank, blank, blank, blank, blank, blank,
            blank, backToMenu, sitting, waving, greet1, greet2, cheer, archer, blank,
            blank, howToPreset, dancing, hanging, present, fishing, blank, blank, blank,
            blank, blank, blank, blank, blank, blank, blank, blank, blank
        };

        menuInv.setContents(items);
    }

    private ItemStack createIcon(ItemStack icon, String path) {
        ItemMeta meta = icon.getItemMeta();
        assert meta != null;
        meta.setDisplayName(getIconName(path));
        ArrayList<String> loreList = new ArrayList<>();
        loreList.add(getIconDescription(path));
        meta.setLore(loreList);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        icon.setItemMeta(meta);
        return icon;
    }

    private String getIconName(String path) {
        return plugin.getLang().getMessage(path, "iconname");
    }

    private String getIconDescription(String path) {
        return plugin.getLang().getMessage(path + ".description", "icondescription");
    }

    public void openMenu() {
        if (pe.getPlayer().hasPermission("asedit.basic")) {
            fillInventory();
            debug.log("Player '" + pe.getPlayer().getDisplayName() + "' has opened the armorStand Preset Menu");
            pe.getPlayer().openInventory(menuInv);
        }
    }

    public static String getName() {
        return name;
    }

    public void handlePresetPose(String itemName, Player player) {
        if (itemName == null) return;
        if (player == null) return;

        debug.log("Player '" + player.getDisplayName() + "' has chosen the Preset AS Pose '" + itemName + "'");
        //Do the Preset
        if (itemName.equals(SITTING)) {
            setPresetPose(player, 345, 0, 10, 350, 0, 350, 280, 20, 0, 280, 340, 0, 0, 0, 0, 0, 0, 0);
            player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 1);
            player.closeInventory();
        } else if (itemName.equals(WAVING)) {
            setPresetPose(player, 220, 20, 0, 350, 0, 350, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
            player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 1);
            player.closeInventory();
        } else if (itemName.equals(GREETING_1)) {
            setPresetPose(player, 260, 20, 0, 260, 340, 0, 340, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 0);
            player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 1);
            player.closeInventory();
        } else if (itemName.equals(GREETING_2)) {
            setPresetPose(player, 260, 10, 0, 260, 350, 0, 320, 0, 0, 10, 0, 0, 340, 0, 350, 0, 0, 0);
            player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 1);
            player.closeInventory();
        } else if (itemName.equals(ARCHER)) {
            setPresetPose(player, 270, 350, 0, 280, 50, 0, 340, 0, 10, 20, 0, 350, 0, 0, 0, 0, 0, 0);
            player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 1);
            player.closeInventory();
        } else if (itemName.equals(DANCING)) {
            setPresetPose(player, 14, 0, 110, 20, 0, 250, 250, 330, 0, 15, 330, 0, 350, 350, 0, 0, 0, 0);
            player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 1);
            player.closeInventory();
        } else if (itemName.equals(CHEERS)) {
            setPresetPose(player, 250, 60, 0, 20, 10, 0, 10, 0, 0, 350, 0, 0, 340, 0, 0, 0, 0, 0);
            player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 1);
            player.closeInventory();
        } else if (itemName.equals(HANGING)) {
            setPresetPose(player, 1, 33, 67, -145, -33, -4, -42, 21, 1, -100, 0, -1, -29, -38, -18, 0, -4, 0);
            player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 1);
            player.closeInventory();
        } else if (itemName.equals(PRESENTING)) {
            setPresetPose(player, 280, 330, 0, 10, 0, 350, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
            player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 1);
            player.closeInventory();
        } else if (itemName.equals(FISHING)) {
            setPresetPose(player, 300, 320, 0, 300, 40, 0, 280, 20, 0, 280, 340, 0, 0, 0, 0, 0, 0, 0);
            player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 1);
            player.closeInventory();
        } else if (itemName.equals(BACKTOMENU)) {
            player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 1);
            player.closeInventory();
            pe.openMenu();
        } else if (itemName.equals(HOWTO)) {
            player.sendMessage(pe.plugin.getLang().getMessage("howtopresetmsg"));
            player.sendMessage(pe.plugin.getLang().getMessage("helpurl"));
            player.sendMessage(pe.plugin.getLang().getMessage("helpdiscord"));
            player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 1);
            player.closeInventory();
        }
    }

    public void setPresetPose(Player player, double rightArmRoll, double rightArmYaw, double rightArmPitch,
        double leftArmRoll, double leftArmYaw, double leftArmPitch,
        double rightLegRoll, double rightLegYaw, double rightLegPitch,
        double leftLegRoll, double LeftLegYaw, double llp_yaw,
        double headRoll, double headYaw, double headPitch,
        double bodyRoll, double bodyYaw, double bodyPitch) {

        if (!armorStand.isValid()) return;
        if (!player.hasPermission("asedit.basic")) return;

        //Do the right positions based on what is given
        armorStand.setRightArmPose(new EulerAngle(
                Math.toRadians(rightArmRoll),
                Math.toRadians(rightArmYaw),
                Math.toRadians(rightArmPitch)
        ));

        armorStand.setLeftArmPose(new EulerAngle(
                Math.toRadians(leftArmRoll),
                Math.toRadians(leftArmYaw),
                Math.toRadians(leftArmPitch)
        ));

        armorStand.setRightLegPose(new EulerAngle(
                Math.toRadians(rightLegRoll),
                Math.toRadians(rightLegYaw),
                Math.toRadians(rightLegPitch)));

        armorStand.setLeftLegPose(new EulerAngle(
                Math.toRadians(leftLegRoll),
                Math.toRadians(LeftLegYaw),
                Math.toRadians(llp_yaw)));

        armorStand.setBodyPose(new EulerAngle(
                Math.toRadians(bodyRoll),
                Math.toRadians(bodyYaw),
                Math.toRadians(bodyPitch)));

        armorStand.setHeadPose(new EulerAngle(
                Math.toRadians(headRoll),
                Math.toRadians(headYaw),
                Math.toRadians(headPitch)));

        


    }

}
