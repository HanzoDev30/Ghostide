package ir.hanzodev1375.components.store.notification;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

/**
 * Ties the {@link PluginCheckService} to the app's foreground state: the service (and its
 * notification) only stays alive while at least one activity is on screen. As soon as the last
 * activity leaves the screen the service is stopped on purpose. Fresh-plugin alerts keep working in
 * the background through the alarm scheduled by {@link PluginNotifier#schedule}.
 */
public final class PluginCheckLifecycle implements Application.ActivityLifecycleCallbacks {

  private static PluginCheckLifecycle INSTANCE;

  private Context app;
  private int visibleActivities;

  public static void init(Context context) {
    if (INSTANCE != null) return;
    Context applicationContext = context.getApplicationContext();
    if (!(applicationContext instanceof Application)) return;
    INSTANCE = new PluginCheckLifecycle();
    INSTANCE.app = applicationContext;
    ((Application) applicationContext).registerActivityLifecycleCallbacks(INSTANCE);
  }

  private void refresh() {
    if (app == null) return;
    if (visibleActivities > 0) {
      PluginCheckService.start(app);
    } else {
      PluginCheckService.stop(app);
    }
  }

  @Override
  public void onActivityStarted(Activity activity) {
    visibleActivities++;
    refresh();
  }

  @Override
  public void onActivityStopped(Activity activity) {
    if (visibleActivities > 0) visibleActivities--;
    refresh();
  }

  @Override
  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

  @Override
  public void onActivityResumed(Activity activity) {}

  @Override
  public void onActivityPaused(Activity activity) {}

  @Override
  public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

  @Override
  public void onActivityDestroyed(Activity activity) {}
}
