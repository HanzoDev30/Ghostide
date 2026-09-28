package ir.hanzodev1375.components.glass;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import com.example.liquidglass.GlassMaterial;
import com.example.liquidglass.LiquidGlassChip;
import java.util.Locale;

/**
 * {@link LiquidGlassChip} that wires its own backdrop, so chips look right inside dialogs and
 * bottom sheets without any manual {@code setBackdropSource(...)} call.
 */
public class ChipCompat extends LiquidGlassChip {

  public ChipCompat(Context context) {
    super(context);
    init();
  }

  public ChipCompat(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public ChipCompat(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    setEnableDynamicBackground(true);
    setMaterial(GlassMaterial.REGULAR);
    setEnableSensorHighlight(true);
    setEnableAdaptiveTint(true);
    setPressScale(0.96f);
    setTextSize(13f);
    setChipIconTintEnabled(true);
    Glass.applyThemeTint(this);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    Glass.autoBackdrop(this);
  }

  /** Convenience alias for {@code setText}. */
  public void setLabel(CharSequence text) {
    setText(text);
  }

  /**
   * Renders a leading icon. Any tint baked into the drawable is cleared so it follows the
   * chip's adaptive light/dark foreground instead of fighting it.
   */
  public void setIconResource(int iconRes) {
    if (iconRes == 0) {
      setChipIcon(null);
      return;
    }
    Drawable drawable = AppCompatResources.getDrawable(getContext(), iconRes);
    if (drawable == null) {
      return;
    }
    DrawableCompat.setTintList(drawable, null);
    setChipIcon(drawable);
  }

  /** Icon plus a compact label, e.g. a star count or an issue count. */
  public void setStat(int iconRes, long value) {
    setIconResource(iconRes);
    setText(formatStat(value));
  }

  /** 1234 becomes "1,234" and anything above 9999 becomes "12.3k". */
  public static String formatStat(long value) {
    if (value < 0) {
      return "-";
    }
    if (value < 10_000) {
      return String.format(Locale.US, "%,d", value);
    }
    return String.format(Locale.US, "%.1fk", value / 1000f);
  }
}
