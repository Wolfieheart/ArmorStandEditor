package io.github.rypofalem.armorstandeditor.UtilsTest;

import io.github.rypofalem.armorstandeditor.modes.InvisibleEmptyMode;
import io.github.rypofalem.armorstandeditor.utils.Util;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class InvisibleEmptyStandTest {

    private static final NamespacedKey KEY = NamespacedKey.fromString("armorstandeditor:auto_glow");

    private ServerMock server;
    private WorldMock world;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    // If world.spawn isn't supported by your MockBukkit version, swap this for new ArmorStandMock(server, UUID.randomUUID())
    private ArmorStand spawnStand() {
        return world.spawn(new Location(world, 0, 64, 0), ArmorStand.class);
    }

    private static void equipHelmet(ArmorStand as) {
        as.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
    }

    private static boolean hasAutoFlag(ArmorStand as) {
        return as.getPersistentDataContainer().has(KEY, PersistentDataType.BYTE);
    }

    // ---- 1. VISIBLE: invisible + empty -> reappears ----

    @Test
    @DisplayName("VISIBLE: invisible stand with no equipment becomes visible")
    void visibleModeRevealsEmptyStand() {
        ArmorStand as = spawnStand();
        as.setVisible(false);

        Util.applyEmptyStandMode(as, InvisibleEmptyMode.VISIBLE, KEY);

        assertTrue(as.isVisible());
    }

    // ---- 2. VISIBLE: with an item -> stays invisible ----

    @Test
    @DisplayName("VISIBLE: invisible stand with equipment stays invisible")
    void visibleModeLeavesEquippedStand() {
        ArmorStand as = spawnStand();
        equipHelmet(as);
        as.setVisible(false);

        Util.applyEmptyStandMode(as, InvisibleEmptyMode.VISIBLE, KEY);

        assertFalse(as.isVisible());
    }

    @Test
    @DisplayName("VISIBLE: an item in the off hand counts as equipment")
    void visibleModeCountsOffHand() {
        ArmorStand as = spawnStand();
        as.getEquipment().setItemInOffHand(new ItemStack(Material.SHIELD));
        as.setVisible(false);

        Util.applyEmptyStandMode(as, InvisibleEmptyMode.VISIBLE, KEY);

        assertFalse(as.isVisible());
    }

    // ---- 3. VISIBLE: last item removed -> reappears (simulates the deferred task) ----

    @Test
    @DisplayName("VISIBLE: removing the last item and re-checking makes the stand visible")
    void visibleModeAfterLastItemRemoved() {
        ArmorStand as = spawnStand();
        equipHelmet(as);
        as.setVisible(false);
        Util.applyEmptyStandMode(as, InvisibleEmptyMode.VISIBLE, KEY);
        assertFalse(as.isVisible());

        as.getEquipment().setHelmet(new ItemStack(Material.AIR)); // what the manipulate event ends up doing
        Util.applyEmptyStandMode(as, InvisibleEmptyMode.VISIBLE, KEY); // what the scheduled task does next tick

        assertTrue(as.isVisible());
    }

    // ---- 4. GLOW ----

    @Test
    @DisplayName("GLOW: invisible empty stand glows and stays invisible")
    void glowModeMakesEmptyStandGlow() {
        ArmorStand as = spawnStand();
        as.setVisible(false);

        Util.applyEmptyStandMode(as, InvisibleEmptyMode.GLOW, KEY);

        assertTrue(as.isGlowing());
        assertFalse(as.isVisible());
        assertTrue(hasAutoFlag(as));
    }

    @Test
    @DisplayName("GLOW: equipping an item stops the auto glow")
    void glowModeStopsWhenEquipped() {
        ArmorStand as = spawnStand();
        as.setVisible(false);
        Util.applyEmptyStandMode(as, InvisibleEmptyMode.GLOW, KEY);

        equipHelmet(as);
        Util.applyEmptyStandMode(as, InvisibleEmptyMode.GLOW, KEY);

        assertFalse(as.isGlowing());
        assertFalse(hasAutoFlag(as));
    }

    @Test
    @DisplayName("GLOW: a player's own glow is never stripped")
    void glowModeKeepsPlayerGlow() {
        ArmorStand as = spawnStand();
        as.setGlowing(true); // player-owned: no flag
        equipHelmet(as);
        as.setVisible(false);

        Util.applyEmptyStandMode(as, InvisibleEmptyMode.GLOW, KEY);

        assertTrue(as.isGlowing());
        assertFalse(hasAutoFlag(as));
    }

    @Test
    @DisplayName("GLOW: making the stand visible again stops the auto glow")
    void glowModeStopsWhenVisible() {
        ArmorStand as = spawnStand();
        as.setVisible(false);
        Util.applyEmptyStandMode(as, InvisibleEmptyMode.GLOW, KEY);

        as.setVisible(true);
        Util.applyEmptyStandMode(as, InvisibleEmptyMode.GLOW, KEY);

        assertFalse(as.isGlowing());
        assertFalse(hasAutoFlag(as));
    }

    // ---- 5. Hologram ----

    @Test
    @DisplayName("Stand with a visible custom name is left alone in both modes")
    void hologramIsIgnored() {
        for (InvisibleEmptyMode mode : List.of(InvisibleEmptyMode.VISIBLE, InvisibleEmptyMode.GLOW)) {
            ArmorStand as = spawnStand();
            as.customName(Component.text("Hologram"));
            as.setCustomNameVisible(true);
            as.setVisible(false);

            Util.applyEmptyStandMode(as, mode, KEY);

            assertFalse(as.isVisible(), mode + " must not reveal a hologram");
            assertFalse(as.isGlowing(), mode + " must not glow a hologram");
        }
    }

    @Test
    @DisplayName("OFF: nothing changes")
    void offModeDoesNothing() {
        ArmorStand as = spawnStand();
        as.setVisible(false);

        Util.applyEmptyStandMode(as, InvisibleEmptyMode.OFF, KEY);

        assertFalse(as.isVisible());
        assertFalse(as.isGlowing());
    }

    // ---- 6. Config parsing ----

    @Test
    @DisplayName("Invalid mode logs a warning and falls back to OFF")
    void invalidModeFallsBackToOff() {
        Logger logger = Logger.getLogger("InvisibleEmptyStandTest");
        logger.setUseParentHandlers(false);
        List<LogRecord> records = new ArrayList<>();
        logger.addHandler(new Handler() {
            @Override public void publish(LogRecord r) { records.add(r); }
            @Override public void flush() { }
            @Override public void close() { }
        });

        assertEquals(InvisibleEmptyMode.OFF, InvisibleEmptyMode.parse("banana", logger));
        assertEquals(1, records.size());
        assertEquals(java.util.logging.Level.WARNING, records.get(0).getLevel());
    }

    @Test
    @DisplayName("Valid modes parse case-insensitively without a warning")
    void validModesParse() {
        Logger logger = Logger.getLogger("InvisibleEmptyStandTestQuiet");
        assertEquals(InvisibleEmptyMode.GLOW, InvisibleEmptyMode.parse("glow", logger));
        assertEquals(InvisibleEmptyMode.VISIBLE, InvisibleEmptyMode.parse("Visible", logger));
        assertEquals(InvisibleEmptyMode.OFF, InvisibleEmptyMode.parse("OFF", logger));
    }
}
