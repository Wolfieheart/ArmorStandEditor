package io.github.rypofalem.armorstandeditor.ScoreboardTest;

import io.github.rypofalem.armorstandeditor.BasePluginTest;

import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArmorStandEditorScoreboardTest extends BasePluginTest{

    @BeforeEach
    void setUp(){
        plugin.setDebugFlag(true);
    }

    @Test
    @DisplayName("Should register required scoreboard teams during plugin startup")
    void shouldRegisterRequiredScoreboardTeams() {
        plugin.debug.log("[shouldRegisterRequiredScoreboardTeams] setup: fetching main scoreboard");
        Scoreboard scoreboard = server.getScoreboardManager().getMainScoreboard();

        plugin.debug.log("[shouldRegisterRequiredScoreboardTeams] action: looking up ASLocked and AS-InUse teams");
        Team locked = scoreboard.getTeam("ASLocked");
        Team inUse = scoreboard.getTeam("AS-InUse");

        plugin.debug.log("[shouldRegisterRequiredScoreboardTeams] assertion: locked=" + locked + ", inUse=" + inUse);
        assertNotNull(locked, "ASLocked scoreboard team was not created during startup.");
        assertNotNull(inUse, "AS-InUse scoreboard team was not created during startup.");
        assertEquals(NamedTextColor.RED, locked.color(), "ASLocked team should be red.");
    }

    @Test
    @DisplayName("Should preserve required scoreboard teams after reload")
    void shouldPreserveScoreboardTeamsAfterReload() {
        plugin.debug.log("[shouldPreserveScoreboardTeamsAfterReload] action: performing plugin.performReload()");
        plugin.performReload();

        Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();

        plugin.debug.log("[shouldPreserveScoreboardTeamsAfterReload] assertion: verifying teams still present after reload");
        assertNotNull(scoreboard.getTeam("ASLocked"));
        assertNotNull(scoreboard.getTeam("AS-InUse"));
        assertEquals(NamedTextColor.RED, scoreboard.getTeam("ASLocked").color(), "ASLocked team should STILL be red.");
    }
}