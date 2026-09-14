package ir.hanzodev1375.ghostide.translator.util;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import ir.hanzodev1375.ghostide.translator.model.AndroidLanguage;
import ir.hanzodev1375.ghostide.translator.model.StringEntry;
import ir.hanzodev1375.ghostide.translator.model.TranslationProgress;

public class StringsTranslator {

  private static final int MAX_CONCURRENT_REQUESTS = 6;
  private static final int MAX_RETRIES = 3;
  private static final long RETRY_BASE_DELAY_MS = 400;
  private int totalSteps = 0;
  private static final OkHttpClient HTTP_CLIENT =
      new OkHttpClient.Builder()
          .connectTimeout(15, TimeUnit.SECONDS)
          .readTimeout(20, TimeUnit.SECONDS)
          .writeTimeout(15, TimeUnit.SECONDS)
          .retryOnConnectionFailure(true)
          .build();

  public interface Callback {
    void onProgress(TranslationProgress progress);

    void onLanguageDone(String folder, int translatedCount, int skippedCount, int failedCount);

    void onComplete();

    void onError(String message);
  }

  public void translate(
      File sourceXml,
      String projectResDir,
      List<AndroidLanguage> languages,
      AtomicBoolean cancelled,
      Callback callback) {
    ExecutorService pool = Executors.newFixedThreadPool(MAX_CONCURRENT_REQUESTS);
    try {
      List<StringEntry> allEntries = StringsXmlParser.parse(sourceXml);
      List<StringEntry> translatableEntries = new ArrayList<>();
      List<StringEntry> nonTranslatableEntries = new ArrayList<>();
      for (StringEntry e : allEntries) {
        if (e.translatable) translatableEntries.add(e);
        else nonTranslatableEntries.add(e);
      }
      Map<AndroidLanguage, Map<String, String>> existingByLang = new HashMap<>();
      for (AndroidLanguage lang : languages) {
        Map<String, String> existing = loadExistingTranslations(projectResDir, lang);
        existingByLang.put(lang, existing);
        for (StringEntry entry : translatableEntries) {
          if (!existing.containsKey(entry.name)) totalSteps++;
        }
      }

      AtomicInteger step = new AtomicInteger(0);
      for (AndroidLanguage lang : languages) {
        if (cancelled.get()) return;
        Map<String, String> existing = existingByLang.get(lang);
        List<StringEntry> missing = new ArrayList<>();
        for (StringEntry entry : translatableEntries) {
          if (!existing.containsKey(entry.name)) missing.add(entry);
        }
        Map<String, String> freshlyTranslated = new ConcurrentHashMap<>();
        Map<String, Boolean> failed = new ConcurrentHashMap<>();
        List<Future<?>> futures = new ArrayList<>();
        for (StringEntry entry : missing) {
          futures.add(
              pool.submit(
                  () -> {
                    if (cancelled.get()) return;
                    try {
                      String translatedValue = translateWithRetry(entry.value, lang.googleCode);
                      freshlyTranslated.put(entry.name, translatedValue);
                    } catch (Exception ex) {
                      failed.put(entry.name, true);
                    }
                    int done = step.incrementAndGet();
                    callback.onProgress(
                        new TranslationProgress(done, totalSteps, entry.name, lang.androidFolder));
                  }));
        }
        for (Future<?> f : futures) {
          try {
            f.get();
          } catch (Exception ignored) {
          }
        }
        if (cancelled.get()) return;

        List<StringEntry> translated = new ArrayList<>(nonTranslatableEntries);
        int translatedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;
        for (StringEntry entry : translatableEntries) {
          String existingValue = existing.get(entry.name);
          if (existingValue != null) {
            translated.add(new StringEntry(entry.name, existingValue, true));
            skippedCount++;
          } else if (failed.containsKey(entry.name)) {
            // Leave it out of this run's output entirely. Since it still won't
            // exist in the written file, the next run will retry it automatically.
            failedCount++;
          } else {
            String value = freshlyTranslated.get(entry.name);
            translated.add(new StringEntry(entry.name, value, true));
            translatedCount++;
          }
        }
        File outFile = new File(projectResDir + "/" + lang.androidFolder + "/strings.xml");
        StringsXmlParser.write(outFile, translated);
        callback.onLanguageDone(lang.androidFolder, translatedCount, skippedCount, failedCount);
      }
      callback.onComplete();
    } catch (Exception e) {
      callback.onError(e.getMessage() != null ? e.getMessage() : "Unknown error");
    } finally {
      pool.shutdownNow();
    }
  }

  private Map<String, String> loadExistingTranslations(String projectResDir, AndroidLanguage lang) {
    Map<String, String> map = new HashMap<>();
    File target = new File(projectResDir + "/" + lang.androidFolder + "/strings.xml");
    if (!target.exists()) return map;
    try {
      List<StringEntry> entries = StringsXmlParser.parse(target);
      for (StringEntry e : entries) {
        if (e.value != null && !e.value.trim().isEmpty()) {
          map.put(e.name, e.value);
        }
      }
    } catch (Exception ignored) {
      // Corrupt/unparsable existing file -- treat as if nothing was translated yet.
    }
    return map;
  }

  private String translateWithRetry(String text, String targetLang) throws Exception {
    if (text == null || text.trim().isEmpty()) return text;
    Exception lastError = null;
    for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
      try {
        return translateText(text, targetLang);
      } catch (IOException e) {
        lastError = e;
        if (attempt < MAX_RETRIES - 1) {
          Thread.sleep(RETRY_BASE_DELAY_MS * (attempt + 1));
        }
      }
    }
    throw lastError;
  }

  private String translateText(String text, String targetLang) throws IOException {
    String encodedText = URLEncoder.encode(text, "UTF-8");
    String url =
        "https://translate.googleapis.com/translate_a/single"
            + "?client=gtx&sl=en&tl="
            + targetLang
            + "&dt=t&q="
            + encodedText;
    Request request = new Request.Builder().url(url).header("User-Agent", "Mozilla/5.0").build();
    Call call = HTTP_CLIENT.newCall(request);
    try (Response response = call.execute()) {
      if (!response.isSuccessful() || response.body() == null) {
        throw new IOException("HTTP " + response.code());
      }
      return parseTranslation(response.body().string());
    }
  }

  private String parseTranslation(String json) throws IOException {
    try {
      JSONArray main = new JSONArray(json);
      JSONArray parts = main.getJSONArray(0);
      StringBuilder result = new StringBuilder();
      for (int i = 0; i < parts.length(); i++) {
        Object chunk = parts.getJSONArray(i).get(0);
        if (chunk != null && !"null".equals(chunk.toString())) result.append(chunk);
      }
      return result.toString();
    } catch (Exception e) {
      throw new IOException("Bad translation response: " + e.getMessage());
    }
  }
}
