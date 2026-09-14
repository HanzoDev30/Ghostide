package ir.hanzodev1375.ghostide.ai.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ir.hanzodev1375.ghostide.ai.model.AttachedFile;
import ir.hanzodev1375.ghostide.ai.model.ChatMessage;
import ir.hanzodev1375.ghostide.ai.utils.AiConstants;

public class ClaudeClient implements AiClient {

  private static final String TAG = "ClaudeClient";

  private final String apiKey;
  private final String model;
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private final Handler mainHandler = new Handler(Looper.getMainLooper());

  public ClaudeClient(String apiKey, String model) {
    this.apiKey = apiKey;
    this.model = model;
  }

  @Override
  public void sendMessage(
      List<ChatMessage> history,
      String userMessage,
      List<AttachedFile> attachments,
      Callback callback) {

    executor.execute(
        () -> {
          try {
            JSONArray messages = new JSONArray();

            // ── History ────────────────────────────────────────────────────
            // Claude API اجازه نمیده دو role یکسان پشت سر هم باشن
            String lastRole = null;
            for (ChatMessage msg : history) {
              if (msg.getType() != ChatMessage.TYPE_USER && msg.getType() != ChatMessage.TYPE_AI)
                continue;

              String role = msg.getType() == ChatMessage.TYPE_USER ? "user" : "assistant";

              // skip اگه role تکراری پشت سر هم بود
              if (role.equals(lastRole)) continue;

              String content = msg.getContent();
              if (content == null || content.trim().isEmpty()) continue;

              JSONObject m = new JSONObject();
              m.put("role", role);
              m.put("content", content.trim());
              messages.put(m);
              lastRole = role;
            }

            // ── پیام فعلی کاربر ───────────────────────────────────────────
            JSONArray contentParts = new JSONArray();

            // 1. تصاویر ضمیمه
            if (attachments != null) {
              for (AttachedFile file : attachments) {
                if (file.isImage() && file.getBase64Data() != null) {
                  String mime = "";
                  if (mime == null || mime.isEmpty()) mime = "image/jpeg";

                  JSONObject imageSource = new JSONObject();
                  imageSource.put("type", "base64");
                  imageSource.put("media_type", mime);
                  imageSource.put("data", file.getBase64Data());

                  JSONObject imagePart = new JSONObject();
                  imagePart.put("type", "image");
                  imagePart.put("source", imageSource);
                  contentParts.put(imagePart);
                }
              }

              // 2. فایل‌های متنی
              for (AttachedFile file : attachments) {
                if (!file.isImage()
                    && file.getTextContent() != null
                    && !file.getTextContent().trim().isEmpty()) {
                  String fileBlock =
                      "```\n[File: "
                          + file.getName()
                          + "]\n"
                          + file.getTextContent().trim()
                          + "\n```";
                  JSONObject textPart = new JSONObject();
                  textPart.put("type", "text");
                  textPart.put("text", fileBlock);
                  contentParts.put(textPart);
                }
              }
            }

            // 3. متن کاربر
            String safeMessage = (userMessage != null) ? userMessage.trim() : "";
            if (!safeMessage.isEmpty()) {
              JSONObject textPart = new JSONObject();
              textPart.put("type", "text");
              textPart.put("text", safeMessage);
              contentParts.put(textPart);
            }

            // اگه هیچ content نداشتیم، callback با خطا
            if (contentParts.length() == 0) {
              mainHandler.post(() -> callback.onError("پیام خالی است"));
              return;
            }

            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", contentParts);
            messages.put(userMsg);

            // ── Build request body ─────────────────────────────────────────
            JSONObject body = new JSONObject();
            body.put("model", model);
            body.put("max_tokens", 4096);
            body.put("messages", messages);

            // ── HTTP call ──────────────────────────────────────────────────
            URL url = new URL(AiConstants.ApiEndpoints.CLAUDE_BASE_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("x-api-key", apiKey);
            conn.setRequestProperty("anthropic-version", "2023-06-01");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30_000);
            conn.setReadTimeout(60_000);

            byte[] bodyBytes = body.toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conn.getOutputStream()) {
              os.write(bodyBytes);
            }

            int code = conn.getResponseCode();
            BufferedReader reader =
                new BufferedReader(
                    new InputStreamReader(
                        code == 200 ? conn.getInputStream() : conn.getErrorStream(),
                        StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            if (code == 200) {
              JSONObject resp = new JSONObject(sb.toString());
              JSONArray contents = resp.getJSONArray("content");
              StringBuilder result = new StringBuilder();
              for (int i = 0; i < contents.length(); i++) {
                JSONObject block = contents.getJSONObject(i);
                if ("text".equals(block.optString("type"))) {
                  result.append(block.getString("text"));
                }
              }
              final String text = result.toString();
              mainHandler.post(() -> callback.onSuccess(text));
            } else {
              Log.e(TAG, "API Error " + code + ": " + sb);
              final String err = "Error " + code + ": " + sb;
              mainHandler.post(() -> callback.onError(err));
            }

          } catch (Exception e) {
            Log.e(TAG, "Claude API error", e);
            mainHandler.post(() -> callback.onError(e.getMessage()));
          }
        });
  }

  public void shutdown() {
    executor.shutdown();
  }

  @Override
  public String getProviderName() {
    return AiConstants.AiProvider.CLAUDE;
  }
}
