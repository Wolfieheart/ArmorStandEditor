package io.github.rypofalem.armorstandeditor.ProtectionsTest;

import io.github.rypofalem.armorstandeditor.BaseProtectionTest;
import io.github.rypofalem.armorstandeditor.protections.PlotSquaredProtection;

import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.location.Location;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.sk89q.worldedit.math.BlockVector3;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class PlotSquaredProtectionTest extends BaseProtectionTest {

    private static final UUID PLOT_PLAYER_UUID = UUID.randomUUID();

    private MockedStatic<Location> locationMock;
    private MockedConstruction<PlotAPI> plotApiConstruction;

    private Location plotLocation;
    private PlotArea plotArea;
    private Plot plot;
    private PlotPlayer<?> plotPlayer;

    private PlotSquaredProtection protection;

    @BeforeEach
    void setUp() {
        setPluginEnabled("PlotSquared", true);
        when(world.getName()).thenReturn("world");

        plotLocation = mock(Location.class);
        plotArea = mock(PlotArea.class);
        plot = mock(Plot.class);
        plotPlayer = mock(PlotPlayer.class);
        when(plotPlayer.getUUID()).thenReturn(PLOT_PLAYER_UUID);

        locationMock = mockStatic(Location.class);
        locationMock.when(() -> Location.at(anyString(), any(BlockVector3.class)))
                .thenReturn(plotLocation);

        // Constructed lazily inside checkPermission(); wire wrapPlayer() at construction time.
        plotApiConstruction = mockConstruction(PlotAPI.class, (mock, context) ->
                doReturn(plotPlayer).when(mock).wrapPlayer(any(UUID.class)));

        protection = new PlotSquaredProtection();
    }

    @AfterEach
    void tearDown() {
        plotApiConstruction.close();
        locationMock.close();
    }

    @Test
    @DisplayName("Allows edit when PlotSquared is not enabled")
    void allowsWhenPlotSquaredNotEnabled() {
        setPluginEnabled("PlotSquared", false);
        PlotSquaredProtection disabledProtection = new PlotSquaredProtection();

        assertTrue(disabledProtection.checkPermission(block, player));
    }

    @Test
    @DisplayName("Allows edit when player is op")
    void allowsWhenPlayerIsOp() {
        setPlayerOp(true);

        assertTrue(protection.checkPermission(block, player));
    }

    @Test
    @DisplayName("Allows edit when player has ignoreProtection.plotSquared permission")
    void allowsWhenPlayerHasIgnoreProtectionPermission() {
        setPlayerPermission("asedit.ignoreProtection.plotSquared", true);

        assertTrue(protection.checkPermission(block, player));
    }

    @Test
    @DisplayName("Allows edit when location is not inside any plot area")
    void allowsWhenLocationIsNotInAPlotArea() {
        when(plotLocation.getPlotArea()).thenReturn(null);

        assertTrue(protection.checkPermission(block, player));
    }

    @Test
    @DisplayName("Falls back to road-build permission when area has no plot")
    void fallsBackToRoadPermissionWhenAreaHasNoPlot() {
        when(plotLocation.getPlotArea()).thenReturn(plotArea);
        when(plotArea.getPlot(plotLocation)).thenReturn(null);
        setPlayerPermission("plots.admin.build.road", true);

        assertTrue(protection.checkPermission(block, player));
    }

    @Test
    @DisplayName("Denies road build without road-build permission")
    void deniesRoadBuildWithoutRoadPermission() {
        when(plotLocation.getPlotArea()).thenReturn(plotArea);
        when(plotArea.getPlot(plotLocation)).thenReturn(null);
        setPlayerPermission("plots.admin.build.road", false);

        assertFalse(protection.checkPermission(block, player));
    }

    @Test
    @DisplayName("Allows edit when the plot player cannot be wrapped")
    void allowsWhenPlotPlayerCannotBeWrapped() {
        when(plotLocation.getPlotArea()).thenReturn(plotArea);
        when(plotArea.getPlot(plotLocation)).thenReturn(plot);
        plotApiConstruction.close();
        plotApiConstruction = mockConstruction(PlotAPI.class, (mock, context) ->
                doReturn(null).when(mock).wrapPlayer(any(UUID.class)));
        protection = new PlotSquaredProtection();

        assertTrue(protection.checkPermission(block, player));
    }

    @Test
    @DisplayName("Allows edit when player is added to the plot")
    void allowsWhenPlayerIsAddedToPlot() {
        when(plotLocation.getPlotArea()).thenReturn(plotArea);
        when(plotArea.getPlot(plotLocation)).thenReturn(plot);
        when(plot.isAdded(PLOT_PLAYER_UUID)).thenReturn(true);

        assertTrue(protection.checkPermission(block, player));
    }

    @Test
    @DisplayName("Allows edit when plot player has build.other permission")
    void allowsWhenPlotPlayerHasBuildOtherPermission() {
        when(plotLocation.getPlotArea()).thenReturn(plotArea);
        when(plotArea.getPlot(plotLocation)).thenReturn(plot);
        when(plot.isAdded(PLOT_PLAYER_UUID)).thenReturn(false);
        when(plotPlayer.hasPermission("plots.admin.build.other")).thenReturn(true);

        assertTrue(protection.checkPermission(block, player));
    }

    @Test
    @DisplayName("Denies edit when not added to plot and no build.other permission")
    void deniesWhenNotAddedAndNoBuildOtherPermission() {
        when(plotLocation.getPlotArea()).thenReturn(plotArea);
        when(plotArea.getPlot(plotLocation)).thenReturn(plot);
        when(plot.isAdded(PLOT_PLAYER_UUID)).thenReturn(false);
        when(plotPlayer.hasPermission("plots.admin.build.other")).thenReturn(false);

        assertFalse(protection.checkPermission(block, player));
    }
}