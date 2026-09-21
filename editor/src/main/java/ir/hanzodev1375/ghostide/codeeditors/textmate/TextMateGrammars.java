package ir.hanzodev1375.ghostide.codeeditors.textmate;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/** بارگذاری یک باره ی گرامرها از assets بر اساس textmate/languages.json. */
public final class TextMateGrammars {

  public static final String LANGUAGES_JSON = "textmate/languages.json";

  private static final Object LOCK = new Object();
  private static final List<Runnable> PENDING = new ArrayList<>();
  private static volatile boolean started;
  private static volatile boolean ready;
  private static volatile Throwable initError;
  private static final Set<String> AVAILABLE_SCOPES = new HashSet<>();

  private TextMateGrammars() {}

  /** اجرای بارگذاری گرامرها در پس زمینه؛ چندباره امن است و ترد را بلاک نمی کند. */
  public static void ensureStarted(Context context) {
    synchronized (LOCK) {
      if (started) {
        return;
      }
      started = true;
      Context app = context.getApplicationContext();
      Thread thread =
          new Thread(
              () -> {
                try {
                  FileProviderRegistry.getInstance()
                      .addFileProvider(new AssetsFileResolver(app.getAssets()));
                  GrammarRegistry.getInstance().loadGrammars(LANGUAGES_JSON);
                  collectScopes(app, AVAILABLE_SCOPES);
                } catch (Throwable error) {
                  initError = error;
                } finally {
                  finish();
                }
              },
              "textmate-grammar-loader");
      thread.setDaemon(true);
      thread.start();
    }
  }

  /** اگر گرامرها آماده شده باشند هم اکنون (روی ترد اصلی) اجرا می شود وگرنه بعد از آماده شدن. */
  public static void whenReady(Runnable action) {
    Handler handler = new Handler(Looper.getMainLooper());
    boolean runNow;
    synchronized (LOCK) {
      runNow = ready;
      if (!runNow) {
        PENDING.add(() -> handler.post(action));
      }
    }
    if (runNow) {
      handler.post(action);
    }
  }

  private static void finish() {
    List<Runnable> actions;
    synchronized (LOCK) {
      ready = true;
      actions = new ArrayList<>(PENDING);
      PENDING.clear();
    }
    for (Runnable action : actions) {
      action.run();
    }
  }

  private static void collectScopes(Context context, Set<String> out) {
    InputStream stream = null;
    try {
      stream = context.getAssets().open(LANGUAGES_JSON);
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
      StringBuilder builder = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        builder.append(line);
      }
      JSONArray languages = new JSONObject(builder.toString()).getJSONArray("languages");
      for (int i = 0; i < languages.length(); i++) {
        String scope = languages.getJSONObject(i).optString("scopeName", null);
        if (scope != null) {
          out.add(scope);
        }
      }
    } catch (Exception ignored) {
      // پرونده ی گرامرها هنوز بار می شود؛ تنها scope ها ناقص می مانند
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (Exception ignored) {
        }
      }
    }
  }

  /** آیا گرامرها بارگذاری شده اند یا نه. */
  public static boolean isReady() {
    return ready;
  }

  /** خطای احتمالی بارگذاری گرامرها. */
  public static Throwable getInitError() {
    return initError;
  }

  /** آیا scope ی داده شده در languages.json موجود است یا نه. */
  public static boolean hasScope(String scopeName) {
    return AVAILABLE_SCOPES.contains(scopeName);
  }
}
