package ir.hanzodev1375.ghostide.codeeditors.util;

public class TranslateLanguages {

  public static final String[] NAMES = {
    "فارسی",
    "English",
    "العربية",
    "Deutsch",
    "Français",
    "Español",
    "Italiano",
    "Português",
    "Русский",
    "简体中文",
    "繁體中文",
    "日本語",
    "한국어",
    "हिन्दी",
    "Türkçe",
    "Nederlands",
    "Polski",
    "Українська",
    "Svenska",
    "Norsk",
    "Dansk",
    "Suomi",
    "Ελληνικά",
    "Čeština",
    "Magyar",
    "Română",
    "Български",
    "Hrvatski",
    "Српски",
    "Slovenčina",
    "Slovenščina",
    "Bahasa Indonesia",
    "Bahasa Melayu",
    "Tiếng Việt",
    "ไทย",
    "עברית",
    "বাংলা",
    "اردو",
    "தமிழ்",
    "తెలుగు",
    "Latina"
  };

  public static final String[] CODES = {
    "fa",
    "en",
    "ar",
    "de",
    "fr",
    "es",
    "it",
    "pt",
    "ru",
    "zh-CN",
    "zh-TW",
    "ja",
    "ko",
    "hi",
    "tr",
    "nl",
    "pl",
    "uk",
    "sv",
    "no",
    "da",
    "fi",
    "el",
    "cs",
    "hu",
    "ro",
    "bg",
    "hr",
    "sr",
    "sk",
    "sl",
    "id",
    "ms",
    "vi",
    "th",
    "he",
    "bn",
    "ur",
    "ta",
    "te",
    "la"
  };

  public static String getNameByCode(String code) {
    for (int i = 0; i < CODES.length; i++) {
      if (CODES[i].equals(code)) {
        return NAMES[i];
      }
    }
    return code;
  }

  private TranslateLanguages() {}
}