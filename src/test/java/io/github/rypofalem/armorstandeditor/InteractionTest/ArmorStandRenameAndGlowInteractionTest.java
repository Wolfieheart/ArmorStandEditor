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
import io.github.rypofalem.armorstandeditor.TestUtils.InteractionTestUtils;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the two non-edit-tool right-click branches of
 * {@code PlayerEditorManager#onArmorStandInteract}: renaming an ArmorStand with a
 * name tag, and turning an ItemFrame into a GlowItemFrame with a glow ink sac.
 */
class ArmorStandRenameAndGlowInteractionTest extends BasePluginTest {

    @Test
    @DisplayName("Right-click with a name tag renames the ArmorStand, consumes one tag, and cancels the event")
    void rightClickWithNameTag_renamesArmorStandAndConsumesTag() {
        PlayerMock player = server.addPlayer();
        player.setGameMode(GameMode.SURVIVAL);
        ArmorStand armorStand = InteractionTestUtils.spawnArmorStand(player.getWorld(), player.getLocation());
        ItemStack nameTag = InteractionTestUtils.giveNameTag(player, "TestName");
        int amountBefore = nameTag.getAmount();

        PlayerInteractAtEntityEvent event = InteractionTestUtils.rightClick(player, armorStand);
        server.getScheduler().performOneTick(); // the rename is applied one tick later, see PlayerEditorManager

        assertTrue(event.isCancelled(), "Renaming should cancel the vanilla right-click interaction");
        assertEquals("TestName", PlainTextComponentSerializer.plainText().serialize(armorStand.customName()),
                "ArmorStand should be renamed to the name tag's display name");
        assertTrue(armorStand.isCustomNameVisible(), "Custom name should be made visible after renaming");
        assertEquals(amountBefore - 1, player.getInventory().getItemInMainHand().getAmount(),
                "One name tag should be consumed in Survival mode");
    }

    @Test
    @DisplayName("Right-click with an empty-name name tag clears the ArmorStand's custom name")
    void rightClickWithBlankNameTag_clearsCustomName() {
        PlayerMock player = server.addPlayer();
        ArmorStand armorStand = InteractionTestUtils.spawnArmorStand(player.getWorld(), player.getLocation());
        armorStand.customName(net.kyori.adventure.text.Component.text("OldName"));
        armorStand.setCustomNameVisible(true);
        InteractionTestUtils.giveItem(player, Material.NAME_TAG); // no ItemMeta / display name set

        PlayerInteractAtEntityEvent event = InteractionTestUtils.rightClick(player, armorStand);

        assertTrue(event.isCancelled(), "Clearing the name should still cancel the vanilla interaction");
        assertNull(armorStand.customName(), "A name tag with no display name should clear the custom name");
        assertFalse(armorStand.isCustomNameVisible(), "Custom name visibility should be turned off when cleared");
    }

    @Test
    @DisplayName("Right-click an ItemFrame with a glow ink sac while sneaking swaps it for a GlowItemFrame")
    void rightClickItemFrameWithGlowInkSac_whileSneaking_swapsToGlowItemFrame() {
        PlayerMock player = server.addPlayer();
        player.setSneaking(true);
        player.setGameMode(GameMode.SURVIVAL);
        ItemFrame itemFrame = InteractionTestUtils.spawnItemFrame(player.getWorld(), player.getLocation());
        InteractionTestUtils.giveItem(player, Material.GLOW_INK_SAC);

        InteractionTestUtils.rightClick(player, itemFrame);

        assertTrue(itemFrame.isDead(), "The original ItemFrame should be removed and replaced");
        List<GlowItemFrame> glowFrames = (List<GlowItemFrame>) player.getWorld().getEntitiesByClass(GlowItemFrame.class);
        assertEquals(1, glowFrames.size(), "Exactly one GlowItemFrame should have been spawned in its place");
    }

    @Test
    @DisplayName("Right-click an ItemFrame with a glow ink sac while NOT sneaking leaves it unchanged")
    void rightClickItemFrameWithGlowInkSac_notSneaking_isNoOp() {
        PlayerMock player = server.addPlayer();
        player.setSneaking(false);
        ItemFrame itemFrame = InteractionTestUtils.spawnItemFrame(player.getWorld(), player.getLocation());
        InteractionTestUtils.giveItem(player, Material.GLOW_INK_SAC);

        InteractionTestUtils.rightClick(player, itemFrame);

        assertFalse(itemFrame.isDead(), "Without sneaking, the ItemFrame should be left as-is");
        assertTrue(player.getWorld().getEntitiesByClass(GlowItemFrame.class).isEmpty(),
                "No GlowItemFrame should have been spawned");
    }
}