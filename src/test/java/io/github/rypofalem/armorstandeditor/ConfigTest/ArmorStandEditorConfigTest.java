package io.github.rypofalem.armorstandeditor.ConfigTest;

import io.github.rypofalem.armorstandeditor.BasePluginTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArmorStandEditorConfigTest extends BasePluginTest {

    @BeforeEach
    void setUp(){
        plugin.setDebugFlag(true);
    }


    @Test
    @DisplayName("Load and check the Values that HAVE TO BE THERE for ASE to work")
    void testRequiredConfigValues() {

        plugin.debug.log("[testRequiredConfigValues] action: loadConfigValues()");
        plugin.loadConfigValues();

        //Override the Default Config Value.
        plugin.setDebugFlag(true);

        // ---------------------------
        // LANGUAGE
        // ---------------------------
        String lang = plugin.getConfig().getString("lang", "");
        plugin.debug.log("[testRequiredConfigValues] assertion: lang='" + lang + "'");
        assertTrue(
                lang != null && !lang.isBlank(),
                "Language config value is missing or empty. ASE cannot start correctly."
        );

        // ---------------------------
        // COARSE AND FINE ROTATION
        // ---------------------------
        double coarseRot = plugin.getConfig().getDouble("coarse", Double.NaN);
        double fineRot = plugin.getConfig().getDouble("fine", Double.NaN);
        plugin.debug.log("[testRequiredConfigValues] assertion: coarseRot=" + coarseRot + ", fineRot=" + fineRot);
        assertFalse(Double.isNaN(coarseRot), "Missing config: coarse");
        assertFalse(Double.isNaN(fineRot), "Missing config: fine");

        // ---------------------------
        // SCALING VALUES
        // ---------------------------
        double maxScale = plugin.getConfig().getDouble("maxScaleValue", Double.NaN);
        double minScale = plugin.getConfig().getDouble("minScaleValue", Double.NaN);
        plugin.debug.log("[testRequiredConfigValues] assertion: maxScale=" + maxScale + ", minScale=" + minScale);
        assertFalse(Double.isNaN(maxScale), "Missing config: maxScaleValue");
        assertFalse(Double.isNaN(minScale), "Missing config: minScaleValue");
        assertTrue(maxScale >= minScale, "Invalid config: maxScaleValue must be >= minScaleValue");

        // ---------------------------
        // RESET RANGE
        // ---------------------------
        double resetRange = plugin.getConfig().getDouble("maxResetRange", Double.NaN);
        plugin.debug.log("[testRequiredConfigValues] assertion: resetRange=" + resetRange);
        assertFalse(Double.isNaN(resetRange), "Missing config: maxResetRange");
        assertTrue(resetRange > 0, "Invalid config: maxResetRange must be > 0");

        // ---------------------------
        // REQUIRED BOOLEAN FLAGS
        // ---------------------------
        plugin.debug.log("[testRequiredConfigValues] assertion: checking armorStandVisibility/defaultGravitySetting keys exist");
        assertTrue(plugin.getConfig().contains("armorStandVisibility"), "Missing config: armorStandVisibility");
        assertTrue(plugin.getConfig().contains("defaultGravitySetting"), "Missing config: defaultGravitySetting");

        // ---------------------------
        // SAFE DEFAULT CHECKS
        // ---------------------------
        plugin.debug.log("[testRequiredConfigValues] assertion: checking requireToolData/requireToolName/requireToolLore keys exist");
        assertTrue(plugin.getConfig().contains("requireToolData"), "Missing config: requireToolData");
        assertTrue(plugin.getConfig().contains("requireToolName"), "Missing config: requireToolName");
        assertTrue(plugin.getConfig().contains("requireToolLore"), "Missing config: requireToolLore");
    }




}