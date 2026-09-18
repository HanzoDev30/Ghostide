package ir.hanzodev1375.ghostide.jgit.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.textfield.TextInputLayout;
import ir.hanzodev1375.components.views.GhostToast;
import ir.hanzodev1375.ghostide.codeeditors.setting.PreferencesUtils;
import ir.hanzodev1375.ghostide.jgit.GitHubClient;
import ir.hanzodev1375.ghostide.jgit.R;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.datamanager.GitViewModel;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.model.OperationResult;
import ir.hanzodev1375.components.sheet.customitemsheet.ui.DialogCompat;
import ir.theme.M3Theme;


public class GitInitDialogFragment extends DialogFragment {

  private static final String ARG_PATH = "repo_path";

  public interface OnGitInitListener {
    void onGitInitFinished(boolean success);
  }

  private GitViewModel viewModel;
  private EditText etUrl;
  private EditText etCommit;
  private TextView tvInfo;
  private TextView tvStatus;
  private Button positiveButton;
  private boolean running = false;
  private GitHubClient gitHubClient;
  private OnGitInitListener onGitInitListener;

  public static GitInitDialogFragment newInstance(String repoPath) {
    GitInitDialogFragment fragment = new GitInitDialogFragment();
    Bundle args = new Bundle();
    args.putString(ARG_PATH, repoPath);
    fragment.setArguments(args);
    return fragment;
  }

  public void setOnGitInitListener(OnGitInitListener listener) {
    this.onGitInitListener = listener;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    gitHubClient = new GitHubClient(requireContext());
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    View view = requireActivity().getLayoutInflater().inflate(R.layout.dialog_git_init, null);
    etUrl = view.findViewById(R.id.etRepoUrl);
    etCommit = view.findViewById(R.id.etCommitMessage);
    tvInfo = view.findViewById(R.id.tvInitInfo);
    tvStatus = view.findViewById(R.id.tvInitStatus);
    M3Theme.text(tvInfo, etUrl, etCommit, tvStatus);
    M3Theme.input((TextInputLayout) view.findViewById(R.id.tilRepoUrl));
    M3Theme.input((TextInputLayout) view.findViewById(R.id.tilCommitMessage));

    DialogCompat builder = new DialogCompat(requireActivity());
    builder.setTitle(R.string.git_init_title);
    builder.setView(view);
    builder.setPositiveButton(R.string.git_init_start, null);
    builder.setNegativeButton(R.string.cancel, null);

    final var dialog = builder.create();
    dialog.setOnShowListener(
        d -> {
          positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
          positiveButton.setOnClickListener(v -> startInit());
        });
    return dialog;
  }

  @Override
  public void onStart() {
    super.onStart();
    viewModel = new ViewModelProvider(requireActivity()).get(GitViewModel.class);
    viewModel.gitInitResult.observe(
        this,
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
            dismiss();
            if (onGitInitListener != null) onGitInitListener.onGitInitFinished(true);
          }
        });
  }

  private void startInit() {
    if (running) return;
    String url = etUrl.getText().toString().trim();
    if (url.isEmpty()) {
      String required = getString(R.string.git_init_url_required);
      tvStatus.setVisibility(View.VISIBLE);
      tvStatus.setText(required);
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
    if (positiveButton != null) positiveButton.setEnabled(enabled);
  }
}
