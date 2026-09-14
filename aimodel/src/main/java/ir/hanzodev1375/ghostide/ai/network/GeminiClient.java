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

public class GeminiClient implements AiClient {

    private static final String TAG = "GeminiClient";

    private final String          apiKey;
    private final String          model;
    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    public GeminiClient(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model  = model;
    }

    @Override
    public void sendMessage(
            List<ChatMessage>  history,
            String             userMessage,
            List<AttachedFile> attachments,
            Callback           callback) {

        executor.execute(() -> {
            try {
                JSONArray contents = new JSONArray();

                // History
                for (ChatMessage msg : history) {
                    if (msg.getType() == ChatMessage.TYPE_USER
                            || msg.getType() == ChatMessage.TYPE_AI) {
                        JSONObject part = new JSONObject();
                        part.put("text", msg.getContent());
                        JSONArray parts = new JSONArray();
                        parts.put(part);

                        JSONObject content = new JSONObject();
                        content.put("role",  msg.getType() == ChatMessage.TYPE_USER ? "user" : "model");
                        content.put("parts", parts);
                        contents.put(content);
                    }
                }

                // Current user turn – build parts list
                JSONArray userParts = new JSONArray();

                // Images
                for (AttachedFile file : attachments) {
                    if (file.isImage() && file.getBase64Data() != null) {
                        JSONObject inlineData = new JSONObject();
                        inlineData.put("mime_type", "image/jpeg");
                        inlineData.put("data",      file.getBase64Data());

                        JSONObject imgPart = new JSONObject();
                        imgPart.put("inline_data", inlineData);
                        userParts.put(imgPart);
                    }
                }

                // Text files
                for (AttachedFile file : attachments) {
                    if (!file.isImage() && file.getTextContent() != null) {
                        String fileBlock = "```\n[File: " + file.getName() + "]\n"
                                + file.getTextContent() + "\n```";
                        JSONObject textPart = new JSONObject();
                        textPart.put("text", fileBlock);
                        userParts.put(textPart);
                    }
                }

                // User text
                if (userMessage != null && !userMessage.isEmpty()) {
                    JSONObject textPart = new JSONObject();
                    textPart.put("text", userMessage);
                    userParts.put(textPart);
                }

                JSONObject userContent = new JSONObject();
                userContent.put("role",  "user");
                userContent.put("parts", userParts);
                contents.put(userContent);

                JSONObject body = new JSONObject();
                body.put("contents", contents);

                String endpoint = AiConstants.ApiEndpoints.GEMINI_BASE_URL
                        + model + ":generateContent?key=" + apiKey;
                URL               url  = new URL(endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(30_000);
                conn.setReadTimeout(60_000);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                }

                int           code   = conn.getResponseCode();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                    code == 200 ? conn.getInputStream() : conn.getErrorStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                if (code == 200) {
                    JSONObject resp = new JSONObject(sb.toString());
                    String text = resp.getJSONArray("candidates")
                                      .getJSONObject(0)
                                      .getJSONObject("content")
                                      .getJSONArray("parts")
                                      .getJSONObject(0)
                                      .getString("text");
                    mainHandler.post(() -> callback.onSuccess(text));
                } else {
                    final String err = "Error " + code + ": " + sb;
                    mainHandler.post(() -> callback.onError(err));
                }

            } catch (Exception e) {
                Log.e(TAG, "Gemini API error", e);
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    @Override
    public String getProviderName() { return AiConstants.AiProvider.GEMINI; }
}
