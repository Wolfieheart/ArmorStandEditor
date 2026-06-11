package io.github.rypofalem.armorstandeditor.SmokeTest;

import io.github.rypofalem.armorstandeditor.BasePluginTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class ArmorStandEditorPluginLifeCycleTest extends BasePluginTest {
    /*
     * Basic LifeCycle Tests
     */
    @Test
    @DisplayName("Verify that ASE Actually Loaded Successfully")
    void testPluginLoad(){
        assertNotNull(plugin);
        assertTrue(plugin.isEnabled());
    }

    @Test
    @DisplayName("Verify that the configs were created and loaded")
    void testConfigs(){
        File playerHeadsFile = new File(plugin.getDataFolder(), "playerheads.yml");

        //Configs that ASE Needs
        assertNotNull(plugin.getConfig());
        assertNotNull(playerHeadsFile);
    }

    @Test
    @DisplayName("Verify we have the HeadDataManager Loaded")
    void shouldInitalizeHeadDataManager() {
        assertNotNull(plugin.getHeadDataMananger());
    }

    @Test
    @DisplayName("Verify we have the language loaded")
    void shouldInitializeLanguageManager() {
        assertNotNull(plugin.getLang());
    }

    @Test
    @DisplayName("Verify that we can get the Scheduler")
    void shouldInitalizeSheduler(){
        assertNotNull(plugin.getScheduler());
    }


    @Test
    @DisplayName("ASE should load successfully in UnitTesting Mode")
    void loadInUTMode(){
        assertTrue(plugin.didLogUnitTestMode(), "Plugin did not log that it is in UnitTesting mode. This likely means it did not load properly.");
    }
}
