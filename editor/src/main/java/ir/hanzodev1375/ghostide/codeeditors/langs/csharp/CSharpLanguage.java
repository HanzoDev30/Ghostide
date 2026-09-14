/**
 * Comment by ghost ide
 *
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.csharp;

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

public class CSharpLanguage implements Language {

  // Snippet‌های پرکاربرد
  private static final CodeSnippet CLASS_SNIPPET =
      CodeSnippetParser.parse("class ${1:MyClass} {\n    $0\n}");

  private static final CodeSnippet METHOD_SNIPPET =
      CodeSnippetParser.parse("${1:void} ${2:MethodName}(${3:params}) {\n    $0\n}");

  private static final CodeSnippet FOR_SNIPPET =
      CodeSnippetParser.parse("for (int ${1:i} = 0; $${1:i} < ${2:10}; $${1:i}++) {\n    $0\n}");

  private static final CodeSnippet FOREACH_SNIPPET =
      CodeSnippetParser.parse("foreach (var ${1:item} in ${2:collection}) {\n    $0\n}");

  private static final CodeSnippet IF_SNIPPET =
      CodeSnippetParser.parse("if (${1:condition}) {\n    $0\n}");

  private static final CodeSnippet ELSE_SNIPPET = CodeSnippetParser.parse("else {\n    $0\n}");

  private static final CodeSnippet ELSEIF_SNIPPET =
      CodeSnippetParser.parse("else if (${1:condition}) {\n    $0\n}");

  private static final CodeSnippet SWITCH_SNIPPET =
      CodeSnippetParser.parse(
          "switch (${1:value}) {\n    case ${2:case}:\n        $0\n        break;\n}");

  private static final CodeSnippet TRY_CATCH_SNIPPET =
      CodeSnippetParser.parse("try {\n    $0\n} catch (${1:Exception} ex) {\n    \n}");

  private static final CodeSnippet USING_SNIPPET =
      CodeSnippetParser.parse("using (${1:resource}) {\n    $0\n}");

  private static final CodeSnippet NAMESPACE_SNIPPET =
      CodeSnippetParser.parse("namespace ${1:MyNamespace} {\n    $0\n}");

  private static final String[] KEYWORDS = {
    "abstract",
    "as",
    "base",
    "bool",
    "break",
    "byte",
    "case",
    "catch",
    "char",
    "checked",
    "class",
    "const",
    "continue",
    "decimal",
    "default",
    "delegate",
    "do",
    "double",
    "else",
    "enum",
    "event",
    "explicit",
    "extern",
    "false",
    "finally",
    "fixed",
    "float",
    "for",
    "foreach",
    "goto",
    "if",
    "implicit",
    "in",
    "int",
    "interface",
    "internal",
    "is",
    "lock",
    "long",
    "namespace",
    "new",
    "null",
    "object",
    "operator",
    "out",
    "override",
    "params",
    "private",
    "protected",
    "public",
    "readonly",
    "ref",
    "return",
    "sbyte",
    "sealed",
    "short",
    "sizeof",
    "stackalloc",
    "static",
    "string",
    "struct",
    "switch",
    "this",
    "throw",
    "true",
    "try",
    "typeof",
    "uint",
    "ulong",
    "unchecked",
    "unsafe",
    "ushort",
    "using",
    "virtual",
    "void",
    "volatile",
    "while",
    "async",
    "await",
    "dynamic",
    "var",
    "yield"
  };

  private IdentifierAutoComplete autoComplete;

  private final CSharpIncrementalAnalyzeManager manager;

  private final CSharpQuoteHandler quoteHandler = new CSharpQuoteHandler();

  public CSharpLanguage() {
    autoComplete = new IdentifierAutoComplete(KEYWORDS);
    manager = new CSharpIncrementalAnalyzeManager();
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
    if ("class".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "class",
              "Snippet - Class",
              new SnippetDescription(prefix.length(), CLASS_SNIPPET, true)));
    if ("func".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "func",
              "Snippet - Method",
              new SnippetDescription(prefix.length(), METHOD_SNIPPET, true)));
    if ("for".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "for", "Snippet - for", new SnippetDescription(prefix.length(), FOR_SNIPPET, true)));
    if ("foreach".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "foreach",
              "Snippet - foreach",
              new SnippetDescription(prefix.length(), FOREACH_SNIPPET, true)));
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
              new SnippetDescription(prefix.length(), ELSEIF_SNIPPET, true)));
    if ("switch".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "switch",
              "Snippet - switch",
              new SnippetDescription(prefix.length(), SWITCH_SNIPPET, true)));
    if ("try".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "try",
              "Snippet - try-catch",
              new SnippetDescription(prefix.length(), TRY_CATCH_SNIPPET, true)));
    if ("using".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "using",
              "Snippet - using",
              new SnippetDescription(prefix.length(), USING_SNIPPET, true)));
    if ("namespace".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "namespace",
              "Snippet - namespace",
              new SnippetDescription(prefix.length(), NAMESPACE_SNIPPET, true)));
  }

  @Override
  public int getIndentAdvance(@NonNull ContentReference text, int line, int column) {
    var content = text.getLine(line).substring(0, column);
    return getIndentAdvance(content);
  }

  private int getIndentAdvance(String content) {
    CSharpTextTokenizer t = new CSharpTextTokenizer(content);
    CSharpTokens token;
    int advance = 0;
    while ((token = t.nextToken()) != CSharpTokens.EOF) {
      if (token == CSharpTokens.LBRACE) advance++;
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
