package ir.hanzodev1375.ghostide.codeeditors.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;

public class TranslateTask {

  public interface Callback {
    void onSuccess(String translatedText);

    void onFailure(String error);
  }

  private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

  private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

  private final String text;
  private final String targetLang;
  private final Callback callback;

  public TranslateTask(String text, String targetLang, Callback callback) {

    this.text = text;
    this.targetLang = targetLang;
    this.callback = callback;
  }

  public void execute() {
    EXECUTOR.execute(
        () -> {
          try {

            String encodedText = URLEncoder.encode(text, "UTF-8");

            String urlStr =
                "https://translate.googleapis.com/translate_a/single"
                    + "?client=gtx"
                    + "&sl=auto"
                    + "&tl="
                    + targetLang
                    + "&dt=t"
                    + "&q="
                    + encodedText;

            URL url = new URL(urlStr);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestProperty("User-Agent", "Mozilla/5.0");

            con.setConnectTimeout(8000);
            con.setReadTimeout(8000);

            StringBuilder sb = new StringBuilder();

            try (BufferedReader in =
                new BufferedReader(new InputStreamReader(con.getInputStream()))) {

              String line;

              while ((line = in.readLine()) != null) {
                sb.append(line);
              }
            }

            JSONArray main = new JSONArray(sb.toString());

            JSONArray parts = main.getJSONArray(0);

            StringBuilder result = new StringBuilder();

            for (int i = 0; i < parts.length(); i++) {

              Object chunk = parts.getJSONArray(i).get(0);

              if (chunk != null && !"null".equals(chunk.toString())) {

                result.append(chunk);
              }
            }

            String translatedText = result.toString();

            MAIN_HANDLER.post(() -> callback.onSuccess(translatedText));

          } catch (Exception e) {

            MAIN_HANDLER.post(
                () ->
                    callback.onFailure(e.getMessage() != null ? e.getMessage() : "Unknown error"));
          }
        });
  }
}
