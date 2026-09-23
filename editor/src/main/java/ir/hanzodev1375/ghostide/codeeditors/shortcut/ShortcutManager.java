package ir.hanzodev1375.ghostide.codeeditors.shortcut;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.rosemoe.sora.event.EditorKeyEvent;
import io.github.rosemoe.sora.event.KeyBindingEvent;
import ir.hanzodev1375.ghostide.codeeditors.IdeEditor;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * مرکز ثبت و اجرای شورت‌کات‌های ادیتور.
 *
 * <p>ترکیب‌های سفارشی در SharedPreferences به‌صورت JSON نگهداری می‌شوند. اجرای اکشن‌ها روی
 * KeyBindingEvent انجام می‌شود تا فقط حالت‌های دارای مودیفایر (Ctrl/Shift/Alt + حروف/کلیدهای مجاز) قابل
 * تعریف باشند و بقیه‌ی ورودی‌های عادی به سورا برسند.
 */
public final class ShortcutManager {

  private static final String PREFS_KEY = "editor_keybindings";
  private static final Gson GSON = new Gson();
  private static final Type MAP_TYPE = new TypeToken<Map<String, ShortcutKey>>() {}.getType();

  private static final Map<String, ShortcutKey> customKeys = new HashMap<>();
  private static Map<ShortcutKey, ShortcutAction> effectiveLookup = new HashMap<>();
  private static SharedPreferences prefs;
  private static boolean initialized;

  private ShortcutManager() {}

  public static void init(Context context) {
    if (initialized) return;
    prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    load();
    initialized = true;
  }

  private static void requireInit() {
    if (!initialized) {
      throw new IllegalStateException("ShortcutManager.init(context) must be called first");
    }
  }

  @SuppressWarnings("unchecked")
  private static void load() {
    customKeys.clear();
    String raw = prefs.getString(PREFS_KEY, null);
    if (raw != null && !raw.isEmpty()) {
      try {
        Map<String, ShortcutKey> saved = GSON.fromJson(raw, MAP_TYPE);
        if (saved != null) customKeys.putAll(saved);
      } catch (Exception ignored) {
        // JSON خراب است؛ از حالت پیش‌فرض استفاده می‌شود
        customKeys.clear();
      }
    }
    rebuildEffective();
  }

  private static void save() {
    prefs.edit().putString(PREFS_KEY, GSON.toJson(customKeys, MAP_TYPE)).apply();
    rebuildEffective();
  }

  private static void rebuildEffective() {
    effectiveLookup.clear();
    for (ShortcutAction action : ShortcutAction.values()) {
      ShortcutKey key = effectiveKeyOf(action);
      if (key != null) effectiveLookup.put(key, action);
    }
  }

  /** ترکیب مؤثر یک اکشن: سفارشی در صورت وجود، وگرنه دیفالت. */
  public static ShortcutKey effectiveKeyOf(ShortcutAction action) {
    ShortcutKey custom = customKeys.get(action.getId());
    return custom != null ? custom : action.getDefaultShortcut();
  }

  /** ترکیب سفارشی ثبت‌شده برای اکشن؛ null یعنی از دیفالت استفاده می‌شود. */
  public static ShortcutKey customKeyOf(ShortcutAction action) {
    return customKeys.get(action.getId());
  }

  public static void setCustomKey(ShortcutAction action, ShortcutKey key) {
    requireInit();
    if (key == null) {
      customKeys.remove(action.getId());
    } else {
      customKeys.put(action.getId(), key);
    }
    save();
  }

  public static void resetAction(ShortcutAction action) {
    requireInit();
    customKeys.remove(action.getId());
    save();
  }

  public static void resetAll() {
    requireInit();
    customKeys.clear();
    save();
  }

  /** اکشنی که این ترکیب از قبل به آن اختصاص دارد؛ null یعنی تداخلی نیست. */
  public static ShortcutAction conflictingAction(ShortcutAction self, ShortcutKey key) {
    if (key == null) return null;
    ShortcutAction owner = effectiveLookup.get(key);
    return owner != null && owner != self ? owner : null;
  }

  /**
   * آیا این ترکیب از نظر سورا قابل پردازش به‌عنوان شورت‌کات است؟ فقط ترکیب (مودیفایر + حروف/کلیدهای
   * مجاز) کلید خورده و بقیه ورودی‌های عادی رد می‌شوند.
   */
  public static boolean isSupportedKey(ShortcutKey key) {
    if (key == null || !key.hasModifier()) return false;
    int keyCode = key.getKeyCode();
    if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) return true;
    return keyCode == KeyEvent.KEYCODE_ENTER
        || keyCode == KeyEvent.KEYCODE_DPAD_UP
        || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
        || keyCode == KeyEvent.KEYCODE_DPAD_LEFT
        || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
        || keyCode == KeyEvent.KEYCODE_MOVE_HOME
        || keyCode == KeyEvent.KEYCODE_MOVE_END
        || keyCode == KeyEvent.KEYCODE_SPACE;
  }

  /**
   * تلاش برای اجرای اکشن متناظر با این رویداد. اگر ترکیب معتبر و دارای اکشن باشد true برمی‌گرداند
   * (صدازننده باید رویداد را consume کند). اگر false برگردد سورا خودش ادامه می‌دهد.
   */
  public static boolean handleEvent(IdeEditor editor, KeyBindingEvent event) {
    if (editor == null || event == null) return false;
    if (event.getEventType() != EditorKeyEvent.Type.DOWN) return false;

    ShortcutKey key = ShortcutKey.from(event);
    ShortcutAction action = effectiveLookup.get(key);
    if (action == null) return false;

    action.execute(editor);
    return true;
  }

  public static Map<ShortcutKey, ShortcutAction> snapshotEffective() {
    return new HashMap<>(effectiveLookup);
  }
}