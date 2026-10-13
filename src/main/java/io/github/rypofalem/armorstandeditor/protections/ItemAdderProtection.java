package io.github.rypofalem.armorstandeditor.protections;

import io.github.rypofalem.armorstandeditor.ArmorStandEditorPlugin;

import dev.lone.itemsadder.api.CustomFurniture;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.Comparator;

public class ItemAdderProtection implements Protection {

    private static final String PLUGIN_NAME = "ItemsAdder";
    private static final String BYPASS_PERMISSION = "asedit.ignoreProtection.itemAdder";
    private static final double SEARCH_RADIUS = 1.0;

    private final ArmorStandEditorPlugin plugin = ArmorStandEditorPlugin.instance();
    private final boolean enabled = Bukkit.getPluginManager().isPluginEnabled(PLUGIN_NAME);

    @Override
    public boolean checkPermission(Block block, Player player) {
        if (!enabled || player.isOp() || player.hasPermission(BYPASS_PERMISSION)) return true;
        if (!plugin.isEditTool(player.getInventory().getItemInMainHand())) return true;

        ArmorStand stand = findNearestArmorStand(block);

        // No stand found, or it isn't ItemsAdder furniture
        if (stand == null || !isItemsAdderFurniture(stand)) return true;

        player.sendMessage(plugin.getLang().getMessage("editUsingItemAdder"));
        return false;
    }

    private static ArmorStand findNearestArmorStand(Block block) {
        Location center = block.getLocation().add(0.5, 0.5, 0.5);
        return block.getWorld()
                .getNearbyEntities(center, SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS, ArmorStand.class::isInstance)
                .stream()
                .map(ArmorStand.class::cast)
                .min(Comparator.comparingDouble(s -> s.getLocation().distanceSquared(center)))
                .orElse(null);
    }

    private static boolean isItemsAdderFurniture(ArmorStand stand) {
        return CustomFurniture.byAlreadySpawned(stand) != null;
    }
}