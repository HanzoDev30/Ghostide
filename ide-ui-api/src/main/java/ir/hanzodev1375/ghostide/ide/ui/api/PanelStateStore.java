package ir.hanzodev1375.ghostide.ide.ui.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Backing store for the default {@link Panel#setState(PluginStateMod)} /
 * {@link Panel#getState()} implementation. Interfaces cannot hold instance fields, so the chosen
 * {@link PluginStateMod} is keyed by {@link Panel#getId()}.
 */
final class PanelStateStore {

  private static final Map<String, PluginStateMod> STATES = new ConcurrentHashMap<>();

  private PanelStateStore() {}

  static PluginStateMod get(String panelId) {
    return STATES.getOrDefault(panelId, PluginStateMod.SIDESHEET);
  }

  static void set(String panelId, PluginStateMod state) {
    if (panelId == null) {
      return;
    }
    if (state == null) {
      STATES.remove(panelId);
    } else {
      STATES.put(panelId, state);
    }
  }
}
