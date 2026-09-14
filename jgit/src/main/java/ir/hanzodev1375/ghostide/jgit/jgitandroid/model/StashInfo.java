package ir.hanzodev1375.ghostide.jgit.jgitandroid.model;

public class StashInfo {
  private final int index;
  private final String message;
  private final String shortHash;
  private final long timestamp;

  public StashInfo(int index, String message, String shortHash, long timestamp) {
    this.index = index;
    this.message = message;
    this.shortHash = shortHash;
    this.timestamp = timestamp;
  }

  public int getIndex() { return index; }
  public String getMessage() { return message; }
  public String getShortHash() { return shortHash; }
  public long getTimestamp() { return timestamp; }

  @Override
  public String toString() {
    return "stash@{" + index + "}: " + message;
  }
}
