package ir.hanzodev1375.ghostide.bookmark;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class BookmarkViewModel extends AndroidViewModel {
  private final BookmarkRepository mRepository;
  private final MutableLiveData<List<BookmarkEntity>> mBookmarksLiveData = new MutableLiveData<>();

  public BookmarkViewModel(@NonNull Application application) {
    super(application);
    mRepository = new BookmarkRepository(application);
    refreshBookmarks();
  }

  @NonNull
  public LiveData<List<BookmarkEntity>> getBookmarks() {
    return mBookmarksLiveData;
  }

  public void toggle(
      @NonNull String path,
      @NonNull String name,
      boolean isDirectory,
      @Nullable OnToggleCallback callback) {
    mRepository.exists(
        path,
        exists -> {
          if (exists) {
            mRepository.deleteByPath(
                path,
                () -> {
                  refreshBookmarks();
                  if (callback != null) callback.onResult(false);
                });
          } else {
            BookmarkEntity newBookmark = BookmarkEntity.forInsert(path, name, isDirectory);
            mRepository.insert(
                newBookmark,
                () -> {
                  refreshBookmarks();
                  if (callback != null) callback.onResult(true);
                });
          }
        });
  }

  public void isBookmarked(@NonNull String path, @NonNull OnCheckCallback callback) {
    mRepository.exists(path, callback::onResult);
  }

  public void removeBookmark(@NonNull String path) {
    mRepository.deleteByPath(path, this::refreshBookmarks);
  }

  public void clearAll() {
    mRepository.clearAll(this::refreshBookmarks);
  }

  private void refreshBookmarks() {
    mRepository.getAll(bookmarks -> mBookmarksLiveData.postValue(bookmarks));
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    mRepository.shutdown();
  }

  public interface OnToggleCallback {
    void onResult(boolean isNowBookmarked);
  }

  public interface OnCheckCallback {
    void onResult(boolean isBookmarked);
  }
}
