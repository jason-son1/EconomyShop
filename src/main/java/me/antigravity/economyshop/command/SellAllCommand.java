package me.antigravity.economyshop.command;

import me.antigravity.economyshop.EconomyShop;
import me.antigravity.economyshop.economy.EconomyProvider;
import me.antigravity.economyshop.model.ShopItem;
import me.antigravity.economyshop.model.ShopSection;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * /sellall 명령어를 처리하는 클래스입니다.
 * 인벤토리의 모든 판매 가능한 아이템을 한번에 판매합니다.
 */
public class SellAllCommand implements CommandExecutor {

    private final EconomyShop plugin;

    public SellAllCommand(EconomyShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("이 명령어는 플레이어만 사용할 수 있습니다.");
            return true;
        }

        if (!player.hasPermission("economyshop.sellall")) {
            player.sendMessage(plugin.getLangManager().getMessage("error.no-permission"));
            return true;
        }

        // 기본 경제 시스템 가져오기
        EconomyProvider economy = plugin.getEconomyManager().getDefaultProvider();
        if (economy == null || !economy.isAvailable()) {
            player.sendMessage("§c경제 시스템 오류: 사용 가능한 경제 시스템을 찾을 수 없습니다.");
            return true;
        }

        double totalEarnings = 0.0;
        int soldCount = 0;
        int itemTypesSold = 0;

        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() == Material.AIR)
                continue;

            // 상점에서 판매 가능한 아이템인지 확인
            SellableItem sellable = findSellableItem(item);
            if (sellable == null)
                continue;

            // 가격 계산
            double pricePerUnit = sellable.item.getCurrentSellPrice();
            double totalPrice = pricePerUnit * item.getAmount();

            totalEarnings += totalPrice;
            soldCount += item.getAmount();
            itemTypesSold++;

            // 동적 가격 재고 업데이트
            if (sellable.item.isDynamicPricing()) {
                long newStock = Math.min(sellable.item.getMaxStock(),
                        sellable.item.getCurrentStock() + item.getAmount());
                sellable.item.setCurrentStock(newStock);
                plugin.getDatabaseManager().saveDynamicPrice(sellable.item.getId(), newStock);
            }

            // 아이템 제거
            player.getInventory().setItem(i, null);
        }

        if (totalEarnings > 0) {
            economy.deposit(player, totalEarnings);

            player.sendMessage("§a=== 판매 완료 ===");
            player.sendMessage("§7판매한 아이템 종류: §f" + itemTypesSold + "가지");
            player.sendMessage("§7판매한 총 수량: §f" + soldCount + "개");
            player.sendMessage("§7총 획득 금액: §e" + economy.formatAmount(totalEarnings));

            // 로깅
            plugin.getLogManager().logTransaction(player.getName(), "SELLALL", "BULK", soldCount, totalEarnings);
        } else {
            player.sendMessage("§c판매 가능한 아이템이 없습니다.");
        }

        return true;
    }

    /**
     * 아이템을 상점에서 찾아 판매 가능 여부를 확인합니다.
     */
    private SellableItem findSellableItem(ItemStack target) {
        for (ShopSection section : plugin.getShopManager().getSections().values()) {
            for (ShopItem item : section.getItems()) {
                if (item.getSellPrice() <= 0)
                    continue; // 판매 불가 아이템

                // 아이템 비교 (Material 및 유사도 체크)
                if (item.getItemStack().isSimilar(target)) {
                    return new SellableItem(section, item);
                }
            }
        }
        return null;
    }

    /**
     * 판매 가능한 아이템 정보를 담는 레코드
     */
    private record SellableItem(ShopSection section, ShopItem item) {
    }
}
