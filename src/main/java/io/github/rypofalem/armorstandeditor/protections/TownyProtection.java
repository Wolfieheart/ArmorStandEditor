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
package io.github.rypofalem.armorstandeditor.protections;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;

import io.github.rypofalem.armorstandeditor.ArmorStandEditorPlugin;
import io.github.rypofalem.armorstandeditor.Debug;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;


//FIX for https://github.com/Wolfieheart/ArmorStandEditor-Issues/issues/15
public class TownyProtection implements Protection {
    private final boolean tEnabled;
    private Debug debug;
    private ArmorStandEditorPlugin plugin;


    public TownyProtection() {
        plugin = ArmorStandEditorPlugin.instance();
        debug = new Debug(plugin);
        tEnabled = Bukkit.getPluginManager().isPluginEnabled("Towny");
    }

    public boolean checkPermission(Block block, Player player) {

        // Bypasses - Towny is not detected, Player is Op or has Bypass Perms
        if (!tEnabled || player.isOp() || player.hasPermission("asedit.ignoreProtection.towny")) return true;

        TownyAPI towny = TownyAPI.getInstance();
        Location playerLoc = player.getLocation();
        Location asLoc = block.getLocation();
        Material target = block.getType();

        // --- Get ArmorStand on the Block --
        ArmorStand entityOnBlock = findArmorStandOnBlock(asLoc);
        if (entityOnBlock == null) {
            debug.log("No Valid ArmorStand has been found - Check if the Player can build at th");
            return PlayerCacheUtil.getCachePermission(
                player,
                playerLoc,            // use the stand's actual location
                target,              // use the actual block material instead of null
                TownyPermission.ActionType.BUILD
            );
        }

        debug.log("Editing ArmorStand: " + entityOnBlock.getUniqueId());

        // --- wilderness checks ---
        if (towny.isWilderness(playerLoc)) {
            if (player.hasPermission("asedit.townyProtection.canEditInWild")) {
                debug.log("User '" + player.getName() + "' is in the Wilderness and has the permission asedit.townyProtection.canEditInWild set to TRUE. Edits are allowed!");
                return true;
            } else {
                player.sendMessage(plugin.getLang().getMessage("townyNoWildEdit", "warn"));
                return false;
            }
        }

        // --- towny permission check ---
        return PlayerCacheUtil.getCachePermission(
            player,
            entityOnBlock.getLocation(),            // use the stand's actual location
                Material.ARMOR_STAND,                   // treat the target as an ArmorStand
            TownyPermission.ActionType.BUILD
        );
    }

    /**
     * Utility: finds an ArmorStand sitting directly on top of the given block.
     */
    private ArmorStand findArmorStandOnBlock(Location asLoc) {
        BoundingBox bbox = BoundingBox.of(asLoc, 1, 1, 1).shift(0, 1, 0);

        for (Entity entity : asLoc.getWorld().getNearbyEntities(bbox)) {
            if (entity instanceof ArmorStand stand) {
                Location entityLoc = stand.getLocation();
                debug.log("ArmorStand Found at X: " + entityLoc.getBlockX() + ", Y: " + entityLoc.getBlockY() + ", Z: " + entityLoc.getBlockZ());
                if (entityLoc.getBlockX() == asLoc.getBlockX()
                    && entityLoc.getBlockZ() == asLoc.getBlockZ()
                    && entityLoc.getBlockY() == asLoc.getBlockY()) {
                    return stand;
                }
            }
        }
        debug.log("Entity found at X: " + asLoc.getBlockX() + ", Y: " + asLoc.getBlockY() + ", Z: " + asLoc.getBlockZ() + " is not an ArmorStand. So we will return NULL - Will do so for ItemFrames etc.");
        return null;
    }
}

