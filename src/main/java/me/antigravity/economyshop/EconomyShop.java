package me.antigravity.economyshop;

import lombok.Getter;
import me.antigravity.economyshop.command.EditShopCommand;
import me.antigravity.economyshop.command.ShopCommand;
import me.antigravity.economyshop.listener.NPCListener;
import me.antigravity.economyshop.listener.ShopListener;
import me.antigravity.economyshop.manager.ConfigManager;
import me.antigravity.economyshop.manager.DatabaseManager;
import me.antigravity.economyshop.manager.EconomyManager;
import me.antigravity.economyshop.manager.EditorManager;
import me.antigravity.economyshop.manager.GUIManager;
import me.antigravity.economyshop.manager.LangManager;
import me.antigravity.economyshop.manager.LimitManager;
import me.antigravity.economyshop.manager.LogManager;
import me.antigravity.economyshop.manager.SellGUIManager;
import me.antigravity.economyshop.manager.ShopManager;
import org.bukkit.plugin.java.JavaPlugin;

public class EconomyShop extends JavaPlugin {

    @Getter
    private static EconomyShop instance;

    @Getter
    private ConfigManager configManager;
    @Getter
    private ShopManager shopManager;
    @Getter
    private EconomyManager economyManager;
    @Getter
    private GUIManager guiManager;
    @Getter
    private LimitManager limitManager;
    @Getter
    private EditorManager editorManager;
    @Getter
    private DatabaseManager databaseManager;
    @Getter
    private LangManager langManager;
    @Getter
    private SellGUIManager sellGUIManager;
    @Getter
    private LogManager logManager;
    @Getter
    private me.antigravity.economyshop.hook.OraxenHook oraxenHook;
    @Getter
    private me.antigravity.economyshop.hook.ItemsAdderHook itemsAdderHook;

    @Override
    public void onEnable() {
        instance = this;

        // 훅 초기화
        this.oraxenHook = new me.antigravity.economyshop.hook.OraxenHook();
        this.itemsAdderHook = new me.antigravity.economyshop.hook.ItemsAdderHook();

        // 매니저 초기화
        this.configManager = new ConfigManager(this);
        this.logManager = new LogManager(this); // 로그는 최대한 빨리 초기화
        this.langManager = new LangManager(this); // ConfigManager 후, 다른 매니저 전
        this.economyManager = new EconomyManager(this);
        this.shopManager = new ShopManager(this);
        this.guiManager = new GUIManager(this);
        this.limitManager = new LimitManager(this);
        this.editorManager = new EditorManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.sellGUIManager = new SellGUIManager(this);

        // 데이터 로드
        this.configManager.loadConfigs();
        this.databaseManager.initialize(); // DB 연결
        this.shopManager.loadShops();

        // 명령어 및 리스너 등록
        ShopCommand shopCommand = new ShopCommand(this);
        getCommand("shop").setExecutor(shopCommand);
        getCommand("shop").setTabCompleter(shopCommand);

        EditShopCommand editShopCommand = new EditShopCommand(this);
        getCommand("editshop").setExecutor(editShopCommand);
        getCommand("editshop").setTabCompleter(editShopCommand);

        getCommand("sellgui").setExecutor(new me.antigravity.economyshop.command.SellGUICommand(this));
        getCommand("sellall").setExecutor(new me.antigravity.economyshop.command.SellAllCommand(this));

        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new me.antigravity.economyshop.listener.EditorChatListener(this),
                this);

        // Citizens 연동 리스너 등록
        if (getServer().getPluginManager().getPlugin("Citizens") != null) {
            getServer().getPluginManager().registerEvents(new NPCListener(this), this);
            getLogger().info("Citizens NPC 연동이 활성화되었습니다.");
        }

        // PlaceholderAPI 연동
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new me.antigravity.economyshop.hook.PapiHook(this).register();
            getLogger().info("PlaceholderAPI 연동이 활성화되었습니다.");
        }

        // 동적 가격 복구 스케줄러 시작
        startPriceRestorationScheduler();

        // GUI 자동 새로고침 태스크 시작
        new me.antigravity.economyshop.task.AutoRefreshTask(this).start();

        getLogger().info("EconomyShop 플러그인이 활성화되었습니다!");
    }

    /**
     * 동적 가격 복구 스케줄러를 시작합니다.
     */
    private void startPriceRestorationScheduler() {
        int interval = configManager.getMainConfig().getInt("price-restoration-interval", 60);
        double rate = configManager.getMainConfig().getDouble("price-restoration-rate", 0.05);

        if (interval > 0) {
            me.antigravity.economyshop.task.PriceRestorationTask task = new me.antigravity.economyshop.task.PriceRestorationTask(
                    this, rate);
            task.start(interval);
        }
    }

    @Override
    public void onDisable() {
        // 필요 시 데이터 저장
        this.shopManager.saveShops();

        // DB 연결 종료
        if (this.databaseManager != null) {
            this.databaseManager.close();
        }

        // Discord 웹훅 워커 스레드 종료
        if (this.logManager != null) {
            this.logManager.shutdown();
        }

        getLogger().info("EconomyShop 플러그인이 비활성화되었습니다!");
    }
}
