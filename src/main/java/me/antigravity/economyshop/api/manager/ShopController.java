package me.antigravity.economyshop.api.manager;

import me.antigravity.economyshop.model.ShopItem;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * 특정 상점을 실시간으로 제어하는 인터페이스입니다.
 * 상점 아이템의 추가, 수정, 삭제 및 설정 변경을 지원합니다.
 * 
 * <p>
 * 사용 예시:
 * </p>
 * 
 * <pre>
 * ShopController controller = api.getShopManager().getShopController("weapons");
 * controller.addItem(diamondSword, 1000.0, 500.0);
 * controller.updateTitle("&c전설 무기 상점");
 * </pre>
 */
public interface ShopController {

    /**
     * 상점 ID를 반환합니다.
     * 
     * @return 상점 ID
     */
    String getShopId();

    /**
     * 상점에 새 아이템을 추가합니다.
     * 아이템은 다음 빈 슬롯에 배치됩니다.
     * 
     * @param item      추가할 ItemStack
     * @param buyPrice  구매 가격
     * @param sellPrice 판매 가격
     * @return 생성된 ShopItem
     */
    ShopItem addItem(ItemStack item, double buyPrice, double sellPrice);

    /**
     * 상점에 새 아이템을 특정 슬롯에 추가합니다.
     * 
     * @param item      추가할 ItemStack
     * @param buyPrice  구매 가격
     * @param sellPrice 판매 가격
     * @param slot      배치할 슬롯 위치
     * @return 생성된 ShopItem
     */
    ShopItem addItem(ItemStack item, double buyPrice, double sellPrice, int slot);

    /**
     * 상점에서 아이템을 제거합니다.
     * 
     * @param itemId 아이템 ID
     * @return 제거 성공 여부
     */
    boolean removeItem(String itemId);

    /**
     * 아이템의 가격을 수정합니다.
     * 
     * @param itemId       아이템 ID
     * @param newBuyPrice  새 구매 가격 (음수면 변경 안 함)
     * @param newSellPrice 새 판매 가격 (음수면 변경 안 함)
     * @return 수정 성공 여부
     */
    boolean updateItemPrice(String itemId, double newBuyPrice, double newSellPrice);

    /**
     * 상점 표시 이름을 변경합니다.
     * 
     * @param newTitle 새 표시 이름 (색상 코드 지원)
     */
    void updateTitle(String newTitle);

    /**
     * 상점에 등록된 모든 아이템을 반환합니다.
     * 
     * @return ShopItem 목록 (읽기 전용)
     */
    List<ShopItem> getItems();

    /**
     * ID로 특정 아이템을 조회합니다.
     * 
     * @param itemId 아이템 ID
     * @return ShopItem, 없으면 null
     */
    ShopItem getItem(String itemId);

    /**
     * 변경사항을 파일에 저장합니다.
     */
    void save();
}
