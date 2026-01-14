package me.antigravity.economyshop.api.event;

import me.antigravity.economyshop.model.ShopItem;
import me.antigravity.economyshop.model.ShopSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 상점 거래가 성공적으로 완료된 후 호출되는 이벤트입니다.
 * 
 * <p>
 * 활용 예시:
 * </p>
 * <ul>
 * <li>거래 로그 기록</li>
 * <li>퀘스트 진행도 업데이트</li>
 * <li>디스코드 알림 전송</li>
 * <li>업적 시스템 연동</li>
 * </ul>
 */
public class ShopPostTransactionEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final ShopSection section;
    private final ShopItem shopItem;
    private final ShopPreTransactionEvent.TransactionType type;
    private final int amount;
    private final double price;

    /**
     * 거래 후 이벤트를 생성합니다.
     * 
     * @param player   거래를 완료한 플레이어
     * @param section  상점 섹션
     * @param shopItem 거래된 아이템
     * @param type     거래 유형 (구매/판매)
     * @param amount   거래 수량
     * @param price    총 거래 금액
     */
    public ShopPostTransactionEvent(Player player, ShopSection section, ShopItem shopItem,
            ShopPreTransactionEvent.TransactionType type, int amount, double price) {
        this.player = player;
        this.section = section;
        this.shopItem = shopItem;
        this.type = type;
        this.amount = amount;
        this.price = price;
    }

    /**
     * 거래를 완료한 플레이어를 반환합니다.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * 상점 섹션을 반환합니다.
     */
    public ShopSection getSection() {
        return section;
    }

    /**
     * 거래된 아이템을 반환합니다.
     */
    public ShopItem getShopItem() {
        return shopItem;
    }

    /**
     * 거래 유형을 반환합니다.
     */
    public ShopPreTransactionEvent.TransactionType getType() {
        return type;
    }

    /**
     * 거래 수량을 반환합니다.
     */
    public int getAmount() {
        return amount;
    }

    /**
     * 총 거래 금액을 반환합니다.
     */
    public double getPrice() {
        return price;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
