/**
 * Comment by ghost ide
 *
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.dart;

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

public class DartLanguage implements Language {

  // Snippet‌های پرکاربرد دارت
  private static final CodeSnippet FOR_SNIPPET =
      CodeSnippetParser.parse("for (var ${1:i} = 0; $${1:i} < ${2:10}; $${1:i}++) {\n    $0\n}");

  private static final CodeSnippet FOR_IN_SNIPPET =
      CodeSnippetParser.parse("for (var ${1:item} in ${2:iterable}) {\n    $0\n}");

  private static final CodeSnippet WHILE_SNIPPET =
      CodeSnippetParser.parse("while (${1:condition}) {\n    $0\n}");

  private static final CodeSnippet IF_SNIPPET =
      CodeSnippetParser.parse("if (${1:condition}) {\n    $0\n}");

  private static final CodeSnippet ELSE_SNIPPET = CodeSnippetParser.parse("else {\n    $0\n}");

  private static final CodeSnippet ELSE_IF_SNIPPET =
      CodeSnippetParser.parse("else if (${1:condition}) {\n    $0\n}");

  private static final CodeSnippet SWITCH_SNIPPET =
      CodeSnippetParser.parse(
          "switch (${1:value}) {\n    case ${2:case}:\n        $0\n        break;\n}");

  private static final CodeSnippet CLASS_SNIPPET =
      CodeSnippetParser.parse("class ${1:Name} {\n    $0\n}");

  private static final CodeSnippet FUNCTION_SNIPPET =
      CodeSnippetParser.parse("${1:void} ${2:name}(${3:params}) {\n    $0\n}");

  private static final CodeSnippet MAIN_SNIPPET =
      CodeSnippetParser.parse("void main() {\n    $0\n}");

  private static final CodeSnippet TRY_CATCH_SNIPPET =
      CodeSnippetParser.parse("try {\n    $0\n} catch (${1:e}) {\n    \n}");

  private static final String[] KEYWORDS = {
    "abstract",
    "as",
    "assert",
    "async",
    "await",
    "break",
    "case",
    "catch",
    "class",
    "const",
    "continue",
    "covariant",
    "default",
    "deferred",
    "do",
    "dynamic",
    "else",
    "enum",
    "export",
    "extends",
    "extension",
    "external",
    "factory",
    "false",
    "final",
    "finally",
    "for",
    "Function",
    "get",
    "hide",
    "if",
    "implements",
    "import",
    "in",
    "interface",
    "is",
    "library",
    "mixin",
    "new",
    "null",
    "on",
    "operator",
    "part",
    "required",
    "rethrow",
    "return",
    "set",
    "show",
    "static",
    "super",
    "switch",
    "sync",
    "this",
    "throw",
    "true",
    "try",
    "typedef",
    "var",
    "void",
    "while",
    "with",
    "yield"
  };

  private IdentifierAutoComplete autoComplete;

  private final DartIncrementalAnalyzeManager manager;

  private final DartQuoteHandler quoteHandler = new DartQuoteHandler();

  public DartLanguage() {
    autoComplete = new IdentifierAutoComplete(KEYWORDS);
    manager = new DartIncrementalAnalyzeManager();
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
    // اضافه کردن Snippet‌ها با پیشوند مناسب
    if ("for".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "for",
              "Snippet - for loop",
              new SnippetDescription(prefix.length(), FOR_SNIPPET, true)));
    if ("forin".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "forin",
              "Snippet - for-in loop",
              new SnippetDescription(prefix.length(), FOR_IN_SNIPPET, true)));
    if ("while".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "while",
              "Snippet - while loop",
              new SnippetDescription(prefix.length(), WHILE_SNIPPET, true)));
    if ("if".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "if", "Snippet - if", new SnippetDescription(prefix.length(), IF_SNIPPET, true)));
    if ("else".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "else",
              "Snippet - else",
              new SnippetDescription(prefix.length(), ELSE_SNIPPET, true)));
    if ("elseif".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "elseif",
              "Snippet - else if",
              new SnippetDescription(prefix.length(), ELSE_IF_SNIPPET, true)));
    if ("switch".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "switch",
              "Snippet - switch",
              new SnippetDescription(prefix.length(), SWITCH_SNIPPET, true)));
    if ("class".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "class",
              "Snippet - class",
              new SnippetDescription(prefix.length(), CLASS_SNIPPET, true)));
    if ("func".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "func",
              "Snippet - function",
              new SnippetDescription(prefix.length(), FUNCTION_SNIPPET, true)));
    if ("main".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "main",
              "Snippet - main function",
              new SnippetDescription(prefix.length(), MAIN_SNIPPET, true)));
    if ("try".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "try",
              "Snippet - try-catch",
              new SnippetDescription(prefix.length(), TRY_CATCH_SNIPPET, true)));
  }

  @Override
  public int getIndentAdvance(@NonNull ContentReference text, int line, int column) {
    var content = text.getLine(line).substring(0, column);
    return getIndentAdvance(content);
  }

  private int getIndentAdvance(String content) {
    DartTextTokenizer t = new DartTextTokenizer(content);
    DartTokens token;
    int advance = 0;
    while ((token = t.nextToken()) != DartTokens.EOF) {
      if (token == DartTokens.LBRACE) advance++;
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
