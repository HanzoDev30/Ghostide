package ir.hanzodev1375.ghostide.utils;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.example.liquidglass.GlassMaterial;
import ir.hanzodev1375.components.sheet.customitemsheet.ui.GlassCompat;
import ir.hanzodev1375.ghostide.R;
import ir.theme.M3Theme;
import java.util.List;

public class ObjectUtil {

  public static final String TRANSITION_EDITOR = "ghost_transition_editor";
  public static final String TRANSITION_IMAGE = "ghost_transition_image";
  public static final String TRANSITION_AI_CHAT = "ghost_transition_ai_chat";
  public static final String TRANSITION_THEME = "ghost_transition_theme";
  public static final String TRANSITION_LOGO = "ghost_transition_logo";

  public interface OnGlassMenuItemClickListener {
    void onItemClick(int position, String title);
  }

  public static PopupWindow showGlassPopup(Activity activity, View anchor, View content) {
    int[] loc = new int[2];
    anchor.getLocationOnScreen(loc);

    int padding = (int) (8 * activity.getResources().getDisplayMetrics().density);

    GlassCompat glassView = new GlassCompat(activity);
    glassView.setLayoutParams(
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    glassView.setCornerRadius(24f * activity.getResources().getDisplayMetrics().density);
    glassView.setRefractionHeight(66f);
    glassView.setBevelWidth(10f);
    glassView.setMaterial(GlassMaterial.REGULAR);
    glassView.setDispersionStrength(0.12f);
    glassView.setEnableDynamicBackground(true);
    glassView.setEnableSensorHighlight(true);
    glassView.setEnableAdaptiveTint(true);
    glassView.setBackdropSource(activity.findViewById(android.R.id.content));

    PopupWindow popupWindow =
        new PopupWindow(
            glassView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true);
    popupWindow.setElevation(0);

    LinearLayout container = new LinearLayout(activity);
    container.setOrientation(LinearLayout.VERTICAL);
    container.setPadding(padding, padding, padding, padding);
    container.addView(content);

    glassView.addView(container);

    popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    popupWindow.setOutsideTouchable(true);
    popupWindow.setAnimationStyle(R.style.GlassMenuFadeAnimation);

    glassView.measure(
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
    int menuHeight = glassView.getMeasuredHeight();
    var dm = activity.getResources().getDisplayMetrics();
    int screenHeight = dm.heightPixels;
    int spaceBelow = screenHeight - (loc[1] + anchor.getHeight());
    int spaceAbove = loc[1];
    int y = loc[1] + anchor.getHeight();
    if (spaceBelow < menuHeight && spaceAbove > spaceBelow) {
      y = loc[1] - menuHeight;
    }
    popupWindow.showAtLocation(anchor, Gravity.TOP | Gravity.START, loc[0], y);
    return popupWindow;
  }

  public static void showGlassMenu(
      Activity activity, View anchor, List<String> items, OnGlassMenuItemClickListener listener) {
    int[] loc = new int[2];
    anchor.getLocationOnScreen(loc);

    int padding = (int) (8 * activity.getResources().getDisplayMetrics().density);

    GlassCompat glassView = new GlassCompat(activity);
    glassView.setLayoutParams(
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    glassView.setCornerRadius(24f * activity.getResources().getDisplayMetrics().density);
    glassView.setRefractionHeight(66f);
    glassView.setBevelWidth(10f);
    glassView.setMaterial(GlassMaterial.REGULAR);
    glassView.setDispersionStrength(0.12f);
    glassView.setEnableDynamicBackground(true);
    glassView.setEnableSensorHighlight(true);
    glassView.setEnableAdaptiveTint(true);
    glassView.setBackdropSource(activity.findViewById(android.R.id.content));
    glassView.setAlpha(0f);

    PopupWindow popupWindow =
        new PopupWindow(
            glassView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true);
    popupWindow.setElevation(0);
    LinearLayout container = new LinearLayout(activity);
    container.setOrientation(LinearLayout.VERTICAL);
    container.setPadding(padding, padding, padding, padding);
    for (int i = 0; i < items.size(); i++) {
      View v = LayoutInflater.from(activity).inflate(R.layout.glass_menu_item, container, false);
      TextView textView = v.findViewById(R.id.menuItemText);
      M3Theme.textView(textView);
      textView.setText(items.get(i));
      final int index = i;
      textView.setOnClickListener(
          vvvv -> {
            if (popupWindow != null) {
              popupWindow.dismiss();
            }
            if (listener != null) listener.onItemClick(index, items.get(index));
          });
      container.addView(v);
    }

    glassView.addView(container);

    popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    popupWindow.setOutsideTouchable(true);
    popupWindow.setAnimationStyle(R.style.GlassMenuFadeAnimation);

    glassView.measure(
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
    int menuHeight = glassView.getMeasuredHeight();
    var dm = activity.getResources().getDisplayMetrics();
    int screenHeight = dm.heightPixels;
    int spaceBelow = screenHeight - (loc[1] + anchor.getHeight());
    int spaceAbove = loc[1];
    int y = loc[1] + anchor.getHeight();
    if (spaceBelow < menuHeight && spaceAbove > spaceBelow) {
      y = loc[1] - menuHeight;
    }
    popupWindow.showAtLocation(anchor, Gravity.TOP | Gravity.START, loc[0], y);

    ValueAnimator fadeIn = ValueAnimator.ofFloat(0f, 1f);
    fadeIn.setDuration(160);
    fadeIn.setInterpolator(new DecelerateInterpolator(1.5f));
    fadeIn.addUpdateListener(
        a -> {
          glassView.setAlpha((float) a.getAnimatedValue());
          glassView.invalidate();
        });
    fadeIn.start();
  }
}