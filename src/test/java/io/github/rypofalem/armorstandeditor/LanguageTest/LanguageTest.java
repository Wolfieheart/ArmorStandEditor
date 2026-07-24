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
        lang = plugin.getLang();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("translateLegacyCodes converts ampersand color codes to MiniMessage tags")
    void translateLegacyCodes_convertsAmpersandColor() {
        String result = Language.translateLegacyCodes("&aHello");
        assertTrue(result.contains("<#55ff55>"), "expected legacy '&a' to become the MiniMessage hex tag, got: " + result);
    }

    @Test
    @DisplayName("translateLegacyCodes converts well-formed &#RRGGBB hex sequences")
    void translateLegacyCodes_convertsHex() {
        String result = Language.translateLegacyCodes("&#ff00ffBright");
        assertTrue(result.startsWith("<#ff00ff>"), "expected 6-digit hex run to translate cleanly, got: " + result);
    }

    @Test
    @DisplayName("Malformed hex colours remain unchanged")
    void translateLegacyCodes_malformedHexRemainsLiteral() {
        String input = "&#12GTest";
        assertEquals(input, Language.translateLegacyCodes(input));
    }

    @Test
    @DisplayName("safeDeserialize never throws on malformed input and returns readable text")
    void safeDeserialize_handlesMalformedInputGracefully() {
        assertDoesNotThrow(() -> {
            Component c = Language.safeDeserialize("&aUnterminated <bold");
            assertNotNull(c);
        });
    }

    @Test
    @DisplayName("safeDeserialize never produces click/hover events from untrusted input")
    void safeDeserialize_neverParsesEventTags() {
        Component c = Language.safeDeserialize("<click:run_command:'/op hacker'>click me</click>");

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
        Component c = Language.safeDeserialize(raw);
        String plain = PlainTextComponentSerializer.plainText().serialize(c);
        assertEquals(raw, plain, "raw JSON-looking input must pass through as literal text unchanged");
    }

    @Test
    @DisplayName("getString falls back to the default language file when a key is missing from the custom one")
    void getString_fallsBackToDefaultLanguage() {
        // "give" and "nogive" ship in en_US.yml (DEFAULT_LANG) per commandGive()'s usage.
        String value = lang.getString("give.msg");
        assertNotNull(value, "expected 'give.msg' to resolve via default-language fallback");
    }

    @Test
    @DisplayName("getMessage applies the configured info color")
    void getMessage_appliesFormatColor() {
        Component msg = lang.getMessage("give", "info");
        assertNotNull(msg);
        // Not asserting an exact color since that's config-driven, just that a
        // color was actually resolved and applied rather than silently dropped.
        assertNotNull(msg.color(), "expected getMessage to resolve and apply a format color");
    }

    @Test
    @DisplayName("resolveFormatValue and the deprecated getFormat alias agree")
    void resolveFormatValue_matchesDeprecatedGetFormat() {
        assertEquals(lang.resolveFormatValue("info"), lang.getFormat("info"));
    }

    @Test
    @DisplayName("resolveFormatValue returns empty string, not null, for a null path")
    void resolveFormatValue_nullPathReturnsEmptyString() {
        assertEquals("", lang.resolveFormatValue(null));
    }

    @RepeatedTest(5)
    @DisplayName("concurrent reloadLang + getString never throws and never mixes half-loaded state")
    void reloadLang_concurrentAccessIsAtomicallyConsistent() throws InterruptedException {
        // Stress test for the AtomicReference<LoadedLang> snapshot swap: readers
        // hammering getString() while reloadLang() runs on another thread should
        // never see a torn/partial state or throw, since each reload publishes a
        // fully-built LoadedLang in one atomic set().
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

        pool.submit(reader);
        pool.submit(reader);
        pool.submit(reader);
        pool.submit(reloader);

        assertTrue(done.await(10, TimeUnit.SECONDS), "test threads did not finish in time");
        pool.shutdownNow();
        assertFalse(failed.get(), "reader or reloader threw during concurrent access");
    }
}