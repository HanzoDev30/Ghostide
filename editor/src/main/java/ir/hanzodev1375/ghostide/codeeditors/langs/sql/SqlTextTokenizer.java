/**
 * Comment by ghost ide
 *
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.sql;

import io.github.rosemoe.sora.util.TrieTree;

public class SqlTextTokenizer {

  private static TrieTree<SqlTokens> keywords;

  static {
    doStaticInit();
  }

  public static TrieTree<SqlTokens> getTree() {
    return keywords;
  }

  private CharSequence source;

  private int bufferLen;

  public int offset;

  public int length;

  private SqlTokens currToken;

  public SqlTextTokenizer(CharSequence src) {
    if (src == null) throw new IllegalArgumentException("src cannot be null");
    this.source = src;
    init();
  }

  private void init() {
    length = 0;
    offset = 0;
    currToken = SqlTokens.WHITESPACE;
    this.bufferLen = source.length();
  }

  public void reset(CharSequence src) {
    if (src == null) throw new IllegalArgumentException("src cannot be null");
    this.source = src;
    this.bufferLen = src.length();
    init();
  }

  public CharSequence getTokenText() {
    return source.subSequence(offset, offset + length);
  }

  public int getTokenLength() {
    return length;
  }

  public SqlTokens nextToken() {
    return currToken = nextTokenInternal();
  }

  private SqlTokens nextTokenInternal() {
    offset += length;
    if (offset >= bufferLen) return SqlTokens.EOF;
    char ch = source.charAt(offset);
    length = 1;
    if (ch == '\n') return SqlTokens.NEWLINE;
    if (ch == '\r') {
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '\n') length++;
      return SqlTokens.NEWLINE;
    }
    if (isWhitespace(ch)) {
      while (offset + length < bufferLen && isWhitespace(source.charAt(offset + length))) {
        length++;
      }
      return SqlTokens.WHITESPACE;
    }
    if (ch == '-' && offset + 1 < bufferLen && source.charAt(offset + 1) == '-') {
      while (offset + length < bufferLen && source.charAt(offset + length) != '\n') {
        length++;
      }
      return SqlTokens.LINE_COMMENT;
    }
    if (ch == '/' && offset + 1 < bufferLen && source.charAt(offset + 1) == '*') {
      length++;
      char pre = 0, cur = 0;
      boolean finished = false;
      while (offset + length < bufferLen) {
        pre = cur;
        cur = source.charAt(offset + length);
        if (pre == '*' && cur == '/') {
          length++;
          finished = true;
          break;
        }
        length++;
      }
      return finished ? SqlTokens.BLOCK_COMMENT_COMPLETE : SqlTokens.BLOCK_COMMENT_INCOMPLETE;
    }
    if (ch == '\'' || ch == '"') {
      scanStringLiteral(ch);
      return SqlTokens.STRING_LITERAL;
    }
    if (ch == '`') {
      scanBacktickIdentifier();
      return SqlTokens.IDENTIFIER;
    }
    if (isDigit(ch)
        || (ch == '.' && offset + 1 < bufferLen && isDigit(source.charAt(offset + 1)))) {
      return scanNumber();
    }
    if (isIdentifierStart(ch)) {
      return scanIdentifier(ch);
    }
    switch (ch) {
      case ';':
        return SqlTokens.SEMICOLON;
      case ':':
        return SqlTokens.COLON;
      case ',':
        return SqlTokens.COMMA;
      case '.':
        return SqlTokens.DOT;
      case '(':
        return SqlTokens.LPAREN;
      case ')':
        return SqlTokens.RPAREN;
      case '[':
        return SqlTokens.LBRACK;
      case ']':
        return SqlTokens.RBRACK;
      case '+':
        return SqlTokens.PLUS;
      case '-':
        return SqlTokens.MINUS;
      case '*':
        return SqlTokens.STAR;
      case '/':
        return SqlTokens.SLASH;
      case '%':
        return SqlTokens.PERCENT;
      case '=':
        return scanAssign();
      case '<':
        return scanLT();
      case '>':
        return scanGT();
      case '!':
        return scanNot();
      case '|':
        if (offset + 1 < bufferLen && source.charAt(offset + 1) == '|') {
          length++;
          return SqlTokens.CONCAT;
        }
        return SqlTokens.UNKNOWN;
      case '&':
        return SqlTokens.AND;
      case '~':
        return SqlTokens.NOT;
      default:
        return SqlTokens.UNKNOWN;
    }
  }

  private SqlTokens scanAssign() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return SqlTokens.EQ;
    }
    return SqlTokens.ASSIGN;
  }

  private SqlTokens scanLT() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '=') {
        length++;
        return SqlTokens.LT_EQ;
      }
      if (n == '>') {
        length++;
        return SqlTokens.NOT_EQ;
      }
    }
    return SqlTokens.LT;
  }

  private SqlTokens scanGT() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return SqlTokens.GT_EQ;
    }
    return SqlTokens.GT;
  }

  private SqlTokens scanNot() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return SqlTokens.NOT_EQ;
    }
    return SqlTokens.NOT;
  }

  private void scanStringLiteral(char quote) {
    while (offset + length < bufferLen) {
      char c = source.charAt(offset + length);
      if (c == quote) {
        length++;
        break;
      }
      if (c == '\\') {
        length++;
        if (offset + length < bufferLen) {
          char next = source.charAt(offset + length);
          if (next == 'n' || next == 'r' || next == 't' || next == '\\' || next == quote) {
            length++;
          }
        }
      } else {
        length++;
      }
    }
  }

  private void scanBacktickIdentifier() {
    while (offset + length < bufferLen) {
      char c = source.charAt(offset + length);
      if (c == '`') {
        length++;
        break;
      }
      length++;
    }
  }

  private SqlTokens scanNumber() {
    boolean isReal = false;
    while (offset + length < bufferLen && isDigit(source.charAt(offset + length))) length++;
    if (offset + length < bufferLen && source.charAt(offset + length) == '.') {
      isReal = true;
      length++;
      while (offset + length < bufferLen && isDigit(source.charAt(offset + length))) length++;
    }
    if (offset + length < bufferLen
        && (source.charAt(offset + length) == 'e' || source.charAt(offset + length) == 'E')) {
      isReal = true;
      length++;
      if (offset + length < bufferLen
          && (source.charAt(offset + length) == '+' || source.charAt(offset + length) == '-')) {
        length++;
      }
      while (offset + length < bufferLen && isDigit(source.charAt(offset + length))) length++;
    }
    return isReal ? SqlTokens.REAL_LITERAL : SqlTokens.INTEGER_LITERAL;
  }

  private SqlTokens scanIdentifier(char first) {
    StringBuilder sb = new StringBuilder();
    sb.append(first);
    while (offset + length < bufferLen && isIdentifierPart(source.charAt(offset + length))) {
      sb.append(source.charAt(offset + length));
      length++;
    }
    String word = sb.toString().toLowerCase();
    SqlTokens tok = keywords.get(word, 0, word.length());
    if (tok != null) {
      if (tok == SqlTokens.NULL) return SqlTokens.NULL_LITERAL;
      return tok;
    }
    return SqlTokens.IDENTIFIER;
  }

  private static boolean isWhitespace(char c) {
    return c == ' ' || c == '\t' || c == '\f';
  }

  private static boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private static boolean isIdentifierStart(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == '@' || c == '#';
  }

  private static boolean isIdentifierPart(char c) {
    return isIdentifierStart(c) || isDigit(c);
  }

  private static void doStaticInit() {
    String[] words = {
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
      "group_concat",
      "string_agg",
      "array_agg",
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
      "last_value",
      "materialized",
      "not materialized",
      "do",
      "for",
      "each",
      "statement",
      "row",
      "trigger",
      "before",
      "after",
      "instead of",
      "cascade",
      "restrict",
      "no action",
      "set null",
      "set default"
    };
    SqlTokens[] tokens = {
      SqlTokens.SELECT,
      SqlTokens.INSERT,
      SqlTokens.UPDATE,
      SqlTokens.DELETE,
      SqlTokens.CREATE,
      SqlTokens.DROP,
      SqlTokens.ALTER,
      SqlTokens.TABLE,
      SqlTokens.DATABASE,
      SqlTokens.INDEX,
      SqlTokens.VIEW,
      SqlTokens.PROCEDURE,
      SqlTokens.FUNCTION,
      SqlTokens.TRIGGER,
      SqlTokens.FROM,
      SqlTokens.WHERE,
      SqlTokens.GROUP_BY,
      SqlTokens.ORDER_BY,
      SqlTokens.HAVING,
      SqlTokens.JOIN,
      SqlTokens.INNER,
      SqlTokens.LEFT,
      SqlTokens.RIGHT,
      SqlTokens.FULL,
      SqlTokens.CROSS,
      SqlTokens.ON,
      SqlTokens.AS,
      SqlTokens.INTO,
      SqlTokens.VALUES,
      SqlTokens.SET,
      SqlTokens.DISTINCT,
      SqlTokens.ALL,
      SqlTokens.ANY,
      SqlTokens.EXISTS,
      SqlTokens.BETWEEN,
      SqlTokens.IN,
      SqlTokens.IS,
      SqlTokens.NULL,
      SqlTokens.NOT,
      SqlTokens.AND,
      SqlTokens.OR,
      SqlTokens.CASE,
      SqlTokens.WHEN,
      SqlTokens.THEN,
      SqlTokens.ELSE,
      SqlTokens.END,
      SqlTokens.UNION,
      SqlTokens.INTERSECT,
      SqlTokens.EXCEPT,
      SqlTokens.PRIMARY_KEY,
      SqlTokens.FOREIGN_KEY,
      SqlTokens.REFERENCES,
      SqlTokens.CONSTRAINT,
      SqlTokens.DEFAULT,
      SqlTokens.UNIQUE,
      SqlTokens.CHECK,
      SqlTokens.AUTO_INCREMENT,
      SqlTokens.SERIAL,
      SqlTokens.COUNT,
      SqlTokens.SUM,
      SqlTokens.AVG,
      SqlTokens.MAX,
      SqlTokens.MIN,
      SqlTokens.INT,
      SqlTokens.BIGINT,
      SqlTokens.SMALLINT,
      SqlTokens.TINYINT,
      SqlTokens.DECIMAL,
      SqlTokens.NUMERIC,
      SqlTokens.FLOAT,
      SqlTokens.DOUBLE,
      SqlTokens.CHAR,
      SqlTokens.VARCHAR,
      SqlTokens.TEXT,
      SqlTokens.NCHAR,
      SqlTokens.NVARCHAR,
      SqlTokens.DATE,
      SqlTokens.TIME,
      SqlTokens.DATETIME,
      SqlTokens.TIMESTAMP,
      SqlTokens.BOOLEAN,
      SqlTokens.BLOB,
      SqlTokens.LIMIT,
      SqlTokens.OFFSET,
      SqlTokens.FETCH,
      SqlTokens.NEXT,
      SqlTokens.ROWS,
      SqlTokens.ONLY,
      SqlTokens.WITH,
      SqlTokens.RECURSIVE,
      SqlTokens.OVER,
      SqlTokens.PARTITION_BY,
      SqlTokens.ROWS_BETWEEN,
      SqlTokens.UNBOUNDED,
      SqlTokens.PRECEDING,
      SqlTokens.FOLLOWING,
      SqlTokens.CURRENT_ROW,
      SqlTokens.GROUP_CONCAT,
      SqlTokens.STRING_AGG,
      SqlTokens.ARRAY_AGG,
      SqlTokens.BEGIN,
      SqlTokens.TRANSACTION,
      SqlTokens.COMMIT,
      SqlTokens.ROLLBACK,
      SqlTokens.SAVEPOINT,
      SqlTokens.CAST,
      SqlTokens.COALESCE,
      SqlTokens.NULLIF,
      SqlTokens.GREATEST,
      SqlTokens.LEAST,
      SqlTokens.EXTRACT,
      SqlTokens.DATE_PART,
      SqlTokens.DATE_TRUNC,
      SqlTokens.RANK,
      SqlTokens.DENSE_RANK,
      SqlTokens.ROW_NUMBER,
      SqlTokens.LAG,
      SqlTokens.LEAD,
      SqlTokens.FIRST_VALUE,
      SqlTokens.LAST_VALUE,
      SqlTokens.MATERIALIZED,
      SqlTokens.NOT_MATERIALIZED,
      SqlTokens.DO,
      SqlTokens.FOR,
      SqlTokens.EACH,
      SqlTokens.STATEMENT,
      SqlTokens.ROW,
      SqlTokens.TRIGGER,
      SqlTokens.BEFORE,
      SqlTokens.AFTER,
      SqlTokens.INSTEAD_OF,
      SqlTokens.CASCADE,
      SqlTokens.RESTRICT,
      SqlTokens.NO_ACTION,
      SqlTokens.SET_NULL,
      SqlTokens.SET_DEFAULT
    };
    keywords = new TrieTree<>();
    for (int i = 0; i < words.length; i++) {
      keywords.put(words[i], tokens[i]);
    }
  }
}
