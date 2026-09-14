package ir.hanzodev1375.ghostide.fileevents;

import androidx.annotation.ColorInt;
import ir.hanzodev1375.ghostide.enums.FileState;

/**
 * Decides how a single file-manager row reflects a file event (created / renamed / searched).
 * Implementations expose the semantic {@link FileState} plus the M3Theme-aware color that should be
 * applied to the file name of the matching row only.
 */
public interface FileEventCheck {

  /** The {@link FileState} this event maps to. */
  FileState getState();

  /** Color applied to the file name text of the matching row. */
  @ColorInt int getTextColor();

  /** Whether the given absolute file path is affected by this event. */
  boolean matches(String path);
}