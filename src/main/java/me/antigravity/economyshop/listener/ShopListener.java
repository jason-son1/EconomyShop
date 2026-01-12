package me.antigravity.economyshop.listener;

import me.antigravity.economyshop.EconomyShop;
import me.antigravity.economyshop.economy.EconomyProvider;
import me.antigravity.economyshop.gui.MainMenuGUIHolder;
import me.antigravity.economyshop.gui.ShopEditorGUIHolder;
import me.antigravity.economyshop.gui.ShopGUIHolder;
import me.antigravity.economyshop.model.ShopItem;
import me.antigravity.economyshop.model.ShopSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ShopListener implements Listener {

    private final EconomyShop plugin;

    public ShopListener(EconomyShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof MainMenuGUIHolder holder) {
            handleMainMenuClick(event, holder);
        } else if (event.getInventory().getHolder() instanceof ShopGUIHolder holder) {
            handleShopInventoryClick(event, holder);
        } else if (event.getInventory().getHolder() instanceof ShopEditorGUIHolder holder) {
            handleEditorInventoryClick(event, holder);
        }
    }

    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        // 플레이어 퇴장 시 메모리에서 제한 데이터 언로드
        plugin.getLimitManager().unloadLimits(event.getPlayer().getUniqueId());
        plugin.getEditorManager().toggleEditor(event.getPlayer().getUniqueId()); // 에디터 모드 해제
        if (plugin.getEditorManager().isEditor(event.getPlayer().getUniqueId())) {
            plugin.getEditorManager().toggleEditor(event.getPlayer().getUniqueId()); // 확실히 제거
        }
    }

    /**
     * 메인 메뉴 클릭을 처리합니다.
     */
    private void handleMainMenuClick(InventoryClickEvent event, MainMenuGUIHolder holder) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        // 슬롯으로 섹션 찾기
        for (ShopSection section : plugin.getShopManager().getSections().values()) {
            if (section.getSlot() == slot) {
                // 권한 확인
                if (section.getPermission() != null && !section.getPermission().isEmpty()
                        && !player.hasPermission(section.getPermission())
                        && !player.hasPermission("economyshop.shop.all")) {
                    player.sendMessage(plugin.getLangManager().getMessage("error.no-permission"));
                    return;
                }

                plugin.getGuiManager().openShop(player, section);
                return;
            }
        }
    }

    private void handleShopInventoryClick(InventoryClickEvent event, ShopGUIHolder holder) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        // 1. 하단 네비게이션 바 (45~53)
        if (slot >= 45 && slot < 54) {
            handleNavigationClick(player, holder, slot);
            return;
        }

        // 2. 상점 아이템 영역 (0~44)
        if (slot >= 0 && slot < 45) {
            if (plugin.getEditorManager().isEditor(player.getUniqueId())) {
                // 에디터 모드
                if (event.isLeftClick()) {
                    handleEditorMove(player, holder, slot);
                } else if (event.isRightClick()) {
                    ShopItem item = findItemBySlot(holder.getSection(), (holder.getPage() * 45) + slot);
                    if (item != null) {
                        plugin.getGuiManager().openItemEditor(player, holder.getSection(), item);
                    }
                }
            } else {
                // 일반 모드
                ShopItem item = findItemBySlot(holder.getSection(), (holder.getPage() * 45) + slot);
                if (item != null) {
                    if (event.isLeftClick()) {
                        performBuy(player, holder.getSection(), item);
                    } else if (event.isRightClick()) {
                        performSell(player, holder.getSection(), item);
                    }
                }
            }
        }
    }

    private void handleEditorMove(Player player, ShopGUIHolder holder, int slot) {
        int absoluteSlot = (holder.getPage() * 45) + slot;
        ShopItem clickedItem = findItemBySlot(holder.getSection(), absoluteSlot);

        if (plugin.getEditorManager().hasSelectedItem(player.getUniqueId())) {
            ShopItem selected = plugin.getEditorManager().getSelectedItem(player.getUniqueId());
            if (clickedItem == selected) {
                // 선택 취소
                plugin.getEditorManager().deselectItem(player.getUniqueId());
                player.sendMessage("§e선택이 취소되었습니다.");
            } else {
                // 이동 실행
                int oldSlot = selected.getSlot();
                selected.setSlot(absoluteSlot);

                if (clickedItem != null) {
                    // 스왑 (대상 위치에 아이템이 있는 경우)
                    clickedItem.setSlot(oldSlot);
                    plugin.getShopManager().saveShopItem(holder.getSection(), clickedItem);
                    player.sendMessage("§a아이템 위치가 교체되었습니다.");
                } else {
                    player.sendMessage("§a아이템이 이동되었습니다.");
                }

                plugin.getShopManager().saveShopItem(holder.getSection(), selected);
                plugin.getEditorManager().deselectItem(player.getUniqueId());
                plugin.getGuiManager().renderPage(holder);
            }
        } else {
            // 아이템 선택
            if (clickedItem != null) {
                plugin.getEditorManager().selectItem(player.getUniqueId(), clickedItem);
                player.sendMessage("§e아이템이 선택되었습니다. 이동할 위치를 클릭하세요.");
            }
        }
    }

    private ShopItem findItemBySlot(ShopSection section, int absoluteSlot) {
        return section.getItems().stream()
                .filter(item -> item.getSlot() == absoluteSlot)
                .findFirst()
                .orElse(null);
    }

    private void handleNavigationClick(Player player, ShopGUIHolder holder, int slot) {
        if (slot == 45 && holder.getPage() > 0) { // 이전 페이지
            holder.setPage(holder.getPage() - 1);
            plugin.getGuiManager().renderPage(holder);
        } else if (slot == 53) { // 다음 페이지
            boolean hasNextPage = holder.getSection().getItems().stream()
                    .anyMatch(item -> item.getSlot() >= (holder.getPage() + 1) * 45);

            if (hasNextPage) {
                holder.setPage(holder.getPage() + 1);
                plugin.getGuiManager().renderPage(holder);
            }
        } else if (slot == 48) { // 메인 메뉴
            plugin.getGuiManager().openMainMenu(player);
        }
    }

    /**
     * 아이템에 맞는 EconomyProvider를 가져옵니다.
     * Logic moved to EconomyManager.getProvider
     */
    private EconomyProvider getEconomyProvider(ShopSection section, ShopItem item) {
        return plugin.getEconomyManager().getProvider(section, item);
    }

    private void performBuy(Player player, ShopSection section, ShopItem item) {
        EconomyProvider economy = getEconomyProvider(section, item);

        if (economy == null || !economy.isAvailable()) {
            player.sendMessage("§c경제 시스템 오류: 사용 가능한 경제 시스템을 찾을 수 없습니다.");
            return;
        }

        // 요구사항 확인 (권한, 레벨 등)
        if (!me.antigravity.economyshop.util.RequirementChecker.checkRequirements(player, item)) {
            String reason = me.antigravity.economyshop.util.RequirementChecker.getFailureReason(player, item);
            me.antigravity.economyshop.util.MessageUtils.sendActionBar(player, "§c" + reason); // Actionbar
            player.sendMessage(plugin.getLangManager().getMessage("error.requirements-not-met"));
            player.sendMessage(reason);
            return;
        }

        // 구매 제한 확인
        if (!plugin.getLimitManager().canPurchase(player.getUniqueId(), item.getId(), item.getPlayerLimit())) {
            String msg = plugin.getLangManager().getMessage("error.limit-reached")
                    .replace("{current}",
                            String.valueOf(
                                    plugin.getLimitManager().getCurrentUsage(player.getUniqueId(), item.getId())))
                    .replace("{max}", String.valueOf(item.getPlayerLimit()));
            me.antigravity.economyshop.util.MessageUtils.sendActionBar(player, msg); // Actionbar
            return;
        }

        double price = item.getCurrentBuyPrice();

        if (economy.has(player, price)) {
            if (economy.withdraw(player, price)) {
                // 아이템 지급 및 잔여물 처리
                java.util.HashMap<Integer, org.bukkit.inventory.ItemStack> leftovers = player.getInventory()
                        .addItem(item.getItemStack().clone());

                if (!leftovers.isEmpty()) {
                    for (org.bukkit.inventory.ItemStack leftover : leftovers.values()) {
                        player.getWorld().dropItem(player.getLocation(), leftover);
                    }
                    player.sendMessage(plugin.getLangManager().getMessage("shop.inventory-full-drop")); // lang에 추가 필요,
                                                                                                        // 혹은 하드코딩 메시지
                    // "인벤토리가 가득 차서 아이템이 바닥에 떨어졌습니다."
                }

                // 재고 감소
                if (item.isDynamicPricing() && item.getCurrentStock() > 0) {
                    item.setCurrentStock(item.getCurrentStock() - 1);
                    // DB 업데이트
                    plugin.getDatabaseManager().saveDynamicPrice(item.getId(), item.getCurrentStock());
                }

                // 구매 기록 갱신
                plugin.getLimitManager().recordPurchase(player.getUniqueId(), item.getId(), 1);

                // 메시지 및 로그
                String msg = plugin.getLangManager().getMessage("shop.buy-success")
                        .replace("{price}", economy.formatAmount(price));
                me.antigravity.economyshop.util.MessageUtils.sendActionBar(player, msg); // Actionbar success

                plugin.getLogManager().logTransaction(player.getName(), "BUY", item.getId(), 1, price);
            } else {
                player.sendMessage("§c거래 처리 중 오류가 발생했습니다. (출금 실패)");
            }
        } else {
            String msg = plugin.getLangManager().getMessage("error.not-enough-money")
                    .replace("{price}", economy.formatAmount(price));
            me.antigravity.economyshop.util.MessageUtils.sendActionBar(player, msg); // Actionbar error
        }
    }

    private void performSell(Player player, ShopSection section, ShopItem item) {
        EconomyProvider economy = getEconomyProvider(section, item);

        if (economy == null || !economy.isAvailable()) {
            player.sendMessage("§c경제 시스템 오류: 사용 가능한 경제 시스템을 찾을 수 없습니다.");
            return;
        }

        if (player.getInventory().containsAtLeast(item.getItemStack(), 1)) {
            player.getInventory().removeItem(item.getItemStack().clone());

            double price = item.getCurrentSellPrice();
            if (economy.deposit(player, price)) {
                // 재고 증가
                if (item.isDynamicPricing() && item.getCurrentStock() < item.getMaxStock()) {
                    item.setCurrentStock(item.getCurrentStock() + 1);
                    // DB 업데이트
                    plugin.getDatabaseManager().saveDynamicPrice(item.getId(), item.getCurrentStock());
                }

                // 메시지 및 로그
                String msg = plugin.getLangManager().getMessage("shop.sell-success")
                        .replace("{price}", economy.formatAmount(price));
                player.sendMessage(msg);

                plugin.getLogManager().logTransaction(player.getName(), "SELL", item.getId(), 1, price);
            } else {
                // 트랜잭션 실패 시 아이템 반환
                player.getInventory().addItem(item.getItemStack().clone());
                player.sendMessage("§c거래 처리 중 오류가 발생했습니다.");
            }
        } else {
            player.sendMessage(plugin.getLangManager().getMessage("shop.sell-fail-no-item"));
        }
    }

    private void handleEditorInventoryClick(InventoryClickEvent event, ShopEditorGUIHolder holder) {
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        ShopItem item = holder.getTargetItem();

        double modifier = event.isShiftClick() ? 100.0 : 10.0;

        // 휠 클릭으로 채팅 편집 모드 진입
        if (event.getClick() == org.bukkit.event.inventory.ClickType.MIDDLE) {
            if (slot == 10) { // 구매 가격
                startDetailEditSession(player, holder.getSection(), item, "BUY_PRICE");
                return;
            } else if (slot == 11) { // 판매 가격
                startDetailEditSession(player, holder.getSection(), item, "SELL_PRICE");
                return;
            }
        }

        switch (slot) {
            case 10: // 구매 가격 수정
                if (event.isLeftClick())
                    item.setBuyPrice(item.getBuyPrice() + modifier);
                else if (event.isRightClick())
                    item.setBuyPrice(Math.max(0, item.getBuyPrice() - modifier));
                plugin.getGuiManager().openItemEditor(player, holder.getSection(), item);
                break;
            case 11: // 판매 가격 수정
                if (event.isLeftClick())
                    item.setSellPrice(item.getSellPrice() + modifier);
                else if (event.isRightClick())
                    item.setSellPrice(Math.max(0, item.getSellPrice() - modifier));
                plugin.getGuiManager().openItemEditor(player, holder.getSection(), item);
                break;
            case 15: // 동적 가격 토글
                item.setDynamicPricing(!item.isDynamicPricing());
                plugin.getGuiManager().openItemEditor(player, holder.getSection(), item);
                break;
            case 22: // 저장
                plugin.getShopManager().saveShopItem(holder.getSection(), item);
                player.sendMessage(plugin.getLangManager().getMessage("editor.saved"));
                player.closeInventory();
                break;
            case 26: // 삭제
                plugin.getShopManager().deleteShopItem(holder.getSection(), item);
                player.sendMessage(plugin.getLangManager().getMessage("editor.deleted"));
                plugin.getGuiManager().openShop(player, holder.getSection()); // 상점 목록으로 돌아감
                break;
        }
    }

    private void startDetailEditSession(Player player, ShopSection section, ShopItem item, String type) {
        player.closeInventory();
        player.sendMessage("§e채팅으로 값을 입력하세요. (소수점 지원, 'cancel' 입력 시 취소)");

        plugin.getEditorManager().startChatSession(player.getUniqueId(), message -> {
            if (message.equalsIgnoreCase("cancel")) {
                player.sendMessage("§c편집이 취소되었습니다.");
                // 다시 에디터 열기 위한 스케줄링 (비동기 콜백일 수 있으므로)
                new org.bukkit.scheduler.BukkitRunnable() {
                    @Override
                    public void run() {
                        plugin.getGuiManager().openItemEditor(player, section, item);
                    }
                }.runTask(plugin);
                return;
            }

            try {
                if (type.equals("BUY_PRICE")) {
                    double val = Double.parseDouble(message);
                    item.setBuyPrice(val);
                    player.sendMessage("§a구매 가격이 설정되었습니다: " + val);
                } else if (type.equals("SELL_PRICE")) {
                    double val = Double.parseDouble(message);
                    item.setSellPrice(val);
                    player.sendMessage("§a판매 가격이 설정되었습니다: " + val);
                }

                // 저장 및 다시 열기
                new org.bukkit.scheduler.BukkitRunnable() {
                    @Override
                    public void run() {
                        plugin.getShopManager().saveShopItem(section, item);
                        plugin.getGuiManager().openItemEditor(player, section, item);
                    }
                }.runTask(plugin);

            } catch (NumberFormatException e) {
                player.sendMessage("§c유효한 숫자가 아닙니다. 편집 세션이 유지됩니다.");
                // 세션 유지하려면 다시 등록해야 함 (Map.remove로 콜백이 사라짐)
                // 하지만 현재 구조상 1회성이므로 그냥 종료되거나 재진입 필요.
                // 사용자 편의를 위해 '다시 입력하세요'가 좋지만, 여기선 일단 취소 처리 or 재등록.
                // 재등록 로직은 복잡해질 수 있으니, "다시 휠 클릭하세요"로 유도하거나 재귀 호출
                // 재귀 호출:
                startDetailEditSession(player, section, item, type);
            }
        });
    }
}
