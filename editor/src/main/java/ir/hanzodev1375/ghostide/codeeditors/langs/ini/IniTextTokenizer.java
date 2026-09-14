/**
 * Comment by ghost ide
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.ini;

public class IniTextTokenizer {

    private CharSequence source;

    private int bufferLen;

    public int offset;

    public int length;

    private IniTokens currToken;

    public IniTextTokenizer(CharSequence src) {
        if (src == null)
            throw new IllegalArgumentException("src cannot be null");
        this.source = src;
        init();
    }

    private void init() {
        length = 0;
        offset = 0;
        currToken = IniTokens.WHITESPACE;
        this.bufferLen = source.length();
    }

    public void reset(CharSequence src) {
        if (src == null)
            throw new IllegalArgumentException("src cannot be null");
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

    public IniTokens nextToken() {
        return currToken = nextTokenInternal();
    }

    private IniTokens nextTokenInternal() {
        offset += length;
        if (offset >= bufferLen)
            return IniTokens.EOF;
        char ch = source.charAt(offset);
        length = 1;
        if (ch == '\n')
            return IniTokens.NEWLINE;
        if (ch == '\r') {
            if (offset + 1 < bufferLen && source.charAt(offset + 1) == '\n')
                length++;
            return IniTokens.NEWLINE;
        }
        if (isWhitespace(ch)) {
            while (offset + length < bufferLen && isWhitespace(source.charAt(offset + length))) {
                length++;
            }
            return IniTokens.WHITESPACE;
        }
        if (ch == ';' || ch == '#') {
            while (offset + length < bufferLen && source.charAt(offset + length) != '\n') {
                length++;
            }
            return IniTokens.LINE_COMMENT;
        }
        if (ch == '[') {
            while (offset + length < bufferLen && source.charAt(offset + length) != ']') {
                length++;
            }
            if (offset + length < bufferLen)
                length++;
            return IniTokens.SECTION;
        }
        if (ch == '"' || ch == '\'') {
            scanQuotedString(ch);
            return IniTokens.QUOTED_STRING;
        }
        if (isIdentifierStart(ch)) {
            return scanKeyValue(ch);
        }
        switch(ch) {
            case '=':
                if (offset + 1 < bufferLen && source.charAt(offset + 1) == '=') {
                    length++;
                    return IniTokens.EQUAL;
                }
                return IniTokens.ASSIGN;
            case ':':
                return IniTokens.COLON;
            default:
                return IniTokens.UNKNOWN;
        }
    }

    private void scanQuotedString(char quote) {
        while (offset + length < bufferLen && source.charAt(offset + length) != quote) {
            if (source.charAt(offset + length) == '\\') {
                length++;
                if (offset + length < bufferLen)
                    length++;
            } else {
                length++;
            }
        }
        if (offset + length < bufferLen)
            length++;
    }

    private IniTokens scanKeyValue(char first) {
        StringBuilder sb = new StringBuilder();
        sb.append(first);
        while (offset + length < bufferLen && isIdentifierPart(source.charAt(offset + length))) {
            sb.append(source.charAt(offset + length));
            length++;
        }
        String text = sb.toString();
        // بررسی value های boolean
        if (text.equalsIgnoreCase("true") || text.equalsIgnoreCase("false") || text.equalsIgnoreCase("on") || text.equalsIgnoreCase("off") || text.equalsIgnoreCase("yes") || text.equalsIgnoreCase("no")) {
            return IniTokens.BOOLEAN_LITERAL;
        }
        // بررسی عدد
        if (isNumeric(text)) {
            return IniTokens.INTEGER_LITERAL;
        }
        // بررسی اینکه key هست یا value
        // نگاه به جلو برای تشخیص =
        int j = offset + length;
        int tempLength = 0;
        while (j + tempLength < bufferLen && isWhitespace(source.charAt(j + tempLength))) {
            tempLength++;
        }
        if (j + tempLength < bufferLen && (source.charAt(j + tempLength) == '=' || source.charAt(j + tempLength) == ':')) {
            return IniTokens.KEY;
        }
        return IniTokens.VALUE;
    }

    private boolean isNumeric(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            try {
                Double.parseDouble(text);
                return true;
            } catch (NumberFormatException e2) {
                return false;
            }
        }
    }

    private static boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\f';
    }

    private static boolean isIdentifierStart(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == '-';
    }

    private static boolean isIdentifierPart(char c) {
        return isIdentifierStart(c) || (c >= '0' && c <= '9') || c == '.' || c == '_' || c == '-';
    }
}
