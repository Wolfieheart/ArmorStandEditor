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

import io.github.rypofalem.armorstandeditor.language.Language;
import io.github.rypofalem.armorstandeditor.menu.ASEHolder;
import io.github.rypofalem.armorstandeditor.modes.InvisibleEmptyMode;
import io.github.rypofalem.armorstandeditor.protections.*;
import io.github.rypofalem.armorstandeditor.utils.Util;

import io.papermc.lib.PaperLib;
import net.kyori.adventure.text.Component;

import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText;

//Manages PlayerEditors and Player Events related to editing armorstands
public class PlayerEditorManager implements Listener {

    private Debug debug;
    private ArmorStandEditorPlugin plugin;
    private HashMap<UUID, PlayerEditor> players;
    private Scheduler scheduler;

    private ASEHolder menuHolder = new ASEHolder(); //Inventory holder that owns the main ase menu inventories for the plugin
    private ASEHolder equipmentHolder = new ASEHolder(); //Inventory holder that owns the equipment menu
    private ASEHolder presetHolder = new ASEHolder(); //Inventory Holder that owns the PresetArmorStand Post Menu
    private ASEHolder sizeMenuHolder = new ASEHolder(); //Inventory Holder that owns the PresetArmorStand Post Menu

    private static final double STEP_SIZE = .5;
    private static final double RANGE = 10;
    private static final double LASER_RADIUS = .3;

    double coarseAdj;
    double fineAdj;
    double coarseMov;
    double fineMov;
    private boolean ignoreNextInteract = false;
    private TickCounter counter;
    Team team;
    static final Set<UUID> foliaInUse = ConcurrentHashMap.newKeySet();

    // Instantiate protections used to determine whether a player may edit an armor stand or item frame
    private final List<Protection> protections = List.of(
        new GriefDefenderProtection(),
        new GriefPreventionProtection(),
        new LandsProtection(),
        new PlotSquaredProtection(),
        new SkyblockProtection(),
        new TownyProtection(),
        new WorldGuardProtection(),
        new ItemAdderProtection(),
        new BoltProtection(),
        new BentoBoxProtection(),
        new DominionProtection());

    PlayerEditorManager(ArmorStandEditorPlugin plugin) {
        this.plugin = plugin;
        this.debug = plugin.debug;
        this.scheduler = plugin.getScheduler();

        players = new HashMap<>();
        coarseAdj = Util.FULL_CIRCLE / plugin.coarseRot;
        fineAdj = Util.FULL_CIRCLE / plugin.fineRot;
        coarseMov = 1;
        fineMov = .03125; // 1/32
        counter = new TickCounter();
        scheduler.runTaskTimer(counter, 1, 1);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    void onArmorStandSpawn(EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ArmorStand armorStand)) return;
        debug.log("Entity being spawned is an ArmorStand");

        Player player = event.getPlayer();
        if(player == null) return;
        Location location = player.getLocation();
        if(location == null) return;

        debug.log("Player " + player.getName()
            + " is placing an ArmorStand at (approx) X: " + Math.round(location.getX())
            + ", Y: " + Math.round(location.getY())
            + ", Z: " + Math.round(location.getZ())
        );

