package ir.hanzodev1375.ghostide.codeeditors.langs.yaml;

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
import io.github.rosemoe.sora.text.CharPosition;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.ContentReference;
import io.github.rosemoe.sora.text.TextUtils;
import io.github.rosemoe.sora.widget.SymbolPairMatch;

public class YamlLanguage implements Language {

  private static final CodeSnippet SNIPPET_MAPPING =
      CodeSnippetParser.parse("${1:key}:\n  ${2:nested_key}: ${3:value}");

  private static final CodeSnippet SNIPPET_LIST =
      CodeSnippetParser.parse("${1:key}:\n  - ${2:item1}\n  - ${3:item2}");

  private static final CodeSnippet SNIPPET_ANCHOR =
      CodeSnippetParser.parse("${1:anchor}: &${2:name}\n  ${3:key}: ${4:value}");

  private static final CodeSnippet SNIPPET_ALIAS =
      CodeSnippetParser.parse("${1:key}: *${2:anchorName}");

  private static final CodeSnippet SNIPPET_BLOCK_LITERAL =
      CodeSnippetParser.parse("${1:key}: |\n  ${2:line1}\n  ${3:line2}");

  private static final CodeSnippet SNIPPET_BLOCK_FOLDED =
      CodeSnippetParser.parse("${1:key}: >\n  ${2:paragraph text}\n  ${3:continues here}");

  private static final CodeSnippet SNIPPET_DOC =
      CodeSnippetParser.parse("---\n${1:key}: ${2:value}\n...");

  private static final CodeSnippet SNIPPET_GITHUB_ACTION =
      CodeSnippetParser.parse(
          "name: ${1:CI}\non:\n  push:\n    branches: [ ${2:main} ]\njobs:\n  ${3:build}:\n    runs-on: ubuntu-latest\n    steps:\n      - uses: actions/checkout@v3\n      - name: ${4:Run}\n        run: $0");

  private static final CodeSnippet SNIPPET_FLUTTER_PUBSPEC =
      CodeSnippetParser.parse(
          "name: ${1:app_name}\ndescription: ${2:A Flutter application}\nversion: 1.0.0+1\n\nenvironment:\n  sdk: '>=3.0.0 <4.0.0'\n\ndependencies:\n  flutter:\n    sdk: flutter\n  $0");

  private IdentifierAutoComplete autoComplete;
  private final YamlIncrementalAnalyzeManager manager;
  private final YamlQuoteHandler quoteHandler = new YamlQuoteHandler();

  private static final String[] KEYWORDS = {
    "true", "false", "yes", "no", "on", "off", "null",
  };

  public YamlLanguage() {
    manager = new YamlIncrementalAnalyzeManager();
    autoComplete = new IdentifierAutoComplete(KEYWORDS);
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

    CharSequence line = content.getLine(position.line);
    int col = Math.min(position.column, line.length());
    int start = col;
    while (start > 0 && !Character.isWhitespace(line.charAt(start - 1))) start--;
    String prefix = line.subSequence(start, col).toString();
    if (prefix.isEmpty()) return;

    final var idt = manager.identifiers;
    if (idt != null && autoComplete != null) {
      autoComplete.requireAutoComplete(content, position, prefix, publisher, idt);
    }

    offer(
        publisher, prefix, "mapping", "Snippet - Nested mapping", SNIPPET_MAPPING, prefix.length());
    offer(publisher, prefix, "list", "Snippet - List / sequence", SNIPPET_LIST, prefix.length());
    offer(
        publisher,
        prefix,
        "anchor",
        "Snippet - Anchor definition",
        SNIPPET_ANCHOR,
        prefix.length());
    offer(publisher, prefix, "alias", "Snippet - Alias reference", SNIPPET_ALIAS, prefix.length());
    offer(
        publisher,
        prefix,
        "literal",
        "Snippet - Block literal (|)",
        SNIPPET_BLOCK_LITERAL,
        prefix.length());
    offer(
        publisher,
        prefix,
        "folded",
        "Snippet - Block folded (>)",
        SNIPPET_BLOCK_FOLDED,
        prefix.length());
    offer(publisher, prefix, "---", "Snippet - Document", SNIPPET_DOC, prefix.length());
    offer(
        publisher,
        prefix,
        "action",
        "Snippet - GitHub Actions",
        SNIPPET_GITHUB_ACTION,
        prefix.length());
    offer(
        publisher,
        prefix,
        "pubspec",
        "Snippet - Flutter pubspec.yaml",
        SNIPPET_FLUTTER_PUBSPEC,
        prefix.length());
  }

