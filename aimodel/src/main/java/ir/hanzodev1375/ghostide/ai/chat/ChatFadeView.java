package ir.hanzodev1375.ghostide.ai.chat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import ir.theme.M3Theme;

/**
 * Telegram-like chat bubble background built from the editor's bracket palette. A {@link
 * SweepGradient} blends three bracket colors around the bubble center so they melt into each other
 * smoothly. The bubble is drawn as a rounded rect with a thin tail corner on the message side (right
 * for user bubbles, left for AI bubbles) so it reads like a chat bubble instead of a plain square.
 * The color wheel rotates live with the RecyclerView scroll offset.
 */
public class ChatFadeView extends FrameLayout {

  public static final int TAIL_NONE = 0;
  public static final int TAIL_RIGHT = 1;
  public static final int TAIL_LEFT = 2;

  private static final int PALETTE_SIZE = 3;
  private static final int STOP_ALPHA = 0xCC;

  private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Path bubblePath = new Path();

  private SweepGradient sweepShader;
  private int shaderKey = Integer.MIN_VALUE;
  private int tailSide = TAIL_NONE;
  private float density;

  public ChatFadeView(Context context) {
    this(context, null);
  }

  public ChatFadeView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    setWillNotDraw(false);
    density = getResources().getDisplayMetrics().density;
  }

  public void setTailSide(int side) {
    if (tailSide != side) {
      tailSide = side;
      buildPath();
      invalidate();
    }
  }

  public void setFadeHeightsDp(int topDp, int bottomDp) {
    float d = getResources().getDisplayMetrics().density;
    setFadeHeights(Math.round(topDp * d), Math.round(bottomDp * d));
  }

  public void setFadeHeights(int topHeightPx, int bottomHeightPx) {
    shaderKey = Integer.MIN_VALUE;
    invalidate();
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    buildPath();
  }

  private void buildPath() {
    bubblePath.reset();
    float w = getWidth();
    float h = getHeight();
    if (w <= 0 || h <= 0) {
      return;
    }
    float r = 16 * density;
    float tail = 12 * density;

    if (tailSide == TAIL_RIGHT) {
      buildRightTailPath(w, h, r, tail);
    } else if (tailSide == TAIL_LEFT) {
      buildLeftTailPath(w, h, r, tail);
    } else {
      buildRoundRectPath(w, h, r);
    }
  }

  private void buildRoundRectPath(float w, float h, float r) {
    Path p = bubblePath;
    p.moveTo(r, 0);
    p.lineTo(w - r, 0);
    p.quadTo(w, 0, w, r);
    p.lineTo(w, h - r);
    p.quadTo(w, h, w - r, h);
    p.lineTo(r, h);
    p.quadTo(0, h, 0, h - r);
    p.lineTo(0, r);
    p.quadTo(0, 0, r, 0);
    p.close();
  }

  private void buildRightTailPath(float w, float h, float r, float tail) {
    Path p = bubblePath;
    p.moveTo(r, 0);
    p.lineTo(w - tail, 0);
    p.lineTo(w, tail);
    p.lineTo(w, h - r);
    p.quadTo(w, h, w - r, h);
    p.lineTo(r, h);
    p.quadTo(0, h, 0, h - r);
    p.lineTo(0, r);
    p.quadTo(0, 0, r, 0);
    p.close();
  }

  private void buildLeftTailPath(float w, float h, float r, float tail) {
    Path p = bubblePath;
    p.moveTo(w - r, 0);
    p.lineTo(tail, 0);
    p.lineTo(0, tail);
    p.lineTo(0, h - r);
    p.quadTo(0, h, r, h);
    p.lineTo(w - r, h);
    p.quadTo(w, h, w, h - r);
    p.lineTo(w, r);
    p.quadTo(w, 0, w - r, 0);
    p.close();
  }

  @Override
  protected void onDraw(@NonNull Canvas canvas) {
    super.onDraw(canvas);
    int[] palette = M3Theme.bracketPalette();
    float t = scrollRatio();
    int key = buildKey(palette, t);
    if (key != shaderKey) {
      shaderKey = key;
      updateShader(palette, t);
    }
    if (sweepShader != null && getWidth() > 0 && getHeight() > 0) {
      paint.setShader(sweepShader);
      canvas.drawPath(bubblePath, paint);
    }
  }

  private float scrollRatio() {
    RecyclerView rv = findRecyclerView();
    if (rv == null) {
      return 0f;
    }
    int range = rv.computeVerticalScrollRange();
    int offset = rv.computeVerticalScrollOffset();
    if (range <= 0) {
      return 0f;
    }
    return Math.min(1f, Math.max(0f, offset / (float) range));
  }

  private RecyclerView findRecyclerView() {
    ViewParent p = getParent();
    while (p != null) {
      if (p instanceof RecyclerView) {
        return (RecyclerView) p;
      }
      p = p.getParent();
    }
    return null;
  }

  private int buildKey(int[] colors, float t) {
    int hash = Float.floatToIntBits(t);
    for (int c : colors) {
      hash = hash * 31 + c;
    }
    return hash;
  }

  /** Uses only the first three bracket colors to keep the bubble calm and readable. */
  private void updateShader(int[] colors, float t) {
    int base = ((int) (t * PALETTE_SIZE)) % PALETTE_SIZE;

    int[] stops = new int[PALETTE_SIZE + 1];
    float[] positions = new float[PALETTE_SIZE + 1];
    for (int i = 0; i <= PALETTE_SIZE; i++) {
      int c = colors[(base + i) % PALETTE_SIZE];
      stops[i] = ColorUtils.setAlphaComponent(c, STOP_ALPHA);
      positions[i] = i / (float) PALETTE_SIZE;
    }

    float cx = getWidth() * 0.5f;
    float cy = getHeight() * 0.55f;
    sweepShader = new SweepGradient(cx, cy, stops, positions);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return false;
  }
}