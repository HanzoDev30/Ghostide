package ir.hanzodev1375.ghostide.models;

import net.lingala.zip4j.model.FileHeader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ZipEntryModel {

  private final String entryName;
  private final String entryPath;
  private final boolean isDirectory;
  private final long compressedSize;
  private final long uncompressedSize;
  private final long lastModifiedTime;
  private final String parentZipPath;
  private final int compressionLevel;
  private final boolean isEncrypted;

  public ZipEntryModel(FileHeader header, String parentZipPath) {
    String rawName = header.getFileName();

    boolean dir = rawName.endsWith("/") || rawName.endsWith("\\");
    if (dir) rawName = rawName.substring(0, rawName.length() - 1);

    int slash = Math.max(rawName.lastIndexOf('/'), rawName.lastIndexOf('\\'));
    this.entryName = slash >= 0 ? rawName.substring(slash + 1) : rawName;
    this.entryPath = header.getFileName();
    this.isDirectory = dir;
    this.compressedSize = header.getCompressedSize();
    this.uncompressedSize = header.getUncompressedSize();
    this.lastModifiedTime = header.getLastModifiedTimeEpoch();
    this.parentZipPath = parentZipPath;
    this.compressionLevel = header.getCompressionMethod().getCode();
    this.isEncrypted = header.isEncrypted();
  }

  public ZipEntryModel(String name, String path, String parentZipPath) {
    this.entryName = name;
    this.entryPath = path.endsWith("/") ? path : path + "/";
    this.isDirectory = true;
    this.compressedSize = 0;
    this.uncompressedSize = 0;
    this.lastModifiedTime = 0;
    this.parentZipPath = parentZipPath;
    this.compressionLevel = 0;
    this.isEncrypted = false;
  }

  public String getName() {
    return entryName;
  }

  public String getEntryPath() {
    return entryPath;
  }

  public boolean isDirectory() {
    return isDirectory;
  }

  public long getCompressedSize() {
    return compressedSize;
  }

  public long getUncompressedSize() {
    return uncompressedSize;
  }

  public long getLastModifiedTime() {
    return lastModifiedTime;
  }

  public String getParentZipPath() {
    return parentZipPath;
  }

  public boolean isEncrypted() {
    return isEncrypted;
  }

  public String getFormattedSize() {
    if (isDirectory) return "";
    long size = uncompressedSize;
    if (size >= 1024 * 1024) {
      return String.format(Locale.getDefault(), "%.2fM", size / (1024.0 * 1024.0));
    } else if (size >= 1024) {
      return String.format(Locale.getDefault(), "%.1fK", size / 1024.0);
    } else {
      return size + "B";
    }
  }

  public String getLastModifiedFormatted() {
    if (lastModifiedTime == 0) return "";
    SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm", Locale.getDefault());
    return sdf.format(new Date(lastModifiedTime));
  }

  public String getSubtitle() {
    String date = getLastModifiedFormatted();
    String size = getFormattedSize();
    if (isDirectory) return date;
    if (date.isEmpty()) return size;
    if (size.isEmpty()) return date;
    return date + "  " + size;
  }
}
