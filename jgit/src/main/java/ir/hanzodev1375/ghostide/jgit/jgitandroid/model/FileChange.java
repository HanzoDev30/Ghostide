package ir.hanzodev1375.ghostide.jgit.jgitandroid.model;

import ir.hanzodev1375.ghostide.jgit.jgitandroid.ChangeType;
import java.util.Objects;

public class FileChange {

  private final String path;
  private final ChangeType changeType;
  private final boolean isStaged;

  public FileChange(String path, ChangeType changeType, boolean isStaged) {
    this.path = path;
    this.changeType = changeType;
    this.isStaged = isStaged;
  }

  public FileChange(String path, ChangeType changeType) {
    this(path, changeType, false);
  }

  public String getPath() {
    return path;
  }

  public ChangeType getChangeType() {
    return changeType;
  }

  public boolean isStaged() {
    return isStaged;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FileChange)) return false;
    var that = (FileChange) o;
    return isStaged == that.isStaged
        && Objects.equals(path, that.path)
        && changeType == that.changeType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(path, changeType, isStaged);
  }

  @Override
  public String toString() {
    return "FileChange{path='"
        + path
        + "', changeType="
        + changeType
        + ", isStaged="
        + isStaged
        + "}";
  }
}
