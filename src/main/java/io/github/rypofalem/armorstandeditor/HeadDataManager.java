package io.github.rypofalem.armorstandeditor;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class HeadDataManager {

    private final Plugin plugin;
    private final File dataFile;
    private FileConfiguration data;

    public HeadDataManager(Plugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerheads.yml");
        load();
    }

    private void load() {
        if (!dataFile.exists()) {
            try {
                boolean created = dataFile.createNewFile();
                if (!created) {
                    plugin.getLogger().warning("playerheads.yml already exists, skipping creation.");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create playerheads.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public int getCount(UUID uuid) {
        return data.getInt(uuid.toString(), 0);
    }

    public void increment(UUID uuid) {
        data.set(uuid.toString(), getCount(uuid) + 1);
        save();
    }

    private void save() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save playerheads.yml: " + e.getMessage());
        }
    }

}
