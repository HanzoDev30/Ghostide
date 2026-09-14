package ir.hanzodev1375.ghostide.jgit.jgitandroid.model;

public class ConflictFile {
  private final String path;
  private final String oursContent;
  private final String theirsContent;
  private final String baseContent;

  public ConflictFile(String path, String oursContent, String theirsContent, String baseContent) {
    this.path = path;
    this.oursContent = oursContent;
    this.theirsContent = theirsContent;
    this.baseContent = baseContent;
  }

  public String getPath() {
    return path;
  }

  public String getOursContent() {
    return oursContent;
  }

  public String getTheirsContent() {
    return theirsContent;
  }

  public String getBaseContent() {
    return baseContent;
  }
}
