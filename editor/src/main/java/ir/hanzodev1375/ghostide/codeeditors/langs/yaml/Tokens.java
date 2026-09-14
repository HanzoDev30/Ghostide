package ir.hanzodev1375.ghostide.codeeditors.langs.yaml;

public enum Tokens {
  WHITESPACE, NEWLINE, UNKNOWN, EOF,

  // Comments
  LINE_COMMENT,           // # comment

  // Document markers
  DOC_START,              // ---
  DOC_END,                // ...

  // Keys
  KEY,                    // key: (before the colon)
  IDENTIFIER,             // plain word used as key or value (for auto-complete)
  COLON,                  // :
  COLON_SPACE,            // ": " separator

  // Values
  STRING_LITERAL,         // "quoted" or 'quoted'
  SCALAR_VALUE,           // plain unquoted value
  INTEGER_LITERAL,        // 42  0x1A  0o17  0b1010
  FLOATING_POINT_LITERAL, // 3.14  1.0e5  .inf  .nan
  BOOLEAN_LITERAL,        // true false yes no on off
  NULL_LITERAL,           // null ~

  // Block scalars
  BLOCK_LITERAL_HEADER,   // |  or  |2-  etc.
  BLOCK_FOLDED_HEADER,    // >  or  >-  etc.
  BLOCK_SCALAR_CONTENT,   // lines inside a block scalar

  // Structure
  LIST_MARKER,            // - (sequence item)
  LBRACE,                 // {  (flow mapping)
  RBRACE,                 // }
  LBRACK,                 // [  (flow sequence)
  RBRACK,                 // ]
  COMMA,                  // ,
  PIPE,                   // |  (already in BLOCK_LITERAL_HEADER but standalone)
  GT,                     // >

  // Anchors & aliases
  ANCHOR,                 // &anchorName
  ALIAS,                  // *aliasName

  // Tags
  TAG,                    // !!str  !!int  !<tag:...>

  // Directives
  DIRECTIVE,              // %YAML  %TAG

  // GitHub Actions / template expression
  EXPRESSION,             // ${{ ... }}
}
