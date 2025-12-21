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
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Language {
    final String DEFAULT_LANG = "en_US.yml";
    private YamlConfiguration langConfig = null;
    private YamlConfiguration defConfig = null;
    private File langFile = null;
    ArmorStandEditorPlugin plugin;

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

        input = null;
        try {
            input = new FileInputStream(langFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        Reader langStream = new InputStreamReader(input, StandardCharsets.UTF_8);
        langConfig = YamlConfiguration.loadConfiguration(langStream);
    }

    //path: yml path to message in language file
    //format: yml path to format in language file
    //option: path-specific variable that may be used
    public Component getMessage(String path, String format, String option) {
        if (langConfig == null) reloadLang(langFile.getName());
        if (path == null) return Component.empty();
        if (option == null) option = "";

        format = getFormat(format);

        String rawMessage = langConfig.getString(path + ".msg");
        if (rawMessage == null) return Component.empty();
        rawMessage = rawMessage.replace("<x>", option);

        // Decorations
        boolean underline = langConfig.getBoolean(path + ".underline", false);
        boolean bold = langConfig.getBoolean(path + ".bold", false);
        boolean italic = langConfig.getBoolean(path + ".italic", false);
        boolean strikethrough = langConfig.getBoolean(path + ".strikethrough", false);

        // Gradient?
        String from = langConfig.getString(path + ".gradient.from");
        String to = langConfig.getString(path + ".gradient.to");

        Component comp;

        if (from != null && to != null) {
            comp = gradient(rawMessage, from, to);
        } else {
            // Normal color
            String hex = langConfig.getString(path + ".color");
            comp = Component.text(rawMessage);

            if (hex != null && !hex.isEmpty()) {
                comp = comp.color(TextColor.fromHexString(hex));
            }
        }

        // Apply decorations
        if (underline) comp = comp.decorate(TextDecoration.UNDERLINED);
        if (bold) comp = comp.decorate(TextDecoration.BOLD);
        if (italic) comp = comp.decorate(TextDecoration.ITALIC);
        if (strikethrough) comp = comp.decorate(TextDecoration.STRIKETHROUGH);

        return comp;
    }

    public Component gradient(String text, String startHex, String endHex) {
        int start = Integer.parseInt(startHex.replace("#", ""), 16);
        int end = Integer.parseInt(endHex.replace("#", ""), 16);

        int length = text.length();
        Component comp = Component.empty();

        for (int i = 0; i < length; i++) {
            float ratio = (float) i / (length - 1);

            int r = (int) (((start >> 16) & 0xFF) * (1 - ratio) + ((end >> 16) & 0xFF) * ratio);
            int g = (int) (((start >> 8) & 0xFF) * (1 - ratio) + ((end >> 8) & 0xFF) * ratio);
            int b = (int) (((start) & 0xFF) * (1 - ratio) + ((end) & 0xFF) * ratio);

            int rgb = (r << 16) | (g << 8) | b;

            comp = comp.append(
                    Component.text(String.valueOf(text.charAt(i)))
                            .color(TextColor.color(rgb))
            );
        }

        return comp;
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