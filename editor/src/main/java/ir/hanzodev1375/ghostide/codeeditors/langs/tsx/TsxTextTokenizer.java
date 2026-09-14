/**
 * Comment by ghost ide
 *
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.tsx;

import io.github.rosemoe.sora.util.TrieTree;

public class TsxTextTokenizer {

  private static TrieTree<TsxTokens> keywords;

  static {
    doStaticInit();
  }

  public static TrieTree<TsxTokens> getTree() {
    return keywords;
  }

  private CharSequence source;

  private int bufferLen;

  public int offset;

  public int length;

  private TsxTokens currToken;

  private boolean insideJsx;

  public TsxTextTokenizer(CharSequence src) {
    if (src == null) throw new IllegalArgumentException("src cannot be null");
    this.source = src;
    init();
  }

  private void init() {
    length = 0;
    offset = 0;
    currToken = TsxTokens.WHITESPACE;
    this.bufferLen = source.length();
    insideJsx = false;
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

  public TsxTokens nextToken() {
    return currToken = nextTokenInternal();
  }

  private TsxTokens nextTokenInternal() {
    offset += length;
    if (offset >= bufferLen) return TsxTokens.EOF;
    char ch = source.charAt(offset);
    length = 1;
    if (ch == '\n') return TsxTokens.NEWLINE;
    if (ch == '\r') {
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '\n') length++;
      return TsxTokens.NEWLINE;
    }
    if (isWhitespace(ch)) {
      while (offset + length < bufferLen && isWhitespace(source.charAt(offset + length))) {
        length++;
      }
      return TsxTokens.WHITESPACE;
    }

    if (ch == '/' && offset + 1 < bufferLen && source.charAt(offset + 1) == '/') {
      while (offset + length < bufferLen && source.charAt(offset + length) != '\n') {
        length++;
      }
      return TsxTokens.LINE_COMMENT;
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
      return finished ? TsxTokens.BLOCK_COMMENT_COMPLETE : TsxTokens.BLOCK_COMMENT_INCOMPLETE;
    }

    if (ch == '"' || ch == '\'') {
      scanStringLiteral(ch);
      return TsxTokens.STRING_LITERAL;
    }
    if(ch == '`') {
    	scanStringLiteral(ch);
      return TsxTokens.BACKTIK_LIERAL;
    }

    if (ch == '<') {
      return scanJsxTag();
    }

    if (ch == '{') {
      insideJsx = false;
      return TsxTokens.LBRACE;
    }
    if (ch == '}') {
      insideJsx = false;
      return TsxTokens.RBRACE;
    }

    if (isDigit(ch)
        || (ch == '.' && offset + 1 < bufferLen && isDigit(source.charAt(offset + 1)))) {
      return scanNumber();
    }

    if (isIdentifierStart(ch)) {
      return scanIdentifier(ch);
    }

    switch (ch) {
      case '(':
        return TsxTokens.LPAREN;
      case ')':
        return TsxTokens.RPAREN;
      case '{':
        return TsxTokens.LBRACE;
      case '}':
        return TsxTokens.RBRACE;
      case '[':
        return TsxTokens.LBRACK;
      case ']':
        return TsxTokens.RBRACK;
      case ';':
        return TsxTokens.SEMICOLON;
      case ':':
        return scanColon();
      case ',':
        return TsxTokens.COMMA;
      case '.':
        return scanDot();
      case '@':
        return TsxTokens.AT;
      case '+':
        return scanPlus();
      case '-':
        return scanMinus();
      case '*':
        return scanStar();
      case '/':
        return TsxTokens.SLASH;
      case '%':
        return scanPercent();
      case '&':
        return scanAnd();
      case '|':
        return scanOr();
      case '^':
        return scanXor();
      case '~':
        return TsxTokens.TILDE;
      case '=':
        return scanAssign();
      case '<':
        return scanLT();
      case '>':
        return scanGT();
      case '!':
        return scanNot();
      case '?':
        return scanQuestion();
      default:
        return TsxTokens.UNKNOWN;
    }
  }

  private TsxTokens scanJsxTag() {
    insideJsx = true;
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '/') {
      length++;
      if (offset + 2 < bufferLen && source.charAt(offset + 2) == '>') {
        length++;

        return TsxTokens.JSX_TAG_CLOSE;
      }

      return TsxTokens.JSX_TAG_OPEN_SLASH;
    }
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '>') {
      length++;

      return TsxTokens.JSX_TAG_CLOSE;
    }
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '?') {

      return TsxTokens.UNKNOWN;
    }

    return TsxTokens.JSX_TAG_OPEN;
  }

  private TsxTokens scanColon() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == ':') {
      length++;
      return TsxTokens.DOUBLE_COLON;
    }
    return TsxTokens.COLON;
  }

  private TsxTokens scanDot() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '.') {
      length++;
      if (offset + 2 < bufferLen && source.charAt(offset + 2) == '.') {
        length++;
        return TsxTokens.ELLIPSIS;
      }
      return TsxTokens.DOT;
    }
    return TsxTokens.DOT;
  }

  private TsxTokens scanPlus() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '+') {
        length++;
        return TsxTokens.INC;
      }
      if (n == '=') {
        length++;
        return TsxTokens.PLUS_ASSIGN;
      }
    }
    return TsxTokens.PLUS;
  }

  private TsxTokens scanMinus() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '-') {
        length++;
        return TsxTokens.DEC;
      }
      if (n == '=') {
        length++;
        return TsxTokens.MINUS_ASSIGN;
      }
      if (n == '>') {
        length++;
        return TsxTokens.ARROW;
      }
    }
    return TsxTokens.MINUS;
  }

  private TsxTokens scanStar() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return TsxTokens.STAR_ASSIGN;
    }
    return TsxTokens.STAR;
  }

  private TsxTokens scanPercent() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return TsxTokens.PERCENT_ASSIGN;
    }
    return TsxTokens.PERCENT;
  }

  private TsxTokens scanAnd() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '&') {
        length++;
        return TsxTokens.LOGICAL_AND;
      }
      if (n == '=') {
        length++;
        return TsxTokens.AND_ASSIGN;
      }
    }
    return TsxTokens.AMPERSAND;
  }

  private TsxTokens scanOr() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '|') {
        length++;
        return TsxTokens.LOGICAL_OR;
      }
      if (n == '=') {
        length++;
        return TsxTokens.OR_ASSIGN;
      }
    }
    return TsxTokens.PIPE;
  }

  private TsxTokens scanXor() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return TsxTokens.XOR_ASSIGN;
    }
    return TsxTokens.CARET;
  }

  private TsxTokens scanAssign() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return TsxTokens.EQ;
    }
    return TsxTokens.ASSIGN;
  }

  private TsxTokens scanLT() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '<') {
        length++;
        if (offset + 2 < bufferLen && source.charAt(offset + 2) == '=') {
          length++;
          return TsxTokens.LT_EQ;
        }
        return TsxTokens.SHIFT_LEFT;
      }
      if (n == '=') {
        length++;
        return TsxTokens.LT_EQ;
      }
    }
    return TsxTokens.LT;
  }

  private TsxTokens scanGT() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '>') {
        length++;
        if (offset + 2 < bufferLen && source.charAt(offset + 2) == '=') {
          length++;
          return TsxTokens.GT_EQ;
        }
        return TsxTokens.SHIFT_RIGHT;
      }
      if (n == '=') {
        length++;
        return TsxTokens.GT_EQ;
      }
    }
    return TsxTokens.GT;
  }

  private TsxTokens scanNot() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '=') {
        length++;
        return TsxTokens.NOT_EQ;
      }
      if (n == '=') {
        length++;
        return TsxTokens.NOT_EQ;
      }
    }
    return TsxTokens.NON_NULL_ASSERT;
  }

  private TsxTokens scanQuestion() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '?') {
        length++;
        if (offset + 2 < bufferLen && source.charAt(offset + 2) == '=') {
          length++;
          return TsxTokens.NULL_COALESCING_ASSIGN;
        }
        return TsxTokens.NULL_COALESCING;
      }
      if (n == '.') {
        length++;
        return TsxTokens.OPTIONAL_CHAINING;
      }
    }
    return TsxTokens.QUESTION;
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
        scanEscape();
      } else {
        length++;
      }
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

  private TsxTokens scanNumber() {
    boolean isFloat = false;
    if (offset + 1 < bufferLen && source.charAt(offset) == '0') {
      char next = source.charAt(offset + 1);
      if (next == 'x' || next == 'X') {
        length++;
        while (offset + length < bufferLen && isHexDigit(source.charAt(offset + length))) length++;
        return TsxTokens.INTEGER_LITERAL;
      }
      if (next == 'b' || next == 'B') {
        length++;
        while (offset + length < bufferLen
            && (source.charAt(offset + length) == '0' || source.charAt(offset + length) == '1'))
          length++;
        return TsxTokens.INTEGER_LITERAL;
      }
    }
    while (offset + length < bufferLen && isDigit(source.charAt(offset + length))) length++;
    if (offset + length < bufferLen && source.charAt(offset + length) == '.') {
      isFloat = true;
      length++;
      while (offset + length < bufferLen && isDigit(source.charAt(offset + length))) length++;
    }
    if (offset + length < bufferLen
        && (source.charAt(offset + length) == 'e' || source.charAt(offset + length) == 'E')) {
      isFloat = true;
      length++;
      if (offset + length < bufferLen
          && (source.charAt(offset + length) == '+' || source.charAt(offset + length) == '-'))
        length++;
      while (offset + length < bufferLen && isDigit(source.charAt(offset + length))) length++;
    }
    return isFloat ? TsxTokens.FLOATING_LITERAL : TsxTokens.INTEGER_LITERAL;
  }

  private TsxTokens scanIdentifier(char first) {
    TrieTree.Node<TsxTokens> node = keywords.root.map.get(first);
    while (offset + length < bufferLen && isIdentifierPart(source.charAt(offset + length))) {
      char c = source.charAt(offset + length);
      node = (node == null) ? null : node.map.get(c);
      length++;
    }
    if (node != null && node.token != null) {
      TsxTokens tok = node.token;
      if (tok == TsxTokens.TRUE || tok == TsxTokens.FALSE) return TsxTokens.BOOLEAN_LITERAL;
      if (tok == TsxTokens.NULL) return TsxTokens.NULL_LITERAL;
      if (insideJsx) {

        return TsxTokens.JSX_IDENTIFIER;
      }
      return tok;
    }
    return insideJsx ? TsxTokens.JSX_IDENTIFIER : TsxTokens.IDENTIFIER;
  }

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
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == '$';
  }

  private static boolean isIdentifierPart(char c) {
    return isIdentifierStart(c) || isDigit(c);
  }

  private static void doStaticInit() {
    String[] words = {
      "abstract",
      "as",
      "asserts",
      "any",
      "boolean",
      "break",
      "case",
      "catch",
      "class",
      "const",
      "continue",
      "debugger",
      "declare",
      "default",
      "delete",
      "do",
      "else",
      "enum",
      "export",
      "extends",
      "false",
      "finally",
      "for",
      "from",
      "function",
      "get",
      "if",
      "implements",
      "import",
      "in",
      "infer",
      "instanceof",
      "interface",
      "is",
      "keyof",
      "let",
      "module",
      "namespace",
      "never",
      "new",
      "null",
      "number",
      "object",
      "package",
      "private",
      "protected",
      "public",
      "readonly",
      "require",
      "return",
      "set",
      "static",
      "string",
      "super",
      "switch",
      "symbol",
      "this",
      "throw",
      "true",
      "try",
      "type",
      "typeof",
      "undefined",
      "unknown",
      "var",
      "void",
      "while",
      "with",
      "yield"
    };
    TsxTokens[] tokens = {
      TsxTokens.ABSTRACT,
      TsxTokens.AS,
      TsxTokens.ASSERTS,
      TsxTokens.ANY,
      TsxTokens.BOOLEAN,
      TsxTokens.BREAK,
      TsxTokens.CASE,
      TsxTokens.CATCH,
      TsxTokens.CLASS,
      TsxTokens.CONST,
      TsxTokens.CONTINUE,
      TsxTokens.DEBUGGER,
      TsxTokens.DECLARE,
      TsxTokens.DEFAULT,
      TsxTokens.DELETE,
      TsxTokens.DO,
      TsxTokens.ELSE,
      TsxTokens.ENUM,
      TsxTokens.EXPORT,
      TsxTokens.EXTENDS,
      TsxTokens.FALSE,
      TsxTokens.FINALLY,
      TsxTokens.FOR,
      TsxTokens.FROM,
      TsxTokens.FUNCTION,
      TsxTokens.GET,
      TsxTokens.IF,
      TsxTokens.IMPLEMENTS,
      TsxTokens.IMPORT,
      TsxTokens.IN,
      TsxTokens.INFER,
      TsxTokens.INSTANCEOF,
      TsxTokens.INTERFACE,
      TsxTokens.IS,
      TsxTokens.KEYOF,
      TsxTokens.LET,
      TsxTokens.MODULE,
      TsxTokens.NAMESPACE,
      TsxTokens.NEVER,
      TsxTokens.NEW,
      TsxTokens.NULL,
      TsxTokens.NUMBER,
      TsxTokens.OBJECT,
      TsxTokens.PACKAGE,
      TsxTokens.PRIVATE,
      TsxTokens.PROTECTED,
      TsxTokens.PUBLIC,
      TsxTokens.READONLY,
      TsxTokens.REQUIRE,
      TsxTokens.RETURN,
      TsxTokens.SET,
      TsxTokens.STATIC,
      TsxTokens.STRING,
      TsxTokens.SUPER,
      TsxTokens.SWITCH,
      TsxTokens.SYMBOL,
      TsxTokens.THIS,
      TsxTokens.THROW,
      TsxTokens.TRUE,
      TsxTokens.TRY,
      TsxTokens.TYPE,
      TsxTokens.TYPEOF,
      TsxTokens.UNDEFINED,
      TsxTokens.UNKNOWN,
      TsxTokens.VAR,
      TsxTokens.VOID,
      TsxTokens.WHILE,
      TsxTokens.WITH,
      TsxTokens.YIELD
    };
    keywords = new TrieTree<>();
    for (int i = 0; i < words.length; i++) {
      keywords.put(words[i], tokens[i]);
    }
  }
}
