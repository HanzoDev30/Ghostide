package ir.hanzodev1375.ghostide.codeeditors.langs.toml;

import android.os.Bundle;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.lang.analysis.AsyncIncrementalAnalyzeManager;
import io.github.rosemoe.sora.lang.styling.CodeBlock;
import io.github.rosemoe.sora.lang.styling.Span;
import io.github.rosemoe.sora.lang.styling.SpanFactory;
import io.github.rosemoe.sora.lang.styling.TextStyle;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.ContentReference;

import ir.hanzodev1375.ghostide.codeeditors.colorscheme.GhostColorScheme;

import java.util.ArrayList;
import java.util.List;

public class TomlIncrementalAnalyzeManager
    extends AsyncIncrementalAnalyzeManager<State, TomlIncrementalAnalyzeManager.HighlightToken> {

  private final ThreadLocal<TomlTextTokenizer> tokenizerProvider = new ThreadLocal<>();

  private synchronized TomlTextTokenizer obtainTokenizer() {
    var res = tokenizerProvider.get();
    if (res == null) {
      res = new TomlTextTokenizer("");
      tokenizerProvider.set(res);
    }
    return res;
  }

  @Override
  public List<CodeBlock> computeBlocks(Content text, CodeBlockAnalyzeDelegate delegate) {
    // TOML doesn't have meaningful code blocks (no braces for indentation)
    return new ArrayList<>();
  }

  @NonNull
  @Override
  public State getInitialState() {
    return new State();
  }

  @Override
  public boolean stateEquals(@NonNull State state, @NonNull State another) {
    return state.equals(another);
  }

  @Override
  public void onAddState(State state) {}

  @Override
  public void onAbandonState(State state) {}

  @Override
  public void reset(@NonNull ContentReference content, @NonNull Bundle extraArguments) {
    super.reset(content, extraArguments);
  }

  @Override
  public LineTokenizeResult<State, HighlightToken> tokenizeLine(
      CharSequence line, State state, int lineIndex) {
    var tokens = new ArrayList<HighlightToken>();
    var stateObj = new State();
    var tokenizer = obtainTokenizer();
    tokenizer.reset(line);
    Tokens token;
    boolean seenEquals = false;
    while ((token = tokenizer.nextToken()) != Tokens.EOF) {
      if (token == Tokens.EQ) seenEquals = true;
      tokens.add(new HighlightToken(token, tokenizer.offset, seenEquals));
    }
    if (tokens.isEmpty()) tokens.add(new HighlightToken(Tokens.UNKNOWN, 0, false));
    return new LineTokenizeResult<>(stateObj, tokens);
  }

  @Override
  public List<Span> generateSpansForLine(LineTokenizeResult<State, HighlightToken> lineResult) {
    var spans = new ArrayList<Span>();
    for (var tokenRecord : lineResult.tokens) {
      var token = tokenRecord.token;
      int offset = tokenRecord.offset;
      boolean afterEquals = tokenRecord.afterEquals;
      Span span;
      switch (token) {
        case WHITESPACE:
        case NEWLINE:
          span = SpanFactory.obtain(offset, TextStyle.makeStyle(GhostColorScheme.TEXT_NORMAL));
          break;
        case LINE_COMMENT:
          span =
              SpanFactory.obtain(
                  offset,
                  TextStyle.makeStyle(GhostColorScheme.COMMENT, 0, false, true, false, true));
          break;
        case TABLE_HEADER:
        case ARRAY_TABLE_HEADER:
          // Sections like [section] are highlighted as class/type names
          span =
              SpanFactory.obtain(
                  offset,
                  TextStyle.makeStyle(GhostColorScheme.COLORUPPERCASE, 0, true, false, false));
          break;
        case STRING_LITERAL:
          span = SpanFactory.obtain(offset, TextStyle.makeStyle(GhostColorScheme.LITERAL, true));
          break;
        case INTEGER_LITERAL:
        case FLOATING_POINT_LITERAL:
        case DATETIME_LITERAL:
          span = SpanFactory.obtain(offset, TextStyle.makeStyle(GhostColorScheme.LITERAL, true));
          break;
        case TRUE:
        case FALSE:
        case BOOLEAN_LITERAL:
          span =
              SpanFactory.obtain(
                  offset, TextStyle.makeStyle(GhostColorScheme.KEYWORD, 0, true, false, false));
          break;
        case KEY:
          if (!afterEquals) {
            // Keys (left of =) highlighted as attribute names
            span = SpanFactory.obtain(offset, TextStyle.makeStyle(GhostColorScheme.ATTRIBUTE_NAME));
          } else {
            span = SpanFactory.obtain(offset, TextStyle.makeStyle(GhostColorScheme.TEXT_NORMAL));
          }
          break;
        case EQ:
          span = SpanFactory.obtain(offset, GhostColorScheme.OPERATOR);
          break;
        case LBRACK:
        case RBRACK:
        case LBRACE:
        case RBRACE:
        case COMMA:
        case DOT:
          span = SpanFactory.obtain(offset, GhostColorScheme.OPERATOR);
          break;
        case GROUP:
        case VERSION:
        case REF:
        case MODULE:
        case NAME:
          span = SpanFactory.obtain(offset, GhostColorScheme.FUNCTION_NAME);
          break;
        default:
          span = SpanFactory.obtain(offset, TextStyle.makeStyle(GhostColorScheme.TEXT_NORMAL));
      }
      spans.add(span);
    }
    return spans;
  }

  public static class HighlightToken {
    public Tokens token;
    public int offset;
    public boolean afterEquals;

    public HighlightToken(Tokens token, int offset, boolean afterEquals) {
      this.token = token;
      this.offset = offset;
      this.afterEquals = afterEquals;
    }
  }
}
