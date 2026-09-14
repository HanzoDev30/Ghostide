package ir.hanzodev1375.ghostide.codeeditors.langs.antlr4base;

import io.github.rosemoe.sora.util.MyCharacter;

public class CharParser {
  public static boolean parserJava(char c) {
    return MyCharacter.isJavaIdentifierPart(c) || c == '.';
  }

  public static boolean parserHtml(char c) {
    return Character.isLetter(c) | Character.isDigit(c)
        || c == '>'
        || c == '+'
        || c == '.'
        || c == '<'
        || c == ':'
        || c == '='
        || c == '*'
        || c == '/'; // file path /user/
  }
}
