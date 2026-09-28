package ir.hanzodev1375.ghostide.jgit.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import ir.hanzodev1375.ghostide.jgit.R;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.ChangeType;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.model.FileChange;
import ir.theme.M3Theme;

public class FileChangeAdapter extends ListAdapter<FileChange, FileChangeAdapter.ViewHolder> {

  private static final DiffUtil.ItemCallback<FileChange> DIFF =
      new DiffUtil.ItemCallback<FileChange>() {
        @Override
        public boolean areItemsTheSame(@NonNull FileChange a, @NonNull FileChange b) {
          return a.getPath().equals(b.getPath());
        }

        @Override
        public boolean areContentsTheSame(@NonNull FileChange a, @NonNull FileChange b) {
          return a.equals(b);
        }
      };

  private OnItemClickListener listener;

  public interface OnItemClickListener {
    void onStageClick(FileChange change);

    void onUnstageClick(FileChange change);

    void onDiscardClick(FileChange change);
  }

  public FileChangeAdapter() {
    super(DIFF);
  }

  public void setOnItemClickListener(OnItemClickListener listener) {
    this.listener = listener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_change, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    FileChange change = getItem(position);
    M3Theme.listCard(holder.itemView);
    holder.tvFileName.setText(change.getPath());
    holder.tvChangeType.setText(labelOf(change.getChangeType()));
    holder.chkStage.setChecked(change.isStaged());
    // باید بعد از listCard اعمال شود چون listCard رنگ متن همه TextView ها را ریست می‌کند
    Integer accent = accentOf(change.getChangeType());
    if (accent != null) holder.tvChangeType.setTextColor(accent);

    holder.itemView.setOnLongClickListener(
        v -> {
          if (listener != null) {
            if (change.isStaged()) listener.onUnstageClick(change);
            else listener.onDiscardClick(change);
          }
          return true;
        });

    holder.itemView.setOnClickListener(
        v -> {
          if (listener != null && !change.isStaged()) {
            listener.onStageClick(change);
          }
        });
  }

  private static String labelOf(ChangeType type) {
    switch (type) {
      case ADDED:
        return "Added";
      case MODIFIED:
        return "Modified";
      case DELETED:
        return "Deleted";
      case CONFLICTING:
        return "Conflict";
      default:
        return "Untracked";
    }
  }

  private static Integer accentOf(ChangeType type) {
    switch (type) {
      case ADDED:
        return M3Theme.tertiary();
      case DELETED:
      case CONFLICTING:
        return M3Theme.error();
      case MODIFIED:
        return M3Theme.primary();
      default:
        return M3Theme.onSurfaceVariant();
    }
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    TextView tvFileName, tvChangeType;
    CheckBox chkStage;

    ViewHolder(View itemView) {
      super(itemView);
      tvFileName = itemView.findViewById(R.id.tvFileName);
      tvChangeType = itemView.findViewById(R.id.tvChangeType);
      chkStage = itemView.findViewById(R.id.chkStage);
    }
  }
}
