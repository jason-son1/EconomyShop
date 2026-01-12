package me.antigravity.economyshop.manager;

import me.antigravity.economyshop.EconomyShop;
import me.antigravity.economyshop.gui.MainMenuGUIHolder;
import me.antigravity.economyshop.gui.ShopEditorGUIHolder;
import me.antigravity.economyshop.gui.ShopGUIHolder;
import me.antigravity.economyshop.model.ShopItem;
import me.antigravity.economyshop.model.ShopSection;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GUIManager {

    private final EconomyShop plugin;
    private static final int ITEMS_PER_PAGE = 45; // 하단 9칸은 네비게이션 바용

    public GUIManager(EconomyShop plugin) {
        this.plugin = plugin;
    }

    /**
     * 메인 상점 메뉴를 엽니다. (모든 섹션 표시)
     * 
     * @param player 대상 플레이어
     */
    public void openMainMenu(Player player) {
        MainMenuGUIHolder holder = new MainMenuGUIHolder();
        Inventory inventory = Bukkit.createInventory(holder, 54, "§8상점 선택");
        holder.setInventory(inventory);

        renderMainMenu(holder, player);
        player.openInventory(inventory);
    }

    /**
     * 메인 메뉴를 렌더링합니다.
     */
    public void renderMainMenu(MainMenuGUIHolder holder, Player player) {
        Inventory inv = holder.getInventory();
        inv.clear();

        Map<String, ShopSection> sections = plugin.getShopManager().getSections();

        for (ShopSection section : sections.values()) {
            // 권한 확인
            if (section.getPermission() != null && !section.getPermission().isEmpty()
                    && !player.hasPermission(section.getPermission())
                    && !player.hasPermission("economyshop.shop.all")) {
                continue; // 권한이 없는 섹션은 표시하지 않음
            }

            ItemStack icon = section.getIcon().clone();
            ItemMeta meta = icon.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(section.getDisplayName());
                List<String> lore = new ArrayList<>();
                lore.add("");
                lore.add("§7클릭하여 상점 열기");
                lore.add("§7아이템 수: §f" + section.getItems().size() + "개");
                lore.add("§7화폐: §e" + section.getEconomy());
                meta.setLore(lore);
                icon.setItemMeta(meta);
            }

            int slot = section.getSlot();
            if (slot >= 0 && slot < 54) {
                inv.setItem(slot, icon);
            }
        }
    }

    /**
     * 플레이어에게 특정 상점 섹션을 엽니다.
     * 
     * @param player  대상 플레이어
     * @param section 열고자 하는 상점 섹션
     */
    public void openShop(Player player, ShopSection section) {
        ShopGUIHolder holder = new ShopGUIHolder(section);
        Inventory inventory = Bukkit.createInventory(holder, 54, section.getDisplayName());
        holder.setInventory(inventory);

        renderPage(holder);
        player.openInventory(inventory);
    }

    /**
     * 상점 페이지를 렌더링합니다.
     * 
     * @param holder GUI 홀더
     */
    public void renderPage(ShopGUIHolder holder) {
        Inventory inv = holder.getInventory();
        inv.clear();

        ShopSection section = holder.getSection();
        int page = holder.getPage();
        List<ShopItem> allItems = section.getItems();

        // 아이템 배치 (Slot 기반)
        for (ShopItem item : allItems) {
            int itemPage = item.getSlot() / 45;
            if (itemPage == page) {
                int slot = item.getSlot() % 45;
                inv.setItem(slot, createDisplayItem(section, item));
            }
        }

        // 하단 네비게이션 바 렌더링 (슬롯 45~53)
        renderNavigationBar(holder);
    }

    // ... (skip lines 121-150) ...
    private ItemStack createDisplayItem(ShopSection section, ShopItem shopItem) {
        if (shopItem.getCachedDisplayItem() != null) {
            return shopItem.getCachedDisplayItem().clone();
        }

        ItemStack item = shopItem.getItemStack().clone();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add("");
            me.antigravity.economyshop.economy.EconomyProvider economy = plugin.getEconomyManager()
                    .getProvider(section, shopItem);

            lore.add(plugin.getLangManager().getRawMessage("gui.item.buy-action").replace("{price}",
                    economy.formatAmount(shopItem.getCurrentBuyPrice())));
            lore.add(plugin.getLangManager().getRawMessage("gui.item.sell-action").replace("{price}",
                    economy.formatAmount(shopItem.getCurrentSellPrice())));

            if (shopItem.isDynamicPricing()) {
                String stockMsg = plugin.getLangManager().getRawMessage("gui.item.stock")
                        .replace("{current}", String.valueOf(shopItem.getCurrentStock()))
                        .replace("{max}", String.valueOf(shopItem.getMaxStock()));
                lore.add(stockMsg);
                lore.add(plugin.getLangManager().getRawMessage("gui.item.dynamic"));
            }

            if (shopItem.getPlayerLimit() > 0) {
                lore.add(plugin.getLangManager().getRawMessage("gui.item.limit"));
            }

            // 요구사항 표시
            List<String> requirements = me.antigravity.economyshop.util.RequirementChecker
                    .getRequirementsList(shopItem);
            if (!requirements.isEmpty()) {
                lore.add("");
                lore.addAll(requirements);
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        shopItem.setCachedDisplayItem(item.clone());
        return item;
    }

    /**
     * 특정 아이템의 속성을 수정하는 에디터 GUI를 엽니다.
     */
    public void openItemEditor(Player player, ShopSection section, ShopItem item) {
        ShopEditorGUIHolder holder = new ShopEditorGUIHolder(section, item);
        Inventory inventory = Bukkit.createInventory(holder, 27, "§8아이템 편집: " + item.getId());
        holder.setInventory(inventory);

        renderEditor(holder);
        player.openInventory(inventory);
    }

    private void renderEditor(ShopEditorGUIHolder holder) {
        Inventory inv = holder.getInventory();
        inv.clear();
        ShopItem item = holder.getTargetItem();

        // 현재 아이템 정보 표시 (슬롯 13)
        inv.setItem(13, item.getItemStack());

        // 구매 가격 수정 (슬롯 10)
        inv.setItem(10, createItem(Material.GOLD_INGOT, "§e구매 가격 수정",
                "§f현재: §a" + item.getBuyPrice(), "", "§7좌클릭: +10 / 우클릭: -10", "§7Shift+좌클릭: +100 / Shift+우클릭: -100",
                "§e휠 클릭: 직접 입력"));

        // 판매 가격 수정 (슬롯 11)
        inv.setItem(11, createItem(Material.IRON_INGOT, "§e판매 가격 수정",
                "§f현재: §c" + item.getSellPrice(), "", "§7좌클릭: +10 / 우클릭: -10", "§7Shift+좌클릭: +100 / Shift+우클릭: -100",
                "§e휠 클릭: 직접 입력"));

        // 동적 가격 설정 토글 (슬롯 15)
        inv.setItem(15, createItem(item.isDynamicPricing() ? Material.LIME_DYE : Material.GRAY_DYE, "§e동적 가격 설정",
                "§f현재: " + (item.isDynamicPricing() ? "§a활성화" : "§7비활성화"), "", "§7클릭하여 토글"));

        // 저장 버튼 (슬롯 22)
        inv.setItem(22, createItem(Material.NETHER_STAR, "§b§l설정 저장", "§7클릭하여 YAML 파일에 즉시 저장합니다."));

        // 아이템 삭제 버튼 (슬롯 26)
        inv.setItem(26,
                createItem(Material.RED_CONCRETE, "§c§l아이템 삭제", "§7클릭 시 이 아이템을 상점에서 영구 삭제합니다.", "§c§l주의: 되돌릴 수 없습니다!"));
    }

    private ItemStack createItem(Material material, String name, String... lores) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> list = new ArrayList<>();
            for (String lore : lores) {
                list.add(lore);
            }
            meta.setLore(list);
            item.setItemMeta(meta);
        }
        return item;
    }
}
