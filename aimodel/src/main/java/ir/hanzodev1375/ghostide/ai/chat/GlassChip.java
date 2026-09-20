package ir.hanzodev1375.ghostide.ai.chat;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.liquidglass.GlassMaterial;
import com.example.liquidglass.LiquidGlassView;

public class GlassChip extends LiquidGlassView {

  private final float cornerPx;
  private final LinearLayout contentRow;
  private final TextView labelView;
  private View statusDot;

  public GlassChip(Context context) {
    this(context, null);
  }

  public GlassChip(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs, 0);

    float density = getResources().getDisplayMetrics().density;
    cornerPx = 8 * density;
    setCornerRadius(999f);
    setMaterial(GlassMaterial.REGULAR);
    setDispersionStrength(0.12f);
    setEnableDynamicBackground(true);
    setEnableSensorHighlight(true);
    setEnableAdaptiveTint(true);
    Activity activity = findActivity(context);
    if (activity != null) {
      View backdrop = activity.findViewById(android.R.id.content);
      if (backdrop != null) {
        setBackdropSource(backdrop);
      }
    }
    setMinimumHeight((int) (32 * density));
    setClickable(true);
    setFocusable(true);

    contentRow = new LinearLayout(context);
    contentRow.setOrientation(LinearLayout.HORIZONTAL);
    contentRow.setGravity(Gravity.CENTER_VERTICAL);
    int pad = (int) (12 * density);
    contentRow.setPadding(pad, 0, pad, 0);
    super.addView(contentRow, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

    labelView = new TextView(context);
    labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
    labelView.setGravity(Gravity.CENTER_VERTICAL);
    labelView.setIncludeFontPadding(false);
    labelView.setTextColor(Color.WHITE);
    contentRow.addView(labelView);
  }

  public void setLabel(CharSequence label) {
    labelView.setText(label);
  }

  public CharSequence getLabel() {
    return labelView.getText();
  }

  public TextView getLabelView() {
    return labelView;
  }

  public void addChipContent(@NonNull View view) {
    addChipContent(view, 8);
  }

  public void addChipContent(@NonNull View view, int startMarginDp) {
    LinearLayout.LayoutParams lp =
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    if (contentRow.getChildCount() > 0) {
      int margin = Math.round(startMarginDp * getResources().getDisplayMetrics().density);
      lp.setMargins(margin, 0, 0, 0);
    }
    contentRow.addView(view, lp);
  }

  public void tint(int color) {
    setGlassTint(color, 0.35f);
  }

  public void clearTint() {
    setGlassTint(Color.TRANSPARENT, 0.35f);
  }

  public void setStatusDot(int color) {
    float d = getResources().getDisplayMetrics().density;
    View dot;
    if (statusDot == null) {
      dot = new View(getContext());
      GradientDrawable bg = new GradientDrawable();
      bg.setShape(GradientDrawable.OVAL);
      dot.setBackground(bg);
      int size = Math.round(8 * d);
      LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
      lp.setMargins(0, 0, Math.round(6 * d), 0);
      contentRow.addView(dot, 0, lp);
      statusDot = dot;
    } else {
      dot = statusDot;
    }
    if (dot.getBackground() instanceof GradientDrawable) {
      ((GradientDrawable) dot.getBackground()).setColor(color);
    }
  }

  private static Activity findActivity(Context context) {
    Context current = context;
    while (current instanceof ContextWrapper) {
      if (current instanceof Activity) {
        return (Activity) current;
      }
      current = ((ContextWrapper) current).getBaseContext();
    }
    return null;
  }

  public void setGroupPosition(boolean topRounded, boolean bottomRounded) {
    float r = topRounded || bottomRounded ? cornerPx : 0f;
    setCornerRadii(topRounded ? r : 0f, topRounded ? r : 0f, bottomRounded ? r : 0f, bottomRounded ? r : 0f);
    invalidate();
  }
}