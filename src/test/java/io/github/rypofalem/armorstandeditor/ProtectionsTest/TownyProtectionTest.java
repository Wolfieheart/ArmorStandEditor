package io.github.rypofalem.armorstandeditor.ProtectionsTest;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.executors.TownyActionEventExecutor;

import io.github.rypofalem.armorstandeditor.BaseProtectionTest;
import io.github.rypofalem.armorstandeditor.protections.TownyProtection;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.ArmorStand;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.MockedStatic;

import static io.github.rypofalem.armorstandeditor.TestUtils.TestHelperFunctions.*;
import static org.bukkit.Material.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TownyProtectionTest extends BaseProtectionTest {

    private MockedStatic<TownyAPI> townyApiMock;
    private MockedStatic<TownyActionEventExecutor> townyExecutorMock;
    private TownyAPI towny;

    @BeforeEach
    void beforeSetup() {
        towny = mock(TownyAPI.class);
        townyApiMock = mockStatic(TownyAPI.class);
        townyApiMock.when(TownyAPI::getInstance).thenReturn(towny);

        townyExecutorMock = mockStatic(TownyActionEventExecutor.class);
    }

    @AfterEach
    void baseTearDown() {
        if (townyApiMock != null) townyApiMock.close();
        if (townyExecutorMock != null) townyExecutorMock.close();
    }

    @Test
    @DisplayName("When Towny is not enabled, all edits are allowed")
    void townyNotEnabled_alwaysAllows() {
        setPluginEnabled("Towny", false);

        assertTrue(new TownyProtection().checkPermission(mockArmorStand(location), player));
    }

    @Test
    @DisplayName("When the player is an operator, all edits are allowed")
    void playerIsOp_alwaysAllowed() {
        setPluginEnabled("Towny", true);
        setPlayerOp(true);

        assertTrue(new TownyProtection().checkPermission(mockArmorStand(location), player));
    }

    @Test
    @DisplayName("When the player has the Towny bypass permission, all edits are allowed")
    void playerHasBypassPermission_allowed() {
        setPluginEnabled("Towny", true);
        setPlayerPermission("asedit.ignoreProtection.towny", true);

        assertTrue(new TownyProtection().checkPermission(mockArmorStand(location), player));
    }

    @Test
    @DisplayName("When in the wilderness without the wilderness-edit permission, the edit is denied and the player is messaged")
    void wildernessWithoutPermission_deniedAndMessaged() {
        setPluginEnabled("Towny", true);
        when(towny.isWilderness(location)).thenReturn(true);
        setPlayerPermission("asedit.townyProtection.canEditInWild", false);

        Component expectedMessage = Component.text("You cannot edit in the wilderness!");
        when(lang.getMessage("townyNoWildEdit", "warn")).thenReturn(expectedMessage);

        assertFalse(new TownyProtection().checkPermission(mockArmorStand(location), player));
        verify(player).sendMessage(expectedMessage);
    }

    @Test
    @DisplayName("When in the wilderness with the wilderness-edit permission, the edit is allowed")
    void wildernessWithPermission_allowed() {
        setPluginEnabled("Towny", true);
        when(towny.isWilderness(location)).thenReturn(true);
        setPlayerPermission("asedit.townyProtection.canEditInWild", true);

        assertTrue(new TownyProtection().checkPermission(mockArmorStand(location), player));
    }

    @Test
    @DisplayName("When not in the wilderness, an ArmorStand edit is delegated to TownyActionEventExecutor with ARMOR_STAND")
    void notWilderness_armorStand_delegatesToTownyActionEventExecutor() {
        setPluginEnabled("Towny", true);
        when(towny.isWilderness(location)).thenReturn(false);

        ArmorStand stand = mockArmorStand(location);
        townyExecutorMock.when(() ->
                TownyActionEventExecutor.canBuild(eq(player), eq(location), eq(ARMOR_STAND))
        ).thenReturn(true);

        assertTrue(new TownyProtection().checkPermission(stand, player));
    }

    @Test
    @DisplayName("When not in the wilderness, a GlowItemFrame edit is checked against GLOW_ITEM_FRAME material")
    void notWilderness_glowItemFrame_usesCorrectMaterial() {
        setPluginEnabled("Towny", true);
        when(towny.isWilderness(location)).thenReturn(false);

        townyExecutorMock.when(() ->
                TownyActionEventExecutor.canBuild(eq(player), eq(location), eq(GLOW_ITEM_FRAME))
        ).thenReturn(false);

        assertFalse(new TownyProtection().checkPermission(mockGlowItemFrame(location), player));
    }

    @Test
    @DisplayName("When not in the wilderness, a plain ItemFrame edit is checked against ITEM_FRAME material")
    void notWilderness_itemFrame_usesCorrectMaterial() {
        setPluginEnabled("Towny", true);
        when(towny.isWilderness(location)).thenReturn(false);

        townyExecutorMock.when(() ->
                TownyActionEventExecutor.canBuild(eq(player), eq(location), eq(ITEM_FRAME))
        ).thenReturn(true);

        assertTrue(new TownyProtection().checkPermission(mockItemFrame(location), player));
    }

    @Test
    @DisplayName("When the entity is not an ArmorStand or ItemFrame, the edit is allowed without checking Towny")
    void unsupportedEntityType_allowed() {
        setPluginEnabled("Towny", true);

        org.bukkit.entity.Entity zombie = mock(org.bukkit.entity.Zombie.class);

        assertTrue(new TownyProtection().checkPermission(zombie, player));
    }
}