package ir.hanzodev1375.ghostide.history;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class HistoryViewModel extends AndroidViewModel {

  private final HistoryRepository repository;

  public HistoryViewModel(Application app) {
    super(app);
    repository = HistoryRepository.getInstance(app);
  }

  public LiveData<List<HistoryEntity>> getHistory() {
    return repository.getHistory();
  }

  public void addToHistory(String path, String name, boolean isDirectory) {
    repository.deleteByPath(path);
    repository.insert(new HistoryEntity(path, name, isDirectory, System.currentTimeMillis()));
    repository.trimToLimit();
  }

  public void clearHistory() {
    repository.clearAll();
  }
}
