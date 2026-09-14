/**
 * Comment by ghost ide
 *
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.go;

import io.github.rosemoe.sora.util.MyCharacter;
import io.github.rosemoe.sora.util.TrieTree;

public class GoTextTokenizer {

  private static TrieTree<GoTokens> keywords;

  static {
    doStaticInit();
  }

  public static TrieTree<GoTokens> getTree() {
    return keywords;
  }

  private CharSequence source;

  private int bufferLen;

  private int index;

  public int offset;

  public int length;

  private GoTokens currToken;

  public GoTextTokenizer(CharSequence src) {
    if (src == null) throw new IllegalArgumentException("src cannot be null");
    this.source = src;
    init();
  }

  private void init() {
    length = 0;
    index = 0;
    offset = 0;
    currToken = GoTokens.WHITESPACE;
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

  public GoTokens nextToken() {
    return currToken = nextTokenInternal();
  }

  private GoTokens nextTokenInternal() {
    index += length;
    offset += length;
    if (offset >= bufferLen) return GoTokens.EOF;
    char ch = source.charAt(offset);
    length = 1;
    if (ch == '\n') return GoTokens.NEWLINE;
    if (ch == '\r') {
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '\n') length++;
      return GoTokens.NEWLINE;
    }
    if (isWhitespace(ch)) {
      while (offset + length < bufferLen && isWhitespace(source.charAt(offset + length))) {
        length++;
      }
      return GoTokens.WHITESPACE;
    }
    if (ch == '/' && offset + 1 < bufferLen && source.charAt(offset + 1) == '/') {
      while (offset + length < bufferLen && source.charAt(offset + length) != '\n') {
        length++;
      }
      return GoTokens.LINE_COMMENT;
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
      return finished ? GoTokens.BLOCK_COMMENT_COMPLETE : GoTokens.BLOCK_COMMENT_INCOMPLETE;
    }
    if (ch == '"') {
      scanStringLiteral();
      return GoTokens.STRING_LITERAL;
    }
    if (ch == '`') {
      scanRawStringLiteral();
      return GoTokens.STRING_LITERAL;
    }
    if (ch == '\'') {
      scanCharLiteral();
      return GoTokens.CHAR_LITERAL;
    }
    if (isDigit(ch)
        || (ch == '.' && offset + 1 < bufferLen && isDigit(source.charAt(offset + 1)))) {
      return scanNumber();
    }
    if (isIdentifierStart(ch)) {
      return scanIdentifier(ch);
    }
    switch (ch) {
      case '=':
        return scanOperatorTwo(GoTokens.ASSIGN, '=', GoTokens.EQ);
      case '+':
        return scanPlus();
      case '-':
        return scanMinus();
      case '*':
        return scanOperatorTwo(GoTokens.STAR, '=', GoTokens.STAR_ASSIGN);
      case '/':
        return scanOperatorTwo(GoTokens.SLASH, '=', GoTokens.SLASH_ASSIGN);
      case '%':
        return scanOperatorTwo(GoTokens.PERCENT, '=', GoTokens.PERCENT_ASSIGN);
      case '^':
        return scanOperatorTwo(GoTokens.XOR, '=', GoTokens.XOR_ASSIGN);
      case '&':
        return scanAnd();
      case '|':
        return scanOr();
      case '<':
        return scanLT();
      case '>':
        return scanGT();
      case '!':
        return scanOperatorTwo(GoTokens.NOT, '=', GoTokens.NOT);
      case ':':
        return GoTokens.COLON;
      case ',':
        return GoTokens.COMMA;
      case ';':
        return GoTokens.SEMICOLON;
      case '.':
        return scanDot();
      case '{':
        return GoTokens.LBRACE;
      case '}':
        return GoTokens.RBRACE;
      case '(':
        return GoTokens.LPAREN;
      case ')':
        return GoTokens.RPAREN;
      case '[':
        return GoTokens.LBRACK;
      case ']':
        return GoTokens.RBRACK;
      case '~':
        return GoTokens.UNKNOWN;
      default:
        return GoTokens.UNKNOWN;
    }
  }

  private GoTokens scanIdentifier(char first) {
    TrieTree.Node<GoTokens> node = keywords.root.map.get(first);
    while (offset + length < bufferLen && isIdentifierPart(source.charAt(offset + length))) {
      char c = source.charAt(offset + length);
      node = (node == null) ? null : node.map.get(c);
      length++;
    }
    return (node != null && node.token != null) ? node.token : GoTokens.IDENTIFIER;
  }

  private GoTokens scanNumber() {
    boolean isFloat = false;
    if (offset + 1 < bufferLen && source.charAt(offset) == '0') {
      char next = source.charAt(offset + 1);
      if (next == 'b' || next == 'B') {
        length++;
        while (offset + length < bufferLen
            && (source.charAt(offset + length) == '0' || source.charAt(offset + length) == '1')) {
          length++;
        }
        return GoTokens.INTEGER_LITERAL;
      }
      if (next == 'o' || next == 'O') {
        length++;
        while (offset + length < bufferLen
            && source.charAt(offset + length) >= '0'
            && source.charAt(offset + length) <= '7') {
          length++;
        }
        return GoTokens.INTEGER_LITERAL;
      }
      if (next == 'x' || next == 'X') {
        length++;
        while (offset + length < bufferLen && isHexDigit(source.charAt(offset + length))) {
          length++;
        }
        return GoTokens.INTEGER_LITERAL;
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
          && (source.charAt(offset + length) == '+' || source.charAt(offset + length) == '-')) {
        length++;
      }
      while (offset + length < bufferLen && isDigit(source.charAt(offset + length))) length++;
    }
    if (offset + length < bufferLen
        && (source.charAt(offset + length) == 'i'
            || source.charAt(offset + length) == 'I'
            || source.charAt(offset + length) == 'f'
            || source.charAt(offset + length) == 'F')) {
      length++;
    }
    return isFloat ? GoTokens.FLOATING_LITERAL : GoTokens.INTEGER_LITERAL;
  }

  private void scanStringLiteral() {
    while (offset + length < bufferLen) {
      char c = source.charAt(offset + length);
      if (c == '"') {
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

  private void scanRawStringLiteral() {
    while (offset + length < bufferLen) {
      char c = source.charAt(offset + length);
      if (c == '`') {
        length++;
        break;
      }
      length++;
    }
  }

  private void scanCharLiteral() {
    while (offset + length < bufferLen) {
      char c = source.charAt(offset + length);
      if (c == '\'') {
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
      if (c == 'a' || c == 'b' || c == 'f' || c == 'n' || c == 'r' || c == 't' || c == 'v'
          || c == '\\' || c == '"' || c == '\'' || c == '`') {
        length++;
      } else if (c == 'x') {
        length++;
        while (offset + length < bufferLen && isHexDigit(source.charAt(offset + length))) length++;
      } else if (isDigit(c)) {
        while (offset + length < bufferLen && isDigit(source.charAt(offset + length))) length++;
      }
    }
  }

  private GoTokens scanPlus() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '+') {
        length++;
        return GoTokens.INC;
      }
      if (n == '=') {
        length++;
        return GoTokens.PLUS_ASSIGN;
      }
    }
    return GoTokens.PLUS;
  }

  private GoTokens scanMinus() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '-') {
        length++;
        return GoTokens.DEC;
      }
      if (n == '=') {
        length++;
        return GoTokens.MINUS_ASSIGN;
      }
    }
    return GoTokens.MINUS;
  }

  private GoTokens scanLT() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '=') {
        length++;
        return GoTokens.LT;
      }
      if (n == '<') {
        length++;
        if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
          length++;
          return GoTokens.AND_ASSIGN;
        }
        return GoTokens.AND_NOT;
      }
    }
    return GoTokens.LT;
  }

  private GoTokens scanGT() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '=') {
        length++;
        return GoTokens.GT;
      }
    }
    return GoTokens.GT;
  }

  private GoTokens scanAnd() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '&') {
        length++;
        return GoTokens.AND;
      }
      if (n == '=') {
        length++;
        return GoTokens.AND_ASSIGN;
      }
    }
    return GoTokens.AND;
  }

  private GoTokens scanOr() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '|') {
      length++;
      return GoTokens.OR;
    }
    return GoTokens.OR;
  }

  private GoTokens scanDot() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '.') {
      length++;
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '.') {
        length++;
        return GoTokens.ELLIPSIS;
      }
    }
    return GoTokens.DOT;
  }

  private GoTokens scanOperatorTwo(GoTokens single, char nextChar, GoTokens doubleToken) {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == nextChar) {
      length++;
      return doubleToken;
    }
    return single;
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
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
  }

  private static boolean isIdentifierPart(char c) {
    return isIdentifierStart(c) || isDigit(c);
  }

  private static void doStaticInit() {
    String[] words = {
      "break",
      "case",
      "chan",
      "const",
      "continue",
      "default",
      "defer",
      "else",
      "fallthrough",
      "for",
      "func",
      "go",
      "goto",
      "if",
      "import",
      "interface",
      "map",
      "package",
      "range",
      "return",
      "select",
      "struct",
      "switch",
      "type",
      "var",
      "nil",
      "true",
      "false",
      "iota"
    };
    GoTokens[] tokens = {
      GoTokens.BREAK,
      GoTokens.CASE,
      GoTokens.CHAN,
      GoTokens.CONST,
      GoTokens.CONTINUE,
      GoTokens.DEFAULT,
      GoTokens.DEFER,
      GoTokens.ELSE,
      GoTokens.FALLTHROUGH,
      GoTokens.FOR,
      GoTokens.FUNC,
      GoTokens.GO,
      GoTokens.GOTO,
      GoTokens.IF,
      GoTokens.IMPORT,
      GoTokens.INTERFACE,
      GoTokens.MAP,
      GoTokens.PACKAGE,
      GoTokens.RANGE,
      GoTokens.RETURN,
      GoTokens.SELECT,
      GoTokens.STRUCT,
      GoTokens.SWITCH,
      GoTokens.TYPE,
      GoTokens.VAR,
      GoTokens.NIL,
      GoTokens.TRUE,
      GoTokens.FALSE,
      GoTokens.IOTA
    };
    keywords = new TrieTree<>();
    for (int i = 0; i < words.length; i++) {
      keywords.put(words[i], tokens[i]);
    }
  }
}
