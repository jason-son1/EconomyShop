package me.antigravity.economyshop.task;

import me.antigravity.economyshop.EconomyShop;
import me.antigravity.economyshop.gui.ShopGUIHolder;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 보고 있는 상점 GUI의 가격을 주기적으로 갱신해주는 태스크입니다.
 * 동적 가격 변동을 실시간으로 반영하기 위해 사용됩니다.
 */
public class AutoRefreshTask extends BukkitRunnable {

    private final EconomyShop plugin;

    public AutoRefreshTask(EconomyShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof ShopGUIHolder holder) {
                // 최적화: 현재 페이지에 동적 가격 아이템이 있는지 확인 후 렌더링
                // 여기서는 무조껀 갱신 (단순화 및 확실한 업데이트)
                plugin.getGuiManager().renderPage(holder);
            }
        }
    }

    public void start() {
        // 20틱(1초)마다 실행
        this.runTaskTimer(plugin, 20L, 20L);
        plugin.getLogger().info("상점 GUI 자동 새로고침 태스크가 시작되었습니다.");
    }
}
