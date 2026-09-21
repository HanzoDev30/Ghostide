package ir.hanzodev1375.ghostide.editorlangs;

import android.content.Context;
import io.github.rosemoe.sora.lang.Language;
import ir.hanzodev1375.ghostide.codeeditors.textmate.TextMateLanguages;
import java.util.function.Consumer;

public final class LanguageManager {

  private LanguageManager() {}

  public static Language resolve(Context context, String filePath) {
    if (filePath == null) {
      return null;
    }
    return TextMateLanguages.resolve(context, filePath);
  }

  /**
   * نسخه ی غیر بلاک کننده؛ کال بک روی ترد اصلی اجرا می شود و اگر زبان پشتیبانی نشود null می گیرد.
   */
  public static void resolveAsync(Context context, String filePath, Consumer<Language> callback) {
    if (filePath == null) {
      callback.accept(null);
      return;
    }
    TextMateLanguages.resolveAsync(context, filePath, callback);
  }

  public static boolean isSupported(String filePath) {
    return TextMateLanguages.isSupported(filePath);
  }
}
