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
import java.util.logging.Level;

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

        //Get the Bento Instance
        BentoBox myBento = BentoBox.getInstance();
        if (myBento == null) return true;

        //Get the Various Managers for Bentobox
        IslandsManager islandsManager = myBento.getIslandsManager();
        AddonsManager addonsManager = myBento.getAddonsManager();

        //Check first if BSkyblock is enabled or if the Player is Owner of that Island
        bSkyBlockEnabled = addonsManager.getAddonByName("BSkyblock").isPresent();
        aOneBlockEnabled = addonsManager.getAddonByName("AOneBlock").isPresent();
        //Logging for Debug - NOTE will trigger each time an edit is done

        if (ArmorStandEditorPlugin.instance().isDebug()) {
            if (bSkyBlockEnabled && !aOneBlockEnabled) {
                Bukkit.getServer().getLogger().log(Level.INFO, "[ArmorStandEditor-Debug] BentoBox Protection for ASE is looking at: BSkyBlock.");
            }
            if (aOneBlockEnabled && !bSkyBlockEnabled) {
                Bukkit.getServer().getLogger().log(Level.INFO, "[ArmorStandEditor-Debug] BentoBox Protection for ASE is looking at: AOneBlock.");
            }
            if (!bSkyBlockEnabled && !aOneBlockEnabled) {
                Bukkit.getServer().getLogger().log(Level.INFO, "[ArmorStandEditor-Debug] BentoBox Protection is currently not using anything. This will automatically allow edits.");
            }
        }


        if (!bSkyBlockEnabled && !aOneBlockEnabled) {
            return true;
        } else {
            Optional<Island> islandOptional = islandsManager.getIslandAt(block.getLocation());

            if (islandOptional.isEmpty()) return true;

            if (islandsManager.isOwner(block.getWorld(), player.getUniqueId())) return true;

            Island theIsland = islandOptional.get();

            if (theIsland.getRank(player.getUniqueId()) == 400) {
                return true;
            } else {
                return theIsland.isAllowed(User.getInstance(player), Flags.BREAK_BLOCKS);
            }
        }
    }
}
