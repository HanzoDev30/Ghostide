package ir.hanzodev1375.components.views;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.shape.ShapeAppearanceModel;

/**
 * Draws the rounded body of a progress button together with its horizontal fill.
 *
 * <p>The fill grows from the layout start edge, so it is right sided in RTL layouts. An instance is
 * owned by a single view, refreshed from {@code onSizeChanged} and painted before the children.
 */
final class RoundedProgressFill {

  private static final float DEFAULT_RADIUS_DP = 14f;

  private final Paint basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Path path = new Path();
  private final RectF bounds = new RectF();
  private final float defaultRadius;

  @Nullable private View source;
  private float progress;

  RoundedProgressFill(float density) {
    defaultRadius = DEFAULT_RADIUS_DP * density;
    basePaint.setColor(Color.DKGRAY);
    fillPaint.setColor(Color.GRAY);
  }

  void attach(@Nullable View source) {
    this.source = source;
  }

  void setColors(@ColorInt int base, @ColorInt int fill) {
    basePaint.setColor(base);
    fillPaint.setColor(fill);
  }

  void setProgress(float value) {
    progress = Math.max(0f, Math.min(1f, value));
  }

  float getProgress() {
    return progress;
  }

  boolean hasShape() {
    return !path.isEmpty();
  }

  void updateShape() {
    View view = source;
    if (view == null) return;
    bounds.set(0f, 0f, view.getWidth(), view.getHeight());
    float radius = defaultRadius;
    if (view instanceof MaterialButton) {
      ShapeAppearanceModel model = ((MaterialButton) view).getShapeAppearanceModel();
      if (model != null) {
        float size = model.getTopLeftCornerSize().getCornerSize(bounds);
        if (size > 0f) {
          radius = size;
        }
      }
    }
    path.reset();
    path.addRoundRect(bounds, radius, radius, Path.Direction.CW);
  }

  void draw(Canvas canvas) {
    if (path.isEmpty()) return;

    int save = canvas.save();
    canvas.clipPath(path);
    canvas.drawRect(bounds, basePaint);
    if (progress > 0f) {
      boolean rtl = source != null && ViewCompat.getLayoutDirection(source) == ViewCompat.LAYOUT_DIRECTION_RTL;
      float width = bounds.width() * progress;
      if (rtl) {
        canvas.drawRect(bounds.right - width, bounds.top, bounds.right, bounds.bottom, fillPaint);
      } else {
        canvas.drawRect(bounds.left, bounds.top, bounds.left + width, bounds.bottom, fillPaint);
      }
    }
    canvas.restoreToCount(save);
  }
}
