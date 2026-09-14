/**
 * Comment by ghost ide
 *
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.xml;

import io.github.rosemoe.sora.util.TrieTree;

public class XmlTextTokenizer {

  private CharSequence source;

  private int bufferLen;

  public int offset;

  public int length;

  private XmlTokens currToken;

  private boolean insideTag;

  public XmlTextTokenizer(CharSequence src) {
    if (src == null) throw new IllegalArgumentException("src cannot be null");
    this.source = src;
    init();
  }

  private void init() {
    length = 0;
    offset = 0;
    currToken = XmlTokens.WHITESPACE;
    this.bufferLen = source.length();
    insideTag = false;
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

  public boolean isInsideTag() {
    return insideTag;
  }

  public void setInsideTag(boolean insideTag) {
    this.insideTag = insideTag;
  }

  public XmlTokens nextToken() {
    return currToken = nextTokenInternal();
  }

  private XmlTokens nextTokenInternal() {
    offset += length;
    if (offset >= bufferLen) return XmlTokens.EOF;
    char ch = source.charAt(offset);
    length = 1;
    if (ch == '\n') return XmlTokens.NEWLINE;
    if (ch == '\r') {
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '\n') length++;
      return XmlTokens.NEWLINE;
    }
    if (isWhitespace(ch)) {
      while (offset + length < bufferLen && isWhitespace(source.charAt(offset + length))) {
        length++;
      }
      return XmlTokens.WHITESPACE;
    }
    if (ch == '<') {
      insideTag = true;
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '!') {
        length++;
        if (offset + 2 < bufferLen && source.charAt(offset + 2) == '-') {
          length++;
          if (offset + 3 < bufferLen && source.charAt(offset + 3) == '-') {
            length++;
            char pre = 0, cur = 0;
            boolean finished = false;
            while (offset + length < bufferLen) {
              pre = cur;
              cur = source.charAt(offset + length);
              if (pre == '-' && cur == '-') {
                length++;
                if (offset + length < bufferLen && source.charAt(offset + length) == '>') {
                  length++;
                  finished = true;
                  break;
                }
              } else {
                length++;
              }
            }
            insideTag = false;
            return finished ? XmlTokens.BLOCK_COMMENT_COMPLETE : XmlTokens.BLOCK_COMMENT_INCOMPLETE;
          }
          return XmlTokens.UNKNOWN;
        }
        if (offset + 2 < bufferLen && source.charAt(offset + 2) == '[') {
          if (offset + 3 < bufferLen && source.charAt(offset + 3) == 'C') {
            length += 7;
            insideTag = false;
            return XmlTokens.CDATA_START;
          }
        }
        if (offset + 2 < bufferLen && source.charAt(offset + 2) == 'D') {
          length += 7;
          return XmlTokens.DOCTYPE;
        }
        return XmlTokens.UNKNOWN;
      }
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '?') {
        length++;
        while (offset + length < bufferLen) {
          char c = source.charAt(offset + length);
          if (c == '?'
              && offset + length + 1 < bufferLen
              && source.charAt(offset + length + 1) == '>') {
            length += 2;
            insideTag = false;
            return XmlTokens.PROCESSING_INSTRUCTION;
          }
          length++;
        }
        return XmlTokens.PROCESSING_INSTRUCTION;
      }
      if (offset + 1 < bufferLen && source.charAt(offset + 1) == '/') {
        length++;
        return XmlTokens.TAG_OPEN_SLASH;
      }
      return XmlTokens.TAG_OPEN;
    }
    if (ch == '>') {
      insideTag = false;
      return XmlTokens.TAG_CLOSE;
    }
    if (ch == '/' && offset + 1 < bufferLen && source.charAt(offset + 1) == '>') {
      length++;
      insideTag = false;
      return XmlTokens.TAG_SELF_CLOSE;
    }
    if (ch == '&') {
      while (offset + length < bufferLen && source.charAt(offset + length) != ';') {
        length++;
      }
      if (offset + length < bufferLen) {
        length++;
      }
      return XmlTokens.ENTITY;
    }
    if (ch == ']' && offset + 1 < bufferLen && source.charAt(offset + 1) == ']') {
      length++;
      if (offset + 2 < bufferLen && source.charAt(offset + 2) == '>') {
        length++;
        return XmlTokens.CDATA_END;
      }
      return XmlTokens.UNKNOWN;
    }
    if (ch == '"' || ch == '\'') {
      scanStringLiteral(ch);
      return insideTag ? XmlTokens.ATTRIBUTE_VALUE : XmlTokens.STRING_LITERAL;
    }
    if (isIdentifierStart(ch)) {
      return scanIdentifier(ch);
    }
    switch (ch) {
      case '(':
        return XmlTokens.LPAREN;
      case ')':
        return XmlTokens.RPAREN;
      case '{':
        return XmlTokens.LBRACE;
      case '}':
        return XmlTokens.RBRACE;
      case '[':
        return XmlTokens.LBRACK;
      case ']':
        return XmlTokens.RBRACK;
      case ';':
        return XmlTokens.SEMICOLON;
      case ':':
        return XmlTokens.COLON;
      case ',':
        return XmlTokens.COMMA;
      case '.':
        return XmlTokens.DOT;
      case '=':
        return XmlTokens.ASSIGN;
      default:
        return XmlTokens.UNKNOWN;
    }
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
          if (next == 'n' || next == 'r' || next == 't' || next == '\\' || next == quote) {
            length++;
          }
        }
      } else {
        length++;
      }
    }
  }

  private XmlTokens scanIdentifier(char first) {
    StringBuilder sb = new StringBuilder();
    sb.append(first);
    while (offset + length < bufferLen && isIdentifierPart(source.charAt(offset + length))) {
      sb.append(source.charAt(offset + length));
      length++;
    }
    String word = sb.toString();
    if (insideTag) {
      int j = offset + length;
      if (j < bufferLen) {
        int tempLength = 0;
        while (j + tempLength < bufferLen && isWhitespace(source.charAt(j + tempLength))) {
          tempLength++;
        }
        if (j + tempLength < bufferLen && source.charAt(j + tempLength) == '=') {
          return XmlTokens.ATTRIBUTE_NAME;
        }
      }
      return XmlTokens.TAG_NAME;
    }
    return XmlTokens.IDENTIFIER;
  }

  private static boolean isWhitespace(char c) {
    return c == ' ' || c == '\t' || c == '\f';
  }

  private static boolean isIdentifierStart(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == ':';
  }

  private static boolean isIdentifierPart(char c) {
    return isIdentifierStart(c) || (c >= '0' && c <= '9') || c == '-' || c == '.';
  }
}
