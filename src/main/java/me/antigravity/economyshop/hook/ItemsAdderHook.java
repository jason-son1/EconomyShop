package me.antigravity.economyshop.hook;

import org.bukkit.inventory.ItemStack;

/**
 * ItemsAdder 플러그인 연동 훅
 */
public class ItemsAdderHook {

    private final boolean enabled;

    public ItemsAdderHook() {
        this.enabled = org.bukkit.Bukkit.getPluginManager().isPluginEnabled("ItemsAdder");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public ItemStack getItem(String id) {
        if (!enabled)
            return null;
        try {
            Class<?> customStackClass = Class.forName("dev.lone.itemsadder.api.CustomStack");
            Object customStack = customStackClass.getMethod("getInstance", String.class).invoke(null, id);

            if (customStack != null) {
                return (ItemStack) customStackClass.getMethod("getItemStack").invoke(customStack);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getItemsAdderId(ItemStack item) {
        if (!enabled)
            return null;
        try {
            Class<?> customStackClass = Class.forName("dev.lone.itemsadder.api.CustomStack");
            Object customStack = customStackClass.getMethod("byItemStack", ItemStack.class).invoke(null, item);

            if (customStack != null) {
                return (String) customStackClass.getMethod("getNamespacedID").invoke(customStack);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
