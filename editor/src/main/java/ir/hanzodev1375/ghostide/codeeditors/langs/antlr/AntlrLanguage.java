/**
 * Comment by ghost ide
 *
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.antlr;

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

public class AntlrLanguage implements Language {

  private static final CodeSnippet GRAMMAR_SNIPPET =
      CodeSnippetParser.parse("grammar ${1:Name};\n\n$0");

  private static final CodeSnippet LEXER_SNIPPET =
      CodeSnippetParser.parse("lexer grammar ${1:Name};\n\n$0");

  private static final CodeSnippet PARSER_SNIPPET =
      CodeSnippetParser.parse("parser grammar ${1:Name};\n\n$0");

  private static final CodeSnippet RULE_SNIPPET =
      CodeSnippetParser.parse("${1:ruleName}\n    : ${2:token} $0\n    ;");

  private static final CodeSnippet TOKEN_SNIPPET =
      CodeSnippetParser.parse("${1:TOKEN_NAME}: ${2:'value'};");

  private static final CodeSnippet FRAGMENT_SNIPPET =
      CodeSnippetParser.parse("fragment ${1:FRAGMENT_NAME}: ${2:'value'};");

  private static final CodeSnippet OPTIONS_SNIPPET =
      CodeSnippetParser.parse("options {\n    ${1:option} = ${2:value};\n}");

  private static final CodeSnippet ACTION_SNIPPET = CodeSnippetParser.parse("{\n    $0\n}");

  private static final String[] KEYWORDS = {
    "fragment",
    "lexer",
    "parser",
    "grammar",
    "options",
    "tokens",
    "channels",
    "import",
    "mode",
    "pushMode",
    "popMode",
    "more",
    "skip",
    "type",
    "returns",
    "throws",
    "catch",
    "finally",
    "local"
  };

  private IdentifierAutoComplete autoComplete;

  private final AntlrIncrementalAnalyzeManager manager;

  private final AntlrQuoteHandler quoteHandler = new AntlrQuoteHandler();

  public AntlrLanguage() {
    autoComplete = new IdentifierAutoComplete(KEYWORDS);
    manager = new AntlrIncrementalAnalyzeManager();
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
    if ("grammar".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "grammar",
              "Snippet - Grammar",
              new SnippetDescription(prefix.length(), GRAMMAR_SNIPPET, true)));
    if ("lexer".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "lexer",
              "Snippet - Lexer Grammar",
              new SnippetDescription(prefix.length(), LEXER_SNIPPET, true)));
    if ("parser".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "parser",
              "Snippet - Parser Grammar",
              new SnippetDescription(prefix.length(), PARSER_SNIPPET, true)));
    if ("rule".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "rule",
              "Snippet - Rule",
              new SnippetDescription(prefix.length(), RULE_SNIPPET, true)));
    if ("token".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "token",
              "Snippet - Token",
              new SnippetDescription(prefix.length(), TOKEN_SNIPPET, true)));
    if ("fragment".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "fragment",
              "Snippet - Fragment",
              new SnippetDescription(prefix.length(), FRAGMENT_SNIPPET, true)));
    if ("options".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "options",
              "Snippet - Options",
              new SnippetDescription(prefix.length(), OPTIONS_SNIPPET, true)));
    if ("action".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "action",
              "Snippet - Action",
              new SnippetDescription(prefix.length(), ACTION_SNIPPET, true)));
  }

  @Override
  public int getIndentAdvance(@NonNull ContentReference text, int line, int column) {
    var content = text.getLine(line).substring(0, column);
    return getIndentAdvance(content);
  }

  int getIndentAdvance(String content) {
    int advance = 0;
    AntlrTextTokenizer t = new AntlrTextTokenizer(content);
    AntlrTokens token;
    while ((token = t.nextToken()) != AntlrTokens.EOF) {
      if (token == AntlrTokens.LBRACE) advance++;
      if (token == AntlrTokens.RBRACE) advance--;
    }
    return Math.max(0, advance) * 2;
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
