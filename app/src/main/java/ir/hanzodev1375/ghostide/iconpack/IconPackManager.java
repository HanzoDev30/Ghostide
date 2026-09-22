package ir.hanzodev1375.ghostide.iconpack;

import android.content.Context;
import ir.hanzodev1375.ghostide.codeeditors.setting.PreferencesUtils;
import ir.hanzodev1375.ghostide.ide.ui.api.FileIconContributor;
import ir.hanzodev1375.ghostide.ide.ui.api.PluginUiExtensionPoints;
import ir.hanzodev1375.ghostide.plugin.api.ExtensionRegistration;
import ir.hanzodev1375.ghostide.plugin.api.GlobalRegistry;
import ir.hanzodev1375.ghostide.plugin.api.PluginDescriptor;
import ir.hanzodev1375.ghostide.plugin.api.PluginIds;
import ir.hanzodev1375.ghostide.plugin.gpl.GplPluginLoader;
import ir.hanzodev1375.ghostide.plugin.gpl.LoadedGplPlugin;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Discovers the icon packs currently installed as plugins.
 *
 * <p>An "icon pack" is simply a loaded plugin that registered at {@link
 * PluginUiExtensionPoints#FILE_ICON_CONTRIBUTOR}. Each owner plugin appears once, even when it
 * registers several contributors. The selected pack id is stored in {@link PreferencesUtils}; an
 * empty id means "built-in icons, no pack". Mirrors how xed keeps a single active pack: the host's
 * icon resolver only consults the contributors of the selected pack (see {@link
 * ir.hanzodev1375.ghostide.GhostIdeAppLoader}).
 */
public final class IconPackManager {

  private IconPackManager() {}

  /** Display identity of one installed icon pack. */
  public record IconPackEntry(String id, String name, String version) {}

  /** Tracks which pack the file list last rendered with, so a change can refresh it. */
  private static volatile String appliedId;

  /**
   * Installed icon packs, in registration (priority) order. Only plugins are listed; host-owned
   * registrations are ignored.
   */
  public static List<IconPackEntry> listInstalled(Context context) {
    List<ExtensionRegistration<FileIconContributor>> registrations =
        GlobalRegistry.extensions().registrations(PluginUiExtensionPoints.FILE_ICON_CONTRIBUTOR);
    Map<String, IconPackEntry> packs = new LinkedHashMap<>();
    for (ExtensionRegistration<FileIconContributor> reg : registrations) {
      String owner = reg.ownerPluginId();
      if (owner == null || owner.isEmpty() || PluginIds.CORE.equals(owner)) continue;
      if (packs.containsKey(owner)) continue;
      LoadedGplPlugin plugin = GplPluginLoader.getInstance(context).getLoaded(owner);
      PluginDescriptor descriptor = plugin == null ? null : plugin.getDescriptor();
      packs.put(
          owner,
          new IconPackEntry(
              owner,
              descriptor == null ? owner : descriptor.getName(),
              descriptor == null ? "" : descriptor.getVersion()));
    }
    return new ArrayList<>(packs.values());
  }

  /** The selected pack id, or {@code ""} for the built-in icons. */
  public static String selected(Context context) {
    return new PreferencesUtils(context.getApplicationContext()).getIconPack();
  }

  /**
   * Returns true when the file list should be rebound because the selected pack changed since it
   * last rendered. Also records the current selection. The very first call just records and returns
   * false, so the initial load is never double-triggered.
   */
  public static boolean shouldRefresh(Context context) {
    String current = selected(context);
    String last = appliedId;
    appliedId = current;
    return last != null && !last.equals(current);
  }
}