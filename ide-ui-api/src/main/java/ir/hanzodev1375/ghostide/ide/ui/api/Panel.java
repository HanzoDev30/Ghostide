package ir.hanzodev1375.ghostide.ide.ui.api;

import android.view.View;

/**
 * Common contract shared by every in-screen panel a plugin can contribute, regardless of which
 * host screen shows it: {@link EditorPanel} inside the editor and {@link FilePanel} inside the file
 * manager. The host only ever deals with this type, so both panels reuse the exact same hosting
 * logic (side sheet, dialog, bottom sheet, floating window, ...).
 */
public interface Panel {

  String getId();

  String getTitle();

  View createView();

  default String getLastPath() {
    return null;
  }

  /** How the host should display this panel. Defaults to {@link PluginStateMod#SIDESHEET}. */
  default PluginStateMod getState() {
    return PanelStateStore.get(getId());
  }

  /** Overrides the display mode used by the host. {@code null} resets back to the default. */
  default void setState(PluginStateMod state) {
    PanelStateStore.set(getId(), state);
  }
}
