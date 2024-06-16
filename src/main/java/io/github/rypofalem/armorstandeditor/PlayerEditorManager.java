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

package io.github.rypofalem.armorstandeditor;

import com.google.common.collect.ImmutableList;

import io.github.rypofalem.armorstandeditor.api.ArmorStandRenameEvent;
import io.github.rypofalem.armorstandeditor.api.ItemFrameGlowEvent;
import io.github.rypofalem.armorstandeditor.menu.ASEHolder;
import io.github.rypofalem.armorstandeditor.protections.*;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

//Manages PlayerEditors and Player Events related to editing armorstands
public class PlayerEditorManager implements Listener {
    private ArmorStandEditorPlugin plugin;
    private HashMap<UUID, PlayerEditor> players;
    private ASEHolder menuHolder = new ASEHolder(); //Inventory holder that owns the main ase menu inventories for the plugin
    private ASEHolder equipmentHolder = new ASEHolder(); //Inventory holder that owns the equipment menu
    private ASEHolder presetHolder = new ASEHolder(); //Inventory Holder that owns the PresetArmorStand Post Menu
    double coarseAdj;
    double fineAdj;
    double coarseMov;
    double fineMov;
    private boolean ignoreNextInteract = false;
    private TickCounter counter;
    private ArrayList<ArmorStand> as = null;
    private ArrayList<ItemFrame> itemF = null;
    private Integer noSize = 0;

    // Instantiate protections used to determine whether a player may edit an armor stand or item frame
    //NOTE: GriefPreventionProtection is Depreciated as of v1.19.3-40
    private final List<Protection> protections = ImmutableList.of(
        new GriefDefenderProtection(),
        new GriefPreventionProtection(),
        new LandsProtection(),
        new PlotSquaredProtection(),
        new SkyblockProtection(),
        new TownyProtection(),
        new WorldGuardProtection(),
        new itemAdderProtection(),
        new BentoBoxProtection());

