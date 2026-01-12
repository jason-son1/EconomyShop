package me.antigravity.economyshop.listener;

import me.antigravity.economyshop.EconomyShop;
import me.antigravity.economyshop.model.ShopSection;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Citizens NPC 연동을 위한 리스너 클래스입니다.
 */
public class NPCListener implements Listener {

    private final EconomyShop plugin;

    public NPCListener(EconomyShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        int npcId = event.getNPC().getId();
        FileConfiguration config = plugin.getConfigManager().getMainConfig();

        // config.yml에서 npc-binds 섹션 확인
        // 예: npc-binds: { 5: "farming" }
        String sectionId = config.getString("npc-binds." + npcId);

        if (sectionId != null) {
            ShopSection section = plugin.getShopManager().getSections().get(sectionId);
            if (section != null) {
                plugin.getGuiManager().openShop(event.getClicker(), section);
            }
        }
    }
}
