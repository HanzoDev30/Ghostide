package ir.hanzodev1375.ghostide.codeeditors.colorrender;

import java.util.*;
import java.util.regex.*;

public final class ColorInlayUtils {

  private static final String COLOR_PATTERN =
      "(#[0-9a-fA-F]{3,8}|0x[0-9a-fA-F]{6,8})|"
          + "rgb\\((\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*\\)|"
          + "rgba\\((\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*([\\d.]+)\\s*\\)";

  private ColorInlayUtils() {}

  public static List<ColorTriad> extract(String text) {
    List<ColorTriad> list = new ArrayList<>();
    Pattern pattern = Pattern.compile(COLOR_PATTERN, Pattern.CASE_INSENSITIVE);
    String[] lines = text.split("\n");

    for (int line = 0; line < lines.length; line++) {
      Matcher matcher = pattern.matcher(lines[line]);
      while (matcher.find()) {
        String matched = matcher.group();
        String normalized = normalize(matched);
        if (normalized != null) {
          list.add(new ColorTriad(line, matcher.start(), normalized));
        }
      }
    }
    return list;
  }

  public static String normalize(String raw) {
    if (raw == null) return null;
    raw = raw.trim();

    try {
      if (raw.startsWith("#") || raw.startsWith("0x")) {
        String hex = raw;
        if (hex.startsWith("0x")) {
          hex = hex.substring(2);
          if (hex.length() == 8) {
            return "#" + hex;
          } else if (hex.length() == 6) {
            return "#" + hex;
          }
        }
        if (hex.length() == 4) {
          char[] c = hex.substring(1).toCharArray();
          return "#" + c[0] + c[0] + c[1] + c[1] + c[2] + c[2];
        }
        if (hex.length() == 7 || hex.length() == 9) {
          return hex;
        }
        return null;
      }
      Matcher rgbMatcher =
          Pattern.compile(
                  "rgb\\((\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*\\)",
                  Pattern.CASE_INSENSITIVE)
              .matcher(raw);
      if (rgbMatcher.matches()) {
        int r = clamp(Integer.parseInt(rgbMatcher.group(1)), 0, 255);
        int g = clamp(Integer.parseInt(rgbMatcher.group(2)), 0, 255);
        int b = clamp(Integer.parseInt(rgbMatcher.group(3)), 0, 255);
        return String.format("#%02X%02X%02X", r, g, b);
      }
      Matcher rgbaMatcher =
          Pattern.compile(
                  "rgba\\((\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*([\\d.]+)\\s*\\)",
                  Pattern.CASE_INSENSITIVE)
              .matcher(raw);
      if (rgbaMatcher.matches()) {
        int r = clamp(Integer.parseInt(rgbaMatcher.group(1)), 0, 255);
        int g = clamp(Integer.parseInt(rgbaMatcher.group(2)), 0, 255);
        int b = clamp(Integer.parseInt(rgbaMatcher.group(3)), 0, 255);
        float aFloat = Float.parseFloat(rgbaMatcher.group(4));
        int a = clamp(Math.round(aFloat * 255), 0, 255);
        return String.format("#%02X%02X%02X%02X", r, g, b, a);
      }
    } catch (Exception e) {
      return null;
    }
    return null;
  }

  private static int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }

  public static boolean isValid(String color) {
    return color != null && color.startsWith("#") && (color.length() == 7 || color.length() == 9);
  }

  public static class ColorTriad {
    public final int line;
    public final int column;
    public final String color; 

    public ColorTriad(int l, int c, String col) {
      line = l;
      column = c;
      color = col;
    }
  }
}
