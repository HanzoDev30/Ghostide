package ir.hanzodev1375.ghostide.jgit.jgitandroid.model;

public class BlameInfo {
  private final int lineNumber;
  private final String content;
  private final String shortHash;
  private final String author;
  private final long timestamp;

  public BlameInfo(
      int lineNumber, String content, String shortHash, String author, long timestamp) {
    this.lineNumber = lineNumber;
    this.content = content;
    this.shortHash = shortHash;
    this.author = author;
    this.timestamp = timestamp;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public String getContent() {
    return content;
  }

  public String getShortHash() {
    return shortHash;
  }

  public String getAuthor() {
    return author;
  }

  public long getTimestamp() {
    return timestamp;
  }
}
