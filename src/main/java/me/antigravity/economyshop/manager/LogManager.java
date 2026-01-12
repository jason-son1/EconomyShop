package me.antigravity.economyshop.manager;

import me.antigravity.economyshop.EconomyShop;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogManager {

    private final EconomyShop plugin;
    private File logFile;
    private me.antigravity.economyshop.hook.DiscordHook discordHook;

    public LogManager(EconomyShop plugin) {
        this.plugin = plugin;
        this.discordHook = new me.antigravity.economyshop.hook.DiscordHook(plugin);
        createLogFile();
    }

    private void createLogFile() {
        File folder = new File(plugin.getDataFolder(), "logs");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        // 날짜별 로그 파일 생성 (logs/2023-10-27.log)
        String date = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE);
        this.logFile = new File(folder, date + ".log");

        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("로그 파일 생성 실패: " + e.getMessage());
            }
        }
    }

    public void log(String message) {
        // 비동기로 파일 쓰기 (성능 저하 방지)
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            String logMessage = "[" + timestamp + "] " + message;

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                writer.write(logMessage);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void logTransaction(String player, String type, String item, int amount, double price) {
        String msg = String.format("[TRANSACTION] Player: %s | Type: %s | Item: %s | Amount: %d | Total: %.2f",
                player, type, item, amount, price);
        log(msg);

        // Discord로 전송 (Embed 형식)
        if (discordHook != null) {
            discordHook.sendTransactionLog(player, type, item, amount, price);
        }
    }

    /**
     * 로그 파일에서 거래 내역을 조회합니다.
     * 
     * @param targetName 조회할 플레이어 이름 (null이면 전체)
     * @param page       페이지 (1부터 시작)
     * @return CompletableFuture<List<String>> 조회된 로그 목록
     */
    public java.util.concurrent.CompletableFuture<java.util.List<String>> getTransactionLogs(String targetName,
            int page) {
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            java.util.List<String> results = new java.util.ArrayList<>();
            if (!logFile.exists()) {
                return results;
            }

            try {
                java.util.List<String> lines = java.nio.file.Files.readAllLines(logFile.toPath(),
                        java.nio.charset.StandardCharsets.UTF_8);
                java.util.Collections.reverse(lines); // 최신순 정렬

                int count = 0;
                int pageSize = 10;
                int start = (page - 1) * pageSize;

                for (String line : lines) {
                    // 트랜잭션 로그만 필터링
                    if (!line.contains("[TRANSACTION]")) {
                        continue;
                    }

                    // 플레이어 필터링
                    if (targetName != null && !line.contains("Player: " + targetName)) {
                        continue;
                    }

                    if (count >= start && count < start + pageSize) {
                        results.add(line);
                    }
                    count++;

                    if (count >= start + pageSize) {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return results;
        });
    }

    /**
     * LogManager 종료 시 DiscordHook 워커 스레드를 정리합니다.
     */
    public void shutdown() {
        if (discordHook != null) {
            discordHook.shutdown();
        }
    }
}
