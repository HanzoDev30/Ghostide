package ir.hanzodev1375.ghostide.history;

public class HistoryEntity {
  public String path;
  public String name;
  public boolean isDirectory;
  public long timestamp;

  public HistoryEntity(String path, String name, boolean isDirectory, long timestamp) {
    this.path = path;
    this.name = name;
    this.isDirectory = isDirectory;
    this.timestamp = timestamp;
  }
}
