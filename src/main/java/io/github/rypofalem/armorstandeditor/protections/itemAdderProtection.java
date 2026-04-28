package io.github.rypofalem.armorstandeditor.protections;

import io.github.rypofalem.armorstandeditor.ArmorStandEditorPlugin;

import dev.lone.itemsadder.api.CustomFurniture;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;

public class itemAdderProtection implements Protection {
    private final boolean itmaddEnabled;
    ArmorStandEditorPlugin plugin;
    Collection<Entity> entities;
    ArmorStand as;

    public itemAdderProtection() {
        itmaddEnabled = Bukkit.getPluginManager().isPluginEnabled("Itemadder");
        if (!itmaddEnabled) return;
    }


    @Override
    public boolean checkPermission(Block block, Player player) {
        if (!itmaddEnabled) return true;
        if (player.isOp()) return true;
        if (player.hasPermission("asedit.ignoreProtection.itemAdder")) return true;

        //Get the ArmorSTand Closest to the player at that point
        World world = block.getWorld();
        entities = world.getNearbyEntities(player.getLocation(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());

        for (Entity e : entities) {
            if (e instanceof ArmorStand stand) {
                as = stand;
                break;
            }
        }

        if (plugin.isEditTool(player.getInventory().getItemInMainHand())) {
            if (CustomFurniture.byAlreadySpawned(as) == null) {
                return true;
            } else {
                player.sendMessage(plugin.getLang().getMessage("editUsingItemAdder"));
                return false;
            }
        } else {
            return true;
        }
    }
}
