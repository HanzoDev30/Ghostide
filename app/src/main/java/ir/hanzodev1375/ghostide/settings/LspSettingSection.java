package ir.hanzodev1375.ghostide.settings;

import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.activity.SettingActivity;
import ir.hanzodev1375.ghostide.codeeditors.setting.PreferencesUtils;
import ir.hanzodev1375.ghostide.models.SettingItem;
import java.util.ArrayList;
import java.util.List;

public class LspSettingSection extends SettingSection {

  private final PreferencesUtils prefs;

  public LspSettingSection(SettingActivity activity, PreferencesUtils prefs) {
    super(activity);
    this.prefs = prefs;
  }

  @Override
  protected List<SettingItem> buildItems() {
    List<SettingItem> items = new ArrayList<>();
    items.add(
        switchItem(
            R.string.lsp_inlaytitle,
            R.string.lsp_inlaysubtitle,
            prefs.isInlayHint(),
            prefs::setInlayHint));
    items.add(
        switchItem(
            R.string.lsp_hovertitle,
            R.string.lsp_hoversubtitle,
            prefs.isHover(),
            prefs::setHover));
    items.add(
        switchItem(
            R.string.lsp_signaturehelptitle,
            R.string.lsp_signaturehelpsubtitle,
            prefs.isSignatureHelp(),
            prefs::setSignatureHelp));
    items.add(
        switchItem(
            R.string.lsp_diagnosticstitle,
            R.string.lsp_diagnosticssubtitle,
            prefs.isDiagnostics(),
            prefs::setDiagnostics));
    return items;
  }

  @Override
  public void onItemClick(int position) {
    // LSP rows are all switches; nothing to handle.
  }
}