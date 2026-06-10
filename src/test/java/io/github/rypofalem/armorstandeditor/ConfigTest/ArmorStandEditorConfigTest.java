package io.github.rypofalem.armorstandeditor.ConfigTest;

import io.github.rypofalem.armorstandeditor.BasePluginTest;
import org.bukkit.Material;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ArmorStandEditorConfigTest extends BasePluginTest {

    @Test
    @DisplayName("Load and check the Values that HAVE TO BE THERE for ASE to work")
    void testRequiredConfigValues() {

        plugin.loadConfigValues();

        // ---------------------------
        // LANGUAGE
        // ---------------------------
        String lang = plugin.getConfig().getString("lang", "");
        Assertions.assertTrue(
                lang != null && !lang.isBlank(),
                "Language config value is missing or empty. ASE cannot start correctly."
        );

        // ---------------------------
        // TOOL IN USE
        // ---------------------------
        Assertions.assertNotNull(plugin.getEditTool());
        Assertions.assertNotEquals(Material.STICK, plugin.getEditTool(), "editTool is not correctly configured or failed to load");

        // ---------------------------
        // COARSE AND FINE ROTATION
        // ---------------------------
        double coarseRot = plugin.getConfig().getDouble("coarse", Double.NaN);
        double fineRot = plugin.getConfig().getDouble("fine", Double.NaN);
        Assertions.assertFalse(Double.isNaN(coarseRot), "Missing config: coarse");
        Assertions.assertFalse(Double.isNaN(fineRot), "Missing config: fine");

        // ---------------------------
        // SCALING VALUES
        // ---------------------------
        double maxScale = plugin.getConfig().getDouble("maxScaleValue", Double.NaN);
        double minScale = plugin.getConfig().getDouble("minScaleValue", Double.NaN);
        Assertions.assertFalse(Double.isNaN(maxScale), "Missing config: maxScaleValue");
        Assertions.assertFalse(Double.isNaN(minScale), "Missing config: minScaleValue");
        Assertions.assertTrue(maxScale >= minScale, "Invalid config: maxScaleValue must be >= minScaleValue");

        // ---------------------------
        // RESET RANGE
        // ---------------------------
        double resetRange = plugin.getConfig().getDouble("maxResetRange", Double.NaN);
        Assertions.assertFalse(Double.isNaN(resetRange), "Missing config: maxResetRange");
        Assertions.assertTrue(resetRange > 0, "Invalid config: maxResetRange must be > 0");

        // ---------------------------
        // REQUIRED BOOLEAN FLAGS
        // ---------------------------
        Assertions.assertTrue(plugin.getConfig().contains("armorStandVisibility"), "Missing config: armorStandVisibility");
        Assertions.assertTrue(plugin.getConfig().contains("defaultGravitySetting"), "Missing config: defaultGravitySetting");

        // ---------------------------
        // SAFE DEFAULT CHECKS
        // ---------------------------
        Assertions.assertTrue(plugin.getConfig().contains("requireToolData"), "Missing config: requireToolData");
        Assertions.assertTrue(plugin.getConfig().contains("requireToolName"), "Missing config: requireToolName");
        Assertions.assertTrue(plugin.getConfig().contains("requireToolLore"), "Missing config: requireToolLore");
    }




}
