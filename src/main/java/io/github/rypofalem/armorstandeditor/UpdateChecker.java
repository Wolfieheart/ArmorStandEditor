package io.github.rypofalem.armorstandeditor;

import io.github.rypofalem.armorstandeditor.utils.VersionUtil;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class UpdateChecker implements Listener {

    private static final String PREFIX = "[ArmorStandEditor] ";
    private static final String HANGAR_URL = "https://hangar.papermc.io/api/v1/projects/Wolfieheart/ArmorStandEditor-Reborn/latest?channel=Release";
    private static final String HANGAR_DOWNLOAD = "https://hangar.papermc.io/Wolfieheart/ArmorStandEditor-Reborn";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();

    private enum Result { FAILED, UP_TO_DATE, UPDATE_AVAILABLE }

    private final ArmorStandEditorPlugin plugin;
    private final Scheduler scheduler;
    private final String pluginVersion;

    // Written on async thread, read on main thread
    private volatile String versionOnHangar;
    private volatile boolean updateAvailable;

    public UpdateChecker(ArmorStandEditorPlugin plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getScheduler();
        this.pluginVersion = plugin.getASEVersion();
    }

    /** Checks for updates and logs the result to console. Used on startup. */
    public void checkForUpdates() {
        scheduler.runAsync(() -> {
            switch (check()) {
                case UPDATE_AVAILABLE -> {
                    plugin.getLogger().info("A new version of ArmorStandEditor-Reborn is available! (Version " + versionOnHangar + ")");
                    plugin.getLogger().info("Download it from: " + HANGAR_DOWNLOAD);
                }
                case UP_TO_DATE -> plugin.getLogger().info("You are running the latest version of ArmorStandEditor-Reborn.");
                case FAILED -> { /* already logged */ }
            }
        });
    }

    /** Checks for updates and sends the result to a player. Used by /ase update. */
    public void checkForUpdatesAndNotify(Player player) {
        scheduler.runAsync(() -> {
            List<Component> messages = switch (check()) {
                case FAILED -> List.of(prefixed("Could not fetch the latest version. Check your internet connection.", NamedTextColor.RED));
                case UP_TO_DATE -> List.of(prefixed("You are running the latest version (" + pluginVersion + ").", NamedTextColor.GREEN));
                case UPDATE_AVAILABLE -> updateMessages();
            };
            scheduler.runTask(() -> messages.forEach(player::sendMessage));
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent evt) {
        if (!updateAvailable) return;
        boolean canSee = evt.getPlayer().hasPermission("asedit.update") || !plugin.getAdminOnlyNotifications();
        if (!canSee) return;

        updateMessages().forEach(evt.getPlayer()::sendMessage);
    }

    // ---------------------------------------------------------------- internals

    private Result check() {
        Optional<String> latest = fetchLatestVersion();
        if (latest.isEmpty()) return Result.FAILED;

        try {
            versionOnHangar = latest.get();
            updateAvailable = VersionUtil.fromString(versionOnHangar)
                    .isNewerThan(VersionUtil.fromString(pluginVersion));
        } catch (RuntimeException ex) {
            plugin.getLogger().warning("Could not parse version '" + latest.get() + "' returned by Hangar.");
            plugin.debug.log("Version parse error: " + ex.getMessage());
            return Result.FAILED;
        }
        return updateAvailable ? Result.UPDATE_AVAILABLE : Result.UP_TO_DATE;
    }

    private Optional<String> fetchLatestVersion() {
        HttpRequest request = HttpRequest.newBuilder(URI.create(HANGAR_URL))
                .timeout(TIMEOUT)
                .header("User-Agent", "ArmorStandEditor-Reborn/" + pluginVersion)
                .GET()
                .build();
        try {
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IOException("HTTP " + response.statusCode());
            }
            return response.body().lines().map(String::trim).filter(s -> !s.isEmpty()).findFirst();
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (Exception ex) {
            plugin.getLogger().warning("Failed to get the latest version from Hangar. Please check your internet connection.");
            plugin.debug.log("Error while checking for updates: " + ex.getMessage());
            return Optional.empty();
        }
    }

    private List<Component> updateMessages() {
        return List.of(
                prefixed("A new version is available! (Version " + versionOnHangar + ")", NamedTextColor.YELLOW),
                prefixed("Download it from: ", NamedTextColor.YELLOW)
                        .append(Component.text(HANGAR_DOWNLOAD, NamedTextColor.AQUA)
                                .decorate(TextDecoration.UNDERLINED))
        );
    }

    private static Component prefixed(String text, NamedTextColor color) {
        return Component.text(PREFIX + text, color);
    }
}