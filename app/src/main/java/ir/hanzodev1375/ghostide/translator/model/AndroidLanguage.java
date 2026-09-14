package ir.hanzodev1375.ghostide.translator.model;

public class AndroidLanguage {
  public final String displayName;
  public final String googleCode;
  public final String androidFolder;

  public AndroidLanguage(String displayName, String googleCode, String androidFolder) {
    this.displayName = displayName;
    this.googleCode = googleCode;
    this.androidFolder = androidFolder;
  }

  public static final AndroidLanguage[] ALL = {
    new AndroidLanguage("العربية", "ar", "values-ar"),
    new AndroidLanguage("فارسی", "fa", "values-fa"),
    new AndroidLanguage("हिन्दी", "hi", "values-hi"),
    new AndroidLanguage("Português", "pt", "values-pt"),
    new AndroidLanguage("Русский", "ru", "values-ru"),
    new AndroidLanguage("简体中文", "zh-CN", "values-zh"),
    new AndroidLanguage("繁體中文", "zh-TW", "values-zh-rTW"),
    new AndroidLanguage("Deutsch", "de", "values-de"),
    new AndroidLanguage("Français", "fr", "values-fr"),
    new AndroidLanguage("Español", "es", "values-es"),
    new AndroidLanguage("Italiano", "it", "values-it"),
    new AndroidLanguage("日本語", "ja", "values-ja"),
    new AndroidLanguage("한국어", "ko", "values-ko"),
    new AndroidLanguage("Türkçe", "tr", "values-tr"),
    new AndroidLanguage("Nederlands", "nl", "values-nl"),
    new AndroidLanguage("Polski", "pl", "values-pl"),
    new AndroidLanguage("Українська", "uk", "values-uk"),
    new AndroidLanguage("Svenska", "sv", "values-sv"),
    new AndroidLanguage("עברית", "he", "values-he"),
    new AndroidLanguage("বাংলা", "bn", "values-bn"),
    new AndroidLanguage("اردو", "ur", "values-ur"),
  };
}
