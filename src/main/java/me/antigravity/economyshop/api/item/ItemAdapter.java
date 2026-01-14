package me.antigravity.economyshop.api.item;

import org.bukkit.inventory.ItemStack;

/**
 * 커스텀 아이템 플러그인(MMOItems, ExecutableItems 등)과의 연동을 위한 어댑터 인터페이스입니다.
 * 외부 플러그인은 이 인터페이스를 구현하여 자신만의 아이템 비교 로직을 제공할 수 있습니다.
 * 
 * <p>
 * 사용 예시:
 * </p>
 * 
 * <pre>
 * public class MMOItemsAdapter implements ItemAdapter {
 *     public boolean matches(ItemStack shopItem, ItemStack playerItem) {
 *         // MMOItems의 고유 ID로 비교
 *         return getMMOItemId(shopItem).equals(getMMOItemId(playerItem));
 *     }
 * }
 * </pre>
 */
public interface ItemAdapter {

    /**
     * 이 어댑터의 고유 이름을 반환합니다.
     * 상점 아이템 설정에서 provider로 지정됩니다.
     * 
     * @return 어댑터 이름 (예: "MMOItems", "ExecutableItems")
     */
    String getName();

    /**
     * 이 어댑터가 현재 사용 가능한지 확인합니다.
     * 관련 플러그인이 설치되지 않았으면 false를 반환해야 합니다.
     * 
     * @return 사용 가능 여부
     */
    boolean isAvailable();

    /**
     * 아이템을 문자열로 직렬화합니다.
     * 이 문자열은 YAML 파일에 저장됩니다.
     * 
     * @param item 직렬화할 ItemStack
     * @return 직렬화된 문자열, 실패 시 null
     */
    String serialize(ItemStack item);

    /**
     * 문자열을 ItemStack으로 역직렬화합니다.
     * 
     * @param data 직렬화된 문자열
     * @return ItemStack, 실패 시 null
     */
    ItemStack deserialize(String data);

    /**
     * 두 아이템이 같은 상품인지 비교합니다.
     * 단순한 isSimilar가 아니라, 플러그인 고유의 비교 로직을 수행합니다.
     * 
     * <p>
     * 예시 (MMOItems):
     * </p>
     * - UUID, 내구도 등은 무시
     * - 아이템 타입과 ID만 비교
     * 
     * @param shopItem   상점에 등록된 아이템
     * @param playerItem 플레이어가 들고 있는 아이템
     * @return 같은 상품이면 true
     */
    boolean matches(ItemStack shopItem, ItemStack playerItem);

    /**
     * 이 어댑터가 특정 아이템을 처리할 수 있는지 확인합니다.
     * 
     * @param item 확인할 ItemStack
     * @return 이 어댑터로 처리 가능하면 true
     */
    boolean canHandle(ItemStack item);
}
