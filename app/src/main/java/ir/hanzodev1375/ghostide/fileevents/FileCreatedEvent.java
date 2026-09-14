package ir.hanzodev1375.ghostide.fileevents;

import ir.hanzodev1375.ghostide.enums.FileState;

/** Freshly created file/folder: green name, like Android Studio's "added" VCS status. */
public final class FileCreatedEvent extends BaseFileEventCheck {

  public FileCreatedEvent(String path) {
    super(path);
  }

  @Override
  public FileState getState() {
    return FileState.CREATOR;
  }

  @Override
  public int getTextColor() {
    return themeVariant(0xFF2E7D32, 0xFF81C784);
  }
}