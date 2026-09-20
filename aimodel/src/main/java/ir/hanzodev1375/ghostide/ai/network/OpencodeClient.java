package ir.hanzodev1375.ghostide.ai.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ir.hanzodev1375.ghostide.ai.model.AttachedFile;
import ir.hanzodev1375.ghostide.ai.model.ChatMessage;
import ir.hanzodev1375.ghostide.ai.model.OpencodeModelInfo;
import ir.hanzodev1375.ghostide.ai.utils.AiConstants;

public class OpencodeClient implements AiClient {

  private static final String TAG = "OpencodeClient";

  private final String baseUrl;
  private final String username;
  private final String password;
  private final String model;
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private final Handler mainHandler = new Handler(Looper.getMainLooper());

  private volatile String sessionId;

  public OpencodeClient(String baseUrl, String username, String password) {
    this(baseUrl, username, password, null);
  }

  public OpencodeClient(String baseUrl, String username, String password, String model) {
    String url = baseUrl == null ? "" : baseUrl.trim();
    while (url.endsWith("/")) {
      url = url.substring(0, url.length() - 1);
    }
    this.baseUrl = url.isEmpty() ? AiConstants.ApiEndpoints.OPENCODE_BASE_URL : url;
    this.username = username;
    this.password = password;
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
            if (sessionId == null) {
              sessionId = createSession();
            }

            JSONObject body = new JSONObject();
            JSONArray parts = new JSONArray();

            if (attachments != null) {
              for (AttachedFile file : attachments) {
                if (file == null) continue;
                if (file.isImage()) {
                  String name = file.getName();
                  parts.put(textPart("[Image: " + (name == null || name.isEmpty() ? "image" : name) + "]"));
                } else if (file.getTextContent() != null && !file.getTextContent().trim().isEmpty()) {
                  parts.put(textPart(fileBlock(file.getName(), file.getTextContent())));
                }
              }
            }

            String safeMessage = userMessage == null ? "" : userMessage.trim();
            if (!safeMessage.isEmpty()) {
              parts.put(textPart(safeMessage));
            }

            if (parts.length() == 0) {
              mainHandler.post(() -> callback.onError("پیام خالی است"));
              return;
            }

            body.put("parts", parts);
            body.put("noReply", false);
            if (model != null && !model.trim().isEmpty()) {
              body.put("model", model.trim());
            }

            JSONObject resp = postJson(baseUrl + "/session/" + sessionId + "/message", body);
            String text = extractText(resp);
            mainHandler.post(() -> callback.onSuccess(text.isEmpty() ? "" : text));
          } catch (Exception e) {
            Log.e(TAG, "OpenCode error", e);
            mainHandler.post(() -> callback.onError(e.getMessage()));
          }
        });
  }

  private String createSession() throws Exception {
    JSONObject body = new JSONObject();
    body.put("title", "GhostIDE Chat");
    JSONObject resp = postJson(baseUrl + "/session", body);
    return resp.getString("id");
  }

  private JSONObject textPart(String text) {
    JSONObject part = new JSONObject();
    try {
      part.put("type", "text");
      part.put("text", text);
    } catch (Exception ignored) {
    }
    return part;
  }

  private String fileBlock(String name, String content) {
    return "```\n[File: " + name + "]\n" + content.trim() + "\n```";
  }

  private String extractText(JSONObject resp) throws Exception {
    JSONArray parts = resp.optJSONArray("parts");
    StringBuilder sb = new StringBuilder();
    if (parts != null) {
      for (int i = 0; i < parts.length(); i++) {
        JSONObject part = parts.optJSONObject(i);
        if (part == null) continue;
        if (!"text".equals(part.optString("type"))) continue;
        String text = part.optString("text");
        if (text == null || text.isEmpty()) continue;
        if (sb.length() > 0) sb.append("\n\n");
        sb.append(text);
      }
    }
    JSONObject info = resp.optJSONObject("info");
    if (sb.length() == 0 && info != null) {
      String text = info.optString("text");
      if (text != null && !text.isEmpty()) sb.append(text);
    }
    return sb.toString();
  }

  private JSONObject postJson(String urlStr, JSONObject body) throws Exception {
    URL url = new URL(urlStr);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
    if (password != null && !password.isEmpty()) {
      String user = (username == null || username.isEmpty()) ? "opencode" : username;
      String cred =
          Base64.encodeToString(
              (user + ":" + password).getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
      conn.setRequestProperty("Authorization", "Basic " + cred);
    }
    conn.setDoOutput(true);
    conn.setConnectTimeout(30_000);
    conn.setReadTimeout(120_000);

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
    conn.disconnect();

    if (code != 200) {
      throw new Exception("OpenCode server error " + code + ": " + sb);
    }
    return new JSONObject(sb.toString());
  }

  public void shutdown() {
    executor.shutdown();
  }

  @Override
  public String getProviderName() {
    return AiConstants.AiProvider.OPENCODE;
  }

  /** Fetches the providers and their models from the opencode server (blocking). */
  public List<OpencodeModelInfo> getModels() throws Exception {
    JSONObject resp = getJson(baseUrl + "/provider");
    JSONArray all = resp.optJSONArray("all");
    if (all == null) {
      return Collections.emptyList();
    }
    JSONArray connected = resp.optJSONArray("connected");
    List<String> connectedIds = new ArrayList<>();
    if (connected != null) {
      for (int i = 0; i < connected.length(); i++) {
        String id = connected.optString(i);
        if (id != null && !id.isEmpty()) {
          connectedIds.add(id);
        }
      }
    }

    List<OpencodeModelInfo> models = new ArrayList<>();
    for (int i = 0; i < all.length(); i++) {
      JSONObject provider = all.optJSONObject(i);
      if (provider == null) {
        continue;
      }
      String providerId = provider.optString("id");
      String providerName = provider.optString("name", providerId);
      boolean connectedState = connectedIds.contains(providerId);
      JSONArray providerModels = provider.optJSONArray("models");
      if (providerModels == null) {
        continue;
      }
      for (int j = 0; j < providerModels.length(); j++) {
        JSONObject m = providerModels.optJSONObject(j);
        if (m == null) {
          continue;
        }
        String modelId = m.optString("modelID");
        if (modelId == null || modelId.isEmpty()) {
          modelId = m.optString("id");
        }
        if (modelId == null || modelId.isEmpty()) {
          continue;
        }
        String name = m.optString("name", modelId);
        boolean free = isFree(m);
        models.add(new OpencodeModelInfo(providerId, providerName, modelId, name, free, connectedState));
      }
    }
    return models;
  }

  private boolean isFree(JSONObject model) {
    JSONObject price = model.optJSONObject("price");
    if (price == null) {
      return true;
    }
    double input = price.optDouble("input", 0d);
    double output = price.optDouble("output", 0d);
    return !price.has("input") && !price.has("output") || (input <= 0 && output <= 0);
  }

  private JSONObject getJson(String urlStr) throws Exception {
    URL url = new URL(urlStr);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Accept", "application/json");
    applyAuth(conn);
    conn.setConnectTimeout(30_000);
    conn.setReadTimeout(30_000);

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
    conn.disconnect();

    if (code != 200) {
      throw new Exception("OpenCode server error " + code + ": " + sb);
    }
    return new JSONObject(sb.toString());
  }

  private void applyAuth(HttpURLConnection conn) {
    if (password != null && !password.isEmpty()) {
      String user = (username == null || username.isEmpty()) ? "opencode" : username;
      String cred =
          Base64.encodeToString(
              (user + ":" + password).getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
      conn.setRequestProperty("Authorization", "Basic " + cred);
    }
  }
}