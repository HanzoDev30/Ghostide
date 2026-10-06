package ir.hanzodev1375.ghostide.codeeditors.rainbow;

/**
 * مشخصات لازم برای نادیده گرفتن کامنت و رشته هنگام اسکن براکت ها. برای زبان های داخلی از {@code
 * language-configuration.json} خوانده می شود (نگا: {@link BracketSyntaxLoader}) و پلاگین ها می
 * توانند با {@link RainbowBracketRegistry} سینتکس دقیق خودشان را ثبت کنند.
 */
public final class BracketSyntax {

  /** فقط رشته ی "..." ؛ برای زمانی که هیچ چیز دیگری معلوم نیست. */
  public static final BracketSyntax DEFAULT =
      new BracketSyntax(null, null, null, null, null, true, false, false, false, false);

  /**
   * پیش فرض برای اسکوپ های ناشناس (مثل گرامر پلاگین های بدون language-configuration): کامنت های //
   * و /* *\/ و
   * <!-- -->
   * ، و رشته های " ' ` . برای خانواده ی C/JS/TS/Astro/Vue/Svelte مناسب است.
   */
  public static final BracketSyntax GENERIC =
      new BracketSyntax("//", "/*", "*/", "<!--", "-->", true, true, true, false, true);

  final String lineComment;
  final String blockStart;
  final String blockEnd;

  /** کامنت بلوکی دوم (مثلا {@code <!-- -->} کنار {@code /* *\/}). */
  final String blockStart2;

  final String blockEnd2;
  final boolean doubleQuote;
  final boolean singleQuote;
  final boolean backtick;

  /** رشته های سه کوتاسیونی ("""...""" و '''...''') که چند خطی هستند. */
  final boolean tripleQuote;

  /** بک‌تیک چند خطی (template literal در JS/TS و raw string در Go). */
  final boolean multiLineBacktick;

  public BracketSyntax(
      String lineComment,
      String blockStart,
      String blockEnd,
      boolean doubleQuote,
      boolean singleQuote,
      boolean backtick,
      boolean tripleQuote,
      boolean multiLineBacktick) {
    this(
        lineComment,
        blockStart,
        blockEnd,
        null,
        null,
        doubleQuote,
        singleQuote,
        backtick,
        tripleQuote,
        multiLineBacktick);
  }

  public BracketSyntax(
      String lineComment,
      String blockStart,
      String blockEnd,
      String blockStart2,
      String blockEnd2,
      boolean doubleQuote,
      boolean singleQuote,
      boolean backtick,
      boolean tripleQuote,
      boolean multiLineBacktick) {
    this.lineComment = isBlank(lineComment) ? null : lineComment;
    boolean block = !isBlank(blockStart) && !isBlank(blockEnd);
    this.blockStart = block ? blockStart : null;
    this.blockEnd = block ? blockEnd : null;
    boolean block2 = !isBlank(blockStart2) && !isBlank(blockEnd2);
    this.blockStart2 = block2 ? blockStart2 : null;
    this.blockEnd2 = block2 ? blockEnd2 : null;
    this.doubleQuote = doubleQuote;
    this.singleQuote = singleQuote;
    this.backtick = backtick;
    this.tripleQuote = tripleQuote;
    this.multiLineBacktick = multiLineBacktick;
  }

  private static boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }
}
