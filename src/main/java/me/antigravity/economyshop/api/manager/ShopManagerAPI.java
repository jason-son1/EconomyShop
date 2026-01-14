package me.antigravity.economyshop.api.manager;

import me.antigravity.economyshop.model.ShopSection;

import java.util.Map;

/**
 * 상점 관리 API 인터페이스입니다.
 * 상점 섹션 조회, 등록, 제어 기능을 제공합니다.
 */
public interface ShopManagerAPI {

    /**
     * ID로 상점 섹션을 조회합니다.
     * 
     * @param id 섹션 ID
     * @return ShopSection, 없으면 null
     */
    ShopSection getSection(String id);

    /**
     * 등록된 모든 상점 섹션을 반환합니다.
     * 
     * @return 섹션 ID와 인스턴스의 맵 (읽기 전용)
     */
    Map<String, ShopSection> getAllSections();

    /**
     * 새로운 상점 섹션을 등록합니다.
     * 
     * @param section    등록할 ShopSection
     * @param persistent true이면 파일에 영구 저장, false이면 메모리에만 등록
     * @return 등록 성공 여부
     */
    boolean registerSection(ShopSection section, boolean persistent);

    /**
     * 상점 섹션을 제거합니다.
     * 
     * @param id         섹션 ID
     * @param deleteFile true이면 YAML 파일도 삭제
     * @return 제거 성공 여부
     */
    boolean unregisterSection(String id, boolean deleteFile);

    /**
     * 특정 상점의 컨트롤러를 반환합니다.
     * 컨트롤러를 통해 상점 아이템을 실시간으로 추가/수정/삭제할 수 있습니다.
     * 
     * @param shopId 상점 ID
     * @return ShopController, 상점이 없으면 null
     */
    ShopController getShopController(String shopId);

    /**
     * 모든 상점 데이터를 다시 로드합니다.
     */
    void reloadShops();
}
