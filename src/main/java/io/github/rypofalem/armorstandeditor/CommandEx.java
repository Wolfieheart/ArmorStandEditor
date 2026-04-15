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

import io.github.rypofalem.armorstandeditor.modes.AdjustmentMode;
import io.github.rypofalem.armorstandeditor.modes.Axis;
import io.github.rypofalem.armorstandeditor.modes.EditMode;
import io.github.rypofalem.armorstandeditor.utils.MinecraftVersion;
import io.github.rypofalem.armorstandeditor.utils.Util;
import io.github.rypofalem.armorstandeditor.utils.VersionUtil;

import net.kyori.adventure.text.Component;

import org.bukkit.Location;
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
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.util.EulerAngle;

import java.util.List;
import java.util.Objects;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;

public class CommandEx implements CommandExecutor {
    ArmorStandEditorPlugin plugin;
    final Component listMode = text("/ase mode <" + Util.getEnumList(EditMode.class) + ">", YELLOW);
    final Component listAxis = text("/ase axis <" + Util.getEnumList(Axis.class) + ">", YELLOW);
    final Component listAdjustment = text("/ase adj <" + Util.getEnumList(AdjustmentMode.class) + ">", YELLOW);
    final Component resetWithinRange = text("/ase resetWithinRange <range>", YELLOW);
    final Component give = text("/ase give", YELLOW);
    final Component listSlot = text("/ase slot <1-9>", YELLOW);
    final Component help = text("/ase help or /ase ?", YELLOW);
    final Component version = text("/ase version", YELLOW);
    final Component update = text("/ase update", YELLOW);
    final Component reload = text("/ase reload", YELLOW);
    final Component givePlayerHead = text("/ase playerhead", YELLOW);
    final Component getArmorStats = text("/ase stats", YELLOW);
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
                sender.sendMessage(version);
                sender.sendMessage(help);
                sender.sendMessage(reload);
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
                player.sendMessage(listMode);
                player.sendMessage(listAxis);
                player.sendMessage(listSlot);
                player.sendMessage(listAdjustment);
                player.sendMessage(version);
                player.sendMessage(update);
                player.sendMessage(help);
                player.sendMessage(reload);
                player.sendMessage(givePlayerHead);
                player.sendMessage(give);
                player.sendMessage(getArmorStats);
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
                case "give" -> commandGive(player);
                case "reload" -> commandReload(player);
                case "stats" -> commandStats(player);
                case "resetwithinrange" -> commandResetWithinRange(player, args);
                default -> {
                    player.sendMessage(listMode);
                    player.sendMessage(listAxis);
                    player.sendMessage(listSlot);
                    player.sendMessage(listAdjustment);
                    player.sendMessage(version);
                    player.sendMessage(update);
                    player.sendMessage(help);
                    player.sendMessage(reload);
                    player.sendMessage(givePlayerHead);
                    player.sendMessage(give);
                    player.sendMessage(getArmorStats);
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
            ItemStack stack = new ItemStack(plugin.getEditTool());
            ItemMeta meta = stack.getItemMeta();

            CustomModelDataComponent dC = meta.getCustomModelDataComponent();
            dC.setFloats(List.of((float) plugin.getCustomModelDataValue()));
            meta.setCustomModelDataComponent(dC);

            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            stack.setItemMeta(meta);
            player.getInventory().addItem(stack);
            player.sendMessage(plugin.getLang().getMessage("give", "info"));
        } else {
            player.sendMessage(plugin.getLang().getMessage("nogive", "warn"));
        }
    }


    private void commandResetWithinRange(Player player, String[] args) {
        if (player.hasPermission("asedit.reset.withinRange")) {
            debug.log(" Player '" + player.getName() + "' is resetting armor stands within range.");
            double range = Double.parseDouble(args[1]);
            debug.log(" Range Chosen: " + range);

            if (range > plugin.getMaxResetRange()) {
                player.sendMessage(plugin.getLang().getMessage("resetwithinrangeexceed", "warn"));
                return;
            }

            Location playerLoc = player.getLocation();
            plugin.editorManager.getPlayerEditor(player.getUniqueId()).resetArmorStandsWithinRange(playerLoc, range);
            player.sendMessage(plugin.getLang().getMessage("resetwithinrange", "info"));
        } else {
            player.sendMessage(plugin.getLang().getMessage("nopermoption", "warn", "resetwithinrange"));
        }
    }

    private void commandGivePlayerHead(Player player) {
        if (player.hasPermission("asedit.head") || plugin.getAllowedToRetrieveOwnPlayerHead()) {
            debug.log("Creating a player head for the OfflinePlayer '" + player.getName() + "'");
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
            player.sendMessage(listSlot);
        }

        if (args.length > 1) {
            try {
                byte slot = (byte) (Byte.parseByte(args[1]) - 0b1);
                if (slot >= 0 && slot < 9) {
                    debug.log("Player has chosen slot: " + slot);
                    plugin.editorManager.getPlayerEditor(player.getUniqueId()).setCopySlot(slot);
                } else {
                    player.sendMessage(listSlot);
                }

            } catch (NumberFormatException _) {
                player.sendMessage(listSlot);
            }
        }
    }

    private void commandAdj(Player player, String[] args) {
        if (args.length <= 1) {
            player.sendMessage(plugin.getLang().getMessage("noadjcom", "warn"));
            player.sendMessage(listAdjustment);
        }

        if (args.length > 1) {
            for (AdjustmentMode adj : AdjustmentMode.values()) {
                if (adj.toString().toLowerCase().contentEquals(args[1].toLowerCase())) {
                    plugin.editorManager.getPlayerEditor(player.getUniqueId()).setAdjMode(adj);
                    return;
                }
            }
            player.sendMessage(listAdjustment);
        }
    }

    private void commandAxis(Player player, String[] args) {
        if (args.length <= 1) {
            player.sendMessage(plugin.getLang().getMessage("noaxiscom", "warn"));
            player.sendMessage(listAxis);
        }

        if (args.length > 1) {
            for (Axis axis : Axis.values()) {
                if (axis.toString().toLowerCase().contentEquals(args[1].toLowerCase())) {
                    debug.log("Player '" + player.getName() + "' sets the axis to " + axis);
                    plugin.editorManager.getPlayerEditor(player.getUniqueId()).setAxis(axis);
                    return;
                }
            }
            player.sendMessage(listAxis);
        }
    }

    private void commandMode(Player player, String[] args) {
        if (args.length <= 1) {
            player.sendMessage(plugin.getLang().getMessage("nomodecom", "warn"));
            player.sendMessage(listMode);
            return; // early return lets us drop the second `if` entirely
        }

        EditMode matched = findMatchingMode(args[1]);
        if (matched == null) return;

        if (!isVisibilityAllowed(player, args[1])) return;

        plugin.editorManager.getPlayerEditor(player.getUniqueId()).setMode(matched);
        debug.log("Player '" + player.getName() + "' chose the mode: " + matched);
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

        debug.log("Current ArmorStandEditor Version is: " + ArmorStandEditorPlugin.ASE_VERSION);

        if (plugin.getRunTheUpdateChecker()) {
            debug.log("Plugin is on Server: Paper/Spigot or a fork thereof.");
            new UpdateChecker(plugin).checkForUpdatesAndNotify(player);
        } else {
            player.sendMessage(text("[ArmorStandEditor] Update Checker is not enabled on this server.", YELLOW));
        }
    }

    private void commandVersion(Player player) {
        debug.log("Player '" + player.getName() + "' permission check for asedit.update: " + getPermissionUpdate(player));
        if (!(getPermissionUpdate(player))) return;
        String verString = plugin.getASEVersion();
        player.sendMessage(text("[ArmorStandEditor] Version: " + verString, YELLOW));
    }

    private void commandVersionConsole(CommandSender sender) {
        String verString = plugin.getASEVersion();
        sender.sendMessage(text("[ArmorStandEditor] Version: " + verString, YELLOW));
    }

    private void commandReload(Player player) {
        debug.log("Player '" + player.getName() + "' permission check for asedit.reload: " + getPermissionReload(player));

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
        debug.log("Player '" + player.getName() + "' permission check for asedit.stats: " + getPermissionStats(player));

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

    private boolean getPermissionResetWithinRange(Player player) {
        return checkPermission(player, "reset.withinRange", false);
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



    /**
     * Returns the EditMode whose name matches the given argument (case-insensitive),
     * or null if none matches.
     */
    private EditMode findMatchingMode(String arg) {
        for (EditMode mode : EditMode.values()) {
            if (mode.toString().equalsIgnoreCase(arg)) return mode;
        }
        return null;
    }

    /**
     * Returns false if the requested mode is a restricted visibility toggle
     * that the player lacks permission to use and the feature is disabled globally.
     */
    private boolean isVisibilityAllowed(Player player, String arg) {
        if (arg.equals("invisible"))
            return checkPermission(player, "togglearmorstandvisibility", true) || plugin.getArmorStandVisibility();
        if (arg.equals("itemframe"))
            return checkPermission(player, "toggleitemframevisibility", true) || plugin.getItemFrameVisibility();
        return true;
    }

}
