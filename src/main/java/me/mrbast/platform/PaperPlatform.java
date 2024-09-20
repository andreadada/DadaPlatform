package me.mrbast.platform;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PaperPlatform extends Platform{

    private static final MiniMessage mm = MiniMessage.miniMessage();

    public static Component toComponent(String text){
        return mm.deserialize(text);
    }


    @Override
    public String format(String text){
        return serializer().serialize(toComponent(text));
    }

    @Override
    public void sendMessage(Player player, String message) {
        player.sendMessage(format(message));
    }

    public void sendMessage(CommandSender sender, String message) {

        sender.sendMessage(format(message));
    }

    @Override
    public void sendActionbar(Player player, String message) {

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(format(message)));

    }


    public String getLabel(){
        return "Paper 1.17+";
    }

    public net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer serializer() {
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.builder()
                .character('§')
                .hexCharacter('#')
                .hexColors()
                .extractUrls()
                .useUnusualXRepeatedCharacterHexFormat()
                .flattener(ComponentFlattener.basic())
                .build();
    }
}
