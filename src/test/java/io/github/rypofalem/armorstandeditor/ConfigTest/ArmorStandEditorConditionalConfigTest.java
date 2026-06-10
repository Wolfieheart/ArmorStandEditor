package io.github.rypofalem.armorstandeditor.ConfigTest;

import io.github.rypofalem.armorstandeditor.BasePluginTest;
import org.bukkit.World;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ArmorStandEditorConditionalConfigTest extends BasePluginTest {

    // -------------------------
    // HELPERS
    // -------------------------

    private void load() {
        plugin.loadConfigValues();
        plugin.loadConditionalConfig();
    }

    private void setBoolean(String path, boolean value) {
        plugin.getConfig().set(path, value);
    }

    private void setList(String path, List<?> value) {
        plugin.getConfig().set(path, value);
    }

    // -------------------------
    // CONDITIONAL FLAGS
    // -------------------------

    @Test
    @DisplayName("Should load conditional config flags correctly")
    void conditionalFlagsAreLoadedCorrectly() {

        setBoolean("allowCustomModelData", true);
        setBoolean("enablePerWorldSupport", true);
        setBoolean("allowedToRetrieveOwnPlayerHead", false);
        setBoolean("enableBlockedNames", true);
        setBoolean("debugFlag", true);

        load();

        assertTrue(plugin.getAllowCustomModelData());
        assertTrue(plugin.getEnablePerWorldSupport());
        assertFalse(plugin.getAllowedToRetrieveOwnPlayerHead());
        assertTrue(plugin.getEnableBlockedNames());
        assertTrue(plugin.isDebug());
    }

    // -------------------------
    // CUSTOM MODEL DATA
    // -------------------------

    @Test
    @DisplayName("Should load custom model data only when enabled")
    void customModelDataLoadsWhenEnabled() {

        setBoolean("allowCustomModelData", true);
        plugin.getConfig().set("customModelDataInt", 42);

        load();

        assertEquals(42, plugin.getCustomModelDataValue());
    }

    @Test
    @DisplayName("Should ignore custom model data when disabled")
    void customModelDataIgnoredWhenDisabled() {

        setBoolean("allowCustomModelData", false);
        plugin.getConfig().set("customModelDataInt", 99);

        load();

        assertEquals(0, plugin.getCustomModelDataValue(),
                "Value should remain default when disabled");
    }

    // -------------------------
    // WORLD SUPPORT
    // -------------------------

    @Test
    @DisplayName("Should load allowed worlds correctly")
    void allowedWorldsAreLoadedCorrectly() {

        setBoolean("enablePerWorldSupport", true);
        setList("allowed-worlds", List.of("world", "world_nether", "world_the_end"));

        load();

        List<String> worlds = plugin.getAllowedWorldList();

        assertNotNull(worlds);
        assertEquals(3, worlds.size());
        assertTrue(worlds.contains("world"));
    }

    @Test
    @DisplayName("Should expand wildcard into all worlds")
    void wildcardExpandsToAllWorlds() {

        setBoolean("enablePerWorldSupport", true);
        setList("allowed-worlds", List.of("*"));

        load();

        List<String> worlds = plugin.getAllowedWorldList();
        List<World> serverWorlds = plugin.getServer().getWorlds();

        assertEquals(serverWorlds.size(), worlds.size());

        for (World world : serverWorlds) {
            assertTrue(worlds.contains(world.getName()));
        }
    }

    // -------------------------
    // PLAYER HEAD
    // -------------------------

    @Test
    @DisplayName("Should load allowedToRetrieveOwnPlayerHead")
    void playerHeadFlag() {

        setBoolean("allowedToRetrieveOwnPlayerHead", false);

        load();

        assertFalse(plugin.getAllowedToRetrieveOwnPlayerHead());
    }

    @Test
    @DisplayName("Should default allowedToRetrieveOwnPlayerHead to false")
    void playerHeadDefault() {

        load();

        assertFalse(plugin.getAllowedToRetrieveOwnPlayerHead());
    }

    // -------------------------
    // DEBUG FLAG
    // -------------------------

    @Test
    @DisplayName("Should load debug flag correctly")
    void debugFlagLoads() {

        setBoolean("debugFlag", true);

        load();

        assertTrue(plugin.isDebug());
    }

    @Test
    @DisplayName("Should default debug flag to false")
    void debugFlagDefault() {

        load();

        assertFalse(plugin.isDebug());
    }

    // -------------------------
    // BLOCKED NAMES
    // -------------------------

    @Test
    @DisplayName("Should load blocked names when enabled")
    void blockedNamesLoads() {

        setBoolean("enableBlockedNames", true);
        setList("blocked-names", List.of("badname1", "badname2"));

        load();

        assertTrue(plugin.getEnableBlockedNames());
        assertEquals(2, plugin.getListOfBlockedNames().size());
        assertTrue(plugin.getListOfBlockedNames().contains("badname1"));
    }

    @Test
    @DisplayName("Should handle empty blocked names list")
    void blockedNamesEmpty() {

        setBoolean("enableBlockedNames", true);
        setList("blocked-names", List.of());

        load();

        assertTrue(plugin.getEnableBlockedNames());
        assertTrue(plugin.getListOfBlockedNames().isEmpty());
    }
}