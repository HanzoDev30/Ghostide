/**
 * Comment by ghost ide
 *
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.antlr;

import io.github.rosemoe.sora.util.TrieTree;

public class AntlrTextTokenizer {

  private static TrieTree<AntlrTokens> keywords;

  static {
    doStaticInit();
  }

  public static TrieTree<AntlrTokens> getTree() {
    return keywords;
  }

  private CharSequence source;

  private int bufferLen;

  public int offset;

  public int length;

  private AntlrTokens currToken;

  public AntlrTextTokenizer(CharSequence src) {
    if (src == null) throw new IllegalArgumentException("src cannot be null");
    this.source = src;
    init();
  }

  private void init() {
    length = 0;
    offset = 0;
    currToken = AntlrTokens.WHITESPACE;
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

  public AntlrTokens nextToken() {
    return currToken = nextTokenInternal();
  }

  private AntlrTokens nextTokenInternal() {
    offset += length;
    if (offset >= bufferLen) return AntlrTokens.EOF;
    char ch = source.charAt(offset);
    length = 1;
    if (ch == '\n') return AntlrTokens.NEWLINE;
    if (ch == '\r') {
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '\n') length++;
      return AntlrTokens.NEWLINE;
    }
    if (isWhitespace(ch)) {
      while (offset + length < bufferLen && isWhitespace(source.charAt(offset + length))) {
        length++;
      }
      return AntlrTokens.WHITESPACE;
    }
    if (ch == '/' && offset + 1 < bufferLen) {
      char next = source.charAt(offset + 1);
      if (next == '/') {
        while (offset + length < bufferLen && source.charAt(offset + length) != '\n') {
          length++;
        }
        return AntlrTokens.LINE_COMMENT;
      } 
      if (next == '*') {
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
        return finished ? AntlrTokens.BLOCK_COMMENT_COMPLETE : AntlrTokens.BLOCK_COMMENT_INCOMPLETE;
      }
    }
    if (ch == '"') {
      while (offset + length < bufferLen && source.charAt(offset + length) != '"') {
        if (source.charAt(offset + length) == '\\') {
          length++;
          if (offset + length < bufferLen) length++;
        } else {
          length++;
        }
      }
      if (offset + length < bufferLen) length++;
      return AntlrTokens.STRING_LITERAL;
    }
    if (ch == '\'') {
      while (offset + length < bufferLen && source.charAt(offset + length) != '\'') {
        if (source.charAt(offset + length) == '\\') {
          length++;
          if (offset + length < bufferLen) length++;
        } else {
          length++;
        }
      }
      if (offset + length < bufferLen) length++;
      return AntlrTokens.STRING_LITERAL;
    }
    if (ch == '{') {
      int braceCount = 1;
      while (offset + length < bufferLen && braceCount > 0) {
        if (source.charAt(offset + length) == '{') braceCount++;
        else if (source.charAt(offset + length) == '}') braceCount--;
        length++;
      }
      return AntlrTokens.ACTION;
    }
    if (isDigit(ch)
        || (ch == '.' && offset + 1 < bufferLen && isDigit(source.charAt(offset + 1)))) {
      while (offset + length < bufferLen && isDigit(source.charAt(offset + length))) {
        length++;
      }
      return AntlrTokens.INTEGER_LITERAL;
    }
    if (isIdentifierStart(ch)) {
      return scanIdentifier(ch);
    }
    switch (ch) {
      case '(':
        return AntlrTokens.LPAREN;
      case ')':
        return AntlrTokens.RPAREN;
      case '{':
        return AntlrTokens.LBRACE;
      case '}':
        return AntlrTokens.RBRACE;
      case '[':
        return AntlrTokens.LBRACK;
      case ']':
        return AntlrTokens.RBRACK;
      case ';':
        return AntlrTokens.SEMICOLON;
      case ':':
        return AntlrTokens.COLON;
      case ',':
        return AntlrTokens.COMMA;
      case '.':
        return AntlrTokens.DOT;
      case '=':
        return scanAssign();
      case '<':
        return scanLT();
      case '>':
        return scanGT();
      case '!':
        return scanNot();
      case '+':
        return AntlrTokens.PLUS;
      case '-':
        return AntlrTokens.MINUS;
      case '*':
        return AntlrTokens.STAR;
      case '/':
        return AntlrTokens.SLASH;
      case '%':
        return AntlrTokens.PERCENT;
      case '?':
        return AntlrTokens.QUESTION;
      case '|':
        return AntlrTokens.PIPE;
      case '&':
        return AntlrTokens.AMPERSAND;
      case '^':
        return AntlrTokens.CARET;
      case '~':
        return AntlrTokens.TILDE;
      default:
        return AntlrTokens.UNKNOWN;
    }
  }

  private AntlrTokens scanAssign() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return AntlrTokens.EQ;
    }
    return AntlrTokens.ASSIGN;
  }

  private AntlrTokens scanLT() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return AntlrTokens.LT_EQ;
    }
    return AntlrTokens.LT;
  }

  private AntlrTokens scanGT() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return AntlrTokens.GT_EQ;
    }
    return AntlrTokens.GT;
  }

  private AntlrTokens scanNot() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return AntlrTokens.NOT_EQ;
    }
    return AntlrTokens.NOT;
  }

  private AntlrTokens scanIdentifier(char first) {
    StringBuilder sb = new StringBuilder();
    sb.append(first);
    while (offset + length < bufferLen && isIdentifierPart(source.charAt(offset + length))) {
      sb.append(source.charAt(offset + length));
      length++;
    }
    String word = sb.toString();
    AntlrTokens tok = keywords.get(word, 0, word.length());
    if (tok != null) {
      return tok;
    }
    if (!word.isEmpty()) {
      if (Character.isUpperCase(word.charAt(0))) {
        return AntlrTokens.TOKEN_REF;
      }
      if (Character.isLowerCase(word.charAt(0))) {
        return AntlrTokens.RULE_REF;
      }
    }
    return AntlrTokens.IDENTIFIER;
  }

  private static boolean isWhitespace(char c) {
    return c == ' ' || c == '\t' || c == '\f';
  }

  private static boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private static boolean isIdentifierStart(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
  }

  private static boolean isIdentifierPart(char c) {
    return isIdentifierStart(c) || isDigit(c);
  }

  private static void doStaticInit() {
    String[] words = {
      "fragment",
      "lexer",
      "parser",
      "grammar",
      "options",
      "tokens",
      "channels",
      "import",
      "mode",
      "pushMode",
      "popMode",
      "more",
      "skip",
      "type",
      "returns",
      "throws",
      "catch",
      "finally",
      "local"
    };
    AntlrTokens[] tokens = {
      AntlrTokens.FRAGMENT,
      AntlrTokens.LEXER,
      AntlrTokens.PARSER,
      AntlrTokens.GRAMMAR,
      AntlrTokens.OPTIONS,
      AntlrTokens.TOKENS,
      AntlrTokens.CHANNELS,
      AntlrTokens.IMPORT,
      AntlrTokens.MODE,
      AntlrTokens.PUSH_MODE,
      AntlrTokens.POP_MODE,
      AntlrTokens.MORE,
      AntlrTokens.SKIP,
      AntlrTokens.TYPE,
      AntlrTokens.RETURNS,
      AntlrTokens.THROWS,
      AntlrTokens.CATCH,
      AntlrTokens.FINALLY,
      AntlrTokens.LOCAL
    };
    keywords = new TrieTree<>();
    for (int i = 0; i < words.length; i++) {
      keywords.put(words[i], tokens[i]);
    }
  }
}
