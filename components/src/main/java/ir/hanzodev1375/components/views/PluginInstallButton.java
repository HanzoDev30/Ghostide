package ir.hanzodev1375.components.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.ColorUtils;
import com.google.android.material.button.MaterialButton;
import ir.hanzodev1375.components.R;
import ir.theme.M3Theme;
import java.util.Locale;
import android.view.*;

/**
 * Install button whose body fills up as the download progresses.
 *
 * <p>The background shape and the progress fill are drawn in {@link #onDraw} so the label stays on
 * top, while the label itself (and the download icon) is rendered by an inner {@link MaterialButton}
 * with a transparent background.
 */
public class PluginInstallButton extends FrameLayout {

  public enum State {
    IDLE,
    INSTALLING,
    INSTALLED
  }

  private static final long SPIN_DURATION = 900L;
  private static final long PROGRESS_DURATION = 180L;
  private static final float SWEEP_ANGLE = 90f;

  private final MaterialButton button;
  private final RingDrawable ring;
  private final RoundedProgressFill fill;

  private final float density;

  private State state = State.IDLE;
  private boolean indeterminate = false;
  private float progress = 0f;
  private ValueAnimator progressAnimator;
  private ValueAnimator spinner;

  @StringRes private int idleTextRes = R.string.pluginstore_install;
  @StringRes private int installedTextRes = R.string.pluginstore_installed_button;
  @StringRes private int installingTextRes = R.string.pluginstore_busy;

  public PluginInstallButton(@NonNull Context context) {
    this(context, null);
  }

