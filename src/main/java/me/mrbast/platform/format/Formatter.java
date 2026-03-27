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

    private String message;

    public Formatter(Platform platform, String message) {
        this.platform = platform;
        this.message = message;
    }


    public Formatter setText(String text){
        this.message = text;
        return this;
    }

    public void send(Format format, Player... players){

        String formatted = message;
        for(Map.Entry<String, Supplier<String>> pair : format.getValues().entrySet()){
            formatted = formatted.replace("%"+pair.getKey()+"%", pair.getValue().get());
        }

        for (Player player : players) {
            platform.sendMessage(player, formatted);
        }
    }
    public String format(Format format){
        String formatted = message;
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

    public String get(){
        return platform.format(message);
    }

    public String get(Format format){
        return platform.format(format(format));
    }



    public void send(Format name, CommandSender sender) {
        platform.sendMessage(sender, format(name));
    }
}