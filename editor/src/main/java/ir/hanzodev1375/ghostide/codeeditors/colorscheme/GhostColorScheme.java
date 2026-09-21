package ir.hanzodev1375.ghostide.codeeditors.colorscheme;

import android.graphics.Color;
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme;
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry;
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel;
import ir.theme.EditorTheme;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * اسکیم اصلی ادیتور که از TextMate پشتیبانی می کند.
 *
 * <p>رنگ token ها از تم شخصی می آید؛ id های سفارشی زیر ۲۵۵ هستند پس با id توکن های textmate
 * (برابر ۲۵۵ یا بیشتر) تداخلی ندارند. پس زمینه always transparent و حالت تاریک always true است.
 */
public class GhostColorScheme extends TextMateColorScheme {

  private static final int MAX_CACHE = 6;
  private static final LinkedHashMap<Integer, GhostColorScheme> CACHE =
      new LinkedHashMap<Integer, GhostColorScheme>(MAX_CACHE, 0.75f, true);

  private static int endColor = END_COLOR_ID;
  public static final int COLORNEXTDOT = ++endColor;
  public static final int COLORNEXTBRAK = ++endColor;
  public static final int COLORNEXTCHAR = ++endColor;
  public static final int COLORUPPERCASE = ++endColor;
  public static final int COLORNEXTLESS = ++endColor;
  public static final int BRACKET1 = ++endColor;
  public static final int BRACKET2 = ++endColor;
  public static final int BRACKET3 = ++endColor;
  public static final int BRACKET4 = ++endColor;
  public static final int BRACKET5 = ++endColor;
  public static final int BRACKET6 = ++endColor;
  public static final int DEPENDENCY_UPDATE_AVAILABLE = ++endColor;
  public static final int DEPENDENCY_UPDATE_AVAILABLE_BG = ++endColor;

  private final ThemeModel themeModel;

  public GhostColorScheme(ThemeModel themeModel) {
    super(ThemeRegistry.getInstance(), themeModel);
    this.themeModel = themeModel;
  }

  /** مدل تم این اسکیم (همان که در ThemeRegistry ثبت شده). */
  public ThemeModel getThemeModel() {
    return themeModel;
  }

  /**
   * تِم سراسری TextMate را با تِم فعال اپ همگام می کند. چون ThemeRegistry یک سینگلتون است و
   * آخرین setTheme برنده می شود، بدون این کار اسکییم شیت های پیش‌نمایش تِم کل ادیتورها را عوض
   * می کند.
   *
   * @return اگر تِم سراسری تغییر کرده باشد true
   */
  public static synchronized boolean syncRegistryTo(EditorTheme theme) {
    ThemeRegistry registry = ThemeRegistry.getInstance();
    GhostColorScheme scheme = create(theme, true);
    if (registry.getCurrentThemeModel() != scheme.getThemeModel()) {
      registry.setTheme(scheme.getThemeModel());
      return true;
    }
    return false;
  }

  /** ساخت یا برگرداندن اسکیم برای این تم شخصی به صورت یک جا (cache شده تا listener ها زیاد نشوند). */
  public static synchronized GhostColorScheme create(EditorTheme theme, boolean dark) {
    byte[] json = GhostTextMateTheme.tokenColorsJson(theme);
    int key = dark ? 1 : 0;
    key = 31 * key + Arrays.hashCode(json);
    GhostColorScheme cached = CACHE.get(key);
    if (cached != null) {
      return cached;
    }
    GhostColorScheme scheme = new GhostColorScheme(GhostTextMateTheme.build(theme, json, dark));
    CACHE.put(key, scheme);
    if (CACHE.size() > MAX_CACHE) {
      Iterator<Integer> iterator = CACHE.keySet().iterator();
      CACHE.remove(iterator.next());
    }
    return scheme;
  }

  @Override
  public void applyDefault() {
    super.applyDefault();
    setColor(COLORNEXTDOT, Color.parseColor("#ff3208"));
    setColor(COLORNEXTBRAK, Color.parseColor("#ff10ba"));
    setColor(COLORNEXTCHAR, Color.parseColor("#6ba108"));
    setColor(COLORUPPERCASE, Color.parseColor("#ff2c11"));
    setColor(COLORNEXTLESS, Color.parseColor("#ffc190"));
    setColor(BRACKET1, Color.parseColor("#FFDD00"));
    setColor(BRACKET2, Color.parseColor("#00D9FF"));
    setColor(BRACKET3, Color.parseColor("#00FF55"));
    setColor(BRACKET4, Color.parseColor("#FF6200"));
    setColor(BRACKET5, Color.parseColor("#FF64F5"));
    setColor(BRACKET6, Color.parseColor("#64FFD0"));
    setColor(DEPENDENCY_UPDATE_AVAILABLE, Color.parseColor("#FFC107"));
    setColor(DEPENDENCY_UPDATE_AVAILABLE_BG, Color.parseColor("#33FFC107"));
    setColor(WHOLE_BACKGROUND, Color.TRANSPARENT);
    setColor(BLOCK_LINE, Color.parseColor("#26FFFFFF"));
    setColor(BLOCK_LINE_CURRENT, Color.parseColor("#33FFFFFF"));
    setColor(SIDE_BLOCK_LINE, Color.parseColor("#66E0E0E0"));
  }

  @Override
  public boolean isDark() {
    return true;
  }
}