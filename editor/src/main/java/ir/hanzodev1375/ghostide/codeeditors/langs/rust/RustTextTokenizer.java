/**
 * Comment by ghost ide
 *
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.rust;

import io.github.rosemoe.sora.util.TrieTree;

public class RustTextTokenizer {

  private static TrieTree<RustTokens> keywords;

  private static TrieTree<RustTokens> primitiveTypes;

  static {
    doStaticInit();
  }

  public static TrieTree<RustTokens> getTree() {
    return keywords;
  }

  private CharSequence source;

  private int bufferLen;

  public int offset;

  public int length;

  private RustTokens currToken;

  public RustTextTokenizer(CharSequence src) {
    if (src == null) throw new IllegalArgumentException("src cannot be null");
    this.source = src;
    init();
  }

  private void init() {
    length = 0;
    offset = 0;
    currToken = RustTokens.WHITESPACE;
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

  public RustTokens nextToken() {
    return currToken = nextTokenInternal();
  }

  private RustTokens nextTokenInternal() {
    offset += length;
    if (offset >= bufferLen) return RustTokens.EOF;
    char ch = source.charAt(offset);
    length = 1;
    if (ch == '\n') return RustTokens.NEWLINE;
    if (ch == '\r') {
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '\n') length++;
      return RustTokens.NEWLINE;
    }
    if (isWhitespace(ch)) {
      while (offset + length < bufferLen && isWhitespace(source.charAt(offset + length))) {
        length++;
      }
      return RustTokens.WHITESPACE;
    }
    if (ch == '/' && offset + 1 < bufferLen) {
      char next = source.charAt(offset + 1);
      if (next == '/') {
        while (offset + length < bufferLen && source.charAt(offset + length) != '\n') {
          length++;
        }
        return RustTokens.LINE_COMMENT;
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
        return finished ? RustTokens.BLOCK_COMMENT_COMPLETE : RustTokens.BLOCK_COMMENT_INCOMPLETE;
      }
    }
    if (ch == '\'' && offset + 1 < bufferLen && isIdentifierPart(source.charAt(offset + 1))) {
      if (offset + 2 < bufferLen && source.charAt(offset + 2) == '\'') {
        length += 2;
        return RustTokens.CHARACTER_LITERAL;
      }
      length++;
      while (offset + length < bufferLen && isIdentifierPart(source.charAt(offset + length))) {
        length++;
      }
      return RustTokens.LIFETIME;
    }
    if (ch == 'b' && offset + 1 < bufferLen && source.charAt(offset + 1) == '"') {
      length++;
      while (offset + length < bufferLen && source.charAt(offset + length) != '"') {
        if (source.charAt(offset + length) == '\\') {
          length++;
          if (offset + length < bufferLen) length++;
        } else {
          length++;
        }
      }
      if (offset + length < bufferLen) length++;
      return RustTokens.BYTE_STRING_LITERAL;
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
      return RustTokens.STRING_LITERAL;
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
      return RustTokens.CHARACTER_LITERAL;
    }
    if (ch == 'r') {
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '#') {
        length++;
        int count = 1;
        while (offset + length < bufferLen && source.charAt(offset + length) == '#') {
          length++;
          count++;
        }
        if (offset + length < bufferLen && source.charAt(offset + length) == '"') {
          length++;
          while (offset + length < bufferLen) {
            if (source.charAt(offset + length) == '"') {
              length++;
              int closeCount = 0;
              while (offset + length < bufferLen && source.charAt(offset + length) == '#') {
                length++;
                closeCount++;
              }
              if (closeCount >= count) break;
            } else {
              length++;
            }
          }
          return RustTokens.STRING_LITERAL;
        }
      }
    }
    if (ch == 'r' && offset + 1 < bufferLen && source.charAt(offset + 1) == '"') {
      length++;
      while (offset + length < bufferLen && source.charAt(offset + length) != '"') {
        length++;
      }
      if (offset + length < bufferLen) length++;
      return RustTokens.STRING_LITERAL;
    }
    if (isDigit(ch)
        || (ch == '.' && offset + 1 < bufferLen && isDigit(source.charAt(offset + 1)))) {
      return scanNumber();
    }
    if (ch == '_') {
      if (offset + length < bufferLen && isIdentifierPart(source.charAt(offset + length))) {
        return scanIdentifier(ch);
      }
      return RustTokens.UNDERSCORE;
    }
    if (isIdentifierStart(ch)) {
      return scanIdentifier(ch);
    }
    switch (ch) {
      case '(':
        return RustTokens.LPAREN;
      case ')':
        return RustTokens.RPAREN;
      case '{':
        return RustTokens.LBRACE;
      case '}':
        return RustTokens.RBRACE;
      case '[':
        return RustTokens.LBRACK;
      case ']':
        return RustTokens.RBRACK;
      case ';':
        return RustTokens.SEMICOLON;
      case ':':
        return scanColon();
      case ',':
        return RustTokens.COMMA;
      case '.':
        return scanDot();
      case '@':
        return RustTokens.AT;
      case '$':
        return RustTokens.DOLLAR;
      case '#':
        return RustTokens.HASH;
      case '&':
        return scanAmpersand();
      case '|':
        return scanPipe();
      case '^':
        return scanXor();
      case '~':
        return RustTokens.TILDE;
      case '=':
        return scanAssign();
      case '<':
        return scanLT();
      case '>':
        return scanGT();
      case '!':
        return scanNot();
      case '+':
        return scanPlus();
      case '-':
        return scanMinus();
      case '*':
        return scanStar();
      case '/':
        return RustTokens.SLASH;
      case '%':
        return scanPercent();
      default:
        return RustTokens.UNKNOWN;
    }
  }

  private RustTokens scanColon() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == ':') {
      length++;
      return RustTokens.PATH_SEP;
    }
    return RustTokens.COLON;
  }

  private RustTokens scanDot() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '.') {
        length++;
        if (offset + 2 < bufferLen && source.charAt(offset + 2) == '=') {
          length++;
          return RustTokens.RANGE_INCLUSIVE;
        }
        if (offset + 2 < bufferLen && source.charAt(offset + 2) == '.') {
          length++;
          return RustTokens.ELLIPSIS;
        }
        return RustTokens.RANGE;
      }
    }
    return RustTokens.DOT;
  }

  private RustTokens scanAmpersand() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '&') {
        length++;
        return RustTokens.LOGICAL_AND;
      }
      if (n == '=') {
        length++;
        return RustTokens.AND_ASSIGN;
      }
      if (n == 'm'
          && offset + 2 < bufferLen
          && source.charAt(offset + 2) == 'u'
          && offset + 3 < bufferLen
          && source.charAt(offset + 3) == 't') {
        length += 3;
        return RustTokens.MUT_REF;
      }
    }
    return RustTokens.AMPERSAND;
  }

  private RustTokens scanPipe() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '|') {
        length++;
        return RustTokens.LOGICAL_OR;
      }
      if (n == '=') {
        length++;
        return RustTokens.OR_ASSIGN;
      }
    }
    return RustTokens.PIPE;
  }

  private RustTokens scanXor() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return RustTokens.XOR_ASSIGN;
    }
    return RustTokens.CARET;
  }

  private RustTokens scanAssign() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '=') {
        length++;
        return RustTokens.EQ;
      }
      if (n == '>') {
        length++;
        return RustTokens.FAT_ARROW;
      }
    }
    return RustTokens.ASSIGN;
  }

  private RustTokens scanLT() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '=') {
        length++;
        return RustTokens.LT_EQ;
      }
      if (n == '<') {
        length++;
        if (offset + 2 < bufferLen && source.charAt(offset + 2) == '=') {
          length++;
          return RustTokens.SHIFT_LEFT;
        }
        return RustTokens.SHIFT_LEFT;
      }
    }
    return RustTokens.LT;
  }

  private RustTokens scanGT() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '=') {
        length++;
        return RustTokens.GT_EQ;
      }
      if (n == '>') {
        length++;
        if (offset + 2 < bufferLen && source.charAt(offset + 2) == '=') {
          length++;
          return RustTokens.SHIFT_RIGHT;
        }
        return RustTokens.SHIFT_RIGHT;
      }
    }
    return RustTokens.GT;
  }

  private RustTokens scanNot() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return RustTokens.NOT_EQ;
    }
    return RustTokens.NOT;
  }

  private RustTokens scanPlus() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '+') {
        length++;
        return RustTokens.INC;
      }
      if (n == '=') {
        length++;
        return RustTokens.PLUS_ASSIGN;
      }
    }
    return RustTokens.PLUS;
  }

  private RustTokens scanMinus() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '-') {
        length++;
        return RustTokens.DEC;
      }
      if (n == '=') {
        length++;
        return RustTokens.MINUS_ASSIGN;
      }
      if (n == '>') {
        length++;
        return RustTokens.ARROW;
      }
    }
    return RustTokens.MINUS;
  }

  private RustTokens scanStar() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return RustTokens.STAR_ASSIGN;
    }
    if (offset + 1 < bufferLen
        && source.charAt(offset + 1) == 'c'
        && offset + 2 < bufferLen
        && source.charAt(offset + 2) == 'o'
        && offset + 3 < bufferLen
        && source.charAt(offset + 3) == 'n'
        && offset + 4 < bufferLen
        && source.charAt(offset + 4) == 's'
        && offset + 5 < bufferLen
        && source.charAt(offset + 5) == 't') {
      length += 5;
      return RustTokens.RAW_POINTER_CONST;
    }
    if (offset + 1 < bufferLen
        && source.charAt(offset + 1) == 'm'
        && offset + 2 < bufferLen
        && source.charAt(offset + 2) == 'u'
        && offset + 3 < bufferLen
        && source.charAt(offset + 3) == 't') {
      length += 3;
      return RustTokens.RAW_POINTER_MUT;
    }
    return RustTokens.STAR;
  }

  private RustTokens scanPercent() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return RustTokens.PERCENT_ASSIGN;
    }
    return RustTokens.PERCENT;
  }

  private RustTokens scanNumber() {
    boolean isFloat = false;
    if (offset + 1 < bufferLen && source.charAt(offset) == '0') {
      char next = source.charAt(offset + 1);
      if (next == 'x' || next == 'X') {
        length++;
        while (offset + length < bufferLen && isHexDigit(source.charAt(offset + length))) length++;
        return RustTokens.INTEGER_LITERAL;
      }
      if (next == 'b' || next == 'B') {
        length++;
        while (offset + length < bufferLen
            && (source.charAt(offset + length) == '0' || source.charAt(offset + length) == '1'))
          length++;
        return RustTokens.INTEGER_LITERAL;
      }
      if (next == 'o' || next == 'O') {
        length++;
        while (offset + length < bufferLen
            && source.charAt(offset + length) >= '0'
            && source.charAt(offset + length) <= '7') length++;
        return RustTokens.INTEGER_LITERAL;
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
    if (offset + length < bufferLen) {
      char suffix = source.charAt(offset + length);
      if (suffix == 'u' || suffix == 'i' || suffix == 'f') {
        length++;
        while (offset + length < bufferLen && isDigit(source.charAt(offset + length))) length++;
      }
    }
    return isFloat ? RustTokens.FLOATING_LITERAL : RustTokens.INTEGER_LITERAL;
  }

  private RustTokens scanIdentifier(char first) {
    int startOffset = offset;
    TrieTree.Node<RustTokens> node = keywords.root.map.get(first);
    while (offset + length < bufferLen && isIdentifierPart(source.charAt(offset + length))) {
      char c = source.charAt(offset + length);
      node = (node == null) ? null : node.map.get(c);
      length++;
    }
    if (node != null && node.token != null) {
      RustTokens tok = node.token;
      if (tok == RustTokens.KEYWORD_TRUE || tok == RustTokens.KEYWORD_FALSE)
        return RustTokens.BOOLEAN_LITERAL;
      if (tok == RustTokens.KEYWORD_SELF || tok == RustTokens.KEYWORD_SELF_TYPE)
        return RustTokens.KEYWORD_SELF;
      // بررسی نوع ابتدایی
      String text = source.subSequence(startOffset, offset + length).toString();
      RustTokens prim = primitiveTypes.get(text, 0, text.length());
      if (prim != null) return RustTokens.PRIMITIVE_TYPE;
      return tok;
    }
    // بررسی ماکرو
    if (offset + length < bufferLen && source.charAt(offset + length) == '!') {
      return RustTokens.MACRO;
    }
    return RustTokens.IDENTIFIER;
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
      "as",
      "break",
      "const",
      "continue",
      "crate",
      "else",
      "enum",
      "extern",
      "false",
      "fn",
      "for",
      "if",
      "impl",
      "in",
      "let",
      "loop",
      "match",
      "mod",
      "move",
      "mut",
      "pub",
      "return",
      "self",
      "Self",
      "static",
      "struct",
      "super",
      "trait",
      "true",
      "type",
      "unsafe",
      "use",
      "where",
      "while",
      "async",
      "await",
      "dyn",
      "ref",
      "box",
      "union",
      "macro_rules",
      "default",
      "final",
      "override",
      "priv",
      "pub(crate)",
      "pub(super)",
      "pub(self)"
    };
    RustTokens[] tokens = {
      RustTokens.KEYWORD_AS,
      RustTokens.KEYWORD_BREAK,
      RustTokens.KEYWORD_CONST,
      RustTokens.KEYWORD_CONTINUE,
      RustTokens.KEYWORD_CRATE,
      RustTokens.KEYWORD_ELSE,
      RustTokens.KEYWORD_ENUM,
      RustTokens.KEYWORD_EXTERN,
      RustTokens.KEYWORD_FALSE,
      RustTokens.KEYWORD_FN,
      RustTokens.KEYWORD_FOR,
      RustTokens.KEYWORD_IF,
      RustTokens.KEYWORD_IMPL,
      RustTokens.KEYWORD_IN,
      RustTokens.KEYWORD_LET,
      RustTokens.KEYWORD_LOOP,
      RustTokens.KEYWORD_MATCH,
      RustTokens.KEYWORD_MOD,
      RustTokens.KEYWORD_MOVE,
      RustTokens.KEYWORD_MUT,
      RustTokens.KEYWORD_PUB,
      RustTokens.KEYWORD_RETURN,
      RustTokens.KEYWORD_SELF,
      RustTokens.KEYWORD_SELF_TYPE,
      RustTokens.KEYWORD_STATIC,
      RustTokens.KEYWORD_STRUCT,
      RustTokens.KEYWORD_SUPER,
      RustTokens.KEYWORD_TRAIT,
      RustTokens.KEYWORD_TRUE,
      RustTokens.KEYWORD_TYPE,
      RustTokens.KEYWORD_UNSAFE,
      RustTokens.KEYWORD_USE,
      RustTokens.KEYWORD_WHERE,
      RustTokens.KEYWORD_WHILE,
      RustTokens.KEYWORD_ASYNC,
      RustTokens.KEYWORD_AWAIT,
      RustTokens.KEYWORD_DYN,
      RustTokens.KEYWORD_REF,
      RustTokens.KEYWORD_BOX,
      RustTokens.KEYWORD_UNION,
      RustTokens.KEYWORD_MACRO_RULES,
      RustTokens.KEYWORD_DEFAULT,
      RustTokens.KEYWORD_FINAL,
      RustTokens.KEYWORD_OVERRIDE,
      RustTokens.KEYWORD_PRIV,
      RustTokens.KEYWORD_PUB_CRATE,
      RustTokens.KEYWORD_PUB_SUPER,
      RustTokens.KEYWORD_PUB_SELF
    };
    keywords = new TrieTree<>();
    for (int i = 0; i < words.length; i++) {
      keywords.put(words[i], tokens[i]);
    }
    String[] prims = {
      "i8", "i16", "i32", "i64", "i128", "isize", "u8", "u16", "u32", "u64", "u128", "usize", "f32",
      "f64", "bool", "char", "str"
    };
    RustTokens[] primTokens = new RustTokens[prims.length];
    for (int i = 0; i < prims.length; i++) {
      primTokens[i] = RustTokens.PRIMITIVE_TYPE;
    }
    primitiveTypes = new TrieTree<>();
    for (int i = 0; i < prims.length; i++) {
      primitiveTypes.put(prims[i], primTokens[i]);
    }
  }
}
