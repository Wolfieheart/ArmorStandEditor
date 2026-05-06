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

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import static com.griefdefender.api.claim.TrustTypes.BUILDER;


public class GriefDefenderProtection implements Protection {

    private final boolean gdEnabled;

    public GriefDefenderProtection() {
        gdEnabled = Bukkit.getPluginManager().isPluginEnabled("GriefDefender");
    }

    @Override
    public boolean checkPermission(Block block, Player player) {
        if (!gdEnabled) return true;
        if (player.isOp()) return true;
        if (player.hasPermission("asedit.ignoreProtection.griefDefender")) return true;
        if (player.hasPermission("griefdefender.admin.bypass.border-check")) return true;

        Location blockLoc = block.getLocation();
        if (GriefDefender.getCore().getClaimAt(blockLoc) != null) {
            Claim landClaim = GriefDefender.getCore().getClaimAt(blockLoc);

            if (landClaim == null || landClaim.isWilderness() || landClaim.isAdminClaim()) {
                return true;
            } else
                return (!landClaim.isBasicClaim() || landClaim.isUserTrusted(player.getUniqueId(), BUILDER) || landClaim.allowEdit(player.getUniqueId()));
        } else {
            return true;
        }
    }
}
