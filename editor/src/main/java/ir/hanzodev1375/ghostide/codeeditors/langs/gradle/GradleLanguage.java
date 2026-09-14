package ir.hanzodev1375.ghostide.codeeditors.langs.gradle;

import static java.lang.Character.isWhitespace;

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

public class GradleLanguage implements Language {

  private static final CodeSnippet DEPENDENCY_SNIPPET =
      CodeSnippetParser.parse("implementation '${1:group}:${2:artifact}:${3:version}'");
  private static final CodeSnippet TASK_SNIPPET =
      CodeSnippetParser.parse("task ${1:name} {\n    $0\n}");

  private IdentifierAutoComplete autoComplete;
  private final GradleIncrementalAnalyzeManager manager;
  private final GradleQuoteHandler quoteHandler = new GradleQuoteHandler();

  private static final String[] KEYWORDS = {
    "apply",
    "plugin",
    "plugins",
    "dependencies",
    "repositories",
    "android",
    "implementation",
    "testImplementation",
    "debugImplementation",
    "compileOnly",
    "runtimeOnly",
    "kapt",
    "annotationProcessor",
    "classpath",
    "buildscript",
    "allprojects",
    "subprojects",
    "task",
    "ext",
    "def",
    "if",
    "else",
    "return",
    "true",
    "false",
    "null",
    "new",
    "import"
  };

  public GradleLanguage() {
    autoComplete = new IdentifierAutoComplete(KEYWORDS);
    manager = new GradleIncrementalAnalyzeManager();
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
    if ("implementation".startsWith(prefix) && !prefix.isEmpty()) {
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "implementation",
              "Snippet - Add dependency",
              new SnippetDescription(prefix.length(), DEPENDENCY_SNIPPET, true)));
    }
    if ("task".startsWith(prefix) && !prefix.isEmpty()) {
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "task",
              "Snippet - Create task",
              new SnippetDescription(prefix.length(), TASK_SNIPPET, true)));
    }
  }

  @Override
  public int getIndentAdvance(@NonNull ContentReference text, int line, int column) {
    var content = text.getLine(line).substring(0, column);
    return getIndentAdvance(content);
  }

  private int getIndentAdvance(String content) {
    GradleTextTokenizer t = new GradleTextTokenizer(content);
    Tokens token;
    int advance = 0;
    while ((token = t.nextToken()) != Tokens.EOF) {
      if (token == Tokens.LBRACE) advance++;
    }
    advance = Math.max(0, advance);
    return advance * 4;
  }

  private final NewlineHandler[] newlineHandlers = new NewlineHandler[] {new BraceHandler()};

  @Override
  public boolean useTab() {
    return false;
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

  @Override
  public NewlineHandler[] getNewlineHandlers() {
    return newlineHandlers;
  }

  private static String getNonEmptyTextBefore(CharSequence text, int index, int length) {
    while (index > 0 && isWhitespace(text.charAt(index - 1))) index--;
    return text.subSequence(Math.max(0, index - length), index).toString();
  }

  private static String getNonEmptyTextAfter(CharSequence text, int index, int length) {
    while (index < text.length() && isWhitespace(text.charAt(index))) index++;
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
      var beforeText = line.subSequence(0, position.column).toString();
      var afterText = line.subSequence(position.column, line.length()).toString();
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
