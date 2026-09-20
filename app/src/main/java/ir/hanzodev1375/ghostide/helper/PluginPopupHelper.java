package ir.hanzodev1375.ghostide.helper;

import android.app.Activity;
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
import ir.hanzodev1375.ghostide.plugin.PluginPopupDispatcher;
import ir.hanzodev1375.ghostide.utils.ObjectUtil;
import java.util.Optional;

/** مسئول ساخت و نمایش پاپ‌آپ لیست پلاگین‌های نصب‌شده (دکمه‌ی پلاگین‌ها). */
public class PluginPopupHelper {

  public void show(Activity activity, View anchor) {
    var installedFiles = GplInstalledPlugins.listInstalled(activity);

    if (installedFiles.isEmpty()) {
      GhostToast.makeText(activity, R.string.no_plugins, GhostToast.LENGTH_SHORT).show();
      return;
    }

    var loader = GplPluginLoader.getInstance(activity);
    for (var f : installedFiles) {
      try {
        var manifest = GplManifestReader.read(f);
        if (manifest == null) continue;
        if (!loader.isLoaded(manifest.id())) {
          loader.load(f);
        }
      } catch (Exception e) {
        Log.e("FileManagerActivity", "showPluginPopup: failed to load " + f.getName(), e);
      }
    }

    var registeredScreens =
        GlobalRegistry.extensions().extensions(PluginUiExtensionPoints.PLUGIN_SCREEN);

    var pluginItems =
        installedFiles.stream()
            .map(
                f -> {
                  try {
                    GplManifest manifest = GplManifestReader.read(f);
                    if (manifest == null) {
                      return Optional.<PluginPopupAdapter.PluginItem>empty();
                    }

                    var ownerScreens = PluginPopupAdapter.screensOf(manifest.id());
                    if (!ownerScreens.isEmpty()) {
                      var screen = ownerScreens.get(0);
                      return Optional.of(
                          new PluginPopupAdapter.PluginItem(
                              screen.getId(), screen.getTitle(), f, manifest));
                    }

                    var matchingScreen =
                        registeredScreens.stream()
                            .filter(s -> manifest.id().equals(s.getId()))
                            .findFirst();

                    if (matchingScreen.isPresent()) {
                      return Optional.of(
                          new PluginPopupAdapter.PluginItem(
                              matchingScreen.get().getId(),
                              matchingScreen.get().getTitle(),
                              f,
                              manifest));
                    } else {
                      return Optional.of(
                          new PluginPopupAdapter.PluginItem(
                              manifest.id(), manifest.name(), f, manifest));
                    }
                  } catch (Exception e) {
                    Log.e(
                        "FileManagerActivity", "showPluginPopup: error reading: " + f.getName(), e);
                    return Optional.<PluginPopupAdapter.PluginItem>empty();
                  }
                })
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();

    if (pluginItems.isEmpty()) {
      GhostToast.makeText(activity, R.string.no_plugins, GhostToast.LENGTH_SHORT).show();
      return;
    }

    var rv = new RecyclerView(activity);
    rv.setLayoutManager(new LinearLayoutManager(activity));
    var popupRef = new PopupWindow[1];
    rv.setAdapter(
        new PluginPopupAdapter(
            (view, item, pos) -> {
              if (popupRef[0] != null) popupRef[0].dismiss();
              PluginPopupDispatcher.dispatch(activity, item, null);
            }));

    ((PluginPopupAdapter) rv.getAdapter()).submit(pluginItems);

    popupRef[0] = ObjectUtil.showGlassPopup(activity, anchor, rv);
  }
}