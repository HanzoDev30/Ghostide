package ir.hanzodev1375.ghostide.codeeditors.lspcustomhot;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VFSManager {

  private static VFSManager instance;
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private final Map<String, List<File>> directoryCache = new HashMap<>();
  private String projectRoot = null;

  private VFSManager() {}

  public static synchronized VFSManager getInstance() {
    if (instance == null) instance = new VFSManager();
    return instance;
  }

  public void buildCache(String rootPath) {
    if (rootPath == null || rootPath.equals(projectRoot)) return;
    projectRoot = rootPath;
    executor.execute(
        () -> {
          directoryCache.clear();
          File root = new File(rootPath);
          if (root.exists() && root.isDirectory()) indexRecursively(root);
        });
  }

  private void indexRecursively(File dir) {
    File[] children = dir.listFiles();
    if (children == null) return;
    List<File> list = new ArrayList<>();
    for (File child : children) {
      String name = child.getName();
      if (name.startsWith(".") || name.equals("session.json") || name.equals("project_meta.json"))
        continue;
      list.add(child);
      if (child.isDirectory()) indexRecursively(child);
    }
    directoryCache.put(dir.getAbsolutePath(), list);
  }

  public List<File> listFiles(File directory) {
    if (directory == null) return null;
    String key = directory.getAbsolutePath();
    if (directoryCache.containsKey(key)) return directoryCache.get(key);
    File[] diskFiles = directory.listFiles();
    if (diskFiles == null) return null;
    List<File> list = new ArrayList<>();
    for (File f : diskFiles) {
      String name = f.getName();
      if (!name.startsWith(".")) list.add(f);
    }
    directoryCache.put(key, list);
    return list;
  }

  public void invalidate(File directory) {
    if (directory != null) directoryCache.remove(directory.getAbsolutePath());
  }
}
