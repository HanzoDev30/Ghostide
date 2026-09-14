package ir.hanzodev1375.ghostide.codeeditors.langs.markdown;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.core.graphics.ColorUtils;
import io.github.rosemoe.sora.lang.analysis.AsyncIncrementalAnalyzeManager;
import io.github.rosemoe.sora.lang.styling.CodeBlock;
import io.github.rosemoe.sora.lang.styling.Span;
import io.github.rosemoe.sora.lang.styling.SpanFactory;
import io.github.rosemoe.sora.lang.styling.TextStyle;
import io.github.rosemoe.sora.lang.styling.color.ConstColor;
import io.github.rosemoe.sora.lang.styling.color.EditorColor;
import io.github.rosemoe.sora.lang.styling.span.SpanClickableUrl;
import io.github.rosemoe.sora.lang.styling.span.SpanExtAttrs;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.ContentReference;

import ir.hanzodev1375.ghostide.codeeditors.colorscheme.GhostColorScheme;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownIncrementalAnalyzeManager
    extends AsyncIncrementalAnalyzeManager<
        State, MarkdownIncrementalAnalyzeManager.HighlightToken> {

  private static final Pattern URL_PATTERN =
      Pattern.compile(
          "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}"
              + "\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)");

  private final ThreadLocal<MarkdownTextTokenizer> tokenizerProvider = new ThreadLocal<>();

  private MarkdownTextTokenizer obtainTokenizer() {
    var res = tokenizerProvider.get();
    if (res == null) {
      res = new MarkdownTextTokenizer("");
      tokenizerProvider.set(res);
    }
    return res;
  }

  @Override
  public List<CodeBlock> computeBlocks(Content text, CodeBlockAnalyzeDelegate delegate) {
    var blocks = new ArrayList<CodeBlock>();
    CodeBlock open = null;

    for (int i = 0; i < text.getLineCount() && delegate.isNotCancelled(); i++) {
      var lineState = getState(i);

      boolean hasFenceOpen = hasToken(lineState.tokens, Tokens.CODE_FENCE_OPEN);
      boolean hasFenceClose = hasToken(lineState.tokens, Tokens.CODE_FENCE_CLOSE);

      if (hasFenceOpen && open == null) {
        open = new CodeBlock();
        open.startLine = i;
        open.startColumn = 0;
      } else if (hasFenceClose && open != null) {
        open.endLine = i;
        open.endColumn = 0;
        if (open.startLine != open.endLine) blocks.add(open);
        open = null;
      }
    }
    return blocks;
  }

  private static boolean hasToken(List<HighlightToken> tokens, Tokens target) {
    for (var t : tokens) if (t.token == target) return true;
    return false;
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
    var newState = new State();

    if (state.state == State.STATE_CODE_FENCE) {
      newState.fenceChar = state.fenceChar;
      newState.fenceLen = state.fenceLen;

      if (isClosingFence(line, state.fenceChar, state.fenceLen)) {
        tokens.add(new HighlightToken(Tokens.CODE_FENCE_CLOSE, 0));
        newState.state = State.STATE_NORMAL;
      } else {
        tokens.add(new HighlightToken(Tokens.CODE_FENCE_CONTENT, 0));
        newState.state = State.STATE_CODE_FENCE;
      }

      if (tokens.isEmpty()) tokens.add(new HighlightToken(Tokens.UNKNOWN, 0));
      return new LineTokenizeResult<>(newState, tokens);
    }

    if (isBlankLine(line)) {
      tokens.add(new HighlightToken(Tokens.BLANK_LINE, 0));
      newState.state = State.STATE_NORMAL;
      return new LineTokenizeResult<>(newState, tokens);
    }

    var tokenizer = obtainTokenizer();
    tokenizer.reset(line);

    Tokens tok;
    while ((tok = tokenizer.nextToken()) != Tokens.EOF) {

      if (tok == Tokens.CODE_FENCE_OPEN) {
        char fc = firstFenceChar(line);
        int fl = fenceRunLength(line, fc);
        tokens.add(new HighlightToken(Tokens.CODE_FENCE_OPEN, tokenizer.getOffset()));
        newState.state = State.STATE_CODE_FENCE;
        newState.fenceChar = fc;
        newState.fenceLen = fl;

        break;
      }

      if (tok == Tokens.TEXT || tok == Tokens.AUTO_LINK) {
        detectUrls(tokenizer.getTokenText(), tokenizer.getOffset(), tok, tokens);
        continue;
      }

      tokens.add(new HighlightToken(tok, tokenizer.getOffset()));
    }

    if (tokens.isEmpty()) tokens.add(new HighlightToken(Tokens.UNKNOWN, 0));

    if (newState.state != State.STATE_CODE_FENCE) {
      newState.state = State.STATE_NORMAL;
    }
    return new LineTokenizeResult<>(newState, tokens);
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
        case HEADING_H1:
          span =
              SpanFactory.obtain(
                  col, TextStyle.makeStyle(GhostColorScheme.KEYWORD, 0, true, false, false));
          break;
        case HEADING_H2:
          span =
              SpanFactory.obtain(
                  col, TextStyle.makeStyle(GhostColorScheme.KEYWORD, 0, true, false, false));
          break;
        case HEADING_H3:
          span =
              SpanFactory.obtain(
                  col, TextStyle.makeStyle(GhostColorScheme.COLORUPPERCASE, 0, true, false, false));
          break;
        case HEADING_H4:
        case HEADING_H5:
        case HEADING_H6:
          span =
              SpanFactory.obtain(
                  col,
                  TextStyle.makeStyle(GhostColorScheme.COLORUPPERCASE, 0, false, false, false));
          break;

        case WHITESPACE:
        case NEWLINE:
        case BLANK_LINE:
          span = SpanFactory.obtain(col, TextStyle.makeStyle(GhostColorScheme.TEXT_NORMAL));

          spans.add(span);
          continue;

        case TEXT:
          span = buildTextSpan(col, previous);
          break;

        case CODE_FENCE_OPEN:
        case CODE_FENCE_CLOSE:
          span =
              SpanFactory.obtain(
                  col, TextStyle.makeStyle(GhostColorScheme.COMMENT, 0, false, true, false, true));
          break;
        case CODE_FENCE_CONTENT:
        case INDENTED_CODE:
        case INLINE_CODE:
          span = SpanFactory.obtain(col, TextStyle.makeStyle(GhostColorScheme.LITERAL, true));
          break;

        case BOLD:
          span =
              SpanFactory.obtain(
                  col, TextStyle.makeStyle(GhostColorScheme.TEXT_NORMAL, 0, true, false, false));
          break;
        case ITALIC:
          span =
              SpanFactory.obtain(
                  col, TextStyle.makeStyle(GhostColorScheme.TEXT_NORMAL, 0, false, true, false));
          break;
        case BOLD_ITALIC:
          span =
              SpanFactory.obtain(
                  col, TextStyle.makeStyle(GhostColorScheme.TEXT_NORMAL, 0, true, true, false));
          break;
        case STRIKETHROUGH:
          span =
              SpanFactory.obtain(
                  col, TextStyle.makeStyle(GhostColorScheme.COMMENT, 0, false, false, true, false));
          break;

        case LINK_TEXT:
        case IMAGE_BANG:
          span = SpanFactory.obtain(col, TextStyle.makeStyle(GhostColorScheme.FUNCTION_NAME));
          break;
        case LINK_URL:
        case LINK_TITLE:
        case AUTO_LINK:
          span =
              SpanFactory.obtain(
                  col,
                  TextStyle.makeStyle(
                      GhostColorScheme.ATTRIBUTE_NAME, 0, false, false, true, false));
          break;

        case BLOCKQUOTE_MARKER:
        case BLOCKQUOTE_CONTENT:
          span =
              SpanFactory.obtain(
                  col,
                  TextStyle.makeStyle(
                      GhostColorScheme.COMMENT,
                      ColorUtils.setAlphaComponent(GhostColorScheme.ATTRIBUTE_VALUE, 120),
                      false,
                      true,
                      false,
                      true));
          break;
        case THEMATIC_BREAK:
          span = SpanFactory.obtain(col, TextStyle.makeStyle(GhostColorScheme.OPERATOR));
          break;
        case UNORDERED_LIST_MARKER:
        case ORDERED_LIST_MARKER:
          span =
              SpanFactory.obtain(
                  col, TextStyle.makeStyle(GhostColorScheme.KEYWORD, 0, true, false, false));
          break;

        case TABLE_PIPE:
        case TABLE_DELIMITER_ROW:
          span = SpanFactory.obtain(col, TextStyle.makeStyle(GhostColorScheme.OPERATOR));
          break;
        case TABLE_CELL:
          span = SpanFactory.obtain(col, TextStyle.makeStyle(GhostColorScheme.TEXT_NORMAL));
          break;

        case HTML_INLINE:
        case HTML_BLOCK:
          span = SpanFactory.obtain(col, TextStyle.makeStyle(GhostColorScheme.ANNOTATION));
          break;

        case ESCAPE:
          span = SpanFactory.obtain(col, TextStyle.makeStyle(GhostColorScheme.LITERAL, true));
          break;

        default:
          span = SpanFactory.obtain(col, TextStyle.makeStyle(GhostColorScheme.TEXT_NORMAL));
          break;
      }

      previous = token;

      if (tr.url != null) {
        span = SpanFactory.obtain(span.getColumn(), span.getStyle());
        span.setSpanExt(SpanExtAttrs.EXT_INTERACTION_INFO, new SpanClickableUrl(tr.url));
        span.setUnderlineColor(new EditorColor(span.getForegroundColorId()));
      }

      spans.add(span);
    }
    return spans;
  }

  /**
   * رنگ توکن TEXT را بر اساس previousToken تعیین می‌کند.
   *
   * <p>مثال‌ها:
   *
   * <pre>
   *  # hello      → HEADING_H1 · WHITESPACE · TEXT("hello")   → TEXT رنگ H1 می‌گیره
   *  ## world     → HEADING_H2 · WHITESPACE · TEXT("world")   → TEXT رنگ H2 می‌گیره
   *  > quote      → BLOCKQUOTE_MARKER · WHITESPACE · TEXT     → TEXT رنگ blockquote می‌گیره
   *  - item       → UNORDERED_LIST_MARKER · WHITESPACE · TEXT → TEXT معمولی
   * </pre>
   */
  private Span buildTextSpan(int col, Tokens previous) {
    switch (previous) {
      case HEADING_H1:
      case HEADING_H2:
        return SpanFactory.obtain(
            col, TextStyle.makeStyle(GhostColorScheme.KEYWORD, 0, true, false, false));

      case HEADING_H3:
        return SpanFactory.obtain(
            col, TextStyle.makeStyle(GhostColorScheme.COLORUPPERCASE, 0, true, false, false));

      case HEADING_H4:
      case HEADING_H5:
      case HEADING_H6:
        return SpanFactory.obtain(
            col, TextStyle.makeStyle(GhostColorScheme.COLORUPPERCASE, 0, false, false, false));

      case BLOCKQUOTE_MARKER:
      case BLOCKQUOTE_CONTENT:
        return SpanFactory.obtain(
            col, TextStyle.makeStyle(GhostColorScheme.COMMENT, 0, false, true, false, true));

      case LINK_TEXT:
        return SpanFactory.obtain(col, TextStyle.makeStyle(GhostColorScheme.FUNCTION_NAME));

      case UNORDERED_LIST_MARKER:
      case ORDERED_LIST_MARKER:
        return SpanFactory.obtain(
            col, TextStyle.makeStyle(GhostColorScheme.ATTRIBUTE_NAME, 0, true, false, false));

      default:
        return SpanFactory.obtain(col, TextStyle.makeStyle(GhostColorScheme.TEXT_NORMAL));
    }
  }

  private void detectUrls(CharSequence text, int offset, Tokens base, List<HighlightToken> out) {
    Matcher m = URL_PATTERN.matcher(text);
    int idx = 0;
    while (idx < text.length() && m.find(idx)) {
      if (m.start() > idx) out.add(new HighlightToken(base, offset + idx));
      out.add(new HighlightToken(base, offset + m.start(), m.group()));
      idx = m.end();
    }
    if (idx < text.length()) out.add(new HighlightToken(base, offset + idx));
  }

  private static boolean isClosingFence(CharSequence line, char fc, int minLen) {
    int i = 0;

    while (i < line.length() && i < 3 && line.charAt(i) == ' ') i++;
    int run = 0;
    while (i < line.length() && line.charAt(i) == fc) {
      run++;
      i++;
    }
    if (run < minLen) return false;

    while (i < line.length()) {
      char ch = line.charAt(i);
      if (ch != ' ' && ch != '\t' && ch != '\r' && ch != '\n') return false;
      i++;
    }
    return true;
  }

  private static char firstFenceChar(CharSequence line) {
    for (int i = 0; i < line.length(); i++) {
      char ch = line.charAt(i);
      if (ch == '`' || ch == '~') return ch;
    }
    return '`';
  }

  private static int fenceRunLength(CharSequence line, char fc) {
    int run = 0;
    for (int i = 0; i < line.length(); i++) {
      if (line.charAt(i) == fc) run++;
      else if (line.charAt(i) != ' ' && line.charAt(i) != '\t') break;
    }
    return Math.max(run, 3);
  }

  private static boolean isBlankLine(CharSequence line) {
    for (int i = 0; i < line.length(); i++) {
      char ch = line.charAt(i);
      if (ch != ' ' && ch != '\t' && ch != '\r' && ch != '\n') return false;
    }
    return true;
  }

  public static class HighlightToken {
    public final Tokens token;
    public final int offset;
    public final String url;

    public HighlightToken(Tokens token, int offset) {
      this.token = token;
      this.offset = offset;
      this.url = null;
    }

    public HighlightToken(Tokens token, int offset, String url) {
      this.token = token;
      this.offset = offset;
      this.url = url;
    }
  }
}
