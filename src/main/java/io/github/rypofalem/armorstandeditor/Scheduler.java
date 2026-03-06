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

import io.papermc.lib.PaperLib;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public class Scheduler {

    private static Boolean IS_FOLIA = null;
    private static Object GLOBAL_REGION_SCHEDULER = null;

    public static <T> T callMethod(Class<?> clazz, Object object, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            return (T) clazz.getMethod(methodName, parameterTypes).invoke(object, args);
        } catch (Throwable t) {
            throw new IllegalStateException(t);
        }
    }

    public static <T> T callMethod(Object object, String methodName, Class<?>[] parameterTypes, Object... args) {
        return callMethod(object.getClass(), object, methodName, parameterTypes, args);
    }

    public static <T> T callMethod(Class<?> clazz, String methodName) {
        return callMethod(clazz, null, methodName, new Class[]{});
    }

    private static boolean methodExist(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            clazz.getMethod(methodName, parameterTypes);
            return true;
        } catch (Throwable ignored) {
        }
        return false;
    }

    public static boolean isFolia() {
        if (IS_FOLIA != null) {
            return IS_FOLIA;
        }
        try {
            Class.forName("io.papermc.paper.threadedregions.ThreadedRegionizer");
            IS_FOLIA = true;
        }
        catch (Exception e) {
            IS_FOLIA = false;
        }
        return IS_FOLIA;
    }

    public static Object getGlobalRegionScheduler() {
        if (GLOBAL_REGION_SCHEDULER == null) {
            GLOBAL_REGION_SCHEDULER = callMethod(Bukkit.class, "getGlobalRegionScheduler");
        }
        return GLOBAL_REGION_SCHEDULER;
    }

    public static void runTask(Plugin plugin, Runnable runnable) {
        if (isFolia()) {
            Object globalRegionScheduler = getGlobalRegionScheduler();
            callMethod(globalRegionScheduler, "run", new Class[]{Plugin.class, Consumer.class}, plugin, (Consumer<?>) (task) -> runnable.run());
            return;
        }
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    public static void runTask(Plugin plugin, Entity entity, Runnable runnable) {
        if (entity == null) {
            runTask(plugin, runnable);
            return;
        }
        if (isFolia()) {
            runEntityTask(entity, plugin, runnable, 0L);
            return;
        }
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    public static void runTaskTimer(Plugin plugin, Runnable runnable, long initialDelayTicks, long periodTicks) {
        if (isFolia()) {
            Object globalRegionScheduler = getGlobalRegionScheduler();
            callMethod(globalRegionScheduler, "runAtFixedRate", new Class[]{Plugin.class, Consumer.class, long.class, long.class},
                plugin, (Consumer<?>) (task) -> runnable.run(), initialDelayTicks, periodTicks);
            return;
        }
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, initialDelayTicks, periodTicks);
    }

    public static void runTaskLater(Plugin plugin, Runnable runnable, long delayedTicks) {
        if (isFolia()) {
            Object globalRegionScheduler = getGlobalRegionScheduler();
            callMethod(globalRegionScheduler, "runDelayed", new Class[]{Plugin.class, Consumer.class, long.class},
                plugin, (Consumer<?>) (task) -> runnable.run(), delayedTicks);
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, runnable, delayedTicks);
    }

    public static void runTaskLater(Plugin plugin, Entity entity, Runnable runnable, long delayedTicks) {
        if (entity == null) {
            runTaskLater(plugin, runnable, delayedTicks);
            return;
        }
        if (isFolia()) {
            runEntityTask(entity, plugin, runnable, delayedTicks);
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, runnable, delayedTicks);
    }

    private static void runEntityTask(Entity entity, Plugin plugin, Runnable runnable, long delayedTicks) {
        Object entityScheduler = callMethod(entity, "getScheduler", new Class[]{});
        Consumer<?> taskConsumer = (task) -> runnable.run();
        Class<?> entitySchedulerClass = entityScheduler.getClass();

        if (delayedTicks <= 0L) {
            if (methodExist(entitySchedulerClass, "run", Plugin.class, Consumer.class, Runnable.class)) {
                callMethod(entityScheduler, "run", new Class[]{Plugin.class, Consumer.class, Runnable.class},
                    plugin, taskConsumer, null);
                return;
            }
            if (methodExist(entitySchedulerClass, "run", Plugin.class, Consumer.class)) {
                callMethod(entityScheduler, "run", new Class[]{Plugin.class, Consumer.class}, plugin, taskConsumer);
                return;
            }
            if (methodExist(entitySchedulerClass, "execute", Plugin.class, Runnable.class, Runnable.class, long.class)) {
                callMethod(entityScheduler, "execute", new Class[]{Plugin.class, Runnable.class, Runnable.class, long.class},
                    plugin, runnable, null, 0L);
                return;
            }
            throw new IllegalStateException("Unable to schedule Folia entity task");
        }

        if (methodExist(entitySchedulerClass, "runDelayed", Plugin.class, Consumer.class, Runnable.class, long.class)) {
            callMethod(entityScheduler, "runDelayed", new Class[]{Plugin.class, Consumer.class, Runnable.class, long.class},
                plugin, taskConsumer, null, delayedTicks);
            return;
        }
        if (methodExist(entitySchedulerClass, "runDelayed", Plugin.class, Consumer.class, long.class)) {
            callMethod(entityScheduler, "runDelayed", new Class[]{Plugin.class, Consumer.class, long.class},
                plugin, taskConsumer, delayedTicks);
            return;
        }
        if (methodExist(entitySchedulerClass, "execute", Plugin.class, Runnable.class, Runnable.class, long.class)) {
            callMethod(entityScheduler, "execute", new Class[]{Plugin.class, Runnable.class, Runnable.class, long.class},
                plugin, runnable, null, delayedTicks);
            return;
        }
        throw new IllegalStateException("Unable to schedule delayed Folia entity task");
    }

    public static void teleport(Entity entity, Location location) {
        if (isFolia()) PaperLib.teleportAsync(entity, location);
        else entity.teleport(location);
    }
}
