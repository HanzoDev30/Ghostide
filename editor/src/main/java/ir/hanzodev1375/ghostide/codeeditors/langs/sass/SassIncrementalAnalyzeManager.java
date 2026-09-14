package ir.hanzodev1375.ghostide.codeeditors.langs.sass;

import android.os.Bundle;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import io.github.rosemoe.sora.lang.analysis.AsyncIncrementalAnalyzeManager;
import io.github.rosemoe.sora.lang.styling.CodeBlock;
import io.github.rosemoe.sora.lang.styling.Span;
import io.github.rosemoe.sora.lang.styling.SpanFactory;
import io.github.rosemoe.sora.lang.styling.TextStyle;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.ContentReference;
import ir.hanzodev1375.ghostide.codeeditors.colorscheme.GhostColorScheme;

public class SassIncrementalAnalyzeManager
    extends AsyncIncrementalAnalyzeManager<
        SassState, SassIncrementalAnalyzeManager.HighlightToken> {

  private final ThreadLocal<SassTextTokenizer> tokenizerProvider = new ThreadLocal<>();

  private synchronized SassTextTokenizer obtainTokenizer() {
    var res = tokenizerProvider.get();
    if (res == null) {
      res = new SassTextTokenizer("");
      tokenizerProvider.set(res);
    }
    return res;
  }

  @Override
  public List<CodeBlock> computeBlocks(Content text, CodeBlockAnalyzeDelegate delegate) {
    return new ArrayList<>();
  }

  @NonNull
  @Override
  public SassState getInitialState() {
    return new SassState();
  }

  @Override
  public boolean stateEquals(@NonNull SassState state, @NonNull SassState another) {
    return state.equals(another);
  }

  @Override
  public void onAddState(SassState state) {}

  @Override
  public void onAbandonState(SassState state) {}

  @Override
  public void reset(@NonNull ContentReference content, @NonNull Bundle extraArguments) {
    super.reset(content, extraArguments);
  }

  @Override
  public LineTokenizeResult<SassState, HighlightToken> tokenizeLine(
      CharSequence line, SassState state, int lineIndex) {
    var tokens = new ArrayList<HighlightToken>();
    var stateObj = new SassState();
    var tokenizer = obtainTokenizer();
    tokenizer.reset(line);

    if (state.insideBlockComment) {
      stateObj.insideBlockComment = true;
      boolean closed = false;
      int end = line.length();
      for (int i = 0; i < line.length() - 1; i++) {
        if (line.charAt(i) == '*' && line.charAt(i + 1) == '/') {
          closed = true;
          end = i + 2;
          break;
        }
      }
      if (closed) {
        stateObj.insideBlockComment = false;
        tokens.add(new HighlightToken(SassTokens.BLOCK_COMMENT, 0));
        if (end < line.length()) {
          tokenizer.reset(line.subSequence(end, line.length()));
          SassTokens tk;
          while ((tk = tokenizer.nextToken()) != SassTokens.EOF) {
            tokens.add(new HighlightToken(tk, tokenizer.offset + end));
          }
        }
      } else {
        tokens.add(new HighlightToken(SassTokens.BLOCK_COMMENT, 0));
      }
      return new LineTokenizeResult<>(stateObj, tokens);
    }

    SassTokens token;
    while ((token = tokenizer.nextToken()) != SassTokens.EOF) {
      if (token == SassTokens.BLOCK_COMMENT) {
        String text =
            line.subSequence(tokenizer.offset, tokenizer.offset + tokenizer.length).toString();
        if (!text.endsWith("*/")) {
          stateObj.insideBlockComment = true;
        }
      }
      tokens.add(new HighlightToken(token, tokenizer.offset));
    }
    if (tokens.isEmpty()) tokens.add(new HighlightToken(SassTokens.UNKNOWN, 0));
    return new LineTokenizeResult<>(stateObj, tokens);
  }

  @Override
  public List<Span> generateSpansForLine(LineTokenizeResult<SassState, HighlightToken> lineResult) {
    var spans = new ArrayList<Span>();
    var tokens = lineResult.tokens;
    SassTokens previous = SassTokens.UNKNOWN;
    SassTokens next = SassTokens.UNKNOWN;

    for (int i = 0; i < tokens.size(); i++) {
      var tokenRecord = tokens.get(i);
      var token = tokenRecord.token;
      int offset = tokenRecord.offset;

      if (token == SassTokens.IDENT) {
        int j = i + 1;
        while (j < tokens.size()) {
          var n = tokens.get(j).token;
          if (n != SassTokens.WHITESPACE && n != SassTokens.NEWLINE) {
            next = n;
            break;
          }
          j++;
        }
      }

      Span span;
      switch (token) {
        case WHITESPACE:
        case NEWLINE:
          span = SpanFactory.obtain(offset, TextStyle.makeStyle(GhostColorScheme.TEXT_NORMAL));
          break;

        case LINE_COMMENT:
        case BLOCK_COMMENT:
          span =
              SpanFactory.obtain(
                  offset,
                  TextStyle.makeStyle(GhostColorScheme.COMMENT, 0, false, true, false, true));
          break;

        case AT_KEYWORD:
          span =
              SpanFactory.obtain(
                  offset, TextStyle.makeStyle(GhostColorScheme.KEYWORD, 0, true, false, false));
          break;

        case VARIABLE:
          span =
              SpanFactory.obtain(
                  offset,
                  TextStyle.makeStyle(GhostColorScheme.IDENTIFIER_VAR, 0, true, false, false));
          break;

        case PLACEHOLDER:
          span =
              SpanFactory.obtain(
                  offset,
                  TextStyle.makeStyle(GhostColorScheme.IDENTIFIER_NAME, 0, true, false, false));
          break;

        case PARENT_SELECTOR:
          span =
              SpanFactory.obtain(
                  offset, TextStyle.makeStyle(GhostColorScheme.KEYWORD, 0, true, false, false));
          break;

        case INTERPOLATION_START:
          span =
              SpanFactory.obtain(
                  offset, TextStyle.makeStyle(GhostColorScheme.KEYWORD, 0, true, false, false));
          break;

        case IDENT:
          {
            int color = GhostColorScheme.TEXT_NORMAL;

            if (previous == SassTokens.AT_KEYWORD) {
              color = GhostColorScheme.FUNCTION_NAME;
            } else if (previous == SassTokens.DOT) {
              color = GhostColorScheme.COLORNEXTDOT;
            } else if (previous == SassTokens.COLON) {
              color = GhostColorScheme.ATTRIBUTE_NAME;
            } else if (previous == SassTokens.PERCENT) {
              color = GhostColorScheme.COLORNEXTBRAK;
            } else if (previous == SassTokens.PARENT_SELECTOR) {
              color = GhostColorScheme.FUNCTION_NAME;
            } else if (previous == SassTokens.VARIABLE) {
              color = GhostColorScheme.ATTRIBUTE_NAME;
            } else if (next == SassTokens.LPAREN) {
              color = GhostColorScheme.FUNCTION_NAME;
            } else if (next == SassTokens.COLON) {
              color = GhostColorScheme.ATTRIBUTE_NAME;
            } else if (next == SassTokens.LBRACE) {
              color = GhostColorScheme.LITERAL;
            }

            span = SpanFactory.obtain(offset, TextStyle.makeStyle(color));
            break;
          }

        case STRING_LITERAL:
          span = SpanFactory.obtain(offset, TextStyle.makeStyle(GhostColorScheme.LITERAL, true));
          break;

        case NUMBER:
        case UNIT:
        case COLOR_HEX:
          span = SpanFactory.obtain(offset, TextStyle.makeStyle(GhostColorScheme.LITERAL, true));
          break;

        case LBRACE:
        case RBRACE:
        case LPAREN:
        case RPAREN:
        case LBRACK:
        case RBRACK:
        case SEMICOLON:
        case COLON:
        case COMMA:
        case DOT:
        case PLUS:
        case MINUS:
        case STAR:
        case SLASH:
        case PERCENT:
        case EQ:
        case GT:
        case LT:
        case TILDE:
        case CARET:
          span = SpanFactory.obtain(offset, GhostColorScheme.OPERATOR);
          break;

        default:
          span = SpanFactory.obtain(offset, TextStyle.makeStyle(GhostColorScheme.TEXT_NORMAL));
      }

      if (token != SassTokens.WHITESPACE && token != SassTokens.NEWLINE) {
        previous = token;
      }
      next = SassTokens.UNKNOWN;

      spans.add(span);
    }
    return spans;
  }

  public static class HighlightToken {
    public SassTokens token;
    public int offset;

    public HighlightToken(SassTokens token, int offset) {
      this.token = token;
      this.offset = offset;
    }
  }
}
