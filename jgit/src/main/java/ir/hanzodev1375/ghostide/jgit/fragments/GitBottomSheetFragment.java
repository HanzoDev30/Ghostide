package ir.hanzodev1375.ghostide.jgit.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import ir.hanzodev1375.components.views.GhostToast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import ir.hanzodev1375.components.sheet.BaseBlurBottomSheet;
import ir.hanzodev1375.components.views.SegmentedAvatarView;
import ir.hanzodev1375.ghostide.codeeditors.setting.PreferencesUtils;
import ir.hanzodev1375.ghostide.jgit.R;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.datamanager.GitViewModel;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.RepositoryStatus;
import ir.hanzodev1375.ghostide.jgit.adapter.ViewPagerAdapter;
import ir.hanzodev1375.ghostide.jgit.model.GitTab;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import ir.hanzodev1375.components.glass.ChipCompat;
import ir.hanzodev1375.components.glass.ChipGroupCompat;
import ir.hanzodev1375.ghostide.jgit.GitHubClient;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.model.RemoteInfo;
import java.util.Locale;
import ir.hanzodev1375.components.sheet.customitemsheet.ui.DialogCompat;
import ir.theme.M3Theme;

public class GitBottomSheetFragment extends BaseBlurBottomSheet {

  private static final int TAB_CHANGES = 0;
  private static final int TAB_HISTORY = 1;
  private static final int TAB_BRANCHES = 2;
  private static final int TAB_REMOTES = 3;

  private GitViewModel viewModel;
  private SegmentedAvatarView avatar;
  private ViewPager2 viewPager;
  private String repoPath;
  private boolean isInitialized = false;
  private boolean statsRequested = false;

  public void setRepoPath(String repoPath) {
    this.repoPath = repoPath;
  }

  public GitBottomSheetFragment() {}

