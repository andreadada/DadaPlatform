package me.mrbast.platform.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PlatformScheduler {

    private final Plugin plugin;
    private final boolean folia;

    public PlatformScheduler(Plugin plugin, boolean folia) {
        this.plugin = plugin;
        this.folia = folia;
    }

    public boolean isFolia() {
        return folia;
    }

    public void runGlobal(Runnable task) {
        if (folia) {
            invokeFoliaScheduler(Bukkit.getServer(), "getGlobalRegionScheduler", "run",
                    new Class<?>[]{Plugin.class, Consumer.class},
                    plugin, consumer(task));
            return;
        }

        Bukkit.getScheduler().runTask(plugin, task);
    }

    public void runGlobalLater(Runnable task, long delayTicks) {
        if (delayTicks <= 0) {
            runGlobal(task);
            return;
        }

        if (folia) {
            invokeFoliaScheduler(Bukkit.getServer(), "getGlobalRegionScheduler", "runDelayed",
                    new Class<?>[]{Plugin.class, Consumer.class, long.class},
                    plugin, consumer(task), delayTicks);
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    public void runAsync(Runnable task) {
        if (folia) {
            invokeFoliaScheduler(Bukkit.getServer(), "getAsyncScheduler", "runNow",
                    new Class<?>[]{Plugin.class, Consumer.class},
                    plugin, consumer(task));
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    public void runAsyncLater(Runnable task, long delayTicks) {
        if (delayTicks <= 0) {
            runAsync(task);
            return;
        }

        if (folia) {
            invokeFoliaScheduler(Bukkit.getServer(), "getAsyncScheduler", "runDelayed",
                    new Class<?>[]{Plugin.class, Consumer.class, long.class, TimeUnit.class},
                    plugin, consumer(task), delayTicks * 50L, TimeUnit.MILLISECONDS);
            return;
        }

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
    }

    public void runAt(Location location, Runnable task) {
        if (folia) {
            invokeFoliaScheduler(Bukkit.getServer(), "getRegionScheduler", "run",
                    new Class<?>[]{Plugin.class, Location.class, Consumer.class},
                    plugin, location, consumer(task));
            return;
        }

        runGlobal(task);
    }

    public void runAtLater(Location location, Runnable task, long delayTicks) {
        if (delayTicks <= 0) {
            runAt(location, task);
            return;
        }

        if (folia) {
            invokeFoliaScheduler(Bukkit.getServer(), "getRegionScheduler", "runDelayed",
                    new Class<?>[]{Plugin.class, Location.class, Consumer.class, long.class},
                    plugin, location, consumer(task), delayTicks);
            return;
        }

        runGlobalLater(task, delayTicks);
    }

    public void runFor(Entity entity, Runnable task) {
        if (folia) {
            invokeEntityScheduler(entity, "run",
                    new Class<?>[]{Plugin.class, Consumer.class, Runnable.class},
                    plugin, consumer(task), null);
            return;
        }

        runGlobal(task);
    }

    public void runForLater(Entity entity, Runnable task, long delayTicks) {
        if (delayTicks <= 0) {
            runFor(entity, task);
            return;
        }

        if (folia) {
            invokeEntityScheduler(entity, "runDelayed",
                    new Class<?>[]{Plugin.class, Consumer.class, Runnable.class, long.class},
                    plugin, consumer(task), null, delayTicks);
            return;
        }

        runGlobalLater(task, delayTicks);
    }

    private Consumer<Object> consumer(Runnable task) {
        return scheduledTask -> task.run();
    }

    private void invokeFoliaScheduler(Object source, String schedulerGetter, String schedulerMethod, Class<?>[] parameterTypes, Object... args) {
        try {
            Method getter = source.getClass().getMethod(schedulerGetter);
            Object scheduler = getter.invoke(source);
            Method method = scheduler.getClass().getMethod(schedulerMethod, parameterTypes);
            method.invoke(scheduler, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to run task with Folia scheduler " + schedulerGetter + "#" + schedulerMethod, e);
        }
    }

    private void invokeEntityScheduler(Entity entity, String schedulerMethod, Class<?>[] parameterTypes, Object... args) {
        try {
            Method getter = entity.getClass().getMethod("getScheduler");
            Object scheduler = getter.invoke(entity);
            Method method = scheduler.getClass().getMethod(schedulerMethod, parameterTypes);
            method.invoke(scheduler, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to run task with Folia entity scheduler " + schedulerMethod, e);
        }
    }
}
