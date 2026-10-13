package io.github.rypofalem.armorstandeditor.ProtectionsTest;

import io.github.rypofalem.armorstandeditor.BaseProtectionTest;

import io.github.rypofalem.armorstandeditor.protections.GriefPreventionProtection;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import net.kyori.adventure.text.Component;

import org.bukkit.entity.ArmorStand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Extends {@link BaseProtectionTest} for the shared Bukkit/ASE plugin scaffolding
 * (loads ASE via the mocked {@code ArmorStandEditorPlugin.instance()}, plus baseline
 * player/block/location/world mocks). GriefPrevention is looked up by
 * {@code GriefPreventionProtection} via {@code Bukkit.getPluginManager().getPlugin(...)},
 * so it's mocked and registered through {@code pluginManager} rather than a static field.
 *
 * All scenarios go through {@code checkPermission(Entity, Player)} with an
 * {@code armorStand} mock, since that's the call path used when a player right-clicks
 * an armor stand with the edit tool.
 */
class GriefPreventionProtectionTest extends BaseProtectionTest {

    private GriefPrevention griefPrevention;
    private DataStore dataStore;
    private ArmorStand armorStand;

    @BeforeEach
    void setUp() {
        griefPrevention = mock(GriefPrevention.class);
        dataStore = mock(DataStore.class);
        griefPrevention.dataStore = dataStore;

        setPluginEnabled("GriefPrevention", true);
        when(pluginManager.getPlugin("GriefPrevention")).thenReturn(griefPrevention);

        armorStand = mock(ArmorStand.class);
        when(armorStand.getLocation()).thenReturn(location);
        when(location.getBlock()).thenReturn(block);

        setPlayerPermission("asedit.ignoreProtection.griefPrevention", false);
        setPlayerPermission("griefprevention.ignoreclaims", false);
        setPlayerOp(false);
    }

    @Test
    @DisplayName("GriefPrevention not present -> editing allowed")
        // GIVEN a player is in a world
        // WHEN the player right-clicks on an armor stand with the edit tool
        // AND GriefPrevention is not there
        // THEN ASE editing should work
    void griefPreventionNotPresent_editingAllowed() {
        setPluginEnabled("GriefPrevention", false);
        GriefPreventionProtection protection = new GriefPreventionProtection();
        assertTrue(protection.checkPermission(armorStand, player));
    }

    @Test
    @DisplayName("ASE bypass permission true, not op -> editing allowed")
        // GIVEN a player is in a world
        // WHEN the player right-clicks on an armor stand with the edit tool
        // AND they have asedit.ignoreProtection.griefPrevention set TRUE
        // AND is NOT op
        // THEN ASE editing should work
    void aseBypassPermissionTrue_notOp_editingAllowed() {
        setPlayerPermission("asedit.ignoreProtection.griefPrevention", true);
        setPlayerOp(false);

        GriefPreventionProtection protection = new GriefPreventionProtection();

        assertTrue(protection.checkPermission(armorStand, player));
    }

    @Test
    @DisplayName("ASE bypass false, ignoreclaims true, not op -> editing allowed")
        // GIVEN a player is in a world
        // WHEN the player right-clicks on an armor stand with the edit tool
        // AND they have asedit.ignoreProtection.griefPrevention set FALSE
        // AND they have griefprevention.ignoreclaims set TRUE
        // AND they are not op
        // THEN ASE editing should work
    void ignoreClaimsPermissionTrue_notOp_editingAllowed() {
        setPlayerPermission("asedit.ignoreProtection.griefPrevention", false);
        setPlayerPermission("griefprevention.ignoreclaims", true);
        setPlayerOp(false);

        GriefPreventionProtection protection = new GriefPreventionProtection();

        assertTrue(protection.checkPermission(armorStand, player));
    }

