package ir.hanzodev1375.ghostide.bookmark;

public class BookmarkEntity {
  public final int id;
  public final String path;
  public final String name;
  public final boolean isDirectory;
  public final long addedAt;

  public BookmarkEntity(int id, String path, String name, boolean isDirectory, long addedAt) {
    this.id = id;
    this.path = path;
    this.name = name;
    this.isDirectory = isDirectory;
    this.addedAt = addedAt;
  }

  public static BookmarkEntity forInsert(String path, String name, boolean isDirectory) {
    return new BookmarkEntity(0, path, name, isDirectory, System.currentTimeMillis());
  }
}
