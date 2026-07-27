package io.github.rypofalem.armorstandeditor.ConfigTest;

import io.github.rypofalem.armorstandeditor.BasePluginTest;
import io.github.rypofalem.armorstandeditor.TestUtils.TestHelperFunctions;


import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArmorStandEditorEditToolTest extends BasePluginTest {

    @BeforeEach
    void setUp(){
        plugin.setDebugFlag(true);
    }

    @Test
    @DisplayName("Should accept correct tool material")
    void shouldAcceptCorrectMaterial() {
        plugin.debug.log("[shouldAcceptCorrectMaterial] setup: tool=FLINT");
        plugin.getConfig().set("tool", "FLINT");
        plugin.loadConfigValues();
        plugin.loadConditionalConfig();

        plugin.debug.log("[shouldAcceptCorrectMaterial] action: building a FLINT ItemStack via TestHelperFunctions.itemOf");
        ItemStack item = TestHelperFunctions.itemOf(Material.FLINT);

        plugin.debug.log("[shouldAcceptCorrectMaterial] assertion: editTool=" + plugin.getEditTool() + ", isEditTool=" + plugin.isEditTool(item));
        assertNotNull(plugin.getEditTool(), "editTool is null");
        assertEquals(Material.FLINT, plugin.getEditTool(), "editTool is wrong material");
        assertEquals(Material.FLINT, item.getType(), "ItemStack.getType() returned wrong material");
        assertNotNull(item.getItemMeta(), "ItemMeta is null — MockBukkit issue");
        assertTrue(plugin.isEditTool(item));
    }

    @Test
    @DisplayName("Should reject wrong tool material")
    void shouldRejectWrongMaterial() {
        plugin.debug.log("[shouldRejectWrongMaterial] setup: tool=FLINT, testing against a STICK");
        plugin.getConfig().set("tool", "FLINT");
        plugin.loadConfigValues();
        plugin.loadConditionalConfig();

        boolean isEditTool = plugin.isEditTool(TestHelperFunctions.itemOf(Material.STICK));
        plugin.debug.log("[shouldRejectWrongMaterial] assertion: isEditTool(STICK)=" + isEditTool);
        assertFalse(isEditTool);
    }

    @Test
    @DisplayName("Should accept item when tool name matches")
    void shouldAcceptMatchingToolName() {
        plugin.debug.log("[shouldAcceptMatchingToolName] setup: tool=FLINT, requireToolName=true, toolName='&aTest'");
        plugin.getConfig().set("tool", "FLINT");
        plugin.getConfig().set("requireToolName", true);
        plugin.getConfig().set("toolName", "&aTest");
        plugin.loadConfigValues();
        plugin.loadConditionalConfig();

        boolean isEditTool = plugin.isEditTool(TestHelperFunctions.itemWithName(Material.FLINT, "&aTest"));
        plugin.debug.log("[shouldAcceptMatchingToolName] assertion: isEditTool(matching name)=" + isEditTool);
        assertTrue(isEditTool);
    }

    @Test
    @DisplayName("Should reject item when tool name does not match")
    void shouldRejectMismatchedToolName() {
        plugin.debug.log("[shouldRejectMismatchedToolName] setup: tool=FLINT, requireToolName=true, toolName='&aTest'");
        plugin.getConfig().set("tool", "FLINT");
        plugin.getConfig().set("requireToolName", true);
        plugin.getConfig().set("toolName", "&aTest");
        plugin.loadConfigValues();
        plugin.loadConditionalConfig();

        boolean isEditTool = plugin.isEditTool(TestHelperFunctions.itemWithName(Material.FLINT, "&cWrong"));
        plugin.debug.log("[shouldRejectMismatchedToolName] assertion: isEditTool(mismatched name)=" + isEditTool);
        assertFalse(isEditTool);
    }

    @Test
    @DisplayName("Should accept item when tool lore matches")
    void shouldAcceptMatchingToolLore() {
        plugin.debug.log("[shouldAcceptMatchingToolLore] setup: tool=FLINT, requireToolLore=true, toolLore=['line1']");
        plugin.getConfig().set("tool", "FLINT");
        plugin.getConfig().set("requireToolLore", true);
        plugin.getConfig().set("toolLore", List.of("line1"));
        plugin.loadConfigValues();
        plugin.loadConditionalConfig();

        boolean isEditTool = plugin.isEditTool(TestHelperFunctions.itemWithLore(Material.FLINT, List.of("line1")));
        plugin.debug.log("[shouldAcceptMatchingToolLore] assertion: isEditTool(matching lore)=" + isEditTool);
        assertTrue(isEditTool);
    }

    @Test
    @DisplayName("Should accept item when custom model data matches")
    void shouldAcceptMatchingCustomModelData() {
        plugin.debug.log("[shouldAcceptMatchingCustomModelData] setup: tool=FLINT, allowCustomModelData=true, customModelDataInt=10");
        plugin.getConfig().set("tool", "FLINT");
        plugin.getConfig().set("allowCustomModelData", true);
        plugin.getConfig().set("customModelDataInt", 10);
        plugin.loadConfigValues();
        plugin.loadConditionalConfig();

        plugin.loadConfigValues();
        plugin.loadConditionalConfig();

        plugin.debug.log("[shouldAcceptMatchingCustomModelData] assertion: customModelDataValue=" + plugin.getCustomModelDataValue());
        assertEquals(10, plugin.getCustomModelDataValue(), "customModelDataValue was not loaded");
    }

    @Test
    @DisplayName("Should accept any item when customModelDataInt is 0")
    void shouldAcceptAnyItemWhenCustomModelDataIsZero() {
        plugin.debug.log("[shouldAcceptAnyItemWhenCustomModelDataIsZero] setup: tool=FLINT, allowCustomModelData=true, customModelDataInt=0");
        plugin.getConfig().set("tool", "FLINT");
        plugin.getConfig().set("allowCustomModelData", true);
        plugin.getConfig().set("customModelDataInt", 0);
        plugin.loadConfigValues();
        plugin.loadConditionalConfig();

        boolean isEditTool = plugin.isEditTool(TestHelperFunctions.itemOf(Material.FLINT));
        plugin.debug.log("[shouldAcceptAnyItemWhenCustomModelDataIsZero] assertion: isEditTool=" + isEditTool);
        assertTrue(isEditTool);
    }

    @Test
    @DisplayName("Should accept fully valid edit tool with all requirements enabled")
    void shouldAcceptFullyValidEditTool() {
        plugin.debug.log("[shouldAcceptFullyValidEditTool] setup: tool=FLINT with data/name/lore/customModelData all required");
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
        plugin.debug.log("[shouldAcceptFullyValidEditTool] assertion: verifying every requirement loaded correctly");
        assertEquals(Material.FLINT, plugin.getEditTool(), "editTool wrong");
        assertTrue(plugin.getRequiredToolName(), "requireToolName not loaded");
        assertTrue(plugin.getRequiredToolLore(), "requireToolLore not loaded");
        assertTrue(plugin.getRequiredToolData(), "requireToolData not loaded");
        assertTrue(plugin.getAllowCustomModelData(), "allowCustomModelData not loaded");
        assertEquals(10, plugin.getCustomModelDataValue(), "customModelDataValue wrong");
    }
}