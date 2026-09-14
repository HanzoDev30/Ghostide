package ir.hanzodev1375.components.ftp.model;

import java.util.Locale;

public class FtpEntry {

  private final String name;
  private final String path;
  private final boolean isDirectory;
  private final long size;
  private final long modified;

  public FtpEntry(String name, String path, boolean isDirectory, long size, long modified) {
    this.name = name;
    this.path = path;
    this.isDirectory = isDirectory;
    this.size = size;
    this.modified = modified;
  }

  public String getName() {
    return name;
  }

  public String getPath() {
    return path;
  }

  public boolean isDirectory() {
    return isDirectory;
  }

  public long getSize() {
    return size;
  }

  public long getModified() {
    return modified;
  }

  public String getFormattedSize() {
    if (isDirectory) return "";
    if (size >= 1024 * 1024)
      return String.format(Locale.getDefault(), "%.1f MB", size / (1024.0 * 1024.0));
    else if (size >= 1024) return String.format(Locale.getDefault(), "%.1f KB", size / 1024.0);
    else return size + " B";
  }
}
