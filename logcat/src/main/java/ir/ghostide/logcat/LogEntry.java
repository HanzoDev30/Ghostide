package ir.ghostide.logcat;

public class LogEntry {
  private String timestamp;
  private String priority;
  private String tag;
  private String message;

  public LogEntry(String timestamp, String priority, String tag, String message) {
    this.timestamp = timestamp;
    this.priority = priority;
    this.tag = tag;
    this.message = message;
  }

  // getter ها
  public String getTimestamp() {
    return timestamp;
  }

  public String getPriority() {
    return priority;
  }

  public String getTag() {
    return tag;
  }

  public String getMessage() {
    return message;
  }
}
