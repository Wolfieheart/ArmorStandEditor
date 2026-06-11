package io.github.rypofalem.armorstandeditor.ScoreboardTest;

import io.github.rypofalem.armorstandeditor.BasePluginTest;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ArmorStandEditorScoreboardTest extends BasePluginTest{

    @Test
    @DisplayName("Should register required scoreboard teams during plugin startup")
    void shouldRegisterRequiredScoreboardTeams() {

        Scoreboard scoreboard = server.getScoreboardManager().getMainScoreboard();

        Team locked = scoreboard.getTeam("ASLocked");
        Team inUse = scoreboard.getTeam("AS-InUse");

        assertNotNull(locked, "ASLocked scoreboard team was not created during startup.");
        assertNotNull(inUse, "AS-InUse scoreboard team was not created during startup.");
        assertEquals(NamedTextColor.RED, locked.color(), "ASLocked team should be red.");
    }

    @Test
    @DisplayName("Should preserve required scoreboard teams after reload")
    void shouldPreserveScoreboardTeamsAfterReload() {

        plugin.performReload();

        Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getMainScoreboard();

        assertNotNull(scoreboard.getTeam("ASLocked"));
        assertNotNull(scoreboard.getTeam("AS-InUse"));
        assertEquals(NamedTextColor.RED, scoreboard.getTeam("ASLocked").color(), "ASLocked team should STILL be red.");
    }
}
