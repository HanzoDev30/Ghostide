package ir.hanzodev1375.components.glass;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import com.example.liquidglass.LiquidGlassChipGroup;
import com.example.liquidglass.LiquidGlassView;
import ir.hanzodev1375.components.colors.AccentPalette;
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

  static {
    // Hands the glass widgets to M3Theme's traversal so they are re-themed with everything else on
    // every theme change. M3Theme cannot import them (that would be a module cycle), hence the hook.
    M3Theme.setGlassThemeApplier(
        new M3Theme.GlassThemeApplier() {
          @Override
          public boolean applyGlassView(View v) {
            return applyGlassChip(v) || applyGlassButton(v);
          }

          @Override
          public boolean applyGlassChip(View v) {
            if (!(v instanceof ChipCompat)) {
              return false;
            }
            applyChip((ChipCompat) v);
            return true;
          }

          @Override
          public boolean applyGlassButton(View v) {
            if (!(v instanceof ButtonCompat)) {
              return false;
            }
            applyButton((ButtonCompat) v);
            return true;
          }
        });
  }

  /**
   * Applies a chip's material, then the accent of the tone it was given.
   *
   * <p>Tones are the only place a gold/green accent enters the app: callers just ask for {@link
   * ChipCompat.Tone#GOLD} and the colour is resolved from the active theme, so nothing has to be
   * hard-coded per screen and light/dark stay in sync.
   */
  public static void applyChip(ChipCompat chip) {
    chip.setChipIconTintEnabled(chip.getTone() == ChipCompat.Tone.DEFAULT);
    applyThemeTint(chip);
    applyChipTone(chip);
  }

  /**
   * Applies a glass button's material and adaptive foreground.
   *
   * <p>An {@code ERROR} tone overrides the plain surface with the theme's error colour. The tint is
   * applied as an opaque colour and left to the library to blend, so a theme that lowers the glass
   * tint strength cannot wash the destructive action out.
   */
  public static void applyButton(ButtonCompat button) {
    button.setEnableAdaptiveTint(true);
    if (button.getTone() == ButtonCompat.Tone.ERROR) {
      applyErrorTint(button);
      return;
    }
    applyThemeTint(button);
  }

  private static void applyErrorTint(ButtonCompat button) {
    Context context = button.getContext();
    Integer error = M3Theme.error();
    if (context == null || error == null) {
      applyThemeTint(button);
      return;
    }
    button.setGlassTint(error);
    if (!new ComponentsPrefs(context).isGlassMaterialColor()) {
      // Without the surface tint the label would sit on a plain error-coloured panel, so it needs
      // the error container's foreground to stay readable.
      Integer onError = M3Theme.onError();
      if (onError != null) {
        button.setTextColor(onError);
      }
    }
  }

  private static void applyChipTone(ChipCompat chip) {
    float strength = toneTintStrength(chip);
    switch (chip.getTone()) {
      case GOLD:
        chip.setTextColor(gold());
        chip.setGlassTint(gold(), strength);
        // The library's adaptive icon tint would repaint the star with the plain foreground, so it
        // is turned off and the accent is baked into the drawable instead.
        applyIconTint(chip, gold());
        break;
      case GREEN:
        chip.setTextColor(green());
        chip.setGlassTint(green(), strength);
        break;
      case DEFAULT:
      default:
        Integer onSurface = M3Theme.onSurface();
        if (onSurface != null) {
          chip.setTextColor(onSurface);
        }
        break;
    }
  }

  private static void applyIconTint(ChipCompat chip, @ColorInt int color) {
    int iconRes = chip.getIconRes();
    if (iconRes == 0) {
      return;
    }
    Drawable drawable = AppCompatResources.getDrawable(chip.getContext(), iconRes);
    if (drawable == null) {
      return;
    }
    DrawableCompat.setTintList(drawable, ColorStateList.valueOf(color));
    chip.setChipIcon(drawable);
  }

  /** Amber accent, brightened for dark themes. */
  public static int gold() {
    return isDarkTheme() ? 0xFFFFD54F : 0xFFB8860B;
  }

  /** Green accent, lightened for dark themes. */
  public static int green() {
    return isDarkTheme() ? 0xFF81C784 : 0xFF2E7D32;
  }

  /**
   * Accent tones are weaker than the plain surface tint on purpose: the colour should tint the
   * glass, not replace it, so the backdrop still shows through.
   */
  private static float toneTintStrength(View view) {
    Context context = view.getContext();
    float strength = context == null ? 0.35f : new ComponentsPrefs(context).getGlassTint();
    return Math.min(strength, 0.35f);
  }

  private static boolean isDarkTheme() {
    Integer surface = M3Theme.surfaceContainer();
    if (surface == null) {
      Integer background = M3Theme.background();
      surface = background;
    }
    if (surface == null) {
      return false;
    }
    return AccentPalette.perceivedBrightness(surface) < 0.5f;
  }

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
