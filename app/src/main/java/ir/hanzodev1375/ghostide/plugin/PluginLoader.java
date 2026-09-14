package ir.hanzodev1375.ghostide.plugin;

import android.content.Context;
import dalvik.system.DexClassLoader;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginLoader {
  private final Context context;
  private final Map<String, Plugin> loadedPlugins = new HashMap<>();

  public PluginLoader(Context context) {
    this.context = context.getApplicationContext();
  }

  public Plugin loadPlugin(PluginConfigModel config) throws Exception {
    if (!config.isEnabled) return null;
    if (loadedPlugins.containsKey(config.id)) return loadedPlugins.get(config.id);
    File apkFile = new File(config.apkFile);
    if (!apkFile.exists()) throw new Exception("APK not found: " + config.apkFile);
    File optDir = new File(context.getCodeCacheDir(), "plugin_opt");
    optDir.mkdirs();
    DexClassLoader classLoader =
        new DexClassLoader(
            apkFile.getAbsolutePath(), optDir.getAbsolutePath(), null, context.getClassLoader());
    Class<?> pluginClass = classLoader.loadClass(config.mainClass);
    Object instance = pluginClass.newInstance();
    if (!(instance instanceof Plugin)) {
      throw new Exception("Class does not implement Plugin");
    }
    Plugin plugin = (Plugin) instance;
    PluginContext pluginContext = new PluginContext(context, config.id);
    plugin.onLoad(pluginContext);
    loadedPlugins.put(config.id, plugin);
    return plugin;
  }

  public Plugin getPlugin(String id) {
    return loadedPlugins.get(id);
  }

  public Map<String, Plugin> getAllPlugins() {
    return new HashMap<>(loadedPlugins);
  }

  public void unloadPlugin(String id) {
    Plugin p = loadedPlugins.remove(id);
    if (p != null) p.onUnload();
  }
}
