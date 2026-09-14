package ir.hanzodev1375.ghostide.codeeditors.lspcustomhot;

import java.util.ArrayList;
import java.util.List;

public class KeyWordConst {

  public static class FontCssFamily {
    public String fontname;
    public String fontdoc;

    public FontCssFamily(String fontname, String fontdoc) {
      this.fontname = fontname;
      this.fontdoc = fontdoc;
    }
  }

  private static List<FontCssFamily> fontList = null;

  private static List<FontCssFamily> getFontList() {
    if (fontList != null) return fontList;
    fontList = new ArrayList<>();

    fontList.add(
        new FontCssFamily(
            "sans-serif", "**Generic** | Sans-serif | No serifs | UI & screens | 14-24px"));
    fontList.add(
        new FontCssFamily("serif", "**Generic** | With serifs | Print & long text | 16-28px"));
    fontList.add(
        new FontCssFamily("monospace", "**Generic** | Fixed width | Code & terminal | 13-16px"));
    fontList.add(
        new FontCssFamily("cursive", "**Generic** | Handwriting | Quotes & signatures | 18-32px"));
    fontList.add(
        new FontCssFamily("fantasy", "**Generic** | Decorative | Headings & logos | 24-48px"));
    fontList.add(new FontCssFamily("system-ui", "**Generic** | OS default | Apps & UI | 14-20px"));
    fontList.add(
        new FontCssFamily("ui-sans-serif", "**Generic** | Modern UI | Web apps | 14-20px"));
    fontList.add(new FontCssFamily("ui-serif", "**Generic** | Modern serif | Blogs | 16-24px"));
    fontList.add(new FontCssFamily("ui-monospace", "**Generic** | Modern code | IDEs | 13-15px"));
    fontList.add(
        new FontCssFamily("ui-rounded", "**Generic** | Rounded corners | Friendly UI | 14-20px"));
    fontList.add(
        new FontCssFamily("Arial", "**Sans** | Helvetica | Body text | 400-700 | 14-18px"));
    fontList.add(new FontCssFamily("Arial Black", "**Sans** | Impact | Headlines | 900 | 24-48px"));
    fontList.add(new FontCssFamily("Arial Narrow", "**Sans** | Arial | Condensed | 400 | 12-16px"));
    fontList.add(
        new FontCssFamily("Avant Garde", "**Sans** | Futura | Geometric | 400-700 | 14-24px"));
    fontList.add(
        new FontCssFamily("Book Antiqua", "**Serif** | Palatino | Classic | 400 | 12-18px"));
    fontList.add(
        new FontCssFamily("Bookman", "**Serif** | Georgia | Old style | 400-700 | 12-20px"));
    fontList.add(
        new FontCssFamily(
            "Calibri", "**Sans** | Segoe UI | Microsoft default | 400-700 | 11-16px"));
    fontList.add(
        new FontCssFamily("Cambria", "**Serif** | Georgia | Microsoft serif | 400 | 11-16px"));
    fontList.add(new FontCssFamily("Candara", "**Sans** | Calibri | Humanist | 400 | 11-16px"));
    fontList.add(
        new FontCssFamily("Comic Sans MS", "**Cursive** | Comic Neue | Casual | 400 | 12-20px"));
    fontList.add(
        new FontCssFamily("Courier New", "**Mono** | Courier | Typewriter | 400 | 12-14px"));
    fontList.add(
        new FontCssFamily("Georgia", "**Serif** | Times | Screen readable | 400-700 | 15-22px"));
    fontList.add(
        new FontCssFamily("Impact", "**Sans** | Arial Black | Bold & condensed | 700 | 24-64px"));
    fontList.add(new FontCssFamily("Lucida Console", "**Mono** | Consolas | Code | 400 | 12-14px"));
    fontList.add(
        new FontCssFamily("Lucida Grande", "**Sans** | Arial | Mac default | 400-700 | 12-18px"));
    fontList.add(
        new FontCssFamily(
            "Lucida Sans Unicode", "**Sans** | Arial | Unicode support | 400 | 12-16px"));
    fontList.add(
        new FontCssFamily(
            "Microsoft Sans Serif", "**Sans** | Arial | Windows fallback | 400 | 12-16px"));
    fontList.add(
        new FontCssFamily("Palatino Linotype", "**Serif** | Georgia | Print | 400 | 12-18px"));
    fontList.add(
        new FontCssFamily("Segoe UI", "**Sans** | Arial | Windows default | 400-700 | 12-20px"));
    fontList.add(new FontCssFamily("Tahoma", "**Sans** | Geneva | Compact | 400 | 11-14px"));
    fontList.add(
        new FontCssFamily("Times New Roman", "**Serif** | Georgia | Classic | 400-700 | 12-20px"));
    fontList.add(
        new FontCssFamily("Trebuchet MS", "**Sans** | Arial | Friendly | 400-700 | 12-18px"));
    fontList.add(
        new FontCssFamily("Verdana", "**Sans** | Tahoma | Wide spacing | 400-700 | 12-16px"));
    fontList.add(new FontCssFamily("Webdings", "**Symbol** | Wingdings | Icons | 400 | 12-24px"));
    fontList.add(new FontCssFamily("Wingdings", "**Symbol** | Webdings | Icons | 400 | 12-24px"));
    fontList.add(
        new FontCssFamily("Helvetica", "**Sans** | Arial | Professional | 300-700 | 12-24px"));
    fontList.add(new FontCssFamily("Optima", "**Sans** | Arial | Elegant | 400 | 14-22px"));
    fontList.add(
        new FontCssFamily("Baskerville", "**Serif** | Georgia | Old classic | 400-700 | 14-22px"));
    fontList.add(
        new FontCssFamily("Bodoni MT", "**Serif** | Didot | High contrast | 400-700 | 18-36px"));
    fontList.add(
        new FontCssFamily("Calisto MT", "**Serif** | Georgia | Book style | 400 | 12-18px"));
    fontList.add(new FontCssFamily("Century", "**Serif** | Times | School books | 400 | 12-18px"));
    fontList.add(
        new FontCssFamily("Century Gothic", "**Sans** | Arial | Rounded | 400-700 | 14-24px"));
    fontList.add(new FontCssFamily("Garamond", "**Serif** | Times | Elegant | 400 | 11-18px"));
    fontList.add(
        new FontCssFamily("Papyrus", "**Fantasy** | Comic Sans | Ancient look | 400 | 16-28px"));
    fontList.add(
        new FontCssFamily("Rockwell", "**Serif** | Georgia | Slab serif | 400-700 | 14-24px"));
    fontList.add(
        new FontCssFamily("Algerian", "**Fantasy** | Rockwell | Decorative | 400 | 24-48px"));
    fontList.add(new FontCssFamily("Perpetua", "**Serif** | Times | Elegant | 400 | 12-18px"));
    fontList.add(
        new FontCssFamily("Symbol", "**Symbol** | Webdings | Math & symbols | 400 | 12-20px"));
    fontList.add(new FontCssFamily("Futura", "**Sans** | Arial | Geometric | 400-700 | 14-28px"));
    fontList.add(
        new FontCssFamily("Gill Sans", "**Sans** | Arial | British classic | 400-700 | 12-20px"));
    fontList.add(
        new FontCssFamily("Josefin Sans", "**Sans** | Arial | Geometric | 300-700 | 16-32px"));
    fontList.add(
        new FontCssFamily("Lobster", "**Cursive** | Pacifico | Bold script | 400 | 24-48px"));
    fontList.add(
        new FontCssFamily(
            "Merriweather", "**Serif** | Georgia | Screen readable | 300-900 | 15-24px"));
    fontList.add(
        new FontCssFamily("Montserrat", "**Sans** | Arial | Geometric | 400-800 | 18-48px"));
    fontList.add(
        new FontCssFamily("Noto Sans", "**Sans** | Arial | Google universal | 400-700 | 14-24px"));
    fontList.add(new FontCssFamily("Raleway", "**Sans** | Arial | Elegant | 100-900 | 14-42px"));
    fontList.add(
        new FontCssFamily("Roboto", "**Sans** | Arial | Android default | 300-700 | 14-24px"));
    fontList.add(new FontCssFamily("Open Sans", "**Sans** | Arial | Friendly | 300-800 | 14-20px"));
    fontList.add(new FontCssFamily("Lato", "**Sans** | Arial | Warm | 300-900 | 14-22px"));
    fontList.add(new FontCssFamily("Poppins", "**Sans** | Arial | Rounded | 300-700 | 14-32px"));
    fontList.add(
        new FontCssFamily("Ubuntu", "**Sans** | Arial | Linux default | 300-700 | 12-20px"));
    fontList.add(new FontCssFamily("Inter", "**Sans** | Arial | Modern web | 300-900 | 14-24px"));
    fontList.add(new FontCssFamily("Nunito", "**Sans** | Arial | Rounded | 300-900 | 14-28px"));
    fontList.add(new FontCssFamily("Quicksand", "**Sans** | Arial | Soft | 300-700 | 16-32px"));
    fontList.add(
        new FontCssFamily("Playfair Display", "**Serif** | Georgia | Elegant | 400-900 | 20-56px"));
    fontList.add(new FontCssFamily("Oswald", "**Sans** | Arial | Condensed | 400-700 | 20-48px"));
    fontList.add(
        new FontCssFamily("Source Sans Pro", "**Sans** | Arial | Adobe | 200-900 | 14-22px"));
    fontList.add(
        new FontCssFamily(
            "Dancing Script", "**Cursive** | Pacifico | Flowing | 400-700 | 20-48px"));
    fontList.add(
        new FontCssFamily("Pacifico", "**Cursive** | Brush Script | Handwritten | 400 | 24-48px"));
    fontList.add(
        new FontCssFamily("Consolas", "**Mono** | Courier New | Clean code | 400 | 12-14px"));
    fontList.add(
        new FontCssFamily("Fira Code", "**Mono** | Consolas | Ligatures | 400-600 | 12-15px"));
    fontList.add(
        new FontCssFamily("Inconsolata", "**Mono** | Consolas | Readable code | 400 | 12-15px"));
    fontList.add(
        new FontCssFamily(
            "Source Code Pro", "**Mono** | Consolas | Adobe code | 400-600 | 12-15px"));
    fontList.add(new FontCssFamily("Menlo", "**Mono** | Consolas | Mac default | 400 | 12-14px"));
    fontList.add(new FontCssFamily("SF Mono", "**Mono** | Menlo | Apple code | 400 | 12-14px"));
    fontList.add(
        new FontCssFamily("JetBrains Mono", "**Mono** | Consolas | Developer | 400-700 | 12-15px"));
    fontList.add(
        new FontCssFamily("Cascadia Code", "**Mono** | Consolas | Microsoft | 400 | 12-15px"));
    fontList.add(
        new FontCssFamily("Droid Sans", "**Sans** | Arial | Android legacy | 400 | 14-18px"));
    fontList.add(
        new FontCssFamily("Droid Serif", "**Serif** | Georgia | Android legacy | 400 | 14-20px"));
    fontList.add(
        new FontCssFamily("Roboto Condensed", "**Sans** | Arial | Narrow | 400-700 | 14-24px"));
    fontList.add(
        new FontCssFamily("Roboto Mono", "**Mono** | Consolas | Code | 400-600 | 12-15px"));
    fontList.add(
        new FontCssFamily("Roboto Slab", "**Serif** | Georgia | Slab serif | 400-700 | 14-24px"));
    fontList.add(new FontCssFamily("Arvo", "**Serif** | Georgia | Slab | 400-700 | 14-24px"));
    fontList.add(new FontCssFamily("Bitter", "**Serif** | Georgia | Screen | 400-700 | 14-22px"));
    fontList.add(new FontCssFamily("Cabin", "**Sans** | Arial | Friendly | 400-700 | 14-22px"));
    fontList.add(
        new FontCssFamily("Crimson Text", "**Serif** | Georgia | Old style | 400-700 | 14-22px"));
    fontList.add(new FontCssFamily("Exo", "**Sans** | Arial | Geometric | 100-900 | 14-28px"));
    fontList.add(new FontCssFamily("Fjalla One", "**Sans** | Impact | Condensed | 400 | 20-48px"));
    fontList.add(new FontCssFamily("Francois One", "**Sans** | Impact | Bold | 400 | 20-48px"));
    fontList.add(new FontCssFamily("Hind", "**Sans** | Arial | Gujarati | 300-700 | 14-22px"));
    fontList.add(new FontCssFamily("Karla", "**Sans** | Arial | Grotesque | 400-700 | 14-22px"));
    fontList.add(new FontCssFamily("Maven Pro", "**Sans** | Arial | Modern | 400-900 | 14-24px"));
    fontList.add(
        new FontCssFamily("Nunito Sans", "**Sans** | Arial | Friendly | 300-900 | 14-24px"));
    fontList.add(new FontCssFamily("Overpass", "**Sans** | Arial | Modern | 300-900 | 14-24px"));
    fontList.add(
        new FontCssFamily("Pathway Gothic One", "**Sans** | Impact | Condensed | 400 | 18-36px"));
    fontList.add(
        new FontCssFamily("Red Hat Display", "**Sans** | Arial | Modern | 400-900 | 16-32px"));
    fontList.add(new FontCssFamily("Righteous", "**Sans** | Impact | Bold | 400 | 20-48px"));
    fontList.add(new FontCssFamily("Saira", "**Sans** | Arial | Condensed | 100-900 | 14-28px"));
    fontList.add(
        new FontCssFamily("Satisfy", "**Cursive** | Pacifico | Handwritten | 400 | 20-40px"));
    fontList.add(
        new FontCssFamily("Spectral", "**Serif** | Georgia | Elegant | 400-800 | 14-24px"));
    fontList.add(new FontCssFamily("Staatliches", "**Sans** | Impact | Display | 400 | 24-56px"));
    fontList.add(
        new FontCssFamily("Titillium Web", "**Sans** | Arial | Modern | 400-700 | 14-22px"));
    fontList.add(new FontCssFamily("Varela Round", "**Sans** | Arial | Rounded | 400 | 14-22px"));
    fontList.add(new FontCssFamily("Yeseva One", "**Serif** | Georgia | Display | 400 | 20-48px"));
    fontList.add(
        new FontCssFamily("Abril Fatface", "**Serif** | Georgia | Bold display | 400 | 28-64px"));
    fontList.add(
        new FontCssFamily(
            "Amatic SC", "**Cursive** | Comic Sans | Handwritten | 400-700 | 18-36px"));
    fontList.add(
        new FontCssFamily("Archivo Black", "**Sans** | Impact | Very bold | 400 | 24-56px"));
    fontList.add(new FontCssFamily("Barlow", "**Sans** | Arial | Modern | 400-700 | 14-24px"));
    fontList.add(new FontCssFamily("Bungee", "**Sans** | Impact | Display | 400 | 20-48px"));
    fontList.add(
        new FontCssFamily("Caveat", "**Cursive** | Pacifico | Handwritten | 400-700 | 18-36px"));
    fontList.add(new FontCssFamily("Chivo", "**Sans** | Arial | Grotesque | 400-900 | 14-24px"));
    fontList.add(
        new FontCssFamily("Cinzel", "**Serif** | Georgia | Decorative | 400-900 | 18-42px"));
    fontList.add(new FontCssFamily("Coda", "**Sans** | Arial | Headings | 400-800 | 14-28px"));
    fontList.add(new FontCssFamily("Dosis", "**Sans** | Arial | Rounded | 400-800 | 14-32px"));
    fontList.add(
        new FontCssFamily("EB Garamond", "**Serif** | Georgia | Classic | 400-800 | 14-24px"));
    fontList.add(
        new FontCssFamily(
            "Gloria Hallelujah", "**Cursive** | Comic Sans | Handwritten | 400 | 16-28px"));
    fontList.add(
        new FontCssFamily("Grand Hotel", "**Cursive** | Pacifico | Formal script | 400 | 20-48px"));
    fontList.add(new FontCssFamily("Heebo", "**Sans** | Arial | Hebrew | 400-900 | 14-24px"));
    fontList.add(
        new FontCssFamily(
            "Indie Flower", "**Cursive** | Comic Sans | Handwritten | 400 | 16-28px"));
    fontList.add(
        new FontCssFamily("Kalam", "**Cursive** | Comic Sans | Handwritten | 400-700 | 14-24px"));
    fontList.add(new FontCssFamily("Khand", "**Sans** | Impact | Condensed | 400-700 | 16-32px"));
    fontList.add(new FontCssFamily("Laila", "**Sans** | Arial | Devanagari | 400-700 | 14-24px"));
    fontList.add(new FontCssFamily("Limelight", "**Sans** | Impact | Display | 400 | 24-56px"));
    fontList.add(
        new FontCssFamily("Mochiy Pop P One", "**Sans** | Arial | Japanese | 400 | 16-32px"));
    fontList.add(
        new FontCssFamily("Old Standard TT", "**Serif** | Georgia | Classic | 400-700 | 14-24px"));
    fontList.add(
        new FontCssFamily("Oregano", "**Cursive** | Pacifico | Handwritten | 400 | 16-32px"));
    fontList.add(new FontCssFamily("Overlock", "**Sans** | Arial | Rounded | 400-900 | 14-24px"));
    fontList.add(new FontCssFamily("Pattaya", "**Sans** | Arial | Rounded | 400 | 18-36px"));
    fontList.add(
        new FontCssFamily(
            "Reenie Beanie", "**Cursive** | Comic Sans | Handwritten | 400 | 14-28px"));
    fontList.add(new FontCssFamily("Rokkitt", "**Serif** | Georgia | Slab | 400-900 | 14-28px"));
    fontList.add(new FontCssFamily("Salsa", "**Sans** | Arial | Friendly | 400 | 14-24px"));
    fontList.add(
        new FontCssFamily("Sarina", "**Cursive** | Pacifico | Decorative | 400 | 18-36px"));
    fontList.add(new FontCssFamily("Spicy Rice", "**Sans** | Impact | Display | 400 | 20-48px"));
    fontList.add(new FontCssFamily("Suez One", "**Serif** | Georgia | Hebrew | 400 | 18-42px"));
    fontList.add(new FontCssFamily("Trirong", "**Serif** | Georgia | Thai | 400-900 | 14-24px"));
    fontList.add(new FontCssFamily("Varela", "**Sans** | Arial | Friendly | 400 | 14-22px"));
    fontList.add(new FontCssFamily("Volkhov", "**Serif** | Georgia | Book | 400-700 | 14-22px"));
    fontList.add(
        new FontCssFamily("Alegreya", "**Serif** | Georgia | Literature | 400-900 | 14-24px"));
    fontList.add(
        new FontCssFamily("Barlow Condensed", "**Sans** | Arial | Narrow | 400-700 | 14-24px"));
    fontList.add(new FontCssFamily("DM Sans", "**Sans** | Arial | Modern | 400-700 | 14-24px"));
    fontList.add(new FontCssFamily("Figtree", "**Sans** | Arial | Geometric | 400-900 | 14-24px"));
    fontList.add(new FontCssFamily("Fraunces", "**Serif** | Georgia | Soft | 400-900 | 14-32px"));
    fontList.add(
        new FontCssFamily("Instrument Sans", "**Sans** | Arial | Modern | 400-700 | 14-24px"));
    fontList.add(new FontCssFamily("Lexend", "**Sans** | Arial | Readability | 400-800 | 14-24px"));
    fontList.add(new FontCssFamily("Manrope", "**Sans** | Arial | Modern | 400-800 | 14-24px"));
    fontList.add(new FontCssFamily("Outfit", "**Sans** | Arial | Geometric | 400-900 | 14-28px"));
    fontList.add(
        new FontCssFamily("Plus Jakarta Sans", "**Sans** | Arial | Modern | 400-800 | 14-24px"));
    fontList.add(new FontCssFamily("Satoshi", "**Sans** | Arial | Modern | 400-900 | 14-24px"));
    fontList.add(
        new FontCssFamily("Space Grotesk", "**Sans** | Arial | Geometric | 400-700 | 14-24px"));
    fontList.add(
        new FontCssFamily("Zilla Slab", "**Serif** | Georgia | Mozilla | 400-700 | 14-24px"));
    fontList.add(new FontCssFamily("Anton", "**Sans** | Impact | Bold | 400 | 24-64px"));
    fontList.add(new FontCssFamily("Bebas Neue", "**Sans** | Impact | Condensed | 400 | 24-64px"));
    fontList.add(new FontCssFamily("Teko", "**Sans** | Impact | Condensed | 300-700 | 18-48px"));

    return fontList;
  }

