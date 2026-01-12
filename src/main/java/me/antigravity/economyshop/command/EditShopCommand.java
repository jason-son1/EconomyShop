package me.antigravity.economyshop.command;

import me.antigravity.economyshop.EconomyShop;
import me.antigravity.economyshop.model.ShopItem;
import me.antigravity.economyshop.model.ShopSection;
import me.antigravity.economyshop.util.ItemSerializer;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * /editshop 명령어를 처리하는 클래스입니다.
 * 
 * 사용법:
 * - /editshop - 에디터 모드 토글
 * - /editshop additem <section> <buyPrice> <sellPrice> - 손에 든 아이템을 상점에 추가
 * - /editshop addsection <id> <displayName> - 새 상점 섹션 생성
 * - /editshop reload - 설정 리로드
 */
public class EditShopCommand implements CommandExecutor, TabCompleter {

    private final EconomyShop plugin;

    public EditShopCommand(EconomyShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("이 명령어는 플레이어만 사용할 수 있습니다.");
            return true;
        }

        if (!player.hasPermission("economyshop.admin")) {
            player.sendMessage("§c권한이 없습니다.");
            return true;
        }

        if (args.length == 0) {
            // 에디터 모드 토글
            plugin.getEditorManager().toggleEditor(player.getUniqueId());
            boolean isEditor = plugin.getEditorManager().isEditor(player.getUniqueId());
            String key = isEditor ? "editor.mode-on" : "editor.mode-off";
            player.sendMessage(plugin.getLangManager().getMessage(key));
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "additem" -> handleAddItem(player, args);
            case "addsection" -> handleAddSection(player, args);
            case "reload" -> handleReload(player);
            case "toggle" -> {
                plugin.getEditorManager().toggleEditor(player.getUniqueId());
                boolean isEditor = plugin.getEditorManager().isEditor(player.getUniqueId());
                player.sendMessage(plugin.getLangManager().getMessage(isEditor ? "editor.mode-on" : "editor.mode-off"));
            }
            default -> sendUsage(player);
        }

        return true;
    }

    /**
     * 손에 든 아이템을 상점에 추가합니다.
     * /editshop additem <section> <buyPrice> <sellPrice>
     */
    private void handleAddItem(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage("§c사용법: /editshop additem <섹션ID> <구매가> <판매가>");
            return;
        }

        String sectionId = args[1];
        double buyPrice, sellPrice;

        try {
            buyPrice = Double.parseDouble(args[2]);
            sellPrice = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            player.sendMessage("§c가격은 숫자로 입력해야 합니다.");
            return;
        }

        // 손에 든 아이템 확인
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (handItem == null || handItem.getType() == Material.AIR) {
            player.sendMessage("§c손에 아이템을 들고 있어야 합니다.");
            return;
        }

        // 섹션 확인
        ShopSection section = plugin.getShopManager().getSections().get(sectionId);
        if (section == null) {
            player.sendMessage("§c존재하지 않는 섹션입니다: " + sectionId);
            player.sendMessage("§7사용 가능한 섹션: " + String.join(", ", plugin.getShopManager().getSections().keySet()));
            return;
        }

        // 다음 사용 가능한 슬롯 찾기
        int nextSlot = findNextAvailableSlot(section);

        // 아이템 ID 생성 (Material_타임스탬프)
        String itemId = handItem.getType().name() + "_" + System.currentTimeMillis() % 100000;

        // ShopItem 생성
        ShopItem newItem = ShopItem.builder()
                .id(itemId)
                .itemStack(handItem.clone())
                .buyPrice(buyPrice)
                .sellPrice(sellPrice)
                .slot(nextSlot)
                .dynamicPricing(false)
                .maxStock(1000L)
                .currentStock(1000L)
                .minPrice(sellPrice * 0.1)
                .maxPrice(buyPrice * 10)
                .playerLimit(0)
                .build();

        // 메모리에 추가
        section.getItems().add(newItem);

        // YAML 파일에 저장
        boolean saved = ItemSerializer.saveItemToSection(plugin, section, newItem);

        if (saved) {
            player.sendMessage("§a아이템이 성공적으로 추가되었습니다!");
            player.sendMessage("§7섹션: §f" + sectionId);
            player.sendMessage("§7아이템 ID: §f" + itemId);
            player.sendMessage("§7구매가: §e" + buyPrice + " §7/ 판매가: §e" + sellPrice);
            player.sendMessage("§7슬롯: §f" + nextSlot);
        } else {
            player.sendMessage("§c아이템 저장 중 오류가 발생했습니다. 콘솔을 확인하세요.");
        }
    }

    /**
     * 새 상점 섹션을 생성합니다.
     * /editshop addsection <id> <displayName>
     */
    private void handleAddSection(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("§c사용법: /editshop addsection <ID> <표시이름>");
            return;
        }

        String sectionId = args[1];
        StringBuilder displayName = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            displayName.append(args[i]).append(" ");
        }

        if (plugin.getShopManager().getSections().containsKey(sectionId)) {
            player.sendMessage("§c이미 존재하는 섹션 ID입니다: " + sectionId);
            return;
        }

        // 손에 든 아이템을 아이콘으로 사용 (없으면 CHEST)
        ItemStack handItem = player.getInventory().getItemInMainHand();
        Material iconMaterial = (handItem != null && handItem.getType() != Material.AIR)
                ? handItem.getType()
                : Material.CHEST;

        // 다음 사용 가능한 슬롯 찾기
        int nextSlot = plugin.getShopManager().getSections().size() * 2 + 10;

        // 섹션 생성 및 저장
        boolean created = ItemSerializer.createSection(plugin, sectionId, displayName.toString().trim(),
                iconMaterial, nextSlot);

        if (created) {
            // 상점 다시 로드
            plugin.getShopManager().loadShops();
            player.sendMessage("§a새 섹션이 생성되었습니다!");
            player.sendMessage("§7ID: §f" + sectionId);
            player.sendMessage("§7이름: §f" + displayName.toString().trim());
            player.sendMessage("§7아이콘: §f" + iconMaterial.name());
        } else {
            player.sendMessage("§c섹션 생성 중 오류가 발생했습니다.");
        }
    }

    /**
     * 설정을 리로드합니다.
     */
    private void handleReload(Player player) {
        plugin.getConfigManager().loadConfigs();
        plugin.getShopManager().loadShops();
        player.sendMessage("§a[EconomyShop] 설정이 리로드되었습니다.");
    }

    private void sendUsage(Player player) {
        player.sendMessage("§6=== EconomyShop 에디터 명령어 ===");
        player.sendMessage("§e/editshop §7- 에디터 모드 토글");
        player.sendMessage("§e/editshop additem <섹션> <구매가> <판매가> §7- 손에 든 아이템 추가");
        player.sendMessage("§e/editshop addsection <ID> <이름> §7- 새 섹션 생성");
        player.sendMessage("§e/editshop reload §7- 설정 리로드");
    }

    private int findNextAvailableSlot(ShopSection section) {
        int maxSlot = -1;
        for (ShopItem item : section.getItems()) {
            if (item.getSlot() > maxSlot) {
                maxSlot = item.getSlot();
            }
        }
        return maxSlot + 1;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("toggle");
            completions.add("additem");
            completions.add("addsection");
            completions.add("reload");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("additem")) {
            // 섹션 ID 자동완성
            completions.addAll(plugin.getShopManager().getSections().keySet());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("additem")) {
            completions.add("<구매가>");
        } else if (args.length == 4 && args[0].equalsIgnoreCase("additem")) {
            completions.add("<판매가>");
        }

        final String input = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(input))
                .collect(Collectors.toList());
    }
}
