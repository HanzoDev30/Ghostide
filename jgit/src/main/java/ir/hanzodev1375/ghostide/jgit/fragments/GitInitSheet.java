package ir.hanzodev1375.ghostide.jgit.fragments;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import ir.hanzodev1375.components.sheet.BaseBlurBottomSheet;
import ir.hanzodev1375.components.views.ButtonProgress;
import ir.hanzodev1375.components.views.GhostToast;
import ir.hanzodev1375.ghostide.codeeditors.setting.PreferencesUtils;
import ir.hanzodev1375.ghostide.jgit.GitHubClient;
import ir.hanzodev1375.ghostide.jgit.R;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.datamanager.GitViewModel;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.model.OperationResult;
import ir.theme.M3Theme;

public class GitInitSheet extends BaseBlurBottomSheet {

  private static final String ARG_PATH = "repo_path";

  public interface OnGitInitListener {
    void onGitInitFinished(boolean success);
  }

  private GitViewModel viewModel;
  private EditText etUrl;
  private EditText etCommit;
  private TextView tvInfo;
  private TextView tvStatus;
  private ButtonProgress bpStart;
  private MaterialButton btnCancel;
  private boolean running = false;
  private GitHubClient gitHubClient;
  private OnGitInitListener onGitInitListener;

  public static GitInitSheet newInstance(String repoPath) {
    GitInitSheet sheet = new GitInitSheet();
    Bundle args = new Bundle();
    args.putString(ARG_PATH, repoPath);
    sheet.setArguments(args);
    return sheet;
  }

  public void setOnGitInitListener(OnGitInitListener listener) {
    this.onGitInitListener = listener;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    gitHubClient = new GitHubClient(requireContext());
    viewModel = new ViewModelProvider(requireActivity()).get(GitViewModel.class);
  }

  @Override
  protected void onContentReady(ViewGroup contentContainer) {
    View view =
        getLayoutInflater().inflate(R.layout.bottom_sheet_git_init, contentContainer, false);
    contentContainer.addView(view);

    etUrl = view.findViewById(R.id.etRepoUrl);
    etCommit = view.findViewById(R.id.etCommitMessage);
    tvInfo = view.findViewById(R.id.tvInitInfo);
    tvStatus = view.findViewById(R.id.tvInitStatus);
    bpStart = view.findViewById(R.id.bpStart);
    btnCancel = view.findViewById(R.id.btnCancel);

    M3Theme.text(tvInfo, etUrl, etCommit, tvStatus);
    M3Theme.input((TextInputLayout) view.findViewById(R.id.tilRepoUrl));
    M3Theme.input((TextInputLayout) view.findViewById(R.id.tilCommitMessage));
    M3Theme.button(btnCancel);
    M3Theme.apply(view);

    bpStart.setText(R.string.git_init_start);
    bpStart.setOnClickListener(v -> startInit());
    btnCancel.setOnClickListener(v -> dismiss());

    viewModel.gitInitResult.observe(
        getViewLifecycleOwner(),
        result -> {
          if (!running || result == null) return;
          running = false;
          setInputEnabled(true);
          String message = result.getMessage();
          tvStatus.setVisibility(View.VISIBLE);
          tvStatus.setText(message != null ? message : "");
          GhostToast.makeText(
                  requireContext(), message != null ? message : "Done", GhostToast.LENGTH_LONG)
              .show();
          if (result.isSuccess()) {
            bpStart.finishLoading(
                () -> {
                  if (isAdded()) dismiss();
                  if (onGitInitListener != null) onGitInitListener.onGitInitFinished(true);
                });
          } else {
            bpStart.stopLoading();
          }
        });

    viewModel.progressMessage.observe(
        getViewLifecycleOwner(),
        msg -> {
          if (msg == null) return;
          tvStatus.setVisibility(View.VISIBLE);
          tvStatus.setText(msg);
        });
  }

  private void startInit() {
    if (running) return;
    String url = etUrl.getText().toString().trim();
    if (url.isEmpty()) {
      tvStatus.setVisibility(View.VISIBLE);
      tvStatus.setText(R.string.git_init_url_required);
      return;
    }
    String path = getArguments() != null ? getArguments().getString(ARG_PATH) : null;
    if (path == null) {
      tvStatus.setVisibility(View.VISIBLE);
      tvStatus.setText("Paths is empty");
      return;
    }

    String commitMessage = etCommit.getText().toString().trim();
    if (commitMessage.isEmpty()) {
      tvStatus.setVisibility(View.VISIBLE);
      tvStatus.setText(R.string.git_init_commit_required);
      return;
    }

    running = true;
    setInputEnabled(false);
    tvStatus.setVisibility(View.VISIBLE);
    tvStatus.setText(R.string.git_init_working);
    bpStart.startLoading();

    String token = gitHubClient.getToken();
    String userName = null;
    String userEmail = null;
    PreferencesUtils prefs = new PreferencesUtils(requireContext());
    if (prefs.hasGitLocalUserConfig()) {
      userName = prefs.getGitLocalUserName();
      userEmail = prefs.getGitLocalUserEmail();
    }
    viewModel.gitInitFromRemote(path, commitMessage, url, token, userName, userEmail);
  }

  private void setInputEnabled(boolean enabled) {
    etUrl.setEnabled(enabled);
    etCommit.setEnabled(enabled);
    btnCancel.setEnabled(enabled);
  }
}