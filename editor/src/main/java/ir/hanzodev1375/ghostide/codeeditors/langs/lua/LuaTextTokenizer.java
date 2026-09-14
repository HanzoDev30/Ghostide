/**
 * Comment by ghost ide
 *
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.lua;

import io.github.rosemoe.sora.util.MyCharacter;
import io.github.rosemoe.sora.util.TrieTree;

public class LuaTextTokenizer {

  private static TrieTree<LuaTokens> keywords;

  static {
    doStaticInit();
  }

  public static TrieTree<LuaTokens> getTree() {
    return keywords;
  }

  private CharSequence source;

  private int bufferLen;

  private int index;

  public int offset;

  public int length;

  private LuaTokens currToken;

  public LuaTextTokenizer(CharSequence src) {
    if (src == null) throw new IllegalArgumentException("src cannot be null");
    this.source = src;
    init();
  }

  private void init() {
    length = 0;
    index = 0;
    offset = 0;
    currToken = LuaTokens.WHITESPACE;
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

  public LuaTokens nextToken() {
    return currToken = nextTokenInternal();
  }

  private LuaTokens nextTokenInternal() {
    index += length;
    offset += length;
    if (offset >= bufferLen) return LuaTokens.EOF;
    char ch = source.charAt(offset);
    length = 1;
    if (ch == '\n') return LuaTokens.NEWLINE;
    if (ch == '\r') {
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '\n') length++;
      return LuaTokens.NEWLINE;
    }
    if (isWhitespace(ch)) {
      while (offset + length < bufferLen && isWhitespace(source.charAt(offset + length))) {
        length++;
      }
      return LuaTokens.WHITESPACE;
    }
    if (ch == '-' && offset + 1 < bufferLen && source.charAt(offset + 1) == '-') {
      if (offset + 2 < bufferLen && source.charAt(offset + 2) == '[') {
        int bracketLevel = 0;
        int pos = offset + 2;
        while (pos < bufferLen && source.charAt(pos) == '=') {
          bracketLevel++;
          pos++;
        }
        if (pos < bufferLen && source.charAt(pos) == '[') {
          length = pos - offset + 1;
          int closeBracketLevel = bracketLevel;
          boolean found = false;
          while (offset + length + 1 < bufferLen) {
            if (source.charAt(offset + length) == ']') {
              int closePos = offset + length + 1;
              int closeLevel = 0;
              while (closePos < bufferLen && source.charAt(closePos) == '=') {
                closeLevel++;
                closePos++;
              }
              if (closePos < bufferLen
                  && source.charAt(closePos) == ']'
                  && closeLevel == closeBracketLevel) {
                length = closePos - offset + 1;
                found = true;
                break;
              }
            }
            length++;
          }
          return found ? LuaTokens.BLOCK_COMMENT_COMPLETE : LuaTokens.BLOCK_COMMENT_INCOMPLETE;
        }
      }
      while (offset + length < bufferLen && source.charAt(offset + length) != '\n') {
        length++;
      }
      return LuaTokens.LINE_COMMENT;
    }
    if (ch == '"' || ch == '\'') {
      scanStringLiteral(ch);
      return LuaTokens.STRING_LITERAL;
    }
    if (ch == '[' && offset + 1 < bufferLen && source.charAt(offset + 1) == '[') {
      int bracketLevel = 0;
      int pos = offset + 1;
      while (pos < bufferLen && source.charAt(pos) == '=') {
        bracketLevel++;
        pos++;
      }
      if (pos < bufferLen && source.charAt(pos) == '[') {
        length = pos - offset + 1;
        int closeBracketLevel = bracketLevel;
        boolean found = false;
        while (offset + length + 1 < bufferLen) {
          if (source.charAt(offset + length) == ']') {
            int closePos = offset + length + 1;
            int closeLevel = 0;
            while (closePos < bufferLen && source.charAt(closePos) == '=') {
              closeLevel++;
              closePos++;
            }
            if (closePos < bufferLen
                && source.charAt(closePos) == ']'
                && closeLevel == closeBracketLevel) {
              length = closePos - offset + 1;
              found = true;
              break;
            }
          }
          length++;
        }
        return found ? LuaTokens.STRING_LITERAL : LuaTokens.STRING_LITERAL;
      }
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
        return scanOperatorTwo(LuaTokens.ASSIGN, '=', LuaTokens.EQ);
      case '+':
        return scanOperatorTwo(LuaTokens.PLUS, '=', LuaTokens.PLUS_ASSIGN);
      case '-':
        return scanOperatorTwo(LuaTokens.MINUS, '=', LuaTokens.MINUS_ASSIGN);
      case '*':
        return scanOperatorTwo(LuaTokens.STAR, '=', LuaTokens.STAR_ASSIGN);
      case '/':
        return scanOperatorTwo(LuaTokens.SLASH, '=', LuaTokens.SLASH_ASSIGN);
      case '%':
        return scanOperatorTwo(LuaTokens.PERCENT, '=', LuaTokens.PERCENT_ASSIGN);
      case '.':
        return scanDot();
      case '^':
        return LuaTokens.CARET;
      case '&':
        return LuaTokens.AND;
      case '|':
        return LuaTokens.OR;
      case '~':
        return LuaTokens.NOT;
      case '<':
        return scanLT();
      case '>':
        return scanGT();
      case '#':
        return LuaTokens.LENGTH;
      case '{':
        return LuaTokens.LBRACE;
      case '}':
        return LuaTokens.RBRACE;
      case '(':
        return LuaTokens.LPAREN;
      case ')':
        return LuaTokens.RPAREN;
      case '[':
        return LuaTokens.LBRACK;
      case ']':
        return LuaTokens.RBRACK;
      case ';':
        return LuaTokens.SEMICOLON;
      case ':':
        return LuaTokens.COLON;
      case ',':
        return LuaTokens.COMMA;
      default:
        return LuaTokens.UNKNOWN;
    }
  }

  private LuaTokens scanIdentifier(char first) {
    TrieTree.Node<LuaTokens> node = keywords.root.map.get(first);
    while (offset + length < bufferLen && isIdentifierPart(source.charAt(offset + length))) {
      char c = source.charAt(offset + length);
      node = (node == null) ? null : node.map.get(c);
      length++;
    }
    return (node != null && node.token != null) ? node.token : LuaTokens.IDENTIFIER;
  }

  private LuaTokens scanNumber() {
    boolean isFloat = false;
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
    return isFloat ? LuaTokens.FLOATING_LITERAL : LuaTokens.INTEGER_LITERAL;
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
      if (c == 'a' || c == 'b' || c == 'f' || c == 'n' || c == 'r' || c == 't' || c == 'v'
          || c == '\\' || c == '"' || c == '\'' || c == 'z') {
        length++;
      } else if (c == 'x') {
        length++;
        while (offset + length < bufferLen && isHexDigit(source.charAt(offset + length))) length++;
      } else if (isDigit(c)) {
        while (offset + length < bufferLen && isDigit(source.charAt(offset + length))) length++;
      }
    }
  }

  private LuaTokens scanDot() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '.') {
      length++;
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '.') {
        length++;
        return LuaTokens.ELLIPSIS;
      }
      return LuaTokens.CONCAT;
    }
    return LuaTokens.DOT;
  }

  private LuaTokens scanLT() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return LuaTokens.LT;
    }
    return LuaTokens.LT;
  }

  private LuaTokens scanGT() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return LuaTokens.GT;
    }
    return LuaTokens.GT;
  }

  private LuaTokens scanOperatorTwo(LuaTokens single, char nextChar, LuaTokens doubleToken) {
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
      "and",
      "break",
      "do",
      "else",
      "elseif",
      "end",
      "false",
      "for",
      "function",
      "goto",
      "if",
      "in",
      "local",
      "nil",
      "not",
      "or",
      "repeat",
      "return",
      "then",
      "true",
      "until",
      "while"
    };
    LuaTokens[] tokens = {
      LuaTokens.AND_KW,
      LuaTokens.BREAK,
      LuaTokens.DO,
      LuaTokens.ELSE,
      LuaTokens.ELSEIF,
      LuaTokens.END,
      LuaTokens.FALSE,
      LuaTokens.FOR,
      LuaTokens.FUNCTION,
      LuaTokens.GOTO,
      LuaTokens.IF,
      LuaTokens.IN,
      LuaTokens.LOCAL,
      LuaTokens.NIL,
      LuaTokens.NOT_KW,
      LuaTokens.OR_KW,
      LuaTokens.REPEAT,
      LuaTokens.RETURN,
      LuaTokens.THEN,
      LuaTokens.TRUE,
      LuaTokens.UNTIL,
      LuaTokens.WHILE
    };
    keywords = new TrieTree<>();
    for (int i = 0; i < words.length; i++) {
      keywords.put(words[i], tokens[i]);
    }
  }
}
