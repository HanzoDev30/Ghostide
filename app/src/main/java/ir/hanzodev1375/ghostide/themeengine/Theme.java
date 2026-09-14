package ir.hanzodev1375.ghostide.themeengine;

import ir.hanzodev1375.ghostide.R;

public enum Theme {
  MAINTHEME(R.style.AppTheme, R.color.md_theme_primary),
  RED(R.style.AppTheme_RED, R.color.md_theme_red_primary),
  GREEN(R.style.AppTheme_GREEN, R.color.md_theme_green_primary),
  LEMON(R.style.AppTheme_Lemon, R.color.md_theme_lemon_primary),
  LAVENDERPINK(R.style.AppTheme_LavenderPink, R.color.theme_themeslavenderpink_primary);
  private final int themeId;
  private final int primaryColor;

  Theme(int themeId, int primaryColor) {
    this.themeId = themeId;
    this.primaryColor = primaryColor;
  }

  public int getThemeId() {
    return themeId;
  }

  public int getPrimaryColor() {
    return primaryColor;
  }
}
