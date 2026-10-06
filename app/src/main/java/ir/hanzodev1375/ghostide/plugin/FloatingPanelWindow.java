package ir.hanzodev1375.ghostide.plugin;

import android.animation.TimeInterpolator;
import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import ir.hanzodev1375.components.animators.AnimationManager;
import ir.hanzodev1375.ghostide.R;
/**
 * Floating window of the plugin panel with smooth and optimized animations.
 *
 * <p>Animations only use GPU-accelerated properties and if it is low
 * Battery (according to {@link AnimationManager}) is completely disabled to keep it running even on weak phones.
 *
 * @author Ghost
 */
public final class FloatingPanelWindow {

  private static final int MIN_W_DP = 180;
  private static final int MIN_H_DP = 140;
  private static final int DEFAULT_W_DP = 300;
  private static final int DEFAULT_H_DP = 400;

  private static final long SHOW_DURATION = 220L;
  private static final long HIDE_DURATION = 170L;
  private static final long PRESS_DURATION = 130L;

  private static final float SHOW_START_SCALE = 0.88f;
  private static final float HIDE_END_SCALE = 0.92f;
  private static final float PRESS_SCALE = 1.02f;

  private static final TimeInterpolator EASE_OUT =
      new PathInterpolator(0.05f, 0.70f, 0.10f, 1.00f);
  private static final TimeInterpolator EASE_IN =
      new PathInterpolator(0.30f, 0.00f, 0.80f, 0.15f);

  private final Activity activity;
  private final String title;
  private final View content;
  private final float density;

  private FrameLayout overlay;
  private FrameLayout windowRoot;
  private boolean showing;
  private float baseElevation;

  public FloatingPanelWindow(Activity activity, String title, View content) {
    this.activity = activity;
    this.title = title == null ? "" : title;
    this.content = content;
    this.density = activity.getResources().getDisplayMetrics().density;
  }

  public boolean isShowing() {
    return showing && overlay != null && overlay.getParent() != null;
  }

  public void show() {
    if (activity.isFinishing() || isShowing()) return;

    ViewGroup root = activity.findViewById(android.R.id.content);
    if (root == null) return;

    overlay = new FrameLayout(activity);
    overlay.setLayoutParams(
        new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    overlay.setClipChildren(false);
    overlay.setClipToPadding(false);
    overlay.setClickable(false);
    overlay.setFocusable(false);

    windowRoot =
        (FrameLayout)
            LayoutInflater.from(activity)
                .inflate(R.layout.floating_panel_window, overlay, false);

    bindViews();

    int w = dp(DEFAULT_W_DP);
    int h = dp(DEFAULT_H_DP);
    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w, h);
    lp.leftMargin = dp(24);
    lp.topMargin = dp(72);
    overlay.addView(windowRoot, lp);

    root.addView(overlay);
    showing = true;

    applyBaseElevation();
    runShowAnimation(w, h);
  }

  public void dismiss() {
    if (!showing) return;
    showing = false;

    final FrameLayout overlayRef = overlay;
    final FrameLayout rootRef = windowRoot;

    if (rootRef == null || !animationsEnabled()) {
      removeOverlay(overlayRef);
      clearRefs(overlayRef, rootRef);
      return;
    }

    rootRef.animate().cancel();
    rootRef
        .animate()
        .alpha(0f)
        .scaleX(HIDE_END_SCALE)
        .scaleY(HIDE_END_SCALE)
        .setDuration(HIDE_DURATION)
        .setInterpolator(EASE_IN)
        .withEndAction(
            () -> {
              removeOverlay(overlayRef);
              clearRefs(overlayRef, rootRef);
            })
        .start();
  }

  private void removeOverlay(FrameLayout overlayRef) {
    if (overlayRef != null && overlayRef.getParent() instanceof ViewGroup) {
      ((ViewGroup) overlayRef.getParent()).removeView(overlayRef);
    }
  }

  private void clearRefs(FrameLayout overlayRef, FrameLayout rootRef) {
    if (overlay == overlayRef) overlay = null;
    if (windowRoot == rootRef) windowRoot = null;
  }

