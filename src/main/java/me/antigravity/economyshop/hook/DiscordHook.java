package me.antigravity.economyshop.hook;

import me.antigravity.economyshop.EconomyShop;
import me.antigravity.economyshop.util.DiscordWebhook;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Discord 웹훅 통합 클래스
 * 비동기 큐 패턴을 사용하여 거래 로그를 Discord로 전송합니다.
 * DiscordSRV 플러그인에 의존하지 않고 직접 웹훅을 사용합니다.
 */
public class DiscordHook {

    private final EconomyShop plugin;
    private DiscordWebhook webhook;
    private final BlockingQueue<LogMessage> messageQueue;
    private Thread workerThread;
    private volatile boolean running = false;

    public DiscordHook(EconomyShop plugin) {
        this.plugin = plugin;
        this.messageQueue = new LinkedBlockingQueue<>();
        setup();
    }

    private void setup() {
        String webhookUrl = plugin.getConfigManager().getMainConfig().getString("discord-webhook-url", "");

        if (webhookUrl == null || webhookUrl.isEmpty()) {
            plugin.getLogger().info("Discord 웹훅 URL이 설정되지 않았습니다. Discord 연동이 비활성화됩니다.");
            return;
        }

        this.webhook = new DiscordWebhook(webhookUrl);
        this.running = true;

        // 생산자-소비자 패턴: 워커 스레드 시작
        workerThread = new Thread(this::processQueue, "EconomyShop-DiscordWorker");
        workerThread.setDaemon(true);
        workerThread.start();

        plugin.getLogger().info("Discord 웹훅 연동이 활성화되었습니다.");
    }

    /**
     * 거래 로그를 Discord로 전송합니다 (비동기).
     * 
     * @param playerName 플레이어 이름
     * @param action     행동 (BUY, SELL, SELLALL)
     * @param itemId     아이템 ID
     * @param amount     수량
     * @param price      가격
     */
    public void sendTransactionLog(String playerName, String action, String itemId, int amount, double price) {
        if (webhook == null || !running)
            return;

        LogMessage message = new LogMessage(playerName, action, itemId, amount, price);
        messageQueue.offer(message);
    }

    /**
     * 간단한 메시지를 Discord로 전송합니다.
     */
    public void sendMessage(String content) {
        if (webhook == null || !running)
            return;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            webhook.sendMessage(content);
        });
    }

    /**
     * 큐에서 메시지를 꺼내 처리하는 워커 스레드
     */
    private void processQueue() {
        while (running) {
            try {
                LogMessage message = messageQueue.take(); // 블로킹 대기
                sendToDiscord(message);

                // API Rate Limit 방지를 위한 짧은 대기
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Discord 웹훅으로 Embed 메시지 전송
     */
    private void sendToDiscord(LogMessage message) {
        try {
            String title = "🛒 " + getActionEmoji(message.action) + " " + getActionName(message.action);
            String description = String.format("플레이어 **%s**님이 아이템을 %s했습니다.",
                    message.playerName, getActionVerb(message.action));

            int color = getActionColor(message.action);

            webhook.sendEmbed(
                    title,
                    description,
                    color,
                    "아이템", message.itemId,
                    "수량", String.valueOf(message.amount),
                    "가격", String.format("%.2f원", message.price));
        } catch (Exception e) {
            plugin.getLogger().warning("Discord 웹훅 전송 실패: " + e.getMessage());
        }
    }

    private String getActionEmoji(String action) {
        return switch (action.toUpperCase()) {
            case "BUY" -> "💰";
            case "SELL", "SELLALL" -> "💸";
            default -> "📦";
        };
    }

    private String getActionName(String action) {
        return switch (action.toUpperCase()) {
            case "BUY" -> "구매";
            case "SELL" -> "판매";
            case "SELLALL" -> "전체 판매";
            default -> "거래";
        };
    }

    private String getActionVerb(String action) {
        return switch (action.toUpperCase()) {
            case "BUY" -> "구매";
            case "SELL", "SELLALL" -> "판매";
            default -> "거래";
        };
    }

    private int getActionColor(String action) {
        return switch (action.toUpperCase()) {
            case "BUY" -> 0x00AA00; // 녹색
            case "SELL", "SELLALL" -> 0xFF5555; // 빨간색
            default -> 0x5555FF; // 파란색
        };
    }

    /**
     * 워커 스레드를 종료하고 남은 메시지를 처리합니다.
     */
    public void shutdown() {
        if (!running)
            return;

        running = false;

        if (workerThread != null) {
            workerThread.interrupt();
            try {
                workerThread.join(5000); // 최대 5초 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 남은 메시지 처리
        while (!messageQueue.isEmpty()) {
            LogMessage message = messageQueue.poll();
            if (message != null) {
                sendToDiscord(message);
            }
        }
    }

    /**
     * 로그 메시지를 담는 레코드
     */
    private record LogMessage(String playerName, String action, String itemId, int amount, double price) {
    }
}
