package ir.hanzodev1375.components.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.util.StringJoiner;
import ir.hanzodev1375.components.searchdata.utils.GlobExcluder;

public class ComponentsPrefs {

  public static final String KEY_PARALLAX = "app_parallax";
  public static final String KEY_EXCLUDED_FILES_SEARCH = "search_excluded_files";

  private final SharedPreferences prefs;

  public ComponentsPrefs(Context context) {
    prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
  }

  public boolean isGlassMaterialColor() {
    return prefs.getBoolean("m3glassColor", true);
  }

  public float getGlassTint() {
    return prefs.getFloat("m3glassTint", 0.6f);
  }

  public void setGlassTint(float value) {
    prefs.edit().putFloat("m3glassTint", value).apply();
  }

  public boolean isBlurMod() {
    return prefs.getBoolean("app_blurmod_allstate", true);
  }

  public boolean isParallaxEnabled() {
    return prefs.getBoolean(KEY_PARALLAX, true);
  }

  public void setParallaxEnabled(boolean enabled) {
    prefs.edit().putBoolean(KEY_PARALLAX, enabled).apply();
  }

  public boolean isShowBackground() {
    return prefs.getBoolean("filemanager_showbackgroundtheme", false);
  }

  public int getAnimationBatteryThreshold() {
    return prefs.getInt("pref_animation_battery_threshold", 20);
  }

  public String getExcludedFilesText() {
    return prefs.getString(KEY_EXCLUDED_FILES_SEARCH, defaultExcludedFilesText());
  }

  public void setExcludedFilesText(String text) {
    prefs.edit().putString(KEY_EXCLUDED_FILES_SEARCH, text).apply();
  }

  public void resetExcludedFiles() {
    prefs.edit().remove(KEY_EXCLUDED_FILES_SEARCH).apply();
  }

  public static String defaultExcludedFilesText() {
    StringJoiner joiner = new StringJoiner("\n");
    for (String glob : GlobExcluder.DEFAULT_EXCLUDED_FILES) joiner.add(glob);
    return joiner.toString();
  }
}