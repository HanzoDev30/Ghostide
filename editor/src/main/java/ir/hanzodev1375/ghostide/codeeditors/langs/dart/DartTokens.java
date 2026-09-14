/**
 * Comment by ghost ide
 *
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.dart;

public enum DartTokens {
  WHITESPACE,
  NEWLINE,
  EOF,
  UNKNOWN,
  // کامنت‌ها
  // //
  LINE_COMMENT,
  // ///
  LINE_COMMENT_DOC,
  BLOCK_COMMENT_COMPLETE,
  BLOCK_COMMENT_INCOMPLETE,
  // علائم و پرانتزها
  LBRACE,
  RBRACE,
  LPAREN,
  RPAREN,
  LBRACK,
  RBRACK,
  SEMICOLON,
  COLON,
  COMMA,
  DOT,
  AT,
  // ...
  ELLIPSIS,
  QUESTION,
  EXCLAMATION,
  TILDE,
  // عملگرها
  PLUS,
  MINUS,
  STAR,
  SLASH,
  PERCENT,
  CARET,
  AMPERSAND,
  PIPE,
  LT,
  GT,
  ASSIGN,
  PLUS_ASSIGN,
  MINUS_ASSIGN,
  STAR_ASSIGN,
  SLASH_ASSIGN,
  PERCENT_ASSIGN,
  CARET_ASSIGN,
  AMPERSAND_ASSIGN,
  PIPE_ASSIGN,
  // ==
  EQ,
  // !=
  NOT_EQ,
  // <=
  LT_EQ,
  // >=
  GT_EQ,
  // ++
  INC,
  // --
  DEC,
  // &&
  LOGICAL_AND,
  // ||
  LOGICAL_OR,
  // ??
  NULL_AWARE,
  // ??=
  NULL_AWARE_ASSIGN,
  // ...
  SPREAD,
  // ..
  CASCADE,
  // ?.
  QUESTION_DOT,
  // !. (non-nullable)
  EXCLAMATION_DOT,
  // =>
  ARROW,
  // ... می‌توان اضافه کرد
  // Literal ها
  INTEGER_LITERAL,
  DOUBLE_LITERAL,
  STRING_LITERAL,
  // true, false
  BOOLEAN_LITERAL,
  NULL_LITERAL,
  IDENTIFIER,
  // کلمات کلیدی
  ABSTRACT,
  AS,
  ASSERT,
  ASYNC,
  AWAIT,
  BREAK,
  CASE,
  CATCH,
  CLASS,
  CONST,
  CONTINUE,
  COVARIANT,
  DEFAULT,
  DEFERRED,
  DO,
  DYNAMIC,
  ELSE,
  ENUM,
  EXPORT,
  EXTENDS,
  EXTENSION,
  EXTERNAL,
  FACTORY,
  FINAL,
  FINALLY,
  FOR,
  // کلمه کلیدی Function (نوع)
  FUNCTION,
  GET,
  HIDE,
  IF,
  IMPLEMENTS,
  IMPORT,
  IN,
  INTERFACE,
  IS,
  LIBRARY,
  MIXIN,
  NEW,
  ON,
  OPERATOR,
  PART,
  REQUIRED,
  RETHROW,
  RETURN,
  SET,
  SHOW,
  STATIC,
  SUPER,
  SWITCH,
  SYNC,
  THIS,
  THROW,
  TRUE,
  FALSE,
  TRY,
  TYPEDEF,
  VAR,
  VOID,
  WHILE,
  WITH,
  YIELD,
  NULL
}