        armorStand.setGravity(plugin.getDefaultGravity());
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
        InvisibleEmptyMode mode = plugin.getInvisibleEmptyMode();
        if (mode == InvisibleEmptyMode.OFF) return;
        ArmorStand as = e.getRightClicked();
        as.getScheduler().run(plugin, t -> Util.applyEmptyStandMode(as, mode, plugin.getAutoGlowKey()), null);
    }


    @EventHandler(priority = EventPriority.LOWEST)
    void onArmorStandInteract(PlayerInteractAtEntityEvent event) {
        if (ignoreNextInteract) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();

        if (event.getRightClicked() instanceof ArmorStand as) {
            debug.log("Player '" + player.getName() + "' has right clicked on an ArmorStand");
            handleArmorStandInteract(event, player, as);
        } else if (event.getRightClicked() instanceof ItemFrame itemFrame) {
            debug.log("Player '" + player.getName() + "' has right clicked on an ItemFrame");
            handleItemFrameInteract(event, player, itemFrame);
        }
    }

    private void handleArmorStandInteract(PlayerInteractAtEntityEvent event, Player player, ArmorStand as) {
        if (!canEdit(player, as)) {
            debug.log("Player '" + player.getName() + "' is not allowed to edit this ArmorStand");
            return;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (plugin.isEditTool(hand)) {
            debug.log("Player '" + player.getName() + "' used the edit tool on an ArmorStand, applying right-click tool");
            getPlayerEditor(player.getUniqueId()).cancelOpenMenu();
            event.setCancelled(true);
            applyRightTool(player, as);
            return;
        }

        if (hand.getType() == Material.NAME_TAG && player.hasPermission("asedit.rename")) {
            debug.log("Player '" + player.getName() + "' is attempting to rename an ArmorStand with a name tag");
            renameArmorStand(event, player, as, hand);
        }
    }

    private void handleItemFrameInteract(PlayerInteractAtEntityEvent event, Player player, ItemFrame itemFrame) {
        if (!canEdit(player, itemFrame)) {
            debug.log("Player '" + player.getName() + "' is not allowed to edit this ItemFrame");
            return;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (plugin.isEditTool(hand)) {
            debug.log("Player '" + player.getName() + "' used the edit tool on an ItemFrame, applying right-click tool");
            getPlayerEditor(player.getUniqueId()).cancelOpenMenu();
            if (itemFrame.getItem().getType() != Material.AIR) {
                event.setCancelled(true);
            }
            applyRightTool(player, itemFrame);
            return;
        }

        if (canMakeGlowFrame(player, hand)) {
            debug.log("Player '" + player.getName() + "' is converting an ItemFrame to a GlowItemFrame");
            replaceWithGlowFrame(player, itemFrame, hand);
        }
    }

    private void renameArmorStand(PlayerInteractAtEntityEvent event, Player player, ArmorStand as, ItemStack nameTag) {
        Component name = resolveNameTagName(player, nameTag);
        event.setCancelled(true);

        if (name == null) {
            debug.log("Name tag has no display name, clearing ArmorStand custom name");
            as.customName(null);
            as.setCustomNameVisible(false);
            return;
        }

        debug.log("Renaming ArmorStand to '" + plainText().serialize(name) + "' for player '" + player.getName() + "'");
        if (player.getGameMode() != GameMode.CREATIVE) {
            nameTag.subtract(1);
            debug.log("Consumed one name tag from player '" + player.getName() + "'");
        }
        // minecraft will set the name after this event even if the event is cancelled.
        // change it 1 tick later to apply formatting without it being overwritten
        scheduler.runForEntity(as, () -> {
            as.customName(name);
            as.setCustomNameVisible(true);
            debug.log("Applied custom name to ArmorStand");
        });
    }


    private Component resolveNameTagName(Player player, ItemStack nameTag) {
        ItemMeta meta = nameTag.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            debug.log("Name tag has no meta or display name");
            return null;
        }

        String raw = plainText().serialize(meta.displayName());
        if (!player.hasPermission("asedit.rename.color")) {
            debug.log("Player '" + player.getName() + "' lacks asedit.rename.color, using plain text name '" + raw + "'");
            return Component.text(raw);
        }
        debug.log("Player '" + player.getName() + "' has asedit.rename.color, deserializing name '" + raw + "'");
        return Language.safeDeserialize(raw)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    private boolean canMakeGlowFrame(Player player, ItemStack hand) {
        return hand.getType() == Material.GLOW_INK_SAC
                && player.hasPermission("asedit.basic")
                && plugin.glowItemFrames
                && player.isSneaking();
    }

    private void replaceWithGlowFrame(Player player, ItemFrame itemFrame, ItemStack glowSacs) {
        boolean hasItem = itemFrame.getItem().getType() != Material.AIR;
        ItemStack contents = hasItem ? itemFrame.getItem() : null; // save item
        Rotation rotation = hasItem ? itemFrame.getRotation() : null; // save item rotation
        Location location = itemFrame.getLocation();
        BlockFace facing = itemFrame.getFacing();
        debug.log("Saved ItemFrame state: hasItem=" + hasItem + ", rotation=" + rotation + ", facing=" + facing);

        if (player.getGameMode() != GameMode.CREATIVE && glowSacs.getAmount() > 1) {
            glowSacs.setAmount(glowSacs.getAmount() - 1);
            debug.log("Consumed one glow ink sac from player '" + player.getName() + "'");
        }

        itemFrame.remove();
        GlowItemFrame glowFrame = (GlowItemFrame) player.getWorld().spawnEntity(location, EntityType.GLOW_ITEM_FRAME);
        glowFrame.setFacingDirection(facing);
        if (contents != null) {
            glowFrame.setItem(contents);
            glowFrame.setRotation(rotation);
            debug.log("Restored item and rotation on the new GlowItemFrame");
        }
        debug.log("Replaced ItemFrame with GlowItemFrame at " + location);
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    void onArmorStandBreak(EntityDamageByEntityEvent event) { // Fixes issue #309
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof ArmorStand armorStand)) return;

        // Prevent creative players from breaking invulnerable ArmorStands.
        if (armorStand.isInvulnerable() && player.getGameMode() == GameMode.CREATIVE) {
            player.sendMessage(plugin.getLang().getMessage("unabledestroycreative"));
            event.setCancelled(true);
        }

        // Dead ArmorStands must always be breakable, and their name is cleared.
        if (armorStand.isDead()) {
            armorStand.customName(Component.empty());
            armorStand.setCustomNameVisible(false);
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

        if(!asTargets.isEmpty() && !frameTargets.isEmpty()) {
            editor.sendMessage("nodoubletarget", "warn");
            return;
        }
        if(!asTargets.isEmpty()) {
            editor.setTarget(asTargets);
            return;
        }

        if (!frameTargets.isEmpty()) {
            editor.setFrameTarget(frameTargets);
            return;
        }

        // No target found
        editor.sendMessage("nodoubletarget", "warn");

    }

    private ArrayList<ArmorStand> getTargets(Player player) {
        return getTargetsOfType(player, ArmorStand.class);
    }

    private ArrayList<ItemFrame> getFrameTargets(Player player) {
        return getTargetsOfType(player, ItemFrame.class);
    }

    private <T extends Entity> ArrayList<T> getTargetsOfType(Player player, Class<T> type) {
        ArrayList<T> targets = new ArrayList<>();
        if (player.getNearbyEntities(RANGE, RANGE, RANGE).isEmpty()) return targets;

        Location eyeLaser = player.getEyeLocation();
        Vector step = player.getLocation().getDirection().multiply(STEP_SIZE);

        for (double i = 0; i < RANGE; i += STEP_SIZE) {
            targets.addAll(findEntitiesAt(player.getWorld(), eyeLaser, type));
            if (!targets.isEmpty() || eyeLaser.getBlock().getType().isSolid()) break;
            eyeLaser.add(step);
        }
        return targets;
    }

    private <T extends Entity> List<T> findEntitiesAt(World world, Location location, Class<T> type) {
        return world.getNearbyEntities(location, LASER_RADIUS, LASER_RADIUS, LASER_RADIUS)
                .stream()
                .filter(type::isInstance)
                .map(type::cast)
                .toList();
    }

    boolean canEdit(Player player, Entity entity) {
        // Check if the entity has a blocked name
        if(entity.customName() != null && plugin.enableBlockedNames) {
            String name = plainText().serialize(entity.customName());
            if (plugin.blockedNames.stream().anyMatch(name::equalsIgnoreCase)) {
                return false;
            }
        }

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

        if(e.getClickedBlock() != null && isInteractable(e.getClickedBlock().getType())) return;


        e.setCancelled(true); // This cancels the event, preventing vanilla interaction
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
                    scheduler.runForEntity(player, player::closeInventory);
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
                String itemName = item.getItemMeta().getPersistentDataContainer().get(plugin.getIconKey(), PersistentDataType.STRING);
                PlayerEditor pe = players.get(player.getUniqueId());
                pe.presetPoseMenu.handlePresetPose(itemName, player);
                scheduler.runForEntity(player, player::closeInventory);
            }
        }

        if (holder == sizeMenuHolder) {
            e.setCancelled(true);
            ItemStack item = e.getCurrentItem();
            if (item != null && item.hasItemMeta()) {
                Player player = (Player) e.getWhoClicked();
                String itemName = item.getItemMeta().getPersistentDataContainer().get(plugin.getIconKey(), PersistentDataType.STRING);
                PlayerEditor pe = players.get(player.getUniqueId());
                pe.sizeModificationMenu.handleAttributeScaling(itemName, player);
                scheduler.runForEntity(player, player::closeInventory);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        debug.log("Player Quit Event Triggered for Player: " + player.getName());
        CommandEx.clearPendingResetAll(player.getUniqueId());
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
            } else {
                foliaInUse.remove(pe.armorStandInUseId);
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

    public static boolean isInteractable(@UnknownNullability Material type) {

        // Iron doors/trapdoors can't be opened by hand
        if (type == Material.IRON_DOOR || type == Material.IRON_TRAPDOOR) return false;

        return Tag.DOORS.isTagged(type)
                || Tag.TRAPDOORS.isTagged(type)
                || Tag.BUTTONS.isTagged(type)
                || Tag.FENCE_GATES.isTagged(type)
                || Tag.BEDS.isTagged(type)
                || Tag.ALL_SIGNS.isTagged(type)
                || Tag.SHULKER_BOXES.isTagged(type)
                || Tag.WOODEN_SHELVES.isTagged(type)
                || Tag.COPPER_CHESTS.isTagged(type)
                || Tag.COPPER_GOLEM_STATUES.isTagged(type)
                || Tag.ANVIL.isTagged(type)
                || Tag.CAULDRONS.isTagged(type)
                || Tag.CAMPFIRES.isTagged(type)
                || Tag.FLOWER_POTS.isTagged(type)
                || Tag.CANDLE_CAKES.isTagged(type)
                || Tag.BEEHIVES.isTagged(type)
                || type.name().endsWith("_SHELF")
                || Tag.CANDLES.isTagged(type)
                || switch (type) {
            // Containers
            case CHEST, TRAPPED_CHEST, ENDER_CHEST, BARREL, HOPPER, DROPPER, DISPENSER,
                 CRAFTER, FURNACE, BLAST_FURNACE, SMOKER, BREWING_STAND, LECTERN,
                 CHISELED_BOOKSHELF, DECORATED_POT,
                 // Switchable
                 LEVER, REPEATER, COMPARATOR, DAYLIGHT_DETECTOR, NOTE_BLOCK, JUKEBOX,
                 // Functional
                 CRAFTING_TABLE, LOOM, GRINDSTONE, STONECUTTER, CARTOGRAPHY_TABLE,
                 SMITHING_TABLE, ENCHANTING_TABLE, BEACON, BELL, COMPOSTER, CAKE,
                 RESPAWN_ANCHOR -> true;
            default -> false;
        };
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
