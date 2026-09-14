package ir.hanzodev1375.ghostide.fileevents;

import androidx.core.graphics.ColorUtils;
import ir.theme.M3Theme;

/** Base class for path-bound file events. */
public abstract class BaseFileEventCheck implements FileEventCheck {

  private final String path;

  protected BaseFileEventCheck(String path) {
    this.path = path;
  }

  @Override
  public boolean matches(String path) {
    return path != null && path.equals(this.path);
  }

  /**
   * Picks a light/dark variant based on the current M3Theme surface luminance so the highlight
   * stays readable on both themes.
   */
  protected int themeVariant(int light, int dark) {
    return ColorUtils.calculateLuminance(M3Theme.surface()) > 0.5f ? light : dark;
  }
}
