package me.mrbast.platform;

import me.mrbast.platform.format.Formatter;
import me.mrbast.platform.format.FormatterBuilder;
import me.mrbast.platform.scheduler.PlatformScheduler;
import me.mrbast.platform.scheduler.PlatformTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public abstract class Platform {

    public static final int DEFAULT_TITLE_FADE_IN = 10;
    public static final int DEFAULT_TITLE_STAY = 70;
    public static final int DEFAULT_TITLE_FADE_OUT = 20;

    private FormatterBuilder formatterBuilder;
    private Plugin plugin;
    private PlatformScheduler scheduler;
    private boolean folia;
    private boolean paperAdventure;


    public static Platform prepare(Plugin plugin){
        Server server = Bukkit.getServer();
        String name = server.getName();       // "CraftBukkit", "Spigot", "Paper", etc.
        String version = server.getVersion(); // Full string with implementation info
        String txt = "Platform: " + name + " | Version: " + version;
        boolean folia = isFoliaServer();
        boolean paperAdventure = supportsPaperAdventure();
        Platform platform = paperAdventure ? (folia ? new FoliaPlatform(txt) : new PaperPlatform(txt)) : new SpigotPlatform(txt);
        platform.configure(plugin, folia, paperAdventure);
        plugin.getLogger().info("Working with: " + platform.getLabel());
        Platform finalPlatform = platform;
        platform.setFormatterBuilder((String text) -> new Formatter(finalPlatform, text));
        return platform;
    }

    private void configure(Plugin plugin, boolean folia, boolean paperAdventure) {
        this.plugin = plugin;
        this.folia = folia;
        this.paperAdventure = paperAdventure;
        this.scheduler = new PlatformScheduler(plugin, folia);
    }

    private static boolean supportsPaperAdventure() {
        try {
            Class<?> componentClass = Class.forName("net.kyori.adventure.text.Component");
            Class<?> titleClass = Class.forName("net.kyori.adventure.title.Title");
            Player.class.getMethod("sendActionBar", componentClass);
            Player.class.getMethod("showTitle", titleClass);
            return true;
        } catch (ClassNotFoundException | NoSuchMethodException | LinkageError e) {
            return false;
        }
    }

    private static boolean isFoliaServer() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void setFormatterBuilder(FormatterBuilder formatterBuilder) {
        this.formatterBuilder = formatterBuilder;
    }

    public Formatter getFormatter(String text) {
        return formatterBuilder.getFormatter(text);
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public boolean isFolia() {
        return folia;
    }

    public boolean hasPaperAdventure() {
        return paperAdventure;
    }

    public PlatformScheduler scheduler() {
        return scheduler;
    }

    public void runGlobal(Runnable task) {
        scheduler.runGlobal(task);
    }

    public void runGlobalLater(Runnable task, long delayTicks) {
        scheduler.runGlobalLater(task, delayTicks);
    }

    public void runAsync(Runnable task) {
        scheduler.runAsync(task);
    }

    public void runAsyncLater(Runnable task, long delayTicks) {
        scheduler.runAsyncLater(task, delayTicks);
    }

    public void runAt(Location location, Runnable task) {
        scheduler.runAt(location, task);
    }

    public void runAtLater(Location location, Runnable task, long delayTicks) {
        scheduler.runAtLater(location, task, delayTicks);
    }

    public void runFor(Entity entity, Runnable task) {
        scheduler.runFor(entity, task);
    }

    public void runForLater(Entity entity, Runnable task, long delayTicks) {
        scheduler.runForLater(entity, task, delayTicks);
    }

    public PlatformTask scheduleGlobal(Runnable task) {
        return scheduler.scheduleGlobal(task);
    }

    public PlatformTask scheduleGlobalLater(Runnable task, long delayTicks) {
        return scheduler.scheduleGlobalLater(task, delayTicks);
    }

    public PlatformTask scheduleGlobalRepeating(Runnable task, long initialDelayTicks, long periodTicks) {
        return scheduler.scheduleGlobalRepeating(task, initialDelayTicks, periodTicks);
    }

    public PlatformTask scheduleAsync(Runnable task) {
        return scheduler.scheduleAsync(task);
    }

    public PlatformTask scheduleAsyncLater(Runnable task, long delayTicks) {
        return scheduler.scheduleAsyncLater(task, delayTicks);
    }

    public PlatformTask scheduleAsyncRepeating(Runnable task, long initialDelayTicks, long periodTicks) {
        return scheduler.scheduleAsyncRepeating(task, initialDelayTicks, periodTicks);
    }

    public PlatformTask scheduleAt(Location location, Runnable task) {
        return scheduler.scheduleAt(location, task);
    }

    public PlatformTask scheduleAtLater(Location location, Runnable task, long delayTicks) {
        return scheduler.scheduleAtLater(location, task, delayTicks);
    }

    public PlatformTask scheduleAtRepeating(Location location, Runnable task,
                                            long initialDelayTicks, long periodTicks) {
        return scheduler.scheduleAtRepeating(location, task, initialDelayTicks, periodTicks);
    }

    public PlatformTask scheduleFor(Entity entity, Runnable task) {
        return scheduler.scheduleFor(entity, task);
    }

    public PlatformTask scheduleForLater(Entity entity, Runnable task, long delayTicks) {
        return scheduler.scheduleForLater(entity, task, delayTicks);
    }

    public PlatformTask scheduleForRepeating(Entity entity, Runnable task,
                                             long initialDelayTicks, long periodTicks) {
        return scheduler.scheduleForRepeating(entity, task, initialDelayTicks, periodTicks);
    }

    public abstract String getLabel();

    public abstract String format(String text);
    public abstract void sendMessage(Player player, String message);
    public abstract void sendMessage(CommandSender sender, String message);
    public abstract void sendActionbar(Player player, String message);
    public abstract void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut);

    public void sendActionBar(Player player, String message) {
        sendActionbar(player, message);
    }

    public void sendTitle(Player player, String title, String subtitle) {
        sendTitle(player, title, subtitle, DEFAULT_TITLE_FADE_IN, DEFAULT_TITLE_STAY, DEFAULT_TITLE_FADE_OUT);
    }

}
