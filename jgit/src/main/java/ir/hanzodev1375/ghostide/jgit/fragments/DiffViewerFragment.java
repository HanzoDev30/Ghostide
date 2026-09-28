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
  private String loadedDiff;

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

    viewModel.fullDiff.observe(getViewLifecycleOwner(), this::showDiff);
  }

  @Override
  public void onResume() {
    super.onResume();
    if (viewModel.currentRepoPath.getValue() == null) {
      showEmptyState("Repository path not found");
      return;
    }
    progressBar.setVisibility(View.VISIBLE);
    diffViewer.setVisibility(View.GONE);
    viewModel.loadFullDiff();
  }

  private void showDiff(String diff) {
    progressBar.setVisibility(View.GONE);
    if (diff == null || diff.isEmpty() || "No changes detected.".equals(diff)) {
      showEmptyState("No changes to display");
      return;
    }
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
