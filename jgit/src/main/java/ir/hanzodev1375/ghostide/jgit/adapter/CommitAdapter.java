package ir.hanzodev1375.ghostide.jgit.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import ir.hanzodev1375.ghostide.jgit.R;
import ir.hanzodev1375.ghostide.jgit.jgitandroid.model.CommitInfo;
import ir.theme.M3Theme;

public class CommitAdapter extends ListAdapter<CommitInfo, CommitAdapter.ViewHolder> {

  private static final DiffUtil.ItemCallback<CommitInfo> DIFF =
      new DiffUtil.ItemCallback<CommitInfo>() {
        @Override
        public boolean areItemsTheSame(@NonNull CommitInfo a, @NonNull CommitInfo b) {
          return a.getHash().equals(b.getHash());
        }

        @Override
        public boolean areContentsTheSame(@NonNull CommitInfo a, @NonNull CommitInfo b) {
          return a.equals(b);
        }
      };

  private final SimpleDateFormat sdf =
      new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
  private OnCommitClickListener listener;

  public interface OnCommitClickListener {
    void onClick(CommitInfo commit);
  }

  public CommitAdapter() {
    super(DIFF);
  }

  public void setOnCommitClickListener(OnCommitClickListener listener) {
    this.listener = listener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_commit, parent, false);
    M3Theme.listCard(view);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    CommitInfo commit = getItem(position);
    holder.tvHash.setText(commit.getShortHash());
    holder.tvMessage.setText(commit.getMessage());
    String date = sdf.format(new Date(commit.getTimestamp()));
    holder.tvAuthor.setText(commit.getAuthor() + " - " + date);
    holder.itemView.setOnClickListener(
        v -> {
          if (listener != null) listener.onClick(commit);
        });
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    TextView tvHash, tvMessage, tvAuthor;

    ViewHolder(View itemView) {
      super(itemView);
      tvHash = itemView.findViewById(R.id.tvCommitHash);
      tvMessage = itemView.findViewById(R.id.tvCommitMessage);
      tvAuthor = itemView.findViewById(R.id.tvCommitAuthor);
    }
  }
}
