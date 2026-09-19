package ir.hanzodev1375.ghostide.helper;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.PopupWindow;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ir.hanzodev1375.components.sheet.PluginSetupSheet;
import ir.hanzodev1375.components.store.model.PluginSetupActionData;
import ir.hanzodev1375.components.views.GhostToast;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.activity.PluginScreenActivity;
import ir.hanzodev1375.ghostide.adapters.PluginPopupAdapter;
import ir.hanzodev1375.ghostide.ide.ui.api.PluginUiExtensionPoints;
import ir.hanzodev1375.ghostide.plugin.api.GlobalRegistry;
import ir.hanzodev1375.ghostide.plugin.api.PluginSetupAction;
import ir.hanzodev1375.ghostide.plugin.gpl.GplInstalledPlugins;
import ir.hanzodev1375.ghostide.plugin.gpl.GplManifest;
import ir.hanzodev1375.ghostide.plugin.gpl.GplManifestReader;
import ir.hanzodev1375.ghostide.plugin.gpl.GplPluginLoader;
import ir.hanzodev1375.ghostide.plugin.gpl.LoadedGplPlugin;
import ir.hanzodev1375.ghostide.terminal.activity.TerminalActivity;
import ir.hanzodev1375.ghostide.utils.ObjectUtil;
import java.util.ArrayList;
import java.util.List;
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
              String ownerId = item.manifest() != null ? item.manifest().id() : item.id();

              var ownerScreens = PluginPopupAdapter.screensOf(ownerId);
              if (!ownerScreens.isEmpty()) {
                activity.startActivity(PluginScreenActivity.createIntent(activity, ownerScreens.get(0).getId()));
                return;
              }

              var allScreens =
                  GlobalRegistry.extensions().extensions(PluginUiExtensionPoints.PLUGIN_SCREEN);

              var matchingScreen =
                  allScreens.stream()
                      .filter(
                          s ->
                              item.id().equals(s.getId())
                                  || (item.manifest() != null
                                      && item.manifest().id().equals(s.getId())))
                      .findFirst();
              if (matchingScreen.isPresent()) {
                activity.startActivity(PluginScreenActivity.createIntent(activity, matchingScreen.get().getId()));
                return;
              }

              showSetupSheet(activity, item);
            }));

    ((PluginPopupAdapter) rv.getAdapter()).submit(pluginItems);

    popupRef[0] = ObjectUtil.showGlassPopup(activity, anchor, rv);
  }

  private void showSetupSheet(Activity activity, PluginPopupAdapter.PluginItem item) {
    if (item.gplFile() == null) {
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
      Log.e("FileManagerActivity", "showSetupSheet failed", e);
    }
  }
}