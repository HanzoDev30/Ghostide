package ir.hanzodev1375.ghostide.history;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryRepository {

  private static final String PREFS_NAME = "history_prefs";
  private static final String KEY_HISTORY = "history_list";
  private final SharedPreferences prefs;
  private final Gson gson = new Gson();
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private final MutableLiveData<List<HistoryEntity>> historyLiveData = new MutableLiveData<>();
  private List<HistoryEntity> cachedList = new ArrayList<>();

  private static HistoryRepository instance;

  public static synchronized HistoryRepository getInstance(Context context) {
    if (instance == null) {
      instance = new HistoryRepository(context.getApplicationContext());
    }
    return instance;
  }

  private HistoryRepository(Context context) {
    prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    loadFromPrefs();
  }

  private void loadFromPrefs() {
    executor.execute(
        () -> {
          String json = prefs.getString(KEY_HISTORY, null);
          List<HistoryEntity> list;
          if (json != null) {
            Type type = new TypeToken<List<HistoryEntity>>() {}.getType();
            list = gson.fromJson(json, type);
            if (list == null) list = new ArrayList<>();
          } else {
            list = new ArrayList<>();
          }
          sortByTimestampDesc(list);
          cachedList = list;
          historyLiveData.postValue(new ArrayList<>(cachedList));
        });
  }

  private void saveToPrefs() {
    executor.execute(
        () -> {
          String json = gson.toJson(cachedList);
          prefs.edit().putString(KEY_HISTORY, json).apply();
        });
  }

  private void sortByTimestampDesc(List<HistoryEntity> list) {
    Collections.sort(list, (a, b) -> Long.compare(b.timestamp, a.timestamp));
  }

  public LiveData<List<HistoryEntity>> getHistory() {
    return historyLiveData;
  }

  public void insert(HistoryEntity entity) {
    executor.execute(
        () -> {
          // حذف مسیر تکراری
          cachedList.removeIf(item -> item.path.equals(entity.path));
          // اضافه کردن آیتم جدید
          cachedList.add(entity);
          // مرتب‌سازی نزولی بر اساس timestamp
          sortByTimestampDesc(cachedList);
          // نگهداری فقط 20 آیتم اول
          if (cachedList.size() > 20) {
            cachedList = new ArrayList<>(cachedList.subList(0, 20));
          }
          // بروزرسانی LiveData در ترد اصلی
          historyLiveData.postValue(new ArrayList<>(cachedList));
          saveToPrefs();
        });
  }

  public void deleteByPath(String path) {
    executor.execute(
        () -> {
          cachedList.removeIf(item -> item.path.equals(path));
          historyLiveData.postValue(new ArrayList<>(cachedList));
          saveToPrefs();
        });
  }

  public void trimToLimit() {
    executor.execute(
        () -> {
          if (cachedList.size() > 20) {
            cachedList = new ArrayList<>(cachedList.subList(0, 20));
            historyLiveData.postValue(new ArrayList<>(cachedList));
            saveToPrefs();
          }
        });
  }

  public void clearAll() {
    executor.execute(
        () -> {
          cachedList.clear();
          historyLiveData.postValue(new ArrayList<>(cachedList));
          saveToPrefs();
        });
  }
}
