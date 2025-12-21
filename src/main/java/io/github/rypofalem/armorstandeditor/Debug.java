package io.github.rypofalem.armorstandeditor;

import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;


public class Debug {

    private ArmorStandEditorPlugin plugin;
    private Logger logger;
    private FileHandler fileHandler;

    public Debug(ArmorStandEditorPlugin plugin) {
        this.plugin = plugin;
        this.logger = Logger.getLogger("[ArmorStandEditor-Debug]");
        debugFileLogSetup();
    }

    public void debugFileLogSetup() {
        try {
            File pluginDataFolder = plugin.getDataFolder();
            if (!pluginDataFolder.exists()) {
                pluginDataFolder.mkdirs();
            }

            File debugFolder = new File(pluginDataFolder, "debug");
            if (!debugFolder.exists()) {
                debugFolder.mkdirs();
            }

            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            File logFile = new File(debugFolder, "debugLog-" + date + ".log");

            fileHandler = new FileHandler(logFile.getAbsolutePath(), true);

            fileHandler.setFormatter(new Formatter() {
                private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                @Override
                public String format(LogRecord record) {
                    String time = dateFormat.format(new Date(record.getMillis()));
                    return String.format("[%s] [ArmorStandEditor-Debug] %s%n", time, record.getMessage());
                }
            });

            // Remove old handlers to avoid duplicates
            for (Handler h : logger.getHandlers()) {
                logger.removeHandler(h);
            }

            logger.setUseParentHandlers(false); // do not pass to parent logger
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);

        } catch (IOException e) {
            Bukkit.getServer().getLogger().severe("[ArmorStandEditor] Could not create debug log file!");
        }
    }

    public void log(String msg) {
        if (!plugin.isDebug()) return;

        String finalMsg = msg; // Timestamp handled by formatter for file
        logger.info(finalMsg);

        Bukkit.getServer().getLogger().info("[ArmorStandEditor-Debug] " + msg);
    }

    /**
     * Optional: Call on plugin disable to flush and close file cleanly.
     */
    public void shutdown() {
        if (fileHandler != null) {
            fileHandler.flush();
            fileHandler.close();
        }
    }
}