  // متد کمکی برای ساخت CompletionItem (می‌تواند از Css3Server هم استفاده کند، ولی برای استقلال
  // اینجا می‌سازیم)
  private CustomCompletionItem css(String label, String description) {
    return new CustomCompletionItem(label, description);
  }

  private CustomCompletionItem css(String label, String detail, String insertText) {
    return new CustomCompletionItem(label, detail, insertText, -1, "");
  }

  // متد جدید که List برمی‌گرداند (جایگزین cssFont)
  public List<CustomCompletionItem> getCssFont(String prefix) {
    List<CustomCompletionItem> list = new ArrayList<>();
    if (prefix == null) return list;

    String propertyName = "";
    if (prefix.startsWith("font-family:")) propertyName = "font-family";

    if (!propertyName.isEmpty()) {
      String fontPrefix = prefix.substring(propertyName.length() + 1).trim();
      for (FontCssFamily font : getFontList()) {
        if (font.fontname.toLowerCase().startsWith(fontPrefix.toLowerCase())) {
          list.add(
              css(font.fontname, font.fontdoc, propertyName + ": \"" + font.fontname + "\" ;"));
        }
      }
    }
    return list;
  }

  // متد setLspCustom که قبلاً بود – اگر لازم نیست می‌توان حذف کرد، ولی برای اتمام کار بازنویسی
  // می‌کنیم
  public List<CustomCompletionItem> getLspCustom(
      String prefix, String propName, String symbol, String[] args, String desc, String algorithm) {
    List<CustomCompletionItem> list = new ArrayList<>();
    if (prefix == null) return list;
    String fullProp = propName + symbol;
    if (prefix.startsWith(fullProp)) {
      String valuePrefix = prefix.substring(fullProp.length()).trim();
      for (String it : args) {
        if (it.startsWith(valuePrefix)) {
          list.add(css(it, desc, algorithm));
        }
      }
    }
    return list;
  }
}
