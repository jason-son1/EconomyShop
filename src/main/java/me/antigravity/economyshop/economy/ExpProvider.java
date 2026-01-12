package me.antigravity.economyshop.economy;

import me.antigravity.economyshop.EconomyShop;
import org.bukkit.entity.Player;

/**
 * 경험치(EXP) 레벨을 화폐로 사용하는 Provider 구현체입니다.
 * 인챈트 상점 등에서 유용하게 사용할 수 있습니다.
 */
public class ExpProvider implements EconomyProvider {

    public ExpProvider(EconomyShop plugin) {
        // plugin field was removed as it was unused.
        // The constructor remains to satisfy potential external instantiation,
        // but the 'plugin' parameter is now unused within this class.
    }

    @Override
    public String getName() {
        return "EXP";
    }

    @Override
    public boolean isAvailable() {
        return true; // EXP는 항상 사용 가능
    }

    @Override
    public double getBalance(Player player) {
        return getTotalExperience(player);
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        int currentExp = getTotalExperience(player);
        if (currentExp < amount)
            return false;

        setTotalExperience(player, (int) (currentExp - amount));
        return true;
    }

    @Override
    public boolean deposit(Player player, double amount) {
        int currentExp = getTotalExperience(player);
        setTotalExperience(player, (int) (currentExp + amount));
        return true;
    }

    @Override
    public String formatAmount(double amount) {
        return String.format("%,d EXP", (int) amount);
    }

    @Override
    public String getCurrencyName() {
        return "경험치";
    }

    /**
     * 플레이어의 총 경험치를 계산합니다.
     */
    private int getTotalExperience(Player player) {
        int level = player.getLevel();
        float exp = player.getExp();

        int totalExp = 0;

        // 레벨까지의 누적 경험치 계산
        if (level <= 16) {
            totalExp = (int) (level * level + 6 * level);
        } else if (level <= 31) {
            totalExp = (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            totalExp = (int) (4.5 * level * level - 162.5 * level + 2220);
        }

        // 현재 레벨에서의 진행도 추가
        totalExp += Math.round(exp * getExpToNextLevel(level));

        return totalExp;
    }

    /**
     * 플레이어의 총 경험치를 설정합니다.
     */
    private void setTotalExperience(Player player, int exp) {
        player.setExp(0);
        player.setLevel(0);
        player.setTotalExperience(0);

        int level = 0;
        while (exp > 0) {
            int expToNext = getExpToNextLevel(level);
            if (exp >= expToNext) {
                exp -= expToNext;
                level++;
            } else {
                break;
            }
        }

        player.setLevel(level);
        int expToNext = getExpToNextLevel(level);
        if (expToNext > 0) {
            player.setExp((float) exp / expToNext);
        }
    }

    /**
     * 다음 레벨까지 필요한 경험치를 반환합니다.
     */
    private int getExpToNextLevel(int level) {
        if (level <= 15) {
            return 2 * level + 7;
        } else if (level <= 30) {
            return 5 * level - 38;
        } else {
            return 9 * level - 158;
        }
    }
}
