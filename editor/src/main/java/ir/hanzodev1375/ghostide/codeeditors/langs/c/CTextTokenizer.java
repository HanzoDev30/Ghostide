/**
 * Comment by ghost ide
 *
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.c;

import io.github.rosemoe.sora.util.MyCharacter;
import io.github.rosemoe.sora.util.TrieTree;

public class CTextTokenizer {

  private static TrieTree<CTokens> keywords;

  static {
    doStaticInit();
  }

  public static TrieTree<CTokens> getTree() {
    return keywords;
  }

  private CharSequence source;

  private int bufferLen;

  private int index;

  public int offset;

  public int length;

  private CTokens currToken;

  public CTextTokenizer(CharSequence src) {
    if (src == null) throw new IllegalArgumentException("src cannot be null");
    this.source = src;
    init();
  }

  private void init() {
    length = 0;
    index = 0;
    offset = 0;
    currToken = CTokens.WHITESPACE;
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

  public CTokens nextToken() {
    return currToken = nextTokenInternal();
  }

  private CTokens nextTokenInternal() {
    index += length;
    offset += length;
    if (offset >= bufferLen) return CTokens.EOF;
    char ch = source.charAt(offset);
    length = 1;
    if (ch == '\n') return CTokens.NEWLINE;
    if (ch == '\r') {
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '\n') length++;
      return CTokens.NEWLINE;
    }
    if (isWhitespace(ch)) {
      while (offset + length < bufferLen && isWhitespace(source.charAt(offset + length))) {
        length++;
      }
      return CTokens.WHITESPACE;
    }

    if (ch == '/' && offset + 1 < bufferLen && source.charAt(offset + 1) == '/') {
      while (offset + length < bufferLen && source.charAt(offset + length) != '\n') length++;
      return CTokens.LINE_COMMENT;
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
      return finished ? CTokens.BLOCK_COMMENT_COMPLETE : CTokens.BLOCK_COMMENT_INCOMPLETE;
    }

    if (ch == '#') {
      while (offset + length < bufferLen && source.charAt(offset + length) != '\n') length++;
      return CTokens.PREPROCESSOR;
    }

    if (ch == '\'') {
      scanCharLiteral();
      return CTokens.CHAR_LITERAL;
    }

    if (ch == '"') {
      scanStringLiteral();
      return CTokens.STRING_LITERAL;
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
        return scanOperatorTwo(CTokens.ASSIGN, '=', CTokens.EQ);
      case '+':
        return scanPlus();
      case '-':
        return scanMinus();
      case '*':
        return scanOperatorTwo(CTokens.STAR, '=', CTokens.STAR_ASSIGN);
      case '/':
        return scanOperatorTwo(CTokens.SLASH, '=', CTokens.SLASH_ASSIGN);
      case '%':
        return scanOperatorTwo(CTokens.PERCENT, '=', CTokens.PERCENT_ASSIGN);
      case '&':
        return scanOperatorTwo(CTokens.AND, '&', CTokens.AND);
      case '|':
        return scanOperatorTwo(CTokens.OR, '|', CTokens.OR);
      case '^':
        return CTokens.XOR;
      case '!':
        return scanOperatorTwo(CTokens.NOT, '=', CTokens.NOT);
      case '<':
        return scanLT();
      case '>':
        return scanGT();
      case '.':
        return scanDot();
      case '~':
        return CTokens.COMP;

      case '?':
        return CTokens.UNKNOWN;
      case '{':
        return CTokens.LBRACE;
      case '}':
        return CTokens.RBRACE;
      case '(':
        return CTokens.LPAREN;
      case ')':
        return CTokens.RPAREN;
      case '[':
        return CTokens.LBRACK;
      case ']':
        return CTokens.RBRACK;
      case ';':
        return CTokens.SEMICOLON;
      case ':':
        return CTokens.COLON;
      case ',':
        return CTokens.COMMA;
      default:
        return CTokens.UNKNOWN;
    }
  }

  private CTokens scanIdentifier(char first) {
    TrieTree.Node<CTokens> node = keywords.root.map.get(first);
    while (offset + length < bufferLen && isIdentifierPart(source.charAt(offset + length))) {
      char c = source.charAt(offset + length);
      node = (node == null) ? null : node.map.get(c);
      length++;
    }
    return (node != null && node.token != null) ? node.token : CTokens.IDENTIFIER;
  }

  private CTokens scanNumber() {
    boolean isFloat = false;

    if (source.charAt(offset) == '0'
        && offset + 1 < bufferLen
        && (source.charAt(offset + 1) == 'x' || source.charAt(offset + 1) == 'X')) {
      length++;
      while (offset + length < bufferLen && isHexDigit(source.charAt(offset + length))) length++;
      return CTokens.INTEGER_LITERAL;
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
        && (source.charAt(offset + length) == 'f'
            || source.charAt(offset + length) == 'F'
            || source.charAt(offset + length) == 'l'
            || source.charAt(offset + length) == 'L')) {
      length++;
    }
    return isFloat ? CTokens.FLOATING_LITERAL : CTokens.INTEGER_LITERAL;
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
      if (c == 'n' || c == 't' || c == 'r' || c == '0' || c == '\\' || c == '\'' || c == '"'
          || c == 'a' || c == 'b' || c == 'f' || c == 'v' || c == '?') {
        length++;
      } else if (c >= '0' && c <= '7') {
        while (offset + length < bufferLen
            && source.charAt(offset + length) >= '0'
            && source.charAt(offset + length) <= '7') length++;
      } else if (c == 'x') {
        length++;
        while (offset + length < bufferLen && isHexDigit(source.charAt(offset + length))) length++;
      }
    }
  }

  private CTokens scanPlus() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '+') {
        length++;
        return CTokens.INC;
      }
      if (n == '=') {
        length++;
        return CTokens.PLUS_ASSIGN;
      }
    }
    return CTokens.PLUS;
  }

  private CTokens scanMinus() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '-') {
        length++;
        return CTokens.DEC;
      }
      if (n == '=') {
        length++;
        return CTokens.MINUS_ASSIGN;
      }
      if (n == '>') {
        length++;
        return CTokens.ARROW;
      }
    }
    return CTokens.MINUS;
  }

  private CTokens scanLT() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '=') {
        length++;
        return CTokens.LT;
      }

      if (n == '<') {
        length++;
        return CTokens.UNKNOWN;
      }
    }
    return CTokens.LT;
  }

  private CTokens scanGT() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '=') {
        length++;
        return CTokens.GT;
      }
      if (n == '>') {
        length++;
        return CTokens.UNKNOWN;
      }
    }
    return CTokens.GT;
  }

  private CTokens scanDot() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '.') {
      length++;
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '.') {

        length++;
      }
      return CTokens.RANGE;
    }
    return CTokens.DOT;
  }

  private CTokens scanOperatorTwo(CTokens single, char nextChar, CTokens doubleToken) {
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
    return MyCharacter.isJavaIdentifierStart(c) || c == '_';
  }

  private static boolean isIdentifierPart(char c) {
    return MyCharacter.isJavaIdentifierPart(c) || c == '_';
  }

  private static void doStaticInit() {
    String[] words = {
      "auto",
      "break",
      "case",
      "char",
      "const",
      "continue",
      "default",
      "do",
      "double",
      "else",
      "enum",
      "extern",
      "float",
      "for",
      "goto",
      "if",
      "int",
      "long",
      "register",
      "return",
      "short",
      "signed",
      "sizeof",
      "static",
      "struct",
      "switch",
      "typedef",
      "union",
      "unsigned",
      "void",
      "volatile",
      "while",
      "inline",
      "restrict",
      "_Bool",
      "_Complex",
      "_Imaginary"
    };
    CTokens[] tokens = {
      CTokens.AUTO,
      CTokens.BREAK,
      CTokens.CASE,
      CTokens.CHAR,
      CTokens.CONST,
      CTokens.CONTINUE,
      CTokens.DEFAULT,
      CTokens.DO,
      CTokens.DOUBLE,
      CTokens.ELSE,
      CTokens.ENUM,
      CTokens.EXTERN,
      CTokens.FLOAT,
      CTokens.FOR,
      CTokens.GOTO,
      CTokens.IF,
      CTokens.INT,
      CTokens.LONG,
      CTokens.REGISTER,
      CTokens.RETURN,
      CTokens.SHORT,
      CTokens.SIGNED,
      CTokens.SIZEOF,
      CTokens.STATIC,
      CTokens.STRUCT,
      CTokens.SWITCH,
      CTokens.TYPEDEF,
      CTokens.UNION,
      CTokens.UNSIGNED,
      CTokens.VOID,
      CTokens.VOLATILE,
      CTokens.WHILE,
      CTokens.INLINE,
      CTokens.RESTRICT,
      CTokens.BOOL,
      CTokens.COMPLEX,
      CTokens.IMAGINARY
    };
    keywords = new TrieTree<>();
    for (int i = 0; i < words.length; i++) {
      keywords.put(words[i], tokens[i]);
    }
  }
}
