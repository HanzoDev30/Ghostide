package ir.hanzodev1375.ghostide.jgit.jgitandroid.datamanager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.model.FileChange;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.model.CommitInfo;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.RepositoryStatus;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.model.OperationResult;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.model.RemoteInfo;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.model.RemoteOperationResult;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.model.PushResult;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.model.PullResult;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.model.FetchResult;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.model.StashInfo;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.model.ConflictFile;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.model.TagInfo;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.model.ResetMode;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.model.BlameInfo;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GitViewModel extends ViewModel {

  private GitManager gitManager = null;

  private final MutableLiveData<List<FileChange>> _changedFiles = new MutableLiveData<>();
  public final LiveData<List<FileChange>> changedFiles = _changedFiles;

  private final MutableLiveData<List<CommitInfo>> _commitHistory = new MutableLiveData<>();
  public final LiveData<List<CommitInfo>> commitHistory = _commitHistory;

  private final MutableLiveData<String> _currentBranch = new MutableLiveData<>();
  public final LiveData<String> currentBranch = _currentBranch;

  private final MutableLiveData<List<String>> _branches = new MutableLiveData<>();
  public final LiveData<List<String>> branches = _branches;

  private final MutableLiveData<RepositoryStatus> _repositoryStatus = new MutableLiveData<>();
  public final LiveData<RepositoryStatus> repositoryStatus = _repositoryStatus;

  private final MutableLiveData<OperationResult> _operationResult = new MutableLiveData<>();
  public final LiveData<OperationResult> operationResult = _operationResult;

  private final MutableLiveData<List<RemoteInfo>> _remotes = new MutableLiveData<>();
  public final LiveData<List<RemoteInfo>> remotes = _remotes;

  private final MutableLiveData<Boolean> _isRepositoryInitialized = new MutableLiveData<>();
  public final LiveData<Boolean> isRepositoryInitialized = _isRepositoryInitialized;

  private final MutableLiveData<RemoteOperationResult> _pushPullResult = new MutableLiveData<>();
  public final LiveData<RemoteOperationResult> pushPullResult = _pushPullResult;

  private final MutableLiveData<String> _progressMessage = new MutableLiveData<>();
  public final LiveData<String> progressMessage = _progressMessage;

  private final MutableLiveData<List<StashInfo>> _stashList = new MutableLiveData<>();
  public final LiveData<List<StashInfo>> stashList = _stashList;

  private final MutableLiveData<List<ConflictFile>> _conflictFiles = new MutableLiveData<>();
  public final LiveData<List<ConflictFile>> conflictFiles = _conflictFiles;

  private final MutableLiveData<List<TagInfo>> _tags = new MutableLiveData<>();
  public final LiveData<List<TagInfo>> tags = _tags;

  private final MutableLiveData<String> _gitIgnoreContent = new MutableLiveData<>();
  public final LiveData<String> gitIgnoreContent = _gitIgnoreContent;

  private final MutableLiveData<List<BlameInfo>> _blameResult = new MutableLiveData<>();
  public final LiveData<List<BlameInfo>> blameResult = _blameResult;

  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private final MutableLiveData<String> _selectedDiffFile = new MutableLiveData<>();
  public final LiveData<String> selectedDiffFile = _selectedDiffFile;

  private final MutableLiveData<String> _currentRepoPath = new MutableLiveData<>();
  public final LiveData<String> currentRepoPath = _currentRepoPath;

  private final MutableLiveData<String> _commitDiff = new MutableLiveData<>();
  public final LiveData<String> commitDiff = _commitDiff;

  private final MutableLiveData<Boolean> _commitCompleted = new MutableLiveData<>();
  public final LiveData<Boolean> commitCompleted = _commitCompleted;

  private final MutableLiveData<OperationResult> _gitInitResult = new MutableLiveData<>();
  public final LiveData<OperationResult> gitInitResult = _gitInitResult;

  private GitManager replaceManager(GitManager next) {
    if (gitManager != null) gitManager.close();
    return gitManager = next;
  }

  public void setUserConfig(String name, String email) {
    executor.execute(
        () -> {
          boolean success = gitManager != null && gitManager.setUserConfig(name, email);
          _operationResult.postValue(
              new OperationResult(
                  success, success ? "User config updated" : "Failed to update user config"));
        });
  }

  public String[] getUserConfig() {
    return gitManager != null ? gitManager.getUserConfig() : null;
  }

  public void checkRepositoryStatus(String projectPath) {
    executor.execute(
        () -> {
          replaceManager(new GitManager(projectPath));
          boolean initialized = gitManager.isRepositoryInitialized();
          _isRepositoryInitialized.postValue(initialized);

          if (initialized) {
            boolean opened = gitManager.openRepository();
            if (opened) {
              _repositoryStatus.postValue(RepositoryStatus.OPENED);
              refreshAll();
            }
          }
        });
  }

  public void initializeRepository(String path, String initialBranch) {
    _progressMessage.postValue("Initializing repository...");
    executor.execute(
        () -> {
          replaceManager(new GitManager(path));
          boolean success = gitManager.initRepository(initialBranch);
          if (success) {
            gitManager.openRepository();
            _repositoryStatus.postValue(RepositoryStatus.INITIALIZED);
            _currentRepoPath.postValue(path);
            refreshAll();
          } else {
            _repositoryStatus.postValue(RepositoryStatus.ERROR);
          }
          _progressMessage.postValue(null);
        });
  }

  public void setSelectedDiffFile(String path) {
    _selectedDiffFile.setValue(path);
  }

  public void initializeRepository(String path) {
    initializeRepository(path, "main");
  }

  public void openExistingRepository(String path) {
    _progressMessage.postValue("Opening repository...");
    executor.execute(
        () -> {
          replaceManager(new GitManager(path));
          boolean success = gitManager.openRepository();
          if (success) {
            _repositoryStatus.postValue(RepositoryStatus.OPENED);
            _currentRepoPath.postValue(path);
            refreshAll();
          } else {
            _repositoryStatus.postValue(RepositoryStatus.ERROR);
          }
          _progressMessage.postValue(null);
        });
  }

  /**
   * یک مخزن گیت از نو با لینک گیت‌هاب می‌سازد. معادل دستورهای صفحه "Quick setup" گیت‌هاب:
   * ساخت README.md (فقط اگر هیچ فایل .md دیگری در پوشه نباشد)، git init، git add README.md،
   * git commit -m &lt;پیام کاربر&gt;, git branch -M main، git remote add origin و
   * git push -u origin main. فقط README/فایل مارک‌داون موجود staged می‌شود و بقیه فایل‌های کاربر
   * دست‌نخورده می‌مانند. اگر توکن وارد نشود، ساخت محلی و کامیت انجام می‌شود ولی push اسکیپ
   * می‌شود تا کاربر بعداً از تب Remotes انجامش بدهد.
   */
  public void gitInitFromRemote(
      String path,
      String commitMessage,
      String remoteUrl,
      String token,
      String userName,
      String userEmail) {
    if (path == null || path.trim().isEmpty() || remoteUrl == null || remoteUrl.trim().isEmpty()) {
      _gitInitResult.postValue(new OperationResult(false, "Path and repository URL are required"));
      return;
    }
    if (commitMessage == null || commitMessage.trim().isEmpty()) {
      _gitInitResult.postValue(new OperationResult(false, "Commit message is required"));
      return;
    }
    final String message = commitMessage.trim();
    _progressMessage.postValue("Initializing repository...");
    executor.execute(
        () -> {
          OperationResult result;
          try {
            replaceManager(new GitManager(path));
            if (gitManager.isRepositoryInitialized()) {
              _progressMessage.postValue(null);
              _gitInitResult.postValue(
                  new OperationResult(false, "This folder is already a git repository"));
              return;
            }

            _progressMessage.postValue("Running git init...");
            if (!gitManager.initRepository("main")
                || !gitManager.openRepository()) {
              _progressMessage.postValue(null);
              _gitInitResult.postValue(new OperationResult(false, "Failed to run git init"));
              return;
            }

            if (userName != null
                && userEmail != null
                && !userName.trim().isEmpty()
                && !userEmail.trim().isEmpty()) {
              gitManager.setUserConfig(userName.trim(), userEmail.trim());
            }

            String readmeFile = chooseReadmeFile(path, remoteUrl);
            _progressMessage.postValue("Staging " + readmeFile + "...");
            OperationResult stage = gitManager.stageFile(readmeFile);
            if (!stage.isSuccess()) {
              _progressMessage.postValue(null);
              _gitInitResult.postValue(new OperationResult(false, stage.getMessage()));
              return;
            }

            _progressMessage.postValue("Creating commit...");
            OperationResult commit = gitManager.commit(message, userName, userEmail);
            if (!commit.isSuccess()) {
              _progressMessage.postValue(null);
              _gitInitResult.postValue(new OperationResult(false, commit.getMessage()));
              return;
            }

            gitManager.renameCurrentBranchTo("main");

            if (!gitManager.hasRemote()) {
              gitManager.addRemote("origin", remoteUrl.trim());
            }

            boolean pushed = false;
            if (token != null && !token.trim().isEmpty()) {
              _progressMessage.postValue("Pushing to GitHub...");
              PushResult push = gitManager.push("origin", "main", "oauth2", token.trim());
              pushed = push.isSuccess();
              if (pushed) {
                gitManager.setUpstream("origin", "main");
              }
              result =
                  new OperationResult(
                      pushed,
                      pushed
                          ? "Repository created, committed and pushed successfully"
                          : "Repository created and committed locally, but push failed:\n"
                              + push.getMessage());
            } else {
              result =
                  new OperationResult(
                      true,
                      "Repository created and committed locally. Log in to GitHub and use the Remotes tab to push.");
            }

            _repositoryStatus.postValue(RepositoryStatus.OPENED);
            _currentRepoPath.postValue(path);
            refreshAll();
            _progressMessage.postValue(null);
            _gitInitResult.postValue(result);
          } catch (Exception e) {
            e.printStackTrace();
            _progressMessage.postValue(null);
            _gitInitResult.postValue(
                new OperationResult(
                    false,
                    "Git init failed: "
                        + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName())));
          }
        });
  }

  /**
   * اگر README.md یا هر فایل .md دیگری در مسیر موجود باشد آن را دست‌نخورده برمی‌گرداند؛ در غیر این
   * صورت یک README.md جدید (فقط نام مخزن) می‌سازد. هرگز فایل‌های موجود کاربر را تغییر نمی‌دهد.
   */
  private static String chooseReadmeFile(String path, String remoteUrl) {
    File readme = new File(path, "README.md");
    if (readme.exists() && readme.isFile()) return "README.md";

    File[] files = new File(path).listFiles();
    if (files != null) {
      for (File f : files) {
        if (f.isFile() && f.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".md")) {
          return f.getName();
        }
      }
    }

    try (FileOutputStream fos = new FileOutputStream(readme)) {
      fos.write(("# " + repoNameFromUrl(remoteUrl) + "\n").getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "README.md";
  }

  private static String repoNameFromUrl(String url) {
    String u = url.trim().replaceAll("/+$", "");
    if (u.endsWith(".git")) u = u.substring(0, u.length() - 4);
    int slash = Math.max(u.lastIndexOf('/'), u.lastIndexOf('\\'));
    String name = slash >= 0 ? u.substring(slash + 1) : u;
    return name.isEmpty() ? "My Repo" : name;
  }

  public void cloneRepository(
      String remoteUrl, String localPath, String username, String password) {
    _progressMessage.postValue("Cloning repository...");
    executor.execute(
        () -> {
          replaceManager(new GitManager(localPath));
          boolean success = gitManager.clone(remoteUrl, localPath, username, password);
          if (success) {
            _repositoryStatus.postValue(RepositoryStatus.INITIALIZED);
            refreshAll();
          } else {
            _repositoryStatus.postValue(RepositoryStatus.ERROR);
            _operationResult.postValue(new OperationResult(false, "Failed to clone repository"));
          }
          _progressMessage.postValue(null);
        });
  }

  public void cloneRepository(String remoteUrl, String localPath) {
    cloneRepository(remoteUrl, localPath, null, null);
  }

  public void stageFile(String filePath) {
    _progressMessage.postValue("Staging file...");
    executor.execute(
        () -> {
          OperationResult result =
              gitManager != null
                  ? gitManager.stageFile(filePath)
                  : new OperationResult(false, "Git manager not initialized");
          _operationResult.postValue(result);
          if (result.isSuccess()) refreshChangedFiles();
          _progressMessage.postValue(null);
        });
  }

  public void stageAllFiles() {
    _progressMessage.postValue("Staging all files...");
    executor.execute(
        () -> {
          OperationResult result =
              gitManager != null
                  ? gitManager.stageAllFiles()
                  : new OperationResult(false, "Git manager not initialized");
          _operationResult.postValue(result);
          if (result.isSuccess()) refreshChangedFiles();
          _progressMessage.postValue(null);
        });
  }

  public void unstageFile(String filePath) {
    _progressMessage.postValue("Unstaging file...");
    executor.execute(
        () -> {
          OperationResult result =
              gitManager != null
                  ? gitManager.unstageFile(filePath)
                  : new OperationResult(false, "Git manager not initialized");
          _operationResult.postValue(result);
          if (result.isSuccess()) refreshChangedFiles();
          _progressMessage.postValue(null);
        });
  }

  public void discardChanges(String filePath) {
    _progressMessage.postValue("Discarding changes...");
    executor.execute(
        () -> {
          OperationResult result =
              gitManager != null
                  ? gitManager.discardChanges(filePath)
                  : new OperationResult(false, "Git manager not initialized");
          _operationResult.postValue(result);
          if (result.isSuccess()) refreshChangedFiles();
          _progressMessage.postValue(null);
        });
  }

  public void commit(String message, String author, String email) {
    _progressMessage.postValue("Committing changes...");
    executor.execute(
        () -> {
          OperationResult result =
              gitManager != null
                  ? gitManager.commit(message, author, email)
                  : new OperationResult(false, "Git manager not initialized");
          _operationResult.postValue(result);
          if (result.isSuccess()) {
            refreshChangedFiles();
            refreshCommitHistory();
            _commitCompleted.postValue(true);
          }
          _progressMessage.postValue(null);
        });
  }

  public void commit(String message) {
    executor.execute(
        () -> {
          OperationResult result =
              gitManager != null
                  ? gitManager.commit(message)
                  : new OperationResult(false, "Git manager not initialized");
          _operationResult.postValue(result);
          if (result.isSuccess()) {
            refreshChangedFiles();
            refreshCommitHistory();
            _commitCompleted.postValue(true);
          }
        });
  }

  public void createBranch(String branchName) {
    _progressMessage.postValue("Creating branch...");
    executor.execute(
        () -> {
          boolean success = gitManager != null && gitManager.createBranch(branchName);
          _operationResult.postValue(
              new OperationResult(success, success ? "Branch created" : "Failed to create branch"));
          if (success) refreshBranches();
          _progressMessage.postValue(null);
        });
  }

  public void checkoutBranch(String branchName) {
    _progressMessage.postValue("Switching branch...");
    executor.execute(
        () -> {
          boolean success = gitManager != null && gitManager.checkoutBranch(branchName);
          _operationResult.postValue(
              new OperationResult(
                  success, success ? "Switched to " + branchName : "Failed to checkout branch"));
          if (success) refreshAll();
          _progressMessage.postValue(null);
        });
  }

  public void deleteBranch(String branchName) {
    _progressMessage.postValue("Deleting branch...");
    executor.execute(
        () -> {
          boolean success = gitManager != null && gitManager.deleteBranch(branchName);
          _operationResult.postValue(
              new OperationResult(success, success ? "Branch deleted" : "Failed to delete branch"));
          if (success) refreshBranches();
          _progressMessage.postValue(null);
        });
  }

  public void refreshRemotes() {
    executor.execute(
        () -> {
          List<RemoteInfo> remotesList =
              gitManager != null ? gitManager.getRemotes() : Collections.emptyList();
          _remotes.postValue(remotesList);
        });
  }

  public void addRemote(String name, String url) {
    executor.execute(
        () -> {
          boolean success = gitManager != null && gitManager.addRemote(name, url);
          _operationResult.postValue(
              new OperationResult(
                  success, success ? "Remote added successfully" : "Failed to add remote"));
          if (success) refreshRemotes();
        });
  }

  public void removeRemote(String name) {
    executor.execute(
        () -> {
          boolean success = gitManager != null && gitManager.removeRemote(name);
          _operationResult.postValue(
              new OperationResult(success, success ? "Remote removed" : "Failed to remove remote"));
          if (success) refreshRemotes();
        });
  }

  public void push(String remoteName, String branchName, String username, String password) {
    _progressMessage.postValue("Pushing to remote...");
    executor.execute(
        () -> {
          PushResult result =
              gitManager != null
                  ? gitManager.push(remoteName, branchName, username, password)
                  : new PushResult(false, "Git manager not initialized");
          _pushPullResult.postValue(
              new RemoteOperationResult(result.isSuccess(), result.getMessage(), "push"));
          _progressMessage.postValue(null);
        });
  }

  public void push() {
    push("origin", null, null, null);
  }

  public void pull(String remoteName, String branchName, String username, String password) {
    _progressMessage.postValue("Pulling from remote...");
    executor.execute(
        () -> {
          PullResult result =
              gitManager != null
                  ? gitManager.pull(remoteName, branchName, username, password)
                  : new PullResult(false, "Git manager not initialized");
          _pushPullResult.postValue(
              new RemoteOperationResult(result.isSuccess(), result.getMessage(), "pull"));
          if (result.isSuccess()) refreshAll();
          _progressMessage.postValue(null);
        });
  }

  public void pull() {
    pull("origin", null, null, null);
  }

  public void fetch(String remoteName, String username, String password) {
    _progressMessage.postValue("Fetching from remote...");
    executor.execute(
        () -> {
          FetchResult result =
              gitManager != null
                  ? gitManager.fetch(remoteName, username, password)
                  : new FetchResult(false, "Git manager not initialized");
          _pushPullResult.postValue(
              new RemoteOperationResult(result.isSuccess(), result.getMessage(), "fetch"));
          if (result.isSuccess()) refreshCommitHistory();
          _progressMessage.postValue(null);
        });
  }

  public void fetch() {
    fetch("origin", null, null);
  }

  public void refreshAll() {
    refreshChangedFiles();
    refreshCommitHistory();
    refreshBranches();
    refreshRemotes();
  }

  public void refreshChangedFiles() {
    executor.execute(
        () -> {
          List<FileChange> changes =
              gitManager != null ? gitManager.getChangedFiles() : Collections.emptyList();
          _changedFiles.postValue(changes);
        });
  }

  public void refreshCommitHistory() {
    executor.execute(
        () -> {
          List<CommitInfo> commits =
              gitManager != null ? gitManager.getCommitHistory() : Collections.emptyList();
          _commitHistory.postValue(commits);
        });
  }

  public void loadCommitDiff(String commitHash) {
    executor.execute(
        () -> {
          String diff = gitManager != null ? gitManager.getCommitDiff(commitHash) : "";
          _commitDiff.postValue(diff);
        });
  }

  public void refreshBranches() {
    executor.execute(
        () -> {
          String current = gitManager != null ? gitManager.getCurrentBranch() : null;
          _currentBranch.postValue(current != null ? current : "");

          List<String> allBranches =
              gitManager != null ? gitManager.getAllBranches() : Collections.emptyList();
          _branches.postValue(allBranches);
        });
  }

  // ─────────────────────────── STASH ───────────────────────────

  public void stashSave(String message) {
    _progressMessage.postValue("Saving stash...");
    executor.execute(
        () -> {
          OperationResult result =
              gitManager != null
                  ? gitManager.stashSave(message)
                  : new OperationResult(false, "Git manager not initialized");
          _operationResult.postValue(result);
          if (result.isSuccess()) {
            refreshChangedFiles();
            refreshStashList();
          }
          _progressMessage.postValue(null);
        });
  }

  public void refreshStashList() {
    executor.execute(
        () -> {
          List<StashInfo> list =
              gitManager != null ? gitManager.getStashList() : Collections.emptyList();
          _stashList.postValue(list);
        });
  }

  public void stashApply(int index) {
    _progressMessage.postValue("Applying stash...");
    executor.execute(
        () -> {
          OperationResult result =
              gitManager != null
                  ? gitManager.stashApply(index)
                  : new OperationResult(false, "Git manager not initialized");
          _operationResult.postValue(result);
          if (result.isSuccess()) refreshChangedFiles();
          _progressMessage.postValue(null);
        });
  }

  public void stashPop(int index) {
    _progressMessage.postValue("Popping stash...");
    executor.execute(
        () -> {
          OperationResult result =
              gitManager != null
                  ? gitManager.stashPop(index)
                  : new OperationResult(false, "Git manager not initialized");
          _operationResult.postValue(result);
          if (result.isSuccess()) {
            refreshChangedFiles();
            refreshStashList();
          }
          _progressMessage.postValue(null);
        });
  }

  public void stashDrop(int index) {
    _progressMessage.postValue("Dropping stash...");
    executor.execute(
        () -> {
          OperationResult result =
              gitManager != null
                  ? gitManager.stashDrop(index)
                  : new OperationResult(false, "Git manager not initialized");
          _operationResult.postValue(result);
          if (result.isSuccess()) refreshStashList();
          _progressMessage.postValue(null);
        });
  }

  // ─────────────────────────── MERGE ───────────────────────────

  public void mergeBranch(String branchName) {
    _progressMessage.postValue("Merging " + branchName + "...");
    executor.execute(
        () -> {
          OperationResult result =
              gitManager != null
                  ? gitManager.mergeBranch(branchName)
                  : new OperationResult(false, "Git manager not initialized");
          _operationResult.postValue(result);
          if (result.isSuccess()) refreshAll();
          else if (result.getMessage().contains("conflict")) refreshConflictFiles();
          _progressMessage.postValue(null);
        });
  }

  // ─────────────────────────── REBASE ───────────────────────────

  public void rebaseBranch(String branchName) {
    _progressMessage.postValue("Rebasing onto " + branchName + "...");
    executor.execute(
        () -> {
          OperationResult result =
              gitManager != null
                  ? gitManager.rebaseBranch(branchName)
                  : new OperationResult(false, "Git manager not initialized");
          _operationResult.postValue(result);
          if (result.isSuccess()) refreshAll();
          else refreshChangedFiles();
          _progressMessage.postValue(null);
        });
  }

  public void abortRebase() {
    _progressMessage.postValue("Aborting rebase...");
    executor.execute(
        () -> {
          OperationResult result =
              gitManager != null
                  ? gitManager.abortRebase()
                  : new OperationResult(false, "Git manager not initialized");
          _operationResult.postValue(result);
          if (result.isSuccess()) refreshAll();
          _progressMessage.postValue(null);
        });
  }

  // ─────────────────────────── CONFLICT RESOLVER ───────────────────────────

  public void refreshConflictFiles() {
    executor.execute(
        () -> {
          List<ConflictFile> list =
              gitManager != null ? gitManager.getConflictFiles() : Collections.emptyList();
          _conflictFiles.postValue(list);
        });
  }

  public void resolveConflictWithOurs(String path) {
    executor.execute(
        () -> {
          OperationResult result =
              gitManager != null
                  ? gitManager.resolveConflictWithOurs(path)
                  : new OperationResult(false, "Git manager not initialized");
          _operationResult.postValue(result);
          if (result.isSuccess()) {
            refreshChangedFiles();
            refreshConflictFiles();
          }
        });
  }

  public void resolveConflictWithTheirs(String path) {
    executor.execute(
        () -> {
          OperationResult result =
              gitManager != null
                  ? gitManager.resolveConflictWithTheirs(path)
                  : new OperationResult(false, "Git manager not initialized");
          _operationResult.postValue(result);
          if (result.isSuccess()) {
            refreshChangedFiles();
            refreshConflictFiles();
          }
        });
  }

  public void resolveConflictWithCustom(String path, String content) {
    executor.execute(
        () -> {
          OperationResult result =
              gitManager != null
                  ? gitManager.resolveConflictWithCustom(path, content)
                  : new OperationResult(false, "Git manager not initialized");
          _operationResult.postValue(result);
          if (result.isSuccess()) {
            refreshChangedFiles();
            refreshConflictFiles();
          }
        });
  }

  // ─────────────────────────── BLAME ───────────────────────────

  public void loadBlame(String filePath) {
    _progressMessage.postValue("Loading blame...");
    executor.execute(
        () -> {
          List<BlameInfo> list =
              gitManager != null ? gitManager.getBlame(filePath) : Collections.emptyList();
          _blameResult.postValue(list);
          _progressMessage.postValue(null);
        });
  }

  // ─────────────────────────── RESET ───────────────────────────

  public void reset(ResetMode mode, int stepsBack) {
    _progressMessage.postValue("Resetting…");
    executor.execute(
        () -> {
          OperationResult result =
              gitManager != null
                  ? gitManager.reset(mode, stepsBack)
                  : new OperationResult(false, "Git manager not initialized");
          _operationResult.postValue(result);
          if (result.isSuccess()) refreshAll();
          _progressMessage.postValue(null);
        });
  }

  // ─────────────────────────── TAGS ───────────────────────────

  public void refreshTags() {
    executor.execute(
        () -> {
          List<TagInfo> list = gitManager != null ? gitManager.getTags() : Collections.emptyList();
          _tags.postValue(list);
        });
  }

  public void createTag(String name, String message) {
    _progressMessage.postValue("Creating tag…");
    executor.execute(
        () -> {
          OperationResult result =
              gitManager != null
                  ? gitManager.createTag(name, message)
                  : new OperationResult(false, "Git manager not initialized");
          _operationResult.postValue(result);
          if (result.isSuccess()) refreshTags();
          _progressMessage.postValue(null);
        });
  }

  public void deleteTag(String name) {
    executor.execute(
        () -> {
          OperationResult result =
              gitManager != null
                  ? gitManager.deleteTag(name)
                  : new OperationResult(false, "Git manager not initialized");
          _operationResult.postValue(result);
          if (result.isSuccess()) refreshTags();
        });
  }

  // ─────────────────────────── GITIGNORE ───────────────────────────

  public void loadGitIgnore() {
    executor.execute(
        () -> {
          String content = gitManager != null ? gitManager.readGitIgnore() : "";
          _gitIgnoreContent.postValue(content);
        });
  }

  public void saveGitIgnore(String content) {
    executor.execute(
        () -> {
          OperationResult result =
              gitManager != null
                  ? gitManager.saveGitIgnore(content)
                  : new OperationResult(false, "Git manager not initialized");
          _operationResult.postValue(result);
        });
  }

  public void addToGitIgnore(String pattern) {
    executor.execute(
        () -> {
          OperationResult result =
              gitManager != null
                  ? gitManager.addToGitIgnore(pattern)
                  : new OperationResult(false, "Git manager not initialized");
          _operationResult.postValue(result);
          if (result.isSuccess()) loadGitIgnore();
        });
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    executor.shutdownNow();
    if (gitManager != null) gitManager.close();
  }
}
