package ir.hanzodev1375.ghostide.codeeditors.snippets;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * همگام با LanguageManager اپ، پسوند فایل را به نام‌های زبان تبدیل می‌کند تا scope اسنیپت کاربر با
 * زبانِ فایلِ بازشده مقایسه شود. اسنیپتی که scope خالی دارد در همه زبان‌ها نمایش داده می‌شود.
 */
public final class SnippetScopeUtil {

  private static final Map<String, Set<String>> SCOPES_BY_EXT = new HashMap<>();

  private SnippetScopeUtil() {}

  private static void register(String ext, String... scopes) {
    Set<String> set = new HashSet<>();
    for (String s : scopes) {
      set.add(s.toLowerCase(Locale.ROOT));
    }
    SCOPES_BY_EXT.put(ext, Collections.unmodifiableSet(set));
  }

  static {
    register("java", "java");
    register("c", "c");
    register("cpp", "cpp", "c++", "cplusplus", "cxx");
    register("cxx", "cpp", "c++", "cplusplus", "cxx");
    register("hpp", "cpp", "c++", "cplusplus", "cxx");
    register("hxx", "cpp", "c++", "cplusplus", "cxx");
    register("cc", "cpp", "c++", "cplusplus", "cxx");
    register("h", "cpp", "c++", "cplusplus", "cxx", "c", "objc");
    register("cs", "csharp", "c-sharp", "c#", "cs");
    register("html", "html", "xml");
    register("htm", "html", "xml");
    register("css", "css");
    register("scss", "scss", "sass", "css");
    register("sass", "scss", "sass", "css");
    register("js", "javascript", "js");
    register("mjs", "javascript", "js");
    register("jsx", "javascript", "js", "jsx");
    register("py", "python", "python3", "py");
    register("json", "json");
    register("gth", "json", "gth");
    register("xml", "xml", "html");
    register("kt", "kotlin", "kt", "kts");
    register("kts", "kotlin", "kt", "kts");
    register("ninja", "ninja");
    register("toml", "toml");
    register("gradle", "gradle", "groovy");
    register("groovy", "gradle", "groovy");
    register("md", "markdown", "md");
    register("markdown", "markdown", "md");
    register("yml", "yaml", "yml");
    register("yaml", "yaml", "yml");
    register("lua", "lua");
    register("go", "go", "golang");
    register("php", "php");
    register("dart", "dart");
    register("ts", "typescript", "ts", "tsx");
    register("tsx", "typescript", "ts", "tsx");
    register("sql", "sql");
    register("sh", "shell", "sh", "bash", "zsh");
    register("bash", "shell", "sh", "bash", "zsh");
    register("bashrc", "shell", "sh", "bash", "zsh");
    register("ash", "shell", "sh", "bash", "zsh");
    register("zsh", "shell", "sh", "bash", "zsh");
    register("zshrc", "shell", "sh", "bash", "zsh");
    register("rc", "shell", "sh", "bash", "zsh");
    register("rs", "rust", "rs");
    register("rb", "ruby", "rb");
    register("g4", "antlr");
    register("ini", "ini", "cfg", "properties");
    register("cfg", "ini", "cfg", "properties");
    register("properties", "ini", "cfg", "properties");
    register("zig", "zig");
    register("asm", "asm", "assembly", "nasm");
    register("s", "asm", "assembly", "nasm");
    register("nasm", "asm", "assembly", "nasm");
    register("swift", "swift");
    register("scala", "scala");
    register("sc", "scala");
    register("pl", "perl", "pl");
    register("pm", "perl", "pl");
    register("jl", "julia");
    register("r", "r");
    register("ex", "elixir");
    register("exs", "elixir");
    register("hs", "haskell");
    register("nim", "nim");
    register("sol", "solidity");
    register("txt", "plaintext", "text", "txt");
    register("log", "plaintext", "text", "txt");
    register("vue", "vue", "html");
  }

  /** بر اساس پسوند فایل، مجموعه scopeهای زبانِ جاری (کوچک‌شده) را برمی‌گرداند. */
  public static Set<String> scopesFor(String filePath) {
    if (filePath == null) return Collections.emptySet();
    int dot = filePath.lastIndexOf('.');
    if (dot < 0 || dot == filePath.length() - 1) return Collections.emptySet();
    String ext = filePath.substring(dot + 1).toLowerCase(Locale.ROOT);
    Set<String> scopes = SCOPES_BY_EXT.get(ext);
    return scopes != null ? scopes : Collections.emptySet();
  }

  /**
   * آیا اسنیپت با scope داده‌شده برای فایلِ بازشده مناسب است؟ scope خالی یعنی همه‌جا.
   *
   * <p>{@code scope} می‌تواند چند مقدار با کاما/سِمی‌کالن/فاصله داشته باشد، مثل {@code "java,kotlin"}
   * یا {@code "php"}.
   */
  public static boolean matches(String scope, String filePath) {
    if (scope == null) return true;
    String trimmed = scope.trim();
    if (trimmed.isEmpty()) return true;
    Set<String> fileScopes = scopesFor(filePath);
    if (fileScopes.isEmpty()) return false;
    String[] tokens = trimmed.toLowerCase(Locale.ROOT).split("[,;\\s]+");
    for (String token : tokens) {
      if (token.isEmpty()) continue;
      if (fileScopes.contains(token)) return true;
    }
    return false;
  }

  /** صرفاً برای تست. */
  public static Set<String> normalize(String scope) {
    if (scope == null || scope.trim().isEmpty()) return Collections.emptySet();
    return new HashSet<>(Arrays.asList(scope.toLowerCase(Locale.ROOT).split("[,;\\s]+")));
  }
}