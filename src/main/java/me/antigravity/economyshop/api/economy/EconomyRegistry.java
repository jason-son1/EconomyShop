package me.antigravity.economyshop.api.economy;

import me.antigravity.economyshop.economy.EconomyProvider;

import java.util.Map;

/**
 * 경제 시스템 Provider를 관리하는 레지스트리입니다.
 * 외부 플러그인은 이 인터페이스를 통해 커스텀 화폐를 등록할 수 있습니다.
 * 
 * <p>
 * 사용 예시:
 * </p>
 * 
 * <pre>
 * EconomyRegistry registry = EconomyShopAPI.getInstance().getEconomyRegistry();
 * registry.registerProvider("rpg_tokens", myCustomProvider);
 * </pre>
 */
public interface EconomyRegistry {

    /**
     * 새로운 경제 Provider를 등록합니다.
     * 
     * @param id       Provider의 고유 식별자 (예: "RPG_TOKENS", "CLAN_POINTS")
     * @param provider EconomyProvider 구현체
     * @return 등록 성공 여부 (이미 같은 ID가 존재하면 false)
     */
    boolean registerProvider(String id, EconomyProvider provider);

    /**
     * 등록된 Provider를 제거합니다.
     * 
     * @param id Provider의 고유 식별자
     * @return 제거 성공 여부
     */
    boolean unregisterProvider(String id);

    /**
     * ID로 Provider를 조회합니다.
     * 
     * @param id Provider의 고유 식별자
     * @return EconomyProvider, 없으면 null
     */
    EconomyProvider getProvider(String id);

    /**
     * 기본 Provider를 반환합니다.
     * 
     * @return 기본 EconomyProvider (보통 Vault)
     */
    EconomyProvider getDefaultProvider();

    /**
     * 등록된 모든 Provider를 반환합니다.
     * 
     * @return Provider ID와 인스턴스의 맵 (읽기 전용)
     */
    Map<String, EconomyProvider> getAllProviders();

    /**
     * 특정 ID의 Provider가 등록되어 있는지 확인합니다.
     * 
     * @param id Provider의 고유 식별자
     * @return 등록 여부
     */
    boolean hasProvider(String id);
}
