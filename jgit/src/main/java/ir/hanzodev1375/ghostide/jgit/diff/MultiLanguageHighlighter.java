package ir.hanzodev1375.ghostide.jgit.diff;

import android.graphics.Color;
import java.util.*;
import java.util.regex.*;

public class MultiLanguageHighlighter implements SyntaxHighlighter {

  private static final int COLOR_KEYWORD = Color.parseColor("#569CD6");
  private static final int COLOR_FUNCTION = Color.parseColor("#DCDCAA");
  private static final int COLOR_VARIABLE = Color.parseColor("#9CDCFE");
  private static final int COLOR_STRING = Color.parseColor("#FF08FF00");
  private static final int COLOR_NUMBER = Color.parseColor("#B5CEA8");
  private static final int COLOR_COMMENT = Color.parseColor("#6A9955");
  private static final int COLOR_OPERATOR = Color.parseColor("#D4D4D4");
  private static final int COLOR_ANNOTATION = Color.parseColor("#C586C0");

  private static class Grammar {
    Pattern keywords;
    Pattern functions;
    Pattern annotations;
    Pattern operators;
    Pattern numbers;
    Pattern strings;
    Pattern comments;
  }

  private static final Pattern IDENTIFIER = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b");

  private static final Map<String, Grammar> GRAMMARS = new HashMap<>();

  static {
    Grammar java = new Grammar();
    java.keywords =
        Pattern.compile(
            "\\b(abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|extends|final|finally|float|for|goto|if|implements|import|instanceof|int|interface|long|native|new|package|private|protected|public|return|short|static|strictfp|super|switch|synchronized|this|throw|throws|transient|try|void|volatile|while|null|true|false)\\b");
    java.functions = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
    java.annotations = Pattern.compile("@[a-zA-Z_][a-zA-Z0-9_]*");
    java.numbers = Pattern.compile("\\b(\\d+\\.?\\d*|\\.\\d+)\\b");
    java.strings = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"");
    java.comments = Pattern.compile("//.*$|/\\*[\\s\\S]*?\\*/", Pattern.MULTILINE);
    GRAMMARS.put("java", java);

    Grammar kotlin = new Grammar();
    kotlin.keywords =
        Pattern.compile(
            "\\b(abstract|actual|annotation|as|break|by|catch|class|companion|const|constructor|continue|crossinline|data|delegate|do|dynamic|else|enum|expect|external|false|field|final|finally|for|fun|get|if|import|in|infix|init|inline|inner|interface|internal|is|lateinit|noinline|null|object|open|operator|out|override|package|param|private|property|protected|public|receiver|reified|return|sealed|set|super|suspend|tailrec|this|throw|true|try|typealias|typeof|val|var|vararg|when|where|while)\\b");
    kotlin.functions = Pattern.compile("\\bfun\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
    kotlin.annotations = Pattern.compile("@[a-zA-Z_][a-zA-Z0-9_]*");
    kotlin.numbers = java.numbers;
    kotlin.strings = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"");
    kotlin.comments = Pattern.compile("//.*$|/\\*[\\s\\S]*?\\*/", Pattern.MULTILINE);
    GRAMMARS.put("kotlin", kotlin);

