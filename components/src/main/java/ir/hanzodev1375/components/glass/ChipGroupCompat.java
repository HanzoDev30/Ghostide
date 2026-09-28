package ir.hanzodev1375.components.glass;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import com.example.liquidglass.LiquidGlassChipGroup;

/**
 * {@link LiquidGlassChipGroup} that wires its own backdrop.
 *
 * <p>The group is a transparent container: it cannot capture a backdrop itself, so the source
 * has to be handed to the chips. Setting it here is enough — the library pushes it to every
 * chip in the group, including ones added later.
 */
public class ChipGroupCompat extends LiquidGlassChipGroup {

  public ChipGroupCompat(Context context) {
    super(context);
    init();
  }

  public ChipGroupCompat(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public ChipGroupCompat(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    // Spacing, single-line and selection are left to the layout, so the library's
    // app:glassChipSpacing* attributes are not overwritten here.
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    Glass.autoBackdrop(this);
  }

  /** Adds a glass chip carrying {@code text} and returns it for further tweaking. */
  public ChipCompat addChip(CharSequence text) {
    ChipCompat chip = new ChipCompat(getContext());
    chip.setText(text);
    addView(chip);
    return chip;
  }

  /** Adds a glass chip with a leading icon, e.g. a count. */
  public ChipCompat addStatChip(int iconRes, long value) {
    ChipCompat chip = new ChipCompat(getContext());
    chip.setStat(iconRes, value);
    addView(chip);
    return chip;
  }
}
