package me.mrbast.platform.format;

import me.mrbast.platform.Platform;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.function.Supplier;

/***
 * Used to format string
 */
public  class Formatter{

    private final Platform platform;

    private final String message;

    public Formatter(Platform platform, String message) {
        this.platform = platform;
        this.message = message;
    }

    /*
    public Formatter setText(String text){
        this.message = text;
        return this;
    }

     */

    public void send(Format format, Player... players){

        String formatted = format(format);

        for (Player player : players) {
            platform.sendMessage(player, formatted);
        }
    }
    public String format(Format format){
        return formatText(message, format);
    }

    private String formatText(String text, Format format){
        String formatted = text;
        if(formatted == null || format == null) return formatted;
        for(Map.Entry<String, Supplier<String>> pair : format.getValues().entrySet()){
            formatted = formatted.replace("%"+pair.getKey()+"%", pair.getValue().get());
        }

        return formatted;
    }

    public void send(CommandSender... senders) {
        for (CommandSender sender : senders) {
            platform.sendMessage(sender, message);
        }
    }

    public void send(Player... players){
        for (Player player : players) {
            platform.sendMessage(player, message);
        }
    }

    public void sendActionbar(Player... players){
        for (Player player : players) {
            platform.sendActionbar(player, message);
        }
    }

    public void sendActionbar(Format format, Player... players){
        String formatted = format(format);
        for (Player player : players) {
            platform.sendActionbar(player, formatted);
        }
    }

    public void sendActionBar(Player... players){
        sendActionbar(players);
    }

    public void sendActionBar(Format format, Player... players){
        sendActionbar(format, players);
    }

    public void sendTitle(String subtitle, Player... players){
        sendTitle(subtitle, Platform.DEFAULT_TITLE_FADE_IN, Platform.DEFAULT_TITLE_STAY, Platform.DEFAULT_TITLE_FADE_OUT, players);
    }

    public void sendTitle(Format format, String subtitle, Player... players){
        sendTitle(format, subtitle, Platform.DEFAULT_TITLE_FADE_IN, Platform.DEFAULT_TITLE_STAY, Platform.DEFAULT_TITLE_FADE_OUT, players);
    }

    public void sendTitle(String subtitle, int fadeIn, int stay, int fadeOut, Player... players){
        for (Player player : players) {
            platform.sendTitle(player, message, subtitle, fadeIn, stay, fadeOut);
        }
    }

    public void sendTitle(Format format, String subtitle, int fadeIn, int stay, int fadeOut, Player... players){
        String formattedTitle = format(format);
        String formattedSubtitle = formatText(subtitle, format);
        for (Player player : players) {
            platform.sendTitle(player, formattedTitle, formattedSubtitle, fadeIn, stay, fadeOut);
        }
    }

    public String get(){
        return platform.format(message);
    }

    public String get(Format format){
        return platform.format(format(format));
    }

    public String getClean(){
        return this.message;
    }



    public void send(Format name, CommandSender sender) {
        platform.sendMessage(sender, format(name));
    }


    @Override
    public String toString() {
        return "Formatter{" +
                "platform=" + platform +
                ", message='" + message + '\'' +
                '}';
    }
}
