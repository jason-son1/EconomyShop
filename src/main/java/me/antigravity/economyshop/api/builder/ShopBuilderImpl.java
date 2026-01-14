package me.antigravity.economyshop.api.builder;

import me.antigravity.economyshop.EconomyShop;
import me.antigravity.economyshop.model.ShopSection;
import me.antigravity.economyshop.util.ItemSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

/**
 * ShopBuilder의 내부 구현체입니다.
 * Fluent API 패턴으로 상점 섹션을 생성합니다.
 */
public class ShopBuilderImpl implements ShopBuilder {

    private final EconomyShop plugin;

    private String id;
    private String displayName;
    private ItemStack icon;
    private int slot = 0;
    private String permission;
    private String economy = "Vault";
    private String ownerPlugin;

    public ShopBuilderImpl(EconomyShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public ShopBuilder id(String id) {
        this.id = id;
        return this;
    }

    @Override
    public ShopBuilder displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    @Override
    public ShopBuilder icon(ItemStack icon) {
        this.icon = icon;
        return this;
    }

    @Override
    public ShopBuilder icon(Material material) {
        this.icon = new ItemStack(material);
        return this;
    }

    @Override
    public ShopBuilder slot(int slot) {
        this.slot = slot;
        return this;
    }

    @Override
    public ShopBuilder permission(String permission) {
        this.permission = permission;
        return this;
    }

    @Override
    public ShopBuilder economy(String economyId) {
        this.economy = economyId;
        return this;
    }

    @Override
    public ShopBuilder ownerPlugin(String pluginName) {
        this.ownerPlugin = pluginName;
        return this;
    }

    @Override
    public boolean build() {
        validateRequired();

        ShopSection section = createSection();

        // 메모리에만 등록
        plugin.getShopManager().getSections().put(id, section);
        plugin.getLogger().info("상점 [" + id + "] 생성 완료 (메모리)");
        return true;
    }

    @Override
    public boolean buildAndSave() {
        validateRequired();

        // 중복 체크
        if (plugin.getShopManager().getSections().containsKey(id)) {
            plugin.getLogger().warning("상점 [" + id + "]가 이미 존재합니다.");
            return false;
        }

        // 파일 생성
        Material iconMaterial = icon != null ? icon.getType() : Material.CHEST;
        boolean created = ItemSerializer.createSection(
                plugin,
                id,
                displayName != null ? displayName : id,
                iconMaterial,
                slot);

        if (!created) {
            plugin.getLogger().severe("상점 [" + id + "] 파일 생성 실패");
            return false;
        }

        // 메모리에 섹션 추가
        ShopSection section = createSection();
        plugin.getShopManager().getSections().put(id, section);

        // 추가 설정 저장 (permission, economy, ownerPlugin)
        if (permission != null) {
            plugin.getConfigManager().getSectionsConfig().set(id + ".permission", permission);
        }
        if (economy != null && !economy.equals("Vault")) {
            plugin.getConfigManager().getSectionsConfig().set(id + ".economy", economy);
        }
        if (ownerPlugin != null) {
            plugin.getConfigManager().getSectionsConfig().set(id + ".owner-plugin", ownerPlugin);
        }
        plugin.getConfigManager().saveSectionsConfig();

        plugin.getLogger().info("상점 [" + id + "] 생성 및 저장 완료");
        return true;
    }

    /**
     * 필수 필드 검증
     */
    private void validateRequired() {
        if (id == null || id.isEmpty()) {
            throw new IllegalStateException("상점 ID는 필수입니다.");
        }
        // ID 유효성 검사 (영문, 숫자, 언더스코어만)
        if (!id.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalStateException("상점 ID는 영문, 숫자, 언더스코어만 사용할 수 있습니다: " + id);
        }
    }

    /**
     * ShopSection 객체를 생성합니다.
     */
    private ShopSection createSection() {
        return ShopSection.builder()
                .id(id)
                .displayName(displayName != null ? displayName : id)
                .icon(icon != null ? icon : new ItemStack(Material.CHEST))
                .slot(slot)
                .permission(permission)
                .economy(economy)
                .items(new ArrayList<>())
                .build();
    }
}
