package io.github.rypofalem.armorstandeditor.ConfigTest;

import io.github.rypofalem.armorstandeditor.BasePluginTest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArmorStandEditorConfigTest extends BasePluginTest {

    @Test
    @DisplayName("Load and check the Values that HAVE TO BE THERE for ASE to work")
    void testRequiredConfigValues() {

        plugin.loadConfigValues();

        // ---------------------------
        // LANGUAGE
        // ---------------------------
        String lang = plugin.getConfig().getString("lang", "");
        assertTrue(
                lang != null && !lang.isBlank(),
                "Language config value is missing or empty. ASE cannot start correctly."
        );

        // ---------------------------
        // COARSE AND FINE ROTATION
        // ---------------------------
        double coarseRot = plugin.getConfig().getDouble("coarse", Double.NaN);
        double fineRot = plugin.getConfig().getDouble("fine", Double.NaN);
        assertFalse(Double.isNaN(coarseRot), "Missing config: coarse");
        assertFalse(Double.isNaN(fineRot), "Missing config: fine");

        // ---------------------------
        // SCALING VALUES
        // ---------------------------
        double maxScale = plugin.getConfig().getDouble("maxScaleValue", Double.NaN);
        double minScale = plugin.getConfig().getDouble("minScaleValue", Double.NaN);
        assertFalse(Double.isNaN(maxScale), "Missing config: maxScaleValue");
        assertFalse(Double.isNaN(minScale), "Missing config: minScaleValue");
        assertTrue(maxScale >= minScale, "Invalid config: maxScaleValue must be >= minScaleValue");

        // ---------------------------
        // RESET RANGE
        // ---------------------------
        double resetRange = plugin.getConfig().getDouble("maxResetRange", Double.NaN);
        assertFalse(Double.isNaN(resetRange), "Missing config: maxResetRange");
        assertTrue(resetRange > 0, "Invalid config: maxResetRange must be > 0");

        // ---------------------------
        // REQUIRED BOOLEAN FLAGS
        // ---------------------------
        assertTrue(plugin.getConfig().contains("armorStandVisibility"), "Missing config: armorStandVisibility");
        assertTrue(plugin.getConfig().contains("defaultGravitySetting"), "Missing config: defaultGravitySetting");

        // ---------------------------
        // SAFE DEFAULT CHECKS
        // ---------------------------
        assertTrue(plugin.getConfig().contains("requireToolData"), "Missing config: requireToolData");
        assertTrue(plugin.getConfig().contains("requireToolName"), "Missing config: requireToolName");
        assertTrue(plugin.getConfig().contains("requireToolLore"), "Missing config: requireToolLore");
    }




}
