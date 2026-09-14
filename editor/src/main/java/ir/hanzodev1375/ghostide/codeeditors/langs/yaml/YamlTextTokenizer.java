package ir.hanzodev1375.ghostide.codeeditors.langs.yaml;

/**
 * Line-oriented YAML tokenizer.
 *
 * <p>YAML ساختار خط‌محور داره: - هر خط با indent مشخص میشه - key: value روی یه خط - block scalar (|
 * یا >) چند خط - # کامنت
 */
public class YamlTextTokenizer {

  private CharSequence source;
  private int bufferLen;
  public int offset;
  public int length;
  private Tokens currToken;

  private boolean seenColon = false;

  public YamlTextTokenizer(CharSequence src) {
    reset(src);
  }

  public void reset(CharSequence src) {
    if (src == null) throw new IllegalArgumentException("src cannot be null");
    source = src;
    bufferLen = src.length();
    offset = 0;
    length = 0;
    currToken = Tokens.WHITESPACE;
    seenColon = false;
  }

  public Tokens getToken() {
    return currToken;
  }

  public int getOffset() {
    return offset;
  }

  public int getLength() {
    return length;
  }

  public CharSequence getTokenText() {
    return source.subSequence(offset, offset + length);
  }

  public boolean hasSeenColon() {
    return seenColon;
  }

  public Tokens nextToken() {
    offset += length;
    length = 0;
    return currToken = nextInternal();
  }

  private Tokens nextInternal() {
    if (offset >= bufferLen) return Tokens.EOF;
    char ch = c(offset);

    if (ch == '\n') {
      length = 1;
      return Tokens.NEWLINE;
    }
    if (ch == '\r') {
      length = 1;
      if (offset + 1 < bufferLen && c(offset + 1) == '\n') length++;
      return Tokens.NEWLINE;
    }

    if (ch == ' ' || ch == '\t') {
      length = 1;
      while (offset + length < bufferLen) {
        char nx = c(offset + length);
        if (nx == ' ' || nx == '\t') length++;
        else break;
      }
      return Tokens.WHITESPACE;
    }

    if (ch == '#') {
      length = 1;
      while (offset + length < bufferLen
          && c(offset + length) != '\n'
          && c(offset + length) != '\r') length++;
      return Tokens.LINE_COMMENT;
    }

    if (ch == '-' && offset + 2 < bufferLen && c(offset + 1) == '-' && c(offset + 2) == '-') {

      if (offset + 3 >= bufferLen
          || c(offset + 3) == '\n'
          || c(offset + 3) == '\r'
          || c(offset + 3) == ' ') {
        length = 3;
        return Tokens.DOC_START;
      }
    }
    if (ch == '.' && offset + 2 < bufferLen && c(offset + 1) == '.' && c(offset + 2) == '.') {
      length = 3;
      return Tokens.DOC_END;
    }

    if (ch == '%') {
      length = 1;
      while (offset + length < bufferLen
          && c(offset + length) != '\n'
          && c(offset + length) != '\r') length++;
      return Tokens.DIRECTIVE;
    }

    if (ch == '&') {
      length = 1;
      while (offset + length < bufferLen && isAnchorChar(c(offset + length))) length++;
      return Tokens.ANCHOR;
    }

    if (ch == '*') {
      length = 1;
      while (offset + length < bufferLen && isAnchorChar(c(offset + length))) length++;
      return Tokens.ALIAS;
    }

    if (ch == '!') {
      length = 1;
      while (offset + length < bufferLen
          && !isWhitespace(c(offset + length))
          && c(offset + length) != '\n'
          && c(offset + length) != '\r') length++;
      return Tokens.TAG;
    }

    if (ch == '{') {
      length = 1;
      return Tokens.LBRACE;
    }
    if (ch == '}') {
      length = 1;
      return Tokens.RBRACE;
    }
    if (ch == '[') {
      length = 1;
      return Tokens.LBRACK;
    }
    if (ch == ']') {
      length = 1;
      return Tokens.RBRACK;
    }
    if (ch == ',') {
      length = 1;
      return Tokens.COMMA;
    }

    if (ch == ':') {

      if (offset + 1 >= bufferLen
          || c(offset + 1) == ' '
          || c(offset + 1) == '\t'
          || c(offset + 1) == '\n'
          || c(offset + 1) == '\r') {
        length = 1;
        seenColon = true;
        return Tokens.COLON;
      }
    }

    if (ch == '-'
        && offset + 1 < bufferLen
        && (c(offset + 1) == ' '
            || c(offset + 1) == '\t'
            || c(offset + 1) == '\n'
            || c(offset + 1) == '\r')) {
      length = 1;
      return Tokens.LIST_MARKER;
    }

    if ((ch == '|' || ch == '>') && !seenColon) {}
    if (ch == '|') {

      length = 1;
      while (offset + length < bufferLen
          && c(offset + length) != '\n'
          && c(offset + length) != '\r'
          && c(offset + length) != '#') length++;
      return Tokens.BLOCK_LITERAL_HEADER;
    }
    if (ch == '>') {
      length = 1;
      while (offset + length < bufferLen
          && c(offset + length) != '\n'
          && c(offset + length) != '\r'
          && c(offset + length) != '#') length++;
      return Tokens.BLOCK_FOLDED_HEADER;
    }

    if (ch == '$') {
      if (offset + 2 < bufferLen && c(offset + 1) == '{' && c(offset + 2) == '{') {
        return scanExpression();
      }
    }

    if (ch == '"') {
      return scanDoubleQuote();
    }
    if (ch == '\'') {
      return scanSingleQuote();
    }

    if (isPrimeDigit(ch) || ch == '-' || ch == '+') {
      Tokens numTok = tryNumber();
      if (numTok != null) return numTok;
    }

    return scanPlain();
  }

