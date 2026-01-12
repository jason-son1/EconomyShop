package me.antigravity.economyshop.manager;

import me.antigravity.economyshop.EconomyShop;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class LangManager {

    private final EconomyShop plugin;
    private FileConfiguration messagesConfig;
    private File messagesFile;

    public LangManager(EconomyShop plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String path) {
        String message = messagesConfig.getString(path);
        if (message == null) {
            return "Message not found: " + path;
        }
        String prefix = messagesConfig.getString("prefix", "&a[EconomyShop] &f");
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }

    public String getRawMessage(String path) {
        String message = messagesConfig.getString(path);
        if (message == null)
            return path;
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
