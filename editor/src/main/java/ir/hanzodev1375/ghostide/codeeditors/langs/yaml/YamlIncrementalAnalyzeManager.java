package ir.hanzodev1375.ghostide.codeeditors.langs.yaml;

import android.os.Bundle;

import androidx.annotation.NonNull;

import io.github.rosemoe.sora.lang.analysis.AsyncIncrementalAnalyzeManager;
import io.github.rosemoe.sora.lang.styling.CodeBlock;
import io.github.rosemoe.sora.lang.styling.Span;
import io.github.rosemoe.sora.lang.styling.SpanFactory;
import io.github.rosemoe.sora.lang.completion.IdentifierAutoComplete;
import io.github.rosemoe.sora.lang.styling.TextStyle;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.ContentReference;

import ir.hanzodev1375.ghostide.codeeditors.colorscheme.GhostColorScheme;

import java.util.ArrayList;
import java.util.List;

public class YamlIncrementalAnalyzeManager
    extends AsyncIncrementalAnalyzeManager<State, YamlIncrementalAnalyzeManager.HighlightToken> {

  protected IdentifierAutoComplete.SyncIdentifiers identifiers =
      new IdentifierAutoComplete.SyncIdentifiers();

  private final ThreadLocal<YamlTextTokenizer> tokenizerProvider = new ThreadLocal<>();

  private YamlTextTokenizer obtainTokenizer() {
    var res = tokenizerProvider.get();
    if (res == null) {
      res = new YamlTextTokenizer("");
      tokenizerProvider.set(res);
    }
    return res;
  }

  @Override
  public List<CodeBlock> computeBlocks(Content text, CodeBlockAnalyzeDelegate delegate) {
    var blocks = new ArrayList<CodeBlock>();
    var openStack = new ArrayList<int[]>();

    for (int i = 0; i < text.getLineCount() && delegate.isNotCancelled(); i++) {
      CharSequence line = text.getLine(i);
      int indent = countLeadingSpaces(line);
      boolean hasContent =
          indent < line.length() && line.charAt(indent) != '\n' && line.charAt(indent) != '\r';
      if (!hasContent) continue;

      while (!openStack.isEmpty()) {
        int[] top = openStack.get(openStack.size() - 1);
        if (top[1] >= indent) {
          openStack.remove(openStack.size() - 1);
          CodeBlock block = new CodeBlock();
          block.startLine = top[0];
          block.startColumn = top[1];
          block.endLine = i - 1;
          block.endColumn = 0;
          if (block.startLine != block.endLine) blocks.add(block);
        } else break;
      }

      String lineStr = line.toString().stripTrailing();
      if (lineStr.endsWith(":") || lineStr.contains(": ")) {
        openStack.add(new int[] {i, indent});
      }
    }
    return blocks;
  }

  private static int countLeadingSpaces(CharSequence line) {
    int i = 0;
    while (i < line.length() && (line.charAt(i) == ' ' || line.charAt(i) == '\t')) i++;
    return i;
  }

  @NonNull
  @Override
  public State getInitialState() {
    return new State();
  }

  @Override
  public boolean stateEquals(@NonNull State a, @NonNull State b) {
    return a.equals(b);
  }

  @Override
  public void onAddState(State state) {
    if (state.identifiers != null)
      for (String id : state.identifiers) identifiers.identifierIncrease(id);
  }

  @Override
  public void onAbandonState(State state) {
    if (state.identifiers != null)
      for (String id : state.identifiers) identifiers.identifierDecrease(id);
  }

  @Override
  public void reset(@NonNull ContentReference content, @NonNull Bundle extraArguments) {
    super.reset(content, extraArguments);
    identifiers.clear();
  }

  @Override
  public LineTokenizeResult<State, HighlightToken> tokenizeLine(
      CharSequence line, State state, int lineIndex) {

    var tokens = new ArrayList<HighlightToken>();
    var newState = new State();

    if (state.state == State.STATE_BLOCK_LITERAL || state.state == State.STATE_BLOCK_FOLDED) {

      int indent = countLeadingSpaces(line);
      boolean isBlank = isBlankLine(line);

      if (isBlank || state.blockIndent < 0 || indent >= state.blockIndent) {
        tokens.add(new HighlightToken(Tokens.BLOCK_SCALAR_CONTENT, 0));
        newState.state = state.state;
        newState.blockIndent = state.blockIndent < 0 ? indent : state.blockIndent;
        return new LineTokenizeResult<>(newState, tokens);
      }

      newState.state = State.STATE_NORMAL;
    }

    var tokenizer = obtainTokenizer();
    tokenizer.reset(line);

    Tokens tok;
    while ((tok = tokenizer.nextToken()) != Tokens.EOF) {
      int col = tokenizer.getOffset();

      if (tok == Tokens.KEY || tok == Tokens.SCALAR_VALUE) {
        CharSequence text = tokenizer.getTokenText();
        newState.addIdentifier(text);
        tok = Tokens.IDENTIFIER;
      }

      tokens.add(new HighlightToken(tok, col));

      if (tok == Tokens.BLOCK_LITERAL_HEADER) {
        newState.state = State.STATE_BLOCK_LITERAL;
        newState.blockIndent = -1;
        break;
      }
      if (tok == Tokens.BLOCK_FOLDED_HEADER) {
        newState.state = State.STATE_BLOCK_FOLDED;
        newState.blockIndent = -1;
        break;
      }
    }

    if (tokens.isEmpty()) tokens.add(new HighlightToken(Tokens.UNKNOWN, 0));
    return new LineTokenizeResult<>(newState, tokens);
  }

  private static boolean isBlankLine(CharSequence line) {
    for (int i = 0; i < line.length(); i++) {
      char ch = line.charAt(i);
      if (ch != ' ' && ch != '\t' && ch != '\n' && ch != '\r') return false;
    }
    return true;
  }

  @Override
  public List<Span> generateSpansForLine(LineTokenizeResult<State, HighlightToken> lineResult) {

    var spans = new ArrayList<Span>();
    var tokens = lineResult.tokens;
    Tokens previous = Tokens.UNKNOWN;

    for (var tr : tokens) {
      int col = tr.offset;
      Tokens token = tr.token;
      Span span;

      switch (token) {
        case WHITESPACE:
        case NEWLINE:
          span = SpanFactory.obtain(col, TextStyle.makeStyle(GhostColorScheme.TEXT_NORMAL));
          spans.add(span);
          continue;

        case LINE_COMMENT:
          span =
              SpanFactory.obtain(
                  col, TextStyle.makeStyle(GhostColorScheme.COMMENT, 0, false, true, false, true));
          break;

        case DOC_START:
        case DOC_END:
          span =
              SpanFactory.obtain(
                  col, TextStyle.makeStyle(GhostColorScheme.KEYWORD, 0, true, false, false));
          break;

        case IDENTIFIER:
          if (previous == Tokens.COLON
              || previous == Tokens.LBRACE
              || previous == Tokens.LBRACK
              || previous == Tokens.COMMA
              || previous == Tokens.COLON_SPACE) {
            span =
                SpanFactory.obtain(
                    col, TextStyle.makeStyle(GhostColorScheme.ATTRIBUTE_VALUE, 0, true, false, false));
          } else {
            span = SpanFactory.obtain(col, TextStyle.makeStyle(GhostColorScheme.ATTRIBUTE_NAME));
          }
          break;

        case KEY:
          span = SpanFactory.obtain(col, TextStyle.makeStyle(GhostColorScheme.ATTRIBUTE_NAME));
          break;

        case COLON:
          span = SpanFactory.obtain(col, TextStyle.makeStyle(GhostColorScheme.OPERATOR));
          break;

        case STRING_LITERAL:
          span = SpanFactory.obtain(col, TextStyle.makeStyle(GhostColorScheme.LITERAL, true));
          break;

        case SCALAR_VALUE:
          span = SpanFactory.obtain(col, TextStyle.makeStyle(GhostColorScheme.TEXT_NORMAL));
          break;

        case INTEGER_LITERAL:
        case FLOATING_POINT_LITERAL:
          span = SpanFactory.obtain(col, TextStyle.makeStyle(GhostColorScheme.LITERAL, true));
          break;

        case BOOLEAN_LITERAL:
          span =
              SpanFactory.obtain(
                  col, TextStyle.makeStyle(GhostColorScheme.KEYWORD, 0, true, false, false));
          break;

        case NULL_LITERAL:
          span =
              SpanFactory.obtain(
                  col, TextStyle.makeStyle(GhostColorScheme.KEYWORD, 0, false, true, false));
          break;

        case BLOCK_LITERAL_HEADER:
        case BLOCK_FOLDED_HEADER:
          span = SpanFactory.obtain(col, TextStyle.makeStyle(GhostColorScheme.OPERATOR));
          break;

        case BLOCK_SCALAR_CONTENT:
          span = SpanFactory.obtain(col, TextStyle.makeStyle(GhostColorScheme.LITERAL, true));
          break;

        case LIST_MARKER:
          span =
              SpanFactory.obtain(
                  col, TextStyle.makeStyle(GhostColorScheme.KEYWORD, 0, true, false, false));
          break;

        case LBRACE:
        case RBRACE:
        case LBRACK:
        case RBRACK:
        case COMMA:
          span = SpanFactory.obtain(col, GhostColorScheme.OPERATOR);
          break;

        case ANCHOR:
          span =
              SpanFactory.obtain(
                  col, TextStyle.makeStyle(GhostColorScheme.ANNOTATION, 0, true, false, false));
          break;

        case ALIAS:
          span = SpanFactory.obtain(col, TextStyle.makeStyle(GhostColorScheme.ANNOTATION));
          break;

        case EXPRESSION:
          span =
              SpanFactory.obtain(
                  col, TextStyle.makeStyle(GhostColorScheme.ANNOTATION, 0, true, false, false));
          break;

        case TAG:
          span =
              SpanFactory.obtain(
                  col, TextStyle.makeStyle(GhostColorScheme.COLORUPPERCASE, 0, false, true, false));
          break;

        case DIRECTIVE:
          span =
              SpanFactory.obtain(
                  col, TextStyle.makeStyle(GhostColorScheme.KEYWORD, 0, true, false, false));
          break;

        default:
          span = SpanFactory.obtain(col, TextStyle.makeStyle(GhostColorScheme.TEXT_NORMAL));
          break;
      }

      previous = token;
      spans.add(span);
    }
    return spans;
  }

  public static class HighlightToken {
    public final Tokens token;
    public final int offset;

    public HighlightToken(Tokens token, int offset) {
      this.token = token;
      this.offset = offset;
    }
  }
}
