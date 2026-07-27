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
import io.github.rypofalem.armorstandeditor.modes.EditMode;
import io.github.rypofalem.armorstandeditor.TestUtils.InteractionTestUtils;
import io.github.rypofalem.armorstandeditor.utils.Util;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.util.EulerAngle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.ArmorStandMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the core edit-tool interactions in {@code PlayerEditorManager}:
 * right-click ("select") and left-click ("reverse") on an ArmorStand, plus the
 * guard rails around them - no edit tool, no permission, and the blocked-names
 * protection. Uses {@link InteractionTestUtils} so every test fires the real
 * Bukkit events instead of calling listener methods directly.
 */
class ArmorStandClickInteractionTest extends BasePluginTest {

    private PlayerMock player;
    private ArmorStandMock armorStand;

    private void spawnPlayerAndArmorStand() {
        player = server.addPlayer();
        armorStand = InteractionTestUtils.spawnArmorStand(player.getWorld(), player.getLocation());
    }

    @Test
    @DisplayName("Right-click with edit tool in HEAD mode adds to the head pose and cancels the interaction")
    void rightClickWithEditTool_headMode_addsEulerAngleAndCancelsEvent() {
        spawnPlayerAndArmorStand();
        InteractionTestUtils.giveEditTool(plugin, player);
        PlayerEditor editor = plugin.editorManager.getPlayerEditor(player.getUniqueId());
        editor.setMode(EditMode.HEAD);

        double expectedX = Util.addAngle(EulerAngle.ZERO.getX(), editor.eulerAngleChange);

        PlayerInteractAtEntityEvent event = InteractionTestUtils.rightClick(player, armorStand);

        assertTrue(event.isCancelled(), "Right-click with the edit tool should cancel the vanilla interaction");
        assertEquals(expectedX, armorStand.getHeadPose().getX(), 1e-9,
                "Right-click should apply reverseEditArmorStand (add) on the active axis");
    }

    @Test
    @DisplayName("Left-click with edit tool in HEAD mode subtracts from the head pose and cancels the damage")
    void leftClickWithEditTool_headMode_subtractsEulerAngleAndCancelsDamage() {
        spawnPlayerAndArmorStand();
        InteractionTestUtils.giveEditTool(plugin, player);
        PlayerEditor editor = plugin.editorManager.getPlayerEditor(player.getUniqueId());
        editor.setMode(EditMode.HEAD);

        double expectedX = Util.subAngle(EulerAngle.ZERO.getX(), editor.eulerAngleChange);

        EntityDamageEvent event = InteractionTestUtils.leftClick(player, armorStand);

        assertTrue(event.isCancelled(), "Left-click with the edit tool should cancel the attack/damage");
        assertEquals(expectedX, armorStand.getHeadPose().getX(), 1e-9,
                "Left-click should apply editArmorStand (subtract) on the active axis");
    }

    @Test
    @DisplayName("Right-click with edit tool marks the open-menu cooldown so the menu doesn't also pop open")
    void rightClickWithEditTool_cancelsPendingMenuOpen() {
        spawnPlayerAndArmorStand();
        InteractionTestUtils.giveEditTool(plugin, player);
        PlayerEditor editor = plugin.editorManager.getPlayerEditor(player.getUniqueId());
        editor.setMode(EditMode.HEAD);

        InteractionTestUtils.rightClick(player, armorStand);

        assertTrue(editor.isMenuCancelled(),
                "Editing via right-click should suppress any pending 'open menu' task for the same interaction");
    }

    @Test
    @DisplayName("Right-click without the edit tool does not touch the ArmorStand or cancel the event")
    void rightClickWithoutEditTool_isNoOp() {
        spawnPlayerAndArmorStand();
        InteractionTestUtils.giveItem(player, Material.DIRT);
        plugin.editorManager.getPlayerEditor(player.getUniqueId()).setMode(EditMode.HEAD);

        PlayerInteractAtEntityEvent event = InteractionTestUtils.rightClick(player, armorStand);

        assertFalse(event.isCancelled(), "A non edit-tool item should leave the vanilla interaction untouched");
        assertEquals(EulerAngle.ZERO, armorStand.getHeadPose(), "Head pose should be unaffected");
    }

    @Test
    @DisplayName("Left-click on a non-ArmorStand, non-ItemFrame entity with edit tool cancels the attack")
    void leftClickOtherEntityWithEditTool_cancelsAttackForMenu() {
        player = server.addPlayer();
        var zombie = (org.mockbukkit.mockbukkit.entity.LivingEntityMock)
                player.getWorld().spawnEntity(player.getLocation(), EntityType.ZOMBIE);
        InteractionTestUtils.giveEditTool(plugin, player);

        double healthBefore = zombie.getHealth();
        EntityDamageEvent event = InteractionTestUtils.leftClick(player, zombie);

        assertTrue(event.isCancelled(),
                "Left-clicking any entity with the edit tool should cancel damage and fall through to opening the menu");
        assertEquals(healthBefore, zombie.getHealth(), 1e-9, "The entity should take no damage");
    }

    @Test
    @DisplayName("Blocked-names protection is off by default, so a name matching the sample entry still edits normally")
    void rightClickWithEditTool_blockedNamesFeatureOffByDefault_stillEdits() {
        spawnPlayerAndArmorStand();
        InteractionTestUtils.giveEditTool(plugin, player);
        PlayerEditor editor = plugin.editorManager.getPlayerEditor(player.getUniqueId());
        editor.setMode(EditMode.HEAD);
        // Matches the sample entry that ships (commented-in) under blocked-names in config.yml,
        // but enableBlockedNames defaults to false, so it should have no effect.
        armorStand.customName(net.kyori.adventure.text.Component.text("BlockedArmorStandName"));

        double expectedX = Util.addAngle(EulerAngle.ZERO.getX(), editor.eulerAngleChange);
        PlayerInteractAtEntityEvent event = InteractionTestUtils.rightClick(player, armorStand);

        assertTrue(event.isCancelled(), "With the feature off by default, editing should proceed as normal");
        assertEquals(expectedX, armorStand.getHeadPose().getX(), 1e-9,
                "Head pose should update normally since enableBlockedNames defaults to false");
    }

    @Test
    @DisplayName("Right-click is a no-op when enableBlockedNames is turned on and the ArmorStand's name matches")
    void rightClickWithEditTool_blockedNamesEnabledViaConfig_isNoOp() {
        spawnPlayerAndArmorStand();
        InteractionTestUtils.giveEditTool(plugin, player);
        plugin.editorManager.getPlayerEditor(player.getUniqueId()).setMode(EditMode.HEAD);

        // Go through the real config path (matching how a server admin would enable this),
        // not the in-memory fields directly, so this proves the config wiring itself works.
        plugin.getConfig().set("enableBlockedNames", true);
        plugin.getConfig().set("blocked-names", java.util.List.of("BlockedStandName"));
        plugin.loadConfigValues();
        armorStand.customName(net.kyori.adventure.text.Component.text("BlockedStandName"));

        PlayerInteractAtEntityEvent event = InteractionTestUtils.rightClick(player, armorStand);

        assertFalse(event.isCancelled(), "Editing a blocked-name ArmorStand should be refused before any action runs");
        assertEquals(EulerAngle.ZERO, armorStand.getHeadPose(), "Head pose should be unaffected");
    }
}