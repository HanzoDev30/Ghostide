/**
 * Comment by ghost ide
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.xml;

public enum XmlTokens {

    WHITESPACE,
    NEWLINE,
    EOF,
    UNKNOWN,
    LINE_COMMENT,
    BLOCK_COMMENT_COMPLETE,
    BLOCK_COMMENT_INCOMPLETE,
    // <
    TAG_OPEN,
    // >
    TAG_CLOSE,
    // />
    TAG_SELF_CLOSE,
    // </
    TAG_OPEN_SLASH,
    // نام تگ
    TAG_NAME,
    // ویژگی
    ATTRIBUTE_NAME,
    // مقدار ویژگی
    ATTRIBUTE_VALUE,
    // متن بین تگ ها
    TEXT,
    // <![CDATA[
    CDATA_START,
    // ]]>
    CDATA_END,
    // <!DOCTYPE
    DOCTYPE,
    // &nbsp;
    ENTITY,
    // <?xml ... ?>
    PROCESSING_INSTRUCTION,
    LPAREN,
    RPAREN,
    LBRACE,
    RBRACE,
    LBRACK,
    RBRACK,
    SEMICOLON,
    COLON,
    COMMA,
    DOT,
    EQ,
    ASSIGN,
    INTEGER_LITERAL,
    STRING_LITERAL,
    IDENTIFIER
}
