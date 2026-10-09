package ir.hanzodev1375.ghostide.glide.gpl;

import java.io.File;
import java.util.Objects;

/**
 * Glide model that loads the icon a {@code .gpl} declares inside {@code assets/plugin.json}. The
 * entry name (for example {@code icon.png}) is resolved from the manifest at load time, so callers
 * only need to hand over the plugin file itself.
 */
public final class GplIcon {

  private final File file;

  public GplIcon(File file) {
    this.file = Objects.requireNonNull(file, "file");
  }

  public File file() {
    return file;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (!(other instanceof GplIcon gplIcon)) return false;
    return file.getAbsolutePath().equals(gplIcon.file.getAbsolutePath());
  }

  @Override
  public int hashCode() {
    return file.getAbsolutePath().hashCode();
  }

  @Override
  public String toString() {
    return "GplIcon[" + file.getName() + "]";
  }
}
