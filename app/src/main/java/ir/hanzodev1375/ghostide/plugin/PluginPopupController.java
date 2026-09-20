package ir.hanzodev1375.ghostide.plugin;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.PopupWindow;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ir.hanzodev1375.components.views.GhostToast;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.adapters.PluginPopupAdapter;
import ir.hanzodev1375.ghostide.ide.ui.api.PluginUiExtensionPoints;
import ir.hanzodev1375.ghostide.plugin.api.GlobalRegistry;
import ir.hanzodev1375.ghostide.plugin.gpl.GplInstalledPlugins;
import ir.hanzodev1375.ghostide.plugin.gpl.GplManifest;
import ir.hanzodev1375.ghostide.plugin.gpl.GplManifestReader;
import ir.hanzodev1375.ghostide.plugin.gpl.GplPluginLoader;
import ir.hanzodev1375.ghostide.utils.ObjectUtil;
import java.util.Optional;

public final class PluginPopupController {

  private static final String TAG = "EditorActivity";

  private final Context context;
  private final PluginPanelHost panelHost;

  public PluginPopupController(Context context, PluginPanelHost panelHost) {
    this.context = context;
    this.panelHost = panelHost;
  }

  public void show(View anchor) {
    var installedFiles = GplInstalledPlugins.listInstalled(context);
    Log.d(TAG, "showPluginPopup: installed files = " + installedFiles.size());

    if (installedFiles.isEmpty()) {
      Log.w(TAG, "showPluginPopup: no .gpl files found on disk");
      GhostToast.makeText(context, R.string.no_plugins, GhostToast.LENGTH_SHORT).show();
      return;
    }

    var loader = GplPluginLoader.getInstance(context);
    for (var f : installedFiles) {
      try {
        var manifest = GplManifestReader.read(f);
        if (manifest == null) continue;
        if (!loader.isLoaded(manifest.id())) {
          loader.load(f);
          Log.d(TAG, "showPluginPopup: loaded .gpl plugin: " + manifest.id());
        }
      } catch (Exception e) {
        Log.e(TAG, "showPluginPopup: failed to load " + f.getName(), e);
      }
    }

    var registeredPanels =
        GlobalRegistry.extensions().extensions(PluginUiExtensionPoints.EDITOR_PANEL);
    var registeredScreens =
        GlobalRegistry.extensions().extensions(PluginUiExtensionPoints.PLUGIN_SCREEN);
    Log.d(TAG, "showPluginPopup: registered EditorPanels = " + registeredPanels.size());
    Log.d(TAG, "showPluginPopup: registered PluginScreens = " + registeredScreens.size());

    var pluginItems =
        installedFiles.stream()
            .map(
                f -> {
                  try {
                    GplManifest manifest = GplManifestReader.read(f);
                    if (manifest == null) {
                      Log.w(TAG, "  manifest null for: " + f.getName());
                      return Optional.<PluginPopupAdapter.PluginItem>empty();
                    }
                    Log.d(TAG, "  file=" + f.getName() + " manifestId=" + manifest.id());

                    var ownerPanels = PluginPopupAdapter.panelsOf(manifest.id());
                    if (!ownerPanels.isEmpty()) {
                      var panel = ownerPanels.get(0);
                      Log.d(TAG, "    -> matched EditorPanel(owner): " + panel.getId());
                      return Optional.of(
                          new PluginPopupAdapter.PluginItem(
                              panel.getId(), panel.getTitle(), f, manifest));
                    }

                    var ownerScreens = PluginPopupAdapter.screensOf(manifest.id());
                    if (!ownerScreens.isEmpty()) {
                      var screen = ownerScreens.get(0);
                      Log.d(TAG, "    -> matched PluginScreen(owner): " + screen.getId());
                      return Optional.of(
                          new PluginPopupAdapter.PluginItem(
                              screen.getId(), screen.getTitle(), f, manifest));
                    }

                    var matchingPanel =
                        registeredPanels.stream()
                            .filter(p -> manifest.id().equals(p.getId()))
                            .findFirst();
                    var matchingScreen =
                        registeredScreens.stream()
                            .filter(s -> manifest.id().equals(s.getId()))
                            .findFirst();

                    if (matchingPanel.isPresent()) {
                      Log.d(
                          TAG,
                          "    -> matched EditorPanel: " + matchingPanel.get().getId());
                      return Optional.of(
                          new PluginPopupAdapter.PluginItem(
                              matchingPanel.get().getId(),
                              matchingPanel.get().getTitle(),
                              f,
                              manifest));
                    } else if (matchingScreen.isPresent()) {
                      Log.d(
                          TAG,
                          "    -> matched PluginScreen: " + matchingScreen.get().getId());
                      return Optional.of(
                          new PluginPopupAdapter.PluginItem(
                              matchingScreen.get().getId(),
                              matchingScreen.get().getTitle(),
                              f,
                              manifest));
                    } else {
                      Log.d(
                          TAG,
                          "    -> no extension for manifestId="
                              + manifest.id()
                              + ", showing by manifest name");
                      return Optional.of(
                          new PluginPopupAdapter.PluginItem(
                              manifest.id(), manifest.name(), f, manifest));
                    }
                  } catch (Exception e) {
                    Log.e(TAG, "  error reading: " + f.getName(), e);
                    return Optional.<PluginPopupAdapter.PluginItem>empty();
                  }
                })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

    Log.d(TAG, "showPluginPopup: pluginItems size = " + pluginItems.size());

    if (pluginItems.isEmpty()) {
      GhostToast.makeText(context, R.string.no_plugins, GhostToast.LENGTH_SHORT).show();
      return;
    }

    var rv = new RecyclerView(context);
    rv.setLayoutManager(new LinearLayoutManager(context));
    var popupRef = new PopupWindow[1];
    rv.setAdapter(
        new PluginPopupAdapter(
            (view, item, pos) -> {
              if (popupRef[0] != null) popupRef[0].dismiss();
              PluginPopupDispatcher.dispatch((Activity) context, item, panelHost);
            }));

    ((PluginPopupAdapter) rv.getAdapter()).submit(pluginItems);

    popupRef[0] = ObjectUtil.showGlassPopup((Activity) context, anchor, rv);
  }
}