package ir.hanzodev1375.ghostide.plugin;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ir.hanzodev1375.components.sheet.PluginSetupSheet;
import ir.hanzodev1375.components.store.model.PluginSetupActionData;
import ir.hanzodev1375.components.views.GhostToast;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.activity.PluginScreenActivity;
import ir.hanzodev1375.ghostide.adapters.PluginPopupAdapter;
import ir.hanzodev1375.ghostide.ide.ui.api.EditorPanel;
import ir.hanzodev1375.ghostide.ide.ui.api.PluginScreen;
import ir.hanzodev1375.ghostide.ide.ui.api.PluginUiExtensionPoints;
import ir.hanzodev1375.ghostide.plugin.api.GlobalRegistry;
import ir.hanzodev1375.ghostide.plugin.api.PluginSetupAction;
import ir.hanzodev1375.ghostide.plugin.gpl.GplPluginLoader;
import ir.hanzodev1375.ghostide.plugin.gpl.LoadedGplPlugin;
import ir.hanzodev1375.ghostide.terminal.activity.TerminalActivity;

/**
 * Runs the right thing when an installed plugin is tapped in the plugin popup:
 *
 * <ul>
 *   <li>editor plugins ({@code EDITOR_PANEL}) → open the editor panel;</li>
 *   <li>whole-screen / file-manager plugins ({@code PLUGIN_SCREEN}) → open the plugin screen;</li>
 *   <li>LSP plugins ({@code LSP_SERVER_PROVIDER}) → run their setup actions in the terminal;</li>
 *   <li>anything else → show the setup bottom sheet.</li>
 * </ul>
 */
public final class PluginPopupDispatcher {

  private static final String TAG = "PluginPopupDispatcher";

  private PluginPopupDispatcher() {}

  public static void dispatch(
      Activity activity, PluginPopupAdapter.PluginItem item, PluginPanelHost panelHost) {
    if (activity == null || item == null) {
      return;
    }
    String ownerId = item.manifest() != null ? item.manifest().id() : item.id();

    List<EditorPanel> ownerPanels = PluginPopupAdapter.panelsOf(ownerId);
    if (!ownerPanels.isEmpty() && panelHost != null) {
      panelHost.showPanel(ownerPanels.get(0));
      return;
    }

    List<PluginScreen> ownerScreens = PluginPopupAdapter.screensOf(ownerId);
    if (!ownerScreens.isEmpty()) {
      activity.startActivity(
          PluginScreenActivity.createIntent(activity, ownerScreens.get(0).getId()));
      return;
    }

    if (PluginPopupAdapter.isLsp(ownerId)) {
      runSetup(activity, item);
      return;
    }

    if (PluginPopupAdapter.isEditor(ownerId) && panelHost != null) {
      EditorPanel panel = findPanel(item, ownerId);
      if (panel != null) {
        panelHost.showPanel(panel);
        return;
      }
    }

    PluginScreen screen = findScreen(item, ownerId);
    if (screen != null) {
      activity.startActivity(PluginScreenActivity.createIntent(activity, screen.getId()));
      return;
    }

    showSetupSheet(activity, item);
  }

  /** Runs an LSP plugin's setup actions (e.g. "install clangd") directly in the terminal. */
  private static void runSetup(Activity activity, PluginPopupAdapter.PluginItem item) {
    if (item == null) {
      return;
    }
    try {
      GplPluginLoader loader = GplPluginLoader.getInstance(activity);
      LoadedGplPlugin loaded = loader.load(item.gplFile());
      List<PluginSetupAction> actions = loaded.getPlugin().getSetupActions();
      if (actions == null) {
        actions = List.of();
      }
      StringBuilder combined = new StringBuilder();
      for (int i = 0; i < actions.size(); i++) {
        String command = actions.get(i).command();
        if (command == null || command.trim().isEmpty()) {
          continue;
        }
        if (combined.length() > 0) {
          combined.append(" && ");
        }
        combined.append(command);
      }
      if (combined.length() == 0) {
        GhostToast.makeText(activity, R.string.plugin_manager_no_setup, GhostToast.LENGTH_SHORT)
            .show();
        return;
      }
      Intent intent = new Intent(activity, TerminalActivity.class);
      intent.putExtra(TerminalActivity.EXTRA_COMMAND, combined.toString());
      activity.startActivity(intent);
    } catch (Exception e) {
      Log.e(TAG, "runSetup failed", e);
      GhostToast.makeText(
              activity,
              activity.getString(
                  R.string.plugin_manager_install_error,
                  e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()),
              GhostToast.LENGTH_LONG)
          .show();
    }
  }

  private static EditorPanel findPanel(PluginPopupAdapter.PluginItem item, String ownerId) {
    for (var reg : GlobalRegistry.extensions().registrations(PluginUiExtensionPoints.EDITOR_PANEL)) {
      EditorPanel panel = (EditorPanel) reg.extension();
      if (ownerId.equals(reg.ownerPluginId())
          || ownerId.equals(panel.getId())
          || (item.id() != null && item.id().equals(panel.getId()))) {
        return panel;
      }
    }
    return null;
  }

  private static PluginScreen findScreen(PluginPopupAdapter.PluginItem item, String ownerId) {
    for (var reg :
        GlobalRegistry.extensions().registrations(PluginUiExtensionPoints.PLUGIN_SCREEN)) {
      PluginScreen screen = (PluginScreen) reg.extension();
      if (ownerId.equals(reg.ownerPluginId())
          || ownerId.equals(screen.getId())
          || (item.id() != null && item.id().equals(screen.getId()))) {
        return screen;
      }
    }
    return null;
  }

  private static void showSetupSheet(Activity activity, PluginPopupAdapter.PluginItem item) {
    if (item == null || item.gplFile() == null) {
      return;
    }
    try {
      GplPluginLoader loader = GplPluginLoader.getInstance(activity);
      LoadedGplPlugin loaded = loader.load(item.gplFile());
      List<PluginSetupAction> setupActions = loaded.getPlugin().getSetupActions();
      if (setupActions == null) {
        setupActions = List.of();
      }
      List<PluginSetupActionData> data = new ArrayList<>();
      for (PluginSetupAction action : setupActions) {
        data.add(
            new PluginSetupActionData(
                action.id(), action.label(), action.command(), action.description()));
      }
      if (data.isEmpty()) {
        GhostToast.makeText(
                activity, R.string.plugin_manager_no_setup, GhostToast.LENGTH_SHORT)
            .show();
        return;
      }
      String name = item.manifest() != null ? item.manifest().name() : item.name();
      new PluginSetupSheet(
              activity,
              name,
              null,
              data,
              command -> {
                if (command == null || command.trim().isEmpty()) {
                  return;
                }
                Intent intent = new Intent(activity, TerminalActivity.class);
                intent.putExtra(TerminalActivity.EXTRA_COMMAND, command);
                activity.startActivity(intent);
              })
          .show();
    } catch (Exception e) {
      Log.e(TAG, "showSetupSheet failed", e);
    }
  }
}