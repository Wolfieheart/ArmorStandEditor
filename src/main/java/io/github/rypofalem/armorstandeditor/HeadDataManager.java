package io.github.rypofalem.armorstandeditor;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class HeadDataManager {

    private final ArmorStandEditorPlugin plugin;
    private final File dataFile;
    private FileConfiguration data;

    public HeadDataManager(ArmorStandEditorPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerheads.yml");
        load();
    }

    private void debug(String msg) {
        plugin.debug.log("[HeadData] " + msg);
    }

    private void load() {
        if (!dataFile.exists()) {
            try {
                boolean created = dataFile.createNewFile();
                debug("Created playerheads.yml: " + created);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create playerheads.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
        debug("Loaded playerheads.yml with " + data.getKeys(false).size() + " entries");
    }

    public int getCount(UUID uuid) {
        return data.getInt(uuid.toString(), 0);
    }

    public void increment(UUID uuid) {
        int before = getCount(uuid);
        data.set(uuid.toString(), before + 1);
        debug("Increment " + uuid + ": " + before + " -> " + (before + 1));
        save();
    }

    public void reset(UUID uuid) {
        debug("reset " + uuid + " (was " + getCount(uuid) + ")");
        data.set(uuid.toString(), null);
        save();
        debug("reset " + uuid + " done, now " + getCount(uuid));
    }

    public void resetAll() {
        debug("resetAll clearing " + data.getKeys(false).size() + " entries");
        for (String key : data.getKeys(false)) {
            data.set(key, null);
        }
        save();
        debug("resetAll saved, entries left=" + data.getKeys(false).size());
    }

    private void save() {
        try {
            data.save(dataFile);
            debug("Saved playerheads.yml");
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save playerheads.yml: " + e.getMessage());
            debug("Save FAILED: " + e.getMessage());
        }
    }
}