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
import org.bukkit.block.Block;
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

    public boolean checkPermission(Block block, Player player) {
        TownyAPI towny;
        if (!tEnabled) return true;
        if (player.isOp()) return true;
        if (player.hasPermission("asedit.ignoreProtection.towny")) return true; //Add Additional Permission

        towny = TownyAPI.getInstance();
        Location playerLoc = player.getLocation();
        Location asLoc = block.getLocation();

        if(towny.isWilderness(playerLoc) && player.hasPermission("asedit.townyProtection.canEditInWild")){
            debug.log(" User is in the Wilderness and Can Edit.");
            return true;
        } else if(towny.isWilderness(playerLoc) && !player.hasPermission("asedit.townyProtection.canEditInWild")) {
            player.sendMessage(plugin.getLang().getMessage("townyNoWildEdit","warn"));
            return false;
        }

        return PlayerCacheUtil.getCachePermission(player, asLoc, block.getType(), TownyPermission.ActionType.BUILD);
    }
}

