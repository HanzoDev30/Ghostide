package ir.hanzodev1375.ghostide.codeeditors.snippets;

import androidx.annotation.NonNull;
import io.github.rosemoe.sora.lang.completion.CompletionHelper;
import io.github.rosemoe.sora.lang.completion.CompletionPublisher;
import io.github.rosemoe.sora.lang.completion.SimpleSnippetCompletionItem;
import io.github.rosemoe.sora.lang.completion.SnippetDescription;
import io.github.rosemoe.sora.lang.completion.snippet.CodeSnippet;
import io.github.rosemoe.sora.lang.completion.snippet.parser.CodeSnippetParser;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.ContentReference;
import io.github.rosemoe.sora.util.MyCharacter;
import ir.hanzodev1375.ghostide.codeeditors.IdeEditor;
import java.util.List;

/**
 * اسنیپت‌های کاربر را مثل نمونه زبان سورا (JavaLanguage) خودکار به پنجره autocomplete تزریق می‌کند.
 *
 * <p>برای هر زبان/فایلی که autocomplete اجرا می‌شود، اول scope زبانیِ فایل چک می‌شود (طبق فیلد
 * {@code scope} اسنیپت) و بعد پیشوند تایپ‌شده؛ اگر match بود آیتم اسنیپت به publisher اضافه می‌شود تا
 * بدون کلیکِ دستی داخل تکمیل‌کننده متن نمایش داده شود.
 */
public final class UserSnippetCompletion {

  private UserSnippetCompletion() {}

  public static void publish(
      @NonNull IdeEditor editor,
      @NonNull ContentReference content,
      @NonNull CharPosition position,
      @NonNull CompletionPublisher publisher) {
    UserSnippetProvider provider = editor.getUserSnippetProvider();
    if (provider == null) return;
    List<UserSnippet> snippets = provider.getSnippets();
    if (snippets == null || snippets.isEmpty()) return;

    String prefix = CompletionHelper.computePrefix(content, position, MyCharacter::isJavaIdentifierPart);
    if (prefix == null || prefix.isEmpty()) return;

    String filePath = editor.getCurrentFilePath();

    for (UserSnippet snippet : snippets) {
      if (snippet == null || !SnippetScopeUtil.matches(snippet.scope(), filePath)) {
        continue;
      }
      String trigger = snippet.prefix();
      if (trigger == null || trigger.trim().isEmpty()) {
        trigger = snippet.description();
      }
      if (trigger == null || trigger.trim().isEmpty()) {
        continue;
      }
      trigger = trigger.trim();
      if (!startsWithIgnoreCase(trigger, prefix)) {
        continue;
      }
      if (snippet.body() == null || snippet.body().isEmpty()) {
        continue;
      }
      CodeSnippet codeSnippet;
      try {
        codeSnippet = CodeSnippetParser.parse(snippet.body());
      } catch (Exception ignored) {
        continue;
      }
      String desc =
          snippet.description() != null && !snippet.description().trim().isEmpty()
              ? snippet.description().trim()
              : "Snippet";
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              trigger, desc, new SnippetDescription(prefix.length(), codeSnippet, true)));
    }
  }

  private static boolean startsWithIgnoreCase(String text, String prefix) {
    return text.regionMatches(true, 0, prefix, 0, prefix.length());
  }
}