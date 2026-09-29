package ir.hanzodev1375.components.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.google.android.material.button.MaterialButton;
import ir.theme.M3Theme;
import java.util.Locale;

/**
 * Button that reports its download progress through its own label, the way Xed Editor does.
 *
 * <p>No spinner or ring is drawn: while loading the label becomes {@code <label> 42%} and the body
 * fills up with {@code primaryContainer}. When the total size is unknown the label animates through
 * a dot pattern instead and the body stays empty.
 */
public class ButtonProgress extends FrameLayout {

  private static final long PROGRESS_DURATION = 180L;
  private static final long DOTS_DURATION = 1300L;
  private static final String[] DOTS = {"", ".", "..", "..."};
  private static final float COLOR_SWITCH_AT = 0.5f;

  private final MaterialButton button;
  private final RoundedProgressFill fill;
  private final float density;

  private CharSequence idleText = "";
  @Nullable private CharSequence loadingText;
  private boolean loading;
  private boolean indeterminate = true;
  private float displayedProgress;
  private String shownLabel = "";
  private int appliedTextColor = Color.TRANSPARENT;

  private ValueAnimator progressAnimator;
  private ValueAnimator dotsAnimator;

  public ButtonProgress(@NonNull Context context) {
    this(context, null);
  }

  public ButtonProgress(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ButtonProgress(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    density = getResources().getDisplayMetrics().density;
    setWillNotDraw(false);

    button = new MaterialButton(context);
    button.setMinHeight(0);
    button.setMinWidth(0);
    button.setAllCaps(false);

    fill = new RoundedProgressFill(density);
    fill.attach(button);

    addView(button, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    applyTheme();
  }

  /** Re-reads the Material colors; call after a theme change. */
  public void applyTheme() {
    M3Theme.button(button);
    button.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
    fill.setColors(
        color(M3Theme.primary(), Color.DKGRAY), color(M3Theme.primaryContainer(), Color.LTGRAY));
    applyTextColor(true);
    invalidate();
  }

  public MaterialButton getButton() {
    return button;
  }

  public void setText(CharSequence text) {
    idleText = text == null ? "" : text;
    if (loading) {
      updateLabel();
    } else {
      shownLabel = idleText.toString();
      button.setText(idleText);
    }
  }

  public void setText(@StringRes int resId) {
    setText(getContext().getString(resId));
  }

  /** Overrides the label prefix used while loading, e.g. "Downloading". */
  public void setLoadingText(@Nullable CharSequence text) {
    loadingText = text;
    if (loading) {
      updateLabel();
    }
  }

  /** Returns the label the button falls back to once loading ends. */
  public CharSequence getText() {
    return idleText;
  }

  @Override
  public void setOnClickListener(@Nullable OnClickListener listener) {
    button.setOnClickListener(listener);
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    button.setEnabled(enabled);
  }

  public boolean isLoading() {
    return loading;
  }

  public float getProgress() {
    return displayedProgress;
  }

  public void setFillColor(@ColorInt int color) {
    fill.setColors(color(M3Theme.primary(), Color.DKGRAY), color);
    invalidate();
  }

  public void startLoading() {
    if (loading) return;
    if (button.getText() != null) {
      idleText = button.getText();
    }
    loading = true;
    indeterminate = true;
    setEnabled(false);
    startDots();
    applyTextColor();
    updateLabel();
    invalidate();
  }

  public void stopLoading() {
    if (!loading) return;
    loading = false;
    indeterminate = true;
    displayedProgress = 0f;
    stopDots();
    stopProgressAnimator();
    fill.setProgress(0f);
    setEnabled(true);
    applyTextColor();
    shownLabel = idleText.toString();
    button.setText(idleText);
    invalidate();
  }

  public void finishLoading(@Nullable final Runnable onFinished) {
    stopLoading();
    if (onFinished != null) postDelayed(onFinished, PROGRESS_DURATION);
  }

  /**
   * Sets the download progress. Pass a negative value when the total size is unknown to get the
   * animated dot label without any fill.
   */
  public void setProgress(float value) {
    if (!loading) return;
    if (value < 0f) {
      if (!indeterminate) {
        indeterminate = true;
        stopProgressAnimator();
        displayedProgress = 0f;
        fill.setProgress(0f);
        applyTextColor();
        startDots();
        invalidate();
      }
      return;
    }
    indeterminate = false;
    stopDots();
    animateTo(Math.max(0f, Math.min(1f, value)));
  }

  private void animateTo(float target) {
    stopProgressAnimator();
    progressAnimator = ValueAnimator.ofFloat(displayedProgress, target);
    progressAnimator.setDuration(PROGRESS_DURATION);
    progressAnimator.setInterpolator(new LinearInterpolator());
    progressAnimator.addUpdateListener(
        animation -> {
          displayedProgress = (float) animation.getAnimatedValue();
          fill.setProgress(displayedProgress);
          applyTextColor();
          updateLabel();
          invalidate();
        });
    progressAnimator.start();
  }

  private void startDots() {
    if (dotsAnimator != null) return;
    dotsAnimator = ValueAnimator.ofFloat(0f, 1f);
    dotsAnimator.setDuration(DOTS_DURATION);
    dotsAnimator.setRepeatCount(ValueAnimator.INFINITE);
    dotsAnimator.setInterpolator(new LinearInterpolator());
    dotsAnimator.addUpdateListener(
        animation -> {
          float phase = (float) animation.getAnimatedValue();
          updateLabel(phase);
        });
    dotsAnimator.start();
  }

  private void stopDots() {
    if (dotsAnimator == null) return;
    dotsAnimator.cancel();
    dotsAnimator = null;
  }

  private void stopProgressAnimator() {
    if (progressAnimator == null) return;
    progressAnimator.cancel();
    progressAnimator = null;
  }

  private void updateLabel() {
    updateLabel(0f);
  }

  private void updateLabel(float dotsPhase) {
    if (!loading) return;
    CharSequence base = baseLabel();
    String label;
    if (indeterminate) {
      String dots = DOTS[((int) (dotsPhase * DOTS.length)) % DOTS.length];
      label = dots.isEmpty() ? base.toString() : base + " " + dots;
    } else {
      int percent = Math.round(displayedProgress * 100f);
      label = String.format(Locale.getDefault(), "%s %d%%", base, percent);
    }
    if (label.equals(shownLabel)) return;
    shownLabel = label;
    button.setText(label);
  }

  private CharSequence baseLabel() {
    return loadingText != null ? loadingText : idleText;
  }

  private void applyTextColor() {
    applyTextColor(false);
  }

  private void applyTextColor(boolean force) {
    int next =
        indeterminate || displayedProgress < COLOR_SWITCH_AT
            ? color(M3Theme.onPrimary(), Color.WHITE)
            : color(M3Theme.onPrimaryContainer(), Color.BLACK);
    if (!force && next == appliedTextColor) return;
    appliedTextColor = next;
    button.setTextColor(next);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    fill.updateShape();
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (!fill.hasShape()) {
      fill.updateShape();
    }
    fill.draw(canvas);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    stopDots();
    stopProgressAnimator();
  }

  private static int color(@Nullable Integer value, int fallback) {
    return value != null ? value : fallback;
  }
}
