package ir.hanzodev1375.ghostide.ai.network;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import ir.hanzodev1375.ghostide.ai.model.OpencodeModelInfo;

/**
 * Fetches the public model catalogue from openrouter.ai. Lists every model (free, paid or
 * currently-broken) so the user can pick whichever they want.
 */
public class OpenRouterModelsClient {

  private static final String MODELS_URL = "https://openrouter.ai/api/v1/models";

  /** Blocking call – run it off the main thread. */
  public List<OpencodeModelInfo> getModels() throws Exception {
    URL url = new URL(MODELS_URL);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Accept", "application/json");
    conn.setRequestProperty("User-Agent", "GhostIDE/1.0");
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
      throw new Exception("OpenRouter error " + code + ": " + sb);
    }

    JSONObject root = new JSONObject(sb.toString());
    JSONArray data = root.optJSONArray("data");
    List<OpencodeModelInfo> models = new ArrayList<>();
    if (data == null) {
      return models;
    }

    for (int i = 0; i < data.length(); i++) {
      JSONObject m = data.optJSONObject(i);
      if (m == null) continue;

      String id = m.optString("id");
      if (id == null || id.isEmpty()) continue;

      String name = m.optString("name", id);
      String splitProvider = "openrouter";
      String cleanId = id;
      int slash = id.indexOf('/');
      if (slash > 0) {
        splitProvider = id.substring(0, slash);
        cleanId = id.substring(slash + 1);
      }

      boolean free = isFree(m, id);
      boolean available = isAvailable(m);

      models.add(
          new OpencodeModelInfo(splitProvider, splitProvider, cleanId, name, free, available));
    }
    return models;
  }

  private boolean isFree(JSONObject m, String id) {
    if (id != null && id.endsWith(":free")) {
      return true;
    }
    JSONObject pricing = m.optJSONObject("pricing");
    if (pricing == null) {
      return false;
    }
    double prompt = parsePrice(pricing.optString("prompt"), pricing.opt("prompt"));
    double completion = parsePrice(pricing.optString("completion"), pricing.opt("completion"));
    return prompt <= 0 && completion <= 0;
  }

  private double parsePrice(String str, Object raw) {
    if (str != null && !str.isEmpty() && !"null".equals(str)) {
      try {
        return Double.parseDouble(str);
      } catch (NumberFormatException ignored) {
      }
    }
    if (raw instanceof Number) {
      return ((Number) raw).doubleValue();
    }
    return -1;
  }

  private boolean isAvailable(JSONObject m) {
    String endpoint = m.optString("endpoint");
    if (endpoint == null || endpoint.isEmpty() || "{{ BASE_URL }}".equals(endpoint)) {
      return false;
    }
    boolean untested = m.optBoolean("untested", false);
    boolean deprecated = m.optBoolean("deprecated", false);
    return !deprecated && !untested;
  }
}