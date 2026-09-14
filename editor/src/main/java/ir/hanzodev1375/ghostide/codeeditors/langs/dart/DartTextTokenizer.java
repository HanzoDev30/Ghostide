/**
 * Comment by ghost ide
 *
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.dart;

import io.github.rosemoe.sora.util.TrieTree;

public class DartTextTokenizer {

  private static TrieTree<DartTokens> keywords;

  static {
    doStaticInit();
  }

  public static TrieTree<DartTokens> getTree() {
    return keywords;
  }

  private CharSequence source;

  private int bufferLen;

  private int index;

  public int offset;

  public int length;

  private DartTokens currToken;

  public DartTextTokenizer(CharSequence src) {
    if (src == null) throw new IllegalArgumentException("src cannot be null");
    this.source = src;
    init();
  }

  private void init() {
    length = 0;
    index = 0;
    offset = 0;
    currToken = DartTokens.WHITESPACE;
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

  public DartTokens nextToken() {
    return currToken = nextTokenInternal();
  }

  private DartTokens nextTokenInternal() {
    index += length;
    offset += length;
    if (offset >= bufferLen) return DartTokens.EOF;
    char ch = source.charAt(offset);
    length = 1;
    if (ch == '\n') return DartTokens.NEWLINE;
    if (ch == '\r') {
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '\n') length++;
      return DartTokens.NEWLINE;
    }
    if (isWhitespace(ch)) {
      while (offset + length < bufferLen && isWhitespace(source.charAt(offset + length))) {
        length++;
      }
      return DartTokens.WHITESPACE;
    }

    if (ch == '/' && offset + 1 < bufferLen && source.charAt(offset + 1) == '/') {
      length++;

      if (offset + 2 < bufferLen && source.charAt(offset + 2) == '/') {
        length++;
        while (offset + length < bufferLen && source.charAt(offset + length) != '\n') {
          length++;
        }
        return DartTokens.LINE_COMMENT_DOC;
      }
      while (offset + length < bufferLen && source.charAt(offset + length) != '\n') {
        length++;
      }
      return DartTokens.LINE_COMMENT;
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
      return finished ? DartTokens.BLOCK_COMMENT_COMPLETE : DartTokens.BLOCK_COMMENT_INCOMPLETE;
    }

    if (ch == '"' || ch == '\'') {

      if (offset + 2 < bufferLen
          && source.charAt(offset + 1) == ch
          && source.charAt(offset + 2) == ch) {
        length = 3;
        while (offset + length < bufferLen) {

          if (source.charAt(offset + length) == ch
              && offset + length + 2 < bufferLen
              && source.charAt(offset + length + 1) == ch
              && source.charAt(offset + length + 2) == ch) {
            length += 3;
            break;
          }
          length++;
        }
        return DartTokens.STRING_LITERAL;
      }

      scanStringLiteral(ch);
      return DartTokens.STRING_LITERAL;
    }

    if (isDigit(ch)
        || (ch == '.' && offset + 1 < bufferLen && isDigit(source.charAt(offset + 1)))) {
      return scanNumber();
    }

    if (isIdentifierStart(ch)) {
      return scanIdentifier(ch);
    }

    switch (ch) {
      case '{':
        return DartTokens.LBRACE;
      case '}':
        return DartTokens.RBRACE;
      case '(':
        return DartTokens.LPAREN;
      case ')':
        return DartTokens.RPAREN;
      case '[':
        return DartTokens.LBRACK;
      case ']':
        return DartTokens.RBRACK;
      case ';':
        return DartTokens.SEMICOLON;
      case ':':
        return DartTokens.COLON;
      case ',':
        return DartTokens.COMMA;
      case '?':
        return scanQuestion();
      case '!':
        return scanExclamation();
      case '~':
        return DartTokens.TILDE;
      case '|':
        return scanPipe();
      case '&':
        return scanAmpersand();
      case '^':
        return scanCaret();
      case '+':
        return scanPlus();
      case '-':
        return scanMinus();
      case '*':
        return scanStar();
      case '/':
        return scanSlash();
      case '%':
        return scanPercent();
      case '<':
        return scanLT();
      case '>':
        return scanGT();
      case '=':
        return scanAssign();
      case '.':
        return scanDot();
      case '@':
        return DartTokens.AT;
      default:
        return DartTokens.UNKNOWN;
    }
  }

  private DartTokens scanQuestion() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '?') {
        length++;
        if (offset + 2 < bufferLen && source.charAt(offset + 2) == '=') {
          length++;
          return DartTokens.NULL_AWARE_ASSIGN;
        }
        return DartTokens.NULL_AWARE;
      }
      if (n == '.') {
        length++;
        return DartTokens.QUESTION_DOT;
      }
    }
    return DartTokens.QUESTION;
  }

  private DartTokens scanExclamation() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '=') {
        length++;
        return DartTokens.NOT_EQ;
      }
      if (n == '.') {
        length++;
        return DartTokens.EXCLAMATION_DOT;
      }
    }
    return DartTokens.EXCLAMATION;
  }

  private DartTokens scanPipe() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '|') {
        length++;
        return DartTokens.LOGICAL_OR;
      }
      if (n == '=') {
        length++;
        return DartTokens.PIPE_ASSIGN;
      }
    }
    return DartTokens.PIPE;
  }

  private DartTokens scanAmpersand() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '&') {
        length++;
        return DartTokens.LOGICAL_AND;
      }
      if (n == '=') {
        length++;
        return DartTokens.AMPERSAND_ASSIGN;
      }
    }
    return DartTokens.AMPERSAND;
  }

  private DartTokens scanCaret() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return DartTokens.CARET_ASSIGN;
    }
    return DartTokens.CARET;
  }

  private DartTokens scanPlus() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '+') {
        length++;
        return DartTokens.INC;
      }
      if (n == '=') {
        length++;
        return DartTokens.PLUS_ASSIGN;
      }
    }
    return DartTokens.PLUS;
  }

  private DartTokens scanMinus() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '-') {
        length++;
        return DartTokens.DEC;
      }
      if (n == '=') {
        length++;
        return DartTokens.MINUS_ASSIGN;
      }
      if (n == '>') {
        length++;
        return DartTokens.ARROW;
      }
    }
    return DartTokens.MINUS;
  }

  private DartTokens scanStar() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return DartTokens.STAR_ASSIGN;
    }
    return DartTokens.STAR;
  }

  private DartTokens scanSlash() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return DartTokens.SLASH_ASSIGN;
    }
    return DartTokens.SLASH;
  }

  private DartTokens scanPercent() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return DartTokens.PERCENT_ASSIGN;
    }
    return DartTokens.PERCENT;
  }

  private DartTokens scanLT() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '=') {
        length++;
        return DartTokens.LT_EQ;
      }
    }
    return DartTokens.LT;
  }

  private DartTokens scanGT() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '=') {
        length++;
        return DartTokens.GT_EQ;
      }
    }
    return DartTokens.GT;
  }

  private DartTokens scanAssign() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '=') {
        length++;
        return DartTokens.EQ;
      }
    }
    return DartTokens.ASSIGN;
  }

  private DartTokens scanDot() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '.') {
        length++;
        if (offset + 2 < bufferLen && source.charAt(offset + 2) == '.') {
          length++;

          return DartTokens.SPREAD;
        }

        return DartTokens.CASCADE;
      }
    }
    return DartTokens.DOT;
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
          || c == '$'
          || c == 'x'
          || isDigit(c)) {
        length++;
        if (c == 'x') {
          while (offset + length < bufferLen && isHexDigit(source.charAt(offset + length))) {
            length++;
          }
        } else if (isDigit(c)) {
          while (offset + length < bufferLen && isDigit(source.charAt(offset + length))) {
            length++;
          }
        }
      }
    }
  }

  private DartTokens scanNumber() {
    boolean isDouble = false;

    if (offset + 1 < bufferLen && source.charAt(offset) == '0') {
      char next = source.charAt(offset + 1);
      if (next == 'x' || next == 'X') {
        length++;
        while (offset + length < bufferLen && isHexDigit(source.charAt(offset + length))) {
          length++;
        }
        return DartTokens.INTEGER_LITERAL;
      }
      if (next == 'b' || next == 'B') {
        length++;
        while (offset + length < bufferLen
            && (source.charAt(offset + length) == '0' || source.charAt(offset + length) == '1')) {
          length++;
        }
        return DartTokens.INTEGER_LITERAL;
      }
    }
    while (offset + length < bufferLen && isDigit(source.charAt(offset + length))) length++;
    if (offset + length < bufferLen && source.charAt(offset + length) == '.') {
      isDouble = true;
      length++;
      while (offset + length < bufferLen && isDigit(source.charAt(offset + length))) length++;
    }
    if (offset + length < bufferLen
        && (source.charAt(offset + length) == 'e' || source.charAt(offset + length) == 'E')) {
      isDouble = true;
      length++;
      if (offset + length < bufferLen
          && (source.charAt(offset + length) == '+' || source.charAt(offset + length) == '-')) {
        length++;
      }
      while (offset + length < bufferLen && isDigit(source.charAt(offset + length))) length++;
    }
    return isDouble ? DartTokens.DOUBLE_LITERAL : DartTokens.INTEGER_LITERAL;
  }

  private DartTokens scanIdentifier(char first) {
    TrieTree.Node<DartTokens> node = keywords.root.map.get(first);
    while (offset + length < bufferLen && isIdentifierPart(source.charAt(offset + length))) {
      char c = source.charAt(offset + length);
      node = (node == null) ? null : node.map.get(c);
      length++;
    }
    if (node != null && node.token != null) {

      DartTokens tok = node.token;
      if (tok == DartTokens.TRUE || tok == DartTokens.FALSE) return DartTokens.BOOLEAN_LITERAL;
      if (tok == DartTokens.NULL) return DartTokens.NULL_LITERAL;
      return tok;
    }
    return DartTokens.IDENTIFIER;
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
    DartTokens[] tokens = {
      DartTokens.ABSTRACT,
      DartTokens.AS,
      DartTokens.ASSERT,
      DartTokens.ASYNC,
      DartTokens.AWAIT,
      DartTokens.BREAK,
      DartTokens.CASE,
      DartTokens.CATCH,
      DartTokens.CLASS,
      DartTokens.CONST,
      DartTokens.CONTINUE,
      DartTokens.COVARIANT,
      DartTokens.DEFAULT,
      DartTokens.DEFERRED,
      DartTokens.DO,
      DartTokens.DYNAMIC,
      DartTokens.ELSE,
      DartTokens.ENUM,
      DartTokens.EXPORT,
      DartTokens.EXTENDS,
      DartTokens.EXTENSION,
      DartTokens.EXTERNAL,
      DartTokens.FACTORY,
      DartTokens.FALSE,
      DartTokens.FINAL,
      DartTokens.FINALLY,
      DartTokens.FOR,
      DartTokens.FUNCTION,
      DartTokens.GET,
      DartTokens.HIDE,
      DartTokens.IF,
      DartTokens.IMPLEMENTS,
      DartTokens.IMPORT,
      DartTokens.IN,
      DartTokens.INTERFACE,
      DartTokens.IS,
      DartTokens.LIBRARY,
      DartTokens.MIXIN,
      DartTokens.NEW,
      DartTokens.NULL,
      DartTokens.ON,
      DartTokens.OPERATOR,
      DartTokens.PART,
      DartTokens.REQUIRED,
      DartTokens.RETHROW,
      DartTokens.RETURN,
      DartTokens.SET,
      DartTokens.SHOW,
      DartTokens.STATIC,
      DartTokens.SUPER,
      DartTokens.SWITCH,
      DartTokens.SYNC,
      DartTokens.THIS,
      DartTokens.THROW,
      DartTokens.TRUE,
      DartTokens.TRY,
      DartTokens.TYPEDEF,
      DartTokens.VAR,
      DartTokens.VOID,
      DartTokens.WHILE,
      DartTokens.WITH,
      DartTokens.YIELD
    };
    keywords = new TrieTree<>();
    for (int i = 0; i < words.length; i++) {
      keywords.put(words[i], tokens[i]);
    }
  }
}
