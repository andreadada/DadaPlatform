package me.mrbast.platform;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public abstract class Platform {


    public static Platform prepare(Plugin plugin){

        Platform platform = new SpigotPlatform();
        try {
            Class.forName("io.papermc.paper.math.BlockPosition");
            platform = new PaperPlatform();
        } catch (ClassNotFoundException e) {

        }
        plugin.getLogger().info("Working with: " + platform.getLabel());
        return platform;
    }

    public abstract String getLabel();

    public abstract String format(String text);
    public abstract void sendMessage(Player player, String message);
    public abstract void sendMessage(CommandSender sender, String message);

    public abstract void sendActionbar(Player player, String message);

}
