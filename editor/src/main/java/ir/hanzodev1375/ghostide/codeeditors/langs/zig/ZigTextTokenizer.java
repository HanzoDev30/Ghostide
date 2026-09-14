/**
 * Comment by ghost ide
 *
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.zig;

import io.github.rosemoe.sora.util.TrieTree;

public class ZigTextTokenizer {

  private static TrieTree<ZigTokens> keywords;

  private static TrieTree<ZigTokens> primitiveTypes;

  private static TrieTree<ZigTokens> builtinFunctions;

  static {
    doStaticInit();
  }

  public static TrieTree<ZigTokens> getTree() {
    return keywords;
  }

  private CharSequence source;
  private int bufferLen;
  public int offset;
  public int length;
  private ZigTokens currToken;

  public ZigTextTokenizer(CharSequence src) {
    if (src == null) throw new IllegalArgumentException("src cannot be null");
    this.source = src;
    init();
  }

  private void init() {
    length = 0;
    offset = 0;
    currToken = ZigTokens.WHITESPACE;
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

  public ZigTokens nextToken() {
    return currToken = nextTokenInternal();
  }

  private ZigTokens nextTokenInternal() {
    offset += length;
    if (offset >= bufferLen) return ZigTokens.EOF;
    char ch = source.charAt(offset);
    length = 1;
    if (ch == '\n') return ZigTokens.NEWLINE;
    if (ch == '\r') {
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '\n') length++;
      return ZigTokens.NEWLINE;
    }
    if (isWhitespace(ch)) {
      while (offset + length < bufferLen && isWhitespace(source.charAt(offset + length))) {
        length++;
      }
      return ZigTokens.WHITESPACE;
    }
    if (ch == '/' && offset + 1 < bufferLen) {
      char next = source.charAt(offset + 1);
      if (next == '/') {
        while (offset + length < bufferLen && source.charAt(offset + length) != '\n') {
          length++;
        }
        return ZigTokens.LINE_COMMENT;
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
        return finished ? ZigTokens.BLOCK_COMMENT_COMPLETE : ZigTokens.BLOCK_COMMENT_INCOMPLETE;
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
      return ZigTokens.STRING_LITERAL;
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
      return ZigTokens.CHARACTER_LITERAL;
    }
    if (ch == '@' && offset + 1 < bufferLen && isIdentifierStart(source.charAt(offset + 1))) {
      length++;
      while (offset + length < bufferLen && isIdentifierPart(source.charAt(offset + length))) {
        length++;
      }
      return ZigTokens.BUILTIN_FUNCTION;
    }
    if (isDigit(ch)
        || (ch == '.' && offset + 1 < bufferLen && isDigit(source.charAt(offset + 1)))) {
      return scanNumber();
    }
    if (isIdentifierStart(ch)) {
      return scanIdentifier(ch);
    }
    switch (ch) {
      case '(':
        return ZigTokens.LPAREN;
      case ')':
        return ZigTokens.RPAREN;
      case '{':
        return ZigTokens.LBRACE;
      case '}':
        return ZigTokens.RBRACE;
      case '[':
        return ZigTokens.LBRACK;
      case ']':
        return ZigTokens.RBRACK;
      case ';':
        return ZigTokens.SEMICOLON;
      case ':':
        return ZigTokens.COLON;
      case ',':
        return ZigTokens.COMMA;
      case '.':
        return scanDot();
      case '@':
        return ZigTokens.AT;
      case '+':
        return scanPlus();
      case '-':
        return scanMinus();
      case '*':
        return scanStar();
      case '/':
        return ZigTokens.SLASH;
      case '%':
        return scanPercent();
      case '&':
        return scanAnd();
      case '|':
        return scanOr();
      case '^':
        return scanXor();
      case '~':
        return ZigTokens.TILDE;
      case '=':
        return scanAssign();
      case '<':
        return scanLT();
      case '>':
        return scanGT();
      case '!':
        return scanNot();
      default:
        return ZigTokens.UNKNOWN;
    }
  }

  private ZigTokens scanDot() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '.') {
      length++;
      if (offset + 2 < bufferLen && source.charAt(offset + 2) == '.') {
        length++;
        return ZigTokens.ELLIPSIS;
      }
      return ZigTokens.DOUBLE_DOT;
    }
    return ZigTokens.DOT;
  }

  private ZigTokens scanPlus() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '+') {
        length++;
        return ZigTokens.INC;
      }
      if (n == '=') {
        length++;
        return ZigTokens.PLUS_ASSIGN;
      }
    }
    return ZigTokens.PLUS;
  }

  private ZigTokens scanMinus() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '-') {
        length++;
        return ZigTokens.DEC;
      }
      if (n == '=') {
        length++;
        return ZigTokens.MINUS_ASSIGN;
      }
      if (n == '>') {
        length++;
        return ZigTokens.ARROW;
      }
    }
    return ZigTokens.MINUS;
  }

  private ZigTokens scanStar() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return ZigTokens.STAR_ASSIGN;
    }
    return ZigTokens.STAR;
  }

  private ZigTokens scanPercent() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return ZigTokens.PERCENT_ASSIGN;
    }
    return ZigTokens.PERCENT;
  }

  private ZigTokens scanAnd() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '&') {
        length++;
        return ZigTokens.LOGICAL_AND;
      }
      if (n == '=') {
        length++;
        return ZigTokens.AND_ASSIGN;
      }
    }
    return ZigTokens.AMPERSAND;
  }

  private ZigTokens scanOr() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '|') {
        length++;
        return ZigTokens.LOGICAL_OR;
      }
      if (n == '=') {
        length++;
        return ZigTokens.OR_ASSIGN;
      }
    }
    return ZigTokens.PIPE;
  }

  private ZigTokens scanXor() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return ZigTokens.XOR_ASSIGN;
    }
    return ZigTokens.CARET;
  }

  private ZigTokens scanAssign() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '=') {
        length++;
        return ZigTokens.EQ;
      }
      if (n == '>') {
        length++;
        return ZigTokens.FAT_ARROW;
      }
    }
    return ZigTokens.ASSIGN;
  }

  private ZigTokens scanLT() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '=') {
        length++;
        return ZigTokens.LT_EQ;
      }
      if (n == '<') {
        length++;
        return ZigTokens.SHIFT_LEFT;
      }
    }
    return ZigTokens.LT;
  }

  private ZigTokens scanGT() {
    if (offset + 1 < bufferLen) {
      char n = source.charAt(offset + 1);
      if (n == '=') {
        length++;
        return ZigTokens.GT_EQ;
      }
      if (n == '>') {
        length++;
        return ZigTokens.SHIFT_RIGHT;
      }
    }
    return ZigTokens.GT;
  }

  private ZigTokens scanNot() {
    if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
      length++;
      return ZigTokens.NOT_EQ;
    }
    return ZigTokens.NOT;
  }

  private ZigTokens scanNumber() {
    boolean isFloat = false;
    if (offset + 1 < bufferLen && source.charAt(offset) == '0') {
      char next = source.charAt(offset + 1);
      if (next == 'x' || next == 'X') {
        length++;
        while (offset + length < bufferLen && isHexDigit(source.charAt(offset + length))) length++;
        return ZigTokens.INTEGER_LITERAL;
      }
      if (next == 'b' || next == 'B') {
        length++;
        while (offset + length < bufferLen
            && (source.charAt(offset + length) == '0' || source.charAt(offset + length) == '1'))
          length++;
        return ZigTokens.INTEGER_LITERAL;
      }
      if (next == 'o' || next == 'O') {
        length++;
        while (offset + length < bufferLen
            && source.charAt(offset + length) >= '0'
            && source.charAt(offset + length) <= '7') length++;
        return ZigTokens.INTEGER_LITERAL;
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
    return isFloat ? ZigTokens.FLOATING_LITERAL : ZigTokens.INTEGER_LITERAL;
  }

  private ZigTokens scanIdentifier(char first) {
    TrieTree.Node<ZigTokens> node = keywords.root.map.get(first);
    while (offset + length < bufferLen && isIdentifierPart(source.charAt(offset + length))) {
      char c = source.charAt(offset + length);
      node = (node == null) ? null : node.map.get(c);
      length++;
    }
    if (node != null && node.token != null) {
      ZigTokens tok = node.token;
      if (tok == ZigTokens.KEYWORD_TRUE || tok == ZigTokens.KEYWORD_FALSE)
        return ZigTokens.BOOLEAN_LITERAL;
      if (tok == ZigTokens.KEYWORD_NULL || tok == ZigTokens.KEYWORD_UNDEFINED)
        return ZigTokens.NULL_LITERAL;
      return tok;
    }
    String text = source.subSequence(offset, offset + length).toString();
    ZigTokens prim = primitiveTypes.get(text, 0, text.length());
    if (prim != null) return ZigTokens.PRIMITIVE_TYPE;
    return ZigTokens.IDENTIFIER;
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
      "asm",
      "break",
      "call",
      "catch",
      "comptime",
      "const",
      "continue",
      "defer",
      "else",
      "enum",
      "export",
      "extern",
      "fn",
      "for",
      "if",
      "inline",
      "noinline",
      "opaque",
      "or",
      "packed",
      "pub",
      "resume",
      "return",
      "struct",
      "suspend",
      "switch",
      "test",
      "threadlocal",
      "try",
      "union",
      "unreachable",
      "using",
      "var",
      "volatile",
      "while",
      "allowzero",
      "anytype",
      "anyframe",
      "dist",
      "errdefer",
      "error",
      "extensible",
      "interface",
      "nosuspend",
      "proto",
      "section",
      "true",
      "false",
      "null",
      "undefined"
    };
    ZigTokens[] tokens = {
      ZigTokens.KEYWORD_ASM,
      ZigTokens.KEYWORD_BREAK,
      ZigTokens.KEYWORD_CALL,
      ZigTokens.KEYWORD_CATCH,
      ZigTokens.KEYWORD_COMPTIME,
      ZigTokens.KEYWORD_CONST,
      ZigTokens.KEYWORD_CONTINUE,
      ZigTokens.KEYWORD_DEFER,
      ZigTokens.KEYWORD_ELSE,
      ZigTokens.KEYWORD_ENUM,
      ZigTokens.KEYWORD_EXPORT,
      ZigTokens.KEYWORD_EXTERN,
      ZigTokens.KEYWORD_FN,
      ZigTokens.KEYWORD_FOR,
      ZigTokens.KEYWORD_IF,
      ZigTokens.KEYWORD_INLINE,
      ZigTokens.KEYWORD_NOINLINE,
      ZigTokens.KEYWORD_OPAQUE,
      ZigTokens.KEYWORD_OR,
      ZigTokens.KEYWORD_PACKED,
      ZigTokens.KEYWORD_PUB,
      ZigTokens.KEYWORD_RESUME,
      ZigTokens.KEYWORD_RETURN,
      ZigTokens.KEYWORD_STRUCT,
      ZigTokens.KEYWORD_SUSPEND,
      ZigTokens.KEYWORD_SWITCH,
      ZigTokens.KEYWORD_TEST,
      ZigTokens.KEYWORD_THREADLOCAL,
      ZigTokens.KEYWORD_TRY,
      ZigTokens.KEYWORD_UNION,
      ZigTokens.KEYWORD_UNREACHABLE,
      ZigTokens.KEYWORD_USING,
      ZigTokens.KEYWORD_VAR,
      ZigTokens.KEYWORD_VOLATILE,
      ZigTokens.KEYWORD_WHILE,
      ZigTokens.KEYWORD_ALLOWZERO,
      ZigTokens.KEYWORD_ANYTYPE,
      ZigTokens.KEYWORD_ANYFRAME,
      ZigTokens.KEYWORD_DIST,
      ZigTokens.KEYWORD_ERRDEFER,
      ZigTokens.KEYWORD_ERROR,
      ZigTokens.KEYWORD_EXTENSIBLE,
      ZigTokens.KEYWORD_INTERFACE,
      ZigTokens.KEYWORD_NOSUSPEND,
      ZigTokens.KEYWORD_PROTO,
      ZigTokens.KEYWORD_SECTION,
      ZigTokens.KEYWORD_TRUE,
      ZigTokens.KEYWORD_FALSE,
      ZigTokens.KEYWORD_NULL,
      ZigTokens.KEYWORD_UNDEFINED
    };
    keywords = new TrieTree<>();
    for (int i = 0; i < words.length; i++) {
      keywords.put(words[i], tokens[i]);
    }
    String[] prims = {
      "i8",
      "i16",
      "i32",
      "i64",
      "i128",
      "isize",
      "u8",
      "u16",
      "u32",
      "u64",
      "u128",
      "usize",
      "f16",
      "f32",
      "f64",
      "f128",
      "bool",
      "void",
      "noreturn",
      "type",
      "anyerror",
      "anyopaque",
      "c_char",
      "c_short",
      "c_int",
      "c_long",
      "c_longlong"
    };
    primitiveTypes = new TrieTree<>();
    for (String prim : prims) {
      primitiveTypes.put(prim, ZigTokens.PRIMITIVE_TYPE);
    }
    String[] builtins = {
      "as",
      "bitCast",
      "boolToInt",
      "byteSwap",
      "call",
      "cDefine",
      "cInclude",
      "cImport",
      "cUndef",
      "compileError",
      "compileLog",
      "constCast",
      "divExact",
      "divFloor",
      "divTrunc",
      "embedFile",
      "export",
      "extern",
      "fieldParentPtr",
      "floatCast",
      "floatToInt",
      "hasDecl",
      "hasField",
      "import",
      "intCast",
      "intToEnum",
      "intToFloat",
      "intToPtr",
      "max",
      "memcpy",
      "memset",
      "min",
      "mod",
      "mulAdd",
      "mulWide",
      "offsetOf",
      "panic",
      "ptrCast",
      "ptrToInt",
      "rem",
      "returnAddress",
      "setAlignStack",
      "setCold",
      "setEvalBranchQuota",
      "setRuntimeSafety",
      "sizeOf",
      "src",
      "sqrt",
      "trunc",
      "typeInfo",
      "typeName",
      "typeOf",
      "unionInit",
      "workGroupId",
      "workGroupSize"
    };
    builtinFunctions = new TrieTree<>();
    for (String builtin : builtins) {
      builtinFunctions.put(builtin, ZigTokens.BUILTIN_FUNCTION);
    }
  }
}
