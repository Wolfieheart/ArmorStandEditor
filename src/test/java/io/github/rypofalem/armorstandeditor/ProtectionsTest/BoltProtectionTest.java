package io.github.rypofalem.armorstandeditor.ProtectionsTest;
import io.github.rypofalem.armorstandeditor.BaseProtectionTest;

import io.github.rypofalem.armorstandeditor.protections.BoltProtection;
import org.bukkit.entity.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.popcraft.bolt.BoltAPI;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Extends {@link BaseProtectionTest} for the shared Bukkit/ASE plugin scaffolding.
 * {@code BoltProtection} looks up its API via
 * {@code Bukkit.getServer().getServicesManager().load(BoltAPI.class)}, which the
 * base class exposes through {@code server}/{@code servicesManager}.
 *
 * Note: {@code BoltProtection} constructs its own {@code Debug} instance directly
 * (rather than reading {@code plugin.debug}), so no extra debug wiring is needed here.
 */
class BoltProtectionTest extends BaseProtectionTest {

    private Entity entity;
    private BoltAPI boltAPI;

    @BeforeEach
    void setUp() {
        entity = mock(Entity.class);
        boltAPI = mock(BoltAPI.class);

        setPluginEnabled("Bolt", true);
        when(servicesManager.load(BoltAPI.class)).thenReturn(boltAPI);

        setPlayerPermission("asedit.ignoreProtection.bolt", false);
        setPlayerOp(false);
    }

    @Test
    @DisplayName("Bolt plugin not enabled -> edits go through")
        // GIVEN the plugin isn't enabled
        // WHEN checking against the protection
        // THEN edits go through
    void pluginNotEnabled_editsGoThrough() {
        setPluginEnabled("Bolt", false);

        BoltProtection protection = new BoltProtection();

        assertTrue(protection.checkPermission(entity, player));
    }

    @Test
    @DisplayName("Player is op -> edits go through")
        // GIVEN the player is OP
        // WHEN checking against the protection
        // THEN edits go through
    void playerIsOp_editsGoThrough() {
        setPlayerOp(true);

        BoltProtection protection = new BoltProtection();

        assertTrue(protection.checkPermission(entity, player));
    }

    @Test
    @DisplayName("Player has asedit.ignoreProtection.bolt -> edits go through")
        // GIVEN the player has asedit.ignoreProtection.bolt = TRUE
        // WHEN checking against the protection
        // THEN edits go through
    void playerHasBoltBypassPermission_editsGoThrough() {
        setPlayerPermission("asedit.ignoreProtection.bolt", true);

        BoltProtection protection = new BoltProtection();

        assertTrue(protection.checkPermission(entity, player));
    }

    @Test
    @DisplayName("Plugin enabled but BoltAPI can't be retrieved -> edits go through")
        // GIVEN the plugin is enabled
        // WHEN the API can not be retrieved
        // THEN edits go through
    void apiNotRetrievable_editsGoThrough() {
        when(servicesManager.load(BoltAPI.class)).thenReturn(null);

        BoltProtection protection = new BoltProtection();

        assertTrue(protection.checkPermission(entity, player));
    }

    @Test
    @DisplayName("Entity protected, player denied interact -> edits blocked")
        // GIVEN the plugin is enabled
        // WHEN the entity is marked protected by Bolt
        // THEN edits do NOT go through
    void entityProtected_editsBlocked() {
        when(boltAPI.isProtected(entity)).thenReturn(true);
        when(boltAPI.canAccess(entity, player, "interact")).thenReturn(false);

        BoltProtection protection = new BoltProtection();

        assertFalse(protection.checkPermission(entity, player));
    }

    @Test
    @DisplayName("Entity not protected -> edits go through")
        // GIVEN the plugin is enabled
        // WHEN the entity is NOT marked protected by Bolt
        // THEN edits do go through
    void entityNotProtected_editsGoThrough() {
        when(boltAPI.isProtected(entity)).thenReturn(false);

        BoltProtection protection = new BoltProtection();

        assertTrue(protection.checkPermission(entity, player));
    }

    @Test
    @DisplayName("Entity protected, player can interact -> edits go through")
        // GIVEN the plugin is enabled
        // WHEN the player has the ability to interact with the entity in question
        // THEN edits do go through
    void playerCanInteract_editsGoThrough() {
        when(boltAPI.isProtected(entity)).thenReturn(true);
        when(boltAPI.canAccess(entity, player, "interact")).thenReturn(true);

        BoltProtection protection = new BoltProtection();

        assertTrue(protection.checkPermission(entity, player));
    }

    @Test
    @DisplayName("Entity protected, player can NOT interact -> edits blocked")
        // GIVEN the plugin is enabled
        // WHEN the player does not have the ability to interact with the entity in question
        // THEN edits do NOT go through
        //
        // NOTE: the scenario as written says "Edits do go through" here, but checkPermission()
        // returns boltAPI.canAccess(...) directly, so a denied canAccess() blocks the edit.
        // This test asserts the actual current behavior of the code above -- flag if the
        // intent was actually different.
    void playerCannotInteract_editsBlocked() {
        when(boltAPI.isProtected(entity)).thenReturn(true);
        when(boltAPI.canAccess(entity, player, "interact")).thenReturn(false);

        BoltProtection protection = new BoltProtection();

        assertFalse(protection.checkPermission(entity, player));
    }
}
