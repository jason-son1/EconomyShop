package me.antigravity.economyshop.economy;

import me.antigravity.economyshop.EconomyShop;
import org.bukkit.entity.Player;

/**
 * PlayerPoints 플러그인을 위한 Provider 구현체입니다.
 * PlayerPoints가 설치되지 않은 경우에도 플러그인이 작동하도록 soft dependency로 처리합니다.
 */
public class PlayerPointsProvider implements EconomyProvider {

    private final EconomyShop plugin;
    private Object playerPointsAPI; // PlayerPointsAPI 타입을 직접 참조하지 않음 (soft dependency)
    private boolean available = false;

    public PlayerPointsProvider(EconomyShop plugin) {
        this.plugin = plugin;
        setupPlayerPoints();
    }

    private void setupPlayerPoints() {
        if (plugin.getServer().getPluginManager().getPlugin("PlayerPoints") == null) {
            plugin.getLogger().info("PlayerPoints 플러그인이 설치되지 않았습니다. PlayerPoints 경제가 비활성화됩니다.");
            return;
        }

        try {
            // Reflection을 사용하여 PlayerPoints API에 접근 (soft dependency)
            Class<?> playerPointsClass = Class.forName("org.black_iern.playerpoints.PlayerPoints");
            Object playerPoints = plugin.getServer().getPluginManager().getPlugin("PlayerPoints");

            if (playerPoints != null) {
                java.lang.reflect.Method getAPIMethod = playerPointsClass.getMethod("getAPI");
                playerPointsAPI = getAPIMethod.invoke(playerPoints);
                available = true;
                plugin.getLogger().info("PlayerPoints 연동이 활성화되었습니다.");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("PlayerPoints API 연동 중 오류 발생: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "PlayerPoints";
    }

    @Override
    public boolean isAvailable() {
        return available && playerPointsAPI != null;
    }

    @Override
    public double getBalance(Player player) {
        if (!isAvailable())
            return 0;
        try {
            java.lang.reflect.Method lookMethod = playerPointsAPI.getClass().getMethod("look", java.util.UUID.class);
            return (int) lookMethod.invoke(playerPointsAPI, player.getUniqueId());
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        if (!isAvailable())
            return false;
        try {
            java.lang.reflect.Method takeMethod = playerPointsAPI.getClass().getMethod("take", java.util.UUID.class,
                    int.class);
            return (boolean) takeMethod.invoke(playerPointsAPI, player.getUniqueId(), (int) amount);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean deposit(Player player, double amount) {
        if (!isAvailable())
            return false;
        try {
            java.lang.reflect.Method giveMethod = playerPointsAPI.getClass().getMethod("give", java.util.UUID.class,
                    int.class);
            return (boolean) giveMethod.invoke(playerPointsAPI, player.getUniqueId(), (int) amount);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String formatAmount(double amount) {
        return String.format("%,d 포인트", (int) amount);
    }

    @Override
    public String getCurrencyName() {
        return "포인트";
    }
}
