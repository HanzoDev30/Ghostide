package ir.hanzodev1375.ghostide.dialogs;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.content.res.ColorStateList;
import androidx.core.widget.ImageViewCompat;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.codeeditors.shortcut.ShortcutAction;
import ir.hanzodev1375.ghostide.codeeditors.shortcut.ShortcutKey;
import ir.hanzodev1375.ghostide.codeeditors.shortcut.ShortcutManager;
import ir.hanzodev1375.components.sheet.customitemsheet.ui.DialogCompat;
import ir.theme.M3Theme;

/** فهرست اکشن‌های قابل شخصی‌سازی + دیالوگ ضبط میان‌بر. */
public class ShortcutSettingsDialog {

  private final Context context;
  private final float density;
  private LinearLayout list;

  public ShortcutSettingsDialog(Context context) {
    this.context = context;
    this.density = context.getResources().getDisplayMetrics().density;
  }

  public void showList() {
    ShortcutManager.init(context);

    list = new LinearLayout(context);
    list.setOrientation(LinearLayout.VERTICAL);

    ScrollView scroll = new ScrollView(context);
    scroll.setFillViewport(true);
    scroll.addView(list);

    new DialogCompat(context)
        .setTitle(R.string.shortcuts_title)
        .setView(scroll)
        .setNeutralButton(R.string.shortcuts_reset_all, (d, w) -> confirmResetAll())
        .setNegativeButton(android.R.string.cancel, null)
        .show();

    rebuild();
  }

  private void rebuild() {
    if (list == null) return;
    list.removeAllViews();
    for (ShortcutAction action : ShortcutAction.values()) {
      list.addView(buildRow(action));
    }
  }

