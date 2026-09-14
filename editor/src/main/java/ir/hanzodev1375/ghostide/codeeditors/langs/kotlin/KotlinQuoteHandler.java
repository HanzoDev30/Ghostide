/**
 * Comment by ghost ide
 *
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.kotlin;

import static io.github.rosemoe.sora.lang.styling.StylesUtils.checkNoCompletion;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.github.rosemoe.sora.lang.QuickQuoteHandler;
import io.github.rosemoe.sora.lang.styling.Styles;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.TextRange;

public class KotlinQuoteHandler implements QuickQuoteHandler {

  @NonNull
  @Override
  public HandleResult onHandleTyping(
      @NonNull String candidateCharacter,
      @NonNull Content text,
      @NonNull TextRange cursor,
      @Nullable Styles style) {
    // For double quotes, handle as Java but also handle triple quotes?
    if (!checkNoCompletion(style, cursor.getStart())
        && !checkNoCompletion(style, cursor.getEnd())
        && "\"".equals(candidateCharacter)
        && cursor.getStart().line == cursor.getEnd().line) {
      // Check if we are inside a string already? But we just handle auto-pairing
      text.insert(cursor.getStart().line, cursor.getStart().column, "\"");
      text.insert(cursor.getEnd().line, cursor.getEnd().column + 1, "\"");
      return new HandleResult(
          true,
          new TextRange(
              text.getIndexer().getCharPosition(cursor.getStartIndex() + 1),
              text.getIndexer().getCharPosition(cursor.getEndIndex() + 1)));
    }
    return HandleResult.NOT_CONSUMED;
  }
}
