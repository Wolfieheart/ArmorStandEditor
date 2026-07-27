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

package io.github.rypofalem.armorstandeditor.InteractionTest;

import io.github.rypofalem.armorstandeditor.BasePluginTest;
import io.github.rypofalem.armorstandeditor.PlayerEditor;
import io.github.rypofalem.armorstandeditor.modes.Axis;

import org.bukkit.Material;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.bukkit.Bukkit.getPluginManager;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers {@code PlayerEditorManager#onScrollNCrouch}: cycling the active edit
 * axis (X/Y/Z) by scrolling the hotbar while sneaking and holding the edit tool.
 * <p>
 * The listener reads the item at the hotbar slot being scrolled <em>away from</em>
 * (the "previous" slot), so each test seeds that slot directly rather than going
 * through the main-hand item.
 */
class ScrollAxisInteractionTest extends BasePluginTest {

    private static PlayerItemHeldEvent heldEvent(PlayerMock player, int previousSlot, int newSlot) {
        return new PlayerItemHeldEvent(player, previousSlot, newSlot);
    }

    @Test
    @DisplayName("Sneaking + scrolling forward with the edit tool cycles the axis forward (X -> Y) and cancels the slot change")
    void scrollForwardWhileSneakingWithEditTool_cyclesAxisForward() {
        PlayerMock player = server.addPlayer();
        player.setSneaking(true);
        player.getInventory().setItem(0, new ItemStack(plugin.getEditTool()));

        PlayerEditor editor = plugin.editorManager.getPlayerEditor(player.getUniqueId());
        assertEquals(Axis.X, editor.axis, "Axis should start at the default, X");

        PlayerItemHeldEvent event = heldEvent(player, 0, 1);
        getPluginManager().callEvent(event);

        assertTrue(event.isCancelled(),
                "The hotbar slot change should be cancelled so the axis-cycle scroll doesn't also switch items");
        assertEquals(Axis.Y, editor.axis, "Scrolling forward from X should cycle to Y");
    }

    @Test
    @DisplayName("Sneaking + scrolling backward with the edit tool cycles the axis backward (X -> Z, wrapping)")
    void scrollBackwardWhileSneakingWithEditTool_cyclesAxisBackwardWithWraparound() {
        PlayerMock player = server.addPlayer();
        player.setSneaking(true);
        player.getInventory().setItem(1, new ItemStack(plugin.getEditTool()));

        PlayerEditor editor = plugin.editorManager.getPlayerEditor(player.getUniqueId());
        assertEquals(Axis.X, editor.axis, "Axis should start at the default, X");

        // previousSlot=1 -> newSlot=0 is a "backward" scroll per onScrollNCrouch
        PlayerItemHeldEvent event = heldEvent(player, 1, 0);
        getPluginManager().callEvent(event);

        assertTrue(event.isCancelled());
        assertEquals(Axis.Z, editor.axis, "Scrolling backward from X should wrap around to Z");
    }

    @Test
    @DisplayName("Scrolling without sneaking does not cycle the axis, even with the edit tool selected")
    void scrollWithoutSneaking_isNoOp() {
        PlayerMock player = server.addPlayer();
        player.setSneaking(false);
        player.getInventory().setItem(0, new ItemStack(plugin.getEditTool()));

        PlayerEditor editor = plugin.editorManager.getPlayerEditor(player.getUniqueId());

        PlayerItemHeldEvent event = heldEvent(player, 0, 1);
        getPluginManager().callEvent(event);

        assertEquals(Axis.X, editor.axis, "Without sneaking, the axis should be untouched");
    }

    @Test
    @DisplayName("Scrolling while sneaking without the edit tool in the previous slot does not cycle the axis")
    void scrollWhileSneakingWithoutEditTool_isNoOp() {
        PlayerMock player = server.addPlayer();
        player.setSneaking(true);
        player.getInventory().setItem(0, new ItemStack(Material.DIRT));

        PlayerEditor editor = plugin.editorManager.getPlayerEditor(player.getUniqueId());

        PlayerItemHeldEvent event = heldEvent(player, 0, 1);
        getPluginManager().callEvent(event);

        assertEquals(Axis.X, editor.axis, "Without the edit tool in the previous slot, the axis should be untouched");
    }
}