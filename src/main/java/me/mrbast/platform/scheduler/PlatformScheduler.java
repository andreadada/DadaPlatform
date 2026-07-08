package me.mrbast.platform.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Scheduler facade that preserves the classic Bukkit implementation while
 * routing location and entity work to the owning Folia region when available.
 */
public class PlatformScheduler {

    private final Plugin plugin;
    private final boolean folia;

    public PlatformScheduler(Plugin plugin, boolean folia) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.folia = folia;
    }

    public boolean isFolia() {
        return folia;
    }

    public void runGlobal(Runnable task) {
        scheduleGlobal(task);
    }

    public PlatformTask scheduleGlobal(Runnable task) {
        requireTask(task);
        if (folia) {
            return reflected(invokeScheduler(Bukkit.getServer(), "getGlobalRegionScheduler", "run",
                    new Class<?>[]{Plugin.class, Consumer.class}, plugin, consumer(task)));
        }
        return bukkit(Bukkit.getScheduler().runTask(plugin, task));
    }

    public void runGlobalLater(Runnable task, long delayTicks) {
        scheduleGlobalLater(task, delayTicks);
    }

    public PlatformTask scheduleGlobalLater(Runnable task, long delayTicks) {
        requireTask(task);
        if (delayTicks <= 0) return scheduleGlobal(task);
        if (folia) {
            return reflected(invokeScheduler(Bukkit.getServer(), "getGlobalRegionScheduler", "runDelayed",
                    new Class<?>[]{Plugin.class, Consumer.class, long.class},
                    plugin, consumer(task), delayTicks));
        }
        return bukkit(Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks));
    }

    public PlatformTask scheduleGlobalRepeating(Runnable task, long initialDelayTicks, long periodTicks) {
        validateRepeating(task, periodTicks);
        long initialDelay = Math.max(1L, initialDelayTicks);
        if (folia) {
            return reflected(invokeScheduler(Bukkit.getServer(), "getGlobalRegionScheduler", "runAtFixedRate",
                    new Class<?>[]{Plugin.class, Consumer.class, long.class, long.class},
                    plugin, consumer(task), initialDelay, periodTicks));
        }
        return bukkit(Bukkit.getScheduler().runTaskTimer(plugin, task, initialDelay, periodTicks));
    }

    public void runAsync(Runnable task) {
        scheduleAsync(task);
    }

    public PlatformTask scheduleAsync(Runnable task) {
        requireTask(task);
        if (folia) {
            return reflected(invokeScheduler(Bukkit.getServer(), "getAsyncScheduler", "runNow",
                    new Class<?>[]{Plugin.class, Consumer.class}, plugin, consumer(task)));
        }
        return bukkit(Bukkit.getScheduler().runTaskAsynchronously(plugin, task));
    }

    public void runAsyncLater(Runnable task, long delayTicks) {
        scheduleAsyncLater(task, delayTicks);
    }

    public PlatformTask scheduleAsyncLater(Runnable task, long delayTicks) {
        requireTask(task);
        if (delayTicks <= 0) return scheduleAsync(task);
        if (folia) {
            return reflected(invokeScheduler(Bukkit.getServer(), "getAsyncScheduler", "runDelayed",
                    new Class<?>[]{Plugin.class, Consumer.class, long.class, TimeUnit.class},
                    plugin, consumer(task), ticksToMillis(delayTicks), TimeUnit.MILLISECONDS));
        }
        return bukkit(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks));
    }

    public PlatformTask scheduleAsyncRepeating(Runnable task, long initialDelayTicks, long periodTicks) {
        validateRepeating(task, periodTicks);
        long initialDelay = Math.max(1L, initialDelayTicks);
        if (folia) {
            return reflected(invokeScheduler(Bukkit.getServer(), "getAsyncScheduler", "runAtFixedRate",
                    new Class<?>[]{Plugin.class, Consumer.class, long.class, long.class, TimeUnit.class},
                    plugin, consumer(task), ticksToMillis(initialDelay), ticksToMillis(periodTicks), TimeUnit.MILLISECONDS));
        }
        return bukkit(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, initialDelay, periodTicks));
    }

    public void runAt(Location location, Runnable task) {
        scheduleAt(location, task);
    }

    public PlatformTask scheduleAt(Location location, Runnable task) {
        requireLocationAndTask(location, task);
        if (folia) {
            return reflected(invokeScheduler(Bukkit.getServer(), "getRegionScheduler", "run",
                    new Class<?>[]{Plugin.class, Location.class, Consumer.class},
                    plugin, location, consumer(task)));
        }
        return scheduleGlobal(task);
    }

    public void runAtLater(Location location, Runnable task, long delayTicks) {
        scheduleAtLater(location, task, delayTicks);
    }

    public PlatformTask scheduleAtLater(Location location, Runnable task, long delayTicks) {
        requireLocationAndTask(location, task);
        if (delayTicks <= 0) return scheduleAt(location, task);
        if (folia) {
            return reflected(invokeScheduler(Bukkit.getServer(), "getRegionScheduler", "runDelayed",
                    new Class<?>[]{Plugin.class, Location.class, Consumer.class, long.class},
                    plugin, location, consumer(task), delayTicks));
        }
        return scheduleGlobalLater(task, delayTicks);
    }

    public PlatformTask scheduleAtRepeating(Location location, Runnable task,
                                            long initialDelayTicks, long periodTicks) {
        requireLocationAndTask(location, task);
        validatePeriod(periodTicks);
        long initialDelay = Math.max(1L, initialDelayTicks);
        if (folia) {
            return reflected(invokeScheduler(Bukkit.getServer(), "getRegionScheduler", "runAtFixedRate",
                    new Class<?>[]{Plugin.class, Location.class, Consumer.class, long.class, long.class},
                    plugin, location, consumer(task), initialDelay, periodTicks));
        }
        return scheduleGlobalRepeating(task, initialDelay, periodTicks);
    }

    public void runFor(Entity entity, Runnable task) {
        scheduleFor(entity, task);
    }

    public PlatformTask scheduleFor(Entity entity, Runnable task) {
        requireEntityAndTask(entity, task);
        if (folia) {
            return reflected(invokeEntityScheduler(entity, "run",
                    new Class<?>[]{Plugin.class, Consumer.class, Runnable.class},
                    plugin, consumer(task), null));
        }
        return scheduleGlobal(task);
    }

    public void runForLater(Entity entity, Runnable task, long delayTicks) {
        scheduleForLater(entity, task, delayTicks);
    }

    public PlatformTask scheduleForLater(Entity entity, Runnable task, long delayTicks) {
        requireEntityAndTask(entity, task);
        if (delayTicks <= 0) return scheduleFor(entity, task);
        if (folia) {
            return reflected(invokeEntityScheduler(entity, "runDelayed",
                    new Class<?>[]{Plugin.class, Consumer.class, Runnable.class, long.class},
                    plugin, consumer(task), null, delayTicks));
        }
        return scheduleGlobalLater(task, delayTicks);
    }

    public PlatformTask scheduleForRepeating(Entity entity, Runnable task,
                                             long initialDelayTicks, long periodTicks) {
        requireEntityAndTask(entity, task);
        validatePeriod(periodTicks);
        long initialDelay = Math.max(1L, initialDelayTicks);
        if (folia) {
            return reflected(invokeEntityScheduler(entity, "runAtFixedRate",
                    new Class<?>[]{Plugin.class, Consumer.class, Runnable.class, long.class, long.class},
                    plugin, consumer(task), null, initialDelay, periodTicks));
        }
        return scheduleGlobalRepeating(task, initialDelay, periodTicks);
    }

    private Object invokeScheduler(Object source, String schedulerGetter, String schedulerMethod,
                                   Class<?>[] parameterTypes, Object... args) {
        try {
            Method getter = source.getClass().getMethod(schedulerGetter);
            Object scheduler = getter.invoke(source);
            Method method = scheduler.getClass().getMethod(schedulerMethod, parameterTypes);
            return method.invoke(scheduler, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Unable to run task with Folia scheduler "
                    + schedulerGetter + "#" + schedulerMethod, unwrap(exception));
        }
    }

    private Object invokeEntityScheduler(Entity entity, String schedulerMethod,
                                         Class<?>[] parameterTypes, Object... args) {
        try {
            Method getter = entity.getClass().getMethod("getScheduler");
            Object scheduler = getter.invoke(entity);
            Class<?> schedulerType = Class.forName(
                    "io.papermc.paper.threadedregions.scheduler.EntityScheduler");
            Method method = schedulerType.getMethod(schedulerMethod, parameterTypes);
            return method.invoke(scheduler, args);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to run task with Folia entity scheduler "
                    + schedulerMethod, unwrap(exception));
        }
    }

    private static Throwable unwrap(Exception exception) {
        if (exception instanceof InvocationTargetException
                && ((InvocationTargetException) exception).getCause() != null) {
            return ((InvocationTargetException) exception).getCause();
        }
        return exception;
    }

    private static Consumer<Object> consumer(Runnable task) {
        return scheduledTask -> task.run();
    }

    private static long ticksToMillis(long ticks) {
        return Math.multiplyExact(ticks, 50L);
    }

    private static void requireTask(Runnable task) {
        Objects.requireNonNull(task, "task");
    }

    private static void requireLocationAndTask(Location location, Runnable task) {
        Objects.requireNonNull(location, "location");
        requireTask(task);
    }

    private static void requireEntityAndTask(Entity entity, Runnable task) {
        Objects.requireNonNull(entity, "entity");
        requireTask(task);
    }

    private static void validateRepeating(Runnable task, long periodTicks) {
        requireTask(task);
        validatePeriod(periodTicks);
    }

    private static void validatePeriod(long periodTicks) {
        if (periodTicks <= 0) throw new IllegalArgumentException("periodTicks must be positive");
    }

    private static PlatformTask bukkit(BukkitTask task) {
        return new BukkitPlatformTask(task);
    }

    private static PlatformTask reflected(Object task) {
        if (task == null) return CompletedPlatformTask.INSTANCE;
        return new ReflectedPlatformTask(task);
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

    private static final class ReflectedPlatformTask implements PlatformTask {
        private final Object task;
        private final AtomicBoolean cancelled = new AtomicBoolean();

        private ReflectedPlatformTask(Object task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            if (!cancelled.compareAndSet(false, true)) return;
            try {
                Class<?> scheduledTask = Class.forName(
                        "io.papermc.paper.threadedregions.scheduler.ScheduledTask");
                scheduledTask.getMethod("cancel").invoke(task);
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Unable to cancel Folia task", exception);
            }
        }

        @Override
        public boolean isCancelled() {
            return cancelled.get();
        }
    }

    private enum CompletedPlatformTask implements PlatformTask {
        INSTANCE;

        @Override
        public void cancel() {
        }

        @Override
        public boolean isCancelled() {
            return true;
        }
    }
}
