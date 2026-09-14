package ir.hanzodev1375.ghostide.codeeditors.langs.sass;

public class SassTextTokenizer {

  private CharSequence source;
  private int bufferLen;
  private int index;
  public int offset;
  public int length;
  private SassTokens currToken;

  public SassTextTokenizer(CharSequence src) {
    if (src == null) throw new IllegalArgumentException("src cannot be null");
    this.source = src;
    init();
  }

  private void init() {
    length = 0;
    index = 0;
    offset = 0;
    currToken = SassTokens.WHITESPACE;
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

  public SassTokens nextToken() {
    return currToken = nextTokenInternal();
  }

  private SassTokens nextTokenInternal() {
    index = index + length;
    offset = offset + length;
    if (offset >= bufferLen) return SassTokens.EOF;

    char ch = source.charAt(offset);
    length = 1;

    if (ch == '\n') return SassTokens.NEWLINE;
    if (ch == '\r') {
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '\n') length++;
      return SassTokens.NEWLINE;
    }

    if (ch == ' ' || ch == '\t') {
      while (offset + length < bufferLen) {
        char c = source.charAt(offset + length);
        if (c == ' ' || c == '\t') length++;
        else break;
      }
      return SassTokens.WHITESPACE;
    }

    if (ch == '/' && offset + 1 < bufferLen && source.charAt(offset + 1) == '/') {
      while (offset + length < bufferLen && source.charAt(offset + length) != '\n') length++;
      return SassTokens.LINE_COMMENT;
    }

    if (ch == '/' && offset + 1 < bufferLen && source.charAt(offset + 1) == '*') {
      length++;
      while (offset + length + 1 < bufferLen) {
        if (source.charAt(offset + length) == '*' && source.charAt(offset + length + 1) == '/') {
          length += 2;
          break;
        }
        length++;
      }
      return SassTokens.BLOCK_COMMENT;
    }

    if (ch == '#' && offset + 1 < bufferLen && source.charAt(offset + 1) == '{') {
      length++;
      return SassTokens.INTERPOLATION_START;
    }

    if (ch == '$') {
      length++;
      while (offset + length < bufferLen && isIdentPart(source.charAt(offset + length))) length++;
      return SassTokens.VARIABLE;
    }

    if (ch == '%') {
      length++;
      while (offset + length < bufferLen && isIdentPart(source.charAt(offset + length))) length++;
      return SassTokens.PLACEHOLDER;
    }

    if (ch == '&') {
      while (offset + length < bufferLen && isIdentPart(source.charAt(offset + length))) length++;
      return SassTokens.PARENT_SELECTOR;
    }

    if (ch == '@') {
      length++;
      while (offset + length < bufferLen && isIdentPart(source.charAt(offset + length))) length++;
      return SassTokens.AT_KEYWORD;
    }

    if (ch == '#') {
      if (offset + 1 < bufferLen && isHexDigit(source.charAt(offset + 1))) {
        length++;
        while (offset + length < bufferLen && isHexDigit(source.charAt(offset + length))) length++;
        return SassTokens.COLOR_HEX;
      }
      return SassTokens.UNKNOWN;
    }

    if (ch == '"' || ch == '\'') {
      char quote = ch;
      while (offset + length < bufferLen) {
        char c = source.charAt(offset + length);
        if (c == quote) {
          length++;
          break;
        }
        if (c == '\\' && offset + length + 1 < bufferLen) {
          length += 2;
          continue;
        }
        length++;
      }
      return SassTokens.STRING_LITERAL;
    }

    if (isDigit(ch) || ch == '.') {
      boolean hasDot = false;
      while (offset + length < bufferLen) {
        char c = source.charAt(offset + length);
        if (c == '.') {
          if (hasDot) break;
          hasDot = true;
          length++;
          continue;
        }
        if (isDigit(c)) {
          length++;
          continue;
        }
        break;
      }
      if (offset + length < bufferLen && isIdentPart(source.charAt(offset + length))) {
        while (offset + length < bufferLen && isIdentPart(source.charAt(offset + length))) length++;
        return SassTokens.UNIT;
      }
      return SassTokens.NUMBER;
    }

    if (isIdentStart(ch)) {
      while (offset + length < bufferLen && isIdentPart(source.charAt(offset + length))) length++;
      return SassTokens.IDENT;
    }

    switch (ch) {
      case '{':
        return SassTokens.LBRACE;
      case '}':
        return SassTokens.RBRACE;
      case '(':
        return SassTokens.LPAREN;
      case ')':
        return SassTokens.RPAREN;
      case '[':
        return SassTokens.LBRACK;
      case ']':
        return SassTokens.RBRACK;
      case ';':
        return SassTokens.SEMICOLON;
      case ':':
        return SassTokens.COLON;
      case ',':
        return SassTokens.COMMA;
      case '.':
        return SassTokens.DOT;
      case '+':
        return SassTokens.PLUS;
      case '-':
        return SassTokens.MINUS;
      case '*':
        return SassTokens.STAR;
      case '/':
        return SassTokens.SLASH;
      case '%':
        return SassTokens.PERCENT;
      case '=':
        return SassTokens.EQ;
      case '>':
        return SassTokens.GT;
      case '<':
        return SassTokens.LT;
      case '~':
        return SassTokens.TILDE;
      case '^':
        return SassTokens.CARET;
      default:
        return SassTokens.UNKNOWN;
    }
  }

  private static boolean isIdentStart(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
  }

  private static boolean isIdentPart(char c) {
    return isIdentStart(c) || (c >= '0' && c <= '9') || c == '-' || c == '_';
  }

  private static boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private static boolean isHexDigit(char c) {
    return isDigit(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
  }

}
