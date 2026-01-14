package me.antigravity.economyshop.api.event;

import me.antigravity.economyshop.model.ShopItem;
import me.antigravity.economyshop.model.ShopSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 상점 거래가 실행되기 전에 호출되는 이벤트입니다.
 * 이 이벤트를 취소하면 거래가 진행되지 않습니다.
 * 
 * <p>
 * 활용 예시:
 * </p>
 * <ul>
 * <li>특정 조건(퀘스트 미달성 등)에서 거래 차단</li>
 * <li>동적 가격 변동(세금 적용, 할인 이벤트)</li>
 * <li>구매/판매 수량 제한</li>
 * </ul>
 */
public class ShopPreTransactionEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final ShopSection section;
    private final ShopItem shopItem;
    private final TransactionType type;
    private int amount;
    private double price;
    private boolean cancelled;
    private String cancelReason;

    /**
     * 거래 전 이벤트를 생성합니다.
     * 
     * @param player   거래를 시도하는 플레이어
     * @param section  상점 섹션
     * @param shopItem 거래 대상 아이템
     * @param type     거래 유형 (구매/판매)
     * @param amount   거래 수량
     * @param price    총 거래 금액
     */
    public ShopPreTransactionEvent(Player player, ShopSection section, ShopItem shopItem,
            TransactionType type, int amount, double price) {
        this.player = player;
        this.section = section;
        this.shopItem = shopItem;
        this.type = type;
        this.amount = amount;
        this.price = price;
        this.cancelled = false;
    }

    /**
     * 거래를 시도하는 플레이어를 반환합니다.
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
     * 거래 대상 아이템을 반환합니다.
     */
    public ShopItem getShopItem() {
        return shopItem;
    }

    /**
     * 거래 유형을 반환합니다.
     */
    public TransactionType getType() {
        return type;
    }

    /**
     * 거래 수량을 반환합니다.
     */
    public int getAmount() {
        return amount;
    }

    /**
     * 거래 수량을 변경합니다.
     * 
     * @param amount 새 거래 수량 (1 이상)
     */
    public void setAmount(int amount) {
        if (amount >= 1) {
            this.amount = amount;
        }
    }

    /**
     * 총 거래 금액을 반환합니다.
     */
    public double getPrice() {
        return price;
    }

    /**
     * 거래 금액을 변경합니다.
     * 세금, 할인 등을 적용할 때 사용합니다.
     * 
     * @param price 새 거래 금액 (0 이상)
     */
    public void setPrice(double price) {
        if (price >= 0) {
            this.price = price;
        }
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
     * 이 메시지는 플레이어에게 표시됩니다.
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

    /**
     * 거래 유형을 나타내는 열거형입니다.
     */
    public enum TransactionType {
        /** 구매 */
        BUY,
        /** 판매 */
        SELL
    }
}
