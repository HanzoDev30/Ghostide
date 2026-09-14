/**
 * Comment by ghost ide
 *
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.antlr;

public enum AntlrTokens {
  WHITESPACE,
  NEWLINE,
  EOF,
  UNKNOWN,
  // //
  LINE_COMMENT,
  BLOCK_COMMENT_COMPLETE,
  BLOCK_COMMENT_INCOMPLETE,
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
  ASSIGN,
  EQ,
  NOT_EQ,
  LT,
  GT,
  LT_EQ,
  GT_EQ,
  PLUS,
  MINUS,
  STAR,
  SLASH,
  PERCENT,
  QUESTION,
  PIPE,
  AMPERSAND,
  CARET,
  TILDE,
  // ANTLR خاص
  // fragment
  FRAGMENT,
  // lexer
  LEXER,
  // parser
  PARSER,
  // grammar
  GRAMMAR,
  // options
  OPTIONS,
  // tokens
  TOKENS,
  // channels
  CHANNELS,
  // import
  IMPORT,
  // mode
  MODE,
  // pushMode
  PUSH_MODE,
  // popMode
  POP_MODE,
  // more
  MORE,
  // skip
  SKIP,
  // type
  TYPE,
  // returns
  RETURNS,
  // throws
  THROWS,
  // catch
  CATCH,
  // finally
  FINALLY,
  // local
  LOCAL,
  // {...}
  ACTION,
  // {...}?
  SEMANTIC_PREDICATE,
  // #label
  LABEL,
  // ruleName (حروف کوچک)
  RULE_REF,
  // TOKEN_NAME (حروف بزرگ)
  TOKEN_REF,
  STRING_LITERAL,
  INTEGER_LITERAL,
  IDENTIFIER,
  NOT
}
