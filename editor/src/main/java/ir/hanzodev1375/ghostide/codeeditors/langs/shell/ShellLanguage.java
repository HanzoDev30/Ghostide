/**
 * Comment by ghost ide
 *
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.shell;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.github.rosemoe.sora.lang.EmptyLanguage;
import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.lang.QuickQuoteHandler;
import io.github.rosemoe.sora.lang.analysis.AnalyzeManager;
import io.github.rosemoe.sora.lang.completion.CompletionHelper;
import io.github.rosemoe.sora.lang.completion.CompletionPublisher;
import io.github.rosemoe.sora.lang.completion.IdentifierAutoComplete;
import io.github.rosemoe.sora.lang.completion.SimpleSnippetCompletionItem;
import io.github.rosemoe.sora.lang.completion.SnippetDescription;
import io.github.rosemoe.sora.lang.completion.snippet.CodeSnippet;
import io.github.rosemoe.sora.lang.completion.snippet.parser.CodeSnippetParser;
import io.github.rosemoe.sora.lang.format.Formatter;
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandleResult;
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandler;
import io.github.rosemoe.sora.lang.styling.Styles;
import io.github.rosemoe.sora.lang.styling.StylesUtils;
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.ContentReference;
import io.github.rosemoe.sora.text.TextUtils;
import io.github.rosemoe.sora.util.MyCharacter;
import io.github.rosemoe.sora.widget.SymbolPairMatch;

public class ShellLanguage implements Language {

  private static final CodeSnippet IF_SNIPPET =
      CodeSnippetParser.parse("if [ ${1:condition} ]; then\n    $0\nfi");

  private static final CodeSnippet FOR_SNIPPET =
      CodeSnippetParser.parse("for ${1:var} in ${2:list}; do\n    $0\ndone");

  private static final CodeSnippet WHILE_SNIPPET =
      CodeSnippetParser.parse("while [ ${1:condition} ]; do\n    $0\ndone");

  private static final CodeSnippet FUNCTION_SNIPPET =
      CodeSnippetParser.parse("${1:function_name}() {\n    $0\n}");

  private static final CodeSnippet CASE_SNIPPET =
      CodeSnippetParser.parse("case ${1:var} in\n    ${2:pattern})\n        $0\n        ;;\nesac");

  private static final CodeSnippet FOR_LOOP_C_SNIPPET =
      CodeSnippetParser.parse("for (( ${1:i}=0; ${1:i}<${2:10}; ${1:i}++ )); do\n    $0\ndone");

  private static final String[] KEYWORDS = {
    "if",
    "then",
    "else",
    "elif",
    "fi",
    "for",
    "while",
    "until",
    "do",
    "done",
    "case",
    "esac",
    "in",
    "function",
    "return",
    "exit",
    "source",
    "export",
    "readonly",
    "local",
    "declare",
    "typeset",
    "unset",
    "shift",
    "getopts",
    "select",
    "time",
    "eval",
    "exec",
    "trap",
    "wait",
    "suspend",
    "true",
    "false"
  };

  private IdentifierAutoComplete autoComplete;

  private final ShellIncrementalAnalyzeManager manager;

  private final ShellQuoteHandler quoteHandler = new ShellQuoteHandler();

  public ShellLanguage() {
    autoComplete = new IdentifierAutoComplete(KEYWORDS);
    manager = new ShellIncrementalAnalyzeManager();
  }

  @NonNull
  @Override
  public AnalyzeManager getAnalyzeManager() {
    return manager;
  }

  @Nullable
  @Override
  public QuickQuoteHandler getQuickQuoteHandler() {
    return quoteHandler;
  }

  @Override
  public void destroy() {
    autoComplete = null;
  }

  @Override
  public int getInterruptionLevel() {
    return INTERRUPTION_LEVEL_STRONG;
  }

  @Override
  public boolean useTab() {
    return true;
  }

  @Override
  public void requireAutoComplete(
      @NonNull ContentReference content,
      @NonNull CharPosition position,
      @NonNull CompletionPublisher publisher,
      @NonNull Bundle extraArguments) {
    var prefix =
        CompletionHelper.computePrefix(content, position, MyCharacter::isJavaIdentifierPart);
    final var idt = manager.identifiers;
    if (idt != null && autoComplete != null) {
      autoComplete.requireAutoComplete(content, position, prefix, publisher, idt);
    }
    if ("if".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "if",
              "Snippet - if statement",
              new SnippetDescription(prefix.length(), IF_SNIPPET, true)));
    if ("for".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "for",
              "Snippet - for loop",
              new SnippetDescription(prefix.length(), FOR_SNIPPET, true)));
    if ("while".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "while",
              "Snippet - while loop",
              new SnippetDescription(prefix.length(), WHILE_SNIPPET, true)));
    if ("func".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "func",
              "Snippet - function",
              new SnippetDescription(prefix.length(), FUNCTION_SNIPPET, true)));
    if ("case".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "case",
              "Snippet - case statement",
              new SnippetDescription(prefix.length(), CASE_SNIPPET, true)));
    if ("forc".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "forc",
              "Snippet - C style for loop",
              new SnippetDescription(prefix.length(), FOR_LOOP_C_SNIPPET, true)));
  }

  @Override
  public int getIndentAdvance(@NonNull ContentReference text, int line, int column) {
    var content = text.getLine(line).substring(0, column);
    return getIndentAdvance(content);
  }

  private int getIndentAdvance(String content){
    int advance = 0;
    ShellTextTokenizer t = new ShellTextTokenizer(content);
    ShellTokens token;
    while ((token = t.nextToken()) != ShellTokens.EOF) {
      if (token == ShellTokens.LBRACE
          || token == ShellTokens.KEYWORD_DO
          || token == ShellTokens.KEYWORD_THEN
          || token == ShellTokens.KEYWORD_ELSE
          || token == ShellTokens.KEYWORD_ELIF
          || token == ShellTokens.KEYWORD_CASE
          || token == ShellTokens.KEYWORD_SELECT) {
        advance++;
      }
      if (token == ShellTokens.RBRACE
          || token == ShellTokens.KEYWORD_DONE
          || token == ShellTokens.KEYWORD_FI
          || token == ShellTokens.KEYWORD_ESAC) {
        advance--;
      }
    }
    return Math.max(0, advance) * 4;
  }
  private final NewlineHandler[] newlineHandlers = new NewlineHandler[] {new BraceHandler()};

  @Override
  public NewlineHandler[] getNewlineHandlers() {
    return newlineHandlers;
  }

  @NonNull
  @Override
  public Formatter getFormatter() {
    return EmptyLanguage.EmptyFormatter.INSTANCE;
  }

  @Override
  public SymbolPairMatch getSymbolPairs() {
    return new SymbolPairMatch.DefaultSymbolPairs();
  }

  private static String getNonEmptyTextBefore(CharSequence text, int index, int length) {
    while (index > 0 && Character.isWhitespace(text.charAt(index - 1))) {
      index--;
    }
    return text.subSequence(Math.max(0, index - length), index).toString();
  }

  private static String getNonEmptyTextAfter(CharSequence text, int index, int length) {
    while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
      index++;
    }
    return text.subSequence(index, Math.min(index + length, text.length())).toString();
  }

  class BraceHandler implements NewlineHandler {

    @Override
    public boolean matchesRequirement(
        @NonNull Content text, @NonNull CharPosition position, @Nullable Styles style) {
      var line = text.getLine(position.line);
      return !StylesUtils.checkNoCompletion(style, position)
          && getNonEmptyTextBefore(line, position.column, 1).equals("{")
          && getNonEmptyTextAfter(line, position.column, 1).equals("}");
    }

    @NonNull
    @Override
    public NewlineHandleResult handleNewline(
        @NonNull Content text,
        @NonNull CharPosition position,
        @Nullable Styles style,
        int tabSize) {
      var line = text.getLine(position.line);
      int index = position.column;
      var beforeText = line.subSequence(0, index).toString();
      var afterText = line.subSequence(index, line.length()).toString();
      return handleNewline(beforeText, afterText, tabSize);
    }

    @NonNull
    public NewlineHandleResult handleNewline(String beforeText, String afterText, int tabSize) {
      int count = TextUtils.countLeadingSpaceCount(beforeText, tabSize);
      int advanceBefore = getIndentAdvance(beforeText);
      int advanceAfter = getIndentAdvance(afterText);
      String text;
      StringBuilder sb =
          new StringBuilder("\n")
              .append(TextUtils.createIndent(count + advanceBefore, tabSize, useTab()))
              .append('\n')
              .append(text = TextUtils.createIndent(count + advanceAfter, tabSize, useTab()));
      int shiftLeft = text.length() + 1;
      return new NewlineHandleResult(sb, shiftLeft);
    }
  }
}
