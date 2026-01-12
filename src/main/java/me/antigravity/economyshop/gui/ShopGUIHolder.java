package me.antigravity.economyshop.gui;

import lombok.Getter;
import lombok.Setter;
import me.antigravity.economyshop.model.ShopSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class ShopGUIHolder implements InventoryHolder {

    @Getter
    private final ShopSection section;

    @Getter
    @Setter
    private int page = 0;

    @Getter
    @Setter
    private Inventory inventory;

    public ShopGUIHolder(ShopSection section) {
        this.section = section;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
