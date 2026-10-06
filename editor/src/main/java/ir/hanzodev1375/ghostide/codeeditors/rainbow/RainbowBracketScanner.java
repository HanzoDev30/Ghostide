package ir.hanzodev1375.ghostide.codeeditors.rainbow;

/**
 * اسکنر خالص (بدون وابستگی به اندروید) که عمق تو در تویی براکت های {@code () [] {}} را حساب می کند.
 *
 * <p>کامنت ها و رشته ها طبق {@link BracketSyntax} رد می شوند. براکت بسته ی بدون جفت نادیده گرفته
 * می شود تا یک اشتباه در وسط فایل رنگ بقیه را خراب نکند.
 */
public final class RainbowBracketScanner {

  private RainbowBracketScanner() {}

  /** نتیجه: سه آرایه ی موازی مرتب بر اساس موقعیت (خط، ستون، عمق). */
  public static final class Result {
    public int[] line = new int[256];
    public int[] col = new int[256];
    public int[] depth = new int[256];
    public int size;

    void add(int l, int c, int d) {
      if (size == line.length) {
        int n = size * 2;
        line = java.util.Arrays.copyOf(line, n);
        col = java.util.Arrays.copyOf(col, n);
        depth = java.util.Arrays.copyOf(depth, n);
      }
      line[size] = l;
      col[size] = c;
      depth[size] = d;
      size++;
    }

    /** اولین اندیس که خطش >= {@code targetLine} است (جستجوی دودویی). */
    public int firstIndexAtOrAfterLine(int targetLine) {
      int lo = 0;
      int hi = size;
      while (lo < hi) {
        int mid = (lo + hi) >>> 1;
        if (line[mid] < targetLine) {
          lo = mid + 1;
        } else {
          hi = mid;
        }
      }
      return lo;
    }
  }

  public static Result scan(CharSequence text, BracketSyntax syn) {
    Result out = new Result();
    final int n = text.length();
    int line = 0;
    int lineStart = 0;
    int[] stack = new int[64];
    int sp = 0;
    int i = 0;

    while (i < n) {
      char c = text.charAt(i);

      if (c == '\n') {
        line++;
        lineStart = ++i;
        continue;
      }

      // کامنت بلوکی (تا دو نوع: مثلا /* */ و <!-- -->)
      String bs = null;
      String be = null;
      if (syn.blockStart != null && startsWith(text, i, syn.blockStart)) {
        bs = syn.blockStart;
        be = syn.blockEnd;
      } else if (syn.blockStart2 != null && startsWith(text, i, syn.blockStart2)) {
        bs = syn.blockStart2;
        be = syn.blockEnd2;
      }
      if (bs != null) {
        int end = indexOf(text, be, i + bs.length());
        int to = end < 0 ? n : end + be.length();
        for (int k = i; k < to; k++) {
          if (text.charAt(k) == '\n') {
            line++;
            lineStart = k + 1;
          }
        }
        i = to;
        continue;
      }

      // کامنت تک خطی؛ # فقط اول خط یا بعد از فاصله کامنت است (تا ${#x} در shell خراب نشود)
      if (syn.lineComment != null && startsWith(text, i, syn.lineComment)) {
        boolean hashInsideWord =
            syn.lineComment.equals("#") && i > lineStart && !isWhitespace(text.charAt(i - 1));
        if (!hashInsideWord) {
          while (i < n && text.charAt(i) != '\n') {
            i++;
          }
          continue;
        }
      }

      // آپاستروف داخل کلمه (don't, it's) شروع رشته نیست
      if (c == '\'' && i > lineStart && i + 1 < n
          && Character.isLetterOrDigit(text.charAt(i - 1))
          && Character.isLetter(text.charAt(i + 1))) {
        i++;
        continue;
      }

      // رشته ها
      if ((c == '"' && syn.doubleQuote)
          || (c == '\'' && syn.singleQuote)
          || (c == '`' && syn.backtick)) {

        if (syn.tripleQuote
            && c != '`'
            && i + 2 < n
            && text.charAt(i + 1) == c
            && text.charAt(i + 2) == c) {
          int end = indexOfTriple(text, c, i + 3);
          int to = end < 0 ? n : end + 3;
          for (int k = i; k < to; k++) {
            if (text.charAt(k) == '\n') {
              line++;
              lineStart = k + 1;
            }
          }
          i = to;
          continue;
        }

        boolean multi = c == '`' && syn.multiLineBacktick;
        int j = i + 1;
        boolean closed = false;
        while (j < n) {
          char d = text.charAt(j);
          if (d == '\\') {
            j += 2;
            continue;
          }
          if (d == c) {
            closed = true;
            break;
          }
          if (d == '\n' && !multi) {
            break;
          }
          j++;
        }
        if (closed) {
          for (int k = i; k <= j; k++) {
            if (text.charAt(k) == '\n') {
              line++;
              lineStart = k + 1;
            }
          }
          i = j + 1;
          continue;
        }
        // کوتاسیون بسته نشده (مثلا آپاستروف داخل متن)؛ مثل کاراکتر معمولی رد می شود
        i++;
        continue;
      }

      // براکت ها
      int open = openKind(c);
      if (open >= 0) {
        if (sp == stack.length) {
          stack = java.util.Arrays.copyOf(stack, sp * 2);
        }
        out.add(line, i - lineStart, sp);
        stack[sp++] = open;
      } else {
        int close = closeKind(c);
        if (close >= 0) {
          int idx = sp - 1;
          while (idx >= 0 && stack[idx] != close) {
            idx--;
          }
          if (idx >= 0) {
            sp = idx;
            out.add(line, i - lineStart, idx);
          }
        }
      }
      i++;
    }
    return out;
  }

  private static int openKind(char c) {
    switch (c) {
      case '(':
        return 0;
      case '[':
        return 1;
      case '{':
        return 2;
      default:
        return -1;
    }
  }

  private static int closeKind(char c) {
    switch (c) {
      case ')':
        return 0;
      case ']':
        return 1;
      case '}':
        return 2;
      default:
        return -1;
    }
  }

  private static boolean isWhitespace(char c) {
    return c == ' ' || c == '\t' || c == '\n' || c == '\r';
  }

  private static boolean startsWith(CharSequence text, int at, String token) {
    int len = token.length();
    if (at + len > text.length()) {
      return false;
    }
    for (int k = 0; k < len; k++) {
      if (text.charAt(at + k) != token.charAt(k)) {
        return false;
      }
    }
    return true;
  }

  private static int indexOf(CharSequence text, String token, int from) {
    int last = text.length() - token.length();
    for (int p = from; p <= last; p++) {
      if (startsWith(text, p, token)) {
        return p;
      }
    }
    return -1;
  }

  private static int indexOfTriple(CharSequence text, char q, int from) {
    int last = text.length() - 3;
    for (int p = from; p <= last; p++) {
      char ch = text.charAt(p);
      if (ch == '\\') {
        p++;
        continue;
      }
      if (ch == q && text.charAt(p + 1) == q && text.charAt(p + 2) == q) {
        return p;
      }
    }
    return -1;
  }
}
