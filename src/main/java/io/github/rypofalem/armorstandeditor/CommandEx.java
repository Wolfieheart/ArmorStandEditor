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

import io.github.rypofalem.armorstandeditor.utils.MinecraftVersion;
import io.github.rypofalem.armorstandeditor.utils.Util;
import io.github.rypofalem.armorstandeditor.utils.VersionUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.EulerAngle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;

public class CommandEx implements CommandExecutor, TabCompleter {
    ArmorStandEditorPlugin plugin;
    final Component LISTMODE = text("/ase mode <" + Util.getEnumList(EditMode.class) + ">", YELLOW);
    final Component LISTAXIS = text("/ase axis <" + Util.getEnumList(Axis.class) + ">", YELLOW);
    final Component LISTADJUSTMENT = text("/ase adj <" + Util.getEnumList(AdjustmentMode.class) + ">", YELLOW);
    final Component LISTSLOT = text("/ase slot <1-9>", YELLOW);
    final Component HELP = text("/ase help or /ase ?",YELLOW);
    final Component VERSION = text("/ase version", YELLOW);
    final Component UPDATE = text( "/ase update", YELLOW);
    final Component RELOAD = text("/ase reload", YELLOW);
    final Component GIVEPLAYERHEAD = text("/ase playerhead", YELLOW);
    final Component GETARMORSTATS = text( "/ase stats", YELLOW);
    Debug debug;

