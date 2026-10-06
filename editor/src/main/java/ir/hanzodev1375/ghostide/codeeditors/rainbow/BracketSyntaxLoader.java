package ir.hanzodev1375.ghostide.codeeditors.rainbow;

import android.content.Context;
import androidx.annotation.Nullable;
import ir.hanzodev1375.ghostide.codeeditors.textmate.TextMateGrammars;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * از {@code textmate/languages.json} مسیر {@code languageConfiguration} هر scope را پیدا می کند و
 * کامنت ها و کوتاسیون های آن زبان را می خواند. نتیجه cache می شود.
 */
final class BracketSyntaxLoader {

  /** زبان هایی که براکت رنگی برایشان بی معنی یا پر از مثبت کاذب است. */
  private static final Set<String> DISABLED =
      new HashSet<>(
          Arrays.asList(
              "text.plain",
              "text.log",
              "source.diff",
              "source.ignore",
              "source.ini",
              "source.properties",
              "text.html.markdown",
              "text.tex.latex",
              "text.xml",
              "text.html.basic",
              "text.html.htmx"));

  private static final Set<String> TRIPLE =
      new HashSet<>(
          Arrays.asList(
              "source.python",
              "source.kotlin",
              "source.java",
              "source.swift",
              "source.dart",
              "source.cs",
              "source.groovy",
              "source.toml"));

  private static final Set<String> MULTILINE_BACKTICK =
      new HashSet<>(
          Arrays.asList(
              "source.js", "source.js.jsx", "source.ts", "source.tsx", "source.go", "source.dart"));

  private static final Map<String, BracketSyntax> CACHE = new HashMap<>();
  private static Map<String, String> configPathByScope;

  private BracketSyntaxLoader() {}

  /** null یعنی این زبان رنگین کمانی نمی گیرد. */
  @Nullable
  static synchronized BracketSyntax forScope(Context context, @Nullable String scope) {
    return forScope(context, scope, true);
  }

  /**
   * @param respectDisabled اگر true باشد زبان‌های در لیست DISABLED مقدار null برمی‌گردانند.
   *                        وقتی کاربر در تنظیمات rainbow brackets را روشن کرده، false فرستاده می‌شود
   *                        تا به جای غیرفعال کردن کل، از GENERIC استفاده شود.
   */
  @Nullable
  static synchronized BracketSyntax forScope(Context context, @Nullable String scope, boolean respectDisabled) {
    if (scope == null) {
      return BracketSyntax.GENERIC;
    }
    // سینتکسی که پلاگین خودش ثبت کرده از همه مقدم است
    BracketSyntax registered = RainbowBracketRegistry.get(scope);
    if (registered != null) {
      return registered;
    }
    if (respectDisabled && DISABLED.contains(scope)) {
      return null;
    }
    BracketSyntax cached = CACHE.get(scope);
    if (cached != null) {
      return cached;
    }
    BracketSyntax syntax = load(context.getApplicationContext(), scope);
    // اسکوپ افزونه ممکن است دیرتر از languages.json ثبت شود؛ پیش فرض موقت را ماندگار نمی کنیم
    if (configPathByScope != null && configPathByScope.containsKey(scope)) {
      CACHE.put(scope, syntax);
    }
    return syntax;
  }

  private static BracketSyntax load(Context context, String scope) {
    try {
      if (configPathByScope == null) {
        configPathByScope = readConfigPaths(context);
      }
      String path = configPathByScope.get(scope);
      if (path == null) {
        // گرامر پلاگین بدون language-configuration (مثل source.astro)
        return BracketSyntax.GENERIC;
      }
      JSONObject config = new JSONObject(readAsset(context, path));

      String lineComment = null;
      String blockStart = null;
      String blockEnd = null;
      JSONObject comments = config.optJSONObject("comments");
      if (comments != null) {
        Object line = comments.opt("lineComment");
        if (line instanceof String) {
          lineComment = (String) line;
        }
        JSONArray block = comments.optJSONArray("blockComment");
        if (block != null && block.length() == 2) {
          blockStart = block.optString(0, null);
          blockEnd = block.optString(1, null);
        }
      }
      // بعضی کانفیگ ها (ini/properties/diff) برای بلوک مقدار ساختگی دارند
      if (blockStart != null && blockStart.equals(lineComment)) {
        blockStart = null;
        blockEnd = null;
      }

      boolean dq = false;
      boolean sq = false;
      boolean bt = false;
      for (String key : new String[] {"autoClosingPairs", "surroundingPairs"}) {
        JSONArray pairs = config.optJSONArray(key);
        if (pairs == null) {
          continue;
        }
        for (int i = 0; i < pairs.length(); i++) {
          char q = sameCharPair(pairs.opt(i));
          dq |= q == '"';
          sq |= q == '\'';
          bt |= q == '`';
        }
      }
      if (!dq && !sq && !bt) {
        dq = true;
      }

      return new BracketSyntax(
          lineComment,
          blockStart,
          blockEnd,
          dq,
          sq,
          bt,
          TRIPLE.contains(scope),
          MULTILINE_BACKTICK.contains(scope));
    } catch (Exception e) {
      return BracketSyntax.GENERIC;
    }
  }

  /** اگر جفت از یک کاراکتر یکسان تشکیل شده باشد همان را برمی گرداند، وگرنه 0. */
  private static char sameCharPair(Object entry) {
    String open = null;
    String close = null;
    if (entry instanceof JSONArray) {
      JSONArray array = (JSONArray) entry;
      open = array.optString(0, null);
      close = array.optString(1, null);
    } else if (entry instanceof JSONObject) {
      JSONObject object = (JSONObject) entry;
      open = object.optString("open", null);
      close = object.optString("close", null);
    }
    if (open != null && open.length() == 1 && open.equals(close)) {
      char c = open.charAt(0);
      if (c == '"' || c == '\'' || c == '`') {
        return c;
      }
    }
    return 0;
  }

  private static Map<String, String> readConfigPaths(Context context) throws Exception {
    Map<String, String> map = new HashMap<>();
    JSONArray languages =
        new JSONObject(readAsset(context, TextMateGrammars.LANGUAGES_JSON))
            .getJSONArray("languages");
    for (int i = 0; i < languages.length(); i++) {
      JSONObject language = languages.getJSONObject(i);
      String scope = language.optString("scopeName", null);
      String config = language.optString("languageConfiguration", null);
      if (scope != null && config != null && !config.isEmpty()) {
        map.put(scope, config);
      }
    }
    return map;
  }

  private static String readAsset(Context context, String path) throws Exception {
    try (InputStream in = context.getAssets().open(path)) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buffer = new byte[4096];
      int read;
      while ((read = in.read(buffer)) != -1) {
        out.write(buffer, 0, read);
      }
      return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
  }
}
