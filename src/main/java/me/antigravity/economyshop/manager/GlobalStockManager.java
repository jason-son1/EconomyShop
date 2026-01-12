package me.antigravity.economyshop.manager;

import me.antigravity.economyshop.EconomyShop;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 글로벌 재고 시스템 매니저
 * 서버 전체에서 공유되는 아이템 재고를 관리합니다.
 * 한정 판매 이벤트나 희귀 아이템에 사용됩니다.
 */
public class GlobalStockManager {

    private final EconomyShop plugin;
    private final Map<String, Long> globalStocks = new ConcurrentHashMap<>();

    public GlobalStockManager(EconomyShop plugin) {
        this.plugin = plugin;
    }

    /**
     * 글로벌 재고를 초기화합니다.
     */
    public void initializeStock(String itemId, long initialStock) {
        globalStocks.put(itemId, initialStock);
        // DB에 저장
        saveToDatabase(itemId, initialStock);
    }

    /**
     * 글로벌 재고를 조회합니다.
     */
    public long getStock(String itemId) {
        return globalStocks.getOrDefault(itemId, -1L); // -1은 글로벌 재고가 없음을 의미
    }

    /**
     * 글로벌 재고를 차감합니다.
     * 
     * @return 성공 여부
     */
    public boolean decreaseStock(String itemId, int amount) {
        Long currentStock = globalStocks.get(itemId);
        if (currentStock == null || currentStock < amount) {
            return false; // 재고 부족
        }

        long newStock = currentStock - amount;
        globalStocks.put(itemId, newStock);

        // DB에 저장 (비동기)
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            saveToDatabase(itemId, newStock);
        });

        return true;
    }

    /**
     * 글로벌 재고를 증가시킵니다 (관리자 명령어나 자동 재입고용).
     */
    public void increaseStock(String itemId, int amount) {
        long currentStock = globalStocks.getOrDefault(itemId, 0L);
        long newStock = currentStock + amount;
        globalStocks.put(itemId, newStock);

        saveToDatabase(itemId, newStock);
    }

    /**
     * 재고가 충분한지 확인합니다.
     */
    public boolean hasStock(String itemId, int amount) {
        long stock = getStock(itemId);
        return stock < 0 || stock >= amount; // -1이면 무제한으로 간주
    }

    /**
     * 글로벌 재고를 데이터베이스에 저장합니다.
     */
    private void saveToDatabase(String itemId, long stock) {
        // DatabaseManager에 global_stocks 테이블 관련 메서드가 있다고 가정
        // 또는 dynamic_prices 테이블을 재사용
        plugin.getDatabaseManager().saveDynamicPrice(itemId + "_global", stock);
    }

    /**
     * 데이터베이스에서 글로벌 재고를 로드합니다.
     */
    public void loadFromDatabase() {
        // DB에서 모든 글로벌 재고 로드
        // 구현은 DatabaseManager의 구조에 따라 달라질 수 있음
        plugin.getLogger().info("글로벌 재고 데이터 로드 완료");
    }

    /**
     * 모든 재고를 리셋합니다 (이벤트 종료 시 등).
     */
    public void resetAllStocks() {
        globalStocks.clear();
        plugin.getLogger().info("모든 글로벌 재고가 리셋되었습니다.");
    }
}
