package ir.hanzodev1375.ghostide.codeeditors.langs.markdown;

import static io.github.rosemoe.sora.lang.styling.StylesUtils.checkNoCompletion;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.rosemoe.sora.lang.QuickQuoteHandler;
import io.github.rosemoe.sora.lang.styling.Styles;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.TextRange;

/**
 * Auto-pairs the characters that are commonly typed in pairs while writing Markdown: ` → `` (inline
 * code) * → ** (bold/italic) _ → __ (bold/italic) ~ → ~~ (strikethrough) " → "" (link title) [ → []
 * (link text) ( → () (link url — handled by SymbolPairMatch, kept here for clarity)
 */
public class MarkdownQuoteHandler implements QuickQuoteHandler {

  @NonNull
  @Override
  public HandleResult onHandleTyping(
      @NonNull String candidateCharacter,
      @NonNull Content text,
      @NonNull TextRange cursor,
      @Nullable Styles style) {

    // Only auto-pair when there is no active code/no-completion span
    if (checkNoCompletion(style, cursor.getStart()) || checkNoCompletion(style, cursor.getEnd())) {
      return HandleResult.NOT_CONSUMED;
    }

    // Only pair on the same line
    if (cursor.getStart().line != cursor.getEnd().line) {
      return HandleResult.NOT_CONSUMED;
    }

    String closing = getPair(candidateCharacter);
    if (closing == null) return HandleResult.NOT_CONSUMED;
    text.insert(cursor.getStart().line, cursor.getStart().column, candidateCharacter);
    text.insert(
        cursor.getEnd().line, cursor.getEnd().column + candidateCharacter.length(), closing);

    return new HandleResult(
        true,
        new TextRange(
            text.getIndexer().getCharPosition(cursor.getStartIndex() + candidateCharacter.length()),
            text.getIndexer().getCharPosition(cursor.getEndIndex() + candidateCharacter.length())));
  }

  /** Returns the closing counterpart, or null if we should not auto-pair. */
  private static String getPair(String ch) {
    switch (ch) {
      case "`":
        return "`";
      case "*":
        return "*";
      case "_":
        return "_";
      case "~":
        return "~";
      case "\"":
        return "\"";
      default:
        return null;
    }
  }
}
