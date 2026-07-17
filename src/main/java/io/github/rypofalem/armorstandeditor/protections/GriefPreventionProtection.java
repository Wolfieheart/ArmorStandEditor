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

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class GriefPreventionProtection implements Protection {

    private boolean gpEnabled;
    private GriefPrevention griefPrevention = null;

    public GriefPreventionProtection() {
        gpEnabled = Bukkit.getPluginManager().isPluginEnabled("GriefPrevention");

        if (!gpEnabled) return;
        griefPrevention = (GriefPrevention) Bukkit.getPluginManager().getPlugin("GriefPrevention");
    }

    @Override
    public boolean checkPermission(Block block, Player player) {
        if (!gpEnabled) return true;
        if (player.hasPermission("asedit.ignoreProtection.griefPrevention")) return true;

        Location blockLoc = block.getLocation();

        if (GriefPrevention.instance.claimsEnabledForWorld(blockLoc.getWorld())) {

            Claim landClaim = griefPrevention.dataStore.getClaimAt(blockLoc, false, null);
            Material blockMat = block.getType();

            if (landClaim != null && landClaim.allowEdit(player) != null && landClaim.allowBuild(player, blockMat) != null) {
                player.sendMessage(NamedTextColor.RED + landClaim.allowEdit(player));
                player.sendMessage(NamedTextColor.RED + landClaim.allowBuild(player, blockMat));
                return false;
            }
        } else {
            return true;
        }

        return true;


    }
}
