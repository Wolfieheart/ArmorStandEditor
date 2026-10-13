package io.github.rypofalem.armorstandeditor.SmokeTest;

import io.github.rypofalem.armorstandeditor.BasePluginTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ArmorStandEditorPluginLifeCycleTest extends BasePluginTest {
    /*
     * Basic LifeCycle Tests
     */
    @BeforeEach
    void setup(){
        plugin.setDebugFlag(true);
    }


    @Test
    @DisplayName("Verify that ASE Actually Loaded Successfully")
    void testPluginLoad(){
        plugin.debug.log("[testPluginLoad] assertion: checking plugin is non-null and enabled");
        assertNotNull(plugin);
        assertTrue(plugin.isEnabled());
    }

    @Test
    @DisplayName("Verify that the configs were created and loaded")
    void testConfigs(){
        plugin.debug.log("[testConfigs] setup: locating playerheads.yml in the data folder");
        File playerHeadsFile = new File(plugin.getDataFolder(), "playerheads.yml");

        //Configs that ASE Needs
        plugin.debug.log("[testConfigs] assertion: verifying config and playerheads file references are present");
        assertNotNull(plugin.getConfig());
        assertNotNull(playerHeadsFile);
    }

    @Test
    @DisplayName("Verify we have the HeadDataManager Loaded")
    void shouldInitalizeHeadDataManager() {
        plugin.debug.log("[shouldInitalizeHeadDataManager] assertion: checking HeadDataManager is non-null");
        assertNotNull(plugin.getHeadDataMananger());
    }

    @Test
    @DisplayName("Verify we have the language loaded")
    void shouldInitializeLanguageManager() {
        plugin.debug.log("[shouldInitializeLanguageManager] assertion: checking Language manager is non-null");
        assertNotNull(plugin.getLang());
    }

    @Test
    @DisplayName("Verify that we can get the Scheduler")
    void shouldInitalizeSheduler(){
        plugin.debug.log("[shouldInitalizeSheduler] assertion: checking Scheduler is non-null");
        assertNotNull(plugin.getScheduler());
    }


    @Test
    @DisplayName("ASE should load successfully in UnitTesting Mode")
    void loadInUTMode(){
        plugin.debug.log("[loadInUTMode] assertion: checking plugin logged UnitTesting mode");
        assertTrue(plugin.didLogUnitTestMode(), "Plugin did not log that it is in UnitTesting mode. This likely means it did not load properly.");
    }
}