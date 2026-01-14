package me.antigravity.economyshop.api.event;

import me.antigravity.economyshop.model.ShopSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 상점이 열리기 전에 호출되는 이벤트입니다.
 * 이 이벤트를 취소하면 상점 GUI가 열리지 않습니다.
 * 
 * <p>
 * 활용 예시:
 * </p>
 * <ul>
 * <li>특정 조건에서 상점 접근 차단</li>
 * <li>맞춤형 상점 열기 (필터링된 아이템만 표시)</li>
 * <li>상점 접근 로깅</li>
 * </ul>
 */
public class ShopOpenEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final ShopSection section;
    private final boolean isMainMenu;
    private boolean cancelled;
    private String cancelReason;

    /**
     * 상점 열기 이벤트를 생성합니다.
     * 
     * @param player     상점을 여는 플레이어
     * @param section    열려는 상점 섹션 (메인 메뉴면 null)
     * @param isMainMenu 메인 메뉴를 여는 경우 true
     */
    public ShopOpenEvent(Player player, ShopSection section, boolean isMainMenu) {
        this.player = player;
        this.section = section;
        this.isMainMenu = isMainMenu;
        this.cancelled = false;
    }

    /**
     * 상점을 여는 플레이어를 반환합니다.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * 열려는 상점 섹션을 반환합니다.
     * 메인 메뉴를 여는 경우 null입니다.
     */
    public ShopSection getSection() {
        return section;
    }

    /**
     * 메인 메뉴를 여는 경우인지 확인합니다.
     */
    public boolean isMainMenu() {
        return isMainMenu;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * 취소 사유를 설정합니다.
     * 
     * @param reason 취소 사유
     */
    public void setCancelReason(String reason) {
        this.cancelReason = reason;
    }

    /**
     * 취소 사유를 반환합니다.
     */
    public String getCancelReason() {
        return cancelReason;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
