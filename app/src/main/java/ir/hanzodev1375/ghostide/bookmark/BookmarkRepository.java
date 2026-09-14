package ir.hanzodev1375.ghostide.bookmark;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class BookmarkRepository {
  private final BookmarkDatabaseHelper mDbHelper;
  private final ExecutorService mExecutor;
  private final Handler mMainHandler;
  private final AtomicBoolean mIsShutdown = new AtomicBoolean(false);

  public interface GetAllCallback {
    void onResult(@NonNull List<BookmarkEntity> bookmarks);
  }

  public interface ExistsCallback {
    void onResult(boolean exists);
  }

  public interface OperationCallback {
    void onComplete();
  }

  public BookmarkRepository(@NonNull Context context) {
    mDbHelper = BookmarkDatabaseHelper.getInstance(context);
    mExecutor =
        Executors.newSingleThreadExecutor(
            r -> {
              Thread t = new Thread(r, "BookmarkRepositoryThread");
              t.setDaemon(false);
              return t;
            });
    mMainHandler = new Handler(Looper.getMainLooper());
  }

  public void getAll(@NonNull GetAllCallback callback) {
    checkNotShutdown();
    mExecutor.execute(
        () -> {
          List<BookmarkEntity> list = getAllSync();
          postToMain(() -> callback.onResult(list));
        });
  }

  public void exists(@NonNull String path, @NonNull ExistsCallback callback) {
    checkNotShutdown();
    mExecutor.execute(
        () -> {
          boolean exists = existsSync(path);
          postToMain(() -> callback.onResult(exists));
        });
  }

  public void insert(@NonNull BookmarkEntity bookmark, @Nullable OperationCallback callback) {
    checkNotShutdown();
    mExecutor.execute(
        () -> {
          insertSync(bookmark);
          if (callback != null) postToMain(callback::onComplete);
        });
  }

  public void deleteByPath(@NonNull String path, @Nullable OperationCallback callback) {
    checkNotShutdown();
    mExecutor.execute(
        () -> {
          deleteByPathSync(path);
          if (callback != null) postToMain(callback::onComplete);
        });
  }

  public void clearAll(@Nullable OperationCallback callback) {
    checkNotShutdown();
    mExecutor.execute(
        () -> {
          clearAllSync();
          if (callback != null) postToMain(callback::onComplete);
        });
  }

  public void shutdown() {
    if (mIsShutdown.compareAndSet(false, true)) {
      mExecutor.shutdown();
    }
  }

  @NonNull
  private List<BookmarkEntity> getAllSync() {
    List<BookmarkEntity> result = new ArrayList<>();
    SQLiteDatabase db = mDbHelper.getReadableDatabase();
    String orderBy = BookmarkDatabaseHelper.COL_ADDED_AT + " DESC";
    try (Cursor cursor =
        db.query(BookmarkDatabaseHelper.TABLE_BOOKMARKS, null, null, null, null, null, orderBy)) {
      if (cursor == null) return result;
      int idIdx = cursor.getColumnIndex(BookmarkDatabaseHelper.COL_ID);
      int pathIdx = cursor.getColumnIndex(BookmarkDatabaseHelper.COL_PATH);
      int nameIdx = cursor.getColumnIndex(BookmarkDatabaseHelper.COL_NAME);
      int isDirIdx = cursor.getColumnIndex(BookmarkDatabaseHelper.COL_IS_DIR);
      int addedIdx = cursor.getColumnIndex(BookmarkDatabaseHelper.COL_ADDED_AT);
      while (cursor.moveToNext()) {
        BookmarkEntity entity =
            new BookmarkEntity(
                cursor.getInt(idIdx),
                cursor.getString(pathIdx),
                cursor.getString(nameIdx),
                cursor.getInt(isDirIdx) == 1,
                cursor.getLong(addedIdx));
        result.add(entity);
      }
    }
    return result;
  }

  private boolean existsSync(@NonNull String path) {
    SQLiteDatabase db = mDbHelper.getReadableDatabase();
    String[] columns = {BookmarkDatabaseHelper.COL_PATH};
    String selection = BookmarkDatabaseHelper.COL_PATH + " = ?";
    String[] args = {path};
    try (Cursor cursor =
        db.query(
            BookmarkDatabaseHelper.TABLE_BOOKMARKS, columns, selection, args, null, null, null)) {
      return cursor != null && cursor.getCount() > 0;
    }
  }

  private void insertSync(@NonNull BookmarkEntity bookmark) {
    SQLiteDatabase db = mDbHelper.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(BookmarkDatabaseHelper.COL_PATH, bookmark.path);
    values.put(BookmarkDatabaseHelper.COL_NAME, bookmark.name);
    values.put(BookmarkDatabaseHelper.COL_IS_DIR, bookmark.isDirectory ? 1 : 0);
    values.put(BookmarkDatabaseHelper.COL_ADDED_AT, bookmark.addedAt);
    db.insertWithOnConflict(
        BookmarkDatabaseHelper.TABLE_BOOKMARKS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
  }

  private void deleteByPathSync(@NonNull String path) {
    SQLiteDatabase db = mDbHelper.getWritableDatabase();
    String where = BookmarkDatabaseHelper.COL_PATH + " = ?";
    db.delete(BookmarkDatabaseHelper.TABLE_BOOKMARKS, where, new String[] {path});
  }

  private void clearAllSync() {
    SQLiteDatabase db = mDbHelper.getWritableDatabase();
    db.delete(BookmarkDatabaseHelper.TABLE_BOOKMARKS, null, null);
  }

  private void postToMain(@NonNull Runnable action) {
    mMainHandler.post(action);
  }

  private void checkNotShutdown() {
    if (mIsShutdown.get()) {
      throw new IllegalStateException("BookmarkRepository already shut down");
    }
  }
}
