package ir.hanzodev1375.ghostide.plugin;

import android.content.Context;
import android.view.View;
import ir.hanzodev1375.ghostide.codeeditors.IdeEditor;
import ir.hanzodev1375.ghostide.activity.EditorActivity;
import ir.hanzodev1375.ghostide.activity.FileManagerActivity;

public interface Plugin {
  String getId();

  String getName();

  String getVersion();

  String getSupportedFileTypes();

  void onLoad(PluginContext context);

  void onUnload();

  default void setEditorActivity(EditorActivity activity) {}

  default void setFileManagerActivity(FileManagerActivity activity) {}

  default void onEditorReady(IdeEditor editor) {}

  default void onFileOpened(String path, String content) {}

  default View getSettingsView(Context context) {
    return null;
  }

  default void onFileSaved(String path, String content) {}

  default boolean hasUI() {
    return false;
  }
}
