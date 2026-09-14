package ir.hanzodev1375.components.searchdata.model;

public class SearchQuery {
  private final String query;
  private final String rootPath;
  private final SearchMode mode;
  private final SearchType type;

  public SearchQuery(String query, String rootPath, SearchMode mode, SearchType type) {
    this.query = query;
    this.rootPath = rootPath;
    this.mode = mode;
    this.type = type;
  }

  public String getQuery() {
    return query;
  }

  public String getRootPath() {
    return rootPath;
  }

  public SearchMode getMode() {
    return mode;
  }

  public SearchType getType() {
    return type;
  }

  public boolean isValid() {
    return query != null && !query.trim().isEmpty() && rootPath != null;
  }
}
