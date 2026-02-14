package io.github.rypofalem.armorstandeditor;


import java.util.logging.Level;

public class Debug {

    private ArmorStandEditorPlugin plugin;

    public Debug(ArmorStandEditorPlugin plugin) {
        this.plugin = plugin;
    }

    public void log(String msg) {
        if (!plugin.isDebug()) return;
        plugin.getLogger().log(Level.INFO, "[ArmorStandEditor-Debug] {0}", msg);
    }
}