  private View buildRow(ShortcutAction action) {
    int hPad = (int) (16 * density);
    int vPad = (int) (12 * density);

    LinearLayout row = new LinearLayout(context);
    row.setOrientation(LinearLayout.HORIZONTAL);
    row.setGravity(Gravity.CENTER_VERTICAL);
    row.setPadding(hPad, vPad, hPad, vPad);
    row.setMinimumHeight((int) (56 * density));

    ImageView icon = new ImageView(context);
    icon.setImageResource(iconResFor(action));
    int iconSize = (int) (24 * density);
    LinearLayout.LayoutParams iconLp =
        new LinearLayout.LayoutParams(iconSize, iconSize);
    iconLp.setMarginEnd(hPad);
    icon.setLayoutParams(iconLp);
    ImageViewCompat.setImageTintList(icon, colorStateList(M3Theme.onSurfaceVariant()));
    row.addView(icon);

    TextView label = new TextView(context);
    label.setText(context.getString(action.getLabelRes()));
    label.setTextColor(colorOf(M3Theme.onSurface()));
    label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
    label.setLayoutParams(
        new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
    row.addView(label);

    TextView keyView = new TextView(context);
    keyView.setText(ShortcutManager.effectiveKeyOf(action).getDisplayName());
    keyView.setTextColor(colorOf(M3Theme.onSecondaryContainer()));
    keyView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
    keyView.setBackground(keyChip());
    int hk = (int) (10 * density);
    int vk = (int) (4 * density);
    keyView.setPadding(hk, vk, hk, vk);
    row.addView(keyView);

    row.setOnClickListener(v -> showEditDialog(action));
    return row;
  }

  private GradientDrawable keyChip() {
    GradientDrawable chip = new GradientDrawable();
    chip.setColor(colorOf(M3Theme.secondaryContainer()));
    chip.setCornerRadius(16 * density);
    return chip;
  }

  private void showEditDialog(ShortcutAction action) {
    ShortcutManager.init(context);
    View view =
        LayoutInflater.from(context).inflate(R.layout.dialog_edit_shortcut, null, false);
    EditText input = view.findViewById(R.id.shortcut_input);
    CheckBox ctrlCb = view.findViewById(R.id.shortcut_ctrl);
    CheckBox shiftCb = view.findViewById(R.id.shortcut_shift);
    CheckBox altCb = view.findViewById(R.id.shortcut_alt);
    TextView conflict = view.findViewById(R.id.shortcut_conflict);

    final ShortcutKey[] current = {ShortcutManager.effectiveKeyOf(action)};

    final Runnable render =
        () -> {
          ShortcutKey key = current[0];
          ctrlCb.setChecked(key != null && key.isCtrl());
          shiftCb.setChecked(key != null && key.isShift());
          altCb.setChecked(key != null && key.isAlt());
          input.setText(key != null ? key.getDisplayName() : context.getString(R.string.shortcuts_empty));
        };

    final Runnable refreshConflict =
        () -> {
          ShortcutAction other = ShortcutManager.conflictingAction(action, current[0]);
          if (other != null) {
            conflict.setText(
                context.getString(
                    R.string.shortcut_conflict, context.getString(other.getLabelRes())));
            conflict.setVisibility(View.VISIBLE);
          } else {
            conflict.setVisibility(View.GONE);
          }
        };

    input.setOnKeyListener(
        (v, keyCode, event) -> {
          if (event.getAction() != KeyEvent.ACTION_DOWN) return true;
          if (isModifierKey(keyCode)) {
            ctrlCb.setChecked(event.isCtrlPressed());
            shiftCb.setChecked(event.isShiftPressed());
            altCb.setChecked(event.isAltPressed());
            return true;
          }
          ShortcutKey recorded = ShortcutKey.from(event);
          if (!ShortcutManager.isSupportedKey(recorded)) {
            conflict.setText(R.string.shortcut_not_supported);
            conflict.setVisibility(View.VISIBLE);
            return true;
          }
          current[0] = recorded;
          render.run();
          refreshConflict.run();
          return true;
        });

    CompoundButton.OnCheckedChangeListener cbListener =
        (button, checked) -> {
          if (current[0] == null) return;
          current[0] =
              new ShortcutKey(
                  current[0].getKeyCode(),
                  ctrlCb.isChecked(),
                  shiftCb.isChecked(),
                  altCb.isChecked());
          render.run();
          refreshConflict.run();
        };
    ctrlCb.setOnCheckedChangeListener(cbListener);
    shiftCb.setOnCheckedChangeListener(cbListener);
    altCb.setOnCheckedChangeListener(cbListener);

    render.run();

    new DialogCompat(context)
        .setTitle(context.getString(action.getLabelRes()))
        .setView(view)
        .setPositiveButton(
            android.R.string.ok,
            (d, w) -> {
              if (current[0] == null || !ShortcutManager.isSupportedKey(current[0])) return;
              ShortcutManager.setCustomKey(action, current[0]);
              rebuild();
            })
        .setNeutralButton(
            R.string.shortcut_use_default,
            (d, w) -> {
              ShortcutManager.resetAction(action);
              rebuild();
            })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  private void confirmResetAll() {
    new DialogCompat(context)
        .setTitle(R.string.shortcuts_reset_all)
        .setMessage(R.string.shortcuts_reset_all_confirm)
        .setPositiveButton(
            android.R.string.ok,
            (d, w) -> {
              ShortcutManager.resetAll();
              rebuild();
            })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  private static boolean isModifierKey(int keyCode) {
    switch (keyCode) {
      case KeyEvent.KEYCODE_CTRL_LEFT:
      case KeyEvent.KEYCODE_CTRL_RIGHT:
      case KeyEvent.KEYCODE_SHIFT_LEFT:
      case KeyEvent.KEYCODE_SHIFT_RIGHT:
      case KeyEvent.KEYCODE_ALT_LEFT:
      case KeyEvent.KEYCODE_ALT_RIGHT:
      case KeyEvent.KEYCODE_META_LEFT:
      case KeyEvent.KEYCODE_META_RIGHT:
        return true;
      default:
        return false;
    }
  }

  private static int iconResFor(ShortcutAction action) {
    switch (action) {
      case SAVE:
        return R.drawable.outline_save;
      case FIND:
        return R.drawable.outline_search;
      case GOTO_LINE:
        return R.drawable.outline_keyboard;
      case TOGGLE_COMMENT:
        return R.drawable.outline_comment;
      case UNDO:
        return R.drawable.outline_undo;
      case REDO:
        return R.drawable.outline_redo;
      case COPY:
        return R.drawable.outline_content_copy;
      case CUT:
        return R.drawable.outline_content_cut;
      case PASTE:
        return R.drawable.outline_content_paste;
      case SELECT_ALL:
        return R.drawable.outline_select_all;
      case DUPLICATE_LINE:
        return R.drawable.outline_copy_all;
      case SELECT_WORD:
        return R.drawable.outline_highlight;
      default:
        return 0;
    }
  }

  private ColorStateList colorStateList(Integer color) {
    return ColorStateList.valueOf(colorOf(color));
  }

  private static int colorOf(Integer color) {
    return color != null ? color : Color.TRANSPARENT;
  }
}