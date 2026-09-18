package ir.hanzodev1375.ghostide.codeeditors.snippets;

import androidx.annotation.Nullable;
import java.util.List;

/** اسنیپت‌های کاربر را برای تزریق خودکار به autocomplete آماده می‌کند. */
public interface UserSnippetProvider {

  @Nullable
  List<UserSnippet> getSnippets();
}