/*
 * ArmorStandEditor: Bukkit plugin to allow editing armor stand attributes
 * Copyright (C) 2016-2023  RypoFalem
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */


package io.github.rypofalem.armorstandeditor.language;

import io.github.rypofalem.armorstandeditor.ArmorStandEditorPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Language {
    static final String DEFAULT_LANG = "en_US.yml";
    private YamlConfiguration langConfig = null;
    private YamlConfiguration defConfig = null;
    private File langFile = null;
    ArmorStandEditorPlugin plugin;
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final Logger LOGGER = Logger.getLogger(Language.class.getName());

    // Legacy Minecraft color codes mapped to hex values
    private static final Map<String, String> LEGACY_COLORS = createLegacyColorMap();

    private static Map<String, String> createLegacyColorMap() {
        Map<String, String> colors = new HashMap<>();
        colors.put("0", "#000000");
        colors.put("1", "#0000aa");
        colors.put("2", "#00aa00");
        colors.put("3", "#00aaaa");
        colors.put("4", "#aa0000");
        colors.put("5", "#aa00aa");
        colors.put("6", "#ffaa00");
        colors.put("7", "#aaaaaa");
        colors.put("8", "#555555");
        colors.put("9", "#5555ff");
        colors.put("a", "#55ff55");
        colors.put("b", "#55ffff");
        colors.put("c", "#ff5555");
        colors.put("d", "#ff55ff");
        colors.put("e", "#ffff55");
        colors.put("f", "#ffffff");
        return colors;
    }

    public Language(String langFileName, ArmorStandEditorPlugin plugin) {
        this.plugin = plugin;
        reloadLang(langFileName);
    }

    public void reloadLang(String langFileName) {
        if (langFileName == null) langFileName = DEFAULT_LANG;
        File langFolder = new File(plugin.getDataFolder().getPath() + File.separator + "lang");
        langFile = new File(langFolder, langFileName);

        // Load default language config
        try {
            InputStream defaultInput = plugin.getResource("lang" + "/" + DEFAULT_LANG);
            if (defaultInput == null) {
                LOGGER.log(Level.WARNING, "Default language file not found: {0}", DEFAULT_LANG);
                return;
            }
            try (Reader defaultLangStream = new InputStreamReader(defaultInput, StandardCharsets.UTF_8)) {
                defConfig = YamlConfiguration.loadConfiguration(defaultLangStream);
            } // defaultInput is closed implicitly by try-with-resources
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load default language file", e);
            return;
        }

        // Load custom language config
        try {
            try (InputStream customInput = new FileInputStream(langFile);
                 Reader langStream = new InputStreamReader(customInput, StandardCharsets.UTF_8)) {
                langConfig = YamlConfiguration.loadConfiguration(langStream);
            }
        } catch (FileNotFoundException _) {
            LOGGER.log(Level.INFO, "Custom language file not found: {0}. Using default.", langFile.getName());
            langConfig = defConfig; // Fallback to default if custom not found
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load language file: {0}", langFile.getName());
            LOGGER.log(Level.WARNING, "Exception details", e);
            langConfig = defConfig; // Fallback to default on error
        }
    }

    // path: yml path to message in language file
    // format: yml path to format in language file (info, warn, etc)
    // option: path-specific variable
    public Component getMessage(String path, String format, String option) {
        if (langConfig == null) {
            reloadLang(langFile != null ? langFile.getName() : DEFAULT_LANG);
        }

        // If still null after reload attempt, log error and return empty
        if (langConfig == null) {
            LOGGER.log(Level.WARNING, "Language config is null, cannot get message: {0}", path);
            return Component.empty();
        }

        if (path == null) return Component.empty();

        String raw = getString(path + ".msg");
        if (raw == null) return Component.empty();

        String resolvedOption = "";
        if(option != null && !option.isEmpty()){
            String translated = getString(path + "." + option);
            resolvedOption = (translated != null) ? translated : option; // fallback to raw key
        }

        // Resolve format color (info/warn/etc)
        String formatValue = getFormat(format);

        // Use resolvedOption, not option
        Component base = MINI.deserialize(raw, Placeholder.unparsed("x", resolvedOption));


        // Apply format color LAST
        if (formatValue != null && !formatValue.isEmpty()) {
            TextColor color = resolveFormatColor(formatValue);
            if (color != null) {
                base = base.color(color);
            }
        }

        return base;
    }


    private TextColor resolveFormatColor(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        // Hex format
        if (value.startsWith("#")) {
            try {
                return TextColor.fromHexString(value);
            } catch (IllegalArgumentException _) {
                LOGGER.log(Level.WARNING, "Invalid hex color: {0}", value);
                return null;
            }
        }

        // Legacy single-character colors
        String lowerValue = value.toLowerCase();
        String hexColor = LEGACY_COLORS.get(lowerValue);
        if (hexColor != null) {
            return TextColor.fromHexString(hexColor);
        }

        LOGGER.log(Level.FINE, "Unknown color code: {0}", value);
        return null;
    }


    public Component getMessage(String path, String format) {
        return getMessage(path, format, null);
    }

    public Component getMessage(String path) {
        return getMessage(path, "info");
    }

    public String getFormat(String format) {
        format = getString(format);
        return format == null ? "" : format;
    }

    public String getString(String path) {
        String message = null;
        if (langConfig != null && langConfig.contains(path)) {
            message = langConfig.getString(path);
        } else if (defConfig != null && defConfig.contains(path)) {
            message = defConfig.getString(path);
        }
        return message;
    }
}