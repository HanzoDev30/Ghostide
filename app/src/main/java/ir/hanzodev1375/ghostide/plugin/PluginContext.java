package ir.hanzodev1375.ghostide.plugin;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.io.File;

public class PluginContext {
  private final Context hostContext;
  private final String pluginId;
  private final File pluginDataDir;

  public PluginContext(Context hostContext, String pluginId) {
    this.hostContext = hostContext.getApplicationContext();
    this.pluginId = pluginId;
    this.pluginDataDir = new File(this.hostContext.getFilesDir(), "plugins_data/" + pluginId);
    pluginDataDir.mkdirs();
  }

  public Context getHostContext() {
    return hostContext;
  }

  public File getPluginDataDir() {
    return pluginDataDir;
  }

  public SharedPreferences getPrefs() {
    return hostContext.getSharedPreferences("plugin_" + pluginId, Context.MODE_PRIVATE);
  }

  public void log(String message) {
    Log.d("Plugin-" + pluginId, message);
  }
}
