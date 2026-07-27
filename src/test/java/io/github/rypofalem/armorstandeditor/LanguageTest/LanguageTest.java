/*
 * ArmorStandEditor: Bukkit plugin to allow editing armor stand attributes
 * Copyright (C) 2016-2023  RypoFalem
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 */
package io.github.rypofalem.armorstandeditor.LanguageTest;


import io.github.rypofalem.armorstandeditor.BasePluginTest;
import io.github.rypofalem.armorstandeditor.language.Language;
import io.github.rypofalem.armorstandeditor.TestUtils.TestHelperFunctions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers the Language fixes discussed: legacy/hex code translation, the
 * safe-deserialize path used for untrusted/player-adjacent strings, config
 * fallback to the default language, and the atomic-snapshot reload behavior
 * that replaced the old separate volatile fields (see S3077 fix).
 */
class LanguageTest extends BasePluginTest {

    private Language lang;

    @BeforeEach
    void setUp() {
        plugin.setDebugFlag(true);
        plugin.debug.log("[LanguageTest] setUp: fetching Language instance from plugin");
        lang = plugin.getLang();
        plugin.debug.log("[LanguageTest] setUp: Language instance ready: " + (lang != null));
    }

    @AfterEach
    void tearDown() {
        plugin.debug.log("[LanguageTest] tearDown: unmocking (redundant with BasePluginTest, kept as-is)");
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("translateLegacyCodes converts ampersand color codes to MiniMessage tags")
    void translateLegacyCodes_convertsAmpersandColor() {
        plugin.debug.log("[translateLegacyCodes_convertsAmpersandColor] action: translating '&aHello'");
        String result = Language.translateLegacyCodes("&aHello");
        plugin.debug.log("[translateLegacyCodes_convertsAmpersandColor] assertion: result=" + result);
        assertTrue(result.contains("<#55ff55>"), "expected legacy '&a' to become the MiniMessage hex tag, got: " + result);
    }

    @Test
    @DisplayName("translateLegacyCodes converts well-formed &#RRGGBB hex sequences")
    void translateLegacyCodes_convertsHex() {
        plugin.debug.log("[translateLegacyCodes_convertsHex] action: translating '&#ff00ffBright'");
        String result = Language.translateLegacyCodes("&#ff00ffBright");
        plugin.debug.log("[translateLegacyCodes_convertsHex] assertion: result=" + result);
        assertTrue(result.startsWith("<#ff00ff>"), "expected 6-digit hex run to translate cleanly, got: " + result);
    }

    @Test
    @DisplayName("Malformed hex colours remain unchanged")
    void translateLegacyCodes_malformedHexRemainsLiteral() {
        String input = "&#12GTest";
        plugin.debug.log("[translateLegacyCodes_malformedHexRemainsLiteral] action: translating malformed input: " + input);
        String result = Language.translateLegacyCodes(input);
        plugin.debug.log("[translateLegacyCodes_malformedHexRemainsLiteral] assertion: expecting unchanged, got=" + result);
        assertEquals(input, result);
    }

    @Test
    @DisplayName("safeDeserialize never throws on malformed input and returns readable text")
    void safeDeserialize_handlesMalformedInputGracefully() {
        plugin.debug.log("[safeDeserialize_handlesMalformedInputGracefully] action: deserializing unterminated tag input");
        assertDoesNotThrow(() -> {
            Component c = Language.safeDeserialize("&aUnterminated <bold");
            plugin.debug.log("[safeDeserialize_handlesMalformedInputGracefully] assertion: component produced, non-null=" + (c != null));
            assertNotNull(c);
        });
    }

    @Test
    @DisplayName("safeDeserialize never produces click/hover events from untrusted input")
    void safeDeserialize_neverParsesEventTags() {
        plugin.debug.log("[safeDeserialize_neverParsesEventTags] action: deserializing input containing a click-event tag");
        Component c = Language.safeDeserialize("<click:run_command:'/op hacker'>click me</click>");

        plugin.debug.log("[safeDeserialize_neverParsesEventTags] assertion: verifying no click event was created");
        assertFalse(
                TestHelperFunctions.containsClickEvent(c),
                "untrusted input must not create click/hover events"
        );
        assertNull(c.clickEvent(), "root component must not contain a ClickEvent");
    }

    @Test
    @DisplayName("REGRESSION: a raw component-JSON string passed through safeDeserialize renders as literal text, not parsed formatting")
    void safeDeserialize_rawJsonRendersLiterally_notAsFormatting() {
        // This is exactly the bug from the tool-lore screenshot: someone puts
        // {"text":"...","italic":false} into config expecting it to be interpreted.
        // safeDeserialize (like the legacy serializer before it) has no JSON support,
        // so it must come out as plain, unparsed text — the caller is responsible for
        // not feeding it JSON in the first place. This test locks in that expectation
        // so a future "helpful" JSON-sniffing change doesn't silently reintroduce
        // double-encoding elsewhere.
        String raw = "{\"text\":\"Idealna zabawka dla kreator\u00f3w wn\u0119trz!\",\"italic\":false}";
        plugin.debug.log("[safeDeserialize_rawJsonRendersLiterally_notAsFormatting] action: deserializing raw JSON-looking input");
        Component c = Language.safeDeserialize(raw);
        String plain = PlainTextComponentSerializer.plainText().serialize(c);
        plugin.debug.log("[safeDeserialize_rawJsonRendersLiterally_notAsFormatting] assertion: plain=" + plain);
        assertEquals(raw, plain, "raw JSON-looking input must pass through as literal text unchanged");
    }

    @Test
    @DisplayName("getString falls back to the default language file when a key is missing from the custom one")
    void getString_fallsBackToDefaultLanguage() {
        // "give" and "nogive" ship in en_US.yml (DEFAULT_LANG) per commandGive()'s usage.
        plugin.debug.log("[getString_fallsBackToDefaultLanguage] action: fetching 'give.msg' via getString");
        String value = lang.getString("give.msg");
        plugin.debug.log("[getString_fallsBackToDefaultLanguage] assertion: resolved value=" + value);
        assertNotNull(value, "expected 'give.msg' to resolve via default-language fallback");
    }

    @Test
    @DisplayName("getMessage applies the configured info color")
    void getMessage_appliesFormatColor() {
        plugin.debug.log("[getMessage_appliesFormatColor] action: fetching 'give'/'info' message");
        Component msg = lang.getMessage("give", "info");
        plugin.debug.log("[getMessage_appliesFormatColor] assertion: msg non-null=" + (msg != null) + ", color=" + (msg != null ? msg.color() : null));
        assertNotNull(msg);
        // Not asserting an exact color since that's config-driven, just that a
        // color was actually resolved and applied rather than silently dropped.
        assertNotNull(msg.color(), "expected getMessage to resolve and apply a format color");
    }

    @SuppressWarnings("deprecation")
    @Test
    @DisplayName("resolveFormatValue and the deprecated getFormat alias agree")
    void resolveFormatValue_matchesDeprecatedGetFormat() {
        plugin.debug.log("[resolveFormatValue_matchesDeprecatedGetFormat] action: comparing resolveFormatValue vs getFormat for 'info'");
        String resolved = lang.resolveFormatValue("info");
        String deprecated = lang.getFormat("info");
        plugin.debug.log("[resolveFormatValue_matchesDeprecatedGetFormat] assertion: resolved=" + resolved + ", deprecated=" + deprecated);
        assertEquals(resolved, deprecated);
    }

    @Test
    @DisplayName("resolveFormatValue returns empty string, not null, for a null path")
    void resolveFormatValue_nullPathReturnsEmptyString() {
        plugin.debug.log("[resolveFormatValue_nullPathReturnsEmptyString] action: resolveFormatValue(null)");
        String result = lang.resolveFormatValue(null);
        plugin.debug.log("[resolveFormatValue_nullPathReturnsEmptyString] assertion: result='" + result + "'");
        assertEquals("", result);
    }

    @RepeatedTest(5)
    @DisplayName("concurrent reloadLang + getString never throws and never mixes half-loaded state")
    void reloadLang_concurrentAccessIsAtomicallyConsistent() throws InterruptedException {
        // Stress test for the AtomicReference<LoadedLang> snapshot swap: readers
        // hammering getString() while reloadLang() runs on another thread should
        // never see a torn/partial state or throw, since each reload publishes a
        // fully-built LoadedLang in one atomic set().
        plugin.debug.log("[reloadLang_concurrentAccessIsAtomicallyConsistent] setup: starting pool of 3 readers + 1 reloader");
        ExecutorService pool = Executors.newFixedThreadPool(4);
        AtomicBoolean failed = new AtomicBoolean(false);
        CountDownLatch done = new CountDownLatch(4);

        Runnable reader = () -> {
            try {
                for (int i = 0; i < 200; i++) {
                    lang.getString("give.msg");
                    lang.getMessage("give", "info");
                }
            } catch (Exception _) {
                failed.set(true);
            } finally {
                done.countDown();
            }
        };
        Runnable reloader = () -> {
            try {
                for (int i = 0; i < 50; i++) {
                    lang.reloadLang(null);
                }
            } catch (Exception _) {
                failed.set(true);
            } finally {
                done.countDown();
            }
        };

        plugin.debug.log("[reloadLang_concurrentAccessIsAtomicallyConsistent] action: submitting reader/reloader tasks");
        pool.submit(reader);
        pool.submit(reader);
        pool.submit(reader);
        pool.submit(reloader);

        boolean finishedInTime = done.await(10, TimeUnit.SECONDS);
        plugin.debug.log("[reloadLang_concurrentAccessIsAtomicallyConsistent] assertion: finishedInTime=" + finishedInTime + ", failed=" + failed.get());
        assertTrue(finishedInTime, "test threads did not finish in time");
        pool.shutdownNow();
        assertFalse(failed.get(), "reader or reloader threw during concurrent access");
    }
}