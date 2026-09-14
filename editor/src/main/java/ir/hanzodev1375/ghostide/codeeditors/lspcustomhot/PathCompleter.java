package ir.hanzodev1375.ghostide.codeeditors.lspcustomhot;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.github.rosemoe.sora.lang.completion.CompletionItemKind;

public class PathCompleter {
  private static Map<String, File[]> cache = new HashMap<>();

  public static List<CustomCompletionItem> getPathCompletions(
      String currentFilePath, String userPrefix) {
    List<CustomCompletionItem> completions = new ArrayList<>();
    if (currentFilePath == null || currentFilePath.isEmpty()) return completions;
    if (userPrefix == null) userPrefix = "";

    try {
      File currentFile = new File(currentFilePath);
      File parentDir = currentFile.getParentFile();
      if (parentDir == null) return completions;

      File targetDir = resolveDirectory(parentDir.getAbsolutePath(), userPrefix);
      if (targetDir == null || !targetDir.exists() || !targetDir.isDirectory()) return completions;

      cache.remove(targetDir.getAbsolutePath());
      File[] allFiles;
      List<File> cached = VFSManager.getInstance().listFiles(targetDir);
      if (cached != null) {
        allFiles = cached.toArray(new File[0]);
      } else {
        allFiles = Optional.ofNullable(targetDir.listFiles()).orElse(new File[0]);
      }

      String finalPrefix = extractFinalPrefix(userPrefix);
      String basePath = buildBasePath(userPrefix);

      for (File file : allFiles) {
        if (shouldInclude(file, finalPrefix, userPrefix)) {
          String commitText = basePath + file.getName() + (file.isDirectory() ? "/" : "");
          String label = file.getName();
          String desc = file.isDirectory() ? "Folder" : "File";
          int cursorOffset = commitText.length();

          CustomCompletionItem item =
              new CustomCompletionItem(label, desc, commitText, cursorOffset, userPrefix);
          item.kind(file.isDirectory() ? CompletionItemKind.Folder : CompletionItemKind.File);
          completions.add(item);
        }
      }

      if (!finalPrefix.isEmpty()) {
        completions.removeIf(
            it -> !it.label.toString().toLowerCase().startsWith(finalPrefix.toLowerCase()));
      }

      completions.sort(
          (a, b) -> {
            boolean aDir = a.desc.toString().equals("Folder");
            boolean bDir = b.desc.toString().equals("Folder");
            if (aDir && !bDir) return -1;
            if (!aDir && bDir) return 1;
            return a.label.toString().compareToIgnoreCase(b.label.toString());
          });

    } catch (Exception e) {
      e.printStackTrace();
    }
    return completions;
  }

  private static boolean shouldInclude(File file, String finalPrefix, String fullPrefix) {
    if (fullPrefix.endsWith("/")) return true;
    return file.getName().toLowerCase().startsWith(finalPrefix.toLowerCase());
  }

  private static String buildBasePath(String prefix) {
    if (prefix.isEmpty()) return "";
    if (prefix.endsWith("/")) return prefix;
    int lastSlash = prefix.lastIndexOf('/');
    if (lastSlash != -1) {
      return prefix.substring(0, lastSlash + 1);
    }
    return "";
  }

  private static File resolveDirectory(String currentDirPath, String prefix) {
    if (prefix.startsWith("./")) {
      String subPath = prefix.substring(2);
      if (subPath.isEmpty()) {
        return new File(currentDirPath);
      } else {
        return new File(currentDirPath + File.separator + subPath);
      }
    } else if (prefix.startsWith("../")) {
      int upCount = 0;
      String temp = prefix;
      while (temp.startsWith("../")) {
        upCount++;
        temp = temp.substring(3);
      }
      String path = currentDirPath;
      for (int i = 0; i < upCount; i++) {
        File parent = new File(path).getParentFile();
        if (parent == null) break;
        path = parent.getAbsolutePath();
      }
      if (!temp.isEmpty()) {
        path = path + File.separator + temp;
      }
      return new File(path);
    } else if (prefix.contains("/")) {
      String dirPart = prefix.substring(0, prefix.lastIndexOf('/'));
      return new File(currentDirPath + File.separator + dirPart);
    }
    return new File(currentDirPath);
  }

  private static String extractFinalPrefix(String prefix) {
    int lastSlash = prefix.lastIndexOf('/');
    if (lastSlash != -1) {
      return prefix.substring(lastSlash + 1);
    }
    return prefix;
  }
}
