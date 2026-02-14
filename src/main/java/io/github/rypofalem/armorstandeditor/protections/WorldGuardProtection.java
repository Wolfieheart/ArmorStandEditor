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

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import static com.sk89q.worldedit.bukkit.BukkitAdapter.*;

public class WorldGuardProtection implements Protection {
    private final boolean wgEnabled;
    private RegionQuery regionQry;

    public WorldGuardProtection() {
        wgEnabled = Bukkit.getPluginManager().isPluginEnabled("WorldGuard");
        if (!wgEnabled) return;

        RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
        regionQry = regionContainer.createQuery();
    }

    public boolean checkPermission(Block block, Player player) {
        if (!wgEnabled) return true;
        if (player.isOp()) return true;
        if (player.hasPermission("asedit.ignoreProtection.worldGuard")) return true;

        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

        return regionQry.testState(adapt(block.getLocation()), localPlayer, Flags.BUILD);
    }
}
