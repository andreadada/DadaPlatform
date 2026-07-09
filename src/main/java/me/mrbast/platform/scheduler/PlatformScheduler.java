package me.mrbast.platform.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

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
        scheduleGlobal(task);
    }

    public PlatformTask scheduleGlobal(Runnable task) {
        if (folia) {
            return task(invokeFoliaScheduler(Bukkit.getServer(), "getGlobalRegionScheduler", "run",
                    new Class<?>[]{Plugin.class, Consumer.class},
                    plugin, consumer(task)));
        }

        return task(Bukkit.getScheduler().runTask(plugin, task));
    }

    public void runGlobalLater(Runnable task, long delayTicks) {
        scheduleGlobalLater(task, delayTicks);
    }

    public PlatformTask scheduleGlobalLater(Runnable task, long delayTicks) {
        if (delayTicks <= 0) {
            return scheduleGlobal(task);
        }

        if (folia) {
            return task(invokeFoliaScheduler(Bukkit.getServer(), "getGlobalRegionScheduler", "runDelayed",
                    new Class<?>[]{Plugin.class, Consumer.class, long.class},
                    plugin, consumer(task), delayTicks));
        }

        return task(Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks));
    }

    public PlatformTask scheduleGlobalRepeating(Runnable task, long initialDelayTicks, long periodTicks) {
        if (folia) {
            return task(invokeFoliaScheduler(Bukkit.getServer(), "getGlobalRegionScheduler", "runAtFixedRate",
                    new Class<?>[]{Plugin.class, Consumer.class, long.class, long.class},
                    plugin, consumer(task), Math.max(1L, initialDelayTicks), Math.max(1L, periodTicks)));
        }

        return task(Bukkit.getScheduler().runTaskTimer(plugin, task, initialDelayTicks, periodTicks));
    }

    public void runAsync(Runnable task) {
        scheduleAsync(task);
    }

    public PlatformTask scheduleAsync(Runnable task) {
        if (folia) {
            return task(invokeFoliaScheduler(Bukkit.getServer(), "getAsyncScheduler", "runNow",
                    new Class<?>[]{Plugin.class, Consumer.class},
                    plugin, consumer(task)));
        }

        return task(Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }

    public void runAsyncLater(Runnable task, long delayTicks) {
        scheduleAsyncLater(task, delayTicks);
    }

    public PlatformTask scheduleAsyncLater(Runnable task, long delayTicks) {
        if (delayTicks <= 0) {
            return scheduleAsync(task);
        }

        if (folia) {
            return task(invokeFoliaScheduler(Bukkit.getServer(), "getAsyncScheduler", "runDelayed",
                    new Class<?>[]{Plugin.class, Consumer.class, long.class, TimeUnit.class},
                    plugin, consumer(task), delayTicks * 50L, TimeUnit.MILLISECONDS));
        }

        return task(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks));
    }

    public PlatformTask scheduleAsyncRepeating(Runnable task, long initialDelayTicks, long periodTicks) {
        if (folia) {
            return task(invokeFoliaScheduler(Bukkit.getServer(), "getAsyncScheduler", "runAtFixedRate",
                    new Class<?>[]{Plugin.class, Consumer.class, long.class, long.class, TimeUnit.class},
                    plugin, consumer(task), Math.max(1L, initialDelayTicks) * 50L, Math.max(1L, periodTicks) * 50L, TimeUnit.MILLISECONDS));
        }

        return task(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, initialDelayTicks, periodTicks));
    }

    public void runAt(Location location, Runnable task) {
        scheduleAt(location, task);
    }

    public PlatformTask scheduleAt(Location location, Runnable task) {
        if (folia) {
            return task(invokeFoliaScheduler(Bukkit.getServer(), "getRegionScheduler", "run",
                    new Class<?>[]{Plugin.class, Location.class, Consumer.class},
                    plugin, location, consumer(task)));
        }

        return scheduleGlobal(task);
    }

    public void runAtLater(Location location, Runnable task, long delayTicks) {
        scheduleAtLater(location, task, delayTicks);
    }

    public PlatformTask scheduleAtLater(Location location, Runnable task, long delayTicks) {
        if (delayTicks <= 0) {
            return scheduleAt(location, task);
        }

        if (folia) {
            return task(invokeFoliaScheduler(Bukkit.getServer(), "getRegionScheduler", "runDelayed",
                    new Class<?>[]{Plugin.class, Location.class, Consumer.class, long.class},
                    plugin, location, consumer(task), delayTicks));
        }

        return scheduleGlobalLater(task, delayTicks);
    }

    public PlatformTask scheduleAtRepeating(Location location, Runnable task, long initialDelayTicks, long periodTicks) {
        if (folia) {
            return task(invokeFoliaScheduler(Bukkit.getServer(), "getRegionScheduler", "runAtFixedRate",
                    new Class<?>[]{Plugin.class, Location.class, Consumer.class, long.class, long.class},
                    plugin, location, consumer(task), Math.max(1L, initialDelayTicks), Math.max(1L, periodTicks)));
        }

        return scheduleGlobalRepeating(task, initialDelayTicks, periodTicks);
    }

    public void runFor(Entity entity, Runnable task) {
        scheduleFor(entity, task);
    }

    public PlatformTask scheduleFor(Entity entity, Runnable task) {
        if (folia) {
            return task(invokeEntityScheduler(entity, "run",
                    new Class<?>[]{Plugin.class, Consumer.class, Runnable.class},
                    plugin, consumer(task), retired()));
        }

        return scheduleGlobal(task);
    }

    public void runForLater(Entity entity, Runnable task, long delayTicks) {
        scheduleForLater(entity, task, delayTicks);
    }

    public PlatformTask scheduleForLater(Entity entity, Runnable task, long delayTicks) {
        if (delayTicks <= 0) {
            return scheduleFor(entity, task);
        }

        if (folia) {
            return task(invokeEntityScheduler(entity, "runDelayed",
                    new Class<?>[]{Plugin.class, Consumer.class, Runnable.class, long.class},
                    plugin, consumer(task), retired(), delayTicks));
        }

        return scheduleGlobalLater(task, delayTicks);
    }

    public PlatformTask scheduleForRepeating(Entity entity, Runnable task, long initialDelayTicks, long periodTicks) {
        if (folia) {
            return task(invokeEntityScheduler(entity, "runAtFixedRate",
                    new Class<?>[]{Plugin.class, Consumer.class, Runnable.class, long.class, long.class},
                    plugin, consumer(task), retired(), Math.max(1L, initialDelayTicks), Math.max(1L, periodTicks)));
        }

        return scheduleGlobalRepeating(task, initialDelayTicks, periodTicks);
    }

    private Consumer<Object> consumer(Runnable task) {
        return scheduledTask -> task.run();
    }

    private Runnable retired() {
        return () -> {
        };
    }

    private PlatformTask task(Object handle) {
        if (handle == null) {
            return NoopPlatformTask.INSTANCE;
        }
        if (handle instanceof BukkitTask) {
            return new BukkitPlatformTask((BukkitTask) handle);
        }
        return new ReflectivePlatformTask(handle);
    }

    private Object invokeFoliaScheduler(Object source, String schedulerGetter, String schedulerMethod, Class<?>[] parameterTypes, Object... args) {
        try {
            Method getter = source.getClass().getMethod(schedulerGetter);
            Object scheduler = getter.invoke(source);
            Method method = scheduler.getClass().getMethod(schedulerMethod, parameterTypes);
            return method.invoke(scheduler, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to run task with Folia scheduler " + schedulerGetter + "#" + schedulerMethod, e);
        }
    }

    private Object invokeEntityScheduler(Entity entity, String schedulerMethod, Class<?>[] parameterTypes, Object... args) {
        try {
            Method getter = entity.getClass().getMethod("getScheduler");
            Object scheduler = getter.invoke(entity);
            Method method = scheduler.getClass().getMethod(schedulerMethod, parameterTypes);
            return method.invoke(scheduler, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to run task with Folia entity scheduler " + schedulerMethod, e);
        }
    }

    private static final class BukkitPlatformTask implements PlatformTask {
        private final BukkitTask task;

        private BukkitPlatformTask(BukkitTask task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            task.cancel();
        }

        @Override
        public boolean isCancelled() {
            return task.isCancelled();
        }
    }

    private static final class ReflectivePlatformTask implements PlatformTask {
        private final Object task;

        private ReflectivePlatformTask(Object task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            invoke("cancel", false);
        }

        @Override
        public boolean isCancelled() {
            Object cancelled = invoke("isCancelled", true);
            return cancelled instanceof Boolean && (Boolean) cancelled;
        }

        private Object invoke(String methodName, boolean defaultValue) {
            try {
                return task.getClass().getMethod(methodName).invoke(task);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                return defaultValue;
            }
        }
    }

    private enum NoopPlatformTask implements PlatformTask {
        INSTANCE;

        @Override
        public void cancel() {
        }

        @Override
        public boolean isCancelled() {
            return false;
        }
    }
}
