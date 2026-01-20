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

import io.github.rypofalem.armorstandeditor.menu.ASEHolder;
import io.github.rypofalem.armorstandeditor.protections.*;
import io.github.rypofalem.armorstandeditor.utils.Util;

import io.papermc.lib.PaperLib;

import net.kyori.adventure.text.Component;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacy;
import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText;

//Manages PlayerEditors and Player Events related to editing armorstands
public class PlayerEditorManager implements Listener {
    private Debug debug;
    private ArmorStandEditorPlugin plugin;
    private HashMap<UUID, PlayerEditor> players;
    private ASEHolder menuHolder = new ASEHolder(); //Inventory holder that owns the main ase menu inventories for the plugin
    private ASEHolder equipmentHolder = new ASEHolder(); //Inventory holder that owns the equipment menu
    private ASEHolder presetHolder = new ASEHolder(); //Inventory Holder that owns the PresetArmorStand Post Menu
    private ASEHolder sizeMenuHolder = new ASEHolder(); //Inventory Holder that owns the PresetArmorStand Post Menu
    double coarseAdj;
    double fineAdj;
    double coarseMov;
    double fineMov;
    private boolean ignoreNextInteract = false;
    private TickCounter counter;
    private Integer noSize = 0;
    Team team;

    // Instantiate protections used to determine whether a player may edit an armor stand or item frame
    //NOTE: GriefPreventionProtection is Depreciated as of v1.19.3-40
    private final List<Protection> protections = List.of(
        new GriefDefenderProtection(),
        new LandsProtection(),
        new PlotSquaredProtection(),
        new SkyblockProtection(),
        new TownyProtection(),
        new WorldGuardProtection(),
        new itemAdderProtection(),
        new BentoBoxProtection());

