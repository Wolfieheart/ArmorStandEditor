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

import io.github.rypofalem.armorstandeditor.ArmorStandEditorPlugin;
import io.github.rypofalem.armorstandeditor.Debug;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.function.Supplier;


public class GriefPreventionProtection implements Protection {

    private boolean gpEnabled;
    private GriefPrevention griefPrevention = null;
    private Debug debug;
    private ArmorStandEditorPlugin plugin;

    public GriefPreventionProtection() {
        plugin = ArmorStandEditorPlugin.instance();
        debug = plugin.debug;
        gpEnabled = Bukkit.getPluginManager().isPluginEnabled("GriefPrevention");

        if (!gpEnabled) return;
        griefPrevention = (GriefPrevention) Bukkit.getPluginManager().getPlugin("GriefPrevention");
    }

    @Override
    public boolean checkPermission(Block block, Player player) {
        return checkPermission(block.getLocation(), player);
    }

    @Override
    public boolean checkPermission(Entity entity, Player player) {
        return checkPermission(
                entity.getLocation().getBlock().getLocation(),
                player
        );
    }

    public boolean checkPermission(Location loc, Player player) {
        if (!gpEnabled) return true;
        if (player.hasPermission("asedit.ignoreProtection.griefPrevention")) return true;
        if (player.hasPermission("griefprevention.ignoreclaims")) return true;
        if (player.isOp()) return true;

        //Get the Players world -
        // If the world is null or griefprevention is not enabled for the world,
        // allow them to edit the armor stand
        World world = loc.getWorld();
        if (world == null || !griefPrevention.claimsEnabledForWorld(world)) return true;

        // Get the claim at the location of the armor stand
        Claim landClaim = griefPrevention.dataStore.getClaimAt(loc, false, null);

        debug.log("=== GP DEBUG ===");
        debug.log("Location: " + loc);
        debug.log("Claim: " + landClaim);

        // Assumption: User isn't in a claim but Wilderness, so allow them to edit the armor stand
        if(landClaim == null) return true;

        debug.log("Player UUID: " + player.getUniqueId());
        debug.log("Owner UUID: " + landClaim.getOwnerID());
        debug.log("Is owner: " +
                player.getUniqueId().equals(landClaim.getOwnerID()));

        // Check if the player has permission to build or edit in the claim
        Supplier<String> denial = landClaim.checkPermission(
                player.getUniqueId(),
                ClaimPermission.Edit,
                null
        );

        debug.log("Edit permission: " +
                (denial == null ? "ALLOWED" : "DENIED - " + denial.get()));

        if (denial != null) {
            debug.log("Edit denial message: " + denial.get());
            debug.log("=== GP DEBUG END ===");
            player.sendMessage(Component.text(
                    "You do not have permission to edit this armor stand in this claim.",
                    NamedTextColor.RED
            ));
            return false;

        } else {
            debug.log("EDIT ALLOWED");
            debug.log("=== GP DEBUG END ===");
            return true;
        }
    }
}
