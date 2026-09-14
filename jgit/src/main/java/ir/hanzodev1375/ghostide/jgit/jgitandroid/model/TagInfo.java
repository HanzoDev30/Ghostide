package ir.hanzodev1375.ghostide.jgit.jgitandroid.model;

public class TagInfo {
  private final String name;
  private final String hash;
  private final String message;
  private final long timestamp;

  public TagInfo(String name, String hash, String message, long timestamp) {
    this.name = name;
    this.hash = hash;
    this.message = message;
    this.timestamp = timestamp;
  }

  public String getName() { return name; }
  public String getHash() { return hash; }
  public String getMessage() { return message; }
  public long getTimestamp() { return timestamp; }
}
