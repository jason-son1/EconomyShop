package me.antigravity.economyshop.util;

import me.antigravity.economyshop.EconomyShop;
import me.antigravity.economyshop.model.ShopItem;
import me.antigravity.economyshop.model.ShopSection;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;

/**
 * ItemStack을 YAML 형식으로 직렬화하고 역직렬화하는 유틸리티 클래스입니다.
 * NBT 태그, CustomModelData, Enchantments, Lore 등 모든 메타데이터를 지원합니다.
 */
public class ItemSerializer {

    /**
     * 아이템을 상점 섹션의 YAML 파일에 저장합니다.
     * 
     * @param plugin  플러그인 인스턴스
     * @param section 저장할 섹션
     * @param item    저장할 ShopItem
     * @return 성공 여부
     */
    public static boolean saveItemToSection(EconomyShop plugin, ShopSection section, ShopItem item) {
        File shopFile = new File(plugin.getDataFolder(), "shops/" + section.getId() + ".yml");

        try {
            // 디렉토리 생성
            if (!shopFile.getParentFile().exists()) {
                shopFile.getParentFile().mkdirs();
            }

            // 파일 생성 (없는 경우)
            if (!shopFile.exists()) {
                shopFile.createNewFile();
            }

            FileConfiguration config = YamlConfiguration.loadConfiguration(shopFile);

            // 아이템 데이터 저장
            String path = item.getId();
            // Material 저장 (Custom Item 지원)
            String material = item.getItemStack().getType().name();

            // Oraxen
            if (plugin.getOraxenHook().isEnabled()) {
                String oid = plugin.getOraxenHook().getOraxenId(item.getItemStack());
                if (oid != null) {
                    material = "Oraxen:" + oid;
                }
            }

            // ItemsAdder (Oraxen이 아닐 경우)
            if (material.equals(item.getItemStack().getType().name()) && plugin.getItemsAdderHook().isEnabled()) {
                String iaid = plugin.getItemsAdderHook().getItemsAdderId(item.getItemStack());
                if (iaid != null) {
                    material = "ia:" + iaid;
                }
            }

            config.set(path + ".material", material);
            config.set(path + ".buy", item.getBuyPrice());
            config.set(path + ".sell", item.getSellPrice());
            config.set(path + ".slot", item.getSlot());
            config.set(path + ".dynamic-pricing", item.isDynamicPricing());
            config.set(path + ".max-stock", item.getMaxStock());
            config.set(path + ".min-price", item.getMinPrice());
            config.set(path + ".max-price", item.getMaxPrice());

            // 플레이어 제한
            if (item.getPlayerLimit() > 0) {
                config.set(path + ".player-limit", item.getPlayerLimit());
            }

            // 경제 타입
            if (item.getEconomyType() != null && !item.getEconomyType().isEmpty()) {
                config.set(path + ".economy", item.getEconomyType());
            }

            // ItemMeta 저장 (복잡한 아이템의 경우)
            ItemStack itemStack = item.getItemStack();
            if (itemStack.hasItemMeta()) {
                ItemMeta meta = itemStack.getItemMeta();

                // 커스텀 이름
                if (meta.hasDisplayName()) {
                    config.set(path + ".display-name", meta.getDisplayName());
                }

                // 로어
                if (meta.hasLore()) {
                    config.set(path + ".lore", meta.getLore());
                }

                // CustomModelData (1.14+)
                if (meta.hasCustomModelData()) {
                    config.set(path + ".custom-model-data", meta.getCustomModelData());
                }

                // 인챈트
                if (!itemStack.getEnchantments().isEmpty()) {
                    itemStack.getEnchantments().forEach((enchant, level) -> {
                        config.set(path + ".enchantments." + enchant.getKey().getKey(), level);
                    });
                }

                // 수량
                if (itemStack.getAmount() > 1) {
                    config.set(path + ".amount", itemStack.getAmount());
                }
            }

            config.save(shopFile);
            return true;

        } catch (IOException e) {
            plugin.getLogger().severe("아이템 저장 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 새 상점 섹션을 생성합니다.
     * 
     * @param plugin       플러그인 인스턴스
     * @param sectionId    섹션 ID
     * @param displayName  표시 이름
     * @param iconMaterial 아이콘 Material
     * @param slot         메뉴 슬롯 위치
     * @return 성공 여부
     */
    public static boolean createSection(EconomyShop plugin, String sectionId, String displayName,
            Material iconMaterial, int slot) {
        File sectionsFile = new File(plugin.getDataFolder(), "sections.yml");

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(sectionsFile);

            // 섹션 데이터 추가
            config.set(sectionId + ".display-name", displayName);
            config.set(sectionId + ".material", iconMaterial.name());
            config.set(sectionId + ".slot", slot);
            config.set(sectionId + ".permission", "economyshop.shop." + sectionId);
            config.set(sectionId + ".economy", "Vault");

            config.save(sectionsFile);

            // 빈 상점 파일 생성
            File shopFile = new File(plugin.getDataFolder(), "shops/" + sectionId + ".yml");
            if (!shopFile.exists()) {
                shopFile.getParentFile().mkdirs();
                shopFile.createNewFile();

                // 주석 추가
                FileConfiguration shopConfig = YamlConfiguration.loadConfiguration(shopFile);
                shopConfig.options().header("# " + displayName + " 상점 아이템 설정\n");
                shopConfig.save(shopFile);
            }

            return true;

        } catch (IOException e) {
            plugin.getLogger().severe("섹션 생성 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ConfigurationSection에서 아이템을 역직렬화합니다.
     */
    public static ItemStack deserialize(EconomyShop plugin, org.bukkit.configuration.ConfigurationSection section) {
        String materialName = section.getString("material", "STONE");
        int amount = section.getInt("amount", 1);
        ItemStack itemStack = null;

        if (plugin.getOraxenHook().isEnabled() && materialName.startsWith("Oraxen:")) {
            itemStack = plugin.getOraxenHook().getItem(materialName.substring(7));
        } else if (plugin.getItemsAdderHook().isEnabled() && materialName.startsWith("ia:")) {
            itemStack = plugin.getItemsAdderHook().getItem(materialName.substring(3));
        }

        if (itemStack == null) {
            try {
                itemStack = new ItemStack(Material.valueOf(materialName), amount);
            } catch (IllegalArgumentException e) {
                itemStack = new ItemStack(Material.STONE);
            }
        } else {
            itemStack.setAmount(amount);
        }

        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            if (section.contains("display-name")) {
                meta.setDisplayName(
                        org.bukkit.ChatColor.translateAlternateColorCodes('&', section.getString("display-name")));
            }
            if (section.contains("lore")) {
                java.util.List<String> lore = section.getStringList("lore").stream()
                        .map(s -> org.bukkit.ChatColor.translateAlternateColorCodes('&', s))
                        .collect(java.util.stream.Collectors.toList());
                meta.setLore(lore);
            }
            if (section.contains("custom-model-data")) {
                meta.setCustomModelData(section.getInt("custom-model-data"));
            }

            if (section.isConfigurationSection("enchantments")) {
                org.bukkit.configuration.ConfigurationSection enchSec = section.getConfigurationSection("enchantments");
                for (String key : enchSec.getKeys(false)) {
                    org.bukkit.enchantments.Enchantment ench = org.bukkit.enchantments.Enchantment
                            .getByKey(org.bukkit.NamespacedKey.minecraft(key.toLowerCase()));
                    if (ench != null) {
                        meta.addEnchant(ench, enchSec.getInt(key), true);
                    }
                }
            }
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    /**
     * ItemStack을 Base64 문자열로 인코딩합니다.
     * 복잡한 NBT 데이터를 포함한 아이템 저장에 사용됩니다.
     */
    public static String serializeToBase64(ItemStack item) {
        try {
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            org.bukkit.util.io.BukkitObjectOutputStream dataOutput = new org.bukkit.util.io.BukkitObjectOutputStream(
                    outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return java.util.Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Base64 문자열을 ItemStack으로 디코딩합니다.
     */
    public static ItemStack deserializeFromBase64(String base64) {
        try {
            java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(
                    java.util.Base64.getDecoder().decode(base64));
            org.bukkit.util.io.BukkitObjectInputStream dataInput = new org.bukkit.util.io.BukkitObjectInputStream(
                    inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
