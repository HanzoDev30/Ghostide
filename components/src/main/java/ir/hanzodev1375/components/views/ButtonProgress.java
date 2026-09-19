package ir.hanzodev1375.components.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.google.android.material.button.MaterialButton;
import ir.theme.M3Theme;

public class ButtonProgress extends FrameLayout {

  private static final long SPIN_DURATION = 900L;
  private static final long FADE_DURATION = 260L;

  private final MaterialButton button;
  private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RectF oval = new RectF();

  private final float ringWidth;

  private float sweepAngle = 90f;
  private float rotation = 0f;
  private float ringAlpha = 0f;
  private boolean loading = false;

  private ValueAnimator spinner;
  private ValueAnimator fadeAnimator;

  public ButtonProgress(@NonNull Context context) {
    this(context, null);
  }

  public ButtonProgress(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  private static int color(Integer value, int fallback) {
    return value != null ? value : fallback;
  }

  public ButtonProgress(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    Resources res = getResources();
    ringWidth = 2.5f * res.getDisplayMetrics().density;

    @ColorInt int ringColor = color(M3Theme.primary(), Color.GRAY);

    ringPaint.setStyle(Paint.Style.STROKE);
    ringPaint.setStrokeWidth(ringWidth);
    ringPaint.setStrokeCap(Paint.Cap.ROUND);
    ringPaint.setColor(ringColor);

    trackPaint.setStyle(Paint.Style.STROKE);
    trackPaint.setStrokeWidth(ringWidth);
    trackPaint.setStrokeCap(Paint.Cap.ROUND);
    trackPaint.setColor(ringColor);

    button = new MaterialButton(context);
    button.setMinHeight(0);
    button.setMinWidth(0);
    button.setAllCaps(false);
    M3Theme.button(button);
    addView(button, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
  }

  public MaterialButton getButton() {
    return button;
  }

  public void setText(CharSequence text) {
    button.setText(text);
  }

  public void setText(@StringRes int resId) {
    button.setText(resId);
  }

  public CharSequence getText() {
    return button.getText();
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

  public void setSweepAngle(float sweepAngle) {
    this.sweepAngle = sweepAngle;
  }

  public void setRingColor(@ColorInt int color) {
    ringPaint.setColor(color);
    trackPaint.setColor(color);
    invalidate();
  }

  public void startLoading() {
    if (loading) return;
    loading = true;
    setEnabled(false);
    animateRingTo(1f);
    startSpinner();
  }

  public void stopLoading() {
    if (!loading) return;
    loading = false;
    stopSpinner();
    animateRingTo(0f);
    setEnabled(true);
  }

  public void finishLoading(@Nullable final Runnable onFinished) {
    stopLoading();
    if (onFinished != null) postDelayed(onFinished, FADE_DURATION);
  }

  private void startSpinner() {
    if (spinner != null) spinner.cancel();
    spinner = ValueAnimator.ofFloat(0f, 360f);
    spinner.setDuration(SPIN_DURATION);
    spinner.setRepeatCount(ValueAnimator.INFINITE);
    spinner.setInterpolator(new LinearInterpolator());
    spinner.addUpdateListener(
        animation -> {
          rotation = (float) animation.getAnimatedValue();
          invalidate();
        });
    spinner.start();
  }

  private void stopSpinner() {
    if (spinner != null) {
      spinner.cancel();
      spinner = null;
    }
  }

  private void animateRingTo(float target) {
    if (fadeAnimator != null) fadeAnimator.cancel();
    fadeAnimator = ValueAnimator.ofFloat(ringAlpha, target);
    fadeAnimator.setDuration(FADE_DURATION);
    fadeAnimator.setInterpolator(new LinearInterpolator());
    fadeAnimator.addUpdateListener(
        animation -> {
          ringAlpha = (float) animation.getAnimatedValue();
          invalidate();
        });
    fadeAnimator.start();
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);
    if (ringAlpha <= 0.001f) return;

    int left = button.getLeft();
    int top = button.getTop();
    int right = button.getRight();
    int bottom = button.getBottom();

    float inset = ringWidth / 2f;
    oval.set(left + inset, top + inset, right - inset, bottom - inset);

    trackPaint.setAlpha((int) (ringAlpha * 60f));
    canvas.drawArc(oval, 0f, 360f, false, trackPaint);

    ringPaint.setAlpha(Math.min(255, (int) (ringAlpha * 255f)));
    canvas.drawArc(oval, rotation - 90f, sweepAngle, false, ringPaint);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    stopSpinner();
    if (fadeAnimator != null) {
      fadeAnimator.cancel();
      fadeAnimator = null;
    }
  }
}
