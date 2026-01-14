package me.antigravity.economyshop.api.builder;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * 상점 섹션을 Fluent API로 생성하는 빌더입니다.
 * 
 * <p>
 * 사용 예시:
 * </p>
 * 
 * <pre>
 * EconomyShopAPI.getInstance().createShopBuilder()
 *         .id("rpg_weapons")
 *         .displayName("&c무기 상점")
 *         .icon(Material.IRON_SWORD)
 *         .slot(12)
 *         .permission("shop.weapons")
 *         .economy("Vault")
 *         .buildAndSave();
 * </pre>
 */
public interface ShopBuilder {

    /**
     * 상점의 고유 ID를 설정합니다.
     * 이 ID는 파일명과 내부 식별자로 사용됩니다.
     * 
     * @param id 고유 ID (영문, 숫자, 언더스코어만 권장)
     * @return this
     */
    ShopBuilder id(String id);

    /**
     * 상점의 표시 이름을 설정합니다.
     * 색상 코드(&c, &a 등)를 지원합니다.
     * 
     * @param displayName 표시 이름
     * @return this
     */
    ShopBuilder displayName(String displayName);

    /**
     * 메인 메뉴에서 사용할 아이콘을 설정합니다.
     * 
     * @param icon 아이콘 ItemStack
     * @return this
     */
    ShopBuilder icon(ItemStack icon);

    /**
     * 메인 메뉴에서 사용할 아이콘을 Material로 설정합니다.
     * 
     * @param material 아이콘 Material
     * @return this
     */
    ShopBuilder icon(Material material);

    /**
     * 메인 메뉴에서의 슬롯 위치를 설정합니다.
     * 
     * @param slot 슬롯 번호 (0-53)
     * @return this
     */
    ShopBuilder slot(int slot);

    /**
     * 상점 접근에 필요한 권한을 설정합니다.
     * 
     * @param permission 권한 노드 (null이면 권한 검사 안 함)
     * @return this
     */
    ShopBuilder permission(String permission);

    /**
     * 이 상점에서 사용할 경제 시스템을 설정합니다.
     * 
     * @param economyId 경제 시스템 ID (예: "Vault", "PlayerPoints", "Item:EMERALD")
     * @return this
     */
    ShopBuilder economy(String economyId);

    /**
     * 이 상점을 생성한 플러그인 이름을 설정합니다.
     * 추후 관리 및 디버깅에 유용합니다.
     * 
     * @param pluginName 플러그인 이름
     * @return this
     */
    ShopBuilder ownerPlugin(String pluginName);

    /**
     * 상점을 빌드하고 메모리에만 등록합니다.
     * 서버 재시작 시 상점이 사라집니다.
     * 
     * @return 성공 여부
     * @throws IllegalStateException 필수 필드(id)가 설정되지 않은 경우
     */
    boolean build();

    /**
     * 상점을 빌드하고 파일에 영구 저장합니다.
     * sections.yml 및 shops/{id}.yml 파일이 생성됩니다.
     * 
     * @return 성공 여부
     * @throws IllegalStateException 필수 필드(id)가 설정되지 않은 경우
     */
    boolean buildAndSave();
}
