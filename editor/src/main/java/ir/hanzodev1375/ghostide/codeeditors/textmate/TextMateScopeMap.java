package ir.hanzodev1375.ghostide.codeeditors.textmate;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * نگاشت پسوند فایل به scope ی گرامر TextMate داخل assets. فقط زبانی ثبت می شود که گرامرش در assets
 * هست.
 */
public final class TextMateScopeMap {

  private static final Map<String, String> SCOPE_BY_EXTENSION = new HashMap<>();

  static {
    register("java", "source.java");
    register("c", "source.c");
    register("h", "source.c");
    register("cs", "source.cs");
    register("cpp", "source.cpp");
    register("cxx", "source.cpp");
    register("hpp", "source.cpp");
    register("hxx", "source.cpp");
    register("cc", "source.cpp");
    register("css", "source.css");
    register("js", "source.js");
    register("mjs", "source.js");
    register("jsx", "source.js.jsx");
    register("cjs", "source.js");
    register("py", "source.python");
    register("json", "source.json");
    register("gth", "source.json");
    register("xml", "text.xml");
    register("kt", "source.kotlin");
    register("kts", "source.kotlin");
    register("toml", "source.toml");
    register("gradle", "source.groovy");
    register("groovy", "source.groovy");
    register("scss", "source.css.scss");
    register("md", "text.html.markdown");
    register("markdown", "text.html.markdown");
    register("yml", "source.yaml");
    register("yaml", "source.yaml");
    register("lua", "source.lua");
    register("go", "source.go");
    register("php", "text.html.php");
    register("dart", "source.dart");
    register("ts", "source.ts");
    register("tsx", "source.tsx");
    register("sql", "source.sql");
    register("sh", "source.shell");
    register("bash", "source.shell");
    register("rc", "source.shell");
    register("bashrc", "source.shell");
    register("ash", "source.shell");
    register("zsh", "source.shell");
    register("zshrc", "source.shell");
    register("rs", "source.rust");
    register("rb", "source.ruby");
    register("ini", "source.ini");
    register("cfg", "source.ini");
    register("zig", "source.zig");
    register("asm", "source.asm");
    register("s", "source.asm");
    register("nasm", "source.asm");
    register("swift", "source.swift");
    register("r", "source.r");
    register("nim", "source.nim");
    register("txt", "text.plain");
    register("log", "text.log");
    register("bat", "source.batchfile");
    register("cmd", "source.batchfile");
    register("cmake", "source.cmake");
    register("coq", "source.coq");
    register("diff", "source.diff");
    register("patch", "source.diff");
    register("htmx", "text.html.htmx");
    register("ignore", "source.ignore");
    register("tex", "text.tex.latex");
    register("sty", "text.tex.latex");
    register("cls", "text.tex.latex");
    register("less", "source.css.less");
    register("lisp", "source.lisp");
    register("cl", "source.lisp");
    register("nix", "source.nix");
    register("pas", "source.pascal");
    register("pp", "source.pascal");
    register("ps1", "source.powershell");
    register("psm1", "source.powershell");
    register("psd1", "source.powershell");
    register("properties", "source.properties");
    register("smali", "source.smali");
  }

  private TextMateScopeMap() {}

  private static void register(String ext, String scope) {
    SCOPE_BY_EXTENSION.put(ext, scope);
  }

  /** scope ی گرامر برای فایل؛ اگر گرامری در assets نباشد null برمی گرداند. */
  public static String scopeOf(String filePath) {
    if (filePath == null) {
      return null;
    }
    return SCOPE_BY_EXTENSION.get(extensionOf(filePath));
  }

  public static boolean isSupported(String filePath) {
    return filePath != null && SCOPE_BY_EXTENSION.containsKey(extensionOf(filePath));
  }

  private static String extensionOf(String filePath) {
    int slash = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
    String name = slash >= 0 ? filePath.substring(slash + 1) : filePath;
    int dot = name.lastIndexOf('.');
    if (dot < 0 || dot == name.length() - 1) {
      return "";
    }
    return name.substring(dot + 1).toLowerCase(Locale.ROOT);
  }
}
