package me.antigravity.economyshop.api.item;

import me.antigravity.economyshop.EconomyShop;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

/**
 * Oraxen 플러그인의 커스텀 아이템을 처리하는 어댑터입니다.
 * Oraxen 아이템은 PersistentDataContainer에 고유 ID를 저장합니다.
 */
public class OraxenItemAdapter implements ItemAdapter {

    private final EconomyShop plugin;
    private final NamespacedKey oraxenIdKey;
    private boolean available;

    public OraxenItemAdapter(EconomyShop plugin) {
        this.plugin = plugin;
        // Oraxen은 "oraxen:id" 네임스페이스를 사용
        this.oraxenIdKey = new NamespacedKey("oraxen", "id");
        this.available = plugin.getServer().getPluginManager().isPluginEnabled("Oraxen");
    }

    @Override
    public String getName() {
        return "Oraxen";
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String serialize(ItemStack item) {
        if (item == null) {
            return null;
        }

        String oraxenId = getOraxenId(item);
        if (oraxenId != null) {
            return "oraxen:" + oraxenId;
        }
        return null;
    }

    @Override
    public ItemStack deserialize(String data) {
        if (data == null || !data.startsWith("oraxen:")) {
            return null;
        }

        String oraxenId = data.substring(7); // "oraxen:" 제거

        // Oraxen API를 통해 아이템 가져오기 (Reflection 사용)
        try {
            Class<?> oraxenItemsClass = Class.forName("io.th0rgal.oraxen.api.OraxenItems");
            java.lang.reflect.Method getItemById = oraxenItemsClass.getMethod("getItemById", String.class);
            Object itemBuilder = getItemById.invoke(null, oraxenId);

            if (itemBuilder != null) {
                java.lang.reflect.Method build = itemBuilder.getClass().getMethod("build");
                return (ItemStack) build.invoke(itemBuilder);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Oraxen 아이템 역직렬화 실패: " + oraxenId + " - " + e.getMessage());
        }

        return null;
    }

    @Override
    public boolean matches(ItemStack shopItem, ItemStack playerItem) {
        if (shopItem == null || playerItem == null) {
            return false;
        }

        String shopOraxenId = getOraxenId(shopItem);
        String playerOraxenId = getOraxenId(playerItem);

        if (shopOraxenId == null || playerOraxenId == null) {
            return false;
        }

        // Oraxen ID로만 비교 (내구도, 인챈트 등 무시)
        return shopOraxenId.equals(playerOraxenId);
    }

    @Override
    public boolean canHandle(ItemStack item) {
        if (!available || item == null) {
            return false;
        }
        return getOraxenId(item) != null;
    }

    /**
     * 아이템에서 Oraxen ID를 추출합니다.
     * 
     * @param item 확인할 아이템
     * @return Oraxen ID, 없으면 null
     */
    private String getOraxenId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.get(oraxenIdKey, PersistentDataType.STRING);
    }
}
