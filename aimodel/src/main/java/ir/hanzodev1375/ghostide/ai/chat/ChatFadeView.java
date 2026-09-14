package ir.hanzodev1375.ghostide.ai.chat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import ir.theme.M3Theme;

public class ChatFadeView extends View {

  private final Paint topPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint bottomPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

  private LinearGradient topShader;
  private LinearGradient bottomShader;

  private int topHeight;
  private int bottomHeight;
  private int lastColor = Color.TRANSPARENT;

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
      lastColor = Color.TRANSPARENT;
      invalidate();
    }
  }

  @Override
  protected void onDraw(@NonNull Canvas canvas) {
    super.onDraw(canvas);
    int color = resolveFadeColor();
    updateShaders(color);

    if (topHeight > 0) {
      topPaint.setShader(topShader);
      canvas.drawRect(0, 0, getWidth(), topHeight, topPaint);
    }
    if (bottomHeight > 0) {
      bottomPaint.setShader(bottomShader);
      canvas.drawRect(0, getHeight() - bottomHeight, getWidth(), getHeight(), bottomPaint);
    }
  }

  @ColorInt
  private int resolveFadeColor() {
    Integer surface = M3Theme.surface();
    if (surface != null) {
      return surface;
    }
    Integer background = M3Theme.background();
    if (background != null) {
      return background;
    }
    return Color.BLACK;
  }

  private void updateShaders(int color) {
    if (color == lastColor && topShader != null) {
      return;
    }
    lastColor = color;
    int a = Color.alpha(color);

    topShader =
        new LinearGradient(
            0,
            0,
            0,
            Math.max(topHeight, 1),
            new int[] {
              withAlpha(color, 0xFF * a / 255),
              withAlpha(color, 0xE8 * a / 255),
              withAlpha(color, 0xB0 * a / 255),
              withAlpha(color, 0x60 * a / 255),
              withAlpha(color, 0),
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
              withAlpha(color, 0),
              withAlpha(color, 0x60 * a / 255),
              withAlpha(color, 0xB0 * a / 255),
              withAlpha(color, 0xE8 * a / 255),
              withAlpha(color, 0xFF * a / 255),
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
