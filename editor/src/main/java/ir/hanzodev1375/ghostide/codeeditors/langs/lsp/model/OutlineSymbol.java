package ir.hanzodev1375.ghostide.codeeditors.langs.lsp.model;

public class OutlineSymbol {

  public final String name;
  public final int kind;
  public final int depth;
  public final int line;
  public final int column;

  public OutlineSymbol(String name, int kind, int depth, int line, int column) {
    this.name = name;
    this.kind = kind;
    this.depth = depth;
    this.line = line;
    this.column = column;
  }
}