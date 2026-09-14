package ir.hanzodev1375.ghostide.codeeditors.ui;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import androidx.annotation.NonNull;

import io.github.rosemoe.sora.widget.component.DefaultCompletionLayout;
import io.github.rosemoe.sora.widget.component.EditorAutoCompletion;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;
import ir.hanzodev1375.ghostide.codeeditors.IdeEditor;

public class CustomCompletionLayout extends DefaultCompletionLayout {

  public static final String TAG = "CustomCompletionLayout";
  private final IdeEditor editor;
  private int layoutCornerRadius = 8;
  private ListView listView;
  private LinearLayout rootView;
  private boolean isLoading = false;
  private boolean enabledAnimation = false;
  private EditorAutoCompletion editorAutoCompletion;

  public CustomCompletionLayout(IdeEditor codeEditor) {
    editor = codeEditor;
  }

  @Override
  public void setEditorCompletion(@NonNull EditorAutoCompletion completion) {
    editorAutoCompletion = completion;
  }

  @Override
  public void setEnabledAnimation(boolean enabledAnimation) {
    this.enabledAnimation = enabledAnimation;

    if (enabledAnimation) {
      LayoutTransition transition = new LayoutTransition();
      transition.enableTransitionType(LayoutTransition.CHANGING);
      transition.enableTransitionType(LayoutTransition.APPEARING);
      transition.enableTransitionType(LayoutTransition.DISAPPEARING);
      transition.enableTransitionType(LayoutTransition.CHANGE_APPEARING);
      transition.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
      transition.addTransitionListener(
          new LayoutTransition.TransitionListener() {
            @Override
            public void startTransition(
                LayoutTransition transition, ViewGroup container, View view, int transitionType) {
              // no-op
            }

            @Override
            public void endTransition(
                LayoutTransition transition, ViewGroup container, View view, int transitionType) {
              if (view != listView) {
                return;
              }
              view.requestLayout();
            }
          });
      rootView.setLayoutTransition(transition);
      listView.setLayoutTransition(transition);
    } else {
      rootView.setLayoutTransition(null);
      listView.setLayoutTransition(null);
    }
  }

  @NonNull
  @Override
  public View inflate(@NonNull Context context) {
    LinearLayout rootLayout = new LinearLayout(context);
    rootView = rootLayout;
    listView = new ListView(context);
    rootLayout.setOrientation(LinearLayout.VERTICAL);
    setEnabledAnimation(false);
    rootLayout.addView(listView, new LinearLayout.LayoutParams(-1, -1));
    listView.setDividerHeight(0);
    setLoading(true);
    listView.setOnItemClickListener(
        (parent, view, position, id) -> {
          try {
            editorAutoCompletion.select(position);
          } catch (Exception e) {
            Log.e(TAG, "Failed to inflate completion layout", e);
          }
        });
    return rootLayout;
  }

  @Override
  public void onApplyColorScheme(@NonNull EditorColorScheme colorScheme) {
    GradientDrawable gd = new GradientDrawable();
    gd.setCornerRadius(
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            layoutCornerRadius,
            editorAutoCompletion.getContext().getResources().getDisplayMetrics()));
    gd.setStroke(
        1,
        colorScheme.getColor(EditorColorScheme.COMPLETION_WND_CORNER));
    gd.setColor(colorScheme.getColor(EditorColorScheme.COMPLETION_WND_BACKGROUND));
    rootView.setBackground(gd);
  }

  public boolean isAnimationEnabled() {
    return enabledAnimation;
  }

  public boolean isLoading() {
    return isLoading;
  }

  @Override
  public void setLoading(boolean state) {
    isLoading = state;
  }

  @NonNull
  @Override
  public ListView getCompletionList() {
    return listView;
  }

  @Override
  public void ensureListPositionVisible(int position, int increment) {
    listView.post(
        () -> {
          while (listView.getFirstVisiblePosition() + 1 > position && listView.canScrollList(-1)) {
            doPerformScrollList(increment / 2);
          }
          while (listView.getLastVisiblePosition() - 1 < position && listView.canScrollList(1)) {
            doPerformScrollList(-increment / 2);
          }
        });
  }

  /** Perform motion events */
  private void doPerformScrollList(int offset) {
    ListView adpView = getCompletionList();

    long down = SystemClock.uptimeMillis();
    MotionEvent ev = MotionEvent.obtain(down, down, MotionEvent.ACTION_DOWN, 0, 0, 0);
    adpView.onTouchEvent(ev);
    ev.recycle();

    ev = MotionEvent.obtain(down, down, MotionEvent.ACTION_MOVE, 0, offset, 0);
    adpView.onTouchEvent(ev);
    ev.recycle();

    ev = MotionEvent.obtain(down, down, MotionEvent.ACTION_CANCEL, 0, offset, 0);
    adpView.onTouchEvent(ev);
    ev.recycle();
  }

  public void setCornerRadius(final int radius) {
    layoutCornerRadius = radius;
  }
}
