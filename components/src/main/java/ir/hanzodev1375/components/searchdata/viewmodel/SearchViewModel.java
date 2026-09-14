package ir.hanzodev1375.components.searchdata.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import ir.hanzodev1375.components.searchdata.FileSearchEngine;
import ir.hanzodev1375.components.searchdata.model.FileSearchResult;
import ir.hanzodev1375.components.searchdata.model.SearchQuery;

public class SearchViewModel extends AndroidViewModel {
  public enum State {
    IDLE,
    SEARCHING,
    DONE,
    ERROR
  }

  private final MutableLiveData<List<FileSearchResult>> results =
      new MutableLiveData<>(new ArrayList<>());
  private final MutableLiveData<State> state = new MutableLiveData<>(State.IDLE);
  private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
  private final MutableLiveData<Integer> resultCount = new MutableLiveData<>(0);
  private final FileSearchEngine engine = new FileSearchEngine();
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private AtomicBoolean cancelled = new AtomicBoolean(false);

  public SearchViewModel(@NonNull Application application) {
    super(application);
  }

  public LiveData<List<FileSearchResult>> getResults() {
    return results;
  }

  public LiveData<State> getState() {
    return state;
  }

  public LiveData<String> getErrorMessage() {
    return errorMessage;
  }

  public LiveData<Integer> getResultCount() {
    return resultCount;
  }

  public void startSearch(SearchQuery query) {
    cancelSearch();
    cancelled = new AtomicBoolean(false);
    List<FileSearchResult> list = new ArrayList<>();
    results.postValue(list);
    resultCount.postValue(0);
    state.postValue(State.SEARCHING);
    executor.execute(
        () ->
            engine.search(
                query,
                cancelled,
                new FileSearchEngine.SearchCallback() {
                  @Override
                  public void onResult(FileSearchResult result) {
                    list.add(result);
                    results.postValue(new ArrayList<>(list));
                    resultCount.postValue(list.size());
                  }

                  @Override
                  public void onComplete(int totalFound) {
                    if (!cancelled.get()) state.postValue(State.DONE);
                  }

                  @Override
                  public void onError(String message) {
                    errorMessage.postValue(message);
                    state.postValue(State.ERROR);
                  }
                }));
  }

  public void cancelSearch() {
    cancelled.set(true);
    state.postValue(State.IDLE);
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    cancelled.set(true);
    executor.shutdownNow();
  }
}
