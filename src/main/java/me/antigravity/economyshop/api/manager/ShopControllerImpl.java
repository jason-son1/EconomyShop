package me.antigravity.economyshop.api.manager;

import me.antigravity.economyshop.EconomyShop;
import me.antigravity.economyshop.model.ShopItem;
import me.antigravity.economyshop.model.ShopSection;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * ShopController의 내부 구현체입니다.
 * 특정 상점 섹션을 실시간으로 제어합니다.
 */
public class ShopControllerImpl implements ShopController {

    private final EconomyShop plugin;
    private final ShopSection section;

    public ShopControllerImpl(EconomyShop plugin, ShopSection section) {
        this.plugin = plugin;
        this.section = section;
    }

    @Override
    public String getShopId() {
        return section.getId();
    }

    @Override
    public ShopItem addItem(ItemStack item, double buyPrice, double sellPrice) {
        // 다음 빈 슬롯 찾기
        int slot = findNextEmptySlot();
        return addItem(item, buyPrice, sellPrice, slot);
    }

    @Override
    public ShopItem addItem(ItemStack item, double buyPrice, double sellPrice, int slot) {
        if (item == null) {
            return null;
        }

        // 고유 ID 생성
        String itemId = generateItemId(item);

        // ShopItem 생성
        ShopItem shopItem = ShopItem.builder()
                .id(itemId)
                .itemStack(item.clone())
                .buyPrice(buyPrice)
                .sellPrice(sellPrice)
                .slot(slot)
                .dynamicPricing(false)
                .maxStock(1000L)
                .currentStock(1000L)
                .build();

        // 메모리에 추가
        section.getItems().add(shopItem);

        // 파일에 저장
        plugin.getShopManager().saveShopItem(section, shopItem);

        plugin.getLogger().info("상점 [" + section.getId() + "]에 아이템 [" + itemId + "] 추가됨");
        return shopItem;
    }

    @Override
    public boolean removeItem(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return false;
        }

        ShopItem toRemove = getItem(itemId);
        if (toRemove == null) {
            return false;
        }

        plugin.getShopManager().deleteShopItem(section, toRemove);
        plugin.getLogger().info("상점 [" + section.getId() + "]에서 아이템 [" + itemId + "] 제거됨");
        return true;
    }

    @Override
    public boolean updateItemPrice(String itemId, double newBuyPrice, double newSellPrice) {
        ShopItem item = getItem(itemId);
        if (item == null) {
            return false;
        }

        if (newBuyPrice >= 0) {
            item.setBuyPrice(newBuyPrice);
        }
        if (newSellPrice >= 0) {
            item.setSellPrice(newSellPrice);
        }

        // 파일에 저장
        plugin.getShopManager().saveShopItem(section, item);
        return true;
    }

    @Override
    public void updateTitle(String newTitle) {
        if (newTitle == null) {
            return;
        }
        section.setDisplayName(newTitle);

        // sections.yml 업데이트
        plugin.getConfigManager().getSectionsConfig().set(section.getId() + ".display-name", newTitle);
        plugin.getConfigManager().saveSectionsConfig();
    }

    @Override
    public List<ShopItem> getItems() {
        return Collections.unmodifiableList(section.getItems());
    }

    @Override
    public ShopItem getItem(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return null;
        }
        return section.getItems().stream()
                .filter(item -> itemId.equals(item.getId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void save() {
        // 모든 아이템 저장
        for (ShopItem item : section.getItems()) {
            plugin.getShopManager().saveShopItem(section, item);
        }
    }

    /**
     * 다음 빈 슬롯을 찾습니다.
     */
    private int findNextEmptySlot() {
        int slot = 0;
        while (true) {
            final int checkSlot = slot;
            boolean occupied = section.getItems().stream()
                    .anyMatch(item -> item.getSlot() == checkSlot);
            if (!occupied) {
                return slot;
            }
            slot++;
        }
    }

    /**
     * 아이템에 대한 고유 ID를 생성합니다.
     */
    private String generateItemId(ItemStack item) {
        String baseName = item.getType().name().toLowerCase();
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        return baseName + "_" + uniqueSuffix;
    }
}
