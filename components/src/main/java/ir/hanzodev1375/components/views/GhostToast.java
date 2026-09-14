package ir.hanzodev1375.components.views;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.StringRes;
import com.example.liquidglass.LiquidGlassToast;

/**
 * GhostToast — thin wrapper over the LiquidGlass library's own {@link LiquidGlassToast}.
 *
 * The library toast draws straight into the Activity window, so the glass really refracts
 * the UI underneath it (a system {@link Toast} is a separate window and can never do that).
 * Public API is unchanged; only the implementation swapped.
 */
public final class GhostToast {

  public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
  public static final int LENGTH_LONG = Toast.LENGTH_LONG;

  private static Context appContext;
  private static Activity currentActivity;
  private static boolean showicon = true;
  private static int iconres = 0;

  private final LiquidGlassToast glassToast;
  private final Toast fallbackToast;

  private GhostToast(LiquidGlassToast glassToast, Toast fallbackToast) {
    this.glassToast = glassToast;
    this.fallbackToast = fallbackToast;
  }

  private GhostToast() {
    this.glassToast = null;
    this.fallbackToast = null;
  }

  public static void bindOfApp(Application app) {
    if (app == null) return;
    appContext = app.getApplicationContext();
    app.registerActivityLifecycleCallbacks(
        new Application.ActivityLifecycleCallbacks() {
          @Override
          public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            currentActivity = activity;
          }

          @Override
          public void onActivityStarted(Activity activity) {
            currentActivity = activity;
          }

          @Override
          public void onActivityResumed(Activity activity) {
            currentActivity = activity;
          }

          @Override
          public void onActivityPaused(Activity activity) {
            if (currentActivity == activity) currentActivity = null;
          }

          @Override
          public void onActivityStopped(Activity activity) {
            if (currentActivity == activity) currentActivity = null;
          }

          @Override
          public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

          @Override
          public void onActivityDestroyed(Activity activity) {
            if (currentActivity == activity) currentActivity = null;
          }
        });
  }

  public static GhostToast makeText(CharSequence text) {
    return makeText(text, LENGTH_SHORT);
  }

  public static GhostToast makeText(CharSequence text, int duration) {
    if (currentActivity != null) {
      return makeLiquid(currentActivity, text, duration);
    }
    if (appContext == null) {
      throw new IllegalStateException(
          "GhostToast is not initialized. Call GhostToast.bindOfApp(Application) first.");
    }
    return standard(appContext, text, duration);
  }

  public static GhostToast makeText(Context ctx, CharSequence text) {
    return makeText(ctx, text, LENGTH_SHORT);
  }

  public static GhostToast makeText(Context ctx, CharSequence text, int duration) {
    Activity activity = findActivity(ctx);
    if (activity != null) {
      return makeLiquid(activity, text, duration);
    }
    Context base = resolveBaseContext(ctx);
    return standard(base != null ? base : appContext, text, duration);
  }

  public static GhostToast makeText(Context ctx, @StringRes int text, int duration) {
    if (ctx == null) {
      return makeText((CharSequence) null, duration);
    }
    return makeText(ctx, ctx.getString(text), duration);
  }

  private static GhostToast makeLiquid(Activity activity, CharSequence text, int duration) {
    LiquidGlassToast toast =
        LiquidGlassToast.makeText(activity, text, duration).setText(text);
    if (showicon) {
      if (iconres != 0) {
        toast.setIconResource(iconres);
      } else {
        try {
          Drawable appIcon = activity.getApplicationInfo().loadIcon(activity.getPackageManager());
          toast.setIcon(appIcon);
          toast.setIconTintEnabled(false);
        } catch (Exception ignored) {
          // app icon unavailable: fall through to no icon
        }
      }
    }
    return new GhostToast(toast, null);
  }

  private static GhostToast standard(Context context, CharSequence text, int duration) {
    if (context == null) context = appContext;
    if (context == null) return new GhostToast();
    return new GhostToast(null, Toast.makeText(context, text, duration));
  }

  public GhostToast show() {
    if (glassToast != null) {
      glassToast.show();
    } else if (fallbackToast != null) {
      fallbackToast.show();
    }
    return this;
  }

  public static GhostToast show(CharSequence text) {
    return makeText(text).show();
  }

  public static boolean getShowicon() {
    return showicon;
  }

  public static void setShowicon(boolean showicon) {
    GhostToast.showicon = showicon;
  }

  public static int getIconRes() {
    return iconres;
  }

  public static void setIconRes(int iconres) {
    GhostToast.iconres = iconres;
  }

  private static Activity findActivity(Context context) {
    Context c = context;
    while (c != null) {
      if (c instanceof Activity) return (Activity) c;
      if (c instanceof ContextWrapper) {
        c = ((ContextWrapper) c).getBaseContext();
      } else {
        return null;
      }
    }
    return null;
  }

  private static Context resolveBaseContext(Context context) {
    Context c = context;
    while (c instanceof ContextWrapper) {
      c = ((ContextWrapper) c).getBaseContext();
    }
    return c;
  }
}