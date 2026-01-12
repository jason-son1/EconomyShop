package me.antigravity.economyshop.economy;

import me.antigravity.economyshop.EconomyShop;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Vault 경제 시스템을 위한 Provider 구현체입니다.
 */
public class VaultProvider implements EconomyProvider {

    private final EconomyShop plugin;
    private Economy economy;

    public VaultProvider(EconomyShop plugin) {
        this.plugin = plugin;
        setupVault();
    }

    private void setupVault() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
        }
    }

    @Override
    public String getName() {
        return "Vault";
    }

    @Override
    public boolean isAvailable() {
        return economy != null;
    }

    @Override
    public double getBalance(Player player) {
        if (!isAvailable())
            return 0;
        return economy.getBalance(player);
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        if (!isAvailable())
            return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    @Override
    public boolean deposit(Player player, double amount) {
        if (!isAvailable())
            return false;
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    @Override
    public String formatAmount(double amount) {
        if (!isAvailable())
            return String.format("%.2f", amount);
        return economy.format(amount);
    }

    @Override
    public String getCurrencyName() {
        if (!isAvailable())
            return "원";
        return economy.currencyNamePlural();
    }
}
