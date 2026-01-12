package me.antigravity.economyshop.manager;

import me.antigravity.economyshop.EconomyShop;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LimitManager {

    private final EconomyShop plugin;
    // UUID -> (ItemID -> Amount)
    private final Map<UUID, Map<String, Integer>> playerDailyLimits = new HashMap<>();

    public LimitManager(EconomyShop plugin) {
        this.plugin = plugin;
    }

    /**
     * DB에서 플레이어의 제한 정보를 로드합니다. (접속 시 호출)
     */
    public void loadLimits(UUID uuid) {
        // 비동기로 처리할지는 호출부에서 결정하거나 여기서 runTaskAsynchronously 사용
        // 현재 구조상 DB 매니저가 connection을 따오므로, ShopManager 등에서 미리 로드하는 것이 좋음
        // 여기서는 필요할 때 로드하는 방식(Lazy Load) 또는 접속 시 전체 로드를 가정
        // 성능을 위해, 접속 시 '오늘' 날짜의 데이터만 로드하여 메모리에 캐싱합니다.

        // 이 예제에서는 단순화를 위해 메모리 맵만 초기화하고,
        // 실제 값은 getOrDefault로 처리하되, 영구 저장이 필요하면
        // recordPurchase 시 DB에 즉시 저장하는 방식을 사용합니다.
        // *읽기*의 경우, 접속 시 DB에서 읽어와야 합니다.
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            // 실제 구현: 모든 아이템에 대해 쿼리하는 것은 비효율적이므로,
            // SELECT * FROM player_limits WHERE uuid = ? AND reset_date = CURRENT_DATE
            // 와 같은 쿼리가 DatabaseManager에 필요함.
            // 현재 DatabaseManager.loadPlayerLimit는 단일 아이템 조회용임.
            // 일단은 메모리 캐시를 비워두고 필요시 DB조회 하거나,
            // 접속 시 로드하는 로직을 추가해야 함.
        });
    }

    /**
     * 플레이어 퇴장 시 메모리 정리
     */
    public void unloadLimits(UUID uuid) {
        playerDailyLimits.remove(uuid);
    }

    /**
     * 플레이어가 해당 아이템을 더 구매할 수 있는지 확인합니다.
     */
    public boolean canPurchase(UUID uuid, String itemId, int limit) {
        if (limit <= 0)
            return true;

        Map<String, Integer> playerMap = playerDailyLimits.computeIfAbsent(uuid, k -> new HashMap<>());

        // 메모리에 없으면 DB에서 확인 (Lazy Loading)
        if (!playerMap.containsKey(itemId)) {
            // 동기적으로 DB 조회 (주의: 메인 스레드 멈춤 가능성 있음.
            // 하지만 구매 클릭 시점이라 정확성이 중요함.
            // 최적화를 위해 접속 시 미리 로드하는 것을 권장)
            int dbCount = plugin.getDatabaseManager().loadPlayerLimit(uuid.toString(), itemId);
            playerMap.put(itemId, dbCount);
        }

        int current = playerMap.getOrDefault(itemId, 0);
        return current < limit;
    }

    /**
     * 플레이어의 구매 기록을 추가하고 DB에 저장합니다.
     */
    public void recordPurchase(UUID uuid, String itemId, int amount) {
        Map<String, Integer> playerMap = playerDailyLimits.computeIfAbsent(uuid, k -> new HashMap<>());
        int current = playerMap.getOrDefault(itemId, 0);
        int newVal = current + amount;
        playerMap.put(itemId, newVal);

        // 비동기 DB 저장
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDatabaseManager().savePlayerLimit(uuid.toString(), itemId, newVal);
        });
    }

    /**
     * 현재 사용량을 조회합니다.
     */
    public int getCurrentUsage(UUID uuid, String itemId) {
        Map<String, Integer> playerMap = playerDailyLimits.computeIfAbsent(uuid, k -> new HashMap<>());
        if (!playerMap.containsKey(itemId)) {
            int dbCount = plugin.getDatabaseManager().loadPlayerLimit(uuid.toString(), itemId);
            playerMap.put(itemId, dbCount);
        }
        return playerMap.getOrDefault(itemId, 0);
    }

    /**
     * 일일 제한을 초기화합니다. (자정에 실행될 스케줄러용)
     */
    public void resetLimits() {
        playerDailyLimits.clear();
        // DB에서도 오늘 날짜가 아닌 레코드는 무효하므로
        // 굳이 DELETE를 날리지 않아도 reset_date 체크로 걸러짐.
        // 필요하다면 배치를 통해 오래된 데이터를 정리하는 로직이 DatabaseManager에 있어야 함.
        plugin.getLogger().info("일일 구매 제한 기록이 초기화되었습니다.");
    }
}
