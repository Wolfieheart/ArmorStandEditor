package io.github.rypofalem.armorstandeditor;

import io.github.rypofalem.armorstandeditor.language.Language;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Shared scaffolding for Protection implementations.
 * Mocks Bukkit's PluginManager/ServicesManager, ArmorStandEditorPlugin.instance(),
 * and a baseline Player/Block/Location/World. Subclasses add only the
 * third-party API mocks specific to the protection under test.
 */
public abstract class BaseProtectionTest {

    protected MockedStatic<Bukkit> bukkitMock;
    protected MockedStatic<ArmorStandEditorPlugin> pluginInstanceMock;

    protected PluginManager pluginManager;
    protected Server server;
    protected ServicesManager servicesManager;

    protected ArmorStandEditorPlugin plugin;
    protected Debug debug;
    protected Language lang;

    protected Player player;
    protected World world;
    protected Location location;
    protected Block block;

    @BeforeEach
    void baseSetUp() {
        // --- Bukkit static mocks ---
        pluginManager = mock(PluginManager.class);
        servicesManager = mock(ServicesManager.class);
        server = mock(Server.class);
        when(server.getServicesManager()).thenReturn(servicesManager);

        bukkitMock = mockStatic(Bukkit.class);
        bukkitMock.when(Bukkit::getPluginManager).thenReturn(pluginManager);
        bukkitMock.when(Bukkit::getServer).thenReturn(server);

        // --- ArmorStandEditorPlugin.instance() ---
        plugin = mock(ArmorStandEditorPlugin.class);
        debug = mock(Debug.class);
        lang = mock(Language.class);
        plugin.debug = debug; // public field set directly on the mock

        pluginInstanceMock = mockStatic(ArmorStandEditorPlugin.class);
        pluginInstanceMock.when(ArmorStandEditorPlugin::instance).thenReturn(plugin);
        when(plugin.getLang()).thenReturn(lang);

        // --- Baseline world/location/block/player ---
        world = mock(World.class);
        location = mock(Location.class);
        when(location.getWorld()).thenReturn(world);

        block = mock(Block.class);
        when(block.getLocation()).thenReturn(location);
        when(block.getWorld()).thenReturn(world);

        player = mock(Player.class);
        when(player.getLocation()).thenReturn(location);
        when(player.getWorld()).thenReturn(world);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());

        // Sensible default: not op, no permissions. Override per-test as needed.
        setDefaultPlayerPermissions();
    }

    @AfterEach
    void baseTearDown() {
        pluginInstanceMock.close();
        bukkitMock.close();
    }

    /** Enables/disables a soft-dependency plugin via Bukkit.getPluginManager().isPluginEnabled(name). */
    protected void setPluginEnabled(String name, boolean enabled) {
        when(pluginManager.isPluginEnabled(name)).thenReturn(enabled);
    }

    /** Resets the player to a non-op with no granted permissions. */
    protected void setDefaultPlayerPermissions() {
        when(player.isOp()).thenReturn(false);
        when(player.hasPermission(anyString())).thenReturn(false);
    }

    protected void setPlayerOp(boolean isOp) {
        when(player.isOp()).thenReturn(isOp);
    }

    protected void setPlayerPermission(String permission, boolean granted) {
        when(player.hasPermission(permission)).thenReturn(granted);
    }
}