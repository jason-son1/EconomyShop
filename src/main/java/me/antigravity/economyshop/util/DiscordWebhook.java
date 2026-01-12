package me.antigravity.economyshop.util;

import com.google.gson.JsonObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Discord 웹훅 전송 유틸리티
 * DiscordSRV에 의존하지 않고 직접 웹훅을 전송합니다.
 */
public class DiscordWebhook {

    private final String webhookUrl;

    public DiscordWebhook(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    /**
     * 간단한 메시지를 Discord로 전송합니다.
     */
    public void sendMessage(String content) {
        if (webhookUrl == null || webhookUrl.isEmpty())
            return;

        JsonObject payload = new JsonObject();
        payload.addProperty("content", content);

        sendPayload(payload.toString());
    }

    /**
     * Embed 형식의 메시지를 전송합니다.
     */
    public void sendEmbed(String title, String description, int color, String... fields) {
        if (webhookUrl == null || webhookUrl.isEmpty())
            return;

        JsonObject embed = new JsonObject();
        embed.addProperty("title", title);
        embed.addProperty("description", description);
        embed.addProperty("color", color);
        embed.addProperty("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

        // 필드 추가 (이름:값 쌍)
        if (fields.length > 0 && fields.length % 2 == 0) {
            com.google.gson.JsonArray fieldsArray = new com.google.gson.JsonArray();
            for (int i = 0; i < fields.length; i += 2) {
                JsonObject field = new JsonObject();
                field.addProperty("name", fields[i]);
                field.addProperty("value", fields[i + 1]);
                field.addProperty("inline", true);
                fieldsArray.add(field);
            }
            embed.add("fields", fieldsArray);
        }

        com.google.gson.JsonArray embeds = new com.google.gson.JsonArray();
        embeds.add(embed);

        JsonObject payload = new JsonObject();
        payload.add("embeds", embeds);

        sendPayload(payload.toString());
    }

    /**
     * JSON 페이로드를 웹훅으로 전송합니다.
     */
    private void sendPayload(String jsonPayload) {
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "EconomyShop-Webhook");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                System.err.println("Discord webhook failed with response code: " + responseCode);
            }

            connection.disconnect();
        } catch (Exception e) {
            System.err.println("Failed to send Discord webhook: " + e.getMessage());
        }
    }
}
