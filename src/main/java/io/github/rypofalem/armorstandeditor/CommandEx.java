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

package io.github.rypofalem.armorstandeditor;

import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker;

import io.github.rypofalem.armorstandeditor.modes.AdjustmentMode;
import io.github.rypofalem.armorstandeditor.modes.Axis;
import io.github.rypofalem.armorstandeditor.modes.EditMode;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CommandEx implements CommandExecutor, TabCompleter {
    ArmorStandEditorPlugin plugin;
    final String LISTMODE = ChatColor.YELLOW + "/ase mode <" + Util.getEnumList(EditMode.class) + ">";
    final String LISTAXIS = ChatColor.YELLOW + "/ase axis <" + Util.getEnumList(Axis.class) + ">";
    final String LISTADJUSTMENT = ChatColor.YELLOW + "/ase adj <" + Util.getEnumList(AdjustmentMode.class) + ">";
    final String LISTSLOT = ChatColor.YELLOW + "/ase slot <1-9>";
    final String HELP = ChatColor.YELLOW + "/ase help or /ase ?";
    final String VERSION = ChatColor.YELLOW + "/ase version";
    final String UPDATE = ChatColor.YELLOW + "/ase update";
    final String RELOAD = ChatColor.YELLOW + "/ase reload";
    final String GIVECUSTOMMODEL = ChatColor.YELLOW + "/ase give";
    final String GIVEPLAYERHEAD = ChatColor.YELLOW + "/ase playerhead";
    final String GETARMORSTATS = ChatColor.YELLOW + "/ase stats";
    private Debug debug;

    public CommandEx(ArmorStandEditorPlugin armorStandEditorPlugin) {
        this.plugin = armorStandEditorPlugin;
        this.debug = new Debug(armorStandEditorPlugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof ConsoleCommandSender) { //Fix to Support #267
            debug.log("Sender is CONSOLE!");
            if (args.length == 0) {
                sender.sendMessage(VERSION);
                sender.sendMessage(HELP);
                sender.sendMessage(RELOAD);
                return true;
            } else {
                switch (args[0].toLowerCase()) {
                    case "reload" -> commandReloadConsole(sender);
                    case "help", "?" -> commandHelpConsole(sender);
                    case "version" -> commandVersionConsole(sender);
                    default -> {
                        sender.sendMessage(plugin.getLang().getMessage("noconsolecom", "warn"));
                    }
                }
                return true;
            }

        }

        if (sender instanceof Player player && !getPermissionBasic(player)) {
            debug.log("Sender is Player but asedit.basic is" + getPermissionBasic(player));
            sender.sendMessage(plugin.getLang().getMessage("nopermoption", "warn", "basic"));
            return true;
        } else {
            Player player = (Player) sender;

            debug.log("Sender is Player and asedit.basic is " + getPermissionBasic(player));
            if (args.length == 0) {
                player.sendMessage(LISTMODE);
                player.sendMessage(LISTAXIS);
                player.sendMessage(LISTSLOT);
                player.sendMessage(LISTADJUSTMENT);
                player.sendMessage(VERSION);
                player.sendMessage(UPDATE);
                player.sendMessage(HELP);
                player.sendMessage(RELOAD);
                player.sendMessage(GIVECUSTOMMODEL);
                player.sendMessage(GIVEPLAYERHEAD);
                player.sendMessage(GETARMORSTATS);
                return true;
            }
            switch (args[0].toLowerCase()) {
                case "mode" -> commandMode(player, args);
                case "axis" -> commandAxis(player, args);
                case "adj" -> commandAdj(player, args);
                case "slot" -> commandSlot(player, args);
                case "help", "?" -> commandHelp(player);
                case "version" -> commandVersion(player);
                case "update" -> commandUpdate(player);
                case "give" -> commandGive(player);
                case "playerhead" -> commandGivePlayerHead(player);
                case "reload" -> commandReload(player);
                case "stats" -> commandStats(player);
                default -> {
                    sender.sendMessage(LISTMODE);
                    sender.sendMessage(LISTAXIS);
                    sender.sendMessage(LISTSLOT);
                    sender.sendMessage(LISTADJUSTMENT);
                    sender.sendMessage(VERSION);
                    sender.sendMessage(UPDATE);
                    sender.sendMessage(HELP);
                    sender.sendMessage(RELOAD);
                    sender.sendMessage(GIVECUSTOMMODEL);
                    sender.sendMessage(GIVEPLAYERHEAD);
                    sender.sendMessage(GETARMORSTATS);
                }
            }
            return true;
        }
    }

    // Implemented to fix:
    // https://github.com/Wolfieheart/ArmorStandEditor-Issues/issues/35 &
    // https://github.com/Wolfieheart/ArmorStandEditor-Issues/issues/30 - See Remarks OTHER
    private void commandGive(Player player) {
        if (player.hasPermission("asedit.give")) {
            ItemStack stack = new ItemStack(plugin.getEditTool()); //Only Support EditTool at the MOMENT
            ItemMeta meta = stack.getItemMeta();
            Objects.requireNonNull(meta).setCustomModelData(plugin.getCustomModelDataInt());
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            stack.setItemMeta(meta);
            player.getInventory().addItem(stack);
            player.sendMessage(plugin.getLang().getMessage("give", "info"));
        } else {
            player.sendMessage(plugin.getLang().getMessage("nogive", "warn"));
        }
    }

    private void commandGivePlayerHead(Player player) {
        if (player.hasPermission("asedit.head")) {
            debug.log("Creating a player head for the OfflinePlayer '" + player.getDisplayName() + "'");
            OfflinePlayer offlinePlayer = player.getPlayer();
            ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1, (short) 3);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            meta.setOwningPlayer(offlinePlayer);
            item.setItemMeta(meta);
            player.getInventory().addItem(item);
            player.sendMessage(plugin.getLang().getMessage("playerhead", "info"));
        } else {
            player.sendMessage(plugin.getLang().getMessage("playerheaderror", "warn"));
        }
    }

    private void commandSlot(Player player, String[] args) {

        if (args.length <= 1) {
            player.sendMessage(plugin.getLang().getMessage("noslotnumcom", "warn"));
            player.sendMessage(LISTSLOT);
        }

        if (args.length > 1) {
            try {
                byte slot = (byte) (Byte.parseByte(args[1]) - 0b1);
                if (slot >= 0 && slot < 9) {
                    debug.log("Player has chosen slot: " + slot);
                    plugin.editorManager.getPlayerEditor(player.getUniqueId()).setCopySlot(slot);
                } else {
                    player.sendMessage(LISTSLOT);
                }

            } catch (NumberFormatException nfe) {
                player.sendMessage(LISTSLOT);
            }
        }
    }

    private void commandAdj(Player player, String[] args) {
        if (args.length <= 1) {
            player.sendMessage(plugin.getLang().getMessage("noadjcom", "warn"));
            player.sendMessage(LISTADJUSTMENT);
        }

        if (args.length > 1) {
            for (AdjustmentMode adj : AdjustmentMode.values()) {
                if (adj.toString().toLowerCase().contentEquals(args[1].toLowerCase())) {
                    plugin.editorManager.getPlayerEditor(player.getUniqueId()).setAdjMode(adj);
                    return;
                }
            }
            player.sendMessage(LISTADJUSTMENT);
        }
    }

    private void commandAxis(Player player, String[] args) {
        if (args.length <= 1) {
            player.sendMessage(plugin.getLang().getMessage("noaxiscom", "warn"));
            player.sendMessage(LISTAXIS);
        }

        if (args.length > 1) {
            for (Axis axis : Axis.values()) {
                if (axis.toString().toLowerCase().contentEquals(args[1].toLowerCase())) {
                    debug.log("Player '" + player.getDisplayName() + "' sets the axis to " + axis);
                    plugin.editorManager.getPlayerEditor(player.getUniqueId()).setAxis(axis);
                    return;
                }
            }
            player.sendMessage(LISTAXIS);
        }
    }

    private void commandMode(Player player, String[] args) {
        if (args.length <= 1) {
            player.sendMessage(plugin.getLang().getMessage("nomodecom", "warn"));
            player.sendMessage(LISTMODE);
        }

        if (args.length > 1) {
            for (EditMode mode : EditMode.values()) {
                if (mode.toString().toLowerCase().contentEquals(args[1].toLowerCase())) {
                    if (args[1].equals("invisible") && !(checkPermission(player, "togglearmorstandvisibility", true) || plugin.getArmorStandVisibility())) return;
                    if (args[1].equals("itemframe") && !(checkPermission(player, "toggleitemframevisibility", true) || plugin.getItemFrameVisibility())) return;
                    plugin.editorManager.getPlayerEditor(player.getUniqueId()).setMode(mode);
                    debug.log("Player '" + player.getDisplayName() + "' chose the mode: " + mode);
                    return;
                }
            }
        }
    }

    private void commandHelp(Player player) {
        player.closeInventory();
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        player.sendMessage(plugin.getLang().getMessage("help", "info", plugin.editTool.name()));
        player.sendMessage("");
        player.sendMessage(plugin.getLang().getMessage("helptips", "info"));
        player.sendMessage("");
        player.sendRawMessage(plugin.getLang().getMessage("helpurl", ""));
        player.sendRawMessage(plugin.getLang().getMessage("helpdiscord", ""));
    }

    private void commandHelpConsole(CommandSender sender) {
        sender.sendMessage(plugin.getLang().getMessage("help", "info", plugin.editTool.name()));
        sender.sendMessage("");
        sender.sendMessage(plugin.getLang().getMessage("helptips", "info"));
        sender.sendMessage("");
        sender.sendMessage(plugin.getLang().getMessage("helpurl", "info"));
        sender.sendMessage(plugin.getLang().getMessage("helpdiscord", "info"));
    }

    private void commandUpdate(Player player) {
        if (!(checkPermission(player, "update", true))) return;

        //Only Run if the Update Command Works
        debug.log("Current ArmorStandEditor Version is: " + plugin.ASE_VERSION);
        if (plugin.ASE_VERSION.contains(".x")) {
            debug.log("Plugin version is DEVELOPMENT");
            player.sendMessage(ChatColor.YELLOW + "[ArmorStandEditor] Update Checker will not work on Development Versions.");
            player.sendMessage(ChatColor.YELLOW + "[ArmorStandEditor] Report all bugs to: https://github.com/Wolfieheart/ArmorStandEditor/issues");
        } else {
            if (!plugin.getHasFolia() && plugin.getRunTheUpdateChecker()) {
                debug.log("Plugin is on Server: Paper/Spigot or a fork thereof.");
                new UpdateChecker(plugin, UpdateCheckSource.SPIGOT, "" + ArmorStandEditorPlugin.SPIGOT_RESOURCE_ID).checkNow(player); //Runs Update Check
            } else if (plugin.getHasFolia()) {
                debug.log("Plugin is on Folia");
                player.sendMessage(ChatColor.YELLOW + "[ArmorStandEditor] Update Checker does not currently work on Folia.");
                player.sendMessage(ChatColor.YELLOW + "[ArmorStandEditor] Report all bugs to: https://github.com/Wolfieheart/ArmorStandEditor/issues");
            } else {
                player.sendMessage(ChatColor.YELLOW + "[ArmorStandEditor] Update Checker is not enabled on this server");
            }
        }
    }

    private void commandVersion(Player player) {
        debug.log("Player '" + player.getDisplayName() + "' permission check for asedit.update: " + getPermissionUpdate(player));
        if (!(getPermissionUpdate(player))) return;
        String verString = plugin.ASE_VERSION;
        player.sendMessage(ChatColor.YELLOW + "[ArmorStandEditor] Version: " + verString);
    }

    private void commandVersionConsole(CommandSender sender) {
        String verString = plugin.ASE_VERSION;
        sender.sendMessage(ChatColor.YELLOW + "[ArmorStandEditor] Version: " + verString);
    }

    private void commandReload(Player player) {
        debug.log("Player '" + player.getDisplayName() + "' permission check for asedit.reload: " + getPermissionReload(player));

        if (!(getPermissionReload(player))) return;
        debug.log("Performing reload of config.yml");
        plugin.performReload();
        player.sendMessage(plugin.getLang().getMessage("reloaded", ""));
    }

    private void commandReloadConsole(CommandSender sender) {
        debug.log("Console has decided to reload the plugin....");
        plugin.performReload();
        sender.sendMessage(plugin.getLang().getMessage("reloaded", "info"));
    }

    private void commandStats(Player player) {
        debug.log("Player '" + player.getDisplayName() + "' permission check for asedit.stats: " + getPermissionStats(player));

        if (getPermissionStats(player)) {
            for (Entity e : player.getNearbyEntities(1, 1, 1)) {
                if (e instanceof ArmorStand as) {

                    //Calculation TIME - Might move this out later, but is OK here for now
                    double sizeAttribute;

                    double headX = as.getHeadPose().getX();
                    headX = Math.toDegrees(headX);
                    headX = Math.rint(headX);

                    double headY = as.getHeadPose().getY();
                    headY = Math.toDegrees(headY);
                    headY = Math.rint(headY);

                    double headZ = as.getHeadPose().getZ();
                    headZ = Math.toDegrees(headZ);
                    headZ = Math.rint(headZ);

                    //Body
                    double bodyX = as.getBodyPose().getX();
                    bodyX = Math.toDegrees(bodyX);
                    bodyX = Math.rint(bodyX);

                    double bodyY = as.getBodyPose().getY();
                    bodyY = Math.toDegrees(bodyY);
                    bodyY = Math.rint(bodyY);

                    double bodyZ = as.getBodyPose().getZ();
                    bodyZ = Math.toDegrees(bodyZ);
                    bodyZ = Math.rint(bodyZ);

                    //Arms
                    double rightArmY = as.getRightArmPose().getY();
                    rightArmY = Math.toDegrees(rightArmY);
                    rightArmY = Math.rint(rightArmY);

                    double rightArmZ = as.getRightArmPose().getZ();
                    rightArmZ = Math.toDegrees(rightArmZ);
                    rightArmZ = Math.rint(rightArmZ);

                    double rightArmX = as.getRightArmPose().getX();
                    rightArmX = Math.toDegrees(rightArmX);
                    rightArmX = Math.rint(rightArmX);

                    double leftArmX = as.getLeftArmPose().getX();
                    leftArmX = Math.toDegrees(leftArmX);
                    leftArmX = Math.rint(leftArmX);

                    double leftArmY = as.getLeftArmPose().getY();
                    leftArmY = Math.toDegrees(leftArmY);
                    leftArmY = Math.rint(leftArmY);

                    double leftArmZ = as.getLeftArmPose().getZ();
                    leftArmZ = Math.toDegrees(leftArmZ);
                    leftArmZ = Math.rint(leftArmZ);

                    //Legs
                    double rightLegX = as.getRightLegPose().getX();
                    rightLegX = Math.toDegrees(rightLegX);
                    rightLegX = Math.rint(rightLegX);

                    double rightLegY = as.getRightLegPose().getY();
                    rightLegY = Math.toDegrees(rightLegY);
                    rightLegY = Math.rint(rightLegY);

                    double rightLegZ = as.getRightLegPose().getZ();
                    rightLegZ = Math.toDegrees(rightLegZ);
                    rightArmX = Math.rint(rightLegZ);

                    double leftLegX = as.getLeftLegPose().getX();
                    leftLegX = Math.toDegrees(leftLegX);
                    leftLegX = Math.rint(leftLegX);

                    double leftLegY = as.getLeftLegPose().getY();
                    leftLegY = Math.toDegrees(leftLegY);
                    leftLegY = Math.rint(leftLegY);

                    double leftLegZ = as.getLeftLegPose().getZ();
                    leftLegZ = Math.toDegrees(leftLegZ);
                    leftLegZ = Math.rint(leftLegZ);

                    if (plugin.getNmsVersion().compareTo("1.21.4") >= 0 || plugin.getNmsVersion().compareTo("v1_21_R3") >= 0) {
                        sizeAttribute = Objects.requireNonNull(as.getAttribute(Attribute.SCALE)).getBaseValue();
                    } else {
                        sizeAttribute = 0;
                    }

                    //Coordinates
                    float locationX = (float) as.getLocation().getX();
                    float locationY = (float) as.getLocation().getY();
                    float locationZ = (float) as.getLocation().getZ();

                    //Toggles
                    boolean isVisible = as.isVisible();
                    boolean armsVisible = as.hasArms();
                    boolean basePlateVisible = as.hasBasePlate();
                    boolean isVulnerable = as.isInvulnerable();
                    boolean hasGravity = as.hasGravity();
                    boolean isSmall = as.isSmall();
                    boolean isGlowing = as.isGlowing();
                    boolean isLocked = plugin.scoreboard.getTeam(plugin.lockedTeam).hasEntry(as.getUniqueId().toString());
                    boolean isInUse = plugin.scoreboard.getTeam(plugin.inUseTeam).hasEntry(as.getUniqueId().toString());

                    // Send messages
                    player.sendMessage(ChatColor.YELLOW + "----------- Armor Stand Statistics -----------");
                    player.sendMessage(ChatColor.YELLOW + plugin.getLang().getMessage("stats"));
                    player.sendMessage(ChatColor.YELLOW + "Head: " + ChatColor.AQUA + headX + " / " + headY + " / " + headZ);
                    player.sendMessage(ChatColor.YELLOW + "Body: " + ChatColor.AQUA + bodyX + " / " + bodyY + " / " + bodyZ);
                    player.sendMessage(ChatColor.YELLOW + "Right Arm: " + ChatColor.AQUA + rightArmX + " / " + rightArmY + " / " + rightArmZ);
                    player.sendMessage(ChatColor.YELLOW + "Left Arm: " + ChatColor.AQUA + leftArmX + " / " + leftArmY + " / " + leftArmZ);
                    player.sendMessage(ChatColor.YELLOW + "Right Leg: " + ChatColor.AQUA + rightLegX + " / " + rightLegY + " / " + rightLegZ);
                    player.sendMessage(ChatColor.YELLOW + "Left Leg: " + ChatColor.AQUA + leftLegX + " / " + leftLegY + " / " + leftLegZ);
                    player.sendMessage(ChatColor.YELLOW + "Coordinates: " + ChatColor.AQUA + "X: " + locationX + " / Y: " + locationY + " / Z: " + locationZ);
                    player.sendMessage(ChatColor.YELLOW + "Is Visible: " + ChatColor.AQUA + isVisible + ". "
                            + ChatColor.YELLOW + "Arms Visible: " + ChatColor.AQUA + armsVisible + ". "
                            + ChatColor.YELLOW + "Base Plate Visible: " + ChatColor.AQUA + basePlateVisible);
                    player.sendMessage(ChatColor.YELLOW + "Is Vulnerable: " + ChatColor.AQUA + isVulnerable + ". "
                            + ChatColor.YELLOW + "Affected by Gravity: " + ChatColor.AQUA + hasGravity);

                    if (plugin.getNmsVersion().compareTo("1.21.4") >= 0 || plugin.getNmsVersion().compareTo("v1_21_R3") >= 0) {
                        player.sendMessage(ChatColor.YELLOW + "Size: " + ChatColor.AQUA + sizeAttribute + "/" + plugin.getMaxScaleValue() + ". "
                                + ChatColor.YELLOW + "Is Glowing: " + ChatColor.AQUA + isGlowing + ". "
                                + ChatColor.YELLOW + "Is Locked: " + ChatColor.AQUA + isLocked + ". "
                                + ChatColor.YELLOW + "Is InUse: " + ChatColor.AQUA + isInUse);
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "Is Small: " + ChatColor.AQUA + isSmall + ". "
                                + ChatColor.YELLOW + "Is Glowing: " + ChatColor.AQUA + isGlowing + ". "
                                + ChatColor.YELLOW + "Is Locked: " + ChatColor.AQUA + isLocked+ ". "
                                + ChatColor.YELLOW + "Is InUse: " + ChatColor.AQUA + isInUse);
                    }
                    player.sendMessage(ChatColor.YELLOW + "----------------------------------------------");

                }
            }
        } else {
            player.sendMessage(plugin.getLang().getMessage("norangeforstats", "warn"));
        }
    }


    private boolean checkPermission(Player player, String permName, boolean sendMessageOnInvalidation) {
        if (permName.equalsIgnoreCase("paste")) {
            permName = "copy";
        }
        if (player.hasPermission("asedit." + permName.toLowerCase())) {
            return true;
        } else {
            if (sendMessageOnInvalidation) {
                player.sendMessage(plugin.getLang().getMessage("noperm", "warn"));
            }
            return false;
        }
    }

    private boolean getPermissionBasic(Player player) {
        return checkPermission(player, "basic", false);
    }

    private boolean getPermissionGive(Player player) {
        return checkPermission(player, "give", false);
    }

    private boolean getPermissionUpdate(Player player) {
        return checkPermission(player, "update", false);
    }

    private boolean getPermissionReload(Player player) {
        return checkPermission(player, "reload", false);
    }

    private boolean getPermissionPlayerHead(Player player) {
        return checkPermission(player, "head", false);
    }

    private boolean getPermissionStats(Player player) {
        return checkPermission(player, "stats", false);
    }

    //REFACTOR COMPLETION
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> argList = new ArrayList<>();
        Player player = (Player) sender;

        if (isCommandValid(command.getName())) {

            if (args.length == 1) {
                argList.add("mode");
                argList.add("axis");
                argList.add("adj");
                argList.add("slot");
                argList.add("help");
                argList.add("?");

                //Will Only work with permissions
                if (getPermissionGive(player)) {
                    argList.add("give");
                }
                if (getPermissionUpdate(player)) {
                    argList.add("update");
                }
                if (getPermissionReload(player)) {
                    argList.add("reload");
                }
                if (getPermissionPlayerHead(player) || plugin.getallowedToRetrieveOwnPlayerHead()) {
                    argList.add("playerhead");
                }

                if (getPermissionStats(player)) {
                    argList.add("stats");
                }
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("mode")) {
                argList.addAll(getModeOptions());
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("axis")) {
                argList.addAll(getAxisOptions());
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("slot")) {
                argList.addAll(getSlotOptions());
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("adj")) {
                argList.addAll(getAdjOptions());
            }

            return argList.stream().filter(a -> a.startsWith(args[0])).toList();
        }

        return Collections.emptyList();
    }

    private boolean isCommandValid(String commandName) {
        return commandName.equalsIgnoreCase("ase") ||
            commandName.equalsIgnoreCase("armorstandeditor") ||
            commandName.equalsIgnoreCase("asedit");
    }

    private List<String> getModeOptions() {
        return List.of(
            "None", "Invisible", "ShowArms", "Gravity", "BasePlate",
            "Size", "Copy", "Paste", "Head", "Body", "LeftArm",
            "RightArm", "LeftLeg", "RightLeg", "Placement",
            "DisableSlots", "Rotate", "Equipment", "Reset",
            "ItemFrame", "ItemFrameGlow", "Vulnerability", "ArmorStandGlow"
        );
    }

    private List<String> getAxisOptions() {
        return List.of("X", "Y", "Z");
    }

    private List<String> getSlotOptions() {
        return List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    }

    private List<String> getAdjOptions() {
        return List.of("Coarse", "Fine");
    }

}
