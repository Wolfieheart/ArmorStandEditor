package io.github.rypofalem.armorstandeditor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AseTabCompleter implements org.bukkit.command.TabCompleter {

    private static final List<String> ROOT_OPTIONS =
            List.of("mode", "axis", "adj", "slot", "help", "version", "?");

    private static final List<String> MODE_OPTIONS = List.of(
            "None", "Invisible", "ShowArms", "Gravity", "BasePlate",
            "Size", "Copy", "Paste", "Head", "Body", "LeftArm",
            "RightArm", "LeftLeg", "RightLeg", "Placement",
            "DisableSlots", "Rotate", "Equipment", "Reset",
            "ItemFrame", "ItemFrameGlow", "Vulnerability", "ArmorStandGlow"
    );

    private static final List<String> AXIS_OPTIONS = List.of("X", "Y", "Z");
    private static final List<String> SLOT_OPTIONS = List.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
    private static final List<String> ADJ_OPTIONS = List.of("Coarse", "Fine");

    private final ArmorStandEditorPlugin plugin = ArmorStandEditorPlugin.instance();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player) || !isCommandValid(command.getName()) || args.length == 0) {
            return List.of();
        }

        String current = args[args.length - 1].toLowerCase(Locale.ROOT); // always the token being typed

        List<String> options = switch (args.length) {
            case 1 -> getRootOptions(player);
            case 2 -> getSubOptions(player, args[0]);
            case 3 -> getThirdOptions(args[0], args[1]);
            default -> List.of();
        };

        return options.stream()
                .filter(a -> a.toLowerCase(Locale.ROOT).startsWith(current))
                .toList();
    }

    private List<String> getRootOptions(Player player) {
        List<String> options = new ArrayList<>(ROOT_OPTIONS);

        if (hasPermission(player, "give"))               options.add("give");
        if (hasPermission(player, "update"))             options.add("update");
        if (hasPermission(player, "reload"))             options.add("reload");
        if (hasPermission(player, "stats"))              options.add("stats");
        if (hasPermission(player, "reset.withinRange"))  options.add("resetwithinrange");
        if (hasPermission(player, "resetHeads"))         options.add("resetheads");

        if (hasPermission(player, "head") || plugin.getAllowedToRetrieveOwnPlayerHead()) {
            options.add("playerhead");
        }
        return options;
    }

    private List<String> getSubOptions(Player player, String firstArg) {
        return switch (firstArg.toLowerCase(Locale.ROOT)) {
            case "mode"       -> MODE_OPTIONS;
            case "axis"       -> AXIS_OPTIONS;
            case "slot"       -> SLOT_OPTIONS;
            case "adj"        -> ADJ_OPTIONS;
            case "resetheads" -> getResetHeadsOptions(player);
            default           -> List.of();
        };
    }

    private List<String> getResetHeadsOptions(Player player) {
        List<String> options = new ArrayList<>();
        if (hasPermission(player, "resetHeads.all")) options.add("all");
        options.addAll(plugin.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .toList());
        return options;
    }

    private List<String> getThirdOptions(String firstArg, String secondArg) {
        if (firstArg.equalsIgnoreCase("resetheads") && secondArg.equalsIgnoreCase("all")) {
            return List.of("confirm");
        }
        return List.of();
    }

    private boolean isCommandValid(String commandName) {
        return commandName.equalsIgnoreCase("ase")
                || commandName.equalsIgnoreCase("armorstandeditor")
                || commandName.equalsIgnoreCase("asedit");
    }

    private boolean hasPermission(Player player, String permName) {
        return player.hasPermission("asedit." + permName.toLowerCase(Locale.ROOT));
    }
}