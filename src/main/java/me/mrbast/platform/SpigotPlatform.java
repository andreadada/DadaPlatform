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
        return isFolia() ? "Folia (legacy message bridge)" : "Spigot or Paper 1.16.5-";
    }


    @Override
    public void sendMessage(Player player, String message) {
        if (isFolia()) {
            scheduler().runFor(player, () -> player.sendMessage(format(message)));
            return;
        }
        player.sendMessage(format(message));
    }

    @Override
    public void sendMessage(CommandSender sender, String message) {
        if (isFolia()) {
            if (sender instanceof Player) {
                sendMessage((Player) sender, message);
            } else {
                scheduler().runGlobal(() -> sender.sendMessage(format(message)));
            }
            return;
        }
        sender.sendMessage(format(message));
    }

    @Override
    public void sendActionbar(Player player, String message) {
        if (isFolia()) {
            scheduler().runFor(player, () -> sendLegacyActionbar(player, format(message)));
            return;
        }
        sendLegacyActionbar(player, format(message));
    }

    @Override
    public void sendActionbar(Player player, String message, String font) {
        sendLegacyActionbar(player, format(message), font);
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (isFolia()) {
            scheduler().runFor(player, () -> sendLegacyTitle(player, format(title), format(subtitle), fadeIn, stay, fadeOut));
            return;
        }
        sendLegacyTitle(player, format(title), format(subtitle), fadeIn, stay, fadeOut);
    }


    public LegacyComponentSerializer serializer() {
        return SERIALIZER;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void sendLegacyActionbar(Player player, String message) {
        sendLegacyActionbar(player, message, null);
    }

    public static void sendLegacyActionbar(Player player, String message, String font) {
        if (font != null && !font.trim().isEmpty() && sendNativeActionbar(player, message, font)) {
            return;
        }
        if (font != null && !font.trim().isEmpty() && sendLegacyNativeActionbar(player, message, font)) {
            return;
        }
        if (font != null && !font.trim().isEmpty() && sendBungeeFontActionbar(player, message, font)) {
            return;
        }
        try {
            Method sendMessage = findActionbarMethod();
            Class<?> chatMessageTypeClass = sendMessage.getParameterTypes()[0];
            Class<?> baseComponentArrayClass = sendMessage.getParameterTypes()[1];
            Class<?> baseComponentClass = baseComponentArrayClass.getComponentType();
            ClassLoader serverClassLoader = chatMessageTypeClass.getClassLoader();
            Class<?> textComponentClass = Class.forName("net.md_5.bungee.api.chat.TextComponent", true, serverClassLoader);
            Object actionBar = Enum.valueOf((Class<Enum>) chatMessageTypeClass.asSubclass(Enum.class), "ACTION_BAR");
            Object components = textComponentClass.getMethod("fromLegacyText", String.class).invoke(null, message);
            if (font != null && !font.trim().isEmpty()) {
                Method setFont = baseComponentClass.getMethod("setFont", String.class);
                int length = Array.getLength(components);
                for (int index = 0; index < length; index++) {
                    setFont.invoke(Array.get(components, index), font);
                }
            }
            sendMessage.invoke(player.spigot(), actionBar, components);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            // Action bars are optional UI. Never turn a repeated prompt into chat spam.
        }
    }

    /**
     * Bungee components preserve the font property in the action-bar packet on modern Spigot.
     * Keep the NMS implementation below as a fallback for server implementations without it.
     */
    private static boolean sendBungeeFontActionbar(Player player, String message, String font) {
        try {
            ClassLoader loader = Player.Spigot.class.getClassLoader();
            Class<?> baseComponentClass = Class.forName("net.md_5.bungee.api.chat.BaseComponent", true, loader);
            Class<?> textComponentClass = Class.forName("net.md_5.bungee.api.chat.TextComponent", true, loader);
            Object component = textComponentClass.getConstructor(String.class).newInstance(message);
            baseComponentClass.getMethod("setFont", String.class).invoke(component, font);

            Object components = Array.newInstance(baseComponentClass, 1);
            Array.set(components, 0, component);
            Class<?> actionBarClass = Class.forName("net.md_5.bungee.api.ChatMessageType", true, loader);
            Object actionBar = Enum.valueOf((Class<? extends Enum>) actionBarClass, "ACTION_BAR");
            findActionbarMethod().invoke(player.spigot(), actionBar, components);
            return true;
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException
                 | InvocationTargetException | IllegalArgumentException e) {
            return false;
        }
    }

    static boolean sendNativeActionbar(Player player, String message, String font) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            ClassLoader loader = handle.getClass().getClassLoader();
            Class<?> componentClass = Class.forName("net.minecraft.network.chat.Component", true, loader);
            Class<?> mutableComponentClass = Class.forName("net.minecraft.network.chat.MutableComponent", true, loader);
            Class<?> styleClass = Class.forName("net.minecraft.network.chat.Style", true, loader);
            Class<?> identifierClass = Class.forName("net.minecraft.resources.Identifier", true, loader);
            Class<?> fontDescriptionClass = Class.forName("net.minecraft.network.chat.FontDescription", true, loader);
            Class<?> resourceFontClass = Class.forName("net.minecraft.network.chat.FontDescription$Resource", true, loader);

            Object component = componentClass.getMethod("literal", String.class).invoke(null, message);
            Object fontId = identifierClass.getMethod("parse", String.class).invoke(null, font);
            Object fontDescription = resourceFontClass.getConstructor(identifierClass).newInstance(fontId);
            Object style = mutableComponentClass.getMethod("getStyle").invoke(component);
            Object styled = styleClass.getMethod("withFont", fontDescriptionClass).invoke(style, fontDescription);
            mutableComponentClass.getMethod("withStyle", styleClass).invoke(component, styled);

            Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket", true, loader);
            Object packet = packetClass.getConstructor(componentClass).newInstance(component);
            Object connection = handle.getClass().getField("connection").get(handle);
            for (Method method : connection.getClass().getMethods()) {
                Class<?>[] parameters = method.getParameterTypes();
                if (method.getName().equals("send") && parameters.length == 1
                        && parameters[0].isAssignableFrom(packetClass)) {
                    method.invoke(connection, packet);
                    return true;
                }
            }
        } catch (ReflectiveOperationException | IllegalArgumentException | LinkageError ignored) {
            // Older servers continue through the Bungee action-bar implementation below.
        }
        return false;
    }

    private static boolean sendLegacyNativeActionbar(Player player, String message, String font) {
        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            ClassLoader loader = handle.getClass().getClassLoader();
            Class<?> componentClass = Class.forName("net.minecraft.network.chat.IChatBaseComponent", true, loader);
            Class<?> serializerClass = Class.forName("net.minecraft.network.chat.IChatBaseComponent$ChatSerializer", true, loader);
            String json = "{\"text\":\"" + escapeJson(message) + "\",\"font\":\"" + escapeJson(font) + "\"}";
            Object component = serializerClass.getMethod("a", String.class).invoke(null, json);

            Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket", true, loader);
            Object packet = packetClass.getConstructor(componentClass).newInstance(component);
            Object connection = handle.getClass().getField("b").get(handle);
            for (Method method : connection.getClass().getMethods()) {
                Class<?>[] parameters = method.getParameterTypes();
                if (method.getName().equals("a") && parameters.length == 1
                        && parameters[0].isAssignableFrom(packetClass)) {
                    method.invoke(connection, packet);
                    return true;
                }
            }
        } catch (ReflectiveOperationException | IllegalArgumentException | LinkageError ignored) {
            // Spigot 1.19.4 uses these legacy Mojang names; newer servers use the method above.
        }
        return false;
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static Method findActionbarMethod() throws NoSuchMethodException {
        for (Method method : Player.Spigot.class.getMethods()) {
            Class<?>[] parameters = method.getParameterTypes();
            if (method.getName().equals("sendMessage") && parameters.length == 2
                    && parameters[0].isEnum() && parameters[0].getName().equals("net.md_5.bungee.api.ChatMessageType")
                    && parameters[1].isArray() && parameters[1].getComponentType().getName()
                    .equals("net.md_5.bungee.api.chat.BaseComponent")) {
                return method;
            }
        }
        throw new NoSuchMethodException("Player.Spigot action-bar sender");
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
