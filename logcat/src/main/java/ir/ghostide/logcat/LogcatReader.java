package ir.ghostide.logcat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LogcatReader {

  public static final int MAX_LINES = 3000;

  public static List<LogEntry> getCurrentAppLogs() {
    return getCurrentAppLogs(MAX_LINES);
  }

  public static List<LogEntry> getCurrentAppLogs(int maxLines) {
    int limit = Math.max(1, maxLines);
    int pid = android.os.Process.myPid();
    List<LogEntry> logs = new ArrayList<>(Math.min(limit, 1024));
    Process process = null;

    try {
      process =
          Runtime.getRuntime()
              .exec(new String[] {"logcat", "-d", "-v", "threadtime", "-t", String.valueOf(limit), "--pid=" + pid});
      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          LogEntry entry = parseLogLine(line);
          if (entry != null) {
            if (logs.size() == limit) {
              logs.remove(0);
            }
            logs.add(entry);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (process != null) {
        process.destroy();
      }
    }
    return logs;
  }

  private static LogEntry parseLogLine(String line) {
    try {
      if (line.length() < 18) return null;
      String timestamp = line.substring(0, 18);
      String rest = line.substring(18).trim();
      String[] parts = rest.split("\\s+", 4);
      if (parts.length < 4) return null;
      String priorityTag = parts[2];
      char priorityChar = priorityTag.charAt(0);
      String tag = priorityTag.substring(1);
      String message = parts[3];
      return new LogEntry(timestamp, String.valueOf(priorityChar), tag, message);
    } catch (Exception e) {
      return null;
    }
  }
}
