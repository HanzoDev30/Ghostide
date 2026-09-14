package ir.hanzodev1375.ghostide.codeeditors.langs.gradle;

import io.github.rosemoe.sora.util.MyCharacter;
import io.github.rosemoe.sora.util.TrieTree;

public class GradleTextTokenizer {

  private static TrieTree<Tokens> keywords;

  static {
    doStaticInit();
  }

  public static TrieTree<Tokens> getTree() {
    return keywords;
  }

  private CharSequence source;
  private int bufferLen;
  private int line;
  private int column;
  private int index;
  public int offset;
  public int length;
  private Tokens currToken;
  private boolean lcCal;

  public GradleTextTokenizer(CharSequence src) {
    if (src == null) throw new IllegalArgumentException("src can not be null");
    this.source = src;
    init();
  }

  private void init() {
    line = 0;
    column = 0;
    length = 0;
    index = 0;
    offset = 0;
    currToken = Tokens.WHITESPACE;
    lcCal = false;
    this.bufferLen = source.length();
  }

  public void reset(CharSequence src) {
    if (src == null) throw new IllegalArgumentException("src can not be null");
    this.source = src;
    this.bufferLen = src.length();
    init();
  }

  public void setCalculateLineColumn(boolean cal) {
    this.lcCal = cal;
  }

  public void pushBack(int length) {
    if (length > getTokenLength()) throw new IllegalArgumentException("pushBack length too large");
    this.length -= length;
  }

  private boolean isIdentifierPart(char ch) {
    return MyCharacter.isJavaIdentifierPart(ch) || ch == '_' || ch == '$';
  }

  private boolean isIdentifierStart(char ch) {
    return MyCharacter.isJavaIdentifierStart(ch) || ch == '_' || ch == '$';
  }

  public CharSequence getTokenText() {
    return source.subSequence(offset, offset + length);
  }

  public int getTokenLength() {
    return length;
  }

  public int getLine() {
    return line;
  }

  public int getColumn() {
    return column;
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
    if (lcCal) {
      boolean r = false;
      for (int i = offset; i < offset + length; i++) {
        char ch = charAt(i);
        if (ch == '\r') {
          r = true;
          line++;
          column = 0;
        } else if (ch == '\n') {
          if (r) {
            r = false;
            continue;
          }
          line++;
          column = 0;
        } else {
          r = false;
          column++;
        }
      }
    }
    index = index + length;
    offset = offset + length;
    if (offset >= bufferLen) return Tokens.EOF;
    char ch = source.charAt(offset);
    length = 1;
    if (ch == '\n') return Tokens.NEWLINE;
    else if (ch == '\r') {
      scanNewline();
      return Tokens.NEWLINE;
    } else if (isWhitespace(ch)) {
      char chLocal;
      while (offset + length < bufferLen && isWhitespace(chLocal = charAt(offset + length))) {
        if (chLocal == '\r' || chLocal == '\n') break;
        length++;
      }
      return Tokens.WHITESPACE;
    } else {
      if (isIdentifierStart(ch)) return scanIdentifier(ch);
      if (isPrimeDigit(ch)) return scanNumber();
      switch (ch) {
        case ';':
          return Tokens.SEMICOLON;
        case '(':
          return Tokens.LPAREN;
        case ')':
          return Tokens.RPAREN;
        case ':':
          return Tokens.COLON;
        case '<':
          return Tokens.LT;
        case '>':
          return Tokens.GT;
        case '=':
          return scanOperatorTwo(Tokens.EQ, '=', Tokens.ASSIGN);
        case '.':
          return Tokens.DOT;
        case '@':
          return Tokens.AT;
        case '{':
          return Tokens.LBRACE;
        case '}':
          return Tokens.RBRACE;
        case '/':
          return scanDIV();
        case '*':
          return Tokens.MULT;
        case '-':
          return Tokens.MINUS;
        case '+':
          return Tokens.PLUS;
        case '[':
          return Tokens.LBRACK;
        case ']':
          return Tokens.RBRACK;
        case ',':
          return Tokens.COMMA;
        case '!':
          return Tokens.NOT;
        case '&':
          return scanOperatorTwo(Tokens.AND, '&', Tokens.AND);
        case '|':
          return scanOperatorTwo(Tokens.OR, '|', Tokens.OR);
        case '%':
          return Tokens.MOD;
        case '\'':
          scanSingleQuoteString();
          return Tokens.STRING_LITERAL;
        case '"':
          scanStringLiteral();
          return Tokens.STRING_LITERAL;
        default:
          return Tokens.UNKNOWN;
      }
    }
  }

  protected void scanNewline() {
    if (offset + length < bufferLen && charAt(offset + length) == '\n') length++;
  }

  protected Tokens scanIdentifier(char ch) {
    TrieTree.Node<Tokens> n = keywords.root.map.get(ch);
    while (offset + length < bufferLen && isIdentifierPart(ch = charAt(offset + length))) {
      length++;
      n = n == null ? null : n.map.get(ch);
    }
    return n == null ? Tokens.IDENTIFIER : (n.token == null ? Tokens.IDENTIFIER : n.token);
  }

  protected void scanTrans() {
    if (offset + length == bufferLen) return;
    char ch = charAt();
    if (ch == '\\'
        || ch == 't'
        || ch == 'f'
        || ch == 'n'
        || ch == 'r'
        || ch == '0'
        || ch == '"'
        || ch == '\''
        || ch == 'b'
        || ch == '$') {
      length++;
    } else if (ch == 'u') {
      length++;
      for (int i = 0; i < 4; i++) {
        if (offset + length >= bufferLen || !isDigit(charAt(offset + length))) return;
        length++;
      }
    }
  }

