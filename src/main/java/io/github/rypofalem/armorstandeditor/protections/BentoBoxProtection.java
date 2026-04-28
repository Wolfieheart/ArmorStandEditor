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
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.lists.Flags;
import world.bentobox.bentobox.managers.AddonsManager;
import world.bentobox.bentobox.managers.IslandsManager;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Optional;

public class BentoBoxProtection implements Protection {

    private final boolean bentoEnabled;
    private boolean bSkyBlockEnabled;
    private boolean aOneBlockEnabled;

    public BentoBoxProtection() {
        bentoEnabled = Bukkit.getPluginManager().isPluginEnabled("BentoBox");
    }

    @Override
    public boolean checkPermission(Block block, Player player) {
        if (!bentoEnabled || player.isOp() ||
                player.hasPermission("asedit.ignoreProtection.bentobox") ||
                player.hasPermission("bentobox.admin")) return true;

        BentoBox myBento = BentoBox.getInstance();
        if (myBento == null) return true;

        IslandsManager islandsManager = myBento.getIslandsManager();
        AddonsManager addonsManager = myBento.getAddonsManager();

        bSkyBlockEnabled = addonsManager.getAddonByName("BSkyblock").isPresent();
        aOneBlockEnabled = addonsManager.getAddonByName("AOneBlock").isPresent();

        logDebugAddonInfo(); // Extracted — no longer adds nesting here

        if (!bSkyBlockEnabled && !aOneBlockEnabled) return true;

        return checkIslandPermission(block, player, islandsManager); // Extracted
    }

    /**
     * Logs which BentoBox addon is active when debug mode is on.
     */
    private void logDebugAddonInfo() {
        if (!ArmorStandEditorPlugin.instance().isDebug()) return;

        Debug logger = ArmorStandEditorPlugin.instance().debug;

        if (bSkyBlockEnabled && !aOneBlockEnabled) {
            logger.log("BentoBox Protection for ASE is looking at: BSkyBlock.");
        } else if (aOneBlockEnabled && !bSkyBlockEnabled) {
            logger.log("BentoBox Protection for ASE is looking at: AOneBlock.");
        } else if (!bSkyBlockEnabled) {
            logger.log("BentoBox Protection is currently not using anything. This will automatically allow edits.");
        }
    }

    /**
     * Checks whether the player has permission to edit on the island at the given block location.
     */
    private boolean checkIslandPermission(Block block, Player player, IslandsManager islandsManager) {
        Optional<Island> islandOptional = islandsManager.getIslandAt(block.getLocation());
        if (islandOptional.isEmpty()) return true;
        if (islandsManager.hasIsland(block.getWorld(), player.getUniqueId())) return true;

        Island theIsland = islandOptional.get();
        if (theIsland.getRank(player.getUniqueId()) == 400) return true;

        return theIsland.isAllowed(User.getInstance(player), Flags.BREAK_BLOCKS);
    }
}
