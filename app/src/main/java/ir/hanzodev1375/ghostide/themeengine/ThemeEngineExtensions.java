package ir.hanzodev1375.ghostide.themeengine;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import androidx.annotation.BoolRes;
import androidx.annotation.IntegerRes;

public final class ThemeEngineExtensions {

  private ThemeEngineExtensions() {}

  public static boolean isDarkMode(Context context) {
    int nightMode =
        context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
    return nightMode == Configuration.UI_MODE_NIGHT_YES;
  }

  public static boolean hasS() {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
  }

  public static boolean getBooleanSafe(Context context, @BoolRes int res, boolean defaultValue) {
    try {
      return context.getResources().getBoolean(res);
    } catch (Resources.NotFoundException e) {
      return defaultValue;
    }
  }

  public static int getIntSafe(Context context, @IntegerRes int res, int defaultValue) {
    try {
      return context.getResources().getInteger(res);
    } catch (Resources.NotFoundException e) {
      return defaultValue;
    }
  }
}
