package io.github.rypofalem.armorstandeditor.ConfigTest;

import io.github.rypofalem.armorstandeditor.BasePluginTest;
import io.github.rypofalem.armorstandeditor.TestHelperFunctions;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArmorStandEditorEditToolTest extends BasePluginTest {

    @Test
    @DisplayName("Should accept correct tool material")
    void shouldAcceptCorrectMaterial() {
        plugin.getConfig().set("tool", "FLINT");
        plugin.loadConfigValues();
        plugin.loadConditionalConfig();

        ItemStack item = TestHelperFunctions.itemOf(Material.FLINT);

        assertNotNull(plugin.getEditTool(), "editTool is null");
        assertEquals(Material.FLINT, plugin.getEditTool(), "editTool is wrong material");
        assertEquals(Material.FLINT, item.getType(), "ItemStack.getType() returned wrong material");
        assertNotNull(item.getItemMeta(), "ItemMeta is null — MockBukkit issue");
        assertTrue(plugin.isEditTool(item));
    }

    @Test
    @DisplayName("Should reject wrong tool material")
    void shouldRejectWrongMaterial() {
        plugin.getConfig().set("tool", "FLINT");
        plugin.loadConfigValues();
        plugin.loadConditionalConfig();

        assertFalse(plugin.isEditTool(TestHelperFunctions.itemOf(Material.STICK)));
    }

    @Test
    @DisplayName("Should accept item when tool name matches")
    void shouldAcceptMatchingToolName() {
        plugin.getConfig().set("tool", "FLINT");
        plugin.getConfig().set("requireToolName", true);
        plugin.getConfig().set("toolName", "&aTest");
        plugin.loadConfigValues();
        plugin.loadConditionalConfig();

        assertTrue(plugin.isEditTool(TestHelperFunctions.itemWithName(Material.FLINT, "&aTest")));
    }

    @Test
    @DisplayName("Should reject item when tool name does not match")
    void shouldRejectMismatchedToolName() {
        plugin.getConfig().set("tool", "FLINT");
        plugin.getConfig().set("requireToolName", true);
        plugin.getConfig().set("toolName", "&aTest");
        plugin.loadConfigValues();
        plugin.loadConditionalConfig();

        assertFalse(plugin.isEditTool(TestHelperFunctions.itemWithName(Material.FLINT, "&cWrong")));
    }

    @Test
    @DisplayName("Should accept item when tool lore matches")
    void shouldAcceptMatchingToolLore() {
        plugin.getConfig().set("tool", "FLINT");
        plugin.getConfig().set("requireToolLore", true);
        plugin.getConfig().set("toolLore", List.of("line1"));
        plugin.loadConfigValues();
        plugin.loadConditionalConfig();

        assertTrue(plugin.isEditTool(TestHelperFunctions.itemWithLore(Material.FLINT, List.of("line1"))));
    }

    @Test
    @DisplayName("Should accept item when custom model data matches")
    void shouldAcceptMatchingCustomModelData() {
        plugin.getConfig().set("tool", "FLINT");
        plugin.getConfig().set("allowCustomModelData", true);
        plugin.getConfig().set("customModelDataInt", 10);
        plugin.loadConfigValues();
        plugin.loadConditionalConfig();

        plugin.loadConfigValues();
        plugin.loadConditionalConfig();

        assertEquals(10, plugin.getCustomModelDataValue(), "customModelDataValue was not loaded");
    }

    @Test
    @DisplayName("Should accept any item when customModelDataInt is 0")
    void shouldAcceptAnyItemWhenCustomModelDataIsZero() {
        plugin.getConfig().set("tool", "FLINT");
        plugin.getConfig().set("allowCustomModelData", true);
        plugin.getConfig().set("customModelDataInt", 0);
        plugin.loadConfigValues();
        plugin.loadConditionalConfig();

        assertTrue(plugin.isEditTool(TestHelperFunctions.itemOf(Material.FLINT)));
    }

    @Test
    @DisplayName("Should accept fully valid edit tool with all requirements enabled")
    void shouldAcceptFullyValidEditTool() {
        plugin.getConfig().set("tool", "FLINT");
        plugin.getConfig().set("requireToolData", true);
        plugin.getConfig().set("toolData", 0);
        plugin.getConfig().set("requireToolName", true);
        plugin.getConfig().set("toolName", "&aTest");
        plugin.getConfig().set("requireToolLore", true);
        plugin.getConfig().set("toolLore", List.of("line1"));
        plugin.getConfig().set("allowCustomModelData", true);
        plugin.getConfig().set("customModelDataInt", 10);
        plugin.loadConfigValues();
        plugin.loadConditionalConfig();

        // after loadConditionalConfig()
        assertEquals(Material.FLINT, plugin.getEditTool(), "editTool wrong");
        assertTrue(plugin.getRequiredToolName(), "requireToolName not loaded");
        assertTrue(plugin.getRequiredToolLore(), "requireToolLore not loaded");
        assertTrue(plugin.getRequiredToolData(), "requireToolData not loaded");
        assertTrue(plugin.getAllowCustomModelData(), "allowCustomModelData not loaded");
        assertEquals(10, plugin.getCustomModelDataValue(), "customModelDataValue wrong");
    }
}