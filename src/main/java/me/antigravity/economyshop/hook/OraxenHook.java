package me.antigravity.economyshop.hook;

import org.bukkit.inventory.ItemStack;

/**
 * Oraxen 플러그인 연동 훅
 */
public class OraxenHook {

    private final boolean enabled;

    public OraxenHook() {
        this.enabled = org.bukkit.Bukkit.getPluginManager().isPluginEnabled("Oraxen");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public ItemStack getItem(String id) {
        if (!enabled)
            return null;
        try {
            Class<?> oraxenItemsClass = Class.forName("io.th0rgal.oraxen.api.OraxenItems");
            Object itemBuilder = oraxenItemsClass.getMethod("getItem", String.class).invoke(null, id);
            if (itemBuilder == null)
                return null;
            return (ItemStack) itemBuilder.getClass().getMethod("build").invoke(itemBuilder);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getOraxenId(ItemStack item) {
        if (!enabled)
            return null;
        try {
            Class<?> oraxenItemsClass = Class.forName("io.th0rgal.oraxen.api.OraxenItems");
            return (String) oraxenItemsClass.getMethod("getIdByItem", ItemStack.class).invoke(null, item);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
