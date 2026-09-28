package ir.hanzodev1375.components.glass;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.View;
import androidx.annotation.Nullable;
import com.example.liquidglass.LiquidGlassChipGroup;
import com.example.liquidglass.LiquidGlassView;
import ir.hanzodev1375.components.utils.ComponentsPrefs;
import ir.theme.M3Theme;

/**
 * Shared plumbing for the Liquid Glass compat views in this package.
 *
 * <p>Glass widgets sample whatever is painted behind them. Inside an Activity the default
 * (the direct parent) is enough, but in a Dialog / BottomSheetDialog the widget lives in its
 * own window, so the parent is transparent and the capture comes back empty. Every one of
 * those widgets has to be pointed at {@code android.R.id.content} of the host Activity by
 * hand, which is exactly the boilerplate this class removes.
 */
public final class Glass {

  private Glass() {}

  /** Unwraps {@code context} until the hosting {@link Activity} is found, or null. */
  @Nullable
  public static Activity findActivity(@Nullable Context context) {
    Context current = context;
    while (current instanceof ContextWrapper) {
      if (current instanceof Activity) {
        return (Activity) current;
      }
      current = ((ContextWrapper) current).getBaseContext();
      if (current == null) {
        return null;
      }
    }
    return null;
  }

  /**
   * The view a glass widget should capture: the host Activity's content view.
   *
   * <p>Always resolved through an explicit {@link Activity} reference on purpose. Inside a
   * receiver-like scope such as {@code view.apply { ... }} the bare {@code findViewById} call
   * binds to {@link View#findViewById} and silently yields null.
   */
  @Nullable
  public static View resolveBackdrop(@Nullable Context context) {
    Activity activity = findActivity(context);
    return activity == null ? null : activity.findViewById(android.R.id.content);
  }

  /**
   * Points {@code glass} at the host Activity's content view and keeps the backdrop live.
   *
   * @return true when a backdrop was found and applied.
   */
  public static boolean autoBackdrop(LiquidGlassView glass) {
    View source = resolveBackdrop(glass.getContext());
    if (source == null) {
      return false;
    }
    glass.setBackdropSource(source);
    glass.setEnableDynamicBackground(true);
    return true;
  }

  /**
   * Same as {@link #autoBackdrop(LiquidGlassView)} for a chip group, which forwards the source
   * to every chip it holds (including chips added later).
   */
  public static boolean autoBackdrop(LiquidGlassChipGroup group) {
    View source = resolveBackdrop(group.getContext());
    if (source == null) {
      return false;
    }
    group.setBackdropSource(source);
    group.setEnableDynamicBackground(true);
    return true;
  }

  /**
   * Applies the user's glass material colour (M3 surface tinted at the configured strength) so
   * compat widgets stay consistent with the rest of the app's glass surfaces.
   */
  public static void applyThemeTint(LiquidGlassView glass) {
    Context context = glass.getContext();
    if (context == null) {
      return;
    }
    ComponentsPrefs prefs = new ComponentsPrefs(context);
    if (!prefs.isGlassMaterialColor()) {
      return;
    }
    Integer surface = M3Theme.surface();
    if (surface != null) {
      glass.setGlassTint(surface, prefs.getGlassTint());
    }
  }

  /** Density independent pixels for {@code view}. */
  public static int dp(View view, float value) {
    return Math.round(value * view.getResources().getDisplayMetrics().density);
  }
}
