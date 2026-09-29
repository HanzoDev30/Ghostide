package ir.hanzodev1375.components.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.shape.ShapeAppearanceModel;
import ir.hanzodev1375.components.R;
import ir.theme.M3Theme;
import java.util.Locale;

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

  private final Paint basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Path shapePath = new Path();
  private final RectF bounds = new RectF();

  private final float density;

  private State state = State.IDLE;
  private boolean indeterminate = false;
  private float progress = 0f;
  private float displayedProgress = 0f;
  private ValueAnimator progressAnimator;
  private ValueAnimator spinner;

  @ColorInt private int baseColor = Color.GRAY;
  @ColorInt private int fillColor = Color.DKGRAY;
  @ColorInt private int contentColor = Color.WHITE;

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
    addView(button, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

    setClickable(true);
    setFocusable(true);
    setOnClickListener(
        v -> {
          if (state == State.IDLE) {
            OnInstallClickListener listener = installListener;
            if (listener != null) listener.onInstallClick();
          }
        });

    applyTheme();
    setState(State.IDLE);
  }

  public interface OnInstallClickListener {
    void onInstallClick();
  }

  private OnInstallClickListener installListener;

  public void setOnInstallClickListener(@Nullable OnInstallClickListener listener) {
    this.installListener = listener;
  }

  @Override
  public void setOnClickListener(@Nullable OnClickListener listener) {
    super.setOnClickListener(listener);
  }

  /** Re-reads the Material colors; call after a theme change. */
  public void applyTheme() {
    baseColor = color(M3Theme.surfaceContainerHigh(), Color.DKGRAY);
    fillColor = color(M3Theme.primaryContainer(), Color.GRAY);
    contentColor = color(M3Theme.onSurface(), Color.WHITE);
    ring.setColor(color(M3Theme.onSurfaceVariant(), Color.LTGRAY));
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
      progressAnimator =
          ValueAnimator.ofFloat(displayedProgress, target)
              .setDuration(PROGRESS_DURATION);
      progressAnimator.setInterpolator(new LinearInterpolator());
      progressAnimator.addUpdateListener(
          animation -> {
            displayedProgress = (float) animation.getAnimatedValue();
            progress = target;
            if (state == State.INSTALLING) {
              button.setText(buildInstallingText());
            }
            invalidate();
          });
      progressAnimator.start();
    } else {
      progress = target;
      displayedProgress = target;
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

  private void updateShape() {
    bounds.set(0f, 0f, getWidth(), getHeight());
    float radius = 14f * density;
    ShapeAppearanceModel model = button.getShapeAppearanceModel();
    if (model != null && getWidth() > 0 && getHeight() > 0) {
      float size = model.getTopLeftCornerSize().getCornerSize(bounds);
      if (size > 0f) {
        radius = size;
      }
    }
    shapePath.reset();
    shapePath.addRoundRect(bounds, radius, radius, Path.Direction.CW);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    updateShape();
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (shapePath.isEmpty()) {
      updateShape();
    }
    if (shapePath.isEmpty()) return;

    int save = canvas.save();
    canvas.clipPath(shapePath);

    basePaint.setColor(baseColor);
    canvas.drawRect(0f, 0f, getWidth(), getHeight(), basePaint);

    if (state == State.INSTALLING && displayedProgress > 0f) {
      fillPaint.setColor(fillColor);
      canvas.drawRect(0f, 0f, getWidth() * displayedProgress, getHeight(), fillPaint);
    }

    canvas.restoreToCount(save);
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