  private void applyBaseElevation() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      baseElevation = windowRoot.getElevation();
      if (baseElevation <= 0f) {
        baseElevation = dp(6);
        windowRoot.setElevation(baseElevation);
      }
    }
  }

  private void runShowAnimation(int w, int h) {
    if (!animationsEnabled()) {
      windowRoot.setAlpha(1f);
      return;
    }
    windowRoot.setPivotX(w / 2f);
    windowRoot.setPivotY(h / 2f);
    windowRoot.setAlpha(0f);
    windowRoot.setScaleX(SHOW_START_SCALE);
    windowRoot.setScaleY(SHOW_START_SCALE);
    windowRoot.setTranslationY(dp(8));
    windowRoot
        .animate()
        .alpha(1f)
        .scaleX(1f)
        .scaleY(1f)
        .translationY(0f)
        .setDuration(SHOW_DURATION)
        .setInterpolator(EASE_OUT)
        .start();
  }

  private void bindViews() {
    TextView titleView = windowRoot.findViewById(R.id.floating_title);
    titleView.setText(title);

    ImageView close = windowRoot.findViewById(R.id.floating_close);
    close.setOnClickListener(v -> dismiss());

    FrameLayout contentArea = windowRoot.findViewById(R.id.floating_content);
    if (content != null) {
      if (content.getParent() instanceof ViewGroup) {
        ((ViewGroup) content.getParent()).removeView(content);
      }
      contentArea.addView(
          content,
          new FrameLayout.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    View titleBar = windowRoot.findViewById(R.id.floating_title_bar);
    View resize = windowRoot.findViewById(R.id.floating_resize);
    attachDrag(titleBar);
    attachResize(resize);
  }

  private void attachDrag(View anchor) {
    anchor.setOnTouchListener(
        new View.OnTouchListener() {
          private float downRawX, downRawY;
          private float startTransX, startTransY;
          private boolean dragging;

          @Override
          public boolean onTouch(View v, MotionEvent e) {
            if (windowRoot == null || overlay == null) return false;
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) windowRoot.getLayoutParams();
            switch (e.getActionMasked()) {
              case MotionEvent.ACTION_DOWN:
                downRawX = e.getRawX();
                downRawY = e.getRawY();
                startTransX = windowRoot.getTranslationX();
                startTransY = windowRoot.getTranslationY();
                dragging = true;
                onDragStart();
                return true;

              case MotionEvent.ACTION_MOVE:
                if (!dragging) return false;
                float dx = e.getRawX() - downRawX;
                float dy = e.getRawY() - downRawY;
                float minX = -lp.leftMargin;
                float maxX = overlay.getWidth() - lp.leftMargin - lp.width;
                float minY = -lp.topMargin;
                float maxY = overlay.getHeight() - lp.topMargin - lp.height;
                windowRoot.setTranslationX(clampF(startTransX + dx, minX, maxX));
                windowRoot.setTranslationY(clampF(startTransY + dy, minY, maxY));
                return true;

              case MotionEvent.ACTION_UP:
              case MotionEvent.ACTION_CANCEL:
                if (!dragging) return false;
                dragging = false;
                commitDrag(lp);
                onDragEnd();
                return true;

              default:
                return false;
            }
          }
        });
  }

  private void commitDrag(FrameLayout.LayoutParams lp) {
    int dx = Math.round(windowRoot.getTranslationX());
    int dy = Math.round(windowRoot.getTranslationY());
    windowRoot.setTranslationX(0f);
    windowRoot.setTranslationY(0f);
    lp.leftMargin = clamp(lp.leftMargin + dx, 0, overlay.getWidth() - lp.width);
    lp.topMargin = clamp(lp.topMargin + dy, 0, overlay.getHeight() - lp.height);
    windowRoot.setLayoutParams(lp);
  }

  private void onDragStart() {
    if (!animationsEnabled()) return;
    windowRoot.animate().cancel();
    windowRoot
        .animate()
        .scaleX(PRESS_SCALE)
        .scaleY(PRESS_SCALE)
        .setDuration(PRESS_DURATION)
        .setInterpolator(EASE_OUT)
        .start();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      windowRoot.setElevation(dp(14));
    }
  }

  private void onDragEnd() {
    if (!animationsEnabled()) return;
    windowRoot
        .animate()
        .scaleX(1f)
        .scaleY(1f)
        .setDuration(PRESS_DURATION)
        .setInterpolator(EASE_OUT)
        .start();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      windowRoot.setElevation(baseElevation);
    }
  }

  private void attachResize(View anchor) {
    anchor.setOnTouchListener(
        new View.OnTouchListener() {
          private float downRawX, downRawY;
          private int startW, startH;
          private boolean resizing;

          @Override
          public boolean onTouch(View v, MotionEvent e) {
            if (windowRoot == null || overlay == null) return false;
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) windowRoot.getLayoutParams();
            switch (e.getActionMasked()) {
              case MotionEvent.ACTION_DOWN:
                downRawX = e.getRawX();
                downRawY = e.getRawY();
                startW = lp.width;
                startH = lp.height;
                resizing = true;
                return true;

              case MotionEvent.ACTION_MOVE:
                if (!resizing) return false;
                int newW = startW + Math.round(e.getRawX() - downRawX);
                int newH = startH + Math.round(e.getRawY() - downRawY);
                int maxW = overlay.getWidth() - lp.leftMargin;
                int maxH = overlay.getHeight() - lp.topMargin;
                int cw = Math.max(dp(MIN_W_DP), Math.min(newW, maxW));
                int ch = Math.max(dp(MIN_H_DP), Math.min(newH, maxH));
                if (cw == lp.width && ch == lp.height) return true;
                lp.width = cw;
                lp.height = ch;
                windowRoot.setPivotX(cw / 2f);
                windowRoot.setPivotY(ch / 2f);
                windowRoot.setLayoutParams(lp);
                return true;

              case MotionEvent.ACTION_UP:
              case MotionEvent.ACTION_CANCEL:
                resizing = false;
                return true;

              default:
                return false;
            }
          }
        });
  }

  private boolean animationsEnabled() {
    try {
      return AnimationManager.getInstance(activity).areAnimationsEnabled();
    } catch (Throwable t) {
      return true;
    }
  }

  private int clamp(int v, int min, int max) {
    if (max < min) max = min;
    return Math.max(min, Math.min(v, max));
  }

  private float clampF(float v, float min, float max) {
    if (max < min) max = min;
    return Math.max(min, Math.min(v, max));
  }

  private int dp(int v) {
    return Math.round(v * density);
  }
}