package me.antigravity.economyshop.manager;

import me.antigravity.economyshop.EconomyShop;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final EconomyShop plugin;
    private FileConfiguration config;
    private FileConfiguration sectionsConfig;
    private File sectionsFile;

    public ConfigManager(EconomyShop plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        // 기본 config.yml 로드
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();

        // sections.yml 로드
        loadSectionsConfig();

        // 상점 데이터 폴더 생성
        createShopFolder();
    }

    private void loadSectionsConfig() {
        this.sectionsFile = new File(plugin.getDataFolder(), "sections.yml");
        if (!sectionsFile.exists()) {
            plugin.saveResource("sections.yml", false);
        }
        this.sectionsConfig = YamlConfiguration.loadConfiguration(sectionsFile);
    }

    private void createShopFolder() {
        File shopFolder = new File(plugin.getDataFolder(), "shops");
        if (!shopFolder.exists()) {
            shopFolder.mkdirs();
            // 예시 상점 파일 생성 (farming.yml 등)
            plugin.saveResource("shops/farming.yml", false);
        }
    }

    public void reloadAll() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        this.sectionsConfig = YamlConfiguration.loadConfiguration(sectionsFile);
    }

    /**
     * sections.yml 파일을 저장합니다.
     */
    public void saveSectionsConfig() {
        if (sectionsConfig != null && sectionsFile != null) {
            try {
                sectionsConfig.save(sectionsFile);
            } catch (IOException e) {
                plugin.getLogger().severe("sections.yml 저장 실패: " + e.getMessage());
            }
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getSectionsConfig() {
        return sectionsConfig;
    }

    public FileConfiguration getMainConfig() {
        return config;
    }

    /**
     * 전체 동적 경제 시스템 활성화 여부를 반환합니다.
     * config.yml의 'dynamic-pricing-enabled' 값을 확인합니다.
     * 
     * @return 활성화 여부 (기본값: true)
     */
    public boolean isGlobalDynamicPricingEnabled() {
        return config.getBoolean("dynamic-pricing-enabled", true);
    }
}
