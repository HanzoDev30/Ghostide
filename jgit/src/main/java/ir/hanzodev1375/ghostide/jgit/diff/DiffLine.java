package ir.hanzodev1375.ghostide.jgit.diff;

public class DiffLine {
  public enum LineType {
    NORMAL,
    ADDED,
    REMOVED,
    HEADER,
    CONTEXT
  }

  private String text;
  private LineType type;
  private int lineNumber;
  private String lineNumberText;
  private byte plainAscii; // 0 = هنوز بررسی نشده، 1 = بله، 2 = خیر

  public DiffLine(String text, LineType type, int lineNumber) {
    this.text = text;
    this.type = type;
    this.lineNumber = lineNumber;
  }

  public String getText() {
    return text;
  }

  public LineType getType() {
    return type;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  /** String.valueOf(lineNumber) فقط یک‌بار ساخته می‌شود؛ قبلاً در هر فریم برای هر خط یک String جدید بود. */
  public String getLineNumberText() {
    if (lineNumberText == null) lineNumberText = String.valueOf(lineNumber);
    return lineNumberText;
  }

  /**
   * true اگر همه‌ی کاراکترها ASCII چاپی باشند. در این حالت با فونت mono عرض هر تکه از متن برابر
   * تعداد کاراکتر × عرض یک کاراکتر است و لازم نیست در هر فریم measureText (یک JNI call) صدا شود.
   * تب و کاراکترهای غیر ASCII عمداً کنار گذاشته شده‌اند چون عرضشان با mono یکسان نیست.
   */
  public boolean isPlainAscii() {
    if (plainAscii == 0) {
      boolean ok = true;
      for (int i = 0; i < text.length(); i++) {
        char c = text.charAt(i);
        if (c < 0x20 || c > 0x7E) {
          ok = false;
          break;
        }
      }
      plainAscii = (byte) (ok ? 1 : 2);
    }
    return plainAscii == 1;
  }
}
