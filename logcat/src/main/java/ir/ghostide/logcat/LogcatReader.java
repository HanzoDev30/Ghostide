package ir.ghostide.logcat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LogcatReader {

  public static List<LogEntry> getCurrentAppLogs() {
    List<LogEntry> logs = new ArrayList<>();
    int pid = android.os.Process.myPid();

    try {
      Process process = Runtime.getRuntime().exec("logcat -d --pid=" + pid + " -v threadtime");
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        LogEntry entry = parseLogLine(line);
        if (entry != null) {
          logs.add(entry);
        }
      }
      reader.close();
      process.destroy();
    } catch (Exception e) {
      e.printStackTrace();
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
