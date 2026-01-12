package me.antigravity.economyshop.command;

import me.antigravity.economyshop.EconomyShop;
import me.antigravity.economyshop.model.ShopSection;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * /shop 명령어를 처리하는 클래스입니다.
 * 
 * 사용법:
 * - /shop - 메인 상점 메뉴 열기
 * - /shop <section> - 특정 상점 섹션 바로 열기
 */
public class ShopCommand implements CommandExecutor, TabCompleter {

    private final EconomyShop plugin;

    public ShopCommand(EconomyShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("이 명령어는 플레이어만 사용할 수 있습니다.");
            return true;
        }

        if (args.length == 0) {
            // 메인 메뉴 열기
            plugin.getGuiManager().openMainMenu(player);
            return true;
        }

        // 로그 조회 커맨드: /shop log [player] [page]
        if (args[0].equalsIgnoreCase("log")) {
            if (!player.hasPermission("economyshop.admin.log")) {
                player.sendMessage(plugin.getLangManager().getMessage("error.no-permission"));
                return true;
            }

            String targetName = null;
            int page = 1;

            if (args.length > 1) {
                // 두 번째 인자가 숫자인지 확인 -> 페이지
                // 숫자가 아니면 -> 플레이어 이름
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    targetName = args[1];
                    // 세 번째 인자가 있으면 페이지
                    if (args.length > 2) {
                        try {
                            page = Integer.parseInt(args[2]);
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }

            player.sendMessage("§e로그를 조회중입니다...");
            final int finalPage = page; // lambda
            plugin.getLogManager().getTransactionLogs(targetName, page).thenAccept(logs -> {
                if (logs.isEmpty()) {
                    player.sendMessage("§c해당 조건의 로그가 없습니다.");
                    return;
                }

                player.sendMessage("§6=== 거래 내역 (Page " + finalPage + ") ===");
                for (String log : logs) {
                    player.sendMessage("§7" + log);
                }
                player.sendMessage("§6==============================");
            });
            return true;
        }

        String sectionId = args[0];
        ShopSection section = plugin.getShopManager().getSections().get(sectionId);

        if (section == null) {
            player.sendMessage("§c존재하지 않는 상점 섹션입니다: " + sectionId);
            player.sendMessage("§7사용 가능한 섹션: " + String.join(", ", plugin.getShopManager().getSections().keySet()));
            return true;
        }

        // 권한 확인
        if (section.getPermission() != null && !section.getPermission().isEmpty()
                && !player.hasPermission(section.getPermission())
                && !player.hasPermission("economyshop.shop.all")) {
            player.sendMessage(plugin.getLangManager().getMessage("error.no-permission"));
            return true;
        }

        plugin.getGuiManager().openShop(player, section);
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            final String input = args[0].toLowerCase();
            List<String> completions = plugin.getShopManager().getSections().keySet().stream()
                    .filter(s -> s.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
            if ("log".startsWith(input) && sender.hasPermission("economyshop.admin.log")) {
                completions.add("log");
            }
            return completions;
        }
        return new ArrayList<>();
    }
}
