package io.github.rypofalem.armorstandeditor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TabCompleter implements org.bukkit.command.TabCompleter {

    private ArmorStandEditorPlugin plugin = ArmorStandEditorPlugin.instance();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();
        if (!isCommandValid(command.getName())) return Collections.emptyList();

        List<String> argList = new ArrayList<>();
        String current = args[args.length - 1].toLowerCase(); // always the token being typed

        if (args.length == 1) {
            argList.addAll(List.of("mode", "axis", "adj", "slot", "help", "version", "?"));

            if (getPermissionUpdate(player))              argList.add("update");
            if (getPermissionReload(player))              argList.add("reload");
            if (getPermissionStats(player))               argList.add("stats");
            if (getPermissionResetWithinRange(player))    argList.add("resetwithinrange");
            if (getPermissionPlayerHead(player) || plugin.getAllowedToRetrieveOwnPlayerHead())
                argList.add("playerhead");
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "mode" -> argList.addAll(getModeOptions());
                case "axis" -> argList.addAll(getAxisOptions());
                case "slot" -> argList.addAll(getSlotOptions());
                case "adj"  -> argList.addAll(getAdjOptions());
            }
        }

        return argList.stream()
                .filter(a -> a.toLowerCase().startsWith(current))
                .toList();
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
}