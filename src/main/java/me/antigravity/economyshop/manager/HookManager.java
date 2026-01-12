package me.antigravity.economyshop.manager;

import me.antigravity.economyshop.EconomyShop;
import org.bukkit.inventory.ItemStack;

/**
 * Oraxen, ItemsAdder 등 외부 플러그인과 연동을 관리하는 클래스입니다.
 * 실제 API 호출은 해당 플러그인이 로드된 경우에만 수행됩니다.
 */
public class HookManager {

    private final EconomyShop plugin;
    private boolean oraxenEnabled = false;
    private boolean itemsAdderEnabled = false;

    public HookManager(EconomyShop plugin) {
        this.plugin = plugin;
        checkHooks();
    }

    private void checkHooks() {
        if (plugin.getServer().getPluginManager().getPlugin("Oraxen") != null) {
            this.oraxenEnabled = true;
            plugin.getLogger().info("Oraxen 연동이 활성화되었습니다.");
        }
        if (plugin.getServer().getPluginManager().getPlugin("ItemsAdder") != null) {
            this.itemsAdderEnabled = true;
            plugin.getLogger().info("ItemsAdder 연동이 활성화되었습니다.");
        }
    }

    /**
     * Oraxen 아이템을 가져옵니다. (실제 API 호출은 런타임에 리플렉션으로 처리)
     */
    public ItemStack getOraxenItem(String id) {
        if (!oraxenEnabled)
            return null;
        // Oraxen API 호출은 리플렉션으로 처리하거나 별도 어댑터 클래스로 분리
        // 현재는 null 반환 (추후 구현)
        return null;
    }

    /**
     * ItemsAdder 아이템을 가져옵니다. (실제 API 호출은 런타임에 리플렉션으로 처리)
     */
    public ItemStack getItemsAdderItem(String id) {
        if (!itemsAdderEnabled)
            return null;
        // ItemsAdder API 호출은 리플렉션으로 처리하거나 별도 어댑터 클래스로 분리
        // 현재는 null 반환 (추후 구현)
        return null;
    }

    public boolean isOraxenEnabled() {
        return oraxenEnabled;
    }

    public boolean isItemsAdderEnabled() {
        return itemsAdderEnabled;
    }
}
