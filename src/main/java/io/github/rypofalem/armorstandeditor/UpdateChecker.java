package io.github.rypofalem.armorstandeditor;

import io.github.rypofalem.armorstandeditor.utils.VersionUtil;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

public class UpdateChecker implements Listener {

    private static final String HANGAR_URL = "https://hangar.papermc.io/api/v1/projects/Wolfieheart/ArmorStandEditor-Reborn/latest?channel=Release";
    private static final String HANGAR_DOWNLOAD = "https://hangar.papermc.io/Wolfieheart/ArmorStandEditor-Reborn";

    private final ArmorStandEditorPlugin plugin;
    private final Scheduler taskScheduler;
    private final String pluginVersion;
    private String versionOnHangar;
    private boolean updateAvailable;

    public UpdateChecker(ArmorStandEditorPlugin plugin) {
        this.plugin = plugin;
        this.taskScheduler = plugin.getScheduler();
        this.pluginVersion = plugin.getASEVersion();
    }

    /**
     * Checks for updates and logs the result to console.
     * Used on startup.
     */
    public void checkForUpdates() {
        taskScheduler.runAsync(() -> {
            if (!fetchLatestVersion()) return;

            if (isUpdateAvailable()) {
                plugin.getLogger().info("A new version of ArmorStandEditor-Reborn is available! (Version " + versionOnHangar + ")");
                plugin.getLogger().info("Download it from: " + HANGAR_DOWNLOAD);
            } else {
                plugin.getLogger().info("You are running the latest version of ArmorStandEditor-Reborn.");
            }
        });
    }

    /**
     * Checks for updates and sends the result to a specific player.
     * Used by /ase update command (CommandEx).
     *
     * @param player the player who ran the command
     */
    public void checkForUpdatesAndNotify(Player player) {
        taskScheduler.runAsync(() -> {
            if (!fetchLatestVersion()) {
                taskScheduler.runTask(() ->                          // ← was runSync
                        player.sendMessage(Component.text("[ArmorStandEditor] Could not fetch the latest version. Check your internet connection.")
                                .color(NamedTextColor.RED))
                );
                return;
            }

            if (isUpdateAvailable()) {
                Component updateMessage = Component.text("[ArmorStandEditor] A new version is available! (Version " + versionOnHangar + ")")
                        .color(NamedTextColor.YELLOW);

                Component downloadMessage = Component.text("[ArmorStandEditor] Download it from: ")
                        .color(NamedTextColor.YELLOW)
                        .append(Component.text(HANGAR_DOWNLOAD)
                                .color(NamedTextColor.AQUA)
                                .decorate(TextDecoration.UNDERLINED));

                taskScheduler.runTask(() -> {                        // ← was runSync
                    player.sendMessage(updateMessage);
                    player.sendMessage(downloadMessage);
                });
            } else {
                taskScheduler.runTask(() ->                          // ← was runSync
                        player.sendMessage(Component.text("[ArmorStandEditor] You are running the latest version (" + pluginVersion + ").")
                                .color(NamedTextColor.GREEN))
                );
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
                versionOnHangar = reader.readLine();
            }

            return versionOnHangar != null && !versionOnHangar.isEmpty();

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
        if (!updateAvailable) return;

        boolean hasPermission = evt.getPlayer().hasPermission("asedit.update");
        boolean shouldNotify = hasPermission || !plugin.getAdminOnlyNotifications();

        if (!shouldNotify) return;

        Component updateMessage = Component.text("A new version of ArmorStandEditor-Reborn is available! (Version " + versionOnHangar + ")")
                .color(NamedTextColor.YELLOW);

        Component downloadMessage = Component.text("Download it from: ")
                .color(NamedTextColor.YELLOW)
                .append(Component.text(HANGAR_DOWNLOAD)
                        .color(NamedTextColor.AQUA)
                        .decorate(TextDecoration.UNDERLINED));

        evt.getPlayer().sendMessage(updateMessage);
        evt.getPlayer().sendMessage(downloadMessage);
    }
}