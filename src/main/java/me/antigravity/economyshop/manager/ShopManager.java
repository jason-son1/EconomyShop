package me.antigravity.economyshop.manager;

import me.antigravity.economyshop.EconomyShop;
import me.antigravity.economyshop.model.ShopItem;
import me.antigravity.economyshop.model.ShopSection;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShopManager {

    private final EconomyShop plugin;
    private final Map<String, ShopSection> sections = new HashMap<>();

    public ShopManager(EconomyShop plugin) {
        this.plugin = plugin;
    }

    public void loadShops() {
        sections.clear();
        FileConfiguration sectionsConfig = plugin.getConfigManager().getSectionsConfig();
        if (sectionsConfig == null)
            return;

        for (String key : sectionsConfig.getKeys(false)) {
            ConfigurationSection sectionData = sectionsConfig.getConfigurationSection(key);
            if (sectionData == null)
                continue;

            ShopSection section = ShopSection.builder()
                    .id(key)
                    .displayName(sectionData.getString("display-name", key))
                    .icon(new ItemStack(Material.valueOf(sectionData.getString("material", "STONE"))))
                    .slot(sectionData.getInt("slot", 0))
                    .permission(sectionData.getString("permission"))
                    .economy(sectionData.getString("economy", "Vault"))
                    .dynamicPricing(sectionData.getBoolean("dynamic-pricing", true)) // 섹션별 동적 경제 설정 로드 (기본값: true)
                    .items(new ArrayList<>())
                    .build();

            loadItemsForSection(section);
            sections.put(key, section);
        }
        plugin.getLogger().info(sections.size() + "개의 상점 섹션을 로드했습니다.");
    }

    private void loadItemsForSection(ShopSection section) {
        File shopFile = new File(plugin.getDataFolder(), "shops/" + section.getId() + ".yml");
        if (!shopFile.exists())
            return;

        FileConfiguration shopConfig = YamlConfiguration.loadConfiguration(shopFile);
        section.setFile(shopFile);
        section.setConfig(shopConfig);
        section.setFileName(shopFile.getName());

        ConfigurationSection itemsRoot = shopConfig.getConfigurationSection("items");
        ConfigurationSection source = itemsRoot != null ? itemsRoot : shopConfig;

        for (String key : source.getKeys(false)) {
            if (itemsRoot == null && key.equalsIgnoreCase("items"))
                continue;

            ConfigurationSection itemData = source.getConfigurationSection(key);
            if (itemData == null)
                continue;

            int slot = itemData.getInt("slot", -1);
            if (slot == -1 || isSlotOccupied(section, slot)) {
                slot = findNextEmptySlot(section);
            }

            // 전역 동적 경제 설정 확인
            boolean globalDynamic = plugin.getConfigManager().isGlobalDynamicPricingEnabled();
            // 섹션 동적 경제 설정 확인
            boolean sectionDynamic = section.isDynamicPricing();
            // 아이템 동적 경제 설정 확인
            boolean itemDynamic = itemData.getBoolean("dynamic-pricing", false);

            // 최종 동적 경제 활성화 여부 결정 (전역 AND 섹션 AND 아이템)
            boolean finalDynamic = globalDynamic && sectionDynamic && itemDynamic;

            // Lazy Loading 적용: itemStackLoader 사용, items.getId() 대신 key 사용 (이미 key임)
            ShopItem item = ShopItem.builder()
                    .id(key)
                    .itemStackLoader(() -> me.antigravity.economyshop.util.ItemSerializer.deserialize(plugin, itemData))
                    .buyPrice(itemData.getDouble("buy", 0.0))
                    .sellPrice(itemData.getDouble("sell", 0.0))
                    .slot(slot)
                    .dynamicPricing(finalDynamic) // 최종 계산된 설정 적용
                    .maxStock(itemData.getLong("max-stock", 1000L))
                    .currentStock(itemData.getLong("max-stock", 1000L)) // 초기 재고는 최대치
                    .minPrice(itemData.getDouble("min-price", 0.0))
                    .maxPrice(itemData.getDouble("max-price", 10000.0))
                    .build();

            // DB에서 현재 재고 로드 (동적 가격인 경우)
            if (finalDynamic) {
                long dbStock = plugin.getDatabaseManager().loadDynamicStock(key, item.getMaxStock());
                item.setCurrentStock(dbStock);
            }

            section.getItems().add(item);
        }
    }

    private boolean isSlotOccupied(ShopSection section, int slot) {
        return section.getItems().stream().anyMatch(item -> item.getSlot() == slot);
    }

    private int findNextEmptySlot(ShopSection section) {
        int slot = 0;
        while (isSlotOccupied(section, slot)) {
            slot++;
        }
        return slot;
    }

    public void saveShops() {
        // 현재는 메모리 기반이므로 종료 시 저장할 특별한 데이터를 따로 처리하지 않음 (추후 동적 가격 영속성 등 추가)
        plugin.getLogger().info("상점 데이터 저장 완료.");
    }

    public Map<String, ShopSection> getSections() {
        return sections;
    }

    /**
     * 특정 아이템의 변경된 설정을 YAML 파일에 저장합니다.
     */
    public void saveShopItem(ShopSection section, ShopItem item) {
        FileConfiguration config = section.getConfig();
        if (config == null)
            return;

        ConfigurationSection itemSection = config.getConfigurationSection("items." + item.getId());
        if (itemSection == null) {
            itemSection = config.createSection("items." + item.getId());
        }

        itemSection.set("buy", item.getBuyPrice());
        itemSection.set("sell", item.getSellPrice());
        itemSection.set("slot", item.getSlot());
        itemSection.set("dynamic-pricing", item.isDynamicPricing());

        saveConfigAsync(config, section.getFile());

        // DB에도 저장 (동적 재고 등)
        if (item.isDynamicPricing()) {
            plugin.getDatabaseManager().saveDynamicPrice(item.getId(), item.getCurrentStock());
        }
    }

    public void deleteShopItem(ShopSection section, ShopItem item) {
        // 메모리에서 제거
        section.getItems().remove(item);

        FileConfiguration config = section.getConfig();
        if (config != null) {
            config.set("items." + item.getId(), null);
            saveConfigAsync(config, section.getFile());
        }
    }

    private void saveConfigAsync(FileConfiguration config, java.io.File file) {
        final String data = config.saveToString();
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                java.nio.file.Files.write(file.toPath(), data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            } catch (java.io.IOException e) {
                plugin.getLogger().severe("비동기 저장 중 오류 발생 (" + file.getName() + "): " + e.getMessage());
            }
        });
    }
}
