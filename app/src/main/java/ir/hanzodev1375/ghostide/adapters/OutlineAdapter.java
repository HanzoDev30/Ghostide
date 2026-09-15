package ir.hanzodev1375.ghostide.adapters;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.listitem.ListItemViewHolder;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.codeeditors.langs.lsp.model.OutlineSymbol;
import ir.theme.M3Theme;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.lsp4j.SymbolKind;

public final class OutlineAdapter extends RecyclerView.Adapter<OutlineAdapter.Holder> {

  public interface OnSymbolClickListener {
    void onSymbolClick(OutlineSymbol symbol);
  }

  private static final int INDENT_PX = 18;

  private final List<OutlineSymbol> items = new ArrayList<>();
  private OnSymbolClickListener listener;

  public void setOnSymbolClickListener(OnSymbolClickListener l) {
    this.listener = l;
  }

  public void submit(List<OutlineSymbol> symbols) {
    items.clear();
    if (symbols != null) {
      items.addAll(symbols);
    }
    notifyDataSetChanged();
  }

  @NonNull
  @Override
  public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_outline, parent, false);
    return new Holder(v);
  }

  @Override
  public void onBindViewHolder(@NonNull Holder holder, int position) {
    OutlineSymbol symbol = items.get(position);
    SymbolKind sk = SymbolKind.forValue(symbol.kind);
    holder.name.setText(symbol.name);
    holder.name.setTextColor(M3Theme.onSurface());
    holder.kind.setText(kindLabel(sk));
    holder.kind.setTextColor(M3Theme.onSurfaceVariant());
    holder.glyph.setText(kindGlyph(sk));
    holder.glyph.setTextColor(M3Theme.primary());
    GradientDrawable gd = new GradientDrawable();
    gd.setColor(M3Theme.surface());
    gd.setStroke(1, M3Theme.outline());
    gd.setCornerRadius(8);
    holder.glyph.setPadding(5, 5, 5, 5);
    holder.glyph.setBackground(gd);
    int pad = holder.itemView.getPaddingStart() + symbol.depth * INDENT_PX;
    holder.itemView.setPadding(pad, 0, holder.itemView.getPaddingEnd(), 0);
    holder.itemView.setOnClickListener(
        v -> {
          if (listener != null) {
            listener.onSymbolClick(symbol);
          }
        });
    M3Theme.listCard(holder.itemView);
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  static String kindGlyph(SymbolKind kind) {
    if (kind == null) return "\u25CB";
    switch (kind) {
      case Class:
        return "C";
      case Interface:
        return "I";
      case Enum:
        return "E";
      case Method:
      case Function:
        return "\u0192";
      case Constructor:
        return "N";
      case Property:
      case Field:
        return "P";
      case Variable:
        return "x";
      case Constant:
        return "c";
      case Namespace:
      case Package:
      case Module:
        return "N";
      case Struct:
        return "S";
      case Event:
        return "E";
      case Operator:
        return "op";
      case TypeParameter:
        return "T";
      default:
        return "\u25CB";
    }
  }

  static String kindLabel(SymbolKind kind) {
    if (kind == null) return "symbol";
    switch (kind) {
      case File:
        return "file";
      case Module:
        return "module";
      case Namespace:
        return "namespace";
      case Package:
        return "package";
      case Class:
        return "class";
      case Method:
        return "method";
      case Property:
        return "property";
      case Field:
        return "field";
      case Constructor:
        return "constructor";
      case Enum:
        return "enum";
      case Interface:
        return "interface";
      case Function:
        return "function";
      case Variable:
        return "variable";
      case Constant:
        return "constant";
      case String:
        return "string";
      case Number:
        return "number";
      case Boolean:
        return "boolean";
      case Array:
        return "array";
      case Object:
        return "object";
      case Key:
        return "key";
      case Null:
        return "null";
      case EnumMember:
        return "enum member";
      case Struct:
        return "struct";
      case Event:
        return "event";
      case Operator:
        return "operator";
      case TypeParameter:
        return "type parameter";
      default:
        return "symbol";
    }
  }

  static final class Holder extends ListItemViewHolder {
    final TextView glyph;
    final TextView name;
    final TextView kind;

    Holder(@NonNull View itemView) {
      super(itemView);
      glyph = itemView.findViewById(R.id.tvOutlineGlyph);
      name = itemView.findViewById(R.id.tvOutlineName);
      kind = itemView.findViewById(R.id.tvOutlineKind);
    }
  }
}
