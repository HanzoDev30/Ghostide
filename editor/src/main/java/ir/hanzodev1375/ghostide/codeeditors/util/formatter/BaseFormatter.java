package ir.hanzodev1375.ghostide.codeeditors.util.formatter;

public abstract class BaseFormatter {
  public int tabSize = 2;

  protected String INDENT = getIndent(); // 2 tabs
  private static final String[] INDENT_CACHE = new String[50];

  public BaseFormatter() {
    INDENT_CACHE[0] = "";
    for (int i = 1; i < INDENT_CACHE.length; i++) {
      INDENT_CACHE[i] = INDENT_CACHE[i - 1] + INDENT;
    }
  }

  public abstract String format(String code);

  protected String getIndentString(int level) {
    if (level <= 0) return "";
    if (level < INDENT_CACHE.length) return INDENT_CACHE[level];

    StringBuilder sb = new StringBuilder(INDENT_CACHE[INDENT_CACHE.length - 1]);
    for (int i = INDENT_CACHE.length; i < level; i++) {
      sb.append(INDENT);
    }
    return sb.toString();
  }

  public String getIndent() {
    StringBuilder indent = new StringBuilder();
    for (int i = 0; i < tabSize; i++) {
      indent.append(" ");
    }
    return indent.toString();
  }
}
