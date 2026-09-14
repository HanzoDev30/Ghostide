package ir.hanzodev1375.ghostide.plugin;

import android.content.Context;
import ir.hanzodev1375.ghostide.codeeditors.IdeEditor;
import ir.hanzodev1375.ghostide.activity.EditorActivity;
import ir.hanzodev1375.ghostide.activity.FileManagerActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginManager {
  private static PluginManager instance;
  private PluginLoader loader;
  private final Map<String, PluginConfigModel> configs = new HashMap<>();
  private EditorActivity currentEditorActivity;
  private FileManagerActivity currentFileManagerActivity;

  private PluginManager(Context context) {
    loader = new PluginLoader(context);
  }

  public static void init(Context context) {
    if (instance == null) instance = new PluginManager(context);
  }

  public static PluginManager getInstance() {
    return instance;
  }

  public void loadPluginsFromConfig(String configPath) {
    List<PluginConfigModel> configList = PluginConfigModel.loadFromFile(configPath);
    for (PluginConfigModel cfg : configList) {
      configs.put(cfg.id, cfg);
      try {
        loader.loadPlugin(cfg);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void setCurrentEditorActivity(
      EditorActivity activity, IdeEditor editor, String filePath, String fileExtension) {
    this.currentEditorActivity = activity;
    for (Plugin p : getPluginsForFileType(fileExtension)) {
      p.setEditorActivity(activity);
      p.onEditorReady(editor);
      if (filePath != null) {
        p.onFileOpened(filePath, editor.getText().toString());
      }
    }
  }

  public void setCurrentFileManagerActivity(FileManagerActivity activity) {
    if (activity == null) return;
    for (Plugin p : loader.getAllPlugins().values()) {
      try {
        p.setFileManagerActivity(activity);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void notifyFileSaved(String filePath, String fileExtension, String content) {
    for (Plugin p : getPluginsForFileType(fileExtension)) {
      p.onFileSaved(filePath, content);
    }
  }

  private List<Plugin> getPluginsForFileType(String extension) {
    List<Plugin> result = new ArrayList<>();
    for (Map.Entry<String, Plugin> entry : loader.getAllPlugins().entrySet()) {
      PluginConfigModel cfg = configs.get(entry.getKey());
      if (cfg != null && cfg.isEnabled && cfg.supportedFileTypes.contains(extension)) {
        result.add(entry.getValue());
      }
    }
    return result;
  }

  public Map<String, Plugin> getAllPlugins() {
    return loader.getAllPlugins();
  }

  public PluginConfigModel getPluginConfig(String id) {
    return configs.get(id);
  }
}
