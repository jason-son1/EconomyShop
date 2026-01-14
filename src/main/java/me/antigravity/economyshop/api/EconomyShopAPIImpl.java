package me.antigravity.economyshop.api;

import me.antigravity.economyshop.EconomyShop;
import me.antigravity.economyshop.api.builder.ShopBuilder;
import me.antigravity.economyshop.api.builder.ShopBuilderImpl;
import me.antigravity.economyshop.api.economy.EconomyRegistry;
import me.antigravity.economyshop.api.economy.EconomyRegistryImpl;
import me.antigravity.economyshop.api.item.ItemRegistry;
import me.antigravity.economyshop.api.item.ItemRegistryImpl;
import me.antigravity.economyshop.api.manager.ShopManagerAPI;
import me.antigravity.economyshop.api.manager.ShopManagerAPIImpl;
import org.bukkit.entity.Player;

/**
 * EconomyShopAPI의 내부 구현체입니다.
 * 외부 플러그인은 이 클래스가 아닌 EconomyShopAPI 인터페이스를 통해 접근해야 합니다.
 */
public class EconomyShopAPIImpl implements EconomyShopAPI {

    private final EconomyShop plugin;
    private final ShopManagerAPI shopManagerAPI;
    private final EconomyRegistry economyRegistry;
    private final ItemRegistry itemRegistry;

    /**
     * API 구현체를 생성합니다.
     * 
     * @param plugin EconomyShop 플러그인 인스턴스
     */
    public EconomyShopAPIImpl(EconomyShop plugin) {
        this.plugin = plugin;
        this.shopManagerAPI = new ShopManagerAPIImpl(plugin);
        this.economyRegistry = new EconomyRegistryImpl(plugin);
        this.itemRegistry = new ItemRegistryImpl(plugin);
    }

    @Override
    public ShopManagerAPI getShopManager() {
        return shopManagerAPI;
    }

    @Override
    public EconomyRegistry getEconomyRegistry() {
        return economyRegistry;
    }

    @Override
    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    @Override
    public void openShop(Player player, String shopId) {
        if (player == null || shopId == null) {
            return;
        }
        // 섹션 ID로 상점 열기
        me.antigravity.economyshop.model.ShopSection section = plugin.getShopManager().getSections().get(shopId);
        if (section != null) {
            plugin.getGuiManager().openShop(player, section);
        }
    }

    @Override
    public void openMainMenu(Player player) {
        if (player == null) {
            return;
        }
        plugin.getGuiManager().openMainMenu(player);
    }

    @Override
    public ShopBuilder createShopBuilder() {
        return new ShopBuilderImpl(plugin);
    }

    @SuppressWarnings("deprecation")
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
}
