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

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Language {
    static final String DEFAULT_LANG = "en_US.yml";
    private YamlConfiguration langConfig = null;
    private YamlConfiguration defConfig = null;
    private File langFile = null;
    ArmorStandEditorPlugin plugin;
    private static final MiniMessage MINI = MiniMessage.miniMessage();

    public Language(String langFileName, ArmorStandEditorPlugin plugin) {
        this.plugin = plugin;
        reloadLang(langFileName);
    }

    public void reloadLang(String langFileName) {
        if (langFileName == null) langFileName = DEFAULT_LANG;
        File langFolder = new File(plugin.getDataFolder().getPath() + File.separator + "lang");
        langFile = new File(langFolder, langFileName);

        InputStream input = plugin.getResource("lang" + "/" + DEFAULT_LANG); //getResource doesn't accept File.seperator on windows, need to hardcode unix seperator "/" instead
        assert input != null;
        Reader defaultLangStream = new InputStreamReader(input, StandardCharsets.UTF_8);
        defConfig = YamlConfiguration.loadConfiguration(defaultLangStream);

        try {
            input = new FileInputStream(langFile);
        } catch (FileNotFoundException e) {
            return;
        }

        Reader langStream = new InputStreamReader(input, StandardCharsets.UTF_8);
        langConfig = YamlConfiguration.loadConfiguration(langStream);
    }

    // path: yml path to message in language file
    // format: yml path to format in language file (info, warn, etc)
    // option: path-specific variable
    public Component getMessage(String path, String format, String option) {
        if (langConfig == null) reloadLang(langFile.getName());
        if (path == null) return Component.empty();
        if (option == null) option = "";

        String raw = getString(path + ".msg");
        if (raw == null) return Component.empty();

        // Replace option placeholder
        raw = raw.replace("<x>", option);

        // Resolve format color (info/warn/etc)
        String formatValue = getFormat(format);

        Component base = MINI.deserialize(raw);

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
        // Hex format
        if (value.startsWith("#")) {
            return TextColor.fromHexString(value);
        }

        // Legacy single-character colors
        return switch (value.toLowerCase()) {
            case "0" -> TextColor.fromHexString("#000000");
            case "1" -> TextColor.fromHexString("#0000aa");
            case "2" -> TextColor.fromHexString("#00aa00");
            case "3" -> TextColor.fromHexString("#00aaaa");
            case "4" -> TextColor.fromHexString("#aa0000");
            case "5" -> TextColor.fromHexString("#aa00aa");
            case "6" -> TextColor.fromHexString("#ffaa00");
            case "7" -> TextColor.fromHexString("#aaaaaa");
            case "8" -> TextColor.fromHexString("#555555");
            case "9" -> TextColor.fromHexString("#5555ff");
            case "a" -> TextColor.fromHexString("#55ff55");
            case "b" -> TextColor.fromHexString("#55ffff");
            case "c" -> TextColor.fromHexString("#ff5555");
            case "d" -> TextColor.fromHexString("#ff55ff");
            case "e" -> TextColor.fromHexString("#ffff55");
            case "f" -> TextColor.fromHexString("#ffffff");
            default -> null;
        };
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
        if (langConfig.contains(path)) {
            message = langConfig.getString(path);
        } else if (defConfig.contains(path)) {
            message = defConfig.getString(path);
        }
        return message;
    }
}