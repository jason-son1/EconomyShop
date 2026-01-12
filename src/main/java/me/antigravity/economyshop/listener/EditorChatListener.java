package me.antigravity.economyshop.listener;

import me.antigravity.economyshop.EconomyShop;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class EditorChatListener implements Listener {

    private final EconomyShop plugin;

    public EditorChatListener(EconomyShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (plugin.getEditorManager().hasChatSession(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);

            // Sync-task로 처리하여 메인 스레드에서 안전하게 로직 수행
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getEditorManager().handleChat(event.getPlayer().getUniqueId(), event.getMessage());
                }
            }.runTask(plugin);
        }
    }
}
