package me.antigravity.economyshop.api.economy;

import me.antigravity.economyshop.EconomyShop;
import me.antigravity.economyshop.economy.EconomyProvider;

import java.util.Collections;
import java.util.Map;

/**
 * EconomyRegistry의 내부 구현체입니다.
 * EconomyManager를 래핑하여 외부 플러그인에 안전한 인터페이스를 제공합니다.
 */
public class EconomyRegistryImpl implements EconomyRegistry {

    private final EconomyShop plugin;

    public EconomyRegistryImpl(EconomyShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean registerProvider(String id, EconomyProvider provider) {
        if (id == null || id.isEmpty() || provider == null) {
            return false;
        }
        // 이미 존재하는지 확인
        if (plugin.getEconomyManager().getProviders().containsKey(id)) {
            plugin.getLogger().warning("경제 Provider [" + id + "]가 이미 등록되어 있습니다.");
            return false;
        }
        plugin.getEconomyManager().registerProvider(provider);
        // 원하는 ID로 별도 등록 (Provider.getName()과 다른 경우)
        if (!provider.getName().equals(id)) {
            plugin.getEconomyManager().getProviders().put(id, provider);
        }
        plugin.getLogger().info("외부 경제 Provider [" + id + "] 등록 완료");
        return true;
    }

    @Override
    public boolean unregisterProvider(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        // 기본 Provider는 제거할 수 없음
        if (id.equalsIgnoreCase("Vault") || id.equalsIgnoreCase("PlayerPoints") || id.equalsIgnoreCase("EXP")) {
            plugin.getLogger().warning("기본 경제 Provider [" + id + "]는 제거할 수 없습니다.");
            return false;
        }
        EconomyProvider removed = plugin.getEconomyManager().getProviders().remove(id);
        return removed != null;
    }

    @Override
    public EconomyProvider getProvider(String id) {
        if (id == null || id.isEmpty()) {
            return getDefaultProvider();
        }
        return plugin.getEconomyManager().getProvider(id);
    }

    @Override
    public EconomyProvider getDefaultProvider() {
        return plugin.getEconomyManager().getDefaultProvider();
    }

    @Override
    public Map<String, EconomyProvider> getAllProviders() {
        return Collections.unmodifiableMap(plugin.getEconomyManager().getProviders());
    }

    @Override
    public boolean hasProvider(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        return plugin.getEconomyManager().getProviders().containsKey(id);
    }
}
