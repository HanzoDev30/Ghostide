package ir.hanzodev1375.ghostide.settings;

import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.activity.SettingActivity;
import ir.hanzodev1375.ghostide.codeeditors.setting.PreferencesUtils;
import ir.hanzodev1375.ghostide.iconpack.IconPackManager;
import ir.hanzodev1375.ghostide.models.SettingItem;
import java.util.ArrayList;
import java.util.List;

/** Pick which installed icon pack (plugin) applies; "Default" means built-in icons. */
public class IconPackSettingSection extends SettingSection {

  private static final int POS_PACK = 0;

  private final PreferencesUtils prefs;

  public IconPackSettingSection(SettingActivity activity, PreferencesUtils prefs) {
    super(activity);
    this.prefs = prefs;
  }

  @Override
  protected List<SettingItem> buildItems() {
    List<SettingItem> items = new ArrayList<>();
    items.add(textItem(R.string.icon_pack_title, currentPackDescription()));
    return items;
  }

  @Override
  public void onItemClick(int position) {
    if (position == POS_PACK) showPackDialog();
  }

  private String currentPackDescription() {
    String selected = prefs.getIconPack();
    if (selected == null || selected.isEmpty()) {
      return getString(R.string.icon_pack_default);
    }
    for (IconPackManager.IconPackEntry entry : IconPackManager.listInstalled(activity)) {
      if (entry.id().equals(selected)) {
        if (entry.version() == null || entry.version().isEmpty()) {
          return entry.name();
        }
        return entry.name() + "  •  v" + entry.version();
      }
    }
    return getString(R.string.icon_pack_default);
  }

  private void showPackDialog() {
    List<IconPackManager.IconPackEntry> packs = IconPackManager.listInstalled(activity);
    String selected = prefs.getIconPack();
    String[] names = new String[packs.size() + 1];
    names[0] = getString(R.string.icon_pack_default);
    int checked = 0;
    for (int i = 0; i < packs.size(); i++) {
      IconPackManager.IconPackEntry pack = packs.get(i);
      String version = pack.version() == null ? "" : pack.version();
      names[i + 1] = version.isEmpty() ? pack.name() : pack.name() + "  •  v" + version;
      if (selected != null && selected.equals(pack.id())) checked = i + 1;
    }
    showSingleChoiceDialog(
        R.string.icon_pack_title,
        names,
        checked,
        which -> {
          String id = which == 0 ? "" : packs.get(which - 1).id();
          prefs.setIconPack(id);
          updateDescriptionAt(POS_PACK, currentPackDescription());
        });
  }
}