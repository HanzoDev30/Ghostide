package ir.hanzodev1375.ghostide.snippets;

import android.content.Context;
import ir.hanzodev1375.ghostide.codeeditors.snippets.UserSnippet;
import ir.hanzodev1375.ghostide.codeeditors.snippets.UserSnippetProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * اسنیپت‌های ذخیره‌شده در {@code files/GhostIDE/snippets.json} را برای تزریق خودکار به autocomplete
 * در اختیار ادیتور می‌گذارد. کش بر اساس lastModified فایل، یعنی بعد از هر ذخیره‌ی اسنیپت خودبه‌خود
 * تازه می‌شود.
 */
public final class UserSnippetCompletionProvider implements UserSnippetProvider {

  private final Context appContext;
  private List<UserSnippet> cache;
  private long cacheLastModified = -1;

  public UserSnippetCompletionProvider(Context context) {
    this.appContext = context.getApplicationContext();
  }

  @Override
  public synchronized List<UserSnippet> getSnippets() {
    File file = UserSnippetStore.snippetsFile(appContext);
    long lastModified = file.lastModified();
    if (cache != null && lastModified == cacheLastModified) {
      return cache;
    }
    List<SnippetEntry> entries = UserSnippetStore.load(appContext);
    List<UserSnippet> out = new ArrayList<>();
    for (SnippetEntry entry : entries) {
      if (entry == null) continue;
      out.add(new UserSnippet(entry.prefix, entry.description, entry.body, entry.scope));
    }
    cache = out;
    cacheLastModified = lastModified;
    return cache;
  }
}