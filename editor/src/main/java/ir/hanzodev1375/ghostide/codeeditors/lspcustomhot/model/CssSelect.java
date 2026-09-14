package ir.hanzodev1375.ghostide.codeeditors.lspcustomhot.model;

public class CssSelect {
  private final String name;
  private final String markdownDoc;

  public CssSelect(String name, String markdownDoc) {
    this.name = name;
    this.markdownDoc = markdownDoc;
  }

  public String getName() {
    return name;
  }

  public String getMarkdownDoc() {
    return markdownDoc;
  }
}
