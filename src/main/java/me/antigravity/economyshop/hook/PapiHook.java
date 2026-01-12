package me.antigravity.economyshop.hook;

import me.antigravity.economyshop.EconomyShop;
import me.antigravity.economyshop.economy.EconomyProvider;
import me.antigravity.economyshop.model.ShopItem;
import me.antigravity.economyshop.model.ShopSection;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * PlaceholderAPI 확장 클래스
 * 
 * 지원하는 Placeholder:
 * - %economyshop_balance_<economy>% - 특정 경제 시스템 잔액
 * - %economyshop_price_<section>_<itemid>% - 아이템 현재 구매 가격
 * - %economyshop_sellprice_<section>_<itemid>% - 아이템 현재 판매 가격
 * - %economyshop_stock_<section>_<itemid>% - 아이템 현재 재고
 * - %economyshop_maxstock_<section>_<itemid>% - 아이템 최대 재고
 * - %economyshop_limit_<itemid>% - 플레이어의 아이템 구매 제한 사용량
 * - %economyshop_limit_max_<itemid>% - 아이템 최대 구매 제한
 */
public class PapiHook extends PlaceholderExpansion {

    private final EconomyShop plugin;

    public PapiHook(EconomyShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "economyshop";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Antigravity";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params == null)
            return "";

        // %economyshop_balance_<economy>%
        if (params.startsWith("balance_")) {
            if (player == null || !player.isOnline())
                return "0";
            String economyName = params.substring(8);
            EconomyProvider provider = plugin.getEconomyManager().getProvider(economyName);
            if (provider == null || !provider.isAvailable())
                return "0";
            return provider.formatAmount(provider.getBalance(player.getPlayer()));
        }

        // %economyshop_price_<section>_<itemid>%
        if (params.startsWith("price_")) {
            String[] parts = params.substring(6).split("_", 2);
            if (parts.length < 2)
                return "N/A";

            ShopItem item = findItem(parts[0], parts[1]);
            if (item == null)
                return "N/A";
            return String.format("%.2f", item.getCurrentBuyPrice());
        }

        // %economyshop_sellprice_<section>_<itemid>%
        if (params.startsWith("sellprice_")) {
            String[] parts = params.substring(10).split("_", 2);
            if (parts.length < 2)
                return "N/A";

            ShopItem item = findItem(parts[0], parts[1]);
            if (item == null)
                return "N/A";
            return String.format("%.2f", item.getCurrentSellPrice());
        }

        // %economyshop_stock_<section>_<itemid>%
        if (params.startsWith("stock_")) {
            String[] parts = params.substring(6).split("_", 2);
            if (parts.length < 2)
                return "N/A";

            ShopItem item = findItem(parts[0], parts[1]);
            if (item == null || !item.isDynamicPricing())
                return "N/A";
            return String.valueOf(item.getCurrentStock());
        }

        // %economyshop_maxstock_<section>_<itemid>%
        if (params.startsWith("maxstock_")) {
            String[] parts = params.substring(9).split("_", 2);
            if (parts.length < 2)
                return "N/A";

            ShopItem item = findItem(parts[0], parts[1]);
            if (item == null || !item.isDynamicPricing())
                return "N/A";
            return String.valueOf(item.getMaxStock());
        }

        // %economyshop_limit_<itemid>%
        if (params.startsWith("limit_")) {
            if (player == null)
                return "0";
            String itemId = params.substring(6);
            return String.valueOf(plugin.getLimitManager().getCurrentUsage(player.getUniqueId(), itemId));
        }

        // %economyshop_limit_max_<itemid>%
        if (params.startsWith("limit_max_")) {
            String itemId = params.substring(10);
            ShopItem item = findItemById(itemId);
            if (item == null || item.getPlayerLimit() <= 0)
                return "무제한";
            return String.valueOf(item.getPlayerLimit());
        }

        // %economyshop_dynamic_<section>_<itemid>%
        if (params.startsWith("dynamic_")) {
            String[] parts = params.substring(8).split("_", 2);
            if (parts.length < 2)
                return "N/A";

            ShopItem item = findItem(parts[0], parts[1]);
            if (item == null)
                return "N/A";
            return item.isDynamicPricing() ? "활성" : "비활성";
        }

        return null;
    }

    /**
     * 섹션 ID와 아이템 ID로 ShopItem을 찾습니다.
     */
    private ShopItem findItem(String sectionId, String itemId) {
        ShopSection section = plugin.getShopManager().getSections().get(sectionId);
        if (section == null)
            return null;

        for (ShopItem item : section.getItems()) {
            if (item.getId().equals(itemId)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 아이템 ID로만 ShopItem을 찾습니다 (모든 섹션 검색).
     */
    private ShopItem findItemById(String itemId) {
        for (ShopSection section : plugin.getShopManager().getSections().values()) {
            for (ShopItem item : section.getItems()) {
                if (item.getId().equals(itemId)) {
                    return item;
                }
            }
        }
        return null;
    }
}
