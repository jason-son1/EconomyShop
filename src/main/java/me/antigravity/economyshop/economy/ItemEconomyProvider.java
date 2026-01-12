package me.antigravity.economyshop.economy;

import me.antigravity.economyshop.EconomyShop;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * 특정 아이템을 화폐로 사용하는 Provider 구현체입니다.
 * 예: 에메랄드, 금괴, 네더의 별 등을 화폐로 사용할 수 있습니다.
 */
public class ItemEconomyProvider implements EconomyProvider {

    private final Material currencyMaterial;
    private final String currencyName;

    /**
     * @param plugin           플러그인 인스턴스 (미래 확장용, 현재 미사용)
     * @param currencyMaterial 화폐로 사용할 아이템의 Material
     * @param currencyName     화폐 이름 (예: "에메랄드")
     */
    public ItemEconomyProvider(EconomyShop plugin, Material currencyMaterial, String currencyName) {
        // plugin is unused but kept for API compatibility
        this.currencyMaterial = currencyMaterial;
        this.currencyName = currencyName;
    }

    /**
     * 에메랄드를 기본 화폐로 사용하는 생성자
     */
    public ItemEconomyProvider(EconomyShop plugin) {
        this(plugin, Material.EMERALD, "에메랄드");
    }

    @Override
    public String getName() {
        return "Item:" + currencyMaterial.name();
    }

    @Override
    public boolean isAvailable() {
        return true; // 아이템 기반 경제는 항상 사용 가능
    }

    @Override
    public double getBalance(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == currencyMaterial) {
                count += item.getAmount();
            }
        }
        return count;
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        int toRemove = (int) amount;
        if (getBalance(player) < toRemove)
            return false;

        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && toRemove > 0; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == currencyMaterial) {
                int itemAmount = item.getAmount();
                if (itemAmount <= toRemove) {
                    toRemove -= itemAmount;
                    player.getInventory().setItem(i, null);
                } else {
                    item.setAmount(itemAmount - toRemove);
                    toRemove = 0;
                }
            }
        }

        player.updateInventory();
        return toRemove == 0;
    }

    @Override
    public boolean deposit(Player player, double amount) {
        int toGive = (int) amount;

        // 인벤토리에 아이템 추가
        ItemStack currency = new ItemStack(currencyMaterial, toGive);
        java.util.HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(currency);

        // 인벤토리가 가득 찬 경우 바닥에 드랍
        if (!overflow.isEmpty()) {
            for (ItemStack item : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
        }

        player.updateInventory();
        return true;
    }

    @Override
    public String formatAmount(double amount) {
        return String.format("%,d %s", (int) amount, currencyName);
    }

    @Override
    public String getCurrencyName() {
        return currencyName;
    }

    public Material getCurrencyMaterial() {
        return currencyMaterial;
    }
}
