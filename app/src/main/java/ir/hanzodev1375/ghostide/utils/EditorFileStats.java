package ir.hanzodev1375.ghostide.utils;

import android.content.Context;
import ir.hanzodev1375.ghostide.R;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

public final class EditorFileStats {

  private static final long MAX_SCAN_BYTES = 8L * 1024 * 1024;

  private EditorFileStats() {}

  public static String getLanguageFromPath(String path) {
    if (path == null) return "";
    int dot = path.lastIndexOf('.');
    if (dot == -1 || dot == path.length() - 1) return "";
    String ext = path.substring(dot + 1);
    return ext.substring(0, 1).toUpperCase(Locale.ROOT) + ext.substring(1);
  }

  /** تعداد خط و حجم فایل رو می‌خونه. برای فایل‌های خیلی بزرگ از شمردن خط صرف‌نظر می‌کنیم. */
  public static String formatFileStats(Context context, String filePath) {
    File file = new File(filePath);
    if (!file.isFile()) return "";
    String size = formatFileSize(file.length());
    if (file.length() > MAX_SCAN_BYTES) return size;
    int lines = 0;
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      while (reader.readLine() != null) lines++;
    } catch (IOException e) {
      return size;
    }
    return context.getString(R.string.editor_status_lines, lines, size);
  }

  public static String formatFileSize(long bytes) {
    if (bytes < 1024) return bytes + " B";
    if (bytes < 1024 * 1024) return String.format(Locale.ROOT, "%.1f KB", bytes / 1024.0);
    return String.format(Locale.ROOT, "%.1f MB", bytes / (1024.0 * 1024.0));
  }
}