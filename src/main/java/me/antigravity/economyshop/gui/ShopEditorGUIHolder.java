package me.antigravity.economyshop.gui;

import lombok.Getter;
import me.antigravity.economyshop.model.ShopItem;
import me.antigravity.economyshop.model.ShopSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class ShopEditorGUIHolder implements InventoryHolder {

    @Getter
    private final ShopSection section;
    @Getter
    private final ShopItem targetItem;
    @Getter
    private Inventory inventory;

    public ShopEditorGUIHolder(ShopSection section, ShopItem targetItem) {
        this.section = section;
        this.targetItem = targetItem;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
