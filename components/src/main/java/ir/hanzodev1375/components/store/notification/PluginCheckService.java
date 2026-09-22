package ir.hanzodev1375.components.store.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import ir.hanzodev1375.components.R;
import java.util.concurrent.TimeUnit;

/**
 * Always-on foreground service that reliably keeps checking the plugin store in the background
 * even when the app is killed. It runs the same dedup logic as {@link PluginNotifier}.
 */
public class PluginCheckService extends Service {

  private static final String TAG = "PluginCheckService";
  private static final String FOREGROUND_CHANNEL = "plugin_check_foreground";
  private static final int FOREGROUND_ID = 9001;
  private final Handler handler = new Handler(Looper.getMainLooper());

  private final Runnable checkTask =
      new Runnable() {
        @Override
        public void run() {
          try {
            PluginNotifier.runCheck(PluginCheckService.this, null);
          } finally {
            if (!handler.postDelayed(this, TimeUnit.HOURS.toMillis(6))) {
              Log.w(TAG, "could not reschedule check");
            }
          }
        }
      };

  public static void start(Context context) {
    if (context == null) return;
    Context app = context.getApplicationContext();
    Intent intent = new Intent(app, PluginCheckService.class);
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        app.startForegroundService(intent);
      } else {
        app.startService(intent);
      }
    } catch (Exception e) {
      Log.w(TAG, "could not start service, alarm fallback will cover checks", e);
    }
  }

  /** Stop the service and drop its foreground notification. Called when the app leaves the screen. */
  public static void stop(Context context) {
    if (context == null) return;
    try {
      context.getApplicationContext().stopService(new Intent(context, PluginCheckService.class));
    } catch (Exception e) {
      Log.w(TAG, "could not stop service", e);
    }
  }

  @Override
  public void onCreate() {
    super.onCreate();
    createForegroundChannel();
    startForeground(FOREGROUND_ID, buildForegroundNotification());
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    handler.removeCallbacks(checkTask);
    handler.post(checkTask);
    return START_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onDestroy() {
    handler.removeCallbacks(checkTask);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      stopForeground(STOP_FOREGROUND_REMOVE);
    } else {
      stopForeground(true);
    }
    super.onDestroy();
  }

  private Notification buildForegroundNotification() {
    Intent open = PluginNotifier.openStore(this);
    int flags =
        PendingIntent.FLAG_UPDATE_CURRENT
            | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0);
    PendingIntent contentIntent = PendingIntent.getActivity(this, 1, open, flags);

    return new NotificationCompat.Builder(this, FOREGROUND_CHANNEL)
        .setSmallIcon(R.drawable.ic_outline_extension)
        .setContentTitle(getString(R.string.plugin_check_foreground_title))
        .setContentText(getString(R.string.plugin_check_foreground_text))
        .setOngoing(true)
        .setContentIntent(contentIntent)
        .build();
  }

  private void createForegroundChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationManager nm = getSystemService(NotificationManager.class);
      if (nm != null && nm.getNotificationChannel(FOREGROUND_CHANNEL) == null) {
        NotificationChannel channel =
            new NotificationChannel(
                FOREGROUND_CHANNEL,
                getString(R.string.plugin_check_foreground_channel),
                NotificationManager.IMPORTANCE_LOW);
        nm.createNotificationChannel(channel);
      }
    }
  }
}