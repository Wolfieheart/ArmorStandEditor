package io.github.rypofalem.armorstandeditor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

/**
 * Folia & Paper-compatible Scheduler utility.
 * Handles runTask, runTaskLater, runTaskTimer, entity-based and location-based tasks safely.
 */
public class Scheduler {

    private final Plugin plugin;
    private final boolean isFolia;

    public Scheduler(Plugin plugin) {
        this.plugin = plugin;
        this.isFolia = detectFolia();
    }

    /** Detects if the server is running Folia */
    private static boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    /** Run a task on the next tick */
    public void runTask(Runnable task) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().run(plugin, t -> task.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }

    /** Run a delayed task */
    public void runTaskLater(Runnable task, long delayTicks) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, t -> task.run(), delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    /** Run a repeating task (Folia-compatible) */
    public void runTaskTimer(Runnable task, long delayTicks, long periodTicks) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, t -> task.run(), 1, periodTicks);
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
        }
    }

    /** Teleport an entity safely */
    public void teleport(Entity entity, Location location) {
        if (isFolia) {
            entity.teleportAsync(location);
        } else {
            entity.teleport(location);
        }
    }

    /** Run a task for a specific entity */
    public void runForEntity(Entity entity, Runnable task) {
        if (isFolia) {
            entity.getScheduler().run(plugin, t -> task.run(), null);
        } else {
            runTask(task);
        }
    }

    /** Run a task at a specific location (region-safe) */
    public void runAtLocation(Location location, Runnable task) {
        if (isFolia) {
            Bukkit.getRegionScheduler().run(plugin, location, t -> task.run());
        } else {
            runTask(task);
        }
    }

    /** Run an async task (Paper only, Folia falls back to region scheduler) */
    public void runAsync(Runnable task) {
        if (isFolia) {
            Bukkit.getGlobalRegionScheduler().run(plugin, t -> task.run());
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }
}