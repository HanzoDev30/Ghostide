package ir.hanzodev1375.ghostide.codeeditors.lspcustomhot;

import androidx.annotation.NonNull;
import io.github.rosemoe.sora.lang.completion.CompletionItem;
import io.github.rosemoe.sora.lang.completion.CompletionItemKind;
import io.github.rosemoe.sora.lang.completion.SimpleCompletionIconDrawer;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.hanzodev1375.ghostide.codeeditors.ui.CustomEditorAutoCompletion;

public class CustomCompletionItem extends CompletionItem {

  private final String insertText;
  private final int cursorOffset;
  private final String prefix;

  public CustomCompletionItem(CharSequence label, CharSequence desc) {
    this(label, desc, label.toString(), -1, "");
  }

  public CustomCompletionItem(
      CharSequence label, CharSequence desc, String insertText, int cursorOffset, String prefix) {
    super(label, desc);
    this.insertText = insertText != null ? insertText : label.toString();
    this.cursorOffset = cursorOffset;
    this.prefix = prefix != null ? prefix : "";
  }

  public CustomCompletionItem(
      CharSequence label,
      CharSequence detail,
      CharSequence desc,
      String insertText,
      int cursorOffset,
      String prefix) {
    super(label, desc);
    this.detail = detail;
    this.insertText = insertText != null ? insertText : label.toString();
    this.cursorOffset = cursorOffset;
    this.prefix = prefix != null ? prefix : "";
  }

  @Override
  public CustomCompletionItem kind(CompletionItemKind kind) {
    super.kind(kind);
    if (this.icon == null) {
      icon = SimpleCompletionIconDrawer.draw(kind);
    }
    return this;
  }

  @Override
  public void performCompletion(
      @NonNull CodeEditor editor, @NonNull Content text, int line, int column) {
    int start = column - prefix.length();
    if (start < 0) start = column;
    text.replace(line, start, line, column, insertText);
    if (cursorOffset >= 0) {
      editor.setSelection(line, start + cursorOffset);
    }
  }
}
