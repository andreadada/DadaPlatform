package me.mrbast.platform;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;

public class PaperPlatform extends Platform {

    private String platform;

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character('\u00A7')
            .hexCharacter('#')
            .hexColors()
            .extractUrls()
            .useUnusualXRepeatedCharacterHexFormat()
            .flattener(ComponentFlattener.basic())
            .build();


    private static final MiniMessage MM = MiniMessage.miniMessage();

    public PaperPlatform(String txt) {
        super();
        this.platform = txt;
    }

    public static Component toComponent(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        return MM.deserialize(text);
    }


    @Override
    public String format(String text) {
        return serializer().serialize(toComponent(text));
    }

    @Override
    public void sendMessage(Player player, String message) {
        if (!invokeAdventure(player, "sendMessage", new Class<?>[]{Component.class}, toComponent(message))) {
            player.sendMessage(format(message));
        }
    }

    public void sendMessage(CommandSender sender, String message) {
        if (!invokeAdventure(sender, "sendMessage", new Class<?>[]{Component.class}, toComponent(message))) {
            sender.sendMessage(format(message));
        }
    }

    @Override
    public void sendActionbar(Player player, String message) {
        if (!invokeAdventure(player, "sendActionBar", new Class<?>[]{Component.class}, toComponent(message))) {
            SpigotPlatform.sendLegacyActionbar(player, format(message));
        }
    }

    @Override
    public void sendActionbar(Player player, String message, String font) {
        String formatted = format(message);
        if (font != null && !font.trim().isEmpty() && SpigotPlatform.sendNativeActionbar(player, formatted, font)) {
            return;
        }
        sendActionbar(player, message);
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Title adventureTitle = Title.title(
                toComponent(title),
                toComponent(subtitle),
                Title.Times.times(toDuration(fadeIn), toDuration(stay), toDuration(fadeOut))
        );

        if (!invokeAdventure(player, "showTitle", new Class<?>[]{Title.class}, adventureTitle)) {
            SpigotPlatform.sendLegacyTitle(player, format(title), format(subtitle), fadeIn, stay, fadeOut);
        }
    }


    public String getLabel() {
        return "Paper 1.17+";
    }

    public LegacyComponentSerializer serializer() {
        return SERIALIZER;
    }

    private Duration toDuration(int ticks) {
        return Duration.ofMillis(Math.max(0, ticks) * 50L);
    }

    private boolean invokeAdventure(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = target.getClass().getMethod(methodName, parameterTypes);
            method.invoke(target, args);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to invoke Paper Adventure method " + methodName, e);
        }
    }

    @Override
    public String toString() {
        return "PaperPlatform{" +
                "platform='" + platform + '\'' +
                '}';
    }
}
