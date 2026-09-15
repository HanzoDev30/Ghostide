package ir.hanzodev1375.ghostide.adapters;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.listitem.ListItemCardView;
import com.google.android.material.listitem.ListItemViewHolder;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.models.CommandItem;
import ir.theme.M3Theme;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class CommandAdapter extends RecyclerView.Adapter<CommandAdapter.Holder> {

  public interface OnCommandClickListener {
    void onCommandClick(CommandItem item);
  }

  private final List<CommandItem> all = new ArrayList<>();
  private final List<CommandItem> visible = new ArrayList<>();
  private OnCommandClickListener listener;

  public void setOnCommandClickListener(OnCommandClickListener l) {
    this.listener = l;
  }

  public void submit(List<CommandItem> items) {
    all.clear();
    if (items != null) {
      all.addAll(items);
    }
    filter("");
  }

  public void filter(String query) {
    visible.clear();
    String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
    for (CommandItem item : all) {
      if (q.isEmpty()
          || item.title().toLowerCase(Locale.ROOT).contains(q)
          || item.desc().toLowerCase(Locale.ROOT).contains(q)
          || item.id().toLowerCase(Locale.ROOT).contains(q)) {
        visible.add(item);
      }
    }
    notifyDataSetChanged();
  }

  public int getVisibleCount() {
    return visible.size();
  }

  @NonNull
  @Override
  public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_command, parent, false);
    return new Holder(v);
  }

  @Override
  public void onBindViewHolder(@NonNull Holder holder, int position) {
    CommandItem item = visible.get(position);
    holder.title.setText(item.title());
    holder.desc.setText(item.desc());
    holder.title.setTextColor(item.enabled() ? M3Theme.onSurface() : M3Theme.onSurfaceVariant());
    holder.desc.setTextColor(M3Theme.outlineVariant());
    if (item.iconRes() != 0) {
      holder.icon.setVisibility(View.VISIBLE);
      holder.icon.setImageResource(item.iconRes());
      holder.icon.setColorFilter(
          item.enabled() ? M3Theme.onSurfaceVariant() : M3Theme.outlineVariant());
      GradientDrawable gd = new GradientDrawable();
      gd.setColor(M3Theme.surface());
      gd.setStroke(1, M3Theme.outline());
      gd.setCornerRadius(8);
      holder.icon.setPadding(5, 5, 5, 5);
      holder.icon.setBackground(gd);
    } else {
      holder.icon.setVisibility(View.INVISIBLE);
      GradientDrawable gd = new GradientDrawable();
      gd.setColor(M3Theme.surface());
      gd.setStroke(1, M3Theme.outline());
      gd.setCornerRadius(8);
      holder.icon.setPadding(5, 5, 5, 5);
      holder.icon.setBackground(gd);
    }
    holder.itemView.setAlpha(item.enabled() ? 1f : 0.55f);
    holder.itemView.setOnClickListener(
        v -> {
          if (listener != null && item.enabled()) {
            listener.onCommandClick(item);
          }
        });
    M3Theme.listCard(holder.itemView);
  }

  @Override
  public int getItemCount() {
    return visible.size();
  }

  static final class Holder extends ListItemViewHolder {
    final ImageView icon;
    final TextView title;
    final TextView desc;

    Holder(@NonNull View itemView) {
      super(itemView);
      icon = itemView.findViewById(R.id.ivCommandIcon);
      title = itemView.findViewById(R.id.tvCommandTitle);
      desc = itemView.findViewById(R.id.tvCommandDesc);
    }
  }
}
