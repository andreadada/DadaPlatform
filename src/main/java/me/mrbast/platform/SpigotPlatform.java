package me.mrbast.platform;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.CharacterAndFormat;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpigotPlatform extends Platform{



    private static final MiniMessage mm = MiniMessage.miniMessage();

    public SpigotPlatform(){

    }

    @Override
    public String format(String text) {
        return serializer().serialize(toComponent(text));
    }


    public Component toComponent(String text) {
        return mm.deserialize(text);
    }


    public String getLabel(){
        return "Spigot or Paper 1.16.5-";
    }


    @Override
    public void sendMessage(Player player, String message) {
        player.sendMessage(format(message));
    }

    @Override
    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(format(message));
    }

    @Override
    public void sendActionbar(Player player, String message) {

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(format(message)));

    }


    public LegacyComponentSerializer serializer() {
        return LegacyComponentSerializer.builder()
                .character('§')
                .hexCharacter('#')
                .hexColors()
                .extractUrls()
                .useUnusualXRepeatedCharacterHexFormat()
                .flattener(ComponentFlattener.basic())
                .formats(CharacterAndFormat.defaults())
                .build();
    }




}
