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

package io.github.rypofalem.armorstandeditor.TestUtils;

import io.github.rypofalem.armorstandeditor.ArmorStandEditorPlugin;

import net.kyori.adventure.text.Component;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.mockbukkit.mockbukkit.entity.ArmorStandMock;
import org.mockbukkit.mockbukkit.entity.LivingEntityMock;

import static org.bukkit.Bukkit.getPluginManager;

/**
 * Reusable helpers for simulating player &lt;-&gt; ArmorStand/Entity interactions
 * against a MockBukkit-backed server.
 * <p>
 * These fire the real Bukkit events that {@code PlayerEditorManager} listens for
 * (through the server's actual {@code PluginManager}), so the full listener chain -
 * permission checks, protections, edit-tool detection, mode application - runs
 * exactly as it would in-game. Prefer these over calling {@code PlayerEditorManager}
 * methods directly so tests exercise the real event wiring, not just internal logic.
 * <p>
 * Not tied to any one test class - reuse across interaction, protection, or menu
 * test suites as they're added.
 */
public final class InteractionTestUtils {

    private InteractionTestUtils() {
    }

    /** Puts the plugin's currently configured edit tool in the player's main hand. */
    public static ItemStack giveEditTool(ArmorStandEditorPlugin plugin, Player player) {
        ItemStack tool = new ItemStack(plugin.getEditTool());
        player.getInventory().setItemInMainHand(tool);
        return tool;
    }

    /** Puts an arbitrary, non-edit-tool item in the player's main hand. */
    public static void giveItem(Player player, Material material) {
        player.getInventory().setItemInMainHand(new ItemStack(material));
    }

    /**
     * Spawns a plain ArmorStand at the given location. Returned as {@link ArmorStandMock}
     * (not just the {@code ArmorStand} interface) so it can be passed straight into
     * {@link #leftClick} without an extra cast.
     */
    public static ArmorStandMock spawnArmorStand(World world, Location location) {
        return (ArmorStandMock) world.spawnEntity(location, EntityType.ARMOR_STAND);
    }

    /**
     * Spawns a plain (non-glowing) ItemFrame at the given location, facing {@link BlockFace#SOUTH}.
     * <p>
     * MockBukkit's {@code HangingMock} leaves {@code facing} {@code null} until explicitly set
     * (real Bukkit always gives a spawned frame a concrete facing), so this sets one -
     * otherwise anything that reads {@code getFacing()} (e.g. the glow ink sac swap in
     * {@code PlayerEditorManager}) NPEs.
     */
    public static ItemFrame spawnItemFrame(World world, Location location) {
        ItemFrame itemFrame = (ItemFrame) world.spawnEntity(location, EntityType.ITEM_FRAME);
        itemFrame.setFacingDirection(BlockFace.SOUTH);
        return itemFrame;
    }

    /**
     * Spawns a {@link GlowItemFrame} at the given location, facing {@link BlockFace#SOUTH} - use
     * this for tests that start from an already-glowing frame (e.g. re-applying a glow ink sac,
     * or verifying the plugin leaves an existing GlowItemFrame alone). For the "plain frame +
     * glow ink sac -> becomes a GlowItemFrame" flow, spawn with {@link #spawnItemFrame} instead
     * and assert on the frame the plugin spawns.
     */
    public static GlowItemFrame spawnGlowItemFrame(World world, Location location) {
        GlowItemFrame glowItemFrame = (GlowItemFrame) world.spawnEntity(location, EntityType.GLOW_ITEM_FRAME);
        glowItemFrame.setFacingDirection(BlockFace.SOUTH);
        return glowItemFrame;
    }

    /**
     * Puts a name tag with the given plain-text display name in the player's main hand,
     * for exercising the rename-on-right-click branch of {@code onArmorStandInteract}.
     */
    public static ItemStack giveNameTag(Player player, String displayName) {
        ItemStack nameTag = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = nameTag.getItemMeta();
        meta.displayName(Component.text(displayName));
        nameTag.setItemMeta(meta);
        player.getInventory().setItemInMainHand(nameTag);
        return nameTag;
    }

    /**
     * Simulates a right-click interaction on {@code target}, firing
     * {@link PlayerInteractAtEntityEvent} exactly as a live client interaction
     * would. This is the event {@code PlayerEditorManager#onArmorStandInteract}
     * listens for.
     */
    public static PlayerInteractAtEntityEvent rightClick(Player player, Entity target) {
        PlayerInteractAtEntityEvent event =
                new PlayerInteractAtEntityEvent(player, target, new Vector(0, 0, 0), EquipmentSlot.HAND);
        getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Simulates a left-click (attack) interaction on {@code target}, firing
     * {@link org.bukkit.event.entity.EntityDamageByEntityEvent} via MockBukkit's
     * damage simulation. This is the event {@code PlayerEditorManager#onArmorStandDamage}
     * listens for.
     *
     * @param target must be a MockBukkit-backed living entity (e.g. one returned by
     *               {@link #spawnArmorStand}); a plain {@code LivingEntity} can't be
     *               damage-simulated.
     */
    public static EntityDamageEvent leftClick(Player player, LivingEntityMock target) {
        return target.simulateDamage(1.0, player);
    }
}