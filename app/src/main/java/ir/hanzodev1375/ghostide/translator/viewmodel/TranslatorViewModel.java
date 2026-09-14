package ir.hanzodev1375.ghostide.translator.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import ir.hanzodev1375.ghostide.translator.model.AndroidLanguage;
import ir.hanzodev1375.ghostide.translator.model.TranslationProgress;
import ir.hanzodev1375.ghostide.translator.util.StringsTranslator;

public class TranslatorViewModel extends AndroidViewModel {
  public enum State {
    IDLE,
    RUNNING,
    DONE,
    ERROR,
    CANCELLED
  }

  private final MutableLiveData<State> state = new MutableLiveData<>(State.IDLE);
  private final MutableLiveData<TranslationProgress> progress = new MutableLiveData<>();
  private final MutableLiveData<String> lastCompletedFolder = new MutableLiveData<>();
  private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
  private final StringsTranslator translator = new StringsTranslator();
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private AtomicBoolean cancelled = new AtomicBoolean(false);

  public TranslatorViewModel(@NonNull Application application) {
    super(application);
  }

  public LiveData<State> getState() {
    return state;
  }

  public LiveData<TranslationProgress> getProgress() {
    return progress;
  }

  public LiveData<String> getLastCompletedFolder() {
    return lastCompletedFolder;
  }

  public LiveData<String> getErrorMessage() {
    return errorMessage;
  }

  public void startTranslation(File sourceXml, String resDir, List<AndroidLanguage> languages) {
    cancelled = new AtomicBoolean(false);
    state.postValue(State.RUNNING);
    executor.execute(
        () ->
            translator.translate(
                sourceXml,
                resDir,
                languages,
                cancelled,
                new StringsTranslator.Callback() {
                  @Override
                  public void onProgress(TranslationProgress p) {
                    progress.postValue(p);
                  }

                  @Override
                  public void onLanguageDone(
                      String folder, int translatedCount, int skippedCount, int failedCount) {
                    StringBuilder msg = new StringBuilder(folder);
                    if (skippedCount > 0 || translatedCount > 0 || failedCount > 0) {
                      msg.append(" (").append(translatedCount).append(" translated");
                      if (skippedCount > 0)
                        msg.append(", ").append(skippedCount).append(" skipped");
                      if (failedCount > 0) {
                        msg.append(", ")
                            .append(failedCount)
                            .append(" failed — will retry next run");
                      }
                      msg.append(")");
                    }
                    lastCompletedFolder.postValue(msg.toString());
                  }

                  @Override
                  public void onComplete() {
                    state.postValue(State.DONE);
                  }

                  @Override
                  public void onError(String message) {
                    errorMessage.postValue(message);
                    state.postValue(State.ERROR);
                  }
                }));
  }

  public void cancel() {
    cancelled.set(true);
    state.postValue(State.CANCELLED);
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    cancelled.set(true);
    executor.shutdownNow();
  }
}