    Grammar python = new Grammar();
    python.keywords =
        Pattern.compile(
            "\\b(and|as|assert|async|await|break|class|continue|def|del|elif|else|except|False|finally|for|from|global|if|import|in|is|lambda|None|nonlocal|not|or|pass|raise|return|True|try|while|with|yield)\\b");
    python.functions = Pattern.compile("\\bdef\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
    python.annotations = Pattern.compile("@[a-zA-Z_][a-zA-Z0-9_]*");
    python.numbers = java.numbers;

    python.strings =
        Pattern.compile(
            "(['\"])(?:(?!\\1|\\\\).|\\\\.)*\\1|\"\"\"[\\s\\S]*?\"\"\"|'''[\\s\\S]*?'''");
    python.comments = Pattern.compile("#.*$");
    GRAMMARS.put("python", python);

    Grammar js = new Grammar();
    js.keywords =
        Pattern.compile(
            "\\b(abstract|arguments|await|boolean|break|byte|case|catch|char|class|const|continue|debugger|default|delete|do|double|else|enum|eval|export|extends|false|final|finally|float|for|function|goto|if|implements|import|in|instanceof|int|interface|let|long|native|new|null|package|private|protected|public|return|short|static|super|switch|synchronized|this|throw|throws|transient|true|try|typeof|var|void|volatile|while|with|yield)\\b");
    js.functions = Pattern.compile("\\bfunction\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
    js.annotations = Pattern.compile("@[a-zA-Z_][a-zA-Z0-9_]*");
    js.numbers = java.numbers;

    js.strings =
        Pattern.compile("(['\"])(?:(?!\\1|\\\\).|\\\\.)*\\1|`[^`\\\\]*(?:\\\\.[^`\\\\]*)*`");
    js.comments = Pattern.compile("//.*$|/\\*[\\s\\S]*?\\*/", Pattern.MULTILINE);
    GRAMMARS.put("javascript", js);

    Grammar cpp = new Grammar();
    cpp.keywords =
        Pattern.compile(
            "\\b(alignas|alignof|and|and_eq|asm|auto|bitand|bitor|bool|break|case|catch|char|class|compl|const|constexpr|const_cast|continue|decltype|default|delete|do|double|dynamic_cast|else|enum|explicit|export|extern|false|float|for|friend|goto|if|inline|int|long|mutable|namespace|new|noexcept|not|not_eq|nullptr|operator|or|or_eq|private|protected|public|register|reinterpret_cast|return|short|signed|sizeof|static|static_assert|static_cast|struct|switch|template|this|throw|true|try|typedef|typeid|typename|union|unsigned|using|virtual|void|volatile|while|xor|xor_eq)\\b");
    cpp.functions = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
    cpp.annotations = Pattern.compile("__[a-zA-Z_][a-zA-Z0-9_]*__");
    cpp.numbers = java.numbers;
    cpp.strings = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"");
    cpp.comments = Pattern.compile("//.*$|/\\*[\\s\\S]*?\\*/", Pattern.MULTILINE);
    GRAMMARS.put("cpp", cpp);

    Grammar csharp = new Grammar();
    csharp.keywords =
        Pattern.compile(
            "\\b(abstract|add|alias|as|ascending|async|await|base|bool|break|by|byte|case|catch|char|checked|class|const|continue|decimal|default|delegate|descending|do|double|dynamic|else|enum|equals|explicit|extern|false|finally|fixed|float|for|foreach|from|get|global|goto|group|if|implicit|in|int|interface|internal|into|is|join|let|lock|long|nameof|namespace|new|null|object|on|operator|orderby|out|override|params|partial|private|protected|public|readonly|ref|remove|return|sbyte|sealed|select|set|short|sizeof|stackalloc|static|string|struct|switch|this|throw|true|try|typeof|uint|ulong|unchecked|unsafe|ushort|using|value|var|virtual|void|volatile|when|where|while|yield)\\b");
    csharp.functions = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
    csharp.annotations = Pattern.compile("@[a-zA-Z_][a-zA-Z0-9_]*");
    csharp.numbers = java.numbers;
    csharp.strings = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|@\"[^\"]*\"");
    csharp.comments = Pattern.compile("//.*$|/\\*[\\s\\S]*?\\*/", Pattern.MULTILINE);
    GRAMMARS.put("csharp", csharp);

