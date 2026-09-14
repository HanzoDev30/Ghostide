package ir.hanzodev1375.components.searchdata.model;

import java.util.List;

public class ContentMatch {
  private final int lineNumber;
  private final String lineText;
  private final List<int[]> matchRanges;

  public ContentMatch(int lineNumber, String lineText, List<int[]> matchRanges) {
    this.lineNumber = lineNumber;
    this.lineText = lineText;
    this.matchRanges = matchRanges;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public String getLineText() {
    return lineText;
  }

  public List<int[]> getMatchRanges() {
    return matchRanges;
  }
}
