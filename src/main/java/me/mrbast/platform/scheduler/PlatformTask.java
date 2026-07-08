package me.mrbast.platform.scheduler;

/** A scheduler-neutral handle that can cancel Bukkit or Folia tasks. */
public interface PlatformTask {
    void cancel();

    boolean isCancelled();
}
