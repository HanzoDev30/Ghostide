package ir.hanzodev1375.components.searchdata.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Filters paths using gitignore-style glob patterns. */
public class GlobExcluder {

  public static final List<String> DEFAULT_EXCLUDED_FILES =
      Arrays.asList(
          "**/node_modules/**",
          "**/bower_components/**",
          "**/jspm_packages/**",
          "**/.npm/**",
          "**/flow-typed/**",
          "**/vendor/**",
          "**/composer/**",
          "**/venv/**",
          "**/.virtualenv/**",
          "**/__pycache__/**",
          "**/.pytest_cache/**",
          "**/.eggs/**",
          "**/*.egg-info/**",
          "**/.git/**",
          "**/.svn/**",
          "**/.hg/**",
          "**/.vscode/**",
          "**/.idea/**",
          "**/.vs/**",
          "**/.project/**",
          "**/.settings/**",
          "**/.classpath/**",
          "**/dist/**",
          "**/build/**",
          "**/out/**",
          "**/target/**",
          "**/bin/**",
          "**/obj/**",
          "**/coverage/**",
          "**/.nyc_output/**",
          "**/htmlcov/**",
          "**/temp/**",
          "**/tmp/**",
          "**/.cache/**",
          "**/logs/**",
          "**/.sass-cache/**",
          "**/.DS_Store/**",
          "**/Thumbs.db/**");

  private final List<String> globs = new ArrayList<>();

  public GlobExcluder(String globsText) {
    if (globsText == null) return;
    for (String line : globsText.split("\n")) {
      String glob = line.trim();
      if (glob.isEmpty()) continue;
      glob = glob.replaceFirst("^/+", "").replaceFirst("/+$", "");
      globs.add(glob);
    }
  }

  public boolean isExcluded(String absolutePath) {
    if (absolutePath == null) return false;
    String[] segments = absolutePath.replaceFirst("^/+", "").split("/");
    for (String glob : globs) {
      if (matches(glob, segments)) return true;
    }
    return false;
  }

  private static boolean matches(String glob, String[] segments) {
    String[] pattern = glob.split("/");
    if (pattern.length == 1) {
      // Plain pattern (no slash): match any segment, like gitignore.
      for (String segment : segments) {
        if (matchSegment(pattern[0], segment)) return true;
      }
      return false;
    }
    return matchSegments(pattern, segments, 0, 0);
  }

  private static boolean matchSegments(String[] pattern, String[] segments, int pi, int si) {
    if (pi >= pattern.length) return si >= segments.length;
    String token = pattern[pi];
    if (token.equals("**")) {
      for (int k = si; k <= segments.length; k++) {
        if (matchSegments(pattern, segments, pi + 1, k)) return true;
      }
      return false;
    }
    if (si >= segments.length) return false;
    if (!matchSegment(token, segments[si])) return false;
    return matchSegments(pattern, segments, pi + 1, si + 1);
  }

  private static boolean matchSegment(String glob, String text) {
    int gi = 0;
    int ti = 0;
    while (gi < glob.length()) {
      char c = glob.charAt(gi);
      if (c == '*') {
        while (gi < glob.length() && glob.charAt(gi) == '*') gi++;
        if (gi >= glob.length()) return true;
        for (int k = ti; k <= text.length(); k++) {
          if (matchSegment(glob.substring(gi), text.substring(k))) return true;
        }
        return false;
      } else if (c == '?') {
        if (ti >= text.length()) return false;
        gi++;
        ti++;
      } else {
        if (ti >= text.length() || c != text.charAt(ti)) return false;
        gi++;
        ti++;
      }
    }
    return ti == text.length();
  }
}