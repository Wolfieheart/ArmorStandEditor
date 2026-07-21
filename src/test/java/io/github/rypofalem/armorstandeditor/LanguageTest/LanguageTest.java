package io.github.rypofalem.armorstandeditor.LanguageTest;

import io.github.rypofalem.armorstandeditor.BasePluginTest;
import io.github.rypofalem.armorstandeditor.language.Language;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockbukkit.mockbukkit.MockBukkit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LanguageTest extends BasePluginTest {

    private Language language;
    private Path langDir;

    @BeforeEach
    void setUp() throws IOException {
        langDir = plugin.getDataFolder().toPath().resolve("lang");
        Files.createDirectories(langDir);
        Path lang = langDir.resolve("test.yml");

        Files.writeString(
                lang,
                """
                test:
                  msg: "&aHello"

                legacy_hex:
                  msg: "&#fff000Test"

                legacy_bold:
                  msg: "&lBold"

                legacy_combined:
                  msg: "&#fff000&lTest"

                minimessage:
                  msg: "<#fff000><bold>Test"

                mixed:
                  msg: "&aHello <red>World"

                placeholder:
                  msg: "Hello <x>"
                  name: "&bSteve"

                section:
                  msg: "§aHello"

                mixed_section:
                  msg: "§aHello &lWorld"

                invalid_hex:
                  msg: "&#GGGGGGHello"

                invalid_minimessage:
                  msg: "<notatag>Hello"

                multi_decor:
                  msg: "&l&nBold Underline"

                info: "#ff0000"
                
                
                """
        );

        language = new Language("test.yml", plugin);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }


    @Test
    @DisplayName("Loads test.yml from the plugin data folder instead of the bundled default")
    void loadsCustomLangFile() {
        assertEquals("&aHello", language.getString("test.msg"));
    }

    @Test
    @DisplayName("Legacy ampersand color code renders as the matching color")
    void legacyAmpersandColorRenders() {
        Component msg = language.getMessage("test");
        assertEquals("Hello", PlainTextComponentSerializer.plainText().serialize(msg));
    }

    @Test
    @DisplayName("Legacy hex code from test.yml renders as plain colored text")
    void legacyHexRenders() {
        Component msg = language.getMessage("legacy_hex");
        assertEquals("Test", PlainTextComponentSerializer.plainText().serialize(msg));
    }

    @Test
    @DisplayName("Legacy bold code from test.yml applies the bold decoration")
    void legacyBoldRenders() {
        Component msg = language.getMessage("legacy_bold");
        assertEquals("Bold", PlainTextComponentSerializer.plainText().serialize(msg));
        assertTrue(msg.hasDecoration(TextDecoration.BOLD));
    }

    @Test
    @DisplayName("Combined legacy hex+bold code from test.yml applies both color and bold")
    void legacyCombinedRenders() {
        Component msg = language.getMessage("legacy_combined");
        assertEquals("Test", PlainTextComponentSerializer.plainText().serialize(msg));
        assertTrue(msg.hasDecoration(TextDecoration.BOLD));
    }

    @Test
    @DisplayName("Null path returns an empty component")
    void nullPathReturnsEmpty() {
        assertEquals(Component.empty(), language.getMessage(null));
    }

    @Test
    @DisplayName("Pure MiniMessage entry from test.yml renders identically to the legacy_combined entry")
    void miniMessageMatchesLegacyEquivalent() {
        Component legacy = language.getMessage("legacy_combined");
        Component mini = language.getMessage("minimessage");
        assertEquals(legacy, mini);
    }

    @Test
    @DisplayName("A single message mixing legacy and MiniMessage tags resolves both")
    void mixedLegacyAndMiniMessageTagsResolve() {
        Component msg = language.getMessage("mixed");
        assertEquals("Hello World", PlainTextComponentSerializer.plainText().serialize(msg));
    }

    @Test
    @DisplayName("Placeholder option value is resolved from test.yml and safely formatted")
    void placeholderOptionResolvesAndFormats() {
        Component msg = language.getMessage("placeholder", "info", "name");
        assertEquals("Hello Steve", PlainTextComponentSerializer.plainText().serialize(msg));
    }

    @Test
    @DisplayName("Placeholder option falls back to the raw literal when no yml key matches")
    void placeholderOptionFallsBackToLiteral() {
        Component msg = language.getMessage("placeholder", "info", "&cRaw");
        assertEquals("Hello Raw", PlainTextComponentSerializer.plainText().serialize(msg));
    }

    @Test
    @DisplayName("Info format entry in test.yml resolves to its hex color")
    void infoFormatResolvesHexColor() {
        assertEquals("#ff0000", language.getFormat("info"));
    }

    @Test
    @DisplayName("Unknown path returns an empty component rather than throwing")
    void unknownPathReturnsEmpty() {
        Component msg = language.getMessage("does_not_exist");
        assertEquals(Component.empty(), msg);
    }

    @Test
    @DisplayName("Unknown format leaves message unchanged")
    void unknownFormatIgnored() {
        Component msg = language.getMessage("test", "does_not_exist");
        assertEquals("Hello", PlainTextComponentSerializer.plainText().serialize(msg));
    }

    @Test
    @DisplayName("Empty message returns an empty component")
    void emptyMessageReturnsEmptyComponent() {
        assertEquals(Component.empty(), language.getMessage("empty"));
    }

    @Test
    @DisplayName("Missing msg node returns empty component")
    void missingMsgReturnsEmpty() {
        assertEquals(Component.empty(), language.getMessage("broken"));
    }

    @Test
    @DisplayName("Section sign legacy colour codes are supported")
    void legacySectionSignRenders() {
        Component msg = language.getMessage("section");
        assertEquals("Hello", PlainTextComponentSerializer.plainText().serialize(msg));
    }

    @Test
    @DisplayName("Section sign and ampersand formatting may be mixed")
    void mixedSectionAndAmpersandRenders() {
        Component msg = language.getMessage("mixed_section");

        assertEquals(
                "Hello World",
                PlainTextComponentSerializer.plainText().serialize(msg)
        );
    }

    @Test
    @DisplayName("Invalid legacy hex does not throw")
    void invalidLegacyHexDoesNotThrow() {
        assertDoesNotThrow(() -> language.getMessage("invalid_hex"));
    }

    @Test
    @DisplayName("Invalid MiniMessage does not throw")
    void invalidMiniMessageDoesNotThrow() {
        assertDoesNotThrow(() -> language.getMessage("invalid_minimessage"));
    }

    @Test
    @DisplayName("Multiple legacy decorations are applied")
    void multipleDecorationsRender() {
        Component msg = language.getMessage("multi_decor");

        assertEquals(
                "Bold Underline",
                PlainTextComponentSerializer.plainText().serialize(msg)
        );

        assertTrue(msg.hasDecoration(TextDecoration.BOLD));
        assertTrue(msg.hasDecoration(TextDecoration.UNDERLINED));
    }

    @Test
    @DisplayName("Null placeholder is handled safely")
    void nullPlaceholderHandled() {
        Component msg = language.getMessage("placeholder", "info", null);
        assertEquals("Hello ", PlainTextComponentSerializer.plainText().serialize(msg));
    }

    @Test
    @DisplayName("Empty placeholder is handled safely")
    void emptyPlaceholderHandled() {
        Component msg = language.getMessage("placeholder", "info", "");
        assertEquals("Hello ", PlainTextComponentSerializer.plainText().serialize(msg));
    }

    @Test
    @DisplayName("Unknown format returns an empty string")
    void unknownFormatReturnsEmptyString() {
        assertEquals("", language.getFormat("does_not_exist"));
    }

    @Test
    @DisplayName("Unknown string path returns null")
    void unknownStringReturnsNull() {
        assertNull(language.getString("does_not_exist"));
    }


}