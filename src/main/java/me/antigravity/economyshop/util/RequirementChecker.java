package me.antigravity.economyshop.util;

import me.antigravity.economyshop.model.ShopItem;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 아이템 구매 요구사항을 검증하는 유틸리티 클래스
 */
public class RequirementChecker {

    /**
     * 플레이어가 아이템 구매 요구사항을 모두 충족하는지 확인합니다.
     * 
     * @param player 플레이어
     * @param item   아이템
     * @return 모든 요구사항 충족 여부
     */
    public static boolean checkRequirements(Player player, ShopItem item) {
        if (player == null || item == null)
            return false;

        // 권한 확인
        if (!checkPermissions(player, item)) {
            return false;
        }

        // 추가 요구사항 확인
        if (!checkCustomRequirements(player, item)) {
            return false;
        }

        return true;
    }

    /**
     * 권한 요구사항을 확인합니다.
     */
    private static boolean checkPermissions(Player player, ShopItem item) {
        List<String> permissions = item.getPermissions();
        if (permissions == null || permissions.isEmpty()) {
            return true; // 권한 요구사항 없음
        }

        // 모든 권한을 가지고 있어야 함
        for (String permission : permissions) {
            if (!player.hasPermission(permission)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 커스텀 요구사항을 확인합니다.
     * requirements 맵에서 다양한 조건을 체크합니다.
     */
    private static boolean checkCustomRequirements(Player player, ShopItem item) {
        Map<String, Object> requirements = item.getRequirements();
        if (requirements == null || requirements.isEmpty()) {
            return true; // 요구사항 없음
        }

        // 레벨 요구사항
        if (requirements.containsKey("level")) {
            int requiredLevel = getIntValue(requirements.get("level"));
            if (player.getLevel() < requiredLevel) {
                return false;
            }
        }

        // 경험치 요구사항
        if (requirements.containsKey("exp")) {
            int requiredExp = getIntValue(requirements.get("exp"));
            if (getTotalExperience(player) < requiredExp) {
                return false;
            }
        }

        // 플레이타임 요구사항 (틱 단위)
        if (requirements.containsKey("playtime")) {
            long requiredPlaytime = getLongValue(requirements.get("playtime"));
            if (player.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE) < requiredPlaytime) {
                return false;
            }
        }

        // 최소 잔액 요구사항
        if (requirements.containsKey("min-balance")) {
            double minBalance = getDoubleValue(requirements.get("min-balance"));
            // Vault 잔액 확인 (간단한 예시)
            // 실제로는 EconomyProvider를 통해 확인해야 함
        }

        return true;
    }

    /**
     * 요구사항 미충족 사유를 반환합니다.
     */
    public static String getFailureReason(Player player, ShopItem item) {
        if (player == null || item == null)
            return "알 수 없는 오류";

        // 권한 확인
        List<String> permissions = item.getPermissions();
        if (permissions != null && !permissions.isEmpty()) {
            for (String permission : permissions) {
                if (!player.hasPermission(permission)) {
                    return "§c필요 권한: §e" + permission;
                }
            }
        }

        // 요구사항 확인
        Map<String, Object> requirements = item.getRequirements();
        if (requirements != null && !requirements.isEmpty()) {
            if (requirements.containsKey("level")) {
                int requiredLevel = getIntValue(requirements.get("level"));
                if (player.getLevel() < requiredLevel) {
                    return String.format("§c필요 레벨: §e%d §7(현재: %d)", requiredLevel, player.getLevel());
                }
            }

            if (requirements.containsKey("exp")) {
                int requiredExp = getIntValue(requirements.get("exp"));
                int currentExp = getTotalExperience(player);
                if (currentExp < requiredExp) {
                    return String.format("§c필요 경험치: §e%,d §7(현재: %,d)", requiredExp, currentExp);
                }
            }

            if (requirements.containsKey("playtime")) {
                long requiredPlaytime = getLongValue(requirements.get("playtime"));
                long currentPlaytime = player.getStatistic(org.bukkit.Statistic.PLAY_ONE_MINUTE);
                if (currentPlaytime < requiredPlaytime) {
                    long requiredHours = requiredPlaytime / 72000; // 틱 → 시간
                    long currentHours = currentPlaytime / 72000;
                    return String.format("§c필요 플레이타임: §e%d시간 §7(현재: %d시간)", requiredHours, currentHours);
                }
            }
        }

        return "§c요구사항을 충족하지 못했습니다.";
    }

    /**
     * 요구사항 목록을 문자열 리스트로 반환합니다 (GUI 로어용).
     */
    public static List<String> getRequirementsList(ShopItem item) {
        List<String> list = new ArrayList<>();

        if (item.getPermissions() != null && !item.getPermissions().isEmpty()) {
            list.add("§7필요 권한:");
            for (String perm : item.getPermissions()) {
                list.add("  §e- " + perm);
            }
        }

        Map<String, Object> requirements = item.getRequirements();
        if (requirements != null && !requirements.isEmpty()) {
            if (requirements.containsKey("level")) {
                list.add("§7필요 레벨: §e" + requirements.get("level"));
            }
            if (requirements.containsKey("exp")) {
                list.add("§7필요 경험치: §e" + String.format("%,d", getIntValue(requirements.get("exp"))));
            }
            if (requirements.containsKey("playtime")) {
                long hours = getLongValue(requirements.get("playtime")) / 72000;
                list.add("§7필요 플레이타임: §e" + hours + "시간");
            }
        }

        return list;
    }

    // Helper methods
    private static int getIntValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static long getLongValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private static double getDoubleValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static int getTotalExperience(Player player) {
        int level = player.getLevel();
        float exp = player.getExp();

        int totalExp = 0;

        if (level <= 16) {
            totalExp = (int) (level * level + 6 * level);
        } else if (level <= 31) {
            totalExp = (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            totalExp = (int) (4.5 * level * level - 162.5 * level + 2220);
        }

        totalExp += Math.round(exp * getExpToNextLevel(level));

        return totalExp;
    }

    private static int getExpToNextLevel(int level) {
        if (level <= 15) {
            return 2 * level + 7;
        } else if (level <= 30) {
            return 5 * level - 38;
        } else {
            return 9 * level - 158;
        }
    }
}