    PlayerEditorManager(ArmorStandEditorPlugin plugin) {
        this.plugin = plugin;
        this.debug = new Debug(plugin);
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
            debug.log("Open Menu Called for Player: " + player.getName());
            getPlayerEditor(player.getUniqueId()).openMenu();
            return;
        }
        if (event.getEntity() instanceof ArmorStand armorStand) {
            debug.log("Player '" + player.getName() + "' has left clicked the ArmorStand");
            getPlayerEditor(player.getUniqueId()).cancelOpenMenu();
            event.setCancelled(true);
            if (canEdit(player, armorStand)) applyLeftTool(player, armorStand);
        } else if (event.getEntity() instanceof ItemFrame itemf) {
            debug.log(" Player '" + player.getName() + "' has right clicked on an ItemFrame");
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

        if (event.getRightClicked() instanceof ArmorStand as) {
            debug.log("Player '" + player.getName() + "' has right clicked on an ArmorStand");
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
                Component getName;
                if (nameTag.getItemMeta() != null && nameTag.getItemMeta().hasDisplayName()) {
                    String name = plainText().serialize(nameTag.getItemMeta().displayName());
                    if (player.hasPermission("asedit.rename.color")) {
                        getName = legacy('&').deserialize(name);
                    } else {
                        getName = Component.text(name);
                    }
                } else {
                    getName = null;
                }


                if (getName == null) {
                    as.setCustomName(null);
                    as.setCustomNameVisible(false);
                    event.setCancelled(true);
                } else {
                    event.setCancelled(true);
                    if ((player.getGameMode() != GameMode.CREATIVE)) {
                        nameTag.subtract(1);
                    }
                    // minecraft will set the name after this event even if the event is cancelled.
                    // change it 1 tick later to apply formatting without it being overwritten
                    final Component finalgetName = getName;
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        as.customName(finalgetName);
                        as.setCustomNameVisible(true);
                    });
                }
            }
        } else if (event.getRightClicked() instanceof ItemFrame itemFrame) {
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
            event.getEntity().setCustomName(null);
            event.getEntity().setCustomNameVisible(false);
            event.setCancelled(false);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onSwitchHands(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        debug.log("PlayerSwapHandItemsEvent trigger for Player: " + player.getName());

        // Ignore if the off-hand item is not the edit tool
        if (!plugin.isEditTool(event.getOffHandItem())) return;
        event.setCancelled(true);

        // Get targets
        ArrayList<ArmorStand> asTargets = getTargets(player);       // Closest ArmorStands
        ArrayList<ItemFrame> frameTargets = getFrameTargets(player); // Closest ItemFrames

        PlayerEditor editor = getPlayerEditor(player.getUniqueId());

        // Handle double target
        if (!isEmpty(asTargets) && !isEmpty(frameTargets)) {
            editor.sendMessage("doubletarget", "warn");
            return;
        }

        // Handle single target: ArmorStand
        if (!isEmpty(asTargets)) {
            editor.setTarget(asTargets);
            return;
        }

        // Handle single target: ItemFrame
        if (!isEmpty(frameTargets)) {
            editor.setFrameTarget(frameTargets);
            return;
        }

        // No target found
        editor.sendMessage("nodoubletarget", "warn");
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
        // Check if all protections allow this edit, if one fails, don't allow edit
        return protections.stream().allMatch(protection -> protection.checkPermission(entity, player));
    }

    void applyLeftTool(Player player, ArmorStand as) {
        debug.log("Applying Left Tool on ArmorStand for Player: " + player.getName());
        getPlayerEditor(player.getUniqueId()).cancelOpenMenu();
        getPlayerEditor(player.getUniqueId()).editArmorStand(as);
    }

    void applyLeftTool(Player player, ItemFrame itemf) {
        debug.log("Applying Left Tool on ItemFrame for Player: " + player.getName());
        getPlayerEditor(player.getUniqueId()).cancelOpenMenu();
        getPlayerEditor(player.getUniqueId()).editItemFrame(itemf);
    }

    void applyRightTool(Player player, ItemFrame itemf) {
        debug.log("Applying Right Tool on ItemFrame for Player: " + player.getName());
        getPlayerEditor(player.getUniqueId()).cancelOpenMenu();
        getPlayerEditor(player.getUniqueId()).editItemFrame(itemf);
    }

    void applyRightTool(Player player, ArmorStand as) {
        debug.log("Applying Right Tool on ArmorStand for Player: " + player.getName());
        getPlayerEditor(player.getUniqueId()).cancelOpenMenu();
        getPlayerEditor(player.getUniqueId()).reverseEditArmorStand(as);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onRightClickTool(PlayerInteractEvent e) {
        if (!(e.getAction() == Action.LEFT_CLICK_AIR
            || e.getAction() == Action.RIGHT_CLICK_AIR
            || e.getAction() == Action.LEFT_CLICK_BLOCK
            || e.getAction() == Action.RIGHT_CLICK_BLOCK)) return;

        debug.log("Ran on Right Click Tool Event.");
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
        debug.log("Open Menu Called for Player: " + player.getName());
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
        final InventoryHolder holder = PaperLib.getHolder(e.getInventory(), false).getHolder();

        if (holder == null) return;
        if (!(holder instanceof ASEHolder)) return;

        if (holder == menuHolder) {
            e.setCancelled(true);
            ItemStack item = e.getCurrentItem();
            if (item != null && item.hasItemMeta()) {
                Player player = (Player) e.getWhoClicked();
                String command = item.getItemMeta().getPersistentDataContainer().get(plugin.getIconKey(), PersistentDataType.STRING);
                if (command == null || command.equals("ase ")) { // Therefore user has clicked a black pane
                    getPlayerEditor(player.getUniqueId()).sendMessage("blackGlassClick", "");
                    return;
                } else {
                    player.performCommand(command);
                    Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
                    return;
                }
            }
        }
        if (holder == equipmentHolder) {
            ItemStack item = e.getCurrentItem();
            if (item == null) return;
            if (item.getItemMeta() == null) return;
            if (item.getItemMeta().getPersistentDataContainer().has(plugin.getIconKey(), PersistentDataType.STRING)) {
                e.setCancelled(true);
            }
        }

        if (holder == presetHolder) {
            e.setCancelled(true);
            ItemStack item = e.getCurrentItem();
            if (item != null && item.hasItemMeta()) {
                Player player = (Player) e.getWhoClicked();
                String itemName = item.getPersistentDataContainer().get(plugin.getIconKey(), PersistentDataType.STRING);
                PlayerEditor pe = players.get(player.getUniqueId());
                pe.presetPoseMenu.handlePresetPose(itemName, player);
                Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
            }
        }

        if (holder == sizeMenuHolder) {
            e.setCancelled(true);
            ItemStack item = e.getCurrentItem();
            if (item != null && item.hasItemMeta()) {
                Player player = (Player) e.getWhoClicked();
                String itemName = item.getPersistentDataContainer().get(plugin.getIconKey(), PersistentDataType.STRING);
                PlayerEditor pe = players.get(player.getUniqueId());
                pe.sizeModificationMenu.handleAttributeScaling(itemName, player);
                Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
            }
        }
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onPlayerMenuClose(InventoryCloseEvent e) {
        final InventoryHolder holder = PaperLib.getHolder(e.getInventory(), false).getHolder();

        if (holder == null) return;
        if (!(holder instanceof ASEHolder)) return;
        if (holder == equipmentHolder) {
            PlayerEditor pe = players.get(e.getPlayer().getUniqueId());
            pe.equipMenu.equipArmorstand();

            // Remove the In Use Lock
            if (!plugin.getHasFolia()) {
                team = plugin.scoreboard.getTeam(plugin.inUseTeam);
                if (team != null) {
                    team.removeEntry(pe.armorStandInUseId.toString());
                }
            }
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

    public ASEHolder getSizeMenuHolder() {
        return sizeMenuHolder;
    }

    public ASEHolder getPresetHolder() {
        return presetHolder;
    }

    long getTime() {
        return counter.ticks;
    }

    private <T> boolean isEmpty(List<T> list) {
        return list.isEmpty();
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
