package me.antigravity.economyshop.manager;

import me.antigravity.economyshop.EconomyShop;
import me.antigravity.economyshop.gui.SellGUIHolder;
import me.antigravity.economyshop.model.ShopItem;
import me.antigravity.economyshop.model.ShopSection;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SellGUIManager implements Listener {

    private final EconomyShop plugin;
    private final String GUI_TITLE = "§8아이템 판매 (넣고 닫으세요)";

    public SellGUIManager(EconomyShop plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void openSellGUI(Player player) {
        SellGUIHolder holder = new SellGUIHolder();
        Inventory inv = Bukkit.createInventory(holder, 36, GUI_TITLE);
        holder.setInventory(inv);

        // 안내 아이콘 (선택 사항)
        // inv.setItem(4, createApiItem(Material.PAPER, "§b§l판매 방법", "§7아이템을 넣고 인벤토리를
        // 닫으면", "§7자동으로 판매됩니다."));

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof SellGUIHolder))
            return;

        Player player = (Player) event.getPlayer();
        Inventory inv = event.getInventory();
        double totalEarnings = 0.0;
        int soldCount = 0;
        List<ItemStack> unsoldItems = new ArrayList<>();

        for (ItemStack item : inv.getContents()) {
            if (item == null || item.getType() == Material.AIR)
                continue;

            // 판매 가능한 아이템인지 확인 (모든 상점 섹션 검색)
            double price = -1.0;
            ShopItem shopItem = findShopItem(item);

            if (shopItem != null) {
                price = shopItem.getCurrentSellPrice();
                // 동적 가격 로직 처리 (재고 증가)
                if (shopItem.isDynamicPricing()) {
                    shopItem.setCurrentStock(
                            Math.min(shopItem.getMaxStock(), shopItem.getCurrentStock() + item.getAmount()));
                    // DB 저장 필요 시 비동기 처리 권장
                    plugin.getDatabaseManager().saveDynamicPrice(shopItem.getId(), shopItem.getCurrentStock());
                }
            }

            if (price > 0) {
                totalEarnings += price * item.getAmount();
                soldCount += item.getAmount();
            } else {
                unsoldItems.add(item);
            }
        }

        // 정산
        if (totalEarnings > 0) {
            me.antigravity.economyshop.economy.EconomyProvider economy = plugin.getEconomyManager()
                    .getDefaultProvider();
            if (economy != null && economy.isAvailable()) {
                economy.deposit(player, totalEarnings);
                String msg = plugin.getLangManager().getMessage("shop.sell-success")
                        .replace("{price}", economy.formatAmount(totalEarnings));
                player.sendMessage(msg);
            } else {
                player.sendMessage("§c경제 시스템 오류: 사용 가능한 경제 시스템을 찾을 수 없습니다.");
            }
        }

        // 판매 불가 아이템 반환
        for (ItemStack unsold : unsoldItems) {
            player.getInventory().addItem(unsold).forEach((idx, drop) -> {
                player.getWorld().dropItem(player.getLocation(), drop);
            });
        }

        if (!unsoldItems.isEmpty()) {
            player.sendMessage("§c판매할 수 없는 아이템 " + unsoldItems.size() + "종류를 돌려받았습니다.");
        }
    }

    // 이 메소드는 성능상 최적화가 필요할 수 있음 (아이템 매칭을 위해 모든 상점 순회)
    private ShopItem findShopItem(ItemStack target) {
        for (ShopSection section : plugin.getShopManager().getSections().values()) {
            for (ShopItem item : section.getItems()) {
                // 재질과 데이터(내구도 등)만 비교하거나, HookManager를 통해 커스텀 아이템 비교
                if (isSimilar(item.getItemStack(), target)) {
                    return item;
                }
            }
        }
        return null;
    }

    private boolean isSimilar(ItemStack shopItem, ItemStack target) {
        // 간단한 비교: 타입과 메타데이터
        return shopItem.isSimilar(target);
    }
}