  public PluginInstallButton(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    density = getResources().getDisplayMetrics().density;
    setWillNotDraw(false);

    ring = new RingDrawable(density);

    button = new MaterialButton(context);
    button.setMinWidth(0);
    button.setMinHeight(0);
    button.setAllCaps(false);
    button.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));

    fill = new RoundedProgressFill(density);
    fill.attach(button);

    addView(button, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

    button.addOnLayoutChangeListener(
        (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
          fill.updateShape();
          invalidate();
        });

    setClickable(true);
    setFocusable(true);
    setOnClickListener(clickHandler);
    button.setOnClickListener(clickHandler);

    applyTheme();
    setState(State.IDLE);
  }

  public interface OnInstallClickListener {
    void onInstallClick();
  }

  private OnInstallClickListener installListener;

  private final View.OnClickListener clickHandler = this::handleClick;

  public void setOnInstallClickListener(@Nullable OnInstallClickListener listener) {
    this.installListener = listener;
  }

  private void handleClick(@NonNull View source) {
    if (state != State.IDLE || !isEnabled()) {
      return;
    }
    OnInstallClickListener listener = installListener;
    if (listener != null) {
      listener.onInstallClick();
    }
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    button.setEnabled(enabled);
  }

  /** Re-reads the Material colors; call after a theme change. */
  public void applyTheme() {
    int primaryColor = color(M3Theme.primary(), Color.DKGRAY);
    int contentColor = color(M3Theme.onPrimary(), Color.WHITE);
    ring.setColor(contentColor);
    fill.setColors(primaryColor, ColorUtils.blendARGB(primaryColor, Color.BLACK, 0.30f));
    button.setTextColor(contentColor);
    button.setIconTint(ColorStateList.valueOf(contentColor));
    M3Theme.button(button);
    button.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
    invalidate();
  }

  public void setState(State next) {
    this.state = next;
    indeterminate = false;
    switch (next) {
      case INSTALLING:
        button.setEnabled(false);
        button.setText(buildInstallingText());
        ring.setVisible(true, false);
        button.setIcon(ring);
        startSpinner();
        break;
      case INSTALLED:
        stopSpinner();
        button.setEnabled(false);
        button.setText(installedTextRes);
        button.setIcon(null);
        setProgressInternal(1f, false);
        break;
      case IDLE:
      default:
        stopSpinner();
        button.setEnabled(true);
        button.setText(idleTextRes);
        setIcon(R.drawable.ic_download);
        setProgressInternal(0f, false);
        break;
    }
    invalidate();
  }

  public State getState() {
    return state;
  }

  /**
   * Sets the download progress. Pass a negative value when the total size is unknown to get an
   * indeterminate fill.
   */
  public void setProgress(float value) {
    indeterminate = value < 0f;
    if (indeterminate) {
      setProgressInternal(0.35f, false);
      return;
    }
    setProgressInternal(value, true);
  }

  public float getProgress() {
    return progress;
  }

  public void setText(@StringRes int resId) {
    this.idleTextRes = resId;
    if (state == State.IDLE) {
      button.setText(resId);
    }
  }

  public void setInstalledText(@StringRes int resId) {
    this.installedTextRes = resId;
    if (state == State.INSTALLED) {
      button.setText(resId);
    }
  }

  private String buildInstallingText() {
    if (indeterminate) {
      return getContext().getString(installingTextRes);
    }
    int percent = (int) (progress * 100f);
    return String.format(
        Locale.getDefault(), "%s %d%%", getContext().getString(installingTextRes), percent);
  }

  private void setProgressInternal(float value, boolean animate) {
    float target = Math.max(0f, Math.min(1f, value));
    if (progressAnimator != null) {
      progressAnimator.cancel();
      progressAnimator = null;
    }
    if (animate) {
      progressAnimator = ValueAnimator.ofFloat(fill.getProgress(), target);
      progressAnimator.setDuration(PROGRESS_DURATION);
      progressAnimator.setInterpolator(new LinearInterpolator());
      progressAnimator.addUpdateListener(
          animation -> {
            float current = (float) animation.getAnimatedValue();
            fill.setProgress(state == State.INSTALLING ? current : 0f);
            progress = current;
            if (state == State.INSTALLING) {
              button.setText(buildInstallingText());
            }
            invalidate();
          });
      progressAnimator.start();
    } else {
      progress = target;
      fill.setProgress(target);
      invalidate();
    }
    if (state == State.INSTALLING && !animate) {
      button.setText(buildInstallingText());
    }
  }

  private void startSpinner() {
    if (spinner != null) return;
    spinner = ValueAnimator.ofFloat(0f, 360f);
    spinner.setDuration(SPIN_DURATION);
    spinner.setRepeatCount(ValueAnimator.INFINITE);
    spinner.setInterpolator(new LinearInterpolator());
    spinner.addUpdateListener(
        animation -> {
          ring.setRotation((float) animation.getAnimatedValue());
        });
    spinner.start();
  }

  private void stopSpinner() {
    if (spinner != null) {
      spinner.cancel();
      spinner = null;
    }
    ring.setVisible(false, false);
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

  public void setIcon(@DrawableRes int iconRes) {
    button.setIcon(AppCompatResources.getDrawable(getContext(), iconRes));
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    stopSpinner();
    if (progressAnimator != null) {
      progressAnimator.cancel();
      progressAnimator = null;
    }
  }

  private static int color(@Nullable Integer value, int fallback) {
    return value != null ? value : fallback;
  }

  /** Small rotating arc used as the button icon while downloading. */
  private static final class RingDrawable extends Drawable {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF oval = new RectF();
    private float rotation;

    RingDrawable(float density) {
      paint.setStyle(Paint.Style.STROKE);
      paint.setStrokeWidth(2f * density);
      paint.setStrokeCap(Paint.Cap.ROUND);
      int size = Math.round(16f * density);
      setBounds(0, 0, size, size);
    }

    void setColor(@ColorInt int color) {
      paint.setColor(color);
      invalidateSelf();
    }

    void setRotation(float value) {
      this.rotation = value;
      invalidateSelf();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
      Rect b = getBounds();
      float inset = paint.getStrokeWidth() / 2f;
      oval.set(b.left + inset, b.top + inset, b.right - inset, b.bottom - inset);
      paint.setAlpha(70);
      canvas.drawArc(oval, 0f, 360f, false, paint);
      paint.setAlpha(255);
      canvas.drawArc(oval, rotation - 90f, SWEEP_ANGLE, false, paint);
    }

    @Override
    public void setAlpha(int alpha) {
      paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
      paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
      return PixelFormat.TRANSLUCENT;
    }
  }
}
