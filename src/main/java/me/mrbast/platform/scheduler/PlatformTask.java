package me.mrbast.platform.scheduler;

public interface PlatformTask {

    void cancel();

    boolean isCancelled();
}
