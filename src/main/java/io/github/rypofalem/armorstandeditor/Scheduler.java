/*
 * ArmorStandEditor: Bukkit plugin to allow editing armor stand attributes
 * Copyright (C) 2016-2023  RypoFalem
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package io.github.rypofalem.armorstandeditor;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Scheduler {
    private static final boolean IS_FOLIA = isFolia();
    private static ArmorStandEditorPlugin plugin;
    private static final Set<ScheduledTask> REPEATING = ConcurrentHashMap.newKeySet();
    private static final Set<BukkitTask> REPEATING_BUKKIT = ConcurrentHashMap.newKeySet();

    public static void init(ArmorStandEditorPlugin plugin) {
        Scheduler.plugin = plugin;
    }

    public static void shutdown() {
        for (ScheduledTask task : REPEATING) {
            task.cancel();
        }
        REPEATING.clear();
        for (BukkitTask task : REPEATING_BUKKIT) {
            task.cancel();
        }
        REPEATING_BUKKIT.clear();
    }

    public static Boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.ThreadedRegionizer");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void runTask(Runnable runnable) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().run(plugin, t -> runnable.run());
        } else {
            Bukkit.getScheduler().runTask(plugin, runnable);
        }
    }

    public static void runTaskTimer(Runnable runnable, long initialDelayTicks, long periodTicks) {
        if (IS_FOLIA) {
            REPEATING.add(Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, t -> runnable.run(), initialDelayTicks, periodTicks));
        } else {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, initialDelayTicks, periodTicks);
        }
    }

    public static void runTaskLater(Runnable runnable, long delayedTicks) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, t -> runnable.run(), delayedTicks);
        } else {
            REPEATING_BUKKIT.add(Bukkit.getScheduler().runTaskLater(plugin, runnable, delayedTicks));
        }
    }

    public static void teleport(Entity entity, Location location) {
        if (IS_FOLIA) {
            entity.teleportAsync(location);
        } else {
            entity.teleport(location);
        }
    }
}
