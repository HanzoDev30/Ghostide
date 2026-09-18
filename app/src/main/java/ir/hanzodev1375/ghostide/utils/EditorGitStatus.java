package ir.hanzodev1375.ghostide.utils;

import android.os.Handler;
import android.os.Looper;
import com.google.android.material.tabs.TabLayout;
import ir.hanzodev1375.ghostide.customui.TabCustomView;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.datamanager.GitManager;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.model.FileChange;
import ir.hanzodev1375.ghostide.models.TabModel;
import ir.hanzodev1375.ghostide.splitlayout.SplitPaneContainerLayout;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * وضعیت گیت مخزنی که تب جاری بهش تعلق داره رو نگه می‌داره و نشانگر "تغییر کرده" روی تب‌های باز رو
 * بعد از هر عملی که ممکنه working tree رو عوض کنه تازه می‌کنه: باز/ذخیره فایل، تعویض تب، و برگشتن از
 * کفشیت گیت بعد از commit/push.
 */
public final class EditorGitStatus {

  public interface Host {
    String getCurrentFilePath();

    List<TabModel> getTabs();

    TabLayout getTabLayout();

    SplitPaneContainerLayout getSplitPaneRoot();
  }

  private static final long REFRESH_DEBOUNCE_MS = 1500;

  private final Host host;
  private final Handler mainHandler = new Handler(Looper.getMainLooper());
  private final ExecutorService gitStatusExecutor = Executors.newSingleThreadExecutor();
  private String gitStatusRepoPath;
  private final Set<String> gitChangedPaths = new HashSet<>();
  private long lastGitRefreshTime = 0;

  public EditorGitStatus(Host host) {
    this.host = host;
  }

  public void refresh() {
    long now = System.currentTimeMillis();
    if (now - lastGitRefreshTime < REFRESH_DEBOUNCE_MS) return;
    lastGitRefreshTime = now;
    String repoPath = findGitRepositoryPath();
    if (repoPath == null) {
      gitStatusRepoPath = null;
      gitChangedPaths.clear();
      updateAllTabsGitStatus();
      return;
    }
    gitStatusRepoPath = repoPath;
    gitStatusExecutor.execute(
        () -> {
          GitManager manager = new GitManager(repoPath);
          if (!manager.openRepository()) return;
          List<FileChange> changes = manager.getChangedFiles();
          mainHandler.post(
              () -> {
                updateGitChangedPaths(changes);
                updateAllTabsGitStatus();
              });
        });
  }

  public boolean isFileGitChanged(String filePath) {
    if (gitStatusRepoPath == null || filePath == null || gitChangedPaths.isEmpty()) return false;
    try {
      File repoDir = new File(gitStatusRepoPath);
      File file = new File(filePath);
      String relative =
          repoDir.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '/');
      return gitChangedPaths.contains(relative);
    } catch (Exception e) {
      return false;
    }
  }

  public boolean isGit() {
    List<TabModel> tabs = host.getTabs();
    for (int i = 0; i < tabs.size(); i++) {
      return isFileGitChanged(tabs.get(i).getFilePath());
    }
    return false;
  }

  public String findRepositoryPath() {
    return findGitRepositoryPath();
  }

  public void shutdown() {
    gitStatusExecutor.shutdownNow();
  }

  private void updateGitChangedPaths(List<FileChange> changes) {
    gitChangedPaths.clear();
    if (changes == null) return;
    for (FileChange change : changes) {
      if (change.getPath() != null) {
        gitChangedPaths.add(change.getPath().replace(File.separatorChar, '/'));
      }
    }
  }

  private void updateAllTabsGitStatus() {
    List<TabModel> tabs = host.getTabs();
    TabLayout tabLayout = host.getTabLayout();
    for (int i = 0; i < tabs.size(); i++) {
      TabLayout.Tab layoutTab = tabLayout.getTabAt(i);
      if (layoutTab != null && layoutTab.getCustomView() instanceof TabCustomView) {
        boolean changed = isFileGitChanged(tabs.get(i).getFilePath());
        ((TabCustomView) layoutTab.getCustomView()).setGitChanged(changed);
      }
    }
    SplitPaneContainerLayout splitPaneRoot = host.getSplitPaneRoot();
    if (splitPaneRoot != null) {
      splitPaneRoot.notifyGitStatus(this::isFileGitChanged);
    }
  }

  private String findGitRepositoryPath() {
    String currentFilePath = host.getCurrentFilePath();
    if (currentFilePath == null) return null;
    File currentFile = new File(currentFilePath);
    File dir = currentFile.isDirectory() ? currentFile : currentFile.getParentFile();
    while (dir != null) {
      File gitDir = new File(dir, ".git");
      if (gitDir.exists() && gitDir.isDirectory()) {
        return dir.getAbsolutePath();
      }
      dir = dir.getParentFile();
    }
    return null;
  }
}