package ir.hanzodev1375.ghostide.glide.gpl;

import java.io.File;
import java.util.Objects;

/**
 * Glide model that loads the icon a {@code .gpl} declares inside {@code assets/plugin.json}. The
 * entry name (for example {@code icon.png}) is resolved from the manifest at load time, so callers
 * only need to hand over the plugin file itself.
 */
public record GplIcon(File file) {

  public GplIcon {
    Objects.requireNonNull(file, "file");
    file = file.getAbsoluteFile();
  }

  @Override
  public String toString() {
    return "GplIcon[" + file.getName() + "]";
  }
}
