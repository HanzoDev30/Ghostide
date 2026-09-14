package ir.hanzodev1375.ghostide.interfaces;

import android.view.View;
/**
 * call all clicks 
 */
public interface OnItemClickListener<T> {
  void onClick(View v, T t, int pos);
}
