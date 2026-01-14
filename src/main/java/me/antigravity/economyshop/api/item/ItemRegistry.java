package me.antigravity.economyshop.api.item;

import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * ItemAdapter를 관리하는 레지스트리입니다.
 * 외부 플러그인은 이 인터페이스를 통해 커스텀 아이템 어댑터를 등록할 수 있습니다.
 * 
 * <p>
 * 사용 예시:
 * </p>
 * 
 * <pre>
 * ItemRegistry registry = EconomyShopAPI.getInstance().getItemRegistry();
 * registry.registerAdapter(new MMOItemsAdapter());
 * </pre>
 */
public interface ItemRegistry {

    /**
     * 새로운 ItemAdapter를 등록합니다.
     * 
     * @param adapter 등록할 ItemAdapter 구현체
     * @return 등록 성공 여부 (이미 같은 이름이 존재하면 false)
     */
    boolean registerAdapter(ItemAdapter adapter);

    /**
     * 등록된 ItemAdapter를 제거합니다.
     * 
     * @param name 어댑터 이름
     * @return 제거 성공 여부
     */
    boolean unregisterAdapter(String name);

    /**
     * 이름으로 ItemAdapter를 조회합니다.
     * 
     * @param name 어댑터 이름
     * @return ItemAdapter, 없으면 null
     */
    ItemAdapter getAdapter(String name);

    /**
     * 아이템에 적합한 ItemAdapter를 자동으로 찾습니다.
     * 등록된 어댑터 중 canHandle()이 true를 반환하는 첫 번째 어댑터를 반환합니다.
     * 
     * @param item 확인할 ItemStack
     * @return 적합한 ItemAdapter, 없으면 기본 바닐라 어댑터
     */
    ItemAdapter findAdapter(ItemStack item);

    /**
     * 기본 바닐라 아이템 어댑터를 반환합니다.
     * 
     * @return 기본 ItemAdapter
     */
    ItemAdapter getDefaultAdapter();

    /**
     * 등록된 모든 어댑터를 반환합니다.
     * 
     * @return 어댑터 이름과 인스턴스의 맵 (읽기 전용)
     */
    Map<String, ItemAdapter> getAllAdapters();

    /**
     * 두 아이템이 같은 상품인지 비교합니다.
     * 적절한 어댑터를 자동으로 선택하여 비교합니다.
     * 
     * @param shopItem   상점에 등록된 아이템
     * @param playerItem 플레이어가 가진 아이템
     * @return 같은 상품이면 true
     */
    boolean matches(ItemStack shopItem, ItemStack playerItem);

    /**
     * 두 아이템이 같은 상품인지 특정 어댑터로 비교합니다.
     * 
     * @param adapterName 사용할 어댑터 이름
     * @param shopItem    상점에 등록된 아이템
     * @param playerItem  플레이어가 가진 아이템
     * @return 같은 상품이면 true
     */
    boolean matches(String adapterName, ItemStack shopItem, ItemStack playerItem);
}
