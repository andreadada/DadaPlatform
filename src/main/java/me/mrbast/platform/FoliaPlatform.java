package me.mrbast.platform;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FoliaPlatform extends PaperPlatform {

    public FoliaPlatform(String txt) {
        super(txt);
    }

    @Override
    public void sendMessage(Player player, String message) {
        scheduler().runFor(player, () -> FoliaPlatform.super.sendMessage(player, message));
    }

    @Override
    public void sendMessage(CommandSender sender, String message) {
        if (sender instanceof Player) {
            sendMessage((Player) sender, message);
            return;
        }

        scheduler().runGlobal(() -> FoliaPlatform.super.sendMessage(sender, message));
    }

    @Override
    public void sendActionbar(Player player, String message) {
        scheduler().runFor(player, () -> FoliaPlatform.super.sendActionbar(player, message));
    }

    @Override
    public void sendActionbar(Player player, String message, String font) {
        scheduler().runFor(player, () -> FoliaPlatform.super.sendActionbar(player, message, font));
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        scheduler().runFor(player, () -> FoliaPlatform.super.sendTitle(player, title, subtitle, fadeIn, stay, fadeOut));
    }

    @Override
    public String getLabel() {
        return "Folia";
    }
}
