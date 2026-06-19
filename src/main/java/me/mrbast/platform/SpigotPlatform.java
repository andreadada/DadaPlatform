package me.mrbast.platform;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.CharacterAndFormat;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SpigotPlatform extends Platform {


    private String platform;

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character('\u00A7')
            .hexCharacter('#')
            .hexColors()
            .extractUrls()
            .useUnusualXRepeatedCharacterHexFormat()
            .flattener(ComponentFlattener.basic())
            .formats(CharacterAndFormat.defaults())
            .build();

    private static final MiniMessage MM = MiniMessage.miniMessage();

    public SpigotPlatform(String string) {
        this.platform = string;
    }

    @Override
    public String format(String text) {
        return serializer().serialize(toComponent(text));
    }


    public Component toComponent(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        return MM.deserialize(text);
    }


    public String getLabel() {
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
        sendLegacyActionbar(player, format(message));
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        sendLegacyTitle(player, format(title), format(subtitle), fadeIn, stay, fadeOut);
    }


    public LegacyComponentSerializer serializer() {
        return SERIALIZER;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void sendLegacyActionbar(Player player, String message) {
        try {
            Class<?> chatMessageTypeClass = Class.forName("net.md_5.bungee.api.ChatMessageType");
            Class<?> textComponentClass = Class.forName("net.md_5.bungee.api.chat.TextComponent");
            Class<?> baseComponentClass = Class.forName("net.md_5.bungee.api.chat.BaseComponent");
            Class<?> baseComponentArrayClass = Array.newInstance(baseComponentClass, 0).getClass();
            Object actionBar = Enum.valueOf((Class<Enum>) chatMessageTypeClass.asSubclass(Enum.class), "ACTION_BAR");
            Object components = textComponentClass.getMethod("fromLegacyText", String.class).invoke(null, message);
            Object spigot = player.spigot();
            Method sendMessage = spigot.getClass().getMethod("sendMessage", chatMessageTypeClass, baseComponentArrayClass);
            sendMessage.invoke(spigot, actionBar, components);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            player.sendMessage(message);
        }
    }

    public static void sendLegacyTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        try {
            Method sendTitle = player.getClass().getMethod("sendTitle", String.class, String.class, int.class, int.class, int.class);
            sendTitle.invoke(player, title, subtitle, fadeIn, stay, fadeOut);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            if (title != null && !title.isEmpty()) {
                player.sendMessage(title);
            }
            if (subtitle != null && !subtitle.isEmpty()) {
                player.sendMessage(subtitle);
            }
        }
    }


    @Override
    public String toString() {
        return "SpigotPlatform{" +
                "platform='" + platform + '\'' +
                '}';
    }
}
