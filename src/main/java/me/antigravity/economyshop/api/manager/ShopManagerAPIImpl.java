package me.antigravity.economyshop.api.manager;

import me.antigravity.economyshop.EconomyShop;
import me.antigravity.economyshop.model.ShopSection;
import me.antigravity.economyshop.util.ItemSerializer;
import org.bukkit.Material;

import java.util.Collections;
import java.util.Map;

/**
 * ShopManagerAPI의 내부 구현체입니다.
 * ShopManager를 래핑하여 외부 플러그인에 안전한 인터페이스를 제공합니다.
 */
public class ShopManagerAPIImpl implements ShopManagerAPI {

    private final EconomyShop plugin;

    public ShopManagerAPIImpl(EconomyShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public ShopSection getSection(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        return plugin.getShopManager().getSections().get(id);
    }

    @Override
    public Map<String, ShopSection> getAllSections() {
        return Collections.unmodifiableMap(plugin.getShopManager().getSections());
    }

    @Override
    public boolean registerSection(ShopSection section, boolean persistent) {
        if (section == null || section.getId() == null || section.getId().isEmpty()) {
            return false;
        }

        // 중복 체크
        if (plugin.getShopManager().getSections().containsKey(section.getId())) {
            plugin.getLogger().warning("상점 섹션 [" + section.getId() + "]가 이미 존재합니다.");
            return false;
        }

        // 메모리에 등록
        plugin.getShopManager().getSections().put(section.getId(), section);

        // 영구 저장
        if (persistent) {
            return ItemSerializer.createSection(
                    plugin,
                    section.getId(),
                    section.getDisplayName(),
                    section.getIcon() != null ? section.getIcon().getType() : Material.CHEST,
                    section.getSlot());
        }

        plugin.getLogger().info("상점 섹션 [" + section.getId() + "] 등록 완료 (메모리)");
        return true;
    }

    @Override
    public boolean unregisterSection(String id, boolean deleteFile) {
        if (id == null || id.isEmpty()) {
            return false;
        }

        ShopSection removed = plugin.getShopManager().getSections().remove(id);
        if (removed == null) {
            return false;
        }

        if (deleteFile) {
            // sections.yml에서 제거
            plugin.getConfigManager().getSectionsConfig().set(id, null);
            plugin.getConfigManager().saveSectionsConfig();

            // shops/id.yml 파일 삭제
            java.io.File shopFile = new java.io.File(plugin.getDataFolder(), "shops/" + id + ".yml");
            if (shopFile.exists()) {
                shopFile.delete();
            }
        }

        plugin.getLogger().info("상점 섹션 [" + id + "] 제거 완료");
        return true;
    }

    @Override
    public ShopController getShopController(String shopId) {
        ShopSection section = getSection(shopId);
        if (section == null) {
            return null;
        }
        return new ShopControllerImpl(plugin, section);
    }

    @Override
    public void reloadShops() {
        plugin.getShopManager().loadShops();
        plugin.getLogger().info("상점 데이터 다시 로드 완료");
    }
}
