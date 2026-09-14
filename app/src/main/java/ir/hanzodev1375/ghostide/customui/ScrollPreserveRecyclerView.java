package ir.hanzodev1375.ghostide.customui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.HashMap;
import java.util.Map;

public class ScrollPreserveRecyclerView extends RecyclerView {

  private static final Map<String, Parcelable> SCROLL_CACHE = new HashMap<>();
  private static final String PREFS_NAME = "scroll_preserve";
  private static final String KEY_PREFIX_POS = "pos_";
  private static final String KEY_PREFIX_OFF = "off_";

  private String locationKey;

  public ScrollPreserveRecyclerView(@NonNull Context context) {
    this(context, null);
  }

  public ScrollPreserveRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ScrollPreserveRecyclerView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public void setLocationKey(@Nullable String key) {
    this.locationKey = key == null || key.isEmpty() ? null : key;
  }

  private SharedPreferences getPrefs() {
    return getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
  }

  public void saveScrollPosition() {
    if (locationKey == null) return;
    RecyclerView.LayoutManager lm = getLayoutManager();
    if (lm == null) return;
    Parcelable state = lm.onSaveInstanceState();
    if (state != null) SCROLL_CACHE.put(locationKey, state);
    if (lm instanceof LinearLayoutManager) {
      LinearLayoutManager glm = (LinearLayoutManager) lm;
      int firstPos = glm.findFirstVisibleItemPosition();
      if (firstPos == RecyclerView.NO_POSITION) return;
      View firstView = glm.findViewByPosition(firstPos);
      int offset = firstView != null ? firstView.getTop() : 0;
      getPrefs().edit()
          .putInt(KEY_PREFIX_POS + locationKey, firstPos)
          .putInt(KEY_PREFIX_OFF + locationKey, offset)
          .apply();
    }
  }

  public void restoreScrollPosition() {
    if (locationKey == null) return;
    if (getAdapter() == null || getAdapter().getItemCount() == 0) return;
    Parcelable target = SCROLL_CACHE.get(locationKey);
    if (target != null) {
      post(
          () -> {
            RecyclerView.LayoutManager lm = getLayoutManager();
            if (lm == null) return;
            if (getAdapter() == null || getAdapter().getItemCount() == 0) return;
            lm.onRestoreInstanceState(target);
          });
      return;
    }
    int savedPos = getPrefs().getInt(KEY_PREFIX_POS + locationKey, -1);
    int savedOff = getPrefs().getInt(KEY_PREFIX_OFF + locationKey, 0);
    if (savedPos >= 0 && savedPos < getAdapter().getItemCount()) {
      final int pos = savedPos;
      final int off = savedOff;
      post(
          () -> {
            RecyclerView.LayoutManager lm = getLayoutManager();
            if (lm == null) return;
            if (getAdapter() == null || getAdapter().getItemCount() == 0) return;
            if (pos < getAdapter().getItemCount()) {
              ((LinearLayoutManager) lm).scrollToPositionWithOffset(pos, off);
            }
          });
    }
  }

  public void clearSavedScroll() {
    if (locationKey != null) {
      SCROLL_CACHE.remove(locationKey);
      getPrefs().edit()
          .remove(KEY_PREFIX_POS + locationKey)
          .remove(KEY_PREFIX_OFF + locationKey)
          .apply();
    }
  }
}
