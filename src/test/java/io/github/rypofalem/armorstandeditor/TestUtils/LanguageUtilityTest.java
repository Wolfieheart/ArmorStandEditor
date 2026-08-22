package io.github.rypofalem.armorstandeditor.TestUtils;

import io.github.rypofalem.armorstandeditor.language.Language;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LanguageUtilityTest {

    @Test
    @DisplayName("Legacy ampersand colour converts to MiniMessage")
    void translateAmpersandColour() {
        assertEquals(
                "<#55ff55>Hello",
                Language.translateLegacyCodes("&aHello"));
    }

    @Test
    @DisplayName("Legacy hex converts to MiniMessage")
    void translateLegacyHex() {
        assertEquals(
                "<#fff000>Test",
                Language.translateLegacyCodes("&#fff000Test"));
    }

    @Test
    @DisplayName("Legacy hex and bold convert to MiniMessage")
    void translateLegacyHexBold() {
        assertEquals(
                "<#fff000><bold>Test",
                Language.translateLegacyCodes("&#fff000&lTest"));
    }

    @Test
    @DisplayName("Existing MiniMessage is unchanged")
    void translateAlreadyMiniMessage() {
        assertEquals(
                "<#fff000><bold>Test",
                Language.translateLegacyCodes("<#fff000><bold>Test"));
    }

    @Test
    @DisplayName("Translation is idempotent")
    void translateIsIdempotent() {
        String once = Language.translateLegacyCodes("&aHello");
        String twice = Language.translateLegacyCodes(once);

        assertEquals(once, twice);
    }

    @Test
    @DisplayName("Safe deserialize accepts legacy formatting")
    void safeDeserializeLegacy() {
        assertDoesNotThrow(() -> Language.safeDeserialize("&aHello"));
    }

    @Test
    @DisplayName("Safe deserialize accepts invalid MiniMessage safely")
    void safeDeserializeInvalidMiniMessage() {
        assertDoesNotThrow(() -> Language.safeDeserialize("<notatag>Hello"));
    }

    @Test
    @DisplayName("Safe deserialize handles null input")
    void safeDeserializeNull() {
        assertEquals(
                net.kyori.adventure.text.Component.empty(),
                Language.safeDeserialize(null)
        );
    }

    @Test
    @DisplayName("Safe deserialize handles empty input")
    void safeDeserializeEmpty() {
        assertEquals(
                net.kyori.adventure.text.Component.empty(),
                Language.safeDeserialize("")
        );
    }
}