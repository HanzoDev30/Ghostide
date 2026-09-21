package ir.hanzodev1375.ghostide.codeeditors.langs.lsp;

import android.content.Context;
import io.github.rosemoe.sora.lang.EmptyLanguage;
import io.github.rosemoe.sora.lang.Language;
import ir.hanzodev1375.ghostide.codeeditors.textmate.TextMateLanguages;
import java.util.function.Consumer;

public final class EditorLanguageFactory {

  private EditorLanguageFactory() {}

  public static Language create(Context context, String filePath) {
    try {
      Language language = TextMateLanguages.resolve(context, filePath);
      if (language != null) {
        return language;
      }
    } catch (RuntimeException ignored) {
    }
    return new EmptyLanguage();
  }

  /**
   * نسخه ی غیر بلاک کننده؛ اگر گرامرها آماده نباشند منتظر می ماند و کال بک را روی ترد اصلی با زبان
   * آماده (یا EmptyLanguage) صدا می زند.
   */
  public static void createAsync(
      Context context, String filePath, Consumer<Language> callback) {
    TextMateLanguages.resolveAsync(
        context, filePath, language -> callback.accept(language != null ? language : new EmptyLanguage()));
  }
}
