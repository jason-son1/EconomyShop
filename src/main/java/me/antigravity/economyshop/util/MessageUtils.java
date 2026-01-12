package me.antigravity.economyshop.util;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * ActionBar 및 Title 메시지 전송을 위한 유틸리티 클래스입니다.
 * 버전 호환성을 고려하여 작성되었습니다.
 */
public class MessageUtils {

    /**
     * 플레이어에게 액션바 메시지를 전송합니다.
     */
    public static void sendActionBar(Player player, String message) {
        if (player == null || message == null)
            return;
        String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);

        try {
            // 1.10+ (Spigot API)
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(coloredMessage));
        } catch (NoSuchMethodError e) {
            // 구버전 호환성 (필요 시 Reflection 구현)
            // 여기서는 Spigot API가 있는 환경을 가정합니다.
        }
    }

    /**
     * 플레이어에게 타이틀 메시지를 전송합니다.
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player == null)
            return;
        player.sendTitle(
                title != null ? ChatColor.translateAlternateColorCodes('&', title) : "",
                subtitle != null ? ChatColor.translateAlternateColorCodes('&', subtitle) : "",
                fadeIn, stay, fadeOut);
    }
}
