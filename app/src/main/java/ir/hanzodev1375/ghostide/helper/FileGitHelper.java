package ir.hanzodev1375.ghostide.helper;

import android.view.View;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.LifecycleOwner;
import ir.hanzodev1375.components.views.GhostToast;
import ir.hanzodev1375.ghostide.activity.FileManagerActivity;
import ir.hanzodev1375.ghostide.adapters.FileManagerAdapter;
import ir.hanzodev1375.ghostide.databinding.ActivityFilemanagerBinding;
import ir.hanzodev1375.ghostide.jgit.fragments.GitBottomSheetFragment;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.datamanager.GitManager;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.model.FileChange;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * مسیول تمام عملیات مرتبط با Git در فایل‌منیجر: پیدا کردن ریشه‌ی ریپو، نمایش دکمه git،
 * اسکن وضعیت فایل‌ها (تغییرنیافته/تغیریافته) و علامت‌گذاری آن‌ها در لیست.
 */
public class FileGitHelper {

  private static boolean isGitRepository(String path) {
    if (path == null) return false;
    File gitDir = new File(path, ".git");
    return gitDir.exists() && gitDir.isDirectory();
  }

  private static String findGitRepositoryPath(String currentDir) {
    if (currentDir == null) return null;
    File dir = new File(currentDir);
    while (dir != null) {
      File gitDir = new File(dir, ".git");
      if (gitDir.exists() && gitDir.isDirectory()) return dir.getAbsolutePath();
      dir = dir.getParentFile();
    }
    return null;
  }

  private final FileManagerActivity activity;
  private final ActivityFilemanagerBinding bind;
  private final Supplier<FileManagerAdapter> adapterSupplier;
  private final Supplier<String> pathSupplier;

  private final ExecutorService gitStatusExecutor = Executors.newSingleThreadExecutor();
  private Set<String> gitChangedAbsPaths = new HashSet<>();
  private final AtomicBoolean gitStatusRunning = new AtomicBoolean(false);
  private final AtomicBoolean gitStatusPending = new AtomicBoolean(false);

  public FileGitHelper(
      FileManagerActivity activity,
      ActivityFilemanagerBinding bind,
      Supplier<FileManagerAdapter> adapterSupplier,
      Supplier<String> pathSupplier) {
    this.activity = activity;
    this.bind = bind;
    this.adapterSupplier = adapterSupplier;
    this.pathSupplier = pathSupplier;
  }

  public void setupButton() {
    bind.gitActionButton.setOnClickListener(
        v -> {
          String repoPath = findGitRepositoryPathForCurrent();
          if (repoPath == null) {
            GhostToast.makeText(activity, "Git dir not found", GhostToast.LENGTH_LONG).show();
            return;
          }
          GitBottomSheetFragment.newInstance(repoPath)
              .show(activity.getSupportFragmentManager(), "git_bottom_sheet");
        });
  }

  public void observe(LifecycleOwner owner, LiveData<String> currentPath) {
    currentPath.observe(
        owner,
        path -> bind.gitActionButton.setVisibility(isGitRepository(path) ? View.VISIBLE : View.GONE));
  }

  public String findGitRepositoryPathForCurrent() {
    return findGitRepositoryPath(pathSupplier.get());
  }

  public void showGitAction(boolean visible) {
    bind.gitActionButton.setVisibility(visible ? View.VISIBLE : View.GONE);
  }

  public void updateGitActionVisibility(String path) {
    bind.gitActionButton.setVisibility(isGitRepository(path) ? View.VISIBLE : View.GONE);
  }

  public boolean isRepo(String path) {
    return isGitRepository(path);
  }

  /**
   * وضعیت git دایرکتوری فعلی را تازه می‌کند و فایل/پوشه‌های با تغییر ثبت‌نشده را در لیست
   * برجسته می‌کند. هر بار که محتوای دایرکتوری روی دیسک عوض شود (ناوبری، بازگشت به فعالیت،
   * بعد از commit/push و ...) صدا بزنید.
   */
  public void refreshGitStatus() {
    String repoRoot = findGitRepositoryPathForCurrent();
    if (repoRoot == null) {
      gitStatusPending.set(false);
      if (!gitChangedAbsPaths.isEmpty()) {
        gitChangedAbsPaths = new HashSet<>();
        Set<String> empty = gitChangedAbsPaths;
        bind.rvfiles.post(() -> setGitChangedPaths(empty));
      }
      return;
    }
    if (!gitStatusRunning.compareAndSet(false, true)) {
      // یک اسکن در حال اجراست؛ بعد از پایانش یک بار دیگر اجرا شود تا وضعیت آخر روی دیسک
      // بازتاب شود.
      gitStatusPending.set(true);
      return;
    }
    gitStatusExecutor.execute(
        () -> {
          try {
            GitManager manager = new GitManager(repoRoot);
            if (manager.openRepository()) {
              applyChangedFiles(repoRoot, manager.getChangedFiles());
            }
          } finally {
            gitStatusRunning.set(false);
            if (gitStatusPending.compareAndSet(true, false)) {
              refreshGitStatus();
            }
          }
        });
  }

  public void applyChangedFiles(String repoRoot, List<FileChange> changes) {
    Set<String> absPaths = new HashSet<>();
    if (changes != null) {
      for (FileChange change : changes) {
        if (change.getPath() != null) {
          absPaths.add(new File(repoRoot, change.getPath()).getAbsolutePath());
        }
      }
    }
    activity.runOnUiThread(() -> setGitChangedPaths(absPaths));
  }

  private void setGitChangedPaths(Set<String> absPaths) {
    gitChangedAbsPaths = absPaths;
    FileManagerAdapter adapter = adapterSupplier.get();
    if (adapter != null) {
      bind.rvfiles.post(() -> adapter.setGitChangedPaths(absPaths));
    }
  }

  public void shutdown() {
    gitStatusExecutor.shutdownNow();
  }
}