package ir.hanzodev1375.ghostide.codeeditors.langs.gradle;

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

public class GradleIncrementalAnalyzeManager
    extends AsyncIncrementalAnalyzeManager<State, GradleIncrementalAnalyzeManager.HighlightToken> {

  private static final int STATE_NORMAL = 0;
  private static final int STATE_INCOMPLETE_COMMENT = 1;
  private static final Pattern URL_PATTERN =
      Pattern.compile(
          "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&/=]*)");

  private final ThreadLocal<GradleTextTokenizer> tokenizerProvider = new ThreadLocal<>();
  protected IdentifierAutoComplete.SyncIdentifiers identifiers =
      new IdentifierAutoComplete.SyncIdentifiers();

  private synchronized GradleTextTokenizer obtainTokenizer() {
    var res = tokenizerProvider.get();
    if (res == null) {
      res = new GradleTextTokenizer("");
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
      if (state.state.hasBraces || state.state.state == STATE_NORMAL) {
        for (var tokenRecord : state.tokens) {
          var token = tokenRecord.token;
          if (token == Tokens.LBRACE) {
            CodeBlock block = new CodeBlock();
            block.startLine = i;
            block.startColumn = tokenRecord.offset;
            stack.push(block);
          } else if (token == Tokens.RBRACE) {
            if (!stack.isEmpty()) {
              CodeBlock block = stack.pop();
              block.endLine = i;
              block.endColumn = tokenRecord.offset;
              if (block.startLine != block.endLine) blocks.add(block);
            }
          }
          var type = getType(token);
          if (type > 0) {
            if (isStart(token)) {
              bracketsStack.push(IntPair.pack(type, text.getCharIndex(i, tokenRecord.offset)));
            } else if (!bracketsStack.isEmpty()) {
              var record = bracketsStack.pop();
              if (IntPair.getFirst(record) == type) {
                brackets.add(IntPair.getSecond(record), text.getCharIndex(i, tokenRecord.offset));
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

  private static int getType(Tokens token) {
    if (token == Tokens.LBRACE || token == Tokens.RBRACE) return 3;
    if (token == Tokens.LBRACK || token == Tokens.RBRACK) return 2;
    if (token == Tokens.LPAREN || token == Tokens.RPAREN) return 1;
    return 0;
  }

  private static boolean isStart(Tokens token) {
    return token == Tokens.LBRACE || token == Tokens.LBRACK || token == Tokens.LPAREN;
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
    int newState;
    var stateObj = new State();
    if (state.state == STATE_NORMAL) {
      newState = tokenizeNormal(line, 0, tokens, stateObj);
    } else {
      var res = tryFillIncompleteComment(line, tokens);
      newState = IntPair.getFirst(res);
      if (newState == STATE_NORMAL) {
        newState = tokenizeNormal(line, IntPair.getSecond(res), tokens, stateObj);
      } else {
        newState = STATE_INCOMPLETE_COMMENT;
      }
    }
    if (tokens.isEmpty()) tokens.add(new HighlightToken(Tokens.UNKNOWN, 0));
    stateObj.state = newState;
    return new LineTokenizeResult<>(stateObj, tokens);
  }

  private long tryFillIncompleteComment(CharSequence line, List<HighlightToken> tokens) {
    char pre = '\0', cur = '\0';
    int offset = 0;
    while ((pre != '*' || cur != '/') && offset < line.length()) {
      pre = cur;
      cur = line.charAt(offset);
      offset++;
    }
    if (pre == '*' && cur == '/') {
      tokens.add(new HighlightToken(Tokens.LONG_COMMENT_COMPLETE, 0));
      return IntPair.pack(STATE_NORMAL, offset);
    }
    tokens.add(new HighlightToken(Tokens.LONG_COMMENT_INCOMPLETE, 0));
    return IntPair.pack(STATE_INCOMPLETE_COMMENT, offset);
  }

  private int tokenizeNormal(CharSequence text, int offset, List<HighlightToken> tokens, State st) {
    var tokenizer = obtainTokenizer();
    tokenizer.reset(text);
    tokenizer.offset = offset;
    Tokens token;
    int state = STATE_NORMAL;
    while ((token = tokenizer.nextToken()) != Tokens.EOF) {
      if (token == Tokens.STRING_LITERAL) {
        addStringLiteralTokens(tokenizer.getTokenText(), tokenizer.offset, tokens);
      } else {
        tokens.add(new HighlightToken(token, tokenizer.offset));
      }
      if (token == Tokens.LBRACE || token == Tokens.RBRACE) st.hasBraces = true;
      if (token == Tokens.IDENTIFIER) st.addIdentifier(tokenizer.getTokenText());
      if (token == Tokens.LONG_COMMENT_INCOMPLETE) {
        state = STATE_INCOMPLETE_COMMENT;
        break;
      }
    }
    return state;
  }

  private void addStringLiteralTokens(CharSequence text, int base, List<HighlightToken> tokens) {
    int len = text.length();
    if (len == 0 || (text.charAt(0) != '"' && text.charAt(0) != '\'')) {
      tokens.add(new HighlightToken(Tokens.STRING_LITERAL, base));
      return;
    }
    int segStart = 0;
    int i = 0;
    while (i < len) {
      char ch = text.charAt(i);
      if (ch == '\\') {
        i = Math.min(len, i + 2);
        continue;
      }
      if (ch == '$' && i + 1 < len && isVarStart(text.charAt(i + 1))) {
        if (i > segStart) tokens.add(new HighlightToken(Tokens.STRING_LITERAL, base + segStart));
        tokens.add(new HighlightToken(Tokens.DOLLAR, base + i));
        int j = i + 1;
        while (j < len && isVarPart(text.charAt(j))) j++;
        tokens.add(new HighlightToken(Tokens.STRING_VARIABLE, base + i + 1));
        i = j;
        segStart = i;
        continue;
      }
      i++;
    }
    if (segStart < len) {
      tokens.add(new HighlightToken(Tokens.STRING_LITERAL, base + segStart));
    }
  }

  private static boolean isVarStart(char c) {
    return Character.isJavaIdentifierStart(c) && c != '$';
  }

  private static boolean isVarPart(char c) {
    return Character.isJavaIdentifierPart(c);
  }

  @Override
  public List<Span> generateSpansForLine(LineTokenizeResult<State, HighlightToken> lineResult) {
    var spans = new ArrayList<Span>();
    var tokens = lineResult.tokens;
    for (var tokenRecord : tokens) {
      var token = tokenRecord.token;
      int offset = tokenRecord.offset;
      Span span;
      switch (token) {
        case WHITESPACE:
        case NEWLINE:
          span = SpanFactory.obtain(offset, TextStyle.makeStyle(GhostColorScheme.TEXT_NORMAL));
          break;
        case INTEGER_LITERAL:
        case FLOATING_POINT_LITERAL:
        case STRING_LITERAL:
        case NULL_LITERAL:
        case TRUE:
        case FALSE:
          span = SpanFactory.obtain(offset, TextStyle.makeStyle(GhostColorScheme.LITERAL, true));
          break;
        case STRING_VARIABLE:
          span =
              SpanFactory.obtain(
                  offset, TextStyle.makeStyle(GhostColorScheme.IDENTIFIER_VAR, true));
          break;
        case APPLY:
        case PLUGIN:
        case PLUGINS:
        case DEPENDENCIES:
        case REPOSITORIES:
        case ANDROID:
        case IMPLEMENTATION:
        case TESTIMPLEMENTATION:
        case DEBUG_IMPLEMENTATION:
        case COMPILE_ONLY:
        case RUNTIME_ONLY:
        case KAPT:
        case ANNOTATIONPROCESSOR:
        case CLASSPATH:
        case BUILDSCRIPT:
        case ALLPROJECTS:
        case SUBPROJECTS:
        case TASK:
        case EXT:
        case DEF:
        case IF:
        case ELSE:
        case RETURN:
        case NEW:
        case IMPORT:
          span =
              SpanFactory.obtain(
                  offset, TextStyle.makeStyle(GhostColorScheme.KEYWORD, 0, true, false, false));
          break;
        case LINE_COMMENT:
        case LONG_COMMENT_COMPLETE:
        case LONG_COMMENT_INCOMPLETE:
          span =
              SpanFactory.obtain(
                  offset,
                  TextStyle.makeStyle(GhostColorScheme.COMMENT, 0, false, true, false, true));
          break;
        case IDENTIFIER:
          span = SpanFactory.obtain(offset, TextStyle.makeStyle(GhostColorScheme.TEXT_NORMAL));
          break;
        case DOLLAR:
          span = SpanFactory.obtain(offset, GhostColorScheme.ATTRIBUTE_NAME);
          break;
        default:
          span = SpanFactory.obtain(offset, GhostColorScheme.OPERATOR);
      }
      spans.add(span);
    }
    return spans;
  }

  public static class HighlightToken {
    public Tokens token;
    public int offset;
    public String url;

    public HighlightToken(Tokens token, int offset) {
      this.token = token;
      this.offset = offset;
    }

    public HighlightToken(Tokens token, int offset, String url) {
      this.token = token;
      this.offset = offset;
      this.url = url;
    }
  }
}