  private static void offer(
      CompletionPublisher pub,
      String prefix,
      String trigger,
      String desc,
      CodeSnippet snippet,
      int prefixLen) {
    if (trigger.startsWith(prefix)) {
      pub.addItem(
          new SimpleSnippetCompletionItem(
              trigger, desc, new SnippetDescription(prefixLen, snippet, true)));
    }
  }

  @Override
  public int getIndentAdvance(@NonNull ContentReference text, int line, int column) {
    CharSequence lineText = text.getLine(line);
    String sub = lineText.subSequence(0, Math.min(column, lineText.length())).toString();

    String trimmed = sub.stripTrailing();
    if (trimmed.endsWith(":") || trimmed.endsWith(": |") || trimmed.endsWith(": >")) {
      return 2;
    }

    if (trimmed.stripLeading().startsWith("- ")) return 2;
    return 0;
  }

  private final NewlineHandler[] newlineHandlers =
      new NewlineHandler[] {new ListItemContinueHandler(), new MappingContinueHandler()};

  @Override
  public NewlineHandler[] getNewlineHandlers() {
    return newlineHandlers;
  }

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
    SymbolPairMatch pairs = new SymbolPairMatch();
    pairs.putPair('{', new SymbolPairMatch.SymbolPair("{", "}"));
    pairs.putPair('[', new SymbolPairMatch.SymbolPair("[", "]"));
    pairs.putPair('"', new SymbolPairMatch.SymbolPair("\"", "\""));
    pairs.putPair('\'', new SymbolPairMatch.SymbolPair("'", "'"));
    return pairs;
  }

  class ListItemContinueHandler implements NewlineHandler {
    private String detectedPrefix = "";

    @Override
    public boolean matchesRequirement(
        @NonNull Content text, @NonNull CharPosition position, @Nullable Styles style) {
      CharSequence line = text.getLine(position.line);
      String lineStr = line.toString();
      String trimmed = lineStr.stripLeading();
      int leading = lineStr.length() - trimmed.length();
      if (trimmed.startsWith("- ") && position.column > leading) {
        detectedPrefix = lineStr.substring(0, leading) + "- ";
        return true;
      }
      return false;
    }

    @NonNull
    @Override
    public NewlineHandleResult handleNewline(
        @NonNull Content text,
        @NonNull CharPosition position,
        @Nullable Styles style,
        int tabSize) {
      return new NewlineHandleResult("\n" + detectedPrefix, 0);
    }
  }

  class MappingContinueHandler implements NewlineHandler {
    private String detectedIndent = "";

    @Override
    public boolean matchesRequirement(
        @NonNull Content text, @NonNull CharPosition position, @Nullable Styles style) {
      CharSequence line = text.getLine(position.line);
      String lineStr = line.toString().stripTrailing();
      if (lineStr.endsWith(":")) {
        int leading = 0;
        while (leading < lineStr.length()
            && (lineStr.charAt(leading) == ' ' || lineStr.charAt(leading) == '\t')) leading++;
        detectedIndent = lineStr.substring(0, leading) + "  ";
        return true;
      }
      return false;
    }

    @NonNull
    @Override
    public NewlineHandleResult handleNewline(
        @NonNull Content text,
        @NonNull CharPosition position,
        @Nullable Styles style,
        int tabSize) {
      return new NewlineHandleResult("\n" + detectedIndent, 0);
    }
  }
}
