package io.github.rypofalem.armorstandeditor.protections;

import io.github.rypofalem.armorstandeditor.BaseProtectionTest;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Extends {@link BaseProtectionTest} for the shared Bukkit/ASE plugin scaffolding
 * (loads ASE via the mocked {@code ArmorStandEditorPlugin.instance()}, plus baseline
 * player/block/location/world mocks). Only the GriefPrevention-specific API is mocked
 * here, since GriefPrevention exposes itself through the static
 * {@link GriefPrevention#instance} field rather than an injectable service.
 */
@SuppressWarnings("deprecation") // Claim#allowEdit / Claim#allowBuild are deprecated upstream but still used by GriefPreventionProtection
class GriefPreventionProtectionTest extends BaseProtectionTest {

    private GriefPrevention griefPrevention;
    private DataStore dataStore;

    // GriefPrevention.instance is a plain public static field, not a method,
    // so it can't be stubbed via MockedStatic (that only intercepts static
    // method calls). We assign it directly and restore the original value
    // afterward so tests don't leak state into each other.
    private GriefPrevention originalInstance;

    @BeforeEach
    void setUp() {
        griefPrevention = mock(GriefPrevention.class);
        dataStore = mock(DataStore.class);

        setPluginEnabled("GriefPrevention", true);
        when(pluginManager.getPlugin("GriefPrevention")).thenReturn(griefPrevention);

        griefPrevention.dataStore = dataStore;
        originalInstance = GriefPrevention.instance;
        GriefPrevention.instance = griefPrevention;

        when(block.getType()).thenReturn(Material.STONE);
        setPlayerPermission("asedit.ignoreProtection.griefPrevention", false);
    }

    @AfterEach
    void tearDown() {
        GriefPrevention.instance = originalInstance;
    }

    @Test
    @DisplayName("Permission check passes when GriefPrevention is not enabled")
    void checkPermission_pluginDisabled_returnsTrue() {
        setPluginEnabled("GriefPrevention", false);

        GriefPreventionProtection protection = new GriefPreventionProtection();

        assertTrue(protection.checkPermission(block, player));
    }

    @Test
    @DisplayName("Permission check passes when player has the bypass permission")
    void checkPermission_playerHasBypassPermission_returnsTrue() {
        setPlayerPermission("asedit.ignoreProtection.griefPrevention", true);

        GriefPreventionProtection protection = new GriefPreventionProtection();

        assertTrue(protection.checkPermission(block, player));
    }

    @Test
    @DisplayName("Permission check passes when claims are disabled for the world")
    void checkPermission_claimsDisabledForWorld_returnsTrue() {
        when(griefPrevention.claimsEnabledForWorld(world)).thenReturn(false);

        GriefPreventionProtection protection = new GriefPreventionProtection();

        assertTrue(protection.checkPermission(block, player));
    }

    @Test
    @DisplayName("Permission check passes when the block is outside any claim")
    void checkPermission_noClaimAtLocation_returnsTrue() {
        when(griefPrevention.claimsEnabledForWorld(world)).thenReturn(true);
        when(dataStore.getClaimAt(location, false, null)).thenReturn(null);

        GriefPreventionProtection protection = new GriefPreventionProtection();

        assertTrue(protection.checkPermission(block, player));
    }

    @Test
    @DisplayName("Permission check passes when claim allows both edit and build")
    void checkPermission_claimAllowsEditAndBuild_returnsTrue() {
        Claim claim = mock(Claim.class);
        when(griefPrevention.claimsEnabledForWorld(world)).thenReturn(true);
        when(dataStore.getClaimAt(location, false, null)).thenReturn(claim);
        when(claim.allowEdit(player)).thenReturn(null);
        when(claim.allowBuild(player, Material.STONE)).thenReturn(null);

        GriefPreventionProtection protection = new GriefPreventionProtection();

        assertTrue(protection.checkPermission(block, player));
    }

    @Test
    @DisplayName("Permission check fails when claim denies both edit and build")
    void checkPermission_claimDeniesEditAndBuild_returnsFalse() {
        Claim claim = mock(Claim.class);
        when(griefPrevention.claimsEnabledForWorld(world)).thenReturn(true);
        when(dataStore.getClaimAt(location, false, null)).thenReturn(claim);
        when(claim.allowEdit(player)).thenReturn("You don't have permission to edit here.");
        when(claim.allowBuild(player, Material.STONE)).thenReturn("You don't have permission to build here.");

        GriefPreventionProtection protection = new GriefPreventionProtection();

        assertFalse(protection.checkPermission(block, player));
        verify(player, Mockito.atLeastOnce()).sendMessage(any(String.class));
    }

    @Test
    @DisplayName("Permission check passes when claim denies edit but allows build (current AND-bug behavior)")
    void checkPermission_claimDeniesEditOnly_returnsTrue() {
        // NOTE: this documents the existing `&&` short-circuit in checkPermission —
        // both allowEdit() and allowBuild() must return non-null for the denial
        // branch to trigger, so a partial denial currently falls through to `true`.
        Claim claim = mock(Claim.class);
        when(griefPrevention.claimsEnabledForWorld(world)).thenReturn(true);
        when(dataStore.getClaimAt(location, false, null)).thenReturn(claim);
        when(claim.allowEdit(player)).thenReturn("You don't have permission to edit here.");
        when(claim.allowBuild(player, Material.STONE)).thenReturn(null);

        GriefPreventionProtection protection = new GriefPreventionProtection();

        assertTrue(protection.checkPermission(block, player));
    }
}