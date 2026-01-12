package me.antigravity.economyshop.manager;

import me.antigravity.economyshop.EconomyShop;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 인게임 상점 에디터 세션을 관리하는 매니저 클래스입니다.
 */
public class EditorManager {

    private final EconomyShop plugin;
    private final Set<UUID> activeEditors = new HashSet<>();

    public EditorManager(EconomyShop plugin) {
        this.plugin = plugin;
    }

    /**
     * 플레이어를 에디터 모드로 설정하거나 해제합니다.
     */
    public void toggleEditor(UUID uuid) {
        if (activeEditors.contains(uuid)) {
            activeEditors.remove(uuid);
        } else {
            activeEditors.add(uuid);
        }
    }

    /**
     * 플레이어가 현재 에디터 모드인지 확인합니다.
     */
    public boolean isEditor(UUID uuid) {
        return activeEditors.contains(uuid);
    }

    private final java.util.Map<UUID, me.antigravity.economyshop.model.ShopItem> selectedItems = new java.util.HashMap<>();

    public void selectItem(UUID uuid, me.antigravity.economyshop.model.ShopItem item) {
        selectedItems.put(uuid, item);
    }

    public me.antigravity.economyshop.model.ShopItem getSelectedItem(UUID uuid) {
        return selectedItems.get(uuid);
    }

    public void deselectItem(UUID uuid) {
        selectedItems.remove(uuid);
    }

    public boolean hasSelectedItem(UUID uuid) {
        return selectedItems.containsKey(uuid);
    }

    private final java.util.Map<UUID, java.util.function.Consumer<String>> chatSessions = new java.util.HashMap<>();

    public void startChatSession(UUID uuid, java.util.function.Consumer<String> callback) {
        chatSessions.put(uuid, callback);
    }

    public boolean hasChatSession(UUID uuid) {
        return chatSessions.containsKey(uuid);
    }

    public void handleChat(UUID uuid, String message) {
        if (chatSessions.containsKey(uuid)) {
            java.util.function.Consumer<String> callback = chatSessions.remove(uuid);
            callback.accept(message);
        }
    }

    public void cancelChatSession(UUID uuid) {
        chatSessions.remove(uuid);
    }
}
