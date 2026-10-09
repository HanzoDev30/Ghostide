package ir.hanzodev1375.ghostide.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.materialfileicon.core.FileIconHelper;
import ir.hanzodev1375.ghostide.models.TabModel;
import ir.theme.M3Theme;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class OpenFilesAdapter extends RecyclerView.Adapter<OpenFilesAdapter.Holder> {

  public interface OnFileClickListener {
    void onFileClick(int position);
  }

  private final List<TabModel> items = new ArrayList<>();
  private final List<TabModel> visible = new ArrayList<>();
  private OnFileClickListener listener;

  public void setOnFileClickListener(OnFileClickListener l) {
    this.listener = l;
  }

  public void submit(List<TabModel> tabs) {
    items.clear();
    if (tabs != null) {
      items.addAll(tabs);
    }
    filter("");
  }

  public void filter(String query) {
    String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
    visible.clear();
    if (q.isEmpty()) {
      visible.addAll(items);
    } else {
      for (int i = 0, size = items.size(); i < size; i++) {
        TabModel tab = items.get(i);
        String name = tab.getFileName();
        String path = tab.getFilePath();
        if ((name != null && name.toLowerCase(Locale.ROOT).contains(q))
            || (path != null && path.toLowerCase(Locale.ROOT).contains(q))) {
          visible.add(tab);
        }
      }
    }
    notifyDataSetChanged();
  }

  public int getVisibleCount() {
    return visible.size();
  }

  public TabModel getVisibleItem(int position) {
    return position >= 0 && position < visible.size() ? visible.get(position) : null;
  }

  @NonNull
  @Override
  public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.item_open_file, parent, false);
    return new Holder(v);
  }

  @Override
  public void onBindViewHolder(@NonNull Holder holder, int position) {
    TabModel tab = visible.get(position);
    M3Theme.listCard(holder.itemView);
    holder.name.setText(tab.getFileName());
    holder.name.setTextColor(color(M3Theme.onSurface()));
    holder.path.setText(tab.getFilePath());
    holder.path.setTextColor(color(M3Theme.onSurfaceVariant()));
    var iconFile = new FileIconHelper(tab.getFilePath());
    iconFile.bindIcon(holder.icon);
    holder.itemView.setOnClickListener(
        v -> {
          int pos = holder.getBindingAdapterPosition();
          if (listener != null && pos != RecyclerView.NO_POSITION) {
            listener.onFileClick(pos);
          }
        });
  }

  @Override
  public int getItemCount() {
    return visible.size();
  }

  private static int color(Integer value) {
    return value == null ? 0xFFDEDEDE : value;
  }

  static final class Holder extends RecyclerView.ViewHolder {
    final ImageView icon;
    final TextView name;
    final TextView path;

    Holder(@NonNull View itemView) {
      super(itemView);
      icon = itemView.findViewById(R.id.ivOpenFileIcon);
      name = itemView.findViewById(R.id.tvOpenFileName);
      path = itemView.findViewById(R.id.tvOpenFilePath);
    }
  }
}
