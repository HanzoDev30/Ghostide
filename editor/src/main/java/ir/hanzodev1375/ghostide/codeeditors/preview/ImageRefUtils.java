package ir.hanzodev1375.ghostide.codeeditors.preview;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ImageRefUtils {

  private ImageRefUtils() {}

  private static final Pattern IMAGE_PATH_PATTERN =
      Pattern.compile(
          "([a-zA-Z0-9_\\-./\\\\]+)\\.(png|jpg|jpeg|gif|bmp|webp|svg)", Pattern.CASE_INSENSITIVE);

  static Match findImagePathAtPosition(String lineText, int cursorColumn) {
    if (lineText == null || lineText.isEmpty()) return null;

    Matcher matcher = IMAGE_PATH_PATTERN.matcher(lineText);
    while (matcher.find()) {
      int start = matcher.start();
      int end = matcher.end();

      if (cursorColumn >= start && cursorColumn <= end) {
        String cleaned = clean(lineText.substring(start, end));
        if (isValidImagePath(cleaned)) {
          return new Match(cleaned, start, end);
        }
      }
    }
    return null;
  }

  private static String clean(String raw) {
    String cleaned = raw.trim();
    if ((cleaned.startsWith("\"") && cleaned.endsWith("\""))
        || (cleaned.startsWith("'") && cleaned.endsWith("'"))
        || (cleaned.startsWith("(") && cleaned.endsWith(")"))) {
      cleaned = cleaned.substring(1, cleaned.length() - 1);
    }
    while (cleaned.endsWith(",") || cleaned.endsWith(";") || cleaned.endsWith(")")) {
      cleaned = cleaned.substring(0, cleaned.length() - 1);
    }
    return cleaned.trim();
  }

  static boolean isValidImagePath(String path) {
    if (path == null || path.isEmpty()) return false;
    String p = path.toLowerCase();
    return p.endsWith(".png")
        || p.endsWith(".jpg")
        || p.endsWith(".jpeg")
        || p.endsWith(".gif")
        || p.endsWith(".bmp")
        || p.endsWith(".webp")
        || p.endsWith(".svg");
  }

  static boolean isRemoteUrl(String path) {
    if (path == null) return false;
    String p = path.trim().toLowerCase();
    return p.startsWith("http://")
        || p.startsWith("https://")
        || p.startsWith("data:")
        || p.startsWith("//");
  }

  static File resolve(String currentFilePath, String srcPath) {
    if (srcPath == null || srcPath.isEmpty()) return null;
    if (isRemoteUrl(srcPath)) return null;

    if (srcPath.startsWith("/")) {
      return new File(srcPath);
    }

    if (currentFilePath == null || currentFilePath.isEmpty()) {
      return new File(srcPath);
    }

    File parentDir = new File(currentFilePath).getParentFile();
    if (parentDir == null) {
      return new File(srcPath);
    }

    String cleaned = srcPath;
    int qIdx = cleaned.indexOf('?');
    if (qIdx != -1) cleaned = cleaned.substring(0, qIdx);
    int hIdx = cleaned.indexOf('#');
    if (hIdx != -1) cleaned = cleaned.substring(0, hIdx);

    try {
      return new File(parentDir, cleaned).getCanonicalFile();
    } catch (Exception e) {
      return new File(parentDir, cleaned);
    }
  }
}
