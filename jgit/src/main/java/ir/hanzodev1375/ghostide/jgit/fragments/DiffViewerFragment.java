package ir.hanzodev1375.ghostide.jgit.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import ir.hanzodev1375.ghostide.jgit.R;
import ir.hanzodev1375.ghostide.jgit.diff.GitDiffViewer;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.datamanager.GitViewModel;
import ir.theme.M3Theme;

public class DiffViewerFragment extends Fragment {

  private GitDiffViewer diffViewer;
  private ProgressBar progressBar;
  private TextView emptyText;
  private GitViewModel viewModel;
  /** متنی که همین الان روی diffViewer نشسته؛ برای جلوگیری از parse دوباره‌ی همان متن. */
  private String shownDiff;
  /** true یعنی لیست تغییرات عوض شده و diff قبلی کهنه است. */
  private boolean diffStale = true;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_diff_viewer, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    diffViewer = view.findViewById(R.id.diffViewer);
    progressBar = view.findViewById(R.id.progressBar);
    emptyText = view.findViewById(R.id.emptyText);
    viewModel = new ViewModelProvider(requireActivity()).get(GitViewModel.class);
    M3Theme.apply(view);

    shownDiff = null;
    diffStale = true;
    viewModel.changedFiles.observe(getViewLifecycleOwner(), changes -> diffStale = true);
    viewModel.fullDiff.observe(getViewLifecycleOwner(), this::showDiff);
  }

  @Override
  public void onResume() {
    super.onResume();
    if (viewModel.currentRepoPath.getValue() == null) {
      showEmptyState("Repository path not found");
      return;
    }
    // قبلاً با هر بار رسیدن به این تب کل diff دوباره ساخته، parse و هایلایت می‌شد حتی اگر هیچ
    // چیز عوض نشده بود. حالا فقط وقتی لیست تغییرات عوض شده یا هنوز چیزی نشان نداده‌ایم.
    if (!diffStale && shownDiff != null) return;
    diffStale = false;
    if (shownDiff == null) {
      progressBar.setVisibility(View.VISIBLE);
      diffViewer.setVisibility(View.GONE);
    }
    viewModel.loadFullDiff();
  }

  @Override
  public void onDestroyView() {
    shownDiff = null;
    super.onDestroyView();
  }

  private void showDiff(String diff) {
    progressBar.setVisibility(View.GONE);
    if (diff == null || diff.isEmpty() || "No changes detected.".equals(diff)) {
      shownDiff = null;
      showEmptyState("No changes to display");
      return;
    }
    if (diff.equals(shownDiff)) {
      diffViewer.setVisibility(View.VISIBLE);
      return;
    }
    shownDiff = diff;
    emptyText.setVisibility(View.GONE);
    diffViewer.setVisibility(View.VISIBLE);
    diffViewer.applyMaterial3();
    diffViewer.setDiffText(diff);
  }

  private void showEmptyState(String message) {
    progressBar.setVisibility(View.GONE);
    diffViewer.setVisibility(View.GONE);
    emptyText.setVisibility(View.VISIBLE);
    emptyText.setText(message);
  }
}
