package ir.hanzodev1375.ghostide.settings;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.google.android.material.slider.Slider;
import ir.hanzodev1375.components.sheet.customitemsheet.ui.DialogCompat;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.activity.SettingActivity;
import ir.hanzodev1375.ghostide.adapters.SettingsAdapter;
import ir.hanzodev1375.ghostide.models.SettingItem;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Base class for every settings section. All the shared wiring lives here so a new section (or a
 * new item in a section) needs almost no boilerplate:
 *
 * <p>1. Extend this class. 2. Return the rows from {@link #buildItems()}. 3. Handle row taps in
 * {@link #onItemClick(int)}.
 *
 * <p>Everything else (adapter creation, click listener, search filter, item refresh, single-choice
 * and slider dialogs) is implemented once below.
 */
public abstract class SettingSection {

  protected final SettingActivity activity;
  private SettingsAdapter adapter;

  public SettingSection(SettingActivity activity) {
    this.activity = activity;
  }

  /** Builds the section rows, in order. Position here == position used by {@link #onItemClick}. */
  protected abstract List<SettingItem> buildItems();

  /** Called when a non-switch row is tapped. Receives the ORIGINAL (pre-filter) position. */
  public abstract void onItemClick(int position);

  /** Builds rows, creates the adapter and wires the click listener. Call once per section. */
  public final SettingsAdapter createAdapter() {
    adapter = new SettingsAdapter(buildItems());
    adapter.setOnItemClickListener(this::onItemClick);
    return adapter;
  }

  public final SettingsAdapter getAdapter() {
    return adapter;
  }

  public final void filter(String query) {
    if (adapter != null) adapter.filter(query);
  }

  public final void resetToFull() {
    if (adapter != null) adapter.resetToFull();
  }

  // ---------------------------------------------------------------------
  // Rows refreshers
  // ---------------------------------------------------------------------

  protected void updateItemAt(int position, SettingItem newItem) {
    if (adapter != null) adapter.updateItem(position, newItem);
  }

  protected void updateDescriptionAt(int position, String description) {
    if (adapter == null) return;
    SettingItem item = adapter.getItemAtPosition(position);
    if (item != null) {
      item.setDescription(description);
      adapter.notifyItemChangedByOriginalPosition(position);
    }
  }

  protected void setCheckedAt(int position, boolean checked) {
    if (adapter == null) return;
    SettingItem item = adapter.getItemAtPosition(position);
    if (item != null) {
      item.setChecked(checked);
      adapter.notifyItemChangedByOriginalPosition(position);
    }
  }

  // ---------------------------------------------------------------------
  // Row builders
  // ---------------------------------------------------------------------

  protected SettingItem textItem(int titleRes, String description) {
    return new SettingItem(getString(titleRes), description, false, 0, null);
  }

  protected SettingItem textItem(int titleRes, int descriptionRes) {
    return textItem(titleRes, getString(descriptionRes));
  }

  protected SettingItem switchItem(
      int titleRes,
      int descriptionRes,
      boolean checked,
      SettingItem.OnSwitchChangeListener listener) {
    return new SettingItem(getString(titleRes), getString(descriptionRes), checked, 0, listener);
  }

  // ---------------------------------------------------------------------
  // Shared dialogs
  // ---------------------------------------------------------------------

  protected void showSingleChoiceDialog(
      int titleRes, String[] entries, int checkedIndex, Consumer<Integer> onSelected) {
    new DialogCompat(activity)
        .setTitle(titleRes)
        .setSingleChoiceItems(
            entries,
            checkedIndex,
            (dialog, which) -> {
              onSelected.accept(which);
              dialog.dismiss();
            })
        .setNegativeButton(R.string.cancel, null)
        .show();
  }

  protected void showSliderDialog(
      int titleRes,
      float from,
      float to,
      float step,
      float current,
      Function<Float, String> labelFormatter,
      Consumer<Float> onApply) {
    View view = LayoutInflater.from(activity).inflate(R.layout.dialog_slider, null);
    Slider slider = view.findViewById(R.id.slider);
    TextView valueText = view.findViewById(R.id.slider_value);
    Function<Float, String> formatter =
        labelFormatter != null
            ? labelFormatter
            : v -> String.format(Locale.US, "%.1f", v);
    slider.setValueFrom(from);
    slider.setValueTo(to);
    slider.setStepSize(step);
    slider.setValue(current);
    valueText.setText(formatter.apply(current));
    slider.addOnChangeListener((s, value, fromUser) -> valueText.setText(formatter.apply(value)));
    new DialogCompat(activity)
        .setTitle(titleRes)
        .setView(view)
        .setPositiveButton(
            R.string.ok, (d, w) -> onApply.accept(slider.getValue()))
        .setNegativeButton(R.string.cancel, null)
        .show();
  }


  protected String getString(int res) {
    return activity.getString(res);
  }

  protected String getString(int res, Object... formatArgs) {
    return activity.getString(res, formatArgs);
  }
}