  private Tokens scanExpression() {

    length = 3;

    while (offset + length < bufferLen) {
      char ch = c(offset + length);
      if (ch == '$' || ch == '{') break;
      if (ch == '}' && offset + length + 1 < bufferLen && c(offset + length + 1) == '}') {
        length += 2;
        break;
      }
      length++;
    }
    return Tokens.EXPRESSION;
  }

  private Tokens scanDoubleQuote() {
    length = 1;
    while (offset + length < bufferLen) {
      char ch = c(offset + length);
      length++;
      if (ch == '"') return Tokens.STRING_LITERAL;
      if (ch == '\\' && offset + length < bufferLen) length++;
      if (ch == '\n' || ch == '\r') break;
    }
    return Tokens.STRING_LITERAL;
  }

  private Tokens scanSingleQuote() {
    length = 1;
    while (offset + length < bufferLen) {
      char ch = c(offset + length);
      length++;
      if (ch == '\'') {

        if (offset + length < bufferLen && c(offset + length) == '\'') {
          length++;
          continue;
        }
        return Tokens.STRING_LITERAL;
      }
      if (ch == '\n' || ch == '\r') break;
    }
    return Tokens.STRING_LITERAL;
  }

  private Tokens tryNumber() {
    int i = 0;
    char ch = c(offset);

    if (ch == '-' || ch == '+') {
      i++;
      if (offset + i >= bufferLen) return null;
    }

    if (offset + i < bufferLen && c(offset + i) == '.') {
      String rest =
          source
              .subSequence(offset + i, Math.min(offset + i + 4, bufferLen))
              .toString()
              .toLowerCase();
      if (rest.startsWith(".inf") || rest.startsWith(".nan")) {
        length = i + 4;
        return Tokens.FLOATING_POINT_LITERAL;
      }
    }

    if (offset + i + 1 < bufferLen
        && c(offset + i) == '0'
        && (c(offset + i + 1) == 'x' || c(offset + i + 1) == 'X')) {
      i += 2;
      if (offset + i >= bufferLen || !isHexDigit(c(offset + i))) return null;
      while (offset + i < bufferLen && isHexDigit(c(offset + i))) i++;
      length = i;
      return Tokens.INTEGER_LITERAL;
    }

    if (offset + i + 1 < bufferLen
        && c(offset + i) == '0'
        && (c(offset + i + 1) == 'o' || c(offset + i + 1) == 'O')) {
      i += 2;
      while (offset + i < bufferLen && c(offset + i) >= '0' && c(offset + i) <= '7') i++;
      length = i;
      return Tokens.INTEGER_LITERAL;
    }

    if (offset + i + 1 < bufferLen
        && c(offset + i) == '0'
        && (c(offset + i + 1) == 'b' || c(offset + i + 1) == 'B')) {
      i += 2;
      while (offset + i < bufferLen && (c(offset + i) == '0' || c(offset + i) == '1')) i++;
      length = i;
      return Tokens.INTEGER_LITERAL;
    }

    if (offset + i >= bufferLen || !isPrimeDigit(c(offset + i))) return null;
    while (offset + i < bufferLen && isPrimeDigit(c(offset + i))) i++;

    boolean isFloat = false;
    if (offset + i < bufferLen && c(offset + i) == '.') {
      isFloat = true;
      i++;
      while (offset + i < bufferLen && isPrimeDigit(c(offset + i))) i++;
    }
    if (offset + i < bufferLen && (c(offset + i) == 'e' || c(offset + i) == 'E')) {
      isFloat = true;
      i++;
      if (offset + i < bufferLen && (c(offset + i) == '+' || c(offset + i) == '-')) i++;
      while (offset + i < bufferLen && isPrimeDigit(c(offset + i))) i++;
    }

    if (offset + i < bufferLen) {
      char nx = c(offset + i);
      if (!isWhitespace(nx)
          && nx != '\n'
          && nx != '\r'
          && nx != ':'
          && nx != ','
          && nx != ']'
          && nx != '}'
          && nx != '#') {
        return null;
      }
    }

    length = i;
    return isFloat ? Tokens.FLOATING_POINT_LITERAL : Tokens.INTEGER_LITERAL;
  }

