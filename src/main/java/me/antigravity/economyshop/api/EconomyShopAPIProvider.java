package me.antigravity.economyshop.api;

/**
 * EconomyShopAPI 인스턴스를 제공하는 내부 클래스입니다.
 * 플러그인 로드 시 API 인스턴스가 등록됩니다.
 */
public final class EconomyShopAPIProvider {

    private static EconomyShopAPI api;

    private EconomyShopAPIProvider() {
        // 인스턴스화 방지
    }

    /**
     * API 인스턴스를 반환합니다.
     * 
     * @return EconomyShopAPI 인스턴스, 또는 플러그인이 로드되지 않았으면 null
     */
    public static EconomyShopAPI getAPI() {
        return api;
    }

    /**
     * API 인스턴스를 등록합니다.
     * 이 메서드는 플러그인 내부에서만 호출되어야 합니다.
     * 
     * @param instance API 구현체 인스턴스
     */
    public static void register(EconomyShopAPI instance) {
        api = instance;
    }

    /**
     * API 인스턴스 등록을 해제합니다.
     * 플러그인 비활성화 시 호출됩니다.
     */
    public static void unregister() {
        api = null;
    }
}
