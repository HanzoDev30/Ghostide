package ir.hanzodev1375.ghostide.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class FileExtensionUtils {

  private FileExtensionUtils() {}

  private static final Set<String> CODE_EXTENSIONS;
  private static final Set<String> IMAGE_EXTENSIONS;
  private static final Set<String> AUDIO_EXTENSIONS;

  static {
    Map<String, Boolean> code = new HashMap<>();
    String[] langs = {
      ".html", ".java", ".c", ".cs", ".cpp", ".cxx", ".hpp", ".hxx", ".cc", ".h",
      ".css", ".js", ".py", ".json", ".xml", ".kt", ".kts", ".ts", ".tsx", ".toml",
      ".groovy", ".gradle", ".sass", ".scss", ".md", ".markdown", ".yml", ".yaml",
      ".lua", ".go", ".php", ".dart", ".jsx", ".sql", ".sh", ".rc", ".bash",
      ".bashrc", ".ash", ".zsh", ".zshrc", ".rs", ".rb", ".g4", ".ini", ".zig",
      ".vue", ".asm", ".s", ".nasm", ".swift", ".scala", ".sc", ".pl", ".pm",
      ".jl", ".r", ".ex", ".exs", ".hs", ".nim", ".sol", ".ninja"
    };
    for (String ext : langs) code.put(ext, Boolean.TRUE);
    CODE_EXTENSIONS = Collections.unmodifiableSet(code.keySet());

    IMAGE_EXTENSIONS = Set.of(
      ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".avif", ".webp", ".svg"
    );

    AUDIO_EXTENSIONS = Set.of(
      ".mp3", ".wav", ".m4a", ".ogg", ".flac", ".aac", ".opus", ".wma"
    );
  }

  public static boolean isCodeFile(String extension) {
    return extension != null && CODE_EXTENSIONS.contains(extension.toLowerCase());
  }

  public static boolean isImageFile(String extension) {
    return extension != null && IMAGE_EXTENSIONS.contains(extension.toLowerCase());
  }

  public static boolean isAudioFile(String extension) {
    return extension != null && AUDIO_EXTENSIONS.contains(extension.toLowerCase());
  }

  public static String getExtension(String fileName) {
    if (fileName == null) return "";
    int lastDot = fileName.lastIndexOf(".");
    return (lastDot > 0) ? fileName.substring(lastDot).toLowerCase() : "";
  }

  public static Set<String> getCodeExtensions() {
    return CODE_EXTENSIONS;
  }

  public static Set<String> getImageExtensions() {
    return IMAGE_EXTENSIONS;
  }

  public static Set<String> getAudioExtensions() {
    return AUDIO_EXTENSIONS;
  }
}
