package io.github.rypofalem.armorstandeditor.SmokeTest;

import io.github.rypofalem.armorstandeditor.BasePluginTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;

public class ArmorStandEditorPluginLifeCycleTest extends BasePluginTest {
    /*
     * Basic LifeCycle Tests
     */
    @Test
    @DisplayName("Verify that ASE Actually Loaded Successfully")
    void testPluginLoad(){
        Assertions.assertNotNull(plugin);
        Assertions.assertTrue(plugin.isEnabled());
    }

    @Test
    @DisplayName("Verify that the configs were created and loaded")
    void testConfigs(){
        File playerHeadsFile = new File(plugin.getDataFolder(), "playerheads.yml");

        //Configs that ASE Needs
        Assertions.assertNotNull(plugin.getConfig());
        Assertions.assertNotNull(playerHeadsFile);
    }

    @Test
    @DisplayName("Verify we have the scheduler and the managers loaded")
    void pluginHasSchedulerAndManagersAfterEnable() {
        Assertions.assertNotNull(plugin.getScheduler());
        Assertions.assertNotNull(plugin.editorManager);
        Assertions.assertNotNull(plugin.getHeadDataMananger());
    }

    @Test
    @DisplayName("ASE should load successfully in UnitTesting Mode")
    void loadInUTMode(){
        Assertions.assertTrue(plugin.didLogUnitTestMode(), "Plugin did not log that it is in UnitTesting mode. This likely means it did not load properly.");
    }
}
