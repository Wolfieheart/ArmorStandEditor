package io.github.rypofalem.armorstandeditor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;

public abstract class BasePluginTest {

    protected ServerMock server;
    protected ArmorStandEditorPlugin plugin;

    @BeforeEach
    void setupServer() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(ArmorStandEditorPlugin.class, true);
    }

    @AfterEach
    void tearDownServer() {
        /*
          ArmorStandEditorPlugin#onDisable walks Bukkit.getOnlinePlayers() and calls
          PaperLib.getHolder(player.getOpenInventory().getTopInventory(), false) for each.
          MockBukkit's default (nothing-open) InventoryView has a null top inventory - real
           Bukkit never hands the plugin a null top there, so onDisable has no null-check for
           it - and that combination NPEs on unmock for any test that leaves a player online.
           Kicking players first empties the online-player list so onDisable's loop never runs.
           (Flagged the underlying missing null-check to Wolfie separately - this is a test
            workaround, not a fix for the plugin itself.)
         */
        for (PlayerMock player : List.copyOf(server.getOnlinePlayers())) {
            player.kick();
        }

        MockBukkit.unmock();
    }

    /**
     * Adds an online player and grants op so every {@code asedit.*} permission
     * check and protection op-bypass branch passes. Use this unless a test is
     * specifically exercising permission denial.
     */
    protected PlayerMock newEditor() {
        PlayerMock player = server.addPlayer();
        player.setOp(true);
        return player;
    }

}