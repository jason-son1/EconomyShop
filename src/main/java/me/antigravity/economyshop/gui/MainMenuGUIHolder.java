package me.antigravity.economyshop.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * 메인 상점 메뉴 GUI의 InventoryHolder입니다.
 * 모든 상점 섹션을 표시하는 메인 메뉴를 위한 홀더입니다.
 */
public class MainMenuGUIHolder implements InventoryHolder {

    private Inventory inventory;
    private int page = 0;

    public MainMenuGUIHolder() {
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
