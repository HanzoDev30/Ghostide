package ir.hanzodev1375.ghostide.fileevents;

import ir.hanzodev1375.ghostide.enums.FileState;

/** Renamed file/folder: yellow name, like Android Studio's "renamed" VCS status. */
public final class FileRenamedEvent extends BaseFileEventCheck {

  public FileRenamedEvent(String path) {
    super(path);
  }

  @Override
  public FileState getState() {
    return FileState.RENAME;
  }

  @Override
  public int getTextColor() {
    return themeVariant(0xFFF9A825, 0xFFFFD54F);
  }
}