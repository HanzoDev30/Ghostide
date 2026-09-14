package ir.hanzodev1375.ghostide.utils;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;
import ir.hanzodev1375.ghostide.R;

public class MarginItemDecoration extends RecyclerView.ItemDecoration {
  private final int itemMargin;

  public MarginItemDecoration(Context context) {
    itemMargin = context.getResources().getDimensionPixelSize(R.dimen.list_item_margin);
  }

  @Override
  public void getItemOffsets(
      @NonNull Rect outRect,
      @NonNull View view,
      @NonNull RecyclerView parent,
      @NonNull RecyclerView.State state) {
    int position = parent.getChildAdapterPosition(view);
    if (position != state.getItemCount() - 1) {
      outRect.bottom = itemMargin;
    }
  }
}
