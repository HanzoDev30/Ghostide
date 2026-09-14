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

/**
 * Handles both ChatGPT (OpenAI) and DeepSeek since they share the same API format. Images are sent
 * via the vision content-part format (GPT-4o supports this). DeepSeek does not support vision –
 * images are skipped gracefully.
 */
public class OpenAiCompatibleClient implements AiClient {

  private static final String TAG = "OpenAiClient";

  private final String apiKey;
  private final String model;
  private final String baseUrl;
  private final String providerName;
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private final Handler mainHandler = new Handler(Looper.getMainLooper());

  public OpenAiCompatibleClient(String apiKey, String model, String baseUrl, String providerName) {
    this.apiKey = apiKey;
    this.model = model;
    this.baseUrl = baseUrl;
    this.providerName = providerName;
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

            // System prompt
            JSONObject system = new JSONObject();
            system.put("role", "system");
            system.put("content", "You are a helpful AI assistant integrated in GhostIDE.");
            messages.put(system);

            // History — جلوگیری از role تکراری
            String lastRole = "system";
            for (ChatMessage msg : history) {
              if (msg.getType() != ChatMessage.TYPE_USER && msg.getType() != ChatMessage.TYPE_AI)
                continue;

              String role = msg.getType() == ChatMessage.TYPE_USER ? "user" : "assistant";
              if (role.equals(lastRole)) continue;

              String content = msg.getContent();
              if (content == null || content.trim().isEmpty()) continue;

              JSONObject m = new JSONObject();
              m.put("role", role);
              m.put("content", content.trim());
              messages.put(m);
              lastRole = role;
            }

            // پیام فعلی کاربر
            String safeMessage = (userMessage != null) ? userMessage.trim() : "";
            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");

            boolean supportsVision = AiConstants.AiProvider.CHATGPT.equals(providerName);
            boolean hasAttachments = attachments != null && !attachments.isEmpty();

            if (!hasAttachments) {
              if (safeMessage.isEmpty()) {
                mainHandler.post(() -> callback.onError("پیام خالی است"));
                return;
              }
              userMsg.put("content", safeMessage);
            } else {
              JSONArray contentParts = new JSONArray();

              // تصاویر — فقط ChatGPT
              for (AttachedFile file : attachments) {
                if (file.isImage() && supportsVision && file.getBase64Data() != null) {
                  String mime = file.getMimeType();
                  if (mime == null || mime.isEmpty()) mime = "image/jpeg";

                  JSONObject imageUrl = new JSONObject();
                  imageUrl.put("url", "data:" + mime + ";base64," + file.getBase64Data());
                  imageUrl.put("detail", "auto");

                  JSONObject imagePart = new JSONObject();
                  imagePart.put("type", "image_url");
                  imagePart.put("image_url", imageUrl);
                  contentParts.put(imagePart);
                }
              }

              // فایل‌های متنی
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

              // متن کاربر
              if (!safeMessage.isEmpty()) {
                JSONObject textPart = new JSONObject();
                textPart.put("type", "text");
                textPart.put("text", safeMessage);
                contentParts.put(textPart);
              }

              if (contentParts.length() == 0) {
                mainHandler.post(() -> callback.onError("پیام خالی است"));
                return;
              }

              userMsg.put("content", contentParts);
            }
            messages.put(userMsg);

            // Build body
            JSONObject body = new JSONObject();
            body.put("model", model);
            body.put("messages", messages);
            body.put("max_tokens", 4096);

            // HTTP
            URL url = new URL(baseUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(30_000);
            conn.setReadTimeout(60_000);

            try (OutputStream os = conn.getOutputStream()) {
              os.write(body.toString().getBytes(StandardCharsets.UTF_8));
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
              String text =
                  resp.getJSONArray("choices")
                      .getJSONObject(0)
                      .getJSONObject("message")
                      .getString("content");
              mainHandler.post(() -> callback.onSuccess(text));
            } else {
              Log.e(TAG, providerName + " Error " + code + ": " + sb);
              final String err = "Error " + code + ": " + sb;
              mainHandler.post(() -> callback.onError(err));
            }

          } catch (Exception e) {
            Log.e(TAG, providerName + " API error", e);
            mainHandler.post(() -> callback.onError(e.getMessage()));
          }
        });
  }

  public void shutdown() {
    executor.shutdown();
  }

  @Override
  public String getProviderName() {
    return providerName;
  }
}
