package me.antigravity.economyshop.api.item;

import me.antigravity.economyshop.EconomyShop;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

/**
 * ItemsAdder 플러그인의 커스텀 아이템을 처리하는 어댑터입니다.
 * ItemsAdder 아이템은 PersistentDataContainer에 고유 ID를 저장합니다.
 */
public class ItemsAdderItemAdapter implements ItemAdapter {

    private final EconomyShop plugin;
    private final NamespacedKey itemsadderIdKey;
    private boolean available;

    public ItemsAdderItemAdapter(EconomyShop plugin) {
        this.plugin = plugin;
        // ItemsAdder는 "itemsadder:id" 네임스페이스를 사용
        this.itemsadderIdKey = new NamespacedKey("itemsadder", "id");
        this.available = plugin.getServer().getPluginManager().isPluginEnabled("ItemsAdder");
    }

    @Override
    public String getName() {
        return "ItemsAdder";
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

        String itemsadderId = getItemsAdderId(item);
        if (itemsadderId != null) {
            return "itemsadder:" + itemsadderId;
        }
        return null;
    }

    @Override
    public ItemStack deserialize(String data) {
        if (data == null || !data.startsWith("itemsadder:")) {
            return null;
        }

        String itemsadderId = data.substring(11); // "itemsadder:" 제거

        // ItemsAdder API를 통해 아이템 가져오기 (Reflection 사용)
        try {
            Class<?> customStackClass = Class.forName("dev.lone.itemsadder.api.CustomStack");
            java.lang.reflect.Method getInstance = customStackClass.getMethod("getInstance", String.class);
            Object customStack = getInstance.invoke(null, itemsadderId);

            if (customStack != null) {
                java.lang.reflect.Method getItemStack = customStack.getClass().getMethod("getItemStack");
                return (ItemStack) getItemStack.invoke(customStack);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("ItemsAdder 아이템 역직렬화 실패: " + itemsadderId + " - " + e.getMessage());
        }

        return null;
    }

    @Override
    public boolean matches(ItemStack shopItem, ItemStack playerItem) {
        if (shopItem == null || playerItem == null) {
            return false;
        }

        String shopId = getItemsAdderId(shopItem);
        String playerId = getItemsAdderId(playerItem);

        if (shopId == null || playerId == null) {
            return false;
        }

        // ItemsAdder ID로만 비교
        return shopId.equals(playerId);
    }

    @Override
    public boolean canHandle(ItemStack item) {
        if (!available || item == null) {
            return false;
        }
        return getItemsAdderId(item) != null;
    }

    /**
     * 아이템에서 ItemsAdder ID를 추출합니다.
     * 
     * @param item 확인할 아이템
     * @return ItemsAdder ID, 없으면 null
     */
    private String getItemsAdderId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.get(itemsadderIdKey, PersistentDataType.STRING);
    }
}
