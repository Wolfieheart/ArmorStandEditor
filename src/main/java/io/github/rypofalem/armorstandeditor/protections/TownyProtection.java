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
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;


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

    @Override
    public boolean checkPermission(Entity entity, Player player) {

        // Bypasses - Towny is not detected, Player is Op or has Bypass Perms
        if (!tEnabled || player.isOp() || player.hasPermission("asedit.ignoreProtection.towny")) return true;

        TownyAPI towny = TownyAPI.getInstance();
        Location playerLoc = player.getLocation();

        // --- Get ArmorStand on the Block --
        if (!(entity instanceof ArmorStand entityOnBlock)) {
            debug.log("No ArmorStand has been found therefore we will continue as intended");
            return true;
        }

        debug.log("Editing ArmorStand: " + entityOnBlock.getUniqueId());

        // --- wilderness checks ---
        if (towny.isWilderness(playerLoc)) {
            if (player.hasPermission("asedit.townyProtection.canEditInWild")) {
                debug.log("User '" + player.getDisplayName() + "' is in the Wilderness and has the permission asedit.townyProtection.canEditInWild set to TRUE. Edits are allowed!");
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
}

