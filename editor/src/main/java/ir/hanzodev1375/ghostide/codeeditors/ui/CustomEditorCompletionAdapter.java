package ir.hanzodev1375.ghostide.codeeditors.ui;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.google.android.material.textview.MaterialTextView;

import io.github.rosemoe.sora.lang.completion.CompletionItem;
import io.github.rosemoe.sora.lang.completion.CompletionItemKind;
import io.github.rosemoe.sora.lang.completion.SimpleCompletionIconDrawer;
import io.github.rosemoe.sora.widget.component.EditorCompletionAdapter;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

import ir.hanzodev1375.ghostide.codeeditors.R;
import ir.hanzodev1375.ghostide.codeeditors.colorrender.model.colorrepo.ColorNameRepository;

public final class CustomEditorCompletionAdapter extends EditorCompletionAdapter {

  private int itemHeight = 45;

  @Override
  public int getItemHeight() {
    return (int)
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            itemHeight,
            getContext().getResources().getDisplayMetrics());
  }

  @Override
  public View getView(int pos, View view, ViewGroup parent, boolean isCurrentCursorPosition) {
    if (view == null) {
      view =
          LayoutInflater.from(getContext())
              .inflate(R.layout.editor_completion_result_item, parent, false);
    }
    CompletionItem item = getItem(pos);

    MaterialTextView tv = view.findViewById(R.id.result_item_label);
    LinearLayout compHolder = view.findViewById(R.id.result_comp_desc_holder);

    tv.setText(item.label);
    tv = view.findViewById(R.id.result_item_desc);
    tv.setText(item.desc);

    if (item instanceof CustomCompletionItem) {
      CustomCompletionItem comp = (CustomCompletionItem) item;
      if (comp.compDescription != null) {
        tv = view.findViewById(R.id.result_item_comp_desc);
        tv.setText(comp.compDescription);
        compHolder.setVisibility(View.VISIBLE);
      } else {
        compHolder.setVisibility(View.GONE);
      }
    } else {
      compHolder.setVisibility(View.GONE);
    }

    view.setTag(pos);
    view.setBackgroundColor(
        isCurrentCursorPosition ? getThemeColor(EditorColorScheme.COMPLETION_WND_ITEM_CURRENT) : 0);

    ImageView iv = view.findViewById(R.id.result_item_image);

    if (item.kind == CompletionItemKind.Color) {
      String colorStr = item.label != null ? item.label.toString() : "";
      ColorNameRepository repo = ColorNameRepository.getInstance(getContext());
      Integer parsed = ColorSwatchDrawable.parseColor(colorStr, repo);
      if (parsed != null) {
        iv.setImageDrawable(SimpleCompletionIconDrawer.INSTANCE.drawColorSpan(parsed));
      } else {
        iv.setImageDrawable(new CustomCircleDrawable(CompletionItemKind.Color, true));
      }
    } else if (item.kind == CompletionItemKind.File || item.kind == CompletionItemKind.Folder) {
      boolean isFolder = item.kind == CompletionItemKind.Folder;
      String src = item.label != null ? item.label.toString() : "";
      var fileIcon = SimpleCompletionIconDrawer.INSTANCE.drawFileFolder(src, isFolder);
      if (fileIcon != null) {
        iv.setImageDrawable(fileIcon);
      } else {
        iv.setImageDrawable(new CustomCircleDrawable(item.kind, true));
      }
    } else {
      CompletionItemKind kind = item.kind != null ? item.kind : CompletionItemKind.Identifier;
      iv.setImageDrawable(new CustomCircleDrawable(kind, true));
    }
    return view;
  }

  public void setItemHeight(final int value) {
    this.itemHeight = value;
  }
}
