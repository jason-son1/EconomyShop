package me.antigravity.economyshop.api.item;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * 바닐라 아이템을 처리하는 기본 어댑터입니다.
 * Bukkit의 isSimilar()를 사용하여 아이템을 비교합니다.
 */
public class VanillaItemAdapter implements ItemAdapter {

    @Override
    public String getName() {
        return "Vanilla";
    }

    @Override
    public boolean isAvailable() {
        return true; // 항상 사용 가능
    }

    @Override
    public String serialize(ItemStack item) {
        if (item == null) {
            return null;
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public ItemStack deserialize(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean matches(ItemStack shopItem, ItemStack playerItem) {
        if (shopItem == null || playerItem == null) {
            return false;
        }

        // 바닐라 비교: isSimilar() 사용 (수량 제외하고 비교)
        return shopItem.isSimilar(playerItem);
    }

    @Override
    public boolean canHandle(ItemStack item) {
        // 바닐라 어댑터는 모든 아이템을 처리할 수 있음 (기본 폴백)
        // 다만, 다른 어댑터가 먼저 처리하도록 우선순위가 낮음
        return true;
    }
}
