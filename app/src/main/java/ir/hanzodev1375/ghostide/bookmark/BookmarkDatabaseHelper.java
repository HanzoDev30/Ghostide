package ir.hanzodev1375.ghostide.bookmark;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

public final class BookmarkDatabaseHelper extends SQLiteOpenHelper {
  private static final String DB_NAME = "bookmark_db";
  private static final int VERSION = 1;

  public static final String TABLE_BOOKMARKS = "bookmarks";
  public static final String COL_ID = "id";
  public static final String COL_PATH = "path";
  public static final String COL_NAME = "name";
  public static final String COL_IS_DIR = "is_directory";
  public static final String COL_ADDED_AT = "added_at";

  private static volatile BookmarkDatabaseHelper sInstance;

  @NonNull
  public static BookmarkDatabaseHelper getInstance(@NonNull Context context) {
    if (sInstance == null) {
      synchronized (BookmarkDatabaseHelper.class) {
        if (sInstance == null) {
          sInstance = new BookmarkDatabaseHelper(context.getApplicationContext());
        }
      }
    }
    return sInstance;
  }

  private BookmarkDatabaseHelper(@NonNull Context context) {
    super(context, DB_NAME, null, VERSION);
  }

  @Override
  public void onCreate(@NonNull SQLiteDatabase db) {
    String createTable =
        "CREATE TABLE "
            + TABLE_BOOKMARKS
            + " ("
            + COL_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_PATH
            + " TEXT UNIQUE NOT NULL, "
            + COL_NAME
            + " TEXT NOT NULL, "
            + COL_IS_DIR
            + " INTEGER NOT NULL, "
            + COL_ADDED_AT
            + " INTEGER NOT NULL)";
    db.execSQL(createTable);
  }

  @Override
  public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKMARKS);
    onCreate(db);
  }
}