  private Tokens scanPlain() {
    length = 0;
    while (offset + length < bufferLen) {
      char ch = c(offset + length);
      if (ch == '\n' || ch == '\r') break;
      if (ch == '#' && length > 0 && isWhitespace(c(offset + length - 1))) break;
      if (ch == ':'
          && offset + length + 1 < bufferLen
          && (c(offset + length + 1) == ' '
              || c(offset + length + 1) == '\t'
              || c(offset + length + 1) == '\n'
              || c(offset + length + 1) == '\r')) break;
      if (ch == ',' || ch == ']' || ch == '}') break;
      length++;
    }
    if (length == 0) {
      length = 1;
      return Tokens.UNKNOWN;
    }

    String text = source.subSequence(offset, offset + length).toString().trim();
    String lower = text.toLowerCase();

    if (lower.equals("true")
        || lower.equals("false")
        || lower.equals("yes")
        || lower.equals("no")
        || lower.equals("on")
        || lower.equals("off")) {
      return Tokens.BOOLEAN_LITERAL;
    }

    if (lower.equals("null") || lower.equals("~") || text.equals("")) {
      return Tokens.NULL_LITERAL;
    }

    return seenColon ? Tokens.SCALAR_VALUE : Tokens.KEY;
  }

  private static boolean isPrimeDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private static boolean isHexDigit(char c) {
    return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
  }

  private static boolean isWhitespace(char c) {
    return c == ' ' || c == '\t';
  }

  private static boolean isAnchorChar(char c) {
    return c != ' ' && c != '\t' && c != '\n' && c != '\r' && c != ',' && c != '[' && c != ']'
        && c != '{' && c != '}';
  }

  private char c(int pos) {
    return source.charAt(pos);
  }
}
