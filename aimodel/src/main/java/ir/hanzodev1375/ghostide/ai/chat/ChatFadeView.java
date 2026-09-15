package ir.hanzodev1375.ghostide.ai.chat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.SweepGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import ir.theme.M3Theme;

public class ChatFadeView extends FrameLayout {

  private final Paint topPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint bottomPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

  private LinearGradient topShader;
  private LinearGradient bottomShader;

  private int topHeight;
  private int bottomHeight;
  private int[] lastColors;

  public ChatFadeView(Context context) {
    this(context, null);
  }

  public ChatFadeView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    setWillNotDraw(false);
  }

  public void setFadeHeightsDp(int topDp, int bottomDp) {
    float density = getResources().getDisplayMetrics().density;
    setFadeHeights(Math.round(topDp * density), Math.round(bottomDp * density));
  }

  public void setFadeHeights(int topHeightPx, int bottomHeightPx) {
    if (topHeightPx != topHeight || bottomHeightPx != bottomHeight) {
      topHeight = topHeightPx;
      bottomHeight = bottomHeightPx;
      lastColors = null;
      invalidate();
    }
  }

  @Override
  protected void onDraw(@NonNull Canvas canvas) {
    super.onDraw(canvas);
    int[] colors = resolveFadeColors();
    if (!colorsMatch(colors, lastColors)) {
      updateShaders(colors);
      lastColors = colors;
    }

    if (topHeight > 0 && topShader != null) {
      topPaint.setShader(topShader);
      canvas.drawRect(0, 0, getWidth(), topHeight, topPaint);
    }
    if (bottomHeight > 0 && bottomShader != null) {
      bottomPaint.setShader(bottomShader);
      canvas.drawRect(0, getHeight() - bottomHeight, getWidth(), getHeight(), bottomPaint);
    }
  }

  @ColorInt
  private int[] resolveFadeColors() {
    int surface = safeColor(M3Theme.surface());
    int surfaceVariant = safeColor(M3Theme.surfaceVariant());
    int surfaceContainer = safeColor(M3Theme.surfaceContainer());
    int surfaceContainerHigh = safeColor(M3Theme.surfaceContainerHigh());
    int background = safeColor(M3Theme.background());

    return new int[] {surface, surfaceVariant, surfaceContainer, surfaceContainerHigh, background};
  }

  @ColorInt
  private int safeColor(Integer c) {
    return c != null ? c : Color.BLACK;
  }

  private boolean colorsMatch(int[] a, int[] b) {
    if (a == b) return true;
    if (a == null || b == null || a.length != b.length) return false;
    for (int i = 0; i < a.length; i++) {
      if (a[i] != b[i]) return false;
    }
    return true;
  }

  private void updateShaders(int[] c) {
    int surface = c[0];
    int surfaceVariant = c[1];
    int surfaceContainer = c[2];
    int surfaceContainerHigh = c[3];
    int background = c[4];

    int a0 = Color.alpha(surface);
    int a1 = Color.alpha(surfaceVariant);
    int a2 = Color.alpha(surfaceContainer);
    int a3 = Color.alpha(surfaceContainerHigh);

    topShader =
        new LinearGradient(
            0,
            0,
            0,
            Math.max(topHeight, 1),
            new int[] {
              withAlpha(surface, 0xFF * a0 / 255),
              withAlpha(surfaceVariant, 0xE0 * a1 / 255),
              withAlpha(surfaceContainer, 0xA0 * a2 / 255),
              withAlpha(surfaceContainerHigh, 0x50 * a3 / 255),
              withAlpha(background, 0x00),
            },
            null,
            Shader.TileMode.CLAMP);

    bottomShader =
        new LinearGradient(
            0,
            0,
            0,
            Math.max(bottomHeight, 1),
            new int[] {
              withAlpha(background, 0x00),
              withAlpha(surfaceContainerHigh, 0x50 * a3 / 255),
              withAlpha(surfaceContainer, 0xA0 * a2 / 255),
              withAlpha(surfaceVariant, 0xE0 * a1 / 255),
              withAlpha(surface, 0xFF * a0 / 255),
            },
            null,
            Shader.TileMode.CLAMP);
  }

  private int withAlpha(int color, int alpha) {
    return ColorUtils.setAlphaComponent(color, alpha);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return false;
  }
}
