package ir.hanzodev1375.ghostide.codeeditors.rainbow;

import androidx.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ثبت سینتکس براکت برای اسکوپ هایی که فایل language-configuration.json ندارند (مثلا گرامرهایی که
 * پلاگین ها از assets خودشان بارگذاری می کنند). نمونه در پلاگین:
 *
 * <pre>
 * RainbowBracketRegistry.register("source.astro", BracketSyntax.GENERIC);
 * </pre>
 *
 * ثبت از هر ترد امن است و بر فایل پیکربندی داخلی مقدم می شود.
 */
public final class RainbowBracketRegistry {

  private static final ConcurrentHashMap<String, BracketSyntax> MAP = new ConcurrentHashMap<>();

  private RainbowBracketRegistry() {}

  public static void register(String scope, BracketSyntax syntax) {
    if (scope != null && syntax != null) {
      MAP.put(scope, syntax);
    }
  }

  public static void unregister(String scope) {
    if (scope != null) {
      MAP.remove(scope);
    }
  }

  @Nullable
  static BracketSyntax get(@Nullable String scope) {
    return scope == null ? null : MAP.get(scope);
  }
}
