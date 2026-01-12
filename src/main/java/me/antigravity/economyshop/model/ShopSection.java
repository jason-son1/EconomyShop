package me.antigravity.economyshop.model;

import lombok.Builder;
import lombok.Data;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Data
@Builder
public class ShopSection {
    private String id;
    private String displayName;
    private ItemStack icon;
    private int slot;
    private String permission;
    private String economy; // e.g., "Vault", "PlayerPoints"
    private List<ShopItem> items;

    // Async I/O Support
    private String fileName;
    private java.io.File file;
    private org.bukkit.configuration.file.FileConfiguration config;
}
