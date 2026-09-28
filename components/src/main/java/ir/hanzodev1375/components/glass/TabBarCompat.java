package ir.hanzodev1375.components.glass;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import com.example.liquidglass.GlassMaterial;
import com.example.liquidglass.LiquidGlassTabBar;

/**
 * {@link LiquidGlassTabBar} that wires its own backdrop and keeps the bar a clean pill.
 *
 * <p>Two things a hand-rolled tab bar gets wrong:
 *
 * <ul>
 *   <li>The bar lives in a separate window, so it needs the host Activity's content view as its
 *       backdrop instead of its (transparent) parent.
 *   <li>Writing system insets into the bar's padding makes the panel far taller than its
 *       content, and a pill's curve scales with the panel's height — so the ends curve so deeply
 *       that the first and last tab appear to poke out through the corner. The inset belongs on
 *       the margin, and the content needs an inset of its own so the tabs stay clear of the
 *       curve.
 * </ul>
 */
public class TabBarCompat extends LiquidGlassTabBar {

  /** Corner radius that renders as a full pill, whatever the bar's height. */
  private static final float PILL_CORNER_RADIUS = 999f;

  private static final float MIN_END_PADDING_DP = 8f;

  private float endPaddingDp = -1f;
  private int baseBottomMargin = -1;

  public TabBarCompat(Context context) {
    this(context, null, 0);
  }

  public TabBarCompat(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public TabBarCompat(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    // A full pill: the curve is what makes a floating bar read as glass rather than a slab.
    setCornerRadius(PILL_CORNER_RADIUS);
    setMaterial(GlassMaterial.REGULAR);
    setEnableSensorHighlight(true);
    setEnableAdaptiveTint(true);
    Glass.applyThemeTint(this);
    applyEndPadding();
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    Glass.autoBackdrop(this);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    applyEndPadding();
  }

  /**
   * Sets how far the first and last tab stay from the ends of the bar. Defaults to half the
   * corner radius, so a rounder bar insets its tabs further.
   */
  public void setEndPaddingDp(float value) {
    endPaddingDp = Math.max(MIN_END_PADDING_DP, value);
    applyEndPadding();
  }

  /**
   * Keeps the system navigation bar away from the glass panel.
   *
   * <p>Applied to the bottom margin, not the padding: padding would grow the panel, and a pill's
   * curve scales with the panel's height, so the ends would eat the outer tabs.
   *
   * <p>Insets can be dispatched more than once (rotation, keyboard, multi-window), so the
   * requested amount is remembered and the margin recomputed from its original value rather
   * than grown on every pass.
   */
  public void applySystemBottomInset(int inset) {
    if (!(getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
      return;
    }
    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) getLayoutParams();
    if (baseBottomMargin < 0) {
      baseBottomMargin = params.bottomMargin;
    }

    params.bottomMargin = baseBottomMargin + Math.max(0, inset);
    setLayoutParams(params);
  }

  private void applyEndPadding() {
    if (endPaddingDp >= 0f) {
      int padding = Glass.dp(this, endPaddingDp);
      setPadding(padding, getPaddingTop(), padding, getPaddingBottom());
      return;
    }
    float radius = getCornerRadius();
    // A pill reports 999f; the curve it actually draws is half the bar's height.
    float effectiveRadius = radius >= PILL_CORNER_RADIUS ? getHeight() / 2f : radius;
    int padding = Glass.dp(this, Math.max(MIN_END_PADDING_DP, effectiveRadius * 0.5f));
    if (getPaddingLeft() < padding) {
      setPadding(padding, getPaddingTop(), padding, getPaddingBottom());
    }
  }
}
