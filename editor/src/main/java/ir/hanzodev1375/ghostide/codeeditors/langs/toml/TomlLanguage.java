package ir.hanzodev1375.ghostide.codeeditors.langs.toml;

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
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.ContentReference;
import io.github.rosemoe.sora.util.MyCharacter;
import io.github.rosemoe.sora.widget.SymbolPairMatch;

public class TomlLanguage implements Language {

  private static final CodeSnippet TABLE_SNIPPET =
      CodeSnippetParser.parse("[${1:table}]\n${2:key} = ${3:value}\n$0");
  private static final CodeSnippet ARRAY_TABLE_SNIPPET =
      CodeSnippetParser.parse("[[${1:array}]]\n${2:key} = ${3:value}\n$0");
  private static final CodeSnippet INLINE_TABLE_SNIPPET =
      CodeSnippetParser.parse("${1:key} = { ${2:field} = ${3:value} }");

  private IdentifierAutoComplete autoComplete;
  private final TomlIncrementalAnalyzeManager manager;
  private final TomlQuoteHandler quoteHandler = new TomlQuoteHandler();

  private static final String[] KEYWORDS = {"true", "false", "inf", "nan"};

  public TomlLanguage() {
    autoComplete = new IdentifierAutoComplete(KEYWORDS);
    manager = new TomlIncrementalAnalyzeManager();
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
    if (autoComplete != null) {
      // No identifier tracking for TOML (it's config, not code)
    }
    // Snippets
    if ("table".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "table",
              "Snippet - Table header",
              new SnippetDescription(prefix.length(), TABLE_SNIPPET, true)));
    if ("array".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "array",
              "Snippet - Array of tables",
              new SnippetDescription(prefix.length(), ARRAY_TABLE_SNIPPET, true)));
    if ("inline".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "inline",
              "Snippet - Inline table",
              new SnippetDescription(prefix.length(), INLINE_TABLE_SNIPPET, true)));
  }

  @Override
  public int getIndentAdvance(@NonNull ContentReference text, int line, int column) {
    // TOML does not use indentation semantics
    return 0;
  }

  private final NewlineHandler[] newlineHandlers = new NewlineHandler[0];

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
}
