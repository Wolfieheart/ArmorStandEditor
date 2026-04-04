package io.github.rypofalem.armorstandeditor;

import io.github.rypofalem.armorstandeditor.utils.VersionUtil;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

public class UpdateChecker implements Listener {

    private static final String HANGAR_URL = "https://hangar.papermc.io/api/v1/projects/Wolfieheart/ArmorStandEditor-Reborn/latest?channel=Release";

    private static ArmorStandEditorPlugin plugin = null;
    private static Scheduler taskScheduler = null;
    private final String pluginVersion;
    public String versionOnHangar;
    public boolean updateAvailable;

    public UpdateChecker(ArmorStandEditorPlugin plugin) {
        UpdateChecker.plugin = plugin;
        taskScheduler = plugin.getScheduler();
        pluginVersion = plugin.getPluginMeta().getVersion();
    }

    public void checkForUpdates() {
        taskScheduler.runAsync(() -> {
            if (!fetchLatestVersion()) {
                return;
            }

            if (isUpdateAvailable()) {
                plugin.getLogger().info("A new version of ArmorStandEditor-Reborn is available! (Version " + versionOnHangar + ")");
                plugin.getLogger().info("Download it from: https://hangar.papermc.io/Wolfieheart/ArmorStandEditor-Reborn");
            }
        });
    }

    private boolean fetchLatestVersion() {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) URI
                .create(HANGAR_URL)
                .toURL()
                .openConnection();
            connection.setRequestMethod("GET");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                JsonElement json = new Gson().fromJson(reader, JsonElement.class);
                versionOnHangar = json.getAsJsonObject().get("version_name").getAsString();
                return versionOnHangar != null && !versionOnHangar.isEmpty();
            }
        } catch (Exception exception) {
            plugin.getLogger().warning("Failed to get the latest version from Hangar. Please check your internet connection.");
            plugin.debug.log("Error while checking for updates: " + exception.getMessage());
            return false;
        }
    }

    private boolean isUpdateAvailable() {
        updateAvailable = VersionUtil.fromString(versionOnHangar).isNewerThan(VersionUtil.fromString(pluginVersion));
        return updateAvailable;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent evt) {
        // Only notify if an update is available
        if (!updateAvailable) return;

        // Notify if the player has permission OR if AdminOnlyNotifications is disabled
        boolean hasPermission = evt.getPlayer().hasPermission("asedit.update");
        boolean shouldNotify = hasPermission || !plugin.getAdminOnlyNotifications();

        if (!shouldNotify) return;

        // Build the messages using Components
        Component updateMessage = Component.text("A new version of ArmorStandEditor-Reborn is available! (Version " + versionOnHangar + ")")
            .color(NamedTextColor.YELLOW);

        Component downloadMessage = Component.text("Download it from: ")
            .color(NamedTextColor.YELLOW)
            .append(Component.text("https://hangar.papermc.io/Wolfieheart/ArmorStandEditor-Reborn")
                .color(NamedTextColor.AQUA)
                .decorate(TextDecoration.UNDERLINED));

        // Send to player
        evt.getPlayer().sendMessage(updateMessage);
        evt.getPlayer().sendMessage(downloadMessage);
    }

}
