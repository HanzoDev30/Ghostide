package ir.hanzodev1375.components.store.event;

import androidx.annotation.Nullable;
import ir.hanzodev1375.components.store.model.PluginItem;

/** Download progress of a plugin package, posted from {@code PluginRepository}. */
public class PluginProgressEvent {

  /** Value of {@link #progress} when the total size is unknown. */
  public static final float INDETERMINATE = -1f;

  @Nullable public final PluginItem plugin;
  public final float progress;

  public PluginProgressEvent(@Nullable PluginItem plugin, float progress) {
    this.plugin = plugin;
    this.progress = progress;
  }
}