  protected void scanStringLiteral() {
    if (offset + 2 < bufferLen && charAt(offset + 1) == '"' && charAt(offset + 2) == '"') {
      length += 2;
      while (offset + length + 2 < bufferLen) {
        if (charAt(offset + length) == '"'
            && charAt(offset + length + 1) == '"'
            && charAt(offset + length + 2) == '"') {
          length += 3;
          return;
        }
        length++;
      }
      return;
    }
    if (offset + length == bufferLen) return;
    char ch;
    while (offset + length < bufferLen && (ch = charAt(offset + length)) != '"') {
      if (ch == '\\') {
        length++;
        scanTrans();
      } else {
        if (ch == '\n') return;
        length++;
      }
    }
    if (offset + length < bufferLen) length++;
  }

  protected void scanSingleQuoteString() {
    if (offset + length == bufferLen) return;
    char ch;
    while (offset + length < bufferLen && (ch = charAt(offset + length)) != '\'') {
      if (ch == '\\') {
        length++;
        scanTrans();
      } else {
        if (ch == '\n') return;
        length++;
      }
    }
    if (offset + length < bufferLen) length++;
  }

  protected Tokens scanNumber() {
    if (offset + length == bufferLen) return Tokens.INTEGER_LITERAL;
    boolean flag = false;
    char ch = charAt(offset);
    if (ch == '0') {
      if (charAt() == 'x' || charAt() == 'X') length++;
      flag = true;
    }
    while (offset + length < bufferLen && isDigit(charAt())) length++;
    if (offset + length == bufferLen) return Tokens.INTEGER_LITERAL;
    ch = charAt();
    if (ch == '.') {
      if (flag) return Tokens.INTEGER_LITERAL;
      length++;
      while (offset + length < bufferLen && isPrimeDigit(charAt())) length++;
      return Tokens.FLOATING_POINT_LITERAL;
    } else if (ch == 'l' || ch == 'L') {
      length++;
      return Tokens.INTEGER_LITERAL;
    } else if (ch == 'F' || ch == 'f' || ch == 'D' || ch == 'd') {
      length++;
      return Tokens.FLOATING_POINT_LITERAL;
    } else return Tokens.INTEGER_LITERAL;
  }

  protected Tokens scanDIV() {
    if (offset + 1 == bufferLen) return Tokens.DIV;
    char ch = charAt();
    if (ch == '/') {
      length++;
      while (offset + length < bufferLen && charAt() != '\n') length++;
      return Tokens.LINE_COMMENT;
    } else if (ch == '*') {
      length++;
      char pre = '\0', curr = '\0';
      boolean finished = false;
      while (offset + length < bufferLen) {
        pre = curr;
        curr = charAt();
        if (curr == '/' && pre == '*') {
          length++;
          finished = true;
          break;
        }
        length++;
      }
      return finished ? Tokens.LONG_COMMENT_COMPLETE : Tokens.LONG_COMMENT_INCOMPLETE;
    } else return Tokens.DIV;
  }

  protected Tokens scanOperatorTwo(Tokens single, char nextChar, Tokens doubles) {
    if (offset + 1 < bufferLen && charAt(offset + 1) == nextChar) {
      length++;
      return doubles;
    }
    return single;
  }

  protected static boolean isDigit(char c) {
    return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
  }

  protected static boolean isPrimeDigit(char c) {
    return c >= '0' && c <= '9';
  }

  protected static boolean isWhitespace(char c) {
    return c == '\t' || c == ' ' || c == '\f' || c == '\n' || c == '\r';
  }

  protected static void doStaticInit() {
    String[] sKeywords = {
      "apply",
      "plugin",
      "plugins",
      "dependencies",
      "repositories",
      "android",
      "implementation",
      "testImplementation",
      "debugImplementation",
      "compileOnly",
      "runtimeOnly",
      "kapt",
      "annotationProcessor",
      "classpath",
      "buildscript",
      "allprojects",
      "subprojects",
      "task",
      "ext",
      "def",
      "if",
      "else",
      "return",
      "true",
      "false",
      "null",
      "new",
      "import"
    };
    Tokens[] sTokens = {
      Tokens.APPLY,
      Tokens.PLUGIN,
      Tokens.PLUGINS,
      Tokens.DEPENDENCIES,
      Tokens.REPOSITORIES,
      Tokens.ANDROID,
      Tokens.IMPLEMENTATION,
      Tokens.TESTIMPLEMENTATION,
      Tokens.DEBUG_IMPLEMENTATION,
      Tokens.COMPILE_ONLY,
      Tokens.RUNTIME_ONLY,
      Tokens.KAPT,
      Tokens.ANNOTATIONPROCESSOR,
      Tokens.CLASSPATH,
      Tokens.BUILDSCRIPT,
      Tokens.ALLPROJECTS,
      Tokens.SUBPROJECTS,
      Tokens.TASK,
      Tokens.EXT,
      Tokens.DEF,
      Tokens.IF,
      Tokens.ELSE,
      Tokens.RETURN,
      Tokens.TRUE,
      Tokens.FALSE,
      Tokens.NULL_LITERAL,
      Tokens.NEW,
      Tokens.IMPORT
    };
    keywords = new TrieTree<>();
    for (int i = 0; i < sKeywords.length; i++) {
      keywords.put(sKeywords[i], sTokens[i]);
    }
  }
}
