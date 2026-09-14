/**
 * Comment by ghost ide
 *
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.sql;

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

public class SqlLanguage implements Language {

  private static final CodeSnippet SELECT_SNIPPET =
      CodeSnippetParser.parse("SELECT ${1:*}\nFROM ${2:table}\nWHERE ${3:condition};");

  private static final CodeSnippet INSERT_SNIPPET =
      CodeSnippetParser.parse("INSERT INTO ${1:table} (${2:columns})\nVALUES (${3:values});");

  private static final CodeSnippet UPDATE_SNIPPET =
      CodeSnippetParser.parse(
          "UPDATE ${1:table}\nSET ${2:column} = ${3:value}\nWHERE ${4:condition};");

  private static final CodeSnippet DELETE_SNIPPET =
      CodeSnippetParser.parse("DELETE FROM ${1:table}\nWHERE ${2:condition};");

  private static final CodeSnippet JOIN_SNIPPET =
      CodeSnippetParser.parse("SELECT *\nFROM ${1:table1}\nJOIN ${2:table2} ON ${3:condition};");

  private static final CodeSnippet CREATE_TABLE_SNIPPET =
      CodeSnippetParser.parse(
          "CREATE TABLE ${1:table_name} (\n    ${2:column1} ${3:type},\n    ${4:column2} ${5:type}\n);");

  private static final CodeSnippet WITH_SNIPPET =
      CodeSnippetParser.parse("WITH ${1:cte_name} AS (\n    ${2:SELECT ...}\n)\n${3:SELECT ...};");

  private static final CodeSnippet CASE_SNIPPET =
      CodeSnippetParser.parse(
          "CASE\n    WHEN ${1:condition1} THEN ${2:result1}\n    WHEN ${3:condition2} THEN ${4:result2}\n    ELSE ${5:default}\nEND");

  private static final String[] KEYWORDS = {
    "select",
    "insert",
    "update",
    "delete",
    "create",
    "drop",
    "alter",
    "table",
    "database",
    "index",
    "view",
    "procedure",
    "function",
    "trigger",
    "from",
    "where",
    "group by",
    "order by",
    "having",
    "join",
    "inner",
    "left",
    "right",
    "full",
    "cross",
    "on",
    "as",
    "into",
    "values",
    "set",
    "distinct",
    "all",
    "any",
    "exists",
    "between",
    "in",
    "is",
    "null",
    "not",
    "and",
    "or",
    "case",
    "when",
    "then",
    "else",
    "end",
    "union",
    "intersect",
    "except",
    "primary key",
    "foreign key",
    "references",
    "constraint",
    "default",
    "unique",
    "check",
    "auto_increment",
    "serial",
    "count",
    "sum",
    "avg",
    "max",
    "min",
    "int",
    "bigint",
    "smallint",
    "tinyint",
    "decimal",
    "numeric",
    "float",
    "double",
    "char",
    "varchar",
    "text",
    "nchar",
    "nvarchar",
    "date",
    "time",
    "datetime",
    "timestamp",
    "boolean",
    "blob",
    "limit",
    "offset",
    "fetch",
    "next",
    "rows",
    "only",
    "with",
    "recursive",
    "over",
    "partition by",
    "rows between",
    "unbounded",
    "preceding",
    "following",
    "current row",
    "begin",
    "transaction",
    "commit",
    "rollback",
    "savepoint",
    "cast",
    "coalesce",
    "nullif",
    "greatest",
    "least",
    "extract",
    "date_part",
    "date_trunc",
    "rank",
    "dense_rank",
    "row_number",
    "lag",
    "lead",
    "first_value",
    "last_value"
  };

  private IdentifierAutoComplete autoComplete;

  private final SqlIncrementalAnalyzeManager manager;

  private final SqlQuoteHandler quoteHandler = new SqlQuoteHandler();

  public SqlLanguage() {
    autoComplete = new IdentifierAutoComplete(KEYWORDS);
    manager = new SqlIncrementalAnalyzeManager();
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
    if ("sel".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "sel",
              "Snippet - SELECT",
              new SnippetDescription(prefix.length(), SELECT_SNIPPET, true)));
    if ("ins".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "ins",
              "Snippet - INSERT",
              new SnippetDescription(prefix.length(), INSERT_SNIPPET, true)));
    if ("upd".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "upd",
              "Snippet - UPDATE",
              new SnippetDescription(prefix.length(), UPDATE_SNIPPET, true)));
    if ("del".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "del",
              "Snippet - DELETE",
              new SnippetDescription(prefix.length(), DELETE_SNIPPET, true)));
    if ("join".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "join",
              "Snippet - JOIN",
              new SnippetDescription(prefix.length(), JOIN_SNIPPET, true)));
    if ("createt".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "createt",
              "Snippet - CREATE TABLE",
              new SnippetDescription(prefix.length(), CREATE_TABLE_SNIPPET, true)));
    if ("with".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "with",
              "Snippet - WITH CTE",
              new SnippetDescription(prefix.length(), WITH_SNIPPET, true)));
    if ("case".startsWith(prefix) && !prefix.isEmpty())
      publisher.addItem(
          new SimpleSnippetCompletionItem(
              "case",
              "Snippet - CASE",
              new SnippetDescription(prefix.length(), CASE_SNIPPET, true)));
  }

  @Override
  public int getIndentAdvance(@NonNull ContentReference text, int line, int column) {
    var content = text.getLine(line).substring(0, column);
    return getIndentAdvance(content);
  }

  private int getIndentAdvance(String content) {
    var t = new SqlTextTokenizer(content);
    SqlTokens token;
    int advance = 0;
    while ((token = t.nextToken()) != SqlTokens.EOF) {
      if (token == SqlTokens.LBRACK) advance++;
    }
    return Math.max(0, advance) * 4;
  }

  private final NewlineHandler[] newlineHandlers = new NewlineHandler[] {new ParenthesisHandler()};

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

  class ParenthesisHandler implements NewlineHandler {

    @Override
    public boolean matchesRequirement(
        @NonNull Content text, @NonNull CharPosition position, @Nullable Styles style) {
      var line = text.getLine(position.line);
      return !StylesUtils.checkNoCompletion(style, position)
          && getNonEmptyTextBefore(line, position.column, 1).equals("(")
          && getNonEmptyTextAfter(line, position.column, 1).equals(")");
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
