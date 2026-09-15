package ir.hanzodev1375.ghostide.snippets;

public class SnippetEntry {

  public String key;
  public String prefix;
  public String description;
  public String body;
  public String scope;

  public SnippetEntry() {}

  public SnippetEntry(String key, String prefix, String description, String body, String scope) {
    this.key = key;
    this.prefix = prefix;
    this.description = description;
    this.body = body;
    this.scope = scope;
  }
}