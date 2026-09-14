/**
 * Comment by ghost ide
 *
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.csharp;

import io.github.rosemoe.sora.util.TrieTree;

public class CSharpTextTokenizer {

  private static TrieTree<CSharpTokens> keywords;

  static {
    doStaticInit();
  }

  public static TrieTree<CSharpTokens> getTree() {
    return keywords;
  }

  private CharSequence source;

  private int bufferLen;

  private int index;

  public int offset;

  public int length;

  private CSharpTokens currToken;

  public CSharpTextTokenizer(CharSequence src) {
    if (src == null) throw new IllegalArgumentException("src cannot be null");
    this.source = src;
    init();
  }

  private void init() {
    length = 0;
    index = 0;
    offset = 0;
    currToken = CSharpTokens.WHITESPACE;
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

  public CSharpTokens nextToken() {
    return currToken = nextTokenInternal();
  }

  private CSharpTokens nextTokenInternal() {
    index += length;
    offset += length;
    if (offset >= bufferLen) return CSharpTokens.EOF;
    char ch = source.charAt(offset);
    length = 1;
    if (ch == '\n') return CSharpTokens.NEWLINE;
    if (ch == '\r') {
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '\n') length++;
      return CSharpTokens.NEWLINE;
    }
    if (isWhitespace(ch)) {
      while (offset + length < bufferLen && isWhitespace(source.charAt(offset + length))) {
        length++;
      }
      return CSharpTokens.WHITESPACE;
    }
    // کامنت خطی //
    if (ch == '/' && offset + 1 < bufferLen && source.charAt(offset + 1) == '/') {
      length++;
      if (offset + 2 < bufferLen && source.charAt(offset + 2) == '/') {
        length++;
        while (offset + length < bufferLen && source.charAt(offset + length) != '\n') {
          length++;
        }
        return CSharpTokens.LINE_COMMENT_DOC;
      }
      while (offset + length < bufferLen && source.charAt(offset + length) != '\n') {
        length++;
      }
      return CSharpTokens.LINE_COMMENT;
    }
    // کامنت بلوکی /* */
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
      return finished ? CSharpTokens.BLOCK_COMMENT_COMPLETE : CSharpTokens.BLOCK_COMMENT_INCOMPLETE;
    }
    // رشته‌های Verbatim @"..." و رشته‌های معمولی
    if (ch == '@' && offset + 1 < bufferLen && source.charAt(offset + 1) == '"') {
      length++;
      while (offset + length < bufferLen) {
        if (source.charAt(offset + length) == '"') {
          length++;
          if (offset + length < bufferLen && source.charAt(offset + length) == '"') {
            // دو تا " پشت سر هم یعنی یک "
            length++;
          } else {
            break;
          }
        } else {
          length++;
        }
      }
      return CSharpTokens.STRING_LITERAL;
    }
    // رشته با $"" (اینترپولیشن) - ساده‌سازی شده
    if (ch == '$' && offset + 1 < bufferLen && source.charAt(offset + 1) == '"') {
      length++;
      scanStringLiteral('"');
      return CSharpTokens.STRING_LITERAL;
    }
    if (ch == '"') {
      scanStringLiteral(ch);
      return CSharpTokens.STRING_LITERAL;
    }
    if (ch == '\'') {
      scanCharLiteral();
      return CSharpTokens.CHAR_LITERAL;
    }
    // عدد
    if (isDigit(ch)
        || (ch == '.' && offset + 1 < bufferLen && isDigit(source.charAt(offset + 1)))) {
      return scanNumber();
    }
    // شناسه یا کلیدواژه (می‌تواند با @ شروع شود)
    if (ch == '@' && offset + 1 < bufferLen && isIdentifierStart(source.charAt(offset + 1))) {
      length++;
      return scanIdentifier(source.charAt(offset + 1));
    }
    if (isIdentifierStart(ch)) {
      return scanIdentifier(ch);
    }
    // عملگرها
    switch (ch) {
      case '{':
        return CSharpTokens.LBRACE;
      case '}':
        return CSharpTokens.RBRACE;
      case '(':
        return CSharpTokens.LPAREN;
      case ')':
        return CSharpTokens.RPAREN;
      case '[':
        return CSharpTokens.LBRACK;
      case ']':
        return CSharpTokens.RBRACK;
      case ';':
        return CSharpTokens.SEMICOLON;
      case ':':
        if (offset + 1 < bufferLen && source.charAt(offset + 1) == ':') {
          length++;
          return CSharpTokens.NAMESPACE_SEP;
        }
        return CSharpTokens.COLON;
      case ',':
        return CSharpTokens.COMMA;
      case '.':
        return scanDot();
      case '?':
        return scanQuestion();
      case '+':
        return scanPlus();
      case '-':
        return scanMinus();
      case '*':
        return scanStar();
      case '/':
        return CSharpTokens.SLASH;
      case '%':
        return scanPercent();
      case '&':
        return scanAnd();
      case '|':
        return scanOr();
      case '^':
        return scanXor();
      case '~':
        return CSharpTokens.TILDE;
      case '=':
        return scanAssign();
      case '<':
        return scanLT();
      case '>':
        return scanGT();
      case '!':
        return scanNot();
      default:
        return CSharpTokens.UNKNOWN;
    }
  }

  // --- اسکن عملگرها ---
  private CSharpTokens scanDot() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '.') {
        length++;
        if (offset + 2 < bufferLen && source.charAt(offset + 2) == '.') {
          length++;
          return CSharpTokens.RANGE;
        }
        return CSharpTokens.ELLIPSIS;
      }
    }
    return CSharpTokens.DOT;
  }

  private CSharpTokens scanQuestion() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '?') {
        length++;
        if (offset + 2 < bufferLen && source.charAt(offset + 2) == '=') {
          length++;
          return CSharpTokens.NULL_COALESCING_ASSIGN;
        }
        return CSharpTokens.NULL_COALESCING;
      }
      if (n == '.') {
        length++;
        return CSharpTokens.MEMBER_ACCESS;
      }
      if (n == '[') {
        length++;
        return CSharpTokens.INDEX_ACCESS;
      }
    }
    return CSharpTokens.QUESTION;
  }

  private CSharpTokens scanPlus() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '+') {
        length++;
        return CSharpTokens.INC;
      }
      if (n == '=') {
        length++;
        return CSharpTokens.PLUS_ASSIGN;
      }
    }
    return CSharpTokens.PLUS;
  }

  private CSharpTokens scanMinus() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '-') {
        length++;
        return CSharpTokens.DEC;
      }
      if (n == '=') {
        length++;
        return CSharpTokens.MINUS_ASSIGN;
      }
      if (n == '>') {
        length++;
        return CSharpTokens.ARROW;
      }
    }
    return CSharpTokens.MINUS;
  }

  private CSharpTokens scanStar() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return CSharpTokens.STAR_ASSIGN;
    }
    return CSharpTokens.STAR;
  }

  private CSharpTokens scanPercent() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return CSharpTokens.PERCENT_ASSIGN;
    }
    return CSharpTokens.PERCENT;
  }

  private CSharpTokens scanAnd() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '&') {
        length++;
        return CSharpTokens.LOGICAL_AND;
      }
      if (n == '=') {
        length++;
        return CSharpTokens.AND_ASSIGN;
      }
    }
    return CSharpTokens.AMPERSAND;
  }

  private CSharpTokens scanOr() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '|') {
        length++;
        return CSharpTokens.LOGICAL_OR;
      }
      if (n == '=') {
        length++;
        return CSharpTokens.OR_ASSIGN;
      }
    }
    return CSharpTokens.PIPE;
  }

  private CSharpTokens scanXor() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return CSharpTokens.XOR_ASSIGN;
    }
    return CSharpTokens.CARET;
  }

  private CSharpTokens scanAssign() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '=') {
        length++;
        return CSharpTokens.EQ;
      }
      if (n == '>') {
        length++;
        return CSharpTokens.LAMBDA_ARROW;
      }
    }
    return CSharpTokens.ASSIGN;
  }

  private CSharpTokens scanLT() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '=') {
        length++;
        return CSharpTokens.LT_EQ;
      }
      if (n == '<') {
        length++;
        return CSharpTokens.LT;
      }
    }
    return CSharpTokens.LT;
  }

  private CSharpTokens scanGT() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '=') {
        length++;
        return CSharpTokens.GT_EQ;
      }
      if (n == '>') {
        length++;
        return CSharpTokens.GT;
      }
    }
    return CSharpTokens.GT;
  }

  private CSharpTokens scanNot() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return CSharpTokens.NOT_EQ;
    }
    return CSharpTokens.NOT;
  }

  // --- اسکن رشته و کاراکتر ---
  private void scanStringLiteral(char quote) {
    while (offset + length < bufferLen) {
      char c = source.charAt(offset + length);
      if (c == quote) {
        length++;
        break;
      }
      if (c == '\\') {
        length++;
        scanEscape();
      } else {
        length++;
      }
    }
  }

  private void scanCharLiteral() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) != '\'') {
      length++;
      if (source.charAt(offset) == '\\') {
        scanEscape();
      }
    }
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '\'') {
      length++;
    }
  }

  private void scanEscape() {
    if (offset + length < bufferLen) {
      char c = source.charAt(offset + length);
      if (c == 'n'
          || c == 'r'
          || c == 't'
          || c == 'v'
          || c == 'e'
          || c == 'f'
          || c == '\\'
          || c == '"'
          || c == '\''
          || c == 'x'
          || isDigit(c)) {
        length++;
        if (c == 'x') {
          while (offset + length < bufferLen && isHexDigit(source.charAt(offset + length)))
            length++;
        } else if (isDigit(c)) {
          while (offset + length < bufferLen && isDigit(source.charAt(offset + length))) length++;
        }
      }
    }
  }

  // --- اسکن عدد ---
  private CSharpTokens scanNumber() {
    boolean isReal = false;
    if (offset + 1 < bufferLen && source.charAt(offset) == '0') {
      char next = source.charAt(offset + 1);
      if (next == 'x' || next == 'X') {
        length++;
        while (offset + length < bufferLen && isHexDigit(source.charAt(offset + length))) length++;
        return CSharpTokens.INTEGER_LITERAL;
      }
      if (next == 'b' || next == 'B') {
        length++;
        while (offset + length < bufferLen
            && (source.charAt(offset + length) == '0' || source.charAt(offset + length) == '1'))
          length++;
        return CSharpTokens.INTEGER_LITERAL;
      }
    }
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
          && (source.charAt(offset + length) == '+' || source.charAt(offset + length) == '-'))
        length++;
      while (offset + length < bufferLen && isDigit(source.charAt(offset + length))) length++;
    }
    if (offset + length < bufferLen) {
      char suffix = source.charAt(offset + length);
      if (suffix == 'f'
          || suffix == 'F'
          || suffix == 'd'
          || suffix == 'D'
          || suffix == 'm'
          || suffix == 'M') {
        isReal = true;
        length++;
      }
    }
    return isReal ? CSharpTokens.REAL_LITERAL : CSharpTokens.INTEGER_LITERAL;
  }

  // --- اسکن شناسه و کلیدواژه ---
  private CSharpTokens scanIdentifier(char first) {
    TrieTree.Node<CSharpTokens> node = keywords.root.map.get(first);
    while (offset + length < bufferLen && isIdentifierPart(source.charAt(offset + length))) {
      char c = source.charAt(offset + length);
      node = (node == null) ? null : node.map.get(c);
      length++;
    }
    if (node != null && node.token != null) {
      CSharpTokens tok = node.token;
      if (tok == CSharpTokens.TRUE || tok == CSharpTokens.FALSE)
        return CSharpTokens.BOOLEAN_LITERAL;
      if (tok == CSharpTokens.NULL) return CSharpTokens.NULL_LITERAL;
      return tok;
    }
    return CSharpTokens.IDENTIFIER;
  }

  // --- متدهای کمکی ---
  private static boolean isWhitespace(char c) {
    return c == ' ' || c == '\t' || c == '\f';
  }

  private static boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private static boolean isHexDigit(char c) {
    return isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
  }

  private static boolean isIdentifierStart(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == '@';
  }

  private static boolean isIdentifierPart(char c) {
    return isIdentifierStart(c) || isDigit(c);
  }

  // --- مقداردهی Trie ---
  private static void doStaticInit() {
    String[] words = {
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
      "add",
      "alias",
      "ascending",
      "async",
      "await",
      "by",
      "descending",
      "dynamic",
      "equals",
      "from",
      "get",
      "global",
      "group",
      "into",
      "join",
      "let",
      "nameof",
      "notnull",
      "on",
      "orderby",
      "partial",
      "remove",
      "select",
      "set",
      "unmanaged",
      "value",
      "var",
      "when",
      "where",
      "with",
      "yield"
    };
    CSharpTokens[] tokens = {
      CSharpTokens.ABSTRACT,
      CSharpTokens.AS,
      CSharpTokens.BASE,
      CSharpTokens.BOOL,
      CSharpTokens.BREAK,
      CSharpTokens.BYTE,
      CSharpTokens.CASE,
      CSharpTokens.CATCH,
      CSharpTokens.CHAR,
      CSharpTokens.CHECKED,
      CSharpTokens.CLASS,
      CSharpTokens.CONST,
      CSharpTokens.CONTINUE,
      CSharpTokens.DECIMAL,
      CSharpTokens.DEFAULT,
      CSharpTokens.DELEGATE,
      CSharpTokens.DO,
      CSharpTokens.DOUBLE,
      CSharpTokens.ELSE,
      CSharpTokens.ENUM,
      CSharpTokens.EVENT,
      CSharpTokens.EXPLICIT,
      CSharpTokens.EXTERN,
      CSharpTokens.FALSE,
      CSharpTokens.FINALLY,
      CSharpTokens.FIXED,
      CSharpTokens.FLOAT,
      CSharpTokens.FOR,
      CSharpTokens.FOREACH,
      CSharpTokens.GOTO,
      CSharpTokens.IF,
      CSharpTokens.IMPLICIT,
      CSharpTokens.IN,
      CSharpTokens.INT,
      CSharpTokens.INTERFACE,
      CSharpTokens.INTERNAL,
      CSharpTokens.IS,
      CSharpTokens.LOCK,
      CSharpTokens.LONG,
      CSharpTokens.NAMESPACE,
      CSharpTokens.NEW,
      CSharpTokens.NULL,
      CSharpTokens.OBJECT,
      CSharpTokens.OPERATOR,
      CSharpTokens.OUT,
      CSharpTokens.OVERRIDE,
      CSharpTokens.PARAMS,
      CSharpTokens.PRIVATE,
      CSharpTokens.PROTECTED,
      CSharpTokens.PUBLIC,
      CSharpTokens.READONLY,
      CSharpTokens.REF,
      CSharpTokens.RETURN,
      CSharpTokens.SBYTE,
      CSharpTokens.SEALED,
      CSharpTokens.SHORT,
      CSharpTokens.SIZEOF,
      CSharpTokens.STACKALLOC,
      CSharpTokens.STATIC,
      CSharpTokens.STRING,
      CSharpTokens.STRUCT,
      CSharpTokens.SWITCH,
      CSharpTokens.THIS,
      CSharpTokens.THROW,
      CSharpTokens.TRUE,
      CSharpTokens.TRY,
      CSharpTokens.TYPEOF,
      CSharpTokens.UINT,
      CSharpTokens.ULONG,
      CSharpTokens.UNCHECKED,
      CSharpTokens.UNSAFE,
      CSharpTokens.USHORT,
      CSharpTokens.USING,
      CSharpTokens.VIRTUAL,
      CSharpTokens.VOID,
      CSharpTokens.VOLATILE,
      CSharpTokens.WHILE,
      CSharpTokens.ADD,
      CSharpTokens.ALIAS,
      CSharpTokens.ASCENDING,
      CSharpTokens.ASYNC,
      CSharpTokens.AWAIT,
      CSharpTokens.BY,
      CSharpTokens.DESCENDING,
      CSharpTokens.DYNAMIC,
      CSharpTokens.EQUALS,
      CSharpTokens.FROM,
      CSharpTokens.GET,
      CSharpTokens.GLOBAL,
      CSharpTokens.GROUP,
      CSharpTokens.INTO,
      CSharpTokens.JOIN,
      CSharpTokens.LET,
      CSharpTokens.NAMEOF,
      CSharpTokens.NOTNULL,
      CSharpTokens.ON,
      CSharpTokens.ORDERBY,
      CSharpTokens.PARTIAL,
      CSharpTokens.REMOVE,
      CSharpTokens.SELECT,
      CSharpTokens.SET,
      CSharpTokens.UNMANAGED,
      CSharpTokens.VALUE,
      CSharpTokens.VAR,
      CSharpTokens.WHEN,
      CSharpTokens.WHERE,
      CSharpTokens.WITH,
      CSharpTokens.YIELD
    };
    keywords = new TrieTree<>();
    for (int i = 0; i < words.length; i++) {
      keywords.put(words[i], tokens[i]);
    }
  }
}
