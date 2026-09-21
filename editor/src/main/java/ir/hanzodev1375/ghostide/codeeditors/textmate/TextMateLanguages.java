package ir.hanzodev1375.ghostide.codeeditors.textmate;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage;
import ir.hanzodev1375.ghostide.codeeditors.langs.html.HtmlLanguage;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * جایگزین متناسب برای زبان های قدیمی توکنایزری.
 *
 * <p>Html جدا نگه داشته می شود چون گرامر TextMate آن ناقص است؛ بقیه زبان هایی که گرامرشان داخل
 * assets هست با TextMate ساخته می شوند و آن هایی که گرامر ندارند skip می شوند.
 */
public final class TextMateLanguages {

  private TextMateLanguages() {}

  public static boolean isHtml(String filePath) {
    if (filePath == null) {
      return false;
    }
    String ext = extensionOf(filePath);
    return ext.equals("html") || ext.equals("htm");
  }

  /**
   * نسخه ی همزمان که هیچ گاه ترد را بلاک نمی کند؛ اگر گرامرها آماده نباشند null برمی گرداند. برای
   * بارگذاری درست از {@link #resolveAsync} استفاده کنید.
   */
  public static Language resolve(Context context, String filePath) {
    if (context != null) {
      TextMateGrammars.ensureStarted(context);
    }
    if (isHtml(filePath)) {
      return new HtmlLanguage(context, filePath);
    }
    String scope = TextMateScopeMap.scopeOf(filePath);
    if (scope == null) {
      return null;
    }
    if (TextMateGrammars.getInitError() != null || !TextMateGrammars.isReady()) {
      return null;
    }
    if (!TextMateGrammars.hasScope(scope)) {
      return null;
    }
    return TextMateLanguage.create(scope, true);
  }

  /**
   * نسخه ی غیر بلاک کننده؛ اگر گرامرها آماده نباشند منتظر می ماند و کال بک را روی ترد اصلی صدا
   * می زند. اگر زبان پشتیبانی نشود کال بک با null فراخوانی می شود.
   */
  public static void resolveAsync(Context context, String filePath, Consumer<Language> callback) {
    if (context != null) {
      TextMateGrammars.ensureStarted(context);
    }
    Handler handler = new Handler(Looper.getMainLooper());
    if (isHtml(filePath)) {
      handler.post(() -> callback.accept(new HtmlLanguage(context, filePath)));
      return;
    }
    String scope = TextMateScopeMap.scopeOf(filePath);
    if (scope == null) {
      handler.post(() -> callback.accept(null));
      return;
    }
    TextMateGrammars.whenReady(
        () -> {
          Language language = null;
          if (TextMateGrammars.getInitError() == null && TextMateGrammars.hasScope(scope)) {
            language = TextMateLanguage.create(scope, true);
          }
          callback.accept(language);
        });
  }

  public static boolean isSupported(String filePath) {
    if (isHtml(filePath)) {
      return true;
    }
    String scope = TextMateScopeMap.scopeOf(filePath);
    return scope != null && TextMateGrammars.hasScope(scope);
  }

  private static String extensionOf(String filePath) {
    int slash = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
    String name = slash >= 0 ? filePath.substring(slash + 1) : filePath;
    int dot = name.lastIndexOf('.');
    if (dot < 0 || dot == name.length() - 1) {
      return "";
    }
    return name.substring(dot + 1).toLowerCase(Locale.ROOT);
  }
}
