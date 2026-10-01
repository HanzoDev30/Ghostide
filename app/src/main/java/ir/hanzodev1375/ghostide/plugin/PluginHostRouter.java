package ir.hanzodev1375.ghostide.plugin;

import android.app.Activity;
import android.content.Intent;
import ir.hanzodev1375.ghostide.activity.EditorActivity;
import ir.hanzodev1375.ghostide.ide.api.EditorExtensionPoints;
import ir.hanzodev1375.ghostide.ide.api.LspServerProvider;
import ir.hanzodev1375.ghostide.ide.api.LspServerRequest;
import ir.hanzodev1375.ghostide.plugin.api.GlobalRegistry;
import ir.hanzodev1375.ghostide.utils.FileExtensionUtils;
import java.io.File;

/**
 * Routes the formats a plugin owns, using only what the plugin already registered — no plugin is
 * asked for anything new.
 *
 * <p>A format is whatever a plugin's {@link LspServerProvider#supports(LspServerRequest)} already
 * answers for, so a plugin that claims {@code .js} or {@code .cpp} owns those extensions. Once such
 * a file shows up, the host that owns the route decides where it goes: the editor host opens the
 * editor, and the file manager keeps its own behaviour for extensions it already understands,
 * falling back to the editor only for a format it has no handling of its own.
 */
public final class PluginHostRouter {

  private PluginHostRouter() {}

  /** Whether {@code activity} is a host whose job is editing rather than browsing files. */
  public static boolean isEditorHost(Activity activity) {
    return activity instanceof EditorActivity;
  }

  /**
   * Id of the first plugin claiming {@code file} through one of its formats, or {@code null} when
   * no installed plugin owns the file.
   */
  public static String claimingPlugin(File file) {
    if (file == null) {
      return null;
    }
    File root = file.getParentFile();
    if (root == null) {
      return null;
    }
    LspServerRequest request = new LspServerRequest(root, file);
    for (var registration :
        GlobalRegistry.extensions().registrations(EditorExtensionPoints.LSP_SERVER_PROVIDER)) {
      try {
        if (registration.extension().supports(request)) {
          return registration.ownerPluginId();
        }
      } catch (Throwable ignored) {
      }
    }
    return null;
  }

  /** Whether any installed plugin claims {@code file} through one of its formats. */
  public static boolean isPluginFormatFile(File file) {
    return claimingPlugin(file) != null;
  }

  /** Opens {@code file} in {@link EditorActivity}. */
  public static void openInEditor(Activity activity, File file) {
    Intent intent = new Intent(activity, EditorActivity.class);
    intent.putExtra("file_path", file.getAbsolutePath());
    intent.putExtra("file_name", file.getName());
    activity.startActivity(intent);
  }

  /**
   * Host-aware entry point for a file a plugin owns. Opens {@link EditorActivity} when the editor
   * host owns the file, and lets the caller run its own chain whenever the host is the file manager.
   *
   * @return {@code true} when the file was routed to the editor and the caller must stop
   */
  public static boolean route(Activity activity, String path, String extension) {
    if (activity == null || path == null || path.isEmpty() || isEditorHost(activity)) {
      return false;
    }
    File file = new File(path);
    if (!isPluginFormatFile(file) || FileExtensionUtils.isCodeFile(extension)) {
      return false;
    }
    openInEditor(activity, file);
    return true;
  }
}
