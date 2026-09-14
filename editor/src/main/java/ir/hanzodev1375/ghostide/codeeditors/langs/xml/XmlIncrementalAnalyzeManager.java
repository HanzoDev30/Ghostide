/**
 * Comment by ghost ide
 *
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.xml;

import android.os.Bundle;
import androidx.annotation.NonNull;
import io.github.rosemoe.sora.lang.analysis.AsyncIncrementalAnalyzeManager;
import io.github.rosemoe.sora.lang.brackets.SimpleBracketsCollector;
import io.github.rosemoe.sora.lang.completion.IdentifierAutoComplete;
import io.github.rosemoe.sora.lang.styling.CodeBlock;
import io.github.rosemoe.sora.lang.styling.Span;
import io.github.rosemoe.sora.lang.styling.SpanFactory;
import io.github.rosemoe.sora.lang.styling.TextStyle;
import io.github.rosemoe.sora.lang.styling.color.EditorColor;
import io.github.rosemoe.sora.lang.styling.span.SpanClickableUrl;
import io.github.rosemoe.sora.lang.styling.span.SpanExtAttrs;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.ContentReference;
import io.github.rosemoe.sora.util.IntPair;
import ir.hanzodev1375.ghostide.codeeditors.colorscheme.GhostColorScheme;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

public class XmlIncrementalAnalyzeManager
    extends AsyncIncrementalAnalyzeManager<XmlState, XmlIncrementalAnalyzeManager.HighlightToken> {

  private static final int STATE_NORMAL = 0;

  private static final int STATE_INCOMPLETE_BLOCK_COMMENT = 1;

  private static final Pattern URL_PATTERN =
      Pattern.compile(
          "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&/=]*)");

  private final ThreadLocal<XmlTextTokenizer> tokenizerProvider = new ThreadLocal<>();

  protected IdentifierAutoComplete.SyncIdentifiers identifiers =
      new IdentifierAutoComplete.SyncIdentifiers();

  private synchronized XmlTextTokenizer obtainTokenizer() {
    var res = tokenizerProvider.get();
    if (res == null) {
      res = new XmlTextTokenizer("");
      tokenizerProvider.set(res);
    }
    return res;
  }

  @Override
  public List<CodeBlock> computeBlocks(Content text, CodeBlockAnalyzeDelegate delegate) {
    var stack = new Stack<CodeBlock>();
    var blocks = new ArrayList<CodeBlock>();
    var brackets = new SimpleBracketsCollector();
    var bracketsStack = new Stack<Long>();
    for (int i = 0; i < text.getLineCount() && delegate.isNotCancelled(); i++) {
      var state = getState(i);
      boolean checkForIdentifiers =
          state.state.state == STATE_NORMAL
              || (state.state.state == STATE_INCOMPLETE_BLOCK_COMMENT && state.tokens.size() > 1);
      if (state.state.hasBraces || checkForIdentifiers) {
        for (var tokenRecord : state.tokens) {
          var token = tokenRecord.token;
          int offset = tokenRecord.offset;
          if (token == XmlTokens.TAG_OPEN) {
            CodeBlock block = new CodeBlock();
            block.startLine = i;
            block.startColumn = offset;
            stack.push(block);
          } else if (token == XmlTokens.TAG_OPEN_SLASH) {
            if (!stack.isEmpty()) {
              CodeBlock block = stack.pop();
              block.endLine = i;
              block.endColumn = offset;
              if (block.startLine != block.endLine) {
                blocks.add(block);
              }
            }
          }
          int type = getType(token);
          if (type > 0) {
            if (isStart(token)) {
              bracketsStack.push(IntPair.pack(type, text.getCharIndex(i, offset)));
            } else {
              if (!bracketsStack.isEmpty()) {
                var record = bracketsStack.pop();
                int typeRecord = IntPair.getFirst(record);
                if (typeRecord == type) {
                  brackets.add(IntPair.getSecond(record), text.getCharIndex(i, offset));
                } else if (type == 3) {
                  while (!bracketsStack.isEmpty()) {
                    record = bracketsStack.pop();
                    if (IntPair.getFirst(record) == 3) {
                      brackets.add(IntPair.getSecond(record), text.getCharIndex(i, offset));
                      break;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    if (delegate.isNotCancelled()) {
      withReceiver(r -> r.updateBracketProvider(this, brackets));
    }
    return blocks;
  }

  private static int getType(XmlTokens token) {
    if (token == XmlTokens.LBRACE || token == XmlTokens.RBRACE) return 3;
    if (token == XmlTokens.LBRACK || token == XmlTokens.RBRACK) return 2;
    if (token == XmlTokens.LPAREN || token == XmlTokens.RPAREN) return 1;
    return 0;
  }

  private static boolean isStart(XmlTokens token) {
    return token == XmlTokens.LBRACE || token == XmlTokens.LBRACK || token == XmlTokens.LPAREN;
  }

  @NonNull
  @Override
  public XmlState getInitialState() {
    return new XmlState();
  }

  @Override
  public boolean stateEquals(@NonNull XmlState state, @NonNull XmlState another) {
    return state.equals(another);
  }

  @Override
  public void onAddState(XmlState state) {
    if (state.identifiers != null) {
      for (String identifier : state.identifiers) {
        identifiers.identifierIncrease(identifier);
      }
    }
  }

  @Override
  public void onAbandonState(XmlState state) {
    if (state.identifiers != null) {
      for (String identifier : state.identifiers) {
        identifiers.identifierDecrease(identifier);
      }
    }
  }

  @Override
  public void reset(@NonNull ContentReference content, @NonNull Bundle extraArguments) {
    super.reset(content, extraArguments);
    identifiers.clear();
  }

  @Override
  public LineTokenizeResult<XmlState, HighlightToken> tokenizeLine(
      CharSequence line, XmlState state, int lineIndex) {
    var tokens = new ArrayList<HighlightToken>();
    int newState = STATE_NORMAL;
    var stateObj = new XmlState();
    if (state.state == STATE_NORMAL) {
      newState = tokenizeNormal(line, 0, tokens, stateObj, state.insideTag);
    } else if (state.state == STATE_INCOMPLETE_BLOCK_COMMENT) {
      var res = tryFillIncompleteComment(line, tokens);
      newState = IntPair.getFirst(res);
      if (newState == STATE_NORMAL) {
        newState = tokenizeNormal(line, IntPair.getSecond(res), tokens, stateObj, state.insideTag);
      } else {
        newState = STATE_INCOMPLETE_BLOCK_COMMENT;
        stateObj.insideTag = state.insideTag;
      }
    }
    if (tokens.isEmpty()) {
      tokens.add(new HighlightToken(XmlTokens.UNKNOWN, 0));
    }
    stateObj.state = newState;
    return new LineTokenizeResult<>(stateObj, tokens);
  }

  private long tryFillIncompleteComment(CharSequence line, List<HighlightToken> tokens) {
    char pre = '\0', cur = '\0';
    int offset = 0;
    while ((pre != '-' || cur != '-') && offset < line.length()) {
      pre = cur;
      cur = line.charAt(offset);
      offset++;
    }
    if (pre == '-' && cur == '-') {
      if (offset < line.length() && line.charAt(offset) == '>') {
        offset++;
        if (offset < 1000) {
          detectHighlightUrls(
              line.subSequence(0, offset), 0, XmlTokens.BLOCK_COMMENT_COMPLETE, tokens);
        } else {
          tokens.add(new HighlightToken(XmlTokens.BLOCK_COMMENT_COMPLETE, 0));
        }
        return IntPair.pack(STATE_NORMAL, offset);
      }
    }
    if (offset < 1000) {
      detectHighlightUrls(
          line.subSequence(0, offset), 0, XmlTokens.BLOCK_COMMENT_INCOMPLETE, tokens);
    } else {
      tokens.add(new HighlightToken(XmlTokens.BLOCK_COMMENT_INCOMPLETE, 0));
    }
    return IntPair.pack(STATE_INCOMPLETE_BLOCK_COMMENT, offset);
  }

  private int tokenizeNormal(
      CharSequence text, int offset, List<HighlightToken> tokens, XmlState st, boolean insideTag) {
    var tokenizer = obtainTokenizer();
    tokenizer.reset(text);
    tokenizer.offset = offset;
    tokenizer.setInsideTag(insideTag);
    XmlTokens token;
    int state = STATE_NORMAL;
    while ((token = tokenizer.nextToken()) != XmlTokens.EOF) {
      if (tokenizer.getTokenLength() < 1000
          && (token == XmlTokens.STRING_LITERAL
              || token == XmlTokens.BLOCK_COMMENT_COMPLETE
              || token == XmlTokens.BLOCK_COMMENT_INCOMPLETE
              || token == XmlTokens.LINE_COMMENT
              || token == XmlTokens.TEXT)) {
        detectHighlightUrls(tokenizer.getTokenText(), tokenizer.offset, token, tokens);
        if (token == XmlTokens.BLOCK_COMMENT_INCOMPLETE) {
          state = STATE_INCOMPLETE_BLOCK_COMMENT;
          break;
        }
        continue;
      }
      HighlightToken ht = new HighlightToken(token, tokenizer.offset);
      if (token == XmlTokens.TAG_NAME
          || token == XmlTokens.ATTRIBUTE_NAME
          || token == XmlTokens.IDENTIFIER) {
        ht.tokenText = tokenizer.getTokenText().toString();
      }
      tokens.add(ht);
      if (token == XmlTokens.LBRACE || token == XmlTokens.RBRACE) {
        st.hasBraces = true;
      }
      if (token == XmlTokens.IDENTIFIER) {
        st.addIdentifier(tokenizer.getTokenText());
      }
      if (token == XmlTokens.BLOCK_COMMENT_INCOMPLETE) {
        state = STATE_INCOMPLETE_BLOCK_COMMENT;
        break;
      }
    }
    st.insideTag = tokenizer.isInsideTag();
    return state;
  }

  private void detectHighlightUrls(
      CharSequence tokenText, int offset, XmlTokens token, List<HighlightToken> tokens) {
    var matcher = URL_PATTERN.matcher(tokenText);
    int index = 0;
    while (index < tokenText.length() && matcher.find(index)) {
      int start = matcher.start();
      int end = matcher.end();
      if (start > index) {
        tokens.add(new HighlightToken(token, offset + index));
      }
      tokens.add(new HighlightToken(token, offset + start, matcher.group()));
      index = end;
    }
    if (index != tokenText.length()) {
      tokens.add(new HighlightToken(token, offset + index));
    }
  }

  @Override
  public List<Span> generateSpansForLine(LineTokenizeResult<XmlState, HighlightToken> lineResult) {
    var spans = new ArrayList<Span>();
    var tokens = lineResult.tokens;
    XmlTokens previous = XmlTokens.UNKNOWN;
    boolean insideTag = false;

    for (int i = 0; i < tokens.size(); i++) {
      var tokenRecord = tokens.get(i);
      var token = tokenRecord.token;
      int offset = tokenRecord.offset;
      Span span;

      switch (token) {
        case WHITESPACE:
        case NEWLINE:
          span = SpanFactory.obtain(offset, TextStyle.makeStyle(GhostColorScheme.TEXT_NORMAL));
          break;

        case LINE_COMMENT:
        case BLOCK_COMMENT_COMPLETE:
        case BLOCK_COMMENT_INCOMPLETE:
          span =
              SpanFactory.obtain(
                  offset,
                  TextStyle.makeStyle(GhostColorScheme.COMMENT, 0, false, true, false, true));
          break;

        case TAG_OPEN:
          insideTag = true;
          span =
              SpanFactory.obtain(
                  offset, TextStyle.makeStyle(GhostColorScheme.HTML_TAG, 0, true, false, false));
          break;

        case TAG_OPEN_SLASH:
          insideTag = true;
          span =
              SpanFactory.obtain(
                  offset, TextStyle.makeStyle(GhostColorScheme.HTML_TAG, 0, true, false, false));
          break;

        case TAG_CLOSE:
        case TAG_SELF_CLOSE:
          insideTag = false;
          span =
              SpanFactory.obtain(
                  offset, TextStyle.makeStyle(GhostColorScheme.HTML_TAG, 0, true, false, false));
          break;

        case TAG_NAME:
          span =
              SpanFactory.obtain(
                  offset, TextStyle.makeStyle(GhostColorScheme.HTML_TAG, 0, true, false, false));
          break;

        case ATTRIBUTE_NAME:
          span =
              SpanFactory.obtain(
                  offset,
                  TextStyle.makeStyle(GhostColorScheme.ATTRIBUTE_NAME, 0, false, true, false));
          break;

        case ATTRIBUTE_VALUE:
        case STRING_LITERAL:
          span = SpanFactory.obtain(offset, TextStyle.makeStyle(GhostColorScheme.LITERAL, true));
          break;

        case TEXT:
          span = SpanFactory.obtain(offset, TextStyle.makeStyle(GhostColorScheme.TEXT_NORMAL));
          break;

        case CDATA_START:
        case CDATA_END:
          span =
              SpanFactory.obtain(
                  offset, TextStyle.makeStyle(GhostColorScheme.KEYWORD, 0, true, false, false));
          break;

        case DOCTYPE:
        case ENTITY:
        case PROCESSING_INSTRUCTION:
          span =
              SpanFactory.obtain(
                  offset, TextStyle.makeStyle(GhostColorScheme.KEYWORD, 0, true, false, false));
          break;

        case IDENTIFIER:
          int color = GhostColorScheme.TEXT_NORMAL;

          if (previous == XmlTokens.TAG_OPEN || previous == XmlTokens.TAG_OPEN_SLASH) {
            color = GhostColorScheme.IDENTIFIER_VAR;
          } else if (previous == XmlTokens.DOCTYPE) {
            color = GhostColorScheme.COLORNEXTCHAR;
          } else if (previous == XmlTokens.ASSIGN) {
           // color = GhostColorScheme.IDENTIFIER_VAR;
          } else if (previous == XmlTokens.COLON) {
           // color = GhostColorScheme.IDENTIFIER_NAME;
          } else if (previous == XmlTokens.DOT) {
            color = GhostColorScheme.COLORNEXTDOT;
          } else {
            int j = i + 1;
            XmlTokens next = XmlTokens.UNKNOWN;
            while (j < tokens.size()) {
              XmlTokens n = tokens.get(j).token;
              if (n != XmlTokens.WHITESPACE && n != XmlTokens.NEWLINE) {
                next = n;
                break;
              }
              j++;
            }
            if (insideTag && next == XmlTokens.ASSIGN) {
              color = GhostColorScheme.ATTRIBUTE_NAME;
            }
          }
          span = SpanFactory.obtain(offset, TextStyle.makeStyle(color));
          break;

        case ASSIGN:
          span = SpanFactory.obtain(offset, GhostColorScheme.OPERATOR);
          break;

        case LPAREN:
        case RPAREN:
        case LBRACE:
        case RBRACE:
        case LBRACK:
        case RBRACK:
        case SEMICOLON:
        case COLON:
        case COMMA:
        case DOT:
          span = SpanFactory.obtain(offset, GhostColorScheme.OPERATOR);
          break;

        default:
          span = SpanFactory.obtain(offset, GhostColorScheme.TEXT_NORMAL);
      }

      if (tokenRecord.url != null) {
        span = SpanFactory.obtain(span.getColumn(), span.getStyle());
        span.setSpanExt(SpanExtAttrs.EXT_INTERACTION_INFO, new SpanClickableUrl(tokenRecord.url));
        span.setUnderlineColor(new EditorColor(span.getForegroundColorId()));
      }

      spans.add(span);

      switch (token) {
        case WHITESPACE:
        case NEWLINE:
        case LINE_COMMENT:
        case BLOCK_COMMENT_COMPLETE:
        case BLOCK_COMMENT_INCOMPLETE:
          break;
        default:
          previous = token;
      }
    }
    return spans;
  }

  public static class HighlightToken {

    public XmlTokens token;

    public int offset;

    public String url;

    public String tokenText;

    public HighlightToken(XmlTokens token, int offset) {
      this.token = token;
      this.offset = offset;
    }

    public HighlightToken(XmlTokens token, int offset, String url) {
      this.token = token;
      this.offset = offset;
      this.url = url;
    }
  }
}