  public static GitBottomSheetFragment newInstance(String repoPath) {
    GitBottomSheetFragment fragment = new GitBottomSheetFragment();
    Bundle args = new Bundle();
    args.putString("repo_path", repoPath);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      repoPath = getArguments().getString("repo_path");
    }
  }

  @Override
  protected void onContentReady(ViewGroup contentContainer) {
    View view = getLayoutInflater().inflate(R.layout.bottom_sheet_git, contentContainer, false);
    contentContainer.addView(view);
    viewModel = new ViewModelProvider(requireActivity()).get(GitViewModel.class);
    setHasPeekMod(false);
    setupHeader(view);
    M3Theme.apply(view);
    viewModel.progressMessage.observe(
        getViewLifecycleOwner(),
        msg -> {
          // The avatar's segmented ring is the only progress indicator in this sheet.
          if (avatar != null) {
            avatar.setLoading(msg != null);
          }
        });

    // تنها جایی که نتیجه‌ی عملیات toast می‌شود. قبلاً هشت فرگمنت جدا observe می‌کردند: با هر ساخته
    // شدن یک تب، آخرین نتیجه‌ی قدیمی دوباره toast می‌شد و چند تب زنده یعنی چند toast همزمان.
    final ir.hanzodev1375.ghostide.jgit.jgitandroid.model.OperationResult staleResult =
        viewModel.operationResult.getValue();
    viewModel.operationResult.observe(
        getViewLifecycleOwner(),
        result -> {
          if (result == null || result == staleResult) return;
          GhostToast.makeText(getContext(), result.getMessage(), GhostToast.LENGTH_SHORT).show();
        });

    viewModel.repositoryStatus.observe(
        getViewLifecycleOwner(),
        status -> {
          if (status == RepositoryStatus.OPENED || status == RepositoryStatus.INITIALIZED) {
            loadRepoStats();
            PreferencesUtils prefsUtils = new PreferencesUtils(requireContext());
            if (prefsUtils.hasGitLocalUserConfig()) {
              viewModel.setUserConfig(
                  prefsUtils.getGitLocalUserName(), prefsUtils.getGitLocalUserEmail());
            } else {
              showUserConfigDialog();
            }
          }
        });

    setupViewPager(view);

    viewModel.commitCompleted.observe(
        getViewLifecycleOwner(),
        completed -> {
          if (Boolean.TRUE.equals(completed) && viewPager != null) {
            viewPager.post(() -> viewPager.setCurrentItem(TAB_REMOTES, true));
          }
        });

    if (!isInitialized && repoPath != null && !repoPath.isEmpty()) {
      File gitDir = new File(repoPath, ".git");
      if (gitDir.exists() && gitDir.isDirectory()) {
        viewModel.openExistingRepository(repoPath);
      } else {
        viewModel.initializeRepository(repoPath);
      }
      isInitialized = true;
    } else if (repoPath == null || repoPath.isEmpty()) {
      GhostToast.makeText(getContext(), "مسیر مخزن تنظیم نشده است!", GhostToast.LENGTH_SHORT)
          .show();
      dismiss();
    }
    M3Theme.applyTopLevel(view);
  }

  private void setupHeader(View root) {
    avatar = root.findViewById(R.id.ivGitAvatar);
    TextView tvUsername = root.findViewById(R.id.tvGitUsername);
    TextView tvRepoName = root.findViewById(R.id.tvGitRepoName);
    ChipGroupCompat chipGroup = root.findViewById(R.id.chipGroupRepoStats);

    PreferencesUtils prefsUtils = new PreferencesUtils(requireContext());
    String username = prefsUtils.getGitHubUsername();
    String avatarUrl = prefsUtils.getGitHubAvatarUrl();

    tvUsername.setText(!TextUtils.isEmpty(username) ? "@" + username : "کاربر مهمان");
    tvRepoName.setText(repoPath != null && !repoPath.isEmpty() ? new File(repoPath).getName() : "");

    if (!TextUtils.isEmpty(avatarUrl)) {
      Glide.with(this).load(avatarUrl).into(avatar);
    }

    // Both counters start on screen at zero rather than hidden. The star count is unknown until
    // the remote request lands, and a chip that pops in once the number arrives shifts the whole
    // header sideways; zero is also the truthful reading for a repo nobody starred yet.
    ChipCompat chipStars = chipGroup.addChip("0");
    chipStars.setTone(ChipCompat.Tone.GOLD);
    chipStars.setIconResource(R.drawable.star_24px);

    ChipCompat chipSize = chipGroup.addChip("0");
    chipSize.setTone(ChipCompat.Tone.GREEN);
  }

  private void loadRepoStats() {
    if (statsRequested) {
      return;
    }
    statsRequested = true;
    loadRepoSize();
    loadRepoStars();
  }

  private static final java.util.concurrent.ConcurrentHashMap<String, long[]> SIZE_CACHE =
      new java.util.concurrent.ConcurrentHashMap<>();
  private static final long SIZE_CACHE_TTL_MS = 60_000L;
  private static final long SIZE_WALK_DELAY_MS = 1_000L;
  private static final int SIZE_WALK_MAX_DEPTH = 64;

  /**
   * اندازه‌ی کل پوشه (همراه .git و build) با پیمایش بازگشتی محاسبه می‌شود؛ روی پروژه‌های بزرگ
   * ده‌ها هزار stat روی دیسک است و قبلاً همزمان با status اولیه‌ی git اجرا می‌شد و I/O را از آن
   * می‌گرفت. حالا: نتیجه کش می‌شود، با تأخیر و با اولویت پس‌زمینه اجرا می‌شود.
   */
  private void loadRepoSize() {
    if (repoPath == null || repoPath.isEmpty()) {
      return;
    }
    View root = getView();
    ChipGroupCompat chipGroup = root == null ? null : root.findViewById(R.id.chipGroupRepoStats);
    if (chipGroup == null || chipGroup.getChildCount() < 2) {
      return;
    }
    ChipCompat chip = (ChipCompat) chipGroup.getChildAt(1);
    final String path = repoPath;

    long[] cached = SIZE_CACHE.get(path);
    if (cached != null) {
      chip.setText(formatSize(cached[0]));
      if (System.currentTimeMillis() - cached[1] < SIZE_CACHE_TTL_MS) {
        return;
      }
    }

    root.postDelayed(
        () -> {
          if (!isAdded()) {
            return;
          }
          Thread worker =
              new Thread(
                  () -> {
                    android.os.Process.setThreadPriority(
                        android.os.Process.THREAD_PRIORITY_BACKGROUND);
                    long bytes = directorySize(new File(path), 0);
                    SIZE_CACHE.put(path, new long[] {bytes, System.currentTimeMillis()});
                    String text = formatSize(bytes);
                    android.app.Activity activity = getActivity();
                    if (activity == null) {
                      return;
                    }
                    activity.runOnUiThread(
                        () -> {
                          if (isAdded() && chipGroup.getChildCount() > 1) {
                            chip.setText(text);
                          }
                        });
                  },
                  "git-repo-size");
          worker.start();
        },
        SIZE_WALK_DELAY_MS);
  }

  private void loadRepoStars() {
    List<RemoteInfo> remotes = viewModel.remotes.getValue();
    if (remotes == null) {
      return;
    }
    for (RemoteInfo remote : remotes) {
      String slug = githubSlug(remote.getFetchUrl());
      if (slug == null) {
        continue;
      }
      new GitHubClient(requireContext())
          .get(
              "https://api.github.com/repos/" + slug,
              new GitHubClient.GitHubRequestCallback() {
                @Override
                public void onSuccess(org.json.JSONObject response) {
                  int stars = response.optInt("stargazers_count", 0);
                  if (getActivity() == null) {
                    return;
                  }
                  requireActivity()
                      .runOnUiThread(
                          () -> {
                            View chipGroupView = getView() == null ? null : getView();
                            if (!isAdded() || chipGroupView == null) {
                              return;
                            }
                            ChipGroupCompat group = chipGroupView.findViewById(R.id.chipGroupRepoStats);
                            if (group == null || group.getChildCount() == 0) {
                              return;
                            }
                            ChipCompat chip = (ChipCompat) group.getChildAt(0);
                            chip.setText(ChipCompat.formatStat(stars));
                          });
                }

                @Override
                public void onFailure(String errorMessage) {
                  // The chip is already showing its zero placeholder; leaving it as-is keeps the
                  // header from moving when the request fails.
                }
              });
      return;
    }
  }

  /** "git@github.com:owner/repo.git" and "https://github.com/owner/repo" both give "owner/repo". */
  private static String githubSlug(String url) {
    if (url == null) {
      return null;
    }
    String path = url.trim();
    int at = path.indexOf('@');
    int scheme = path.indexOf("://");
    if (at >= 0 && (scheme < 0 || at > scheme)) {
      path = path.substring(at + 1);
    } else if (scheme >= 0) {
      path = path.substring(scheme + 3);
    }
    int colon = path.indexOf(':');
    if (colon >= 0) {
      path = path.substring(0, colon) + "/" + path.substring(colon + 1);
    }
    int slash = path.indexOf('/');
    if (slash < 0) {
      return null;
    }
    path = path.substring(slash + 1);
    if (path.endsWith(".git")) {
      path = path.substring(0, path.length() - 4);
    }
    String[] parts = path.split("/");
    return parts.length == 2 && !parts[0].isEmpty() && !parts[1].isEmpty() ? parts[0] + "/" + parts[1] : null;
  }

  private static long directorySize(File dir, int depth) {
    if (dir == null || depth > SIZE_WALK_MAX_DEPTH) {
      return 0L;
    }
    File[] children = dir.listFiles();
    if (children == null) {
      // فایل معمولی (یا پوشه‌ی غیرقابل‌خواندن)
      return dir.isFile() ? dir.length() : 0L;
    }
    long total = 0L;
    for (File child : children) {
      total += child.isDirectory() ? directorySize(child, depth + 1) : child.length();
    }
    return total;
  }

  private static String formatSize(long bytes) {
    if (bytes < 1024L) {
      return bytes + " B";
    }
    double kb = bytes / 1024.0;
    if (kb < 1024.0) {
      return String.format(Locale.getDefault(), "%.1f KB", kb);
    }
    double mb = kb / 1024.0;
    if (mb < 1024.0) {
      return String.format(Locale.getDefault(), "%.1f MB", mb);
    }
    return String.format(Locale.getDefault(), "%.2f GB", mb / 1024.0);
  }

  private void setupViewPager(View root) {
    List<GitTab> tabs = new ArrayList<>();
    tabs.add(new GitTab(getString(R.string.tab_changes), new ChangedFilesFragment()));
    tabs.add(new GitTab(getString(R.string.tab_history), new CommitHistoryFragment()));
    tabs.add(new GitTab(getString(R.string.tab_branches), new BranchesFragment()));
    tabs.add(new GitTab(getString(R.string.tab_remotes), new RemotesFragment()));
    tabs.add(new GitTab(getString(R.string.tab_stash), new StashFragment()));
    tabs.add(new GitTab(getString(R.string.tab_conflicts), new ConflictResolverFragment()));
    tabs.add(new GitTab(getString(R.string.tab_reset), new ResetFragment()));
    tabs.add(new GitTab(getString(R.string.tab_tags), new TagsFragment()));
    tabs.add(new GitTab(getString(R.string.tab_gitignore), new GitignoreFragment()));
    tabs.add(new GitTab(getString(R.string.tab_blame), new BlameFragment()));
    tabs.add(new GitTab(getString(R.string.tab_diff), new DiffViewerFragment()));

    ViewPager2 pager = root.findViewById(R.id.viewPager);
    viewPager = pager;
    // میزبان خودِ شیت است تا با بسته شدنش فرگمنت‌های تب‌ها هم نابود شوند
    ViewPagerAdapter adapter = new ViewPagerAdapter(this, tabs);
    pager.setAdapter(adapter);

    TabLayout tabLayout = root.findViewById(R.id.tabLayout);
    new TabLayoutMediator(
            tabLayout, pager, (tab, position) -> tab.setText(tabs.get(position).getTitle()))
        .attach();
  }

  private void showUserConfigDialog() {
    PreferencesUtils prefsUtils = new PreferencesUtils(requireContext());

    DialogCompat builder = new DialogCompat(requireContext());
    builder.setTitle("Git User Configuration");

    View view = LayoutInflater.from(requireContext()).inflate(R.layout.git_local_config, null);
    EditText etName = view.findViewById(R.id.etGitUserName);
    EditText etEmail = view.findViewById(R.id.etGitUserEmail);

    etName.setText(prefsUtils.getGitLocalUserName());
    etEmail.setText(prefsUtils.getGitLocalUserEmail());

    builder.setView(view);
    builder.setPositiveButton(
        "Save",
        (d, w) -> {
          String name = etName.getText().toString().trim();
          String email = etEmail.getText().toString().trim();
          if (name.isEmpty() || email.isEmpty()) {
            GhostToast.makeText(getContext(), "Name and email required", GhostToast.LENGTH_SHORT)
                .show();
            return;
          }
          prefsUtils.setGitLocalUserName(name);
          prefsUtils.setGitLocalUserEmail(email);
          viewModel.setUserConfig(name, email);
          GhostToast.makeText(getContext(), "Saved", GhostToast.LENGTH_SHORT).show();
        });
    builder.setNegativeButton("Cancel", null);
    builder.show();
  }
}
