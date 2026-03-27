package me.mrbast.platform;

import me.mrbast.platform.format.Formatter;
import me.mrbast.platform.format.FormatterBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public abstract class Platform {


    private FormatterBuilder formatterBuilder;


    public static Platform prepare(Plugin plugin){

        Platform platform = new SpigotPlatform();
        try {
            Class.forName("io.papermc.paper.math.BlockPosition");
            platform = new PaperPlatform();
        } catch (ClassNotFoundException e) {

        }
        plugin.getLogger().info("Working with: " + platform.getLabel());
        Platform finalPlatform = platform;
        platform.setFormatterBuilder((String text) -> new Formatter(finalPlatform, text));
        return platform;
    }

    private void setFormatterBuilder(FormatterBuilder formatterBuilder) {
        this.formatterBuilder = formatterBuilder;
    }

    public Formatter getFormatter(String text) {
        return formatterBuilder.getFormatter(text);
    }

    public abstract String getLabel();

    public abstract String format(String text);
    public abstract void sendMessage(Player player, String message);
    public abstract void sendMessage(CommandSender sender, String message);
    public abstract void sendActionbar(Player player, String message);

}
