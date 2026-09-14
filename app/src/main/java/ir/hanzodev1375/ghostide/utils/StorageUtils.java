package ir.hanzodev1375.ghostide.utils;

import android.content.Context;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Detects the available storage volumes on the device (internal storage and any removable SD card)
 * so the file manager can let the user browse the physical SD card the same way it browses internal
 * storage.
 *
 * <p>Uses {@link Context#getExternalFilesDirs(String)} instead of the hidden StorageVolume APIs: it
 * works on every API level we support (minSdk 26) without extra permissions, and the first entry it
 * returns is always the primary (internal) storage while any additional entries are removable
 * volumes (typically the SD card).
 */
public class StorageUtils {

  public static class StorageEntry {
    public final String path;
    public final String label;
    public final boolean removable;
    public final long totalBytes;
    public final long freeBytes;

    public StorageEntry(String path, String label, boolean removable) {
      this.path = path;
      this.label = label;
      this.removable = removable;
      File root = new File(path);
      this.totalBytes = root.getTotalSpace();
      this.freeBytes = root.getUsableSpace();
    }

    public String getFreeFormatted() {
      return StorageUtils.formatSize(freeBytes);
    }

    public String getTotalFormatted() {
      return StorageUtils.formatSize(totalBytes);
    }
  }

  private static final String APP_FILES_SUFFIX_FORMAT = "/Android/data/%s/files";

  public static String formatSize(long bytes) {
    double gb = bytes / (1024.0 * 1024.0 * 1024.0);
    if (gb >= 1.0) {
      return String.format(java.util.Locale.US, "%.1f GB", gb);
    }
    double mb = bytes / (1024.0 * 1024.0);
    if (mb >= 1.0) {
      return String.format(Locale.US, "%.0f MB", mb);
    }
    return String.format(Locale.US, "%.0f KB", bytes / 1024.0);
  }

  /** Returns every readable storage root, internal storage first. */
  public static List<StorageEntry> getStorageVolumes(Context context) {
    Map<String, StorageEntry> roots = new LinkedHashMap<>();
    File[] dirs = context.getExternalFilesDirs(null);

    if (dirs != null) {
      String suffix = String.format(APP_FILES_SUFFIX_FORMAT, context.getPackageName());
      for (int i = 0; i < dirs.length; i++) {
        File dir = dirs[i];
        if (dir == null) continue;

        String fullPath = dir.getAbsolutePath();
        int idx = fullPath.indexOf(suffix);
        String rootPath = idx > 0 ? fullPath.substring(0, idx) : fullPath;

        File rootFile = new File(rootPath);
        if (!rootFile.exists() || !rootFile.isDirectory()) continue;

        boolean removable = i != 0;
        String label = removable ? "SD Card" : "Internal Storage";
        roots.put(rootPath, new StorageEntry(rootPath, label, removable));
      }
    }

    return new ArrayList<>(roots.values());
  }

  /** Returns the first removable SD card root, or null if none is mounted. */
  public static StorageEntry getSdCardVolume(Context context) {
    for (StorageEntry entry : getStorageVolumes(context)) {
      if (entry.removable) return entry;
    }
    return null;
  }

  /** True if a real removable SD card is currently inserted and mounted. */
  public static boolean hasSdCard(Context context) {
    return getSdCardVolume(context) != null;
  }

  /** Returns true if the given absolute path is the root of a storage volume. */
  public static boolean isStorageRoot(Context context, String path) {
    if (path == null) return false;
    for (StorageEntry entry : getStorageVolumes(context)) {
      if (entry.path.equals(path)) return true;
    }
    return false;
  }
}
