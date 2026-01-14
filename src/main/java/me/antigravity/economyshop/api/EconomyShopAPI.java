package me.antigravity.economyshop.api;

import me.antigravity.economyshop.api.builder.ShopBuilder;
import me.antigravity.economyshop.api.economy.EconomyRegistry;
import me.antigravity.economyshop.api.item.ItemRegistry;
import me.antigravity.economyshop.api.manager.ShopManagerAPI;
import org.bukkit.entity.Player;

/**
 * EconomyShop의 메인 API 인터페이스입니다.
 * 외부 플러그인은 이 인터페이스를 통해 EconomyShop의 기능에 접근합니다.
 * 
 * <p>
 * 사용 예시:
 * </p>
 * 
 * <pre>
 * EconomyShopAPI api = EconomyShopAPI.getInstance();
 * api.openShop(player, "weapons");
 * </pre>
 */
public interface EconomyShopAPI {

    /**
     * EconomyShopAPI의 싱글톤 인스턴스를 반환합니다.
     * 
     * @return EconomyShopAPI 인스턴스, 플러그인이 로드되지 않았으면 null
     */
    static EconomyShopAPI getInstance() {
        return EconomyShopAPIProvider.getAPI();
    }

    /**
     * 상점 관리 API를 반환합니다.
     * 
     * @return ShopManagerAPI 인스턴스
     */
    ShopManagerAPI getShopManager();

    /**
     * 경제 시스템 레지스트리를 반환합니다.
     * 외부 플러그인은 이를 통해 커스텀 화폐를 등록할 수 있습니다.
     * 
     * @return EconomyRegistry 인스턴스
     */
    EconomyRegistry getEconomyRegistry();

    /**
     * 아이템 어댑터 레지스트리를 반환합니다.
     * 외부 플러그인은 이를 통해 커스텀 아이템 어댑터를 등록할 수 있습니다.
     * 
     * @return ItemRegistry 인스턴스
     */
    ItemRegistry getItemRegistry();

    /**
     * 플레이어에게 특정 상점을 엽니다.
     * 
     * @param player 대상 플레이어
     * @param shopId 상점 ID (섹션 ID)
     */
    void openShop(Player player, String shopId);

    /**
     * 플레이어에게 메인 상점 메뉴를 엽니다.
     * 
     * @param player 대상 플레이어
     */
    void openMainMenu(Player player);

    /**
     * 새로운 ShopBuilder를 생성합니다.
     * Fluent API로 상점을 정의하고 등록할 수 있습니다.
     * 
     * <p>
     * 사용 예시:
     * </p>
     * 
     * <pre>
     * api.createShopBuilder()
     *         .id("rpg_weapons")
     *         .displayName("&c무기 상점")
     *         .icon(Material.IRON_SWORD)
     *         .slot(12)
     *         .buildAndSave();
     * </pre>
     * 
     * @return 새로운 ShopBuilder 인스턴스
     */
    ShopBuilder createShopBuilder();

    /**
     * 플러그인 버전을 반환합니다.
     * 
     * @return 버전 문자열
     */
    String getVersion();
}
