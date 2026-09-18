package ir.hanzodev1375.ghostide.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.listitem.ListItemViewHolder;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.materialfileicon.core.langcolor.LanguageColors;
import ir.hanzodev1375.ghostide.snippets.SnippetEntry;
import ir.theme.M3Theme;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class SnippetAdapter extends RecyclerView.Adapter<SnippetAdapter.Holder> {

  public interface OnSnippetActionListener {
    void onInsertSnippet(SnippetEntry entry);

    void onMoreClick(SnippetEntry entry, View anchor);
  }

  private final List<SnippetEntry> items = new ArrayList<>();
  private OnSnippetActionListener listener;

  public void setOnSnippetActionListener(OnSnippetActionListener l) {
    this.listener = l;
  }

  public void submit(List<SnippetEntry> entries) {
    items.clear();
    if (entries != null) {
      items.addAll(entries);
    }
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_snippet, parent, false);
    return new Holder(v);
  }

  @Override
  public void onBindViewHolder(@NonNull Holder holder, int position) {
    SnippetEntry entry = items.get(position);
    String prefix = entry.prefix == null || entry.prefix.isEmpty() ? entry.key : entry.prefix;
    holder.prefix.setText(prefix.length() > 2 ? prefix.substring(0, 2).toUpperCase() : prefix.toUpperCase());
    int color = languageColor(entry.scope);
    holder.bind(position,getItemCount());
    GradientDrawable gd = new GradientDrawable();
    gd.setColor(ColorUtils.setAlphaComponent(color, 128));
    gd.setCornerRadius(
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 15, holder.prefix.getResources().getDisplayMetrics()));
    holder.prefix.setPadding(5, 5, 5, 5);
    holder.prefix.setBackground(gd);
    holder.prefix.setTextColor(color);
    String desc = entry.description;
    if (entry.scope != null && !entry.scope.isEmpty()) {
      desc = (desc == null || desc.isEmpty() ? "" : desc + " · ") + entry.scope;
    }
    if (desc == null || desc.isEmpty()) {
      desc = entry.key;
    }
    holder.desc.setText(desc);
    holder.desc.setTextColor(M3Theme.onSurface());
    String firstLine = entry.body;
    int idx = firstLine.indexOf('\n');
    if (idx >= 0) {
      firstLine = firstLine.substring(0, idx);
    }
    holder.body.setText(firstLine);
    holder.body.setTextColor(M3Theme.onSurfaceVariant());
    holder.itemView.setOnClickListener(
        v -> {
          if (listener != null) {
            listener.onInsertSnippet(entry);
          }
        });
    holder.more.setOnClickListener(
        v -> {
          if (listener != null) {
            listener.onMoreClick(entry, v);
          }
        });
    M3Theme.listCard(holder.itemView);
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  private static int languageColor(String scope) {
    if (scope != null) {
      String token = scope.trim();
      int comma = token.indexOf(',');
      if (comma >= 0) token = token.substring(0, comma).trim();
      if (!token.isEmpty()) {
        String lower = token.toLowerCase();
        String hex = LanguageColors.getColorForExtension(lower);
        if (hex == null) {
          for (Map.Entry<String, String> e : LanguageColors.GITHUB_LANGUAGE_COLORS.entrySet()) {
            if (e.getKey().equalsIgnoreCase(lower)) {
              hex = e.getValue();
              break;
            }
          }
        }
        if (hex == null) {
          int dot = lower.lastIndexOf('.');
          if (dot >= 0 && dot < token.length() - 1) {
            hex = LanguageColors.getColorForExtension(lower.substring(dot + 1));
          }
        }
        if (hex != null) {
          try {
            return Color.parseColor(hex);
          } catch (IllegalArgumentException ignored) {
          }
        }
      }
    }
    return M3Theme.primary();
  }

  static final class Holder extends ListItemViewHolder {
    final TextView prefix;
    final TextView desc;
    final TextView body;
    final ImageView more;

    Holder(@NonNull View itemView) {
      super(itemView);
      prefix = itemView.findViewById(R.id.tvSnippetPrefix);
      desc = itemView.findViewById(R.id.tvSnippetDesc);
      body = itemView.findViewById(R.id.tvSnippetBody);
      more = itemView.findViewById(R.id.ivSnippetMore);
    }
  }
}
