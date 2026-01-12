package me.antigravity.economyshop.task;

import me.antigravity.economyshop.EconomyShop;
import me.antigravity.economyshop.model.ShopItem;
import me.antigravity.economyshop.model.ShopSection;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 동적 가격이 적용된 아이템의 가격을 시간이 지남에 따라 기준 가격으로 복구시키는 스케줄러입니다.
 * 경제 과열/침체를 방지하고 시장 안정성을 유지합니다.
 * 
 * 알고리즘: P_next = P_current + (P_target - P_current) * restoration_rate
 * 이 방식은 가격이 목표값에 가까워질수록 변화량이 줄어드는 지수적 감쇠(Exponential Decay) 형태를 띱니다.
 */
public class PriceRestorationTask extends BukkitRunnable {

    private final EconomyShop plugin;
    private final double restorationRate;

    /**
     * @param plugin          플러그인 인스턴스
     * @param restorationRate 복구율 (0.0 ~ 1.0, 예: 0.05 = 5%)
     */
    public PriceRestorationTask(EconomyShop plugin, double restorationRate) {
        this.plugin = plugin;
        this.restorationRate = Math.max(0.01, Math.min(1.0, restorationRate));
    }

    @Override
    public void run() {
        int updatedCount = 0;

        for (ShopSection section : plugin.getShopManager().getSections().values()) {
            for (ShopItem item : section.getItems()) {
                if (!item.isDynamicPricing())
                    continue;

                // 현재 재고량과 목표 재고량(maxStock)의 차이를 복구
                long currentStock = item.getCurrentStock();
                long targetStock = item.getMaxStock();

                if (currentStock == targetStock)
                    continue;

                // 재고를 목표치로 서서히 복구
                // Stock이 높을수록 가격이 낮아지므로, 재고를 복구하면 가격도 복구됨
                long stockDifference = targetStock - currentStock;
                long adjustment = (long) Math.ceil(Math.abs(stockDifference) * restorationRate);

                if (adjustment == 0)
                    adjustment = 1; // 최소 1단위 변경

                long newStock;
                if (stockDifference > 0) {
                    // 재고가 부족한 경우 → 재고 증가
                    newStock = Math.min(targetStock, currentStock + adjustment);
                } else {
                    // 재고가 과잉인 경우 → 재고 감소
                    newStock = Math.max(targetStock, currentStock - adjustment);
                }

                item.setCurrentStock(newStock);

                // 데이터베이스에 저장
                plugin.getDatabaseManager().saveDynamicPrice(item.getId(), newStock);

                updatedCount++;
            }
        }

        if (updatedCount > 0) {
            plugin.getLogger().fine("동적 가격 복구 작업 완료: " + updatedCount + "개 아이템 조정됨");
        }
    }

    /**
     * 스케줄러를 시작합니다.
     * 
     * @param intervalMinutes 실행 주기 (분 단위)
     */
    public void start(int intervalMinutes) {
        long intervalTicks = intervalMinutes * 60L * 20L; // 분 → 틱 (1초 = 20틱)
        this.runTaskTimerAsynchronously(plugin, intervalTicks, intervalTicks);
        plugin.getLogger()
                .info("동적 가격 복구 스케줄러 시작됨 (주기: " + intervalMinutes + "분, 복구율: " + (int) (restorationRate * 100) + "%)");
    }
}
