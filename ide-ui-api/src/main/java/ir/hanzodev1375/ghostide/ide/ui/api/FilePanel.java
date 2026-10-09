package ir.hanzodev1375.ghostide.ide.ui.api;

import android.view.View;

/**
 * A UI panel contributed by a plugin and hosted <em>inside</em> the file manager, instead of taking
 * over a whole Activity like {@link PluginScreen}. It is the exact counterpart of {@link
 * EditorPanel}: same lifecycle, same display modes, but the host screen is the file manager.
 *
 * <p>Register an implementation at {@link PluginUiExtensionPoints#FILE_PANEL}. The host calls
 * {@link #createView()} once when the panel is first shown and keeps the returned {@link View} for
 * the rest of that Activity's lifetime, so create the view lazily and keep its state inside it.
 */
public interface FilePanel extends Panel {

  String getId();

  String getTitle();

  View createView();

  default String getLastPath() {
    return null;
  }

  default PluginStateMod getState() {
    return PanelStateStore.get(getId());
  }

  default void setState(PluginStateMod state) {
    PanelStateStore.set(getId(), state);
  }
}
