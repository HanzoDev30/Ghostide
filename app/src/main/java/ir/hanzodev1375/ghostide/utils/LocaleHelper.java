package ir.hanzodev1375.ghostide.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.LocaleList;
import java.util.Locale;

public class LocaleHelper {

  private static final String PREF_NAME = "locale_pref";
  private static final String PREF_KEY = "selected_language";

  /** کدهای زبان — باید با پوشه‌های values-XX در res مطابقت داشته باشد */
  public static final String[] LANGUAGE_CODES = {
    "default", "en", "fa", "ar", "de", "fr", "es", "zh", "ru", "tr"
  };

  /** نام نمایشی هر زبان (در دیالوگ نشون داده میشه) */
  public static final String[] LANGUAGE_NAMES = {
    "System Default",
    "English",
    "فارسی",
    "العربية",
    "Deutsch",
    "Français",
    "Español",
    "中文",
    "Русский"
  };

  /**
   * این متد رو در attachBaseContext هر Activity صدا بزن:
   * super.attachBaseContext(LocaleHelper.applyLocale(newBase));
   */
  public static Context applyLocale(Context context) {
    return setLocale(context, getSavedLanguage(context));
  }

  public static Context setLocale(Context context, String langCode) {
    saveLanguage(context, langCode);
    if ("default".equals(langCode)) return context;

    Locale locale = new Locale(langCode);
    Locale.setDefault(locale);
    Configuration config = new Configuration(context.getResources().getConfiguration());
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      config.setLocales(new LocaleList(locale));
    } else {
      config.setLocale(locale);
    }
    return context.createConfigurationContext(config);
  }

  public static void saveLanguage(Context context, String langCode) {
    context
        .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString(PREF_KEY, langCode)
        .apply();
  }

  public static String getSavedLanguage(Context context) {
    return context
        .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .getString(PREF_KEY, "default");
  }

  public static int getSavedLanguageIndex(Context context) {
    String saved = getSavedLanguage(context);
    for (int i = 0; i < LANGUAGE_CODES.length; i++) {
      if (LANGUAGE_CODES[i].equals(saved)) return i;
    }
    return 0;
  }
}
