package me.antigravity.economyshop.api.event;

import me.antigravity.economyshop.model.ShopSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 상점이 닫힐 때 호출되는 이벤트입니다.
 * 
 * <p>
 * 활용 예시:
 * </p>
 * <ul>
 * <li>상점 이용 통계 수집</li>
 * <li>후속 GUI 열기</li>
 * <li>세션 정리</li>
 * </ul>
 */
public class ShopCloseEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final ShopSection section;
    private final boolean wasMainMenu;

    /**
     * 상점 닫기 이벤트를 생성합니다.
     * 
     * @param player      상점을 닫는 플레이어
     * @param section     닫히는 상점 섹션 (메인 메뉴였으면 null)
     * @param wasMainMenu 메인 메뉴를 닫는 경우 true
     */
    public ShopCloseEvent(Player player, ShopSection section, boolean wasMainMenu) {
        this.player = player;
        this.section = section;
        this.wasMainMenu = wasMainMenu;
    }

    /**
     * 상점을 닫는 플레이어를 반환합니다.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * 닫히는 상점 섹션을 반환합니다.
     * 메인 메뉴였으면 null입니다.
     */
    public ShopSection getSection() {
        return section;
    }

    /**
     * 메인 메뉴를 닫는 경우인지 확인합니다.
     */
    public boolean wasMainMenu() {
        return wasMainMenu;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
