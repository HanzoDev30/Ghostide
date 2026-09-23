package ir.hanzodev1375.ghostide.codeeditors.shortcut;

import android.view.KeyEvent;
import ir.hanzodev1375.ghostide.codeeditors.IdeEditor;
import ir.hanzodev1375.ghostide.codeeditors.R;

/** اکشن‌های قابل شخصی‌سازی ادیتور با شورت‌کات دیفالت و نحوه اجرا. */
public enum ShortcutAction {

  SAVE("shortcut.save", R.string.editor_save, new ShortcutKey(KeyEvent.KEYCODE_S, true, false, false)),
  FIND("shortcut.find", R.string.editor_find, new ShortcutKey(KeyEvent.KEYCODE_F, true, false, false)),
  GOTO_LINE("shortcut.goto_line", R.string.editor_goto_line, new ShortcutKey(KeyEvent.KEYCODE_G, true, false, false)),
  TOGGLE_COMMENT(
      "shortcut.toggle_comment",
      R.string.editor_toggle_comment,
      new ShortcutKey(KeyEvent.KEYCODE_C, true, true, false)),
  UNDO("shortcut.undo", R.string.editor_undo, new ShortcutKey(KeyEvent.KEYCODE_Z, true, false, false)),
  REDO("shortcut.redo", R.string.editor_redo, new ShortcutKey(KeyEvent.KEYCODE_Y, true, false, false)),
  COPY("shortcut.copy", R.string.editor_copy_text, new ShortcutKey(KeyEvent.KEYCODE_C, true, false, false)),
  CUT("shortcut.cut", R.string.editor_cut_text, new ShortcutKey(KeyEvent.KEYCODE_X, true, false, false)),
  PASTE("shortcut.paste", R.string.editor_paste_text, new ShortcutKey(KeyEvent.KEYCODE_V, true, false, false)),
  SELECT_ALL("shortcut.select_all", R.string.editor_select_all, new ShortcutKey(KeyEvent.KEYCODE_A, true, false, false)),
  DUPLICATE_LINE(
      "shortcut.duplicate_line",
      R.string.editor_duplicate_line,
      new ShortcutKey(KeyEvent.KEYCODE_D, true, false, false)),
  SELECT_WORD(
      "shortcut.select_word",
      R.string.editor_select_current_word,
      new ShortcutKey(KeyEvent.KEYCODE_W, true, false, false));

  private final String id;
  private final int labelRes;
  private final ShortcutKey defaultShortcut;

  ShortcutAction(String id, int labelRes, ShortcutKey defaultShortcut) {
    this.id = id;
    this.labelRes = labelRes;
    this.defaultShortcut = defaultShortcut;
  }

  public String getId() {
    return id;
  }

  public int getLabelRes() {
    return labelRes;
  }

  public ShortcutKey getDefaultShortcut() {
    return defaultShortcut;
  }

  /** اجرای اکشن روی ادیتور. برای اکشن‌هایی که به اکتیویتی نیاز دارند از کال‌بک‌های IdeEditor استفاده می‌شود. */
  public void execute(IdeEditor editor) {
    switch (this) {
      case SAVE:
        {
          Runnable r = editor.getOnSaveRequest();
          if (r != null) r.run();
          break;
        }
      case FIND:
        {
          Runnable r = editor.getOnSearchRequest();
          if (r != null) r.run();
          break;
        }
      case GOTO_LINE:
        {
          Runnable r = editor.getOnGotoLineRequest();
          if (r != null) r.run();
          break;
        }
      case TOGGLE_COMMENT:
        editor.toggleCommentForCurrentLine();
        break;
      case UNDO:
        editor.undo();
        break;
      case REDO:
        editor.redo();
        break;
      case COPY:
        editor.copyText();
        break;
      case CUT:
        editor.cutText();
        break;
      case PASTE:
        editor.pasteText();
        break;
      case SELECT_ALL:
        editor.selectAll();
        break;
      case DUPLICATE_LINE:
        editor.setDuplicateLine();
        break;
      case SELECT_WORD:
        editor.setSelectCurrentWord();
        break;
      default:
        break;
    }
  }
}