package ir.hanzodev1375.ghostide.codeeditors.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.github.rosemoe.sora.lang.format.AsyncFormatter;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.TextRange;

public class CustomFormatter extends AsyncFormatter {

  public interface FormatAction {
    String format(String text);
  }

  private FormatAction action;

  public void setFormatAction(FormatAction action) {
    this.action = action;
  }

  @Override
  public TextRange formatAsync(Content text, TextRange cursorRange) {

    String formatted = action.format(text.toString());

    if (!formatted.equals(text.toString())) {

      int oldCursor = cursorRange.getStartIndex();

      text.delete(0, text.length());
      text.insert(0, 0, formatted);

      CharPosition pos = text.getIndexer().getCharPosition(Math.min(oldCursor, formatted.length()));

      return new TextRange(pos, pos);
    }

    return cursorRange;
  }

  @Override
  public TextRange formatRegionAsync(Content text, TextRange rangeToFormat, TextRange cursorRange) {
    return null;
  }
}
