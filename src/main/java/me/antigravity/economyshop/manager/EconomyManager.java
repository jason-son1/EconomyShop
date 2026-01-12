package me.antigravity.economyshop.manager;

import me.antigravity.economyshop.EconomyShop;
import me.antigravity.economyshop.economy.EconomyProvider;
import me.antigravity.economyshop.economy.ExpProvider;
import me.antigravity.economyshop.economy.ItemEconomyProvider;
import me.antigravity.economyshop.economy.PlayerPointsProvider;
import me.antigravity.economyshop.economy.VaultProvider;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

/**
 * 여러 경제 시스템을 통합 관리하는 매니저입니다.
 * Vault, PlayerPoints, EXP, Item 기반 경제 등 다양한 Provider를 지원합니다.
 */
public class EconomyManager {

    private final EconomyShop plugin;
    private final Map<String, EconomyProvider> providers = new HashMap<>();
    private String defaultProvider = "Vault";

    public EconomyManager(EconomyShop plugin) {
        this.plugin = plugin;
        registerProviders();
    }

    /**
     * 모든 경제 Provider를 등록합니다.
     */
    private void registerProviders() {
        // Vault (기본)
        VaultProvider vaultProvider = new VaultProvider(plugin);
        registerProvider(vaultProvider);

        // PlayerPoints (소프트 의존성)
        PlayerPointsProvider playerPointsProvider = new PlayerPointsProvider(plugin);
        registerProvider(playerPointsProvider);

        // EXP (경험치)
        ExpProvider expProvider = new ExpProvider(plugin);
        registerProvider(expProvider);

        // 아이템 기반 경제 (에메랄드)
        ItemEconomyProvider emeraldProvider = new ItemEconomyProvider(plugin, Material.EMERALD, "에메랄드");
        registerProvider(emeraldProvider);

        // 아이템 기반 경제 (금괴)
        ItemEconomyProvider goldProvider = new ItemEconomyProvider(plugin, Material.GOLD_INGOT, "금괴");
        providers.put("Item:GOLD_INGOT", goldProvider);

        // 아이템 기반 경제 (다이아몬드)
        ItemEconomyProvider diamondProvider = new ItemEconomyProvider(plugin, Material.DIAMOND, "다이아몬드");
        providers.put("Item:DIAMOND", diamondProvider);

        // config에서 기본 경제 시스템 로드
        defaultProvider = plugin.getConfigManager().getMainConfig().getString("default-economy", "Vault");

        plugin.getLogger().info("등록된 경제 시스템: " + String.join(", ", providers.keySet()));
    }

    /**
     * Provider를 등록합니다.
     */
    public void registerProvider(EconomyProvider provider) {
        providers.put(provider.getName(), provider);
        if (provider.isAvailable()) {
            plugin.getLogger().info("경제 시스템 [" + provider.getName() + "] 연동됨");
        }
    }

    /**
     * 이름으로 Provider를 조회합니다.
     * 
     * @param name Provider 이름 (예: "Vault", "PlayerPoints", "EXP", "Item:EMERALD")
     * @return EconomyProvider 또는 null
     */
    public EconomyProvider getProvider(String name) {
        if (name == null || name.isEmpty()) {
            return getDefaultProvider();
        }
        EconomyProvider provider = providers.get(name);
        if (provider == null || !provider.isAvailable()) {
            plugin.getLogger().warning("요청된 경제 시스템 [" + name + "]을 사용할 수 없습니다. 기본값 사용.");
            return getDefaultProvider();
        }
        return provider;
    }

    /**
     * 기본 경제 시스템 Provider를 반환합니다.
     */
    public EconomyProvider getDefaultProvider() {
        EconomyProvider provider = providers.get(defaultProvider);
        if (provider == null || !provider.isAvailable()) {
            // Vault로 fallback
            provider = providers.get("Vault");
        }
        return provider;
    }

    /**
     * Vault 경제 시스템이 사용 가능한지 확인합니다.
     * 
     * @deprecated getProvider("Vault").isAvailable() 사용 권장
     */
    @Deprecated
    public boolean hasVault() {
        EconomyProvider vault = providers.get("Vault");
        return vault != null && vault.isAvailable();
    }

    /**
     * Vault Economy 객체를 직접 반환합니다.
     * 
     * @deprecated getProvider("Vault") 사용 권장
     */
    @Deprecated
    public Economy getVaultEconomy() {
        // 이 메서드는 deprecated이며 null을 반환합니다.
        // 대신 getProvider("Vault")를 사용하세요.
        return null;
    }

    /**
     * 모든 등록된 Provider를 반환합니다.
     */
    public Map<String, EconomyProvider> getProviders() {
        return providers;
    }

    /**
     * 아이템 및 섹션 설정에 따라 적절한 EconomyProvider를 반환합니다.
     * 우선순위: 아이템 설정 > 섹션 설정 > 기본 설정
     */
    public EconomyProvider getProvider(me.antigravity.economyshop.model.ShopSection section,
            me.antigravity.economyshop.model.ShopItem item) {
        // 아이템에 개별 경제 시스템이 설정된 경우
        if (item.getEconomyType() != null && !item.getEconomyType().isEmpty()) {
            return getProvider(item.getEconomyType());
        }
        // 섹션에 경제 시스템이 설정된 경우
        if (section != null && section.getEconomy() != null && !section.getEconomy().isEmpty()) {
            return getProvider(section.getEconomy());
        }
        // 기본 경제 시스템 반환
        return getDefaultProvider();
    }
}