    @Test
    @DisplayName("Both permissions false, player is op -> editing allowed")
        // GIVEN a player is in a world
        // WHEN the player right-clicks on an armor stand with the edit tool
        // AND they have asedit.ignoreProtection.griefPrevention set FALSE
        // AND they have griefprevention.ignoreclaims set FALSE
        // AND they are op
        // THEN ASE editing should work
    void bothPermissionsFalse_isOp_editingAllowed() {
        setPlayerPermission("asedit.ignoreProtection.griefPrevention", false);
        setPlayerPermission("griefprevention.ignoreclaims", false);
        setPlayerOp(true);

        GriefPreventionProtection protection = new GriefPreventionProtection();

        assertTrue(protection.checkPermission(armorStand, player));
    }

    @Test
    @DisplayName("Claims disabled for world -> editing allowed")
        // GIVEN a player is in a world
        // WHEN the player right-clicks on an armor stand with the edit tool
        // AND GriefPrevention claims is not enabled for that world
        // THEN ASE editing should work
    void claimsNotEnabledForWorld_editingAllowed() {
        when(griefPrevention.claimsEnabledForWorld(world)).thenReturn(false);

        GriefPreventionProtection protection = new GriefPreventionProtection();

        assertTrue(protection.checkPermission(armorStand, player));
    }

    @Test
    @DisplayName("No bypass, not op, land unclaimed -> editing allowed")
        // GIVEN a player is in a world
        // WHEN the player right-clicks on an armor stand with the edit tool
        // AND they have asedit.ignoreProtection.griefPrevention set FALSE
        // AND they have griefprevention.ignoreclaims set FALSE
        // AND they are NOT op
        // AND the land they are in is unclaimed
        // THEN ASE editing should work
    void landUnclaimed_editingAllowed() {
        when(griefPrevention.claimsEnabledForWorld(world)).thenReturn(true);
        when(dataStore.getClaimAt(location, false, null)).thenReturn(null);

        GriefPreventionProtection protection = new GriefPreventionProtection();

        assertTrue(protection.checkPermission(armorStand, player));
    }

    @Test
    @DisplayName("No bypass, not op, land claimed, no edit permission -> editing blocked")
        // GIVEN a player is in a world
        // WHEN the player right-clicks on an armor stand with the edit tool
        // AND they have asedit.ignoreProtection.griefPrevention set FALSE
        // AND they have griefprevention.ignoreclaims set FALSE
        // AND they are NOT op
        // AND the land they are in is CLAIMED
        // AND they do not have permission to edit in that claim
        // THEN ASE editing should NOT work
    void landClaimed_noEditPermission_editingBlocked() {
        Claim claim = mock(Claim.class);
        Supplier<String> denial = () -> "You don't have permission to edit here.";
        when(griefPrevention.claimsEnabledForWorld(world)).thenReturn(true);
        when(dataStore.getClaimAt(location, false, null)).thenReturn(claim);
        when(claim.checkPermission(eq(player.getUniqueId()), eq(ClaimPermission.Edit), eq(null)))
                .thenReturn(denial);

        GriefPreventionProtection protection = new GriefPreventionProtection();

        assertFalse(protection.checkPermission(armorStand, player));
        verify(player).sendMessage(any(Component.class));
    }

    @Test
    @DisplayName("No bypass, not op, land claimed, has edit permission -> editing allowed")
        // GIVEN a player is in a world
        // WHEN the player right-clicks on an armor stand with the edit tool
        // AND they have asedit.ignoreProtection.griefPrevention set FALSE
        // AND they have griefprevention.ignoreclaims set FALSE
        // AND they are NOT op
        // AND the land they are in is CLAIMED
        // AND they have permission to edit in that claim
        // THEN ASE editing should work
    void landClaimed_hasEditPermission_editingAllowed() {
        Claim claim = mock(Claim.class);
        when(griefPrevention.claimsEnabledForWorld(world)).thenReturn(true);
        when(dataStore.getClaimAt(location, false, null)).thenReturn(claim);
        when(claim.checkPermission(eq(player.getUniqueId()), eq(ClaimPermission.Edit), eq(null)))
                .thenReturn(null);

        GriefPreventionProtection protection = new GriefPreventionProtection();

        assertTrue(protection.checkPermission(armorStand, player));
        verify(player, never()).sendMessage(any(Component.class));
    }
}