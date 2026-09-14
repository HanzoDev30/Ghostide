package ir.hanzodev1375.ghostide.codeeditors.langs.toml;

public class TomlTextTokenizer {

  private CharSequence source;
  private int bufferLen;
  private int index;
  public int offset;
  public int length;
  private Tokens currToken;

  public TomlTextTokenizer(CharSequence src) {
    if (src == null) throw new IllegalArgumentException("src can not be null");
    this.source = src;
    init();
  }

  private void init() {
    length = 0;
    index = 0;
    offset = 0;
    currToken = Tokens.WHITESPACE;
    this.bufferLen = source.length();
  }

  public void reset(CharSequence src) {
    if (src == null) throw new IllegalArgumentException("src can not be null");
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

  public int getIndex() {
    return index;
  }

  public Tokens getToken() {
    return currToken;
  }

  private char charAt(int i) {
    return source.charAt(i);
  }

  private char charAt() {
    return source.charAt(offset + length);
  }

  public Tokens nextToken() {
    return currToken = nextTokenInternal();
  }

  private Tokens nextTokenInternal() {
    index = index + length;
    offset = offset + length;
    if (offset >= bufferLen) return Tokens.EOF;
    char ch = source.charAt(offset);
    length = 1;

    if (ch == '\n') return Tokens.NEWLINE;
    if (ch == '\r') {
      if (offset + 1 < bufferLen && charAt(offset + 1) == '\n') length++;
      return Tokens.NEWLINE;
    }

    if (ch == ' ' || ch == '\t') {
      while (offset + length < bufferLen) {
        char c = charAt(offset + length);
        if (c == ' ' || c == '\t') length++;
        else break;
      }
      return Tokens.WHITESPACE;
    }

    // Comment
    if (ch == '#') {
      while (offset + length < bufferLen && charAt(offset + length) != '\n') length++;
      return Tokens.LINE_COMMENT;
    }

    // Table headers [table] or [[array]]
    if (ch == '[') {
      if (offset + 1 < bufferLen && charAt(offset + 1) == '[') {
        // [[array of tables]]
        length++;
        while (offset + length < bufferLen
            && !(charAt(offset + length) == ']'
                && offset + length + 1 < bufferLen
                && charAt(offset + length + 1) == ']')) {
          length++;
        }
        if (offset + length < bufferLen) length++; // first ]
        if (offset + length < bufferLen) length++; // second ]
        return Tokens.ARRAY_TABLE_HEADER;
      } else {
        // [table]
        while (offset + length < bufferLen && charAt(offset + length) != ']') length++;
        if (offset + length < bufferLen) length++; // closing ]
        return Tokens.TABLE_HEADER;
      }
    }

    if (ch == ']') return Tokens.RBRACK;
    if (ch == '{') return Tokens.LBRACE;
    if (ch == '}') return Tokens.RBRACE;
    if (ch == ',') return Tokens.COMMA;
    if (ch == '.') return Tokens.DOT;
    if (ch == '=') return Tokens.EQ;

    // Strings
    if (ch == '"') {
      // Multi-line basic string """..."""
      if (offset + 2 < bufferLen && charAt(offset + 1) == '"' && charAt(offset + 2) == '"') {
        length += 2;
        while (offset + length + 2 < bufferLen) {
          if (charAt(offset + length) == '"'
              && charAt(offset + length + 1) == '"'
              && charAt(offset + length + 2) == '"') {
            length += 3;
            break;
          }
          length++;
        }
        return Tokens.STRING_LITERAL;
      }
      // Basic string
      scanBasicString();
      return Tokens.STRING_LITERAL;
    }

    if (ch == '\'') {
      // Multi-line literal string '''...'''
      if (offset + 2 < bufferLen && charAt(offset + 1) == '\'' && charAt(offset + 2) == '\'') {
        length += 2;
        while (offset + length + 2 < bufferLen) {
          if (charAt(offset + length) == '\''
              && charAt(offset + length + 1) == '\''
              && charAt(offset + length + 2) == '\'') {
            length += 3;
            break;
          }
          length++;
        }
        return Tokens.STRING_LITERAL;
      }
      // Literal string (no escapes)
      scanLiteralString();
      return Tokens.STRING_LITERAL;
    }

    // Numbers (int, float, hex, octal, binary, dates)
    if (ch == '-' || ch == '+' || isPrimeDigit(ch)) {
      return scanNumber();
    }

    // Booleans and bare keys
    if (isKeyStart(ch)) {
      return scanBareKeyOrBoolean();
    }

    return Tokens.UNKNOWN;
  }

  private void scanBasicString() {
    if (offset + length == bufferLen) return;
    char ch;
    while (offset + length < bufferLen && (ch = charAt(offset + length)) != '"') {
      if (ch == '\\') {
        length++;
        if (offset + length < bufferLen) length++; // escaped char
      } else {
        if (ch == '\n') return;
        length++;
      }
    }
    if (offset + length < bufferLen) length++; // closing "
  }

  private void scanLiteralString() {
    if (offset + length == bufferLen) return;
    char ch;
    while (offset + length < bufferLen && (ch = charAt(offset + length)) != '\'') {
      if (ch == '\n') return;
      length++;
    }
    if (offset + length < bufferLen) length++; // closing '
  }

  private Tokens scanNumber() {
    char ch = charAt(offset);
    // Handle sign
    if (ch == '-' || ch == '+') {
      if (offset + length >= bufferLen) return Tokens.UNKNOWN;
      length++;
    }
    // Hex/octal/binary
    if (charAt(offset) == '0' && offset + length < bufferLen) {
      char prefix = charAt(offset + length);
      if (prefix == 'x' || prefix == 'o' || prefix == 'b') {
        length++;
        while (offset + length < bufferLen && isHexDigitOrUnderscore(charAt(offset + length)))
          length++;
        return Tokens.INTEGER_LITERAL;
      }
    }
    // Digits
    while (offset + length < bufferLen
        && (isPrimeDigit(charAt(offset + length)) || charAt(offset + length) == '_')) length++;
    if (offset + length == bufferLen) return Tokens.INTEGER_LITERAL;

    char next = charAt(offset + length);
    // Float
    if (next == '.') {
      length++;
      while (offset + length < bufferLen
          && (isPrimeDigit(charAt(offset + length)) || charAt(offset + length) == '_')) length++;
      // Exponent
      if (offset + length < bufferLen
          && (charAt(offset + length) == 'e' || charAt(offset + length) == 'E')) {
        length++;
        if (offset + length < bufferLen
            && (charAt(offset + length) == '+' || charAt(offset + length) == '-')) length++;
        while (offset + length < bufferLen && isPrimeDigit(charAt(offset + length))) length++;
      }
      return Tokens.FLOATING_POINT_LITERAL;
    }
    // DateTime detection: 1979-05-27 pattern (digit-digit-digit-digit-)
    if (next == '-' && length >= 4) {
      while (offset + length < bufferLen
          && !isWhitespace(charAt(offset + length))
          && charAt(offset + length) != ','
          && charAt(offset + length) != ']') length++;
      return Tokens.DATETIME_LITERAL;
    }
    // Special float values: inf, nan
    if (next == 'i' || next == 'n') {
      length += 3; // inf or nan
      return Tokens.FLOATING_POINT_LITERAL;
    }
    return Tokens.INTEGER_LITERAL;
  }

  private Tokens scanBareKeyOrBoolean() {
    while (offset + length < bufferLen && isKeyPart(charAt(offset + length))) length++;
    String text = source.subSequence(offset, offset + length).toString();
    if ("true".equals(text)) return Tokens.TRUE;
    if ("false".equals(text)) return Tokens.FALSE;
    if ("group".equals(text)) return Tokens.GROUP;
    if ("module".equals(text)) return Tokens.MODULE;
    if ("name".equals(text)) return Tokens.NAME;
    if ("version".equals(text)) return Tokens.VERSION;
    if ("ref".equals(text)) return Tokens.REF;
    if ("inf".equals(text) || "nan".equals(text) || "+inf".equals(text) || "-inf".equals(text))
      return Tokens.FLOATING_POINT_LITERAL;
    return Tokens.KEY;
  }

  private static boolean isKeyStart(char c) {
    return (c >= 'a' && c <= 'z')
        || (c >= 'A' && c <= 'Z')
        || (c >= '0' && c <= '9')
        || c == '_'
        || c == '-';
  }

  private static boolean isKeyPart(char c) {
    return (c >= 'a' && c <= 'z')
        || (c >= 'A' && c <= 'Z')
        || (c >= '0' && c <= '9')
        || c == '_'
        || c == '-';
  }

  private static boolean isHexDigitOrUnderscore(char c) {
    return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') || c == '_';
  }

  protected static boolean isPrimeDigit(char c) {
    return c >= '0' && c <= '9';
  }

  protected static boolean isWhitespace(char c) {
    return c == '\t' || c == ' ' || c == '\f' || c == '\n' || c == '\r';
  }
}