    PlayerEditorManager(ArmorStandEditorPlugin plugin) {
        this.plugin = plugin;
        players = new HashMap<>();
        coarseAdj = Util.FULL_CIRCLE / plugin.coarseRot;
        fineAdj = Util.FULL_CIRCLE / plugin.fineRot;
        coarseMov = 1;
        fineMov = .03125; // 1/32
        counter = new TickCounter();
        Scheduler.runTaskTimer(plugin, counter, 1, 1);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onArmorStandDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        if (!plugin.isEditTool(player.getInventory().getItemInMainHand())) return;
        if (!((event.getEntity() instanceof ArmorStand) || event.getEntity() instanceof ItemFrame)) {
            event.setCancelled(true);
            getPlayerEditor(player.getUniqueId()).openMenu();
            return;
        }
        if (event.getEntity() instanceof ArmorStand) {
            ArmorStand as = (ArmorStand) event.getEntity();
            getPlayerEditor(player.getUniqueId()).cancelOpenMenu();
            event.setCancelled(true);
            if (canEdit(player, as))
                applyLeftTool(player, as);
        } else if (event.getEntity() instanceof ItemFrame) {
            ItemFrame itemf = (ItemFrame) event.getEntity();
            getPlayerEditor(player.getUniqueId()).cancelOpenMenu();
            event.setCancelled(true);
            if (canEdit(player, itemf)) applyLeftTool(player, itemf);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onArmorStandInteract(PlayerInteractAtEntityEvent event) {
        if (ignoreNextInteract) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        if (!((event.getRightClicked() instanceof ArmorStand) || event.getRightClicked() instanceof ItemFrame)) return;

        if (event.getRightClicked() instanceof ArmorStand) {
            ArmorStand as = (ArmorStand) event.getRightClicked();

            if (!canEdit(player, as)) return;
            if (plugin.isEditTool(player.getInventory().getItemInMainHand())) {
                getPlayerEditor(player.getUniqueId()).cancelOpenMenu();
                event.setCancelled(true);
                applyRightTool(player, as);
                return;
            }


            //Attempt rename
            if (player.getInventory().getItemInMainHand().getType() == Material.NAME_TAG && player.hasPermission("asedit.rename")) {
                ItemStack nameTag = player.getInventory().getItemInMainHand();
                String name;
                String name2;
                if (nameTag.getItemMeta() != null && nameTag.getItemMeta().hasDisplayName()) {
                    name = nameTag.getItemMeta().getDisplayName().replace('&', ChatColor.COLOR_CHAR);
                } else {
                    name = null;
                }

                //API: ArmorStandRenameEvent
                ArmorStandRenameEvent e = new ArmorStandRenameEvent(as, player, name);
                Bukkit.getPluginManager().callEvent(e);
                if (e.isCancelled()) return;

                if (name == null) {
                    as.setCustomName(null);
                    as.setCustomNameVisible(false);
                    event.setCancelled(true);
                } else if (name.startsWith("" + ChatColor.COLOR_CHAR + "") && !player.hasPermission("asedit.rename.color")) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.getLang().getMessage("renamestopped"));
                } else if (!name.equals("")) { // nametag is not blank
                    event.setCancelled(true);

                    if ((player.getGameMode() != GameMode.CREATIVE)) {
                        if (nameTag.getAmount() > 1) {
                            nameTag.setAmount(nameTag.getAmount() - 1);
                        } else {
                            nameTag = new ItemStack(Material.AIR);
                        }
                        player.getInventory().setItemInMainHand(nameTag);
                    }

                    //minecraft will set the name after this event even if the event is cancelled.
                    //change it 1 tick later to apply formatting without it being overwritten
                    Scheduler.runTaskLater(plugin, () -> {
                        as.setCustomName(name);
                        as.setCustomNameVisible(false);
                    }, 1);
                }
            }
        } else if (event.getRightClicked() instanceof ItemFrame) {
            ItemFrame itemFrame = (ItemFrame) event.getRightClicked();

            if (!canEdit(player, itemFrame)) return;
            if (plugin.isEditTool(player.getInventory().getItemInMainHand())) {
                getPlayerEditor(player.getUniqueId()).cancelOpenMenu();
                if (!itemFrame.getItem().getType().equals(Material.AIR)) {
                    event.setCancelled(true);
                }
                applyRightTool(player, itemFrame);
                return;
            }

            if (player.getInventory().getItemInMainHand().getType().equals(Material.GLOW_INK_SAC) //attempt glowing
                && player.hasPermission("asedit.basic")
                && plugin.glowItemFrames && player.isSneaking()) {

                ItemFrameGlowEvent e = new ItemFrameGlowEvent(itemFrame, player);
                Bukkit.getPluginManager().callEvent(e);
                if (e.isCancelled()) return;

                ItemStack glowSacs = player.getInventory().getItemInMainHand();
                ItemStack contents = null;
                Rotation rotation = null;
                if (itemFrame.getItem().getType() != Material.AIR) {
                    contents = itemFrame.getItem(); //save item
                    rotation = itemFrame.getRotation(); // save item rotation
                }
                Location itemFrameLocation = itemFrame.getLocation();
                BlockFace facing = itemFrame.getFacing();

                if (player.getGameMode() != GameMode.CREATIVE) {
                    if (glowSacs.getAmount() > 1) {
                        glowSacs.setAmount(glowSacs.getAmount() - 1);
                    } else glowSacs = new ItemStack(Material.AIR);
                }

                itemFrame.remove();
                GlowItemFrame glowFrame = (GlowItemFrame) player.getWorld().spawnEntity(itemFrameLocation, EntityType.GLOW_ITEM_FRAME);
                glowFrame.setFacingDirection(facing);
                if (contents != null) {
                    glowFrame.setItem(contents);
                    glowFrame.setRotation(rotation);
                }

            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    void onArmorStandBreak(EntityDamageByEntityEvent event) { // Fixes issue #309
        if (!(event.getDamager() instanceof Player)) return; // If the damager is not a player, ignore.
        if (!(event.getEntity()  instanceof ArmorStand)) return; // If the damaged entity is not an ArmorStand, ignore.

        if (event.getEntity() instanceof ArmorStand entityAS) {
            // Check if the ArmorStand is invulnerable and if the damager is a player.
            if (entityAS.isInvulnerable() && event.getDamager() instanceof Player p) {
                // Check if the player is in Creative mode.
                if (p.getGameMode() == GameMode.CREATIVE) {
                    // If the player is in Creative mode and the ArmorStand is invulnerable,
                    // cancel the event to prevent breaking the ArmorStand.
                    p.sendMessage(plugin.getLang().getMessage("unabledestroycreative"));
                    event.setCancelled(true); // Cancel the event to prevent ArmorStand destruction.
                }
            }
        }

        if (event.getEntity() instanceof ArmorStand entityAS && entityAS.isDead()) {
            //TODO: Find a more permanent fix for "Once you destroy that armor stand, the armor stand will keep it's name and colour given by the name tag." THIS IS A TEMP SOLUTION FOR NOW.
            event.getEntity().setCustomName(null);
            event.getEntity().setCustomNameVisible(false);
            event.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSwitchHands(PlayerSwapHandItemsEvent event) {
        if (!plugin.isEditTool(event.getOffHandItem())) return; //event assumes they are already switched
        event.setCancelled(true);
        Player player = event.getPlayer();

        as = getTargets(player); //Get All ArmorStand closest to player
        itemF = getFrameTargets(player); //Get ItemFrame Closest to Player


        // Check for null and empty lists
        if (as != null && itemF != null && !as.isEmpty() && !itemF.isEmpty()) {
            getPlayerEditor(player.getUniqueId()).sendMessage("doubletarget", "warn");
        } else if (as != null && !as.isEmpty()) {
            getPlayerEditor(player.getUniqueId()).setTarget(as);
        } else if (itemF != null && !itemF.isEmpty()) {
            getPlayerEditor(player.getUniqueId()).setFrameTarget(itemF);
        } else {
            getPlayerEditor(player.getUniqueId()).sendMessage("nodoubletarget", "warn");
        }
    }

    private ArrayList<ArmorStand> getTargets(Player player) {
        Location eyeLaser = player.getEyeLocation();
        Vector direction = player.getLocation().getDirection();
        ArrayList<ArmorStand> armorStands = new ArrayList<>();

        double STEPSIZE = .5;
        Vector STEP = direction.multiply(STEPSIZE);
        double RANGE = 10;
        double LASERRADIUS = .3;
        List<Entity> nearbyEntities = player.getNearbyEntities(RANGE, RANGE, RANGE);
        if (nearbyEntities.isEmpty()) return null;

        for (double i = 0; i < RANGE; i += STEPSIZE) {
            List<Entity> nearby = (List<Entity>) player.getWorld().getNearbyEntities(eyeLaser, LASERRADIUS, LASERRADIUS, LASERRADIUS);
            if (!nearby.isEmpty()) {
                boolean endLaser = false;
                for (Entity e : nearby) {
                    if (e instanceof ArmorStand stand) {
                        armorStands.add(stand);
                        endLaser = true;
                    }
                }

                if (endLaser) break;
            }
            if (eyeLaser.getBlock().getType().isSolid()) break;
            eyeLaser.add(STEP);
        }
        return armorStands;
    }

    private ArrayList<ItemFrame> getFrameTargets(Player player) {
        Location eyeLaser = player.getEyeLocation();
        Vector direction = player.getLocation().getDirection();
        ArrayList<ItemFrame> itemFrames = new ArrayList<>();

        double STEPSIZE = .5;
        Vector STEP = direction.multiply(STEPSIZE);
        double RANGE = 10;
        double LASERRADIUS = .3;
        List<Entity> nearbyEntities = player.getNearbyEntities(RANGE, RANGE, RANGE);
        if (nearbyEntities.isEmpty()) return null;

        for (double i = 0; i < RANGE; i += STEPSIZE) {
            List<Entity> nearby = (List<Entity>) player.getWorld().getNearbyEntities(eyeLaser, LASERRADIUS, LASERRADIUS, LASERRADIUS);
            if (!nearby.isEmpty()) {
                boolean endLaser = false;
                for (Entity e : nearby) {
                    if (e instanceof ItemFrame frame) {
                        itemFrames.add(frame);
                        endLaser = true;
                    }
                }

                if (endLaser) break;
            }
            if (eyeLaser.getBlock().getType().isSolid()) break;
            eyeLaser.add(STEP);
        }

        return itemFrames;
    }


    boolean canEdit(Player player, Entity entity) {
        //Get the Entity being checked for editing
        Block block = entity.getLocation().getBlock();

        // Check if all protections allow this edit, if one fails, don't allow edit
        return protections.stream().allMatch(protection -> protection.checkPermission(block, player));
    }

    void applyLeftTool(Player player, ArmorStand as) {
        getPlayerEditor(player.getUniqueId()).cancelOpenMenu();
        getPlayerEditor(player.getUniqueId()).editArmorStand(as);
    }

    void applyLeftTool(Player player, ItemFrame itemf) {
        getPlayerEditor(player.getUniqueId()).cancelOpenMenu();
        getPlayerEditor(player.getUniqueId()).editItemFrame(itemf);
    }

    void applyRightTool(Player player, ItemFrame itemf) {
        getPlayerEditor(player.getUniqueId()).cancelOpenMenu();
        getPlayerEditor(player.getUniqueId()).editItemFrame(itemf);
    }

    void applyRightTool(Player player, ArmorStand as) {
        getPlayerEditor(player.getUniqueId()).cancelOpenMenu();
        getPlayerEditor(player.getUniqueId()).reverseEditArmorStand(as);
    }

    //Unused?
    @EventHandler(priority = EventPriority.LOWEST)
    void onRightClickTool(PlayerInteractEvent e) {
        if (!(e.getAction() == Action.LEFT_CLICK_AIR
            || e.getAction() == Action.RIGHT_CLICK_AIR
            || e.getAction() == Action.LEFT_CLICK_BLOCK
            || e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        Player player = e.getPlayer();
        if (!plugin.isEditTool(player.getInventory().getItemInMainHand())) return;
        if (plugin.requireSneaking && !player.isSneaking()) return;
        if (!player.hasPermission("asedit.basic")) return;
        if (plugin.enablePerWorld && (!plugin.allowedWorldList.contains(player.getWorld().getName()))) {
            //Implementation for Per World ASE
                getPlayerEditor(player.getUniqueId()).sendMessage("notincorrectworld", "warn");
            e.setCancelled(true);
            return;
        }
        e.setCancelled(true);
        getPlayerEditor(player.getUniqueId()).openMenu();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    void onScrollNCrouch(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        if (!player.isSneaking()) return;
        if (!plugin.isEditTool(player.getInventory().getItem(e.getPreviousSlot()))) return;

        e.setCancelled(true);
        if (e.getNewSlot() == e.getPreviousSlot() + 1 || (e.getNewSlot() == 0 && e.getPreviousSlot() == 8)) {
            getPlayerEditor(player.getUniqueId()).cycleAxis(1);
        } else if (e.getNewSlot() == e.getPreviousSlot() - 1 || (e.getNewSlot() == 8 && e.getPreviousSlot() == 0)) {
            getPlayerEditor(player.getUniqueId()).cycleAxis(-1);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onPlayerMenuSelect(InventoryClickEvent e) {
        if (e.getInventory().getHolder() == null) return;
        if (!(e.getInventory().getHolder() instanceof ASEHolder)) return;
        if (e.getInventory().getHolder() == menuHolder) {
            e.setCancelled(true);
            ItemStack item = e.getCurrentItem();
            if (item != null && item.hasItemMeta()) {
                Player player = (Player) e.getWhoClicked();
                String command = item.getItemMeta().getPersistentDataContainer().get(plugin.getIconKey(), PersistentDataType.STRING);
                if (command != null) {
                    player.performCommand(command);
                    return;
                }
            }
        }
        if (e.getInventory().getHolder() == equipmentHolder) {
            ItemStack item = e.getCurrentItem();
            if (item == null) return;
            if (item.getItemMeta() == null) return;
            if (item.getItemMeta().getPersistentDataContainer().has(plugin.getIconKey(), PersistentDataType.STRING)) {
                e.setCancelled(true);
            }
        }

        if (e.getInventory().getHolder() == presetHolder) {
            e.setCancelled(true);
            ItemStack item = e.getCurrentItem();
            if (item != null && item.hasItemMeta()) {
                Player player = (Player) e.getWhoClicked();
                String itemName = item.getItemMeta().getDisplayName();
                PlayerEditor pe = players.get(player.getUniqueId());
                pe.presetPoseMenu.handlePresetPose(itemName, player);
            }
        }
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onPlayerMenuClose(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() == null) return;
        if (!(e.getInventory().getHolder() instanceof ASEHolder)) return;
        if (e.getInventory().getHolder() == equipmentHolder) {
            PlayerEditor pe = players.get(e.getPlayer().getUniqueId());
            pe.equipMenu.equipArmorstand();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onPlayerLogOut(PlayerQuitEvent e) {
        removePlayerEditor(e.getPlayer().getUniqueId());
    }

    public PlayerEditor getPlayerEditor(UUID uuid) {
        return players.containsKey(uuid) ? players.get(uuid) : addPlayerEditor(uuid);
    }

    PlayerEditor addPlayerEditor(UUID uuid) {
        PlayerEditor pe = new PlayerEditor(uuid, plugin);
        players.put(uuid, pe);
        return pe;
    }

    private void removePlayerEditor(UUID uuid) {
        players.remove(uuid);
    }

    public ASEHolder getMenuHolder() {
        return menuHolder;
    }

    public ASEHolder getEquipmentHolder() {
        return equipmentHolder;
    }

    public ASEHolder getPresetHolder() {
        return presetHolder;
    }

    long getTime() {
        return counter.ticks;
    }


    class TickCounter implements Runnable {
        long ticks = 0; //I am optimistic

        @Override
        public void run() {
            ticks++;
        }

        public long getTime() {
            return ticks;
        }
    }
}
