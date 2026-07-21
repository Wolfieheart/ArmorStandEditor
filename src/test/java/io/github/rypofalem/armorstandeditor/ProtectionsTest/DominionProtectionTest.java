package io.github.rypofalem.armorstandeditor.ProtectionsTest;

import cn.lunadeer.dominion.api.DominionAPI;
import cn.lunadeer.dominion.api.dtos.flag.Flags;

import io.github.rypofalem.armorstandeditor.BaseProtectionTest;
import io.github.rypofalem.armorstandeditor.protections.DominionProtection;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.ArmorStand;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.MockedStatic;

import static io.github.rypofalem.armorstandeditor.TestUtils.TestHelperFunctions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class DominionProtectionTest extends BaseProtectionTest {

    private MockedStatic<DominionAPI> dominionApiMock;
    private DominionAPI dominion;

    DominionProtection protection;

    @BeforeEach
    void beforeSetup() {
        protection = new DominionProtection();

        dominion = mock(DominionAPI.class);
        dominionApiMock = mockStatic(DominionAPI.class);
        dominionApiMock.when(DominionAPI::getInstance).thenReturn(dominion);
    }

    @AfterEach
    void baseTearDown() {
        if (dominionApiMock != null) dominionApiMock.close();
    }

    @Test
    @DisplayName("When Dominion is not enabled, all edits are allowed")
    void dominionNotEnabled_alwaysAllows() {
        setPluginEnabled("Dominion", false);

        assertTrue(protection.checkPermission(mockArmorStand(location), player));
        verifyNoInteractions(dominion);
    }

    @Test
    @DisplayName("When the player is an operator, all edits are allowed")
    void playerIsOp_alwaysAllowed() {
        setPluginEnabled("Dominion", true);
        setPlayerOp(true);

        assertTrue(protection.checkPermission(mockArmorStand(location), player));
        verifyNoInteractions(dominion);
    }

    @Test
    @DisplayName("When the player has the Dominion bypass permission, all edits are allowed")
    void playerHasBypassPermission_allowed() {
        setPluginEnabled("Dominion", true);
        setPlayerPermission("asedit.ignoreProtection.dominion", true);

        assertTrue(protection.checkPermission(mockArmorStand(location), player));
        verifyNoInteractions(dominion);
    }

    @Test
    @DisplayName("An ArmorStand edit is checked against the PLACE privilege flag and allowed when granted")
    void armorStand_placeFlagGranted_allowed() {
        setPluginEnabled("Dominion", true);

        ArmorStand stand = mockArmorStand(location);
        
        when(dominion.checkPrivilegeFlagSilence(location, Flags.PLACE, player)).thenReturn(true);
        assertTrue(protection.checkPermission(stand, player));
    }

    @Test
    @DisplayName("An ArmorStand edit is denied and the player is messaged when the PLACE flag is not granted")
    void armorStand_placeFlagDenied_deniedAndMessaged() {
        setPluginEnabled("Dominion", true);

        ArmorStand stand = mockArmorStand(location);
        when(dominion.checkPrivilegeFlagSilence(location, Flags.PLACE, player)).thenReturn(false);

        Component expectedMessage = Component.text("You cannot edit here!");
        when(lang.getMessage("dominionNoEdit", "warn")).thenReturn(expectedMessage);

        assertFalse(protection.checkPermission(stand, player));
        verify(player).sendMessage(expectedMessage);
    }

    @Test
    @DisplayName("A GlowItemFrame edit is checked against the ITEM_FRAME_INTERACTIVE flag")
    void glowItemFrame_usesItemFrameInteractiveFlag() {
        setPluginEnabled("Dominion", true);

        when(dominion.checkPrivilegeFlagSilence(location, Flags.ITEM_FRAME_INTERACTIVE, player)).thenReturn(false);
        assertFalse(protection.checkPermission(mockGlowItemFrame(location), player));
    }

    @Test
    @DisplayName("A plain ItemFrame edit is checked against the ITEM_FRAME_INTERACTIVE flag and allowed when granted")
    void itemFrame_flagGranted_allowed() {
        setPluginEnabled("Dominion", true);

        when(dominion.checkPrivilegeFlagSilence(location, Flags.ITEM_FRAME_INTERACTIVE, player)).thenReturn(true);
        assertTrue(protection.checkPermission(mockItemFrame(location), player));
    }

    @Test
    @DisplayName("When the entity is not an ArmorStand or ItemFrame, the edit is allowed without checking Dominion")
    void unsupportedEntityType_allowed() {
        setPluginEnabled("Dominion", true);

        org.bukkit.entity.Entity zombie = mock(org.bukkit.entity.Zombie.class);

        assertTrue(protection.checkPermission(zombie, player));
        verifyNoInteractions(dominion);
    }
}