package ir.hanzodev1375.components.store.notification;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import ir.hanzodev1375.components.R;
import ir.hanzodev1375.components.store.api.PluginStoreApi;
import ir.hanzodev1375.components.store.model.PluginItem;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PluginNotifier {

  public static final String ACTION_CHECK = "ir.hanzodev1375.components.store.action.PLUGIN_CHECK";
  public static final String ACTION_BOOT = "ir.hanzodev1375.components.store.action.PLUGIN_BOOT";

  private static final String TAG = "PluginNotifier";
  private static final String PREFS = "ghostide_plugin_notifier";
  private static final String KEY_SEEN = "seen_names";
  private static final String KEY_BASELINE = "baseline_done";

  private static final String CHANNEL_ID = "new_plugins";
  private static final long CHECK_INTERVAL_MS = TimeUnit.HOURS.toMillis(6);
  private static final long IN_APP_COOLDOWN_MS = TimeUnit.HOURS.toMillis(24);
  private static final String KEY_LAST_IN_APP_CHECK = "last_in_app_check";
  private static final String STORE_ACTIVITY =
      "ir.hanzodev1375.ghostide.activity.StoreActivity";

  private static final OkHttpClient ICON_CLIENT =
      new OkHttpClient.Builder()
          .connectTimeout(10, TimeUnit.SECONDS)
          .readTimeout(20, TimeUnit.SECONDS)
          .build();

  private PluginNotifier() {}

  /** (Re-)arm the repeating background check. Safe to call on every app launch. */
  public static void schedule(Context context) {
    if (context == null) return;
    Context app = context.getApplicationContext();
    AlarmManager am = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
    if (am == null) return;
    PendingIntent pi = checkPendingIntent(app, PendingIntent.FLAG_UPDATE_CURRENT);
    long trigger = SystemClock.elapsedRealtime() + CHECK_INTERVAL_MS;
    am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, trigger, CHECK_INTERVAL_MS, pi);
  }

  /** Run the check when the app is opened. Limited to once per day; silent if no new plugin. */
  public static void checkNow(Context context) {
    if (context == null) return;
    final Context app = context.getApplicationContext();
    SharedPreferences prefs = prefs(app);
    long now = System.currentTimeMillis();
    if (now - prefs.getLong(KEY_LAST_IN_APP_CHECK, 0L) < IN_APP_COOLDOWN_MS) {
      return;
    }
    prefs.edit().putLong(KEY_LAST_IN_APP_CHECK, now).apply();
    runCheck(app, null);
  }

  /**
   * Fetch the store list, detect plugins we have not seen before and notify the user with the
   * plugin icon. The very first run only records the baseline so the user is not spammed with
   * every existing plugin.
   */
  public static void runCheck(final Context context, final Runnable onDone) {
    final Context app = context.getApplicationContext();
    if (!isOnline(app)) {
      finish(onDone);
      return;
    }
    PluginStoreApi.fetchPlugins(
        new PluginStoreApi.ListCallback() {
          @Override
          public void onSuccess(List<PluginItem> plugins) {
            try {
              if (plugins != null) {
                handlePlugins(app, plugins);
              }
            } catch (Exception e) {
              Log.e(TAG, "check failed", e);
            } finally {
              finish(onDone);
            }
          }

          @Override
          public void onError(String message) {
            Log.w(TAG, "fetch failed: " + message);
            finish(onDone);
          }
        });
  }

  private static void handlePlugins(Context app, List<PluginItem> plugins) {
    SharedPreferences prefs = prefs(app);
    Set<String> seen = new HashSet<>(prefs.getStringSet(KEY_SEEN, new HashSet<>()));
    boolean baseline = prefs.getBoolean(KEY_BASELINE, false);

    List<PluginItem> fresh = new ArrayList<>();
    for (PluginItem item : plugins) {
      if (item == null || item.name() == null || item.name().trim().isEmpty()) continue;
      if (seen.add(item.name())) {
        fresh.add(item);
      }
    }

    prefs.edit().putStringSet(KEY_SEEN, seen).putBoolean(KEY_BASELINE, true).apply();

    if (baseline) {
      for (PluginItem item : fresh) {
        showNotification(app, item);
      }
    }
  }

  private static void showNotification(Context app, PluginItem item) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (app.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
          != PackageManager.PERMISSION_GRANTED) {
        return;
      }
    }
    NotificationManager nm = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
    if (nm == null || !nm.areNotificationsEnabled()) return;

    createChannel(app);
    Bitmap icon = downloadIcon(item.icon());
    Notification notification = buildNotification(app, item, icon);
    nm.notify(item.name().hashCode(), notification);
  }

  private static Notification buildNotification(Context app, PluginItem item, Bitmap icon) {
    Intent open = openStoreIntent(app);
    int id = item.name().hashCode();
    int flags =
        PendingIntent.FLAG_UPDATE_CURRENT
            | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);
    PendingIntent contentIntent = PendingIntent.getActivity(app, id, open, flags);

    String text = app.getString(R.string.plugin_notify_text, item.name());
    NotificationCompat.Builder builder =
        new NotificationCompat.Builder(app, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_outline_extension)
            .setContentTitle(app.getString(R.string.plugin_notify_title))
            .setContentText(text)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .setContentIntent(contentIntent);
    if (icon != null) {
      builder.setLargeIcon(icon);
    }
    return builder.build();
  }

  /** Nice-to-have: keep the store icon visible in the always-on service notification. */
  public static Intent openStore(Context app) {
    return openStoreIntent(app);
  }

  private static Intent openStoreIntent(Context app) {
    Intent store = new Intent().setClassName(app.getPackageName(), STORE_ACTIVITY);
    store.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    if (store.resolveActivity(app.getPackageManager()) == null) {
      Intent launcher = app.getPackageManager().getLaunchIntentForPackage(app.getPackageName());
      if (launcher != null) {
        launcher.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return launcher;
      }
    }
    return store;
  }

  private static Bitmap downloadIcon(String url) {
    if (url == null || url.trim().isEmpty()) return null;
    try {
      Request request = new Request.Builder().url(url).get().build();
      try (Response response = ICON_CLIENT.newCall(request).execute()) {
        if (!response.isSuccessful() || response.body() == null) return null;
        InputStream in = response.body().byteStream();
        return BitmapFactory.decodeStream(in);
      } catch (IOException e) {
        Log.w(TAG, "icon download failed for " + url);
        return null;
      }
    } catch (Exception e) {
      Log.w(TAG, "icon decode failed for " + url, e);
      return null;
    }
  }

  private static void createChannel(Context app) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationManager nm = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
      if (nm != null && nm.getNotificationChannel(CHANNEL_ID) == null) {
        NotificationChannel channel =
            new NotificationChannel(
                CHANNEL_ID,
                app.getString(R.string.plugin_notify_channel),
                NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(app.getString(R.string.plugin_notify_channel_desc));
        nm.createNotificationChannel(channel);
      }
    }
  }

  private static PendingIntent checkPendingIntent(Context app, int extraFlags) {
    Intent intent = new Intent(app, PluginCheckReceiver.class).setAction(ACTION_CHECK);
    int flags =
        extraFlags
            | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);
    return PendingIntent.getBroadcast(app, 0, intent, flags);
  }

  private static boolean isOnline(Context app) {
    ConnectivityManager cm = (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (cm == null) return false;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      Network network = cm.getActiveNetwork();
      if (network == null) return false;
      NetworkCapabilities caps = cm.getNetworkCapabilities(network);
      return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }
    NetworkInfo info = cm.getActiveNetworkInfo();
    return info != null && info.isConnected();
  }

  private static SharedPreferences prefs(Context app) {
    return app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
  }

  private static void finish(Runnable onDone) {
    if (onDone != null) onDone.run();
  }
}