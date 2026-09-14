package ir.hanzodev1375.ghostide.codeeditors.ui;

import android.graphics.Color;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import ir.hanzodev1375.ghostide.codeeditors.colorrender.model.colorrepo.ColorNameRepository;

/**
 * فقط یه utility class برای parse کردن رنگ از string. آیکون واقعی رو
 * SimpleCompletionIconDrawer.drawColorSpan() می‌کشه تا با بقیه kind ها هم‌اندازه بمونه.
 */
public class ColorSwatchDrawable {

  private ColorSwatchDrawable() {}

  /**
   * رنگ رو از string parse می‌کنه — همون منطق WebColorIde.
   *
   * @return رنگ ARGB یا null اگه parse نشه
   */
  @Nullable
  @ColorInt
  public static Integer parseColor(@NonNull String label, @Nullable ColorNameRepository colorRepo) {

    String s = label.trim();

    // نام CSS (مثل "tomato")
    if (colorRepo != null) {
      String hex = colorRepo.getHexForName(s.toLowerCase());
      if (hex != null) {
        try {
          return Color.parseColor(hex);
        } catch (Exception ignored) {
        }
      }
    }

    // HEX: #RGB → expand، #RRGGBB، #AARRGGBB
    if (s.startsWith("#")) {
      try {
        if (s.length() == 4) {
          s =
              "#"
                  + s.charAt(1)
                  + s.charAt(1)
                  + s.charAt(2)
                  + s.charAt(2)
                  + s.charAt(3)
                  + s.charAt(3);
        }
        return Color.parseColor(s);
      } catch (Exception ignored) {
      }
    }

    // rgb(r, g, b)
    if (s.startsWith("rgb(") && s.endsWith(")")) {
      try {
        String[] p = s.substring(4, s.length() - 1).split(",");
        int r = clamp(Integer.parseInt(p[0].trim()), 0, 255);
        int g = clamp(Integer.parseInt(p[1].trim()), 0, 255);
        int b = clamp(Integer.parseInt(p[2].trim()), 0, 255);
        return Color.rgb(r, g, b);
      } catch (Exception ignored) {
      }
    }

    // rgba(r, g, b, a)
    if (s.startsWith("rgba(") && s.endsWith(")")) {
      try {
        String[] p = s.substring(5, s.length() - 1).split(",");
        int r = clamp(Integer.parseInt(p[0].trim()), 0, 255);
        int g = clamp(Integer.parseInt(p[1].trim()), 0, 255);
        int b = clamp(Integer.parseInt(p[2].trim()), 0, 255);
        int a = clamp(Math.round(Float.parseFloat(p[3].trim()) * 255f), 0, 255);
        return Color.argb(a, r, g, b);
      } catch (Exception ignored) {
      }
    }

    return null;
  }

  private static int clamp(int v, int min, int max) {
    return Math.max(min, Math.min(max, v));
  }
}
