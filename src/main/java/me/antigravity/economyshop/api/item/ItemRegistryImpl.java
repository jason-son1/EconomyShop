package me.antigravity.economyshop.api.item;

import me.antigravity.economyshop.EconomyShop;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ItemRegistry의 내부 구현체입니다.
 * 등록된 어댑터를 순서대로 검사하여 적합한 어댑터를 찾습니다.
 */
public class ItemRegistryImpl implements ItemRegistry {

    private final EconomyShop plugin;
    private final Map<String, ItemAdapter> adapters = new LinkedHashMap<>();
    private final ItemAdapter defaultAdapter;

    public ItemRegistryImpl(EconomyShop plugin) {
        this.plugin = plugin;
        this.defaultAdapter = new VanillaItemAdapter();

        // 기본 어댑터 등록
        registerAdapter(defaultAdapter);

        // 플러그인 연동 어댑터 등록
        registerPluginAdapters();
    }

    /**
     * 외부 플러그인 연동 어댑터들을 등록합니다.
     */
    private void registerPluginAdapters() {
        // Oraxen 어댑터
        if (plugin.getServer().getPluginManager().getPlugin("Oraxen") != null) {
            registerAdapter(new OraxenItemAdapter(plugin));
            plugin.getLogger().info("Oraxen 아이템 어댑터가 등록되었습니다.");
        }

        // ItemsAdder 어댑터
        if (plugin.getServer().getPluginManager().getPlugin("ItemsAdder") != null) {
            registerAdapter(new ItemsAdderItemAdapter(plugin));
            plugin.getLogger().info("ItemsAdder 아이템 어댑터가 등록되었습니다.");
        }
    }

    @Override
    public boolean registerAdapter(ItemAdapter adapter) {
        if (adapter == null || adapter.getName() == null || adapter.getName().isEmpty()) {
            return false;
        }

        if (adapters.containsKey(adapter.getName())) {
            plugin.getLogger().warning("아이템 어댑터 [" + adapter.getName() + "]가 이미 등록되어 있습니다.");
            return false;
        }

        adapters.put(adapter.getName(), adapter);
        plugin.getLogger().info("아이템 어댑터 [" + adapter.getName() + "] 등록 완료 (사용 가능: " + adapter.isAvailable() + ")");
        return true;
    }

    @Override
    public boolean unregisterAdapter(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        // 기본 어댑터는 제거할 수 없음
        if (name.equals(defaultAdapter.getName())) {
            plugin.getLogger().warning("기본 아이템 어댑터는 제거할 수 없습니다.");
            return false;
        }

        return adapters.remove(name) != null;
    }

    @Override
    public ItemAdapter getAdapter(String name) {
        if (name == null || name.isEmpty()) {
            return defaultAdapter;
        }
        ItemAdapter adapter = adapters.get(name);
        return adapter != null ? adapter : defaultAdapter;
    }

    @Override
    public ItemAdapter findAdapter(ItemStack item) {
        if (item == null) {
            return defaultAdapter;
        }

        // 등록된 어댑터를 순서대로 검사
        for (ItemAdapter adapter : adapters.values()) {
            if (adapter.isAvailable() && adapter.canHandle(item)) {
                return adapter;
            }
        }

        return defaultAdapter;
    }

    @Override
    public ItemAdapter getDefaultAdapter() {
        return defaultAdapter;
    }

    @Override
    public Map<String, ItemAdapter> getAllAdapters() {
        return Collections.unmodifiableMap(adapters);
    }

    @Override
    public boolean matches(ItemStack shopItem, ItemStack playerItem) {
        if (shopItem == null || playerItem == null) {
            return false;
        }

        // 상점 아이템에 맞는 어댑터 찾기
        ItemAdapter adapter = findAdapter(shopItem);
        return adapter.matches(shopItem, playerItem);
    }

    @Override
    public boolean matches(String adapterName, ItemStack shopItem, ItemStack playerItem) {
        if (shopItem == null || playerItem == null) {
            return false;
        }

        ItemAdapter adapter = getAdapter(adapterName);
        return adapter.matches(shopItem, playerItem);
    }
}
