package ir.hanzodev1375.components.searchdata.model;

import java.util.List;

public class FileSearchResult {
  private final String fileName;
  private final String filePath;
  private final List<ContentMatch> contentMatches;

  public FileSearchResult(String fileName, String filePath, List<ContentMatch> contentMatches) {
    this.fileName = fileName;
    this.filePath = filePath;
    this.contentMatches = contentMatches;
  }

  public String getFileName() {
    return fileName;
  }

  public String getFilePath() {
    return filePath;
  }

  public List<ContentMatch> getContentMatches() {
    return contentMatches;
  }

  public boolean hasContentMatches() {
    return contentMatches != null && !contentMatches.isEmpty();
  }
}