    Grammar php = new Grammar();
    php.keywords =
        Pattern.compile(
            "\\b(abstract|and|array|as|break|callable|case|catch|class|clone|const|continue|declare|default|die|do|echo|else|elseif|empty|enddeclare|endfor|endforeach|endif|endswitch|endwhile|eval|exit|extends|final|finally|for|foreach|function|global|goto|if|implements|include|include_once|instanceof|insteadof|interface|isset|list|namespace|new|or|print|private|protected|public|require|require_once|return|static|switch|throw|trait|try|unset|use|var|while|xor|yield)\\b");
    php.functions = Pattern.compile("\\bfunction\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
    php.annotations = Pattern.compile("@[a-zA-Z_][a-zA-Z0-9_]*");
    php.numbers = java.numbers;
    php.strings = Pattern.compile("(['\"])(?:(?!\\1|\\\\).|\\\\.)*\\1");
    php.comments = Pattern.compile("//.*$|#.*$|/\\*[\\s\\S]*?\\*/", Pattern.MULTILINE);
    GRAMMARS.put("php", php);

    Grammar ruby = new Grammar();
    ruby.keywords =
        Pattern.compile(
            "\\b(BEGIN|END|alias|and|begin|break|case|class|def|defined|do|else|elsif|end|ensure|false|for|if|in|module|next|nil|not|or|redo|rescue|retry|return|self|super|then|true|undef|unless|until|when|while|yield)\\b");
    ruby.functions = Pattern.compile("\\bdef\\s+([a-zA-Z_][a-zA-Z0-9_]*[!?]?)\\s*\\(");
    ruby.annotations = Pattern.compile("@[a-zA-Z_][a-zA-Z0-9_]*");
    ruby.numbers = java.numbers;
    ruby.strings = Pattern.compile("(['\"])(?:(?!\\1|\\\\).|\\\\.)*\\1|%[qQwWx]?\\{[^}]*\\}");
    ruby.comments = Pattern.compile("#.*$");
    GRAMMARS.put("ruby", ruby);

    Grammar go = new Grammar();
    go.keywords =
        Pattern.compile(
            "\\b(break|case|chan|const|continue|default|defer|else|fallthrough|for|func|go|goto|if|import|interface|map|package|range|return|select|struct|switch|type|var)\\b");
    go.functions = Pattern.compile("\\bfunc\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
    go.annotations = Pattern.compile("@[a-zA-Z_][a-zA-Z0-9_]*");
    go.numbers = java.numbers;
    go.strings = Pattern.compile("`[^`]*`|\"([^\"\\\\]|\\\\.)*\"");
    go.comments = Pattern.compile("//.*$|/\\*[\\s\\S]*?\\*/", Pattern.MULTILINE);
    GRAMMARS.put("go", go);

    Grammar rust = new Grammar();
    rust.keywords =
        Pattern.compile(
            "\\b(as|break|const|continue|crate|dyn|else|enum|extern|false|fn|for|if|impl|in|let|loop|match|mod|move|mut|pub|ref|return|self|Self|static|struct|super|trait|true|type|unsafe|use|where|while|abstract|async|await|become|box|do|final|macro|override|priv|try|typeof|unsized|virtual|yield)\\b");
    rust.functions = Pattern.compile("\\bfn\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
    rust.annotations = Pattern.compile("#\\[[a-zA-Z_][a-zA-Z0-9_]*\\]");
    rust.numbers = java.numbers;
    rust.strings = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"|r#\"[^\"]*\"#");
    rust.comments = Pattern.compile("//.*$|/\\*[\\s\\S]*?\\*/", Pattern.MULTILINE);
    GRAMMARS.put("rust", rust);

    Grammar swift = new Grammar();
    swift.keywords =
        Pattern.compile(
            "\\b(associatedtype|class|deinit|enum|extension|fileprivate|func|import|init|inout|internal|let|open|operator|private|protocol|public|static|struct|subscript|typealias|var|break|case|continue|default|defer|do|else|fallthrough|for|guard|if|in|repeat|return|switch|where|while|as|Any|catch|false|is|nil|rethrows|super|self|Self|throw|throws|true|try|associativity|convenience|dynamic|didSet|final|get|infix|indirect|lazy|left|mutating|nonmutating|optional|override|postfix|precedence|prefix|Protocol|required|right|set|Type|unowned|weak|willSet)\\b");
    swift.functions = Pattern.compile("\\bfunc\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
    swift.annotations = Pattern.compile("@[a-zA-Z_][a-zA-Z0-9_]*");
    swift.numbers = java.numbers;
    swift.strings = Pattern.compile("\"([^\"\\\\]|\\\\.)*\"");
    swift.comments = Pattern.compile("//.*$|/\\*[\\s\\S]*?\\*/", Pattern.MULTILINE);
    GRAMMARS.put("swift", swift);

    Grammar ts = new Grammar();
    ts.keywords =
        Pattern.compile(
            "\\b(abstract|arguments|await|boolean|break|byte|case|catch|char|class|const|continue|debugger|declare|default|delete|do|double|else|enum|eval|export|extends|false|final|finally|float|for|function|goto|if|implements|import|in|instanceof|int|interface|let|long|native|new|null|package|private|protected|public|return|short|static|super|switch|synchronized|this|throw|throws|transient|true|try|typeof|var|void|volatile|while|with|yield|any|boolean|never|number|string|symbol|unknown|readonly)\\b");
    ts.functions = Pattern.compile("\\bfunction\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
    ts.annotations = Pattern.compile("@[a-zA-Z_][a-zA-Z0-9_]*");
    ts.numbers = java.numbers;
    ts.strings = Pattern.compile("(['\"])(?:(?!\\1|\\\\).|\\\\.)*\\1");
    ts.comments = Pattern.compile("//.*$|/\\*[\\s\\S]*?\\*/", Pattern.MULTILINE);
    GRAMMARS.put("typescript", ts);
  }

  public static String detectLanguageFromFileName(String fileName) {
    if (fileName == null) return "java";
    String lower = fileName.toLowerCase();
    if (lower.endsWith(".java")) return "java";
    if (lower.endsWith(".kt") || lower.endsWith(".kts")) return "kotlin";
    if (lower.endsWith(".py")) return "python";
    if (lower.endsWith(".js") || lower.endsWith(".mjs")) return "javascript";
    if (lower.endsWith(".cpp")
        || lower.endsWith(".cc")
        || lower.endsWith(".cxx")
        || lower.endsWith(".hpp")) return "cpp";
    if (lower.endsWith(".cs")) return "csharp";
    if (lower.endsWith(".php")) return "php";
    if (lower.endsWith(".rb")) return "ruby";
    if (lower.endsWith(".go")) return "go";
    if (lower.endsWith(".rs")) return "rust";
    if (lower.endsWith(".swift")) return "swift";
    if (lower.endsWith(".ts") || lower.endsWith(".tsx")) return "typescript";
    return "java";
  }

  /** خط‌های خیلی بلند (minified و ...) هایلایت نمی‌شوند؛ هزینه‌ی regex روی آن‌ها به فریم‌ها می‌رسید. */
  private static final int MAX_HIGHLIGHT_LENGTH = 800;

  private static final List<SyntaxHighlighter.HighlightSpan> NO_SPANS = Collections.emptyList();

  private static final Comparator<SyntaxHighlighter.HighlightSpan> BY_START =
      (a, b) -> Integer.compare(a.start, b.start);

  @Override
  public void highlight(String text, DiffLine.LineType type, String language, Callback callback) {
    // هدر diff (diff --git / @@ ...) کد نیست؛ با رنگ هدر کشیده می‌شود
    if (type == DiffLine.LineType.HEADER || text.length() > MAX_HIGHLIGHT_LENGTH) {
      callback.onResult(NO_SPANS);
      return;
    }

    Grammar grammar = GRAMMARS.get(language);
    if (grammar == null) grammar = GRAMMARS.get("java");

    // used[i] == true یعنی کاراکتر i قبلاً به یک span (یا داخل کامنت/استرینگ) تعلق دارد. با این
    // آرایه هر بررسی هم‌پوشانی O(طول توکن) است و دیگر نه لیست ignoreRange پیمایش می‌شود، نه
    // HashSet با کلید String برای حذف تکراری‌ها ساخته می‌شود.
    boolean[] used = new boolean[text.length()];

    List<SyntaxHighlighter.HighlightSpan> ignoreRanges = new ArrayList<>(4);
    addIgnoreRanges(ignoreRanges, text, grammar.comments);
    addIgnoreRanges(ignoreRanges, text, grammar.strings);
    sortRanges(ignoreRanges);
    for (int i = 0; i < ignoreRanges.size(); i++) {
      SyntaxHighlighter.HighlightSpan r = ignoreRanges.get(i);
      markUsed(used, r.start, r.end);
    }

    List<SyntaxHighlighter.HighlightSpan> spans = new ArrayList<>(16);

    // ترتیب همان اولویت قبلی است: اولین دسته‌ای که یک کاراکتر را بگیرد برنده است
    addMatches(spans, used, text, grammar.keywords, COLOR_KEYWORD, 0);
    addMatches(spans, used, text, grammar.functions, COLOR_FUNCTION, 1);
    addMatches(spans, used, text, grammar.annotations, COLOR_ANNOTATION, 0);
    addMatches(spans, used, text, grammar.operators, COLOR_OPERATOR, 0);
    addMatches(spans, used, text, grammar.numbers, COLOR_NUMBER, 0);
    addMatches(spans, used, text, IDENTIFIER, COLOR_VARIABLE, 1);

    List<SyntaxHighlighter.HighlightSpan> bracketSpans =
        RainbowBracketHighlighter.findBrackets(text, ignoreRanges);
    for (int i = 0; i < bracketSpans.size(); i++) {
      SyntaxHighlighter.HighlightSpan b = bracketSpans.get(i);
      if (isFree(used, b.start, b.end)) {
        markUsed(used, b.start, b.end);
        spans.add(b);
      }
    }

    // span ها هم‌پوشانی ندارند، پس مرتب‌سازی روی start کافی است
    spans.sort(BY_START);
    callback.onResult(spans);
  }

  /** ignore range ها بر اساس شروع مرتب می‌شوند (RainbowBracketHighlighter به این ترتیب تکیه دارد). */
  private static void sortRanges(List<SyntaxHighlighter.HighlightSpan> ranges) {
    ranges.sort(BY_START);
  }

  private static boolean isFree(boolean[] used, int start, int end) {
    for (int i = start; i < end; i++) {
      if (used[i]) return false;
    }
    return true;
  }

  private static void markUsed(boolean[] used, int start, int end) {
    int to = Math.min(end, used.length);
    for (int i = Math.max(start, 0); i < to; i++) used[i] = true;
  }

  private void addIgnoreRanges(
      List<SyntaxHighlighter.HighlightSpan> ranges, String text, Pattern pattern) {
    if (pattern == null) return;
    Matcher m = pattern.matcher(text);
    while (m.find()) {
      ranges.add(new SyntaxHighlighter.HighlightSpan(m.start(), m.end(), 0));
    }
  }

  /** group == 0 یعنی کل match، در غیر این صورت گروه مشخص‌شده (مثلاً اسم تابع بدون پرانتز). */
  private static void addMatches(
      List<SyntaxHighlighter.HighlightSpan> spans,
      boolean[] used,
      String text,
      Pattern pattern,
      int color,
      int group) {
    if (pattern == null) return;
    Matcher m = pattern.matcher(text);
    while (m.find()) {
      int start = m.start();
      int end = m.end();
      if (group > 0 && m.groupCount() >= group) {
        start = m.start(group);
        end = m.end(group);
      }
      if (start < 0 || end < 0) continue;
      if (!isFree(used, start, end)) continue;
      markUsed(used, start, end);
      spans.add(new SyntaxHighlighter.HighlightSpan(start, end, color));
    }
  }
}
