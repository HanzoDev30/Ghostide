package ir.hanzodev1375.components.glass;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import com.example.liquidglass.GlassMaterial;
import com.example.liquidglass.LiquidGlassButton;

/**
 * {@link LiquidGlassButton} that wires its own backdrop.
 *
 * <p>Use this instead of the raw library class and the glass keeps working inside dialogs and
 * bottom sheets without any manual {@code setBackdropSource(android.R.id.content)} call.
 */
public class ButtonCompat extends LiquidGlassButton {

  private LinearLayout contentRow;
  private TextView label;
  private ImageView iconView;

  public ButtonCompat(Context context) {
    super(context);
    init();
  }

  public ButtonCompat(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public ButtonCompat(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    setEnableDynamicBackground(true);
    setMaterial(GlassMaterial.REGULAR);
    setEnableSensorHighlight(true);
    setEnableAdaptiveTint(true);
    setPressScale(0.96f);
    Glass.applyThemeTint(this);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    Glass.autoBackdrop(this);
  }

  /** Convenience alias for {@link #setText(CharSequence)}. */
  public void setLabel(CharSequence text) {
    setText(text);
  }

  /**
   * Puts a leading icon in front of the label.
   *
   * <p>Any tint baked into the drawable is cleared so the icon follows the button's adaptive
   * light/dark foreground like the label does.
   */
  public void setIconResource(int iconRes) {
    if (iconRes == 0) {
      setIconDrawable(null);
      return;
    }
    Drawable drawable = AppCompatResources.getDrawable(getContext(), iconRes);
    if (drawable == null) {
      return;
    }
    DrawableCompat.setTintList(drawable, null);
    setIconDrawable(drawable);
  }

  public void setIconDrawable(@Nullable Drawable drawable) {
    ensureContentRow();
    if (contentRow == null) {
      return;
    }
    if (drawable == null) {
      if (iconView != null) {
        iconView.setVisibility(View.GONE);
      }
      return;
    }
    if (iconView == null) {
      iconView = new ImageView(getContext());
      iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
      LinearLayout.LayoutParams iconParams =
          new LinearLayout.LayoutParams(
              Glass.dp(this, 18), Glass.dp(this, 18), Gravity.CENTER_VERTICAL);
      iconParams.setMarginEnd(Glass.dp(this, 8));
      contentRow.addView(iconView, 0, iconParams);
    }
    iconView.setImageDrawable(drawable);
    iconView.setVisibility(VISIBLE);
  }

  /**
   * Moves the library's centred label into a horizontal row so an icon can sit beside it. The
   * original label view is reused, so {@code android:text} from XML and every style applied to
   * it keep working.
   */
  private void ensureContentRow() {
    if (contentRow != null) {
      return;
    }
    label = getTextView();
    contentRow = new LinearLayout(getContext());
    contentRow.setOrientation(LinearLayout.HORIZONTAL);
    contentRow.setGravity(Gravity.CENTER);
    removeView(label);
    contentRow.addView(
        label,
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    addView(
        contentRow,
        new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
  }
}
