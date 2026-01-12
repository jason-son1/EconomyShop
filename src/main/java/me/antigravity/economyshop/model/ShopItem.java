package me.antigravity.economyshop.model;

import lombok.Builder;
import lombok.Data;
import org.bukkit.inventory.ItemStack;

@Data
@Builder
public class ShopItem {
    private String id;
    private ItemStack itemStack;
    private transient java.util.function.Supplier<ItemStack> itemStackLoader;
    private double buyPrice;
    private double sellPrice;
    private int slot;

    // Dynamic Pricing attributes
    private boolean dynamicPricing;
    private long maxStock;
    private long currentStock;
    private double minPrice;
    private double maxPrice;

    // 제한 시스템
    private int playerLimit; // 0은 무제한

    // 경제 시스템 (null이면 섹션의 economy 사용)
    private String economyType;

    // 권한 및 요구사항 시스템
    private java.util.List<String> permissions; // 구매에 필요한 권한 목록
    private java.util.Map<String, Object> requirements; // 레벨, 플레이타임 등 요구사항

    // Display Cache
    private transient ItemStack cachedDisplayItem;

    public void clearCache() {
        this.cachedDisplayItem = null;
    }

    /**
     * 현재 재고량에 따른 실시간 구매 가격을 계산합니다.
     * 공식: P_now = P_base * (1 + (S_max - S_current) / S_max)
     */
    public double getCurrentBuyPrice() {
        if (!dynamicPricing)
            return buyPrice;

        double multiplier = 1.0 + (double) (maxStock - currentStock) / Math.max(1, maxStock);
        double calculatedPrice = buyPrice * multiplier;

        return Math.max(minPrice, Math.min(maxPrice, calculatedPrice));
    }

    /**
     * 현재 재고량에 따른 실시간 판매 가격을 계산합니다.
     * 판매 가격은 보통 구매 가격의 일정 비율이거나 별도 계산됩니다.
     */
    public double getCurrentSellPrice() {
        if (!dynamicPricing)
            return sellPrice;

        double multiplier = 1.0 + (double) (maxStock - currentStock) / Math.max(1, maxStock);
        double calculatedPrice = sellPrice * multiplier;

        return Math.max(minPrice * 0.5, Math.min(maxPrice, calculatedPrice));
    }

    // Setters with cache invalidation
    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
        clearCache();
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
        clearCache();
    }

    public void setDynamicPricing(boolean dynamicPricing) {
        this.dynamicPricing = dynamicPricing;
        clearCache();
    }

    public void setCurrentStock(long currentStock) {
        this.currentStock = currentStock;
        clearCache();
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public void setMaxStock(long maxStock) {
        this.maxStock = maxStock;
        clearCache();
    }

    public void setPlayerLimit(int playerLimit) {
        this.playerLimit = playerLimit;
        clearCache();
    }

    // Lazy Loading
    public ItemStack getItemStack() {
        if (itemStack == null && itemStackLoader != null) {
            itemStack = itemStackLoader.get();
        }
        return itemStack;
    }
}
