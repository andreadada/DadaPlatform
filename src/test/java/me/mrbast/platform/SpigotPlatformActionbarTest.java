package me.mrbast.platform;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpigotPlatformActionbarTest {

    @Test
    void legacyActionbarUsesActionbarChannelInsteadOfChat() {
        Player player = mock(Player.class);
        Player.Spigot spigot = mock(Player.Spigot.class);
        when(player.spigot()).thenReturn(spigot);

        SpigotPlatform.sendLegacyActionbar(player, "Now you can craft serious items");

        verify(spigot).sendMessage(eq(ChatMessageType.ACTION_BAR), any(BaseComponent[].class));
        verify(player, never()).sendMessage(any(String.class));
    }
}
