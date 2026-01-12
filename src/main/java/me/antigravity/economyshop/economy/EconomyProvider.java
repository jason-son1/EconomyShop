package me.antigravity.economyshop.economy;

import org.bukkit.entity.Player;

/**
 * 다양한 경제 시스템을 추상화하는 인터페이스입니다.
 * Vault, PlayerPoints, EXP, Item 기반 경제 등을 지원합니다.
 */
public interface EconomyProvider {

    /**
     * 경제 시스템의 이름을 반환합니다.
     */
    String getName();

    /**
     * 이 경제 시스템이 현재 사용 가능한지 확인합니다.
     */
    boolean isAvailable();

    /**
     * 플레이어의 잔액을 반환합니다.
     */
    double getBalance(Player player);

    /**
     * 플레이어로부터 금액을 차감합니다.
     * 
     * @return 성공 여부
     */
    boolean withdraw(Player player, double amount);

    /**
     * 플레이어에게 금액을 입금합니다.
     * 
     * @return 성공 여부
     */
    boolean deposit(Player player, double amount);

    /**
     * 플레이어가 해당 금액을 가지고 있는지 확인합니다.
     */
    default boolean has(Player player, double amount) {
        return getBalance(player) >= amount;
    }

    /**
     * 금액을 포맷팅하여 문자열로 반환합니다.
     */
    String formatAmount(double amount);

    /**
     * 화폐 단위 이름을 반환합니다.
     */
    String getCurrencyName();
}