    public CommandEx(ArmorStandEditorPlugin armorStandEditorPlugin) {
        this.plugin = armorStandEditorPlugin;
        this.debug = plugin.debug;
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
                    default -> sender.sendMessage(plugin.getLang().getMessage("noconsolecom", "warn"));
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
                    sender.sendMessage(GIVEPLAYERHEAD);
                    sender.sendMessage(GETARMORSTATS);
                }
            }
            return true;
        }
    }

    private void commandGivePlayerHead(Player player) {
        if (player.hasPermission("asedit.head") || plugin.getallowedToRetrieveOwnPlayerHead()) {
            debug.log("Creating a player head for the OfflinePlayer '" + player.displayName() + "'");
            OfflinePlayer offlinePlayer = player.getPlayer();
            ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
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
                    debug.log("Player '" + player.displayName() + "' sets the axis to " + axis);
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
                    debug.log("Player '" + player.displayName() + "' chose the mode: " + mode);
                    return;
                }
            }
        }
    }

    private void commandHelp(Player player) {
        player.closeInventory();
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        player.sendMessage(plugin.getLang().getMessage("help", "info", plugin.editTool.name()));
        player.sendMessage(Component.empty());
        player.sendMessage(plugin.getLang().getMessage("helptips", "info"));
        player.sendMessage(Component.empty());
        player.sendMessage(plugin.getLang().getMessage("helpurl", ""));
        player.sendMessage(plugin.getLang().getMessage("helpdiscord", ""));
    }

    private void commandHelpConsole(CommandSender sender) {
        sender.sendMessage(plugin.getLang().getMessage("help", "info", plugin.editTool.name()));
        sender.sendMessage(Component.empty());
        sender.sendMessage(plugin.getLang().getMessage("helptips", "info"));
        sender.sendMessage(Component.empty());
        sender.sendMessage(plugin.getLang().getMessage("helpurl", "info"));
        sender.sendMessage(plugin.getLang().getMessage("helpdiscord", "info"));
    }

    private void commandUpdate(Player player) {
        if (!(checkPermission(player, "update", true))) return;

        //Only Run if the Update Command Works
        debug.log("Current ArmorStandEditor Version is: " + plugin.ASE_VERSION);
        if (!plugin.getHasFolia() && plugin.getRunTheUpdateChecker()) {
           debug.log("Plugin is on Server: Paper/Spigot or a fork thereof.");
           new UpdateChecker(plugin, UpdateCheckSource.HANGAR, ArmorStandEditorPlugin.HANGAR_RELEASE_CHANNEL).checkNow(player); //Runs Update Check
        } else if (plugin.getHasFolia()) {
           debug.log("Plugin is on Folia");
           player.sendMessage(text("[ArmorStandEditor] Update Checker does not currently work on Folia.",YELLOW));
           player.sendMessage(text( "[ArmorStandEditor] Report all bugs to: https://github.com/Wolfieheart/ArmorStandEditor/issues", YELLOW));
        } else {
           player.sendMessage(text("[ArmorStandEditor] Update Checker is not enabled on this server",YELLOW));
        }
    }

    private void commandVersion(Player player) {
        debug.log("Player '" + player.displayName() + "' permission check for asedit.update: " + getPermissionUpdate(player));
        if (!(getPermissionUpdate(player))) return;
        String verString = plugin.getASEVersion();
        player.sendMessage(text("[ArmorStandEditor] Version: " + verString, YELLOW));
    }

    private void commandVersionConsole(CommandSender sender) {
        String verString = plugin.getASEVersion();
        sender.sendMessage(text("[ArmorStandEditor] Version: " + verString, YELLOW));
    }

    private void commandReload(Player player) {
        debug.log("Player '" + player.displayName() + "' permission check for asedit.reload: " + getPermissionReload(player));

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
        debug.log("Player '" + player.displayName() + "' permission check for asedit.stats: " + getPermissionStats(player));

        if (getPermissionStats(player)) {
            for (Entity e : player.getNearbyEntities(1, 1, 1)) {
                if (e instanceof ArmorStand as) {
                    sendArmorStandStats(player, as);
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

    /*
    * Helper Functions for Stats
     */
    private Component label(String label, Object value) {
        return text(label + ": ", YELLOW)
                .append(text(String.valueOf(value), AQUA));
    }

    private void sendArmorStandStats(Player player, ArmorStand as) {
        PoseData pose = PoseData.from(as);
        Location loc = as.getLocation();

        player.sendMessage(text("----------- Armor Stand Statistics -----------", YELLOW));
        player.sendMessage(plugin.getLang().getMessage("stats"));

        sendPose(player, "Head", pose.head());
        sendPose(player, "Body", pose.body());
        sendPose(player, "Right Arm", pose.rightArm());
        sendPose(player, "Left Arm", pose.leftArm());
        sendPose(player, "Right Leg", pose.rightLeg());
        sendPose(player, "Left Leg", pose.leftLeg());

        sendCoordinates(player, loc);
        sendVisibility(player, as);
        sendPhysics(player, as);
        sendSizeInfo(player, as);

        player.sendMessage(text("----------------------------------------------", YELLOW));
    }

    private void sendPose(Player player, String name, EulerAngle angle) {
        player.sendMessage(
                text(name + ": ", YELLOW)
                        .append(text(
                                round(angle.getX()) + " / " +
                                        round(angle.getY()) + " / " +
                                        round(angle.getZ()),
                                AQUA))
        );
    }

    private double round(double radians) {
        return Math.rint(Math.toDegrees(radians));
    }



    private void sendSizeInfo(Player player, ArmorStand as) {
        if (isScaleSupported()) {
            double scale = Objects.requireNonNull(as.getAttribute(Attribute.SCALE)).getBaseValue();
            player.sendMessage(
                    text("Size: ", YELLOW)
                            .append(text(scale + "/" + plugin.getMaxScaleValue(), AQUA))
                            .append(text(". ", YELLOW))
                            .append(label("Is Glowing", as.isGlowing()))
                            .append(text(". ", YELLOW))
                            .append(label("Is Locked", isLocked(as)))
                            .append(text(". ", YELLOW))
                            .append(label("Is InUse", isInUse(as)))
            );
            return;
        }

        player.sendMessage(
                label("Is Small", as.isSmall())
                        .append(text(". ", YELLOW))
                        .append(label("Is Glowing", as.isGlowing()))
                        .append(text(". ", YELLOW))
                        .append(label("Is Locked", isLocked(as)))
                        .append(text(". ", YELLOW))
                        .append(label("Is InUse", isInUse(as)))
        );
    }

    private boolean isScaleSupported() {
        return VersionUtil.fromString(plugin.getNmsVersion())
                .isNewerThanOrEquals(MinecraftVersion.MINECRAFT_1_20_4);
    }

    private boolean isLocked(ArmorStand as) {
        return plugin.scoreboard
                .getTeam(plugin.lockedTeam)
                .hasEntry(as.getUniqueId().toString());
    }

    private boolean isInUse(ArmorStand as) {
        return plugin.scoreboard
                .getTeam(plugin.inUseTeam)
                .hasEntry(as.getUniqueId().toString());
    }

    private void sendCoordinates(Player player, Location loc) {
        player.sendMessage(
                text("Coordinates: ", YELLOW)
                        .append(text(
                                "X: " + loc.getX() +
                                        " / Y: " + loc.getY() +
                                        " / Z: " + loc.getZ(),
                                AQUA))
        );
    }

    private void sendVisibility(Player player, ArmorStand as) {
        player.sendMessage(
                label("Is Visible", as.isVisible())
                        .append(text(". ", YELLOW))
                        .append(label("Arms Visible", as.hasArms()))
                        .append(text(". ", YELLOW))
                        .append(label("Base Plate Visible", as.hasBasePlate()))
        );
    }

    private void sendPhysics(Player player, ArmorStand as) {
        player.sendMessage(
                label("Is Vulnerable", as.isInvulnerable())
                        .append(text(". ", YELLOW))
                        .append(label("Affected by Gravity", as.hasGravity()))
        );
    }

    private record PoseData(
            EulerAngle head,
            EulerAngle body,
            EulerAngle rightArm,
            EulerAngle leftArm,
            EulerAngle rightLeg,
            EulerAngle leftLeg
    ) {
        static PoseData from(ArmorStand as) {
            return new PoseData(
                    as.getHeadPose(),
                    as.getBodyPose(),
                    as.getRightArmPose(),
                    as.getLeftArmPose(),
                    as.getRightLegPose(),
                    as.getLeftLegPose()
            );
        }
    }



}
