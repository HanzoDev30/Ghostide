package ir.hanzodev1375.ghostide.plugin;

import android.app.Activity;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import ir.hanzodev1375.ghostide.R;

/**
 * پنجره ی شناور برای نمایش پنل پلاگین که فقط داخل همان Activity زندگی می کند.
 *
 * <p>به عنوان overlay روی {@code android.R.id.content} اضافه می شود؛ نه مجوز {@code
 * SYSTEM_ALERT_WINDOW} لازم دارد و نه می تواند از اپ خارج شود. همراه Activity از بین می رود.
 */
public final class FloatingPanelWindow {

  private static final int MIN_W_DP = 180;
  private static final int MIN_H_DP = 140;
  private static final int DEFAULT_W_DP = 300;
  private static final int DEFAULT_H_DP = 400;

  private final Activity activity;
  private final String title;
  private final View content;

  private FrameLayout overlay;
  private FrameLayout windowRoot;
  private boolean showing;

  public FloatingPanelWindow(Activity activity, String title, View content) {
    this.activity = activity;
    this.title = title == null ? "" : title;
    this.content = content;
  }

  public boolean isShowing() {
    return showing && overlay != null && overlay.getParent() != null;
  }

  // ------------------------------------------------------------------ lifecycle

  public void show() {
    if (activity.isFinishing() || isShowing()) {
      return;
    }
    ViewGroup root = activity.findViewById(android.R.id.content);
    if (root == null) {
      return;
    }

    overlay = new FrameLayout(activity);
    overlay.setLayoutParams(
        new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    overlay.setClipChildren(false);
    overlay.setClipToPadding(false);
    // لمس های بیرون پنجره به لایه های زیر منتقل شوند
    overlay.setClickable(false);
    overlay.setFocusable(false);

    windowRoot =
        (FrameLayout)
            LayoutInflater.from(activity).inflate(R.layout.floating_panel_window, overlay, false);

    bindViews();

    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(DEFAULT_W_DP), dp(DEFAULT_H_DP));
    lp.leftMargin = dp(24);
    lp.topMargin = dp(72);
    overlay.addView(windowRoot, lp);

    root.addView(overlay);
    showing = true;
  }

  public void dismiss() {
    showing = false;
    if (overlay != null && overlay.getParent() instanceof ViewGroup) {
      ((ViewGroup) overlay.getParent()).removeView(overlay);
    }
    if (content != null && content.getParent() instanceof ViewGroup) {
      ((ViewGroup) content.getParent()).removeView(content);
    }
    overlay = null;
    windowRoot = null;
  }

  // ------------------------------------------------------------------ binding

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

  // ------------------------------------------------------------------ gestures

  private void attachDrag(View anchor) {
    anchor.setOnTouchListener(
        new View.OnTouchListener() {
          private float downRawX, downRawY;
          private int startLeft, startTop;
          private boolean dragging;

          @Override
          public boolean onTouch(View v, MotionEvent e) {
            if (windowRoot == null) return false;
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) windowRoot.getLayoutParams();
            switch (e.getActionMasked()) {
              case MotionEvent.ACTION_DOWN:
                downRawX = e.getRawX();
                downRawY = e.getRawY();
                startLeft = lp.leftMargin;
                startTop = lp.topMargin;
                dragging = true;
                return true;
              case MotionEvent.ACTION_MOVE:
                if (!dragging) return false;
                int newLeft = startLeft + Math.round(e.getRawX() - downRawX);
                int newTop = startTop + Math.round(e.getRawY() - downRawY);
                lp.leftMargin = clamp(newLeft, 0, overlay.getWidth() - lp.width);
                lp.topMargin = clamp(newTop, 0, overlay.getHeight() - lp.height);
                windowRoot.setLayoutParams(lp);
                return true;
              case MotionEvent.ACTION_UP:
              case MotionEvent.ACTION_CANCEL:
                dragging = false;
                return true;
              default:
                return false;
            }
          }
        });
  }

  private void attachResize(View anchor) {
    anchor.setOnTouchListener(
        new View.OnTouchListener() {
          private float downRawX, downRawY;
          private int startW, startH;
          private boolean resizing;

          @Override
          public boolean onTouch(View v, MotionEvent e) {
            if (windowRoot == null) return false;
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
                lp.width =
                    Math.max(dp(MIN_W_DP), Math.min(newW, overlay.getWidth() - lp.leftMargin));
                lp.height =
                    Math.max(dp(MIN_H_DP), Math.min(newH, overlay.getHeight() - lp.topMargin));
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

  // ------------------------------------------------------------------ helpers

  private int clamp(int v, int min, int max) {
    if (max < min) max = min;
    return Math.max(min, Math.min(v, max));
  }

  private int dp(int v) {
    Resources r = activity.getResources();
    return Math.round(v * r.getDisplayMetrics().density);
  }
}
