package ir.hanzodev1375.ghostide.codeeditors.shortcut;

import android.view.KeyEvent;
import io.github.rosemoe.sora.event.EditorKeyEvent;
import java.util.Objects;

/** یک ترکیب کلید سخت‌افزاری قابل شخصی‌سازی (keyCode + مودیفایرها). */
public class ShortcutKey {

  private int keyCode;
  private boolean ctrl;
  private boolean shift;
  private boolean alt;

  public ShortcutKey() {}

  public ShortcutKey(int keyCode, boolean ctrl, boolean shift, boolean alt) {
    this.keyCode = keyCode;
    this.ctrl = ctrl;
    this.shift = shift;
    this.alt = alt;
  }

  public static ShortcutKey of(int keyCode, boolean ctrl, boolean shift, boolean alt) {
    return new ShortcutKey(keyCode, ctrl, shift, alt);
  }

  public static ShortcutKey of(int keyCode) {
    return new ShortcutKey(keyCode, false, false, false);
  }

  public static ShortcutKey from(KeyEvent event) {
    int meta = event.getMetaState();
    return new ShortcutKey(
        event.getKeyCode(),
        (meta & KeyEvent.META_CTRL_ON) != 0,
        (meta & KeyEvent.META_SHIFT_ON) != 0,
        (meta & KeyEvent.META_ALT_ON) != 0);
  }

  public static ShortcutKey from(EditorKeyEvent event) {
    return new ShortcutKey(
        event.getKeyCode(), event.isCtrlPressed(), event.isShiftPressed(), event.isAltPressed());
  }

  public int getKeyCode() {
    return keyCode;
  }

  public boolean isCtrl() {
    return ctrl;
  }

  public boolean isShift() {
    return shift;
  }

  public boolean isAlt() {
    return alt;
  }

  public boolean hasModifier() {
    return ctrl || shift || alt;
  }

  public String getDisplayName() {
    StringBuilder sb = new StringBuilder();
    if (ctrl) sb.append("Ctrl+");
    if (shift) sb.append("Shift+");
    if (alt) sb.append("Alt+");

    String name = KeyEvent.keyCodeToString(keyCode);
    if (name == null) {
      name = String.valueOf(keyCode);
    } else if (name.startsWith("KEYCODE_")) {
      name = name.substring("KEYCODE_".length());
    }
    sb.append(name.replace('_', ' '));
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ShortcutKey)) return false;
    ShortcutKey that = (ShortcutKey) o;
    return keyCode == that.keyCode && ctrl == that.ctrl && shift == that.shift && alt == that.alt;
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyCode, ctrl, shift, alt);
  }

  @Override
  public String toString() {
    return getDisplayName();
  }
}