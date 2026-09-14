/**
 * Comment by ghost ide
 *
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.shell;

import io.github.rosemoe.sora.util.TrieTree;

public class ShellTextTokenizer {

  private static TrieTree<ShellTokens> keywords;

  static {
    doStaticInit();
  }

  public static TrieTree<ShellTokens> getTree() {
    return keywords;
  }

  private CharSequence source;

  private int bufferLen;

  public int offset;

  public int length;

  private ShellTokens currToken;

  public ShellTextTokenizer(CharSequence src) {
    if (src == null) throw new IllegalArgumentException("src cannot be null");
    this.source = src;
    init();
  }

  private void init() {
    length = 0;
    offset = 0;
    currToken = ShellTokens.WHITESPACE;
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

  public ShellTokens nextToken() {
    return currToken = nextTokenInternal();
  }

  private ShellTokens nextTokenInternal() {
    offset += length;
    if (offset >= bufferLen) return ShellTokens.EOF;
    char ch = source.charAt(offset);
    length = 1;
    if (ch == '\n') return ShellTokens.NEWLINE;
    if (ch == '\r') {
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '\n') length++;
      return ShellTokens.NEWLINE;
    }
    if (isWhitespace(ch)) {
      while (offset + length < bufferLen && isWhitespace(source.charAt(offset + length))) {
        length++;
      }
      return ShellTokens.WHITESPACE;
    }
    if (ch == '#') {
      while (offset + length < bufferLen && source.charAt(offset + length) != '\n') {
        length++;
      }
      return ShellTokens.LINE_COMMENT;
    }
    if (ch == '"' || ch == '\'') {
      scanStringLiteral(ch);
      return ShellTokens.STRING_LITERAL;
    }
    if (ch == '`') {
      while (offset + length < bufferLen && source.charAt(offset + length) != '`') {
        length++;
      }
      if (offset + length < bufferLen) length++;
      return ShellTokens.COMMAND_SUBSTITUTION;
    }
    if (ch == '$') {
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '(') {
        if (offset + 2 < bufferLen && source.charAt(offset + 2) == '(') {
          length += 3;
          while (offset + length < bufferLen) {
            if (source.charAt(offset + length) == ')'
                && offset + length + 1 < bufferLen
                && source.charAt(offset + length + 1) == ')') {
              length += 2;
              break;
            }
            length++;
          }
          return ShellTokens.ARITHMETIC;
        }
        length++;
        while (offset + length < bufferLen) {
          if (source.charAt(offset + length) == ')') {
            length++;
            break;
          }
          length++;
        }
        return ShellTokens.COMMAND_SUBSTITUTION;
      }
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '{') {
        length++;
        while (offset + length < bufferLen && source.charAt(offset + length) != '}') {
          length++;
        }
        if (offset + length < bufferLen) length++;
        return ShellTokens.PARAM_EXPANSION;
      }
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '#') {
        length++;
        return ShellTokens.PARAM_EXPANSION;
      }
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '?') {
        length++;
        return ShellTokens.PARAM_EXPANSION;
      }
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '0') {
        length++;
        return ShellTokens.PARAM_EXPANSION;
      }
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '*') {
        length++;
        return ShellTokens.PARAM_EXPANSION;
      }
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '@') {
        length++;
        return ShellTokens.PARAM_EXPANSION;
      }
      if (offset + 1 < bufferLen && isDigit(source.charAt(offset + 1))) {
        length++;
        while (offset + length < bufferLen && isDigit(source.charAt(offset + length))) {
          length++;
        }
        return ShellTokens.PARAM_EXPANSION;
      }
      return ShellTokens.DOLLAR;
    }
    if (ch == '{') {
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '}') {
        length++;
        return ShellTokens.BRACE_EXPANSION;
      }
      if (offset + 2 < bufferLen
          && source.charAt(offset + 1) >= '0'
          && source.charAt(offset + 1) <= '9') {
        int j = offset + 1;
        while (j < bufferLen && isDigit(source.charAt(j))) j++;
        if (j < bufferLen
            && source.charAt(j) == '.'
            && j + 1 < bufferLen
            && source.charAt(j + 1) == '.') {
          length = j + 1 - offset;
          while (length < bufferLen && isDigit(source.charAt(offset + length))) length++;
          if (offset + length < bufferLen && source.charAt(offset + length) == '}') {
            length++;
            return ShellTokens.BRACE_EXPANSION;
          }
        }
      }
      return ShellTokens.LBRACE;
    }
    if (ch == '}') return ShellTokens.RBRACE;
    if (ch == '(') return ShellTokens.LPAREN;
    if (ch == ')') return ShellTokens.RPAREN;
    if (ch == '[') return ShellTokens.LBRACK;
    if (ch == ']') return ShellTokens.RBRACK;
    if (ch == ';') return ShellTokens.SEMICOLON;
    if (ch == ':') return ShellTokens.COLON;
    if (ch == ',') return ShellTokens.COMMA;
    if (ch == '.') return ShellTokens.DOT;
    if (ch == '`') return ShellTokens.BACKTICK;
    if (ch == '|') {
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '|') {
        length++;
        return ShellTokens.OR;
      }
      return ShellTokens.PIPE;
    }
    if (ch == '&') {
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '&') {
        length++;
        return ShellTokens.AND;
      }
      return ShellTokens.AMPERSAND;
    }
    if (ch == '<') {
      if (offset + 1 < bufferLen) {
        char n = source.charAt(offset + 1);
        if (n == '<') {
          length++;
          if (offset + 2 < bufferLen && source.charAt(offset + 2) == '<') {
            length++;
            return ShellTokens.HERESTRING;
          }
          return ShellTokens.HERE_DOC;
        }
        if (n == '=') {
          length++;
          return ShellTokens.LT_EQ;
        }
      }
      return ShellTokens.REDIRECT_IN;
    }
    if (ch == '>') {
      if (offset + 1 < bufferLen) {
        char n = source.charAt(offset + 1);
        if (n == '>') {
          length++;
          return ShellTokens.REDIRECT_APPEND;
        }
        if (n == '=') {
          length++;
          return ShellTokens.GT_EQ;
        }
      }
      return ShellTokens.REDIRECT_OUT;
    }
    if (ch == '=') {
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
        length++;
        return ShellTokens.EQ;
      }
      return ShellTokens.ASSIGN;
    }
    if (ch == '!') {
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
        length++;
        return ShellTokens.NOT_EQ;
      }
      return ShellTokens.NOT;
    }
    if (ch == '+') {
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '+') {
        length++;
        return ShellTokens.INC;
      }
      return ShellTokens.PLUS;
    }
    if (ch == '-') {
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '-') {
        length++;
        return ShellTokens.DEC;
      }
      if (offset + 1 < bufferLen && isIdentifierStart(source.charAt(offset + 1))) {
        length++;
        while (offset + length < bufferLen && isIdentifierPart(source.charAt(offset + length))) {
          length++;
        }
        return ShellTokens.TEST_OPERATOR;
      }
      return ShellTokens.MINUS;
    }
    if (ch == '*') return ShellTokens.STAR;
    if (ch == '/') return ShellTokens.SLASH;
    if (ch == '%') return ShellTokens.PERCENT;
    if (isDigit(ch)) {
      while (offset + length < bufferLen && isDigit(source.charAt(offset + length))) {
        length++;
      }
      return ShellTokens.INTEGER_LITERAL;
    }
    if (isIdentifierStart(ch)) {
      return scanIdentifier(ch);
    }
    return ShellTokens.UNKNOWN;
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
        if (offset + length < bufferLen) {
          char next = source.charAt(offset + length);
          if (next == 'n'
              || next == 'r'
              || next == 't'
              || next == '\\'
              || next == quote
              || next == '$') {
            length++;
          }
        }
      } else {
        length++;
      }
    }
  }

  private ShellTokens scanIdentifier(char first) {
    StringBuilder sb = new StringBuilder();
    sb.append(first);
    while (offset + length < bufferLen && isIdentifierPart(source.charAt(offset + length))) {
      sb.append(source.charAt(offset + length));
      length++;
    }
    String word = sb.toString();
    ShellTokens tok = keywords.get(word, 0, word.length());
    if (tok != null) {
      return tok;
    }
    return ShellTokens.IDENTIFIER;
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
    return isIdentifierStart(c) || isDigit(c) || c == '-';
  }

  private static void doStaticInit() {
    String[] words = {
      "if",
      "then",
      "else",
      "elif",
      "fi",
      "for",
      "while",
      "until",
      "do",
      "done",
      "case",
      "esac",
      "in",
      "function",
      "return",
      "exit",
      "source",
      "export",
      "readonly",
      "local",
      "declare",
      "typeset",
      "unset",
      "shift",
      "getopts",
      "select",
      "time",
      "eval",
      "exec",
      "trap",
      "wait",
      "suspend",
      "true",
      "false",
      "echo"
    };
    ShellTokens[] tokens = {
      ShellTokens.KEYWORD_IF,
      ShellTokens.KEYWORD_THEN,
      ShellTokens.KEYWORD_ELSE,
      ShellTokens.KEYWORD_ELIF,
      ShellTokens.KEYWORD_FI,
      ShellTokens.KEYWORD_FOR,
      ShellTokens.KEYWORD_WHILE,
      ShellTokens.KEYWORD_UNTIL,
      ShellTokens.KEYWORD_DO,
      ShellTokens.KEYWORD_DONE,
      ShellTokens.KEYWORD_CASE,
      ShellTokens.KEYWORD_ESAC,
      ShellTokens.KEYWORD_IN,
      ShellTokens.KEYWORD_FUNCTION,
      ShellTokens.KEYWORD_RETURN,
      ShellTokens.KEYWORD_EXIT,
      ShellTokens.KEYWORD_SOURCE,
      ShellTokens.KEYWORD_EXPORT,
      ShellTokens.KEYWORD_READONLY,
      ShellTokens.KEYWORD_LOCAL,
      ShellTokens.KEYWORD_DECLARE,
      ShellTokens.KEYWORD_TYPESET,
      ShellTokens.KEYWORD_UNSET,
      ShellTokens.KEYWORD_SHIFT,
      ShellTokens.KEYWORD_GETOPTS,
      ShellTokens.KEYWORD_SELECT,
      ShellTokens.KEYWORD_TIME,
      ShellTokens.KEYWORD_EVAL,
      ShellTokens.KEYWORD_EXEC,
      ShellTokens.KEYWORD_TRAP,
      ShellTokens.KEYWORD_WAIT,
      ShellTokens.KEYWORD_SUSPEND,
      ShellTokens.BOOLEAN_LITERAL,
      ShellTokens.BOOLEAN_LITERAL,
      ShellTokens.ECHO
    };
    keywords = new TrieTree<>();
    for (int i = 0; i < words.length; i++) {
      keywords.put(words[i], tokens[i]);
    }
  }
}
