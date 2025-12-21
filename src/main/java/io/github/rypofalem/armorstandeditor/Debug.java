package io.github.rypofalem.armorstandeditor;

import java.util.logging.*;


public class Debug {

    private ArmorStandEditorPlugin plugin;

    public Debug(ArmorStandEditorPlugin plugin) {
        this.plugin = plugin;
    }

    public void log(String msg) {
        if (!plugin.isDebug()) return;
        plugin.getLogger().info("[ArmorStandEditor-Debug] " + msg);
    }
}
