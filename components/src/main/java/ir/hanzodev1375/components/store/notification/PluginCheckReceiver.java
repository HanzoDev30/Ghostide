package ir.hanzodev1375.components.store.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PluginCheckReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent == null) return;
    String action = intent.getAction();
    if (PluginNotifier.ACTION_BOOT.equals(action)) {
      PluginNotifier.schedule(context);
    } else if (PluginNotifier.ACTION_CHECK.equals(action)) {
      final PendingResult result = goAsync();
      PluginNotifier.runCheck(
          context,
          () -> {
            if (result != null) result.finish();
          });
    }
  }
}
