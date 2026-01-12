package me.antigravity.economyshop.manager;

import me.antigravity.economyshop.EconomyShop;
import me.antigravity.economyshop.model.ShopItem;
import org.bukkit.entity.Player;

/**
 * 할인 시스템 매니저
 * 권한 기반 할인율을 계산하고 적용합니다.
 */
public class DiscountManager {

    private final EconomyShop plugin;

    public DiscountManager(EconomyShop plugin) {
        this.plugin = plugin;
    }

    /**
     * 플레이어의 할인율을 계산합니다.
     * 
     * 권한 노드 예시:
     * - economyshop.discount.5 -> 5% 할인
     * - economyshop.discount.10 -> 10% 할인
     * - economyshop.discount.vip -> config에서 정의된 VIP 할인율
     * 
     * @param player 플레이어
     * @param item   아이템 (아이템별 할인이 있을 경우)
     * @return 할인율 (0.0 ~ 1.0, 예: 0.1 = 10% 할인)
     */
    public double getDiscountRate(Player player, ShopItem item) {
        double maxDiscount = 0.0;

        // 숫자 기반 권한 노드 확인 (economyshop.discount.X)
        for (int i = 100; i >= 5; i -= 5) {
            if (player.hasPermission("economyshop.discount." + i)) {
                maxDiscount = Math.max(maxDiscount, i / 100.0);
                break;
            }
        }

        // 이름 기반 권한 노드 (config.yml에서 정의)
        if (player.hasPermission("economyshop.discount.vip")) {
            double vipDiscount = plugin.getConfigManager().getMainConfig().getDouble("discounts.vip", 0.15);
            maxDiscount = Math.max(maxDiscount, vipDiscount);
        }

        if (player.hasPermission("economyshop.discount.mvp")) {
            double mvpDiscount = plugin.getConfigManager().getMainConfig().getDouble("discounts.mvp", 0.25);
            maxDiscount = Math.max(maxDiscount, mvpDiscount);
        }

        if (player.hasPermission("economyshop.discount.premium")) {
            double premiumDiscount = plugin.getConfigManager().getMainConfig().getDouble("discounts.premium", 0.35);
            maxDiscount = Math.max(maxDiscount, premiumDiscount);
        }

        // 최대 할인율 제한 (90%)
        return Math.min(maxDiscount, 0.90);
    }

    /**
     * 할인이 적용된 가격을 계산합니다.
     * 
     * @param player        플레이어
     * @param item          아이템
     * @param originalPrice 원래 가격
     * @return 할인된 가격
     */
    public double getDiscountedPrice(Player player, ShopItem item, double originalPrice) {
        if (player == null)
            return originalPrice;

        double discountRate = getDiscountRate(player, item);
        if (discountRate <= 0)
            return originalPrice;

        double discountedPrice = originalPrice * (1.0 - discountRate);
        return Math.max(0.01, discountedPrice); // 최소 0.01
    }

    /**
     * 할인율을 퍼센트 문자열로 반환합니다.
     */
    public String getDiscountString(Player player, ShopItem item) {
        double rate = getDiscountRate(player, item);
        if (rate <= 0)
            return "";

        return String.format("§a-%d%% 할인", (int) (rate * 100));
    }

    /**
     * 플레이어가 할인 혜택을 받을 수 있는지 확인합니다.
     */
    public boolean hasDiscount(Player player) {
        if (player == null)
            return false;

        // 할인 관련 권한이 하나라도 있는지 확인
        for (int i = 5; i <= 100; i += 5) {
            if (player.hasPermission("economyshop.discount." + i)) {
                return true;
            }
        }

        return player.hasPermission("economyshop.discount.vip")
                || player.hasPermission("economyshop.discount.mvp")
                || player.hasPermission("economyshop.discount.premium");
    }
}
