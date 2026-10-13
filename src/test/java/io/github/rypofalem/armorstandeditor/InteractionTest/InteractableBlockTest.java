package io.github.rypofalem.armorstandeditor.InteractionTest;

import io.github.rypofalem.armorstandeditor.BasePluginTest;
import io.github.rypofalem.armorstandeditor.PlayerEditorManager;
import io.github.rypofalem.armorstandeditor.utils.Util;
import org.bukkit.Material;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InteractableBlockTest extends BasePluginTest {

    static Stream<Material> interactable() {
        return Stream.of(
                // Tag-based
                Material.OAK_DOOR, Material.OAK_TRAPDOOR, Material.STONE_BUTTON,
                Material.OAK_FENCE_GATE, Material.RED_BED, Material.OAK_SIGN,
                Material.OAK_HANGING_SIGN, Material.SHULKER_BOX, Material.ANVIL,
                Material.CAULDRON, Material.CAMPFIRE, Material.FLOWER_POT,
                Material.WHITE_CANDLE, Material.BEEHIVE,
                // Containers
                Material.CHEST, Material.TRAPPED_CHEST, Material.ENDER_CHEST, Material.BARREL,
                Material.HOPPER, Material.DROPPER, Material.DISPENSER, Material.CRAFTER,
                Material.FURNACE, Material.BLAST_FURNACE, Material.SMOKER,
                Material.BREWING_STAND, Material.LECTERN, Material.CHISELED_BOOKSHELF,
                Material.DECORATED_POT,
                // Switchable
                Material.LEVER, Material.REPEATER, Material.COMPARATOR,
                Material.DAYLIGHT_DETECTOR, Material.NOTE_BLOCK, Material.JUKEBOX,
                // Functional
                Material.CRAFTING_TABLE, Material.LOOM, Material.GRINDSTONE,
                Material.STONECUTTER, Material.CARTOGRAPHY_TABLE, Material.SMITHING_TABLE,
                Material.ENCHANTING_TABLE, Material.BEACON, Material.BELL,
                Material.COMPOSTER, Material.CAKE, Material.RESPAWN_ANCHOR
        );
    }

    static Stream<Material> notInteractable() {
        return Stream.of(
                Material.STONE, Material.DIRT, Material.OAK_PLANKS, Material.OAK_LOG,
                Material.GLASS, Material.AIR, Material.TORCH, Material.OAK_FENCE
        );
    }

    @ParameterizedTest(name = "{0} is interactable")
    @MethodSource("interactable")
    @DisplayName("Interactable blocks are detected")
    void interactableBlocksReturnTrue(Material type) {
        assertTrue(PlayerEditorManager.isInteractable(type), type + " should be interactable");
    }

    @ParameterizedTest(name = "{0} is not interactable")
    @MethodSource("notInteractable")
    @DisplayName("Plain blocks are not detected as interactable")
    void plainBlocksReturnFalse(Material type) {
        assertFalse(PlayerEditorManager.isInteractable(type), type + " should not be interactable");
    }

    @Test
    @DisplayName("Iron door and iron trapdoor cannot be opened by hand")
    void ironDoorsAreExcluded() {
        assertFalse(PlayerEditorManager.isInteractable(Material.IRON_DOOR));
        assertFalse(PlayerEditorManager.isInteractable(Material.IRON_TRAPDOOR));
    }

    @ParameterizedTest(name = "{0} is interactable")
    @EnumSource(value = Material.class, names = {"^.*_SHELF$"}, mode = EnumSource.Mode.MATCH_ANY)
    @DisplayName("Every shelf material is interactable")
    void allShelvesAreInteractable(Material type) {
        assertTrue(PlayerEditorManager.isInteractable(type), type + " should be interactable");
    }
}