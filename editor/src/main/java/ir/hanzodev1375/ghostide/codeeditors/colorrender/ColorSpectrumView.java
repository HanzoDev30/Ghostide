package ir.hanzodev1375.ghostide.codeeditors.colorrender;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

public class ColorSpectrumView extends View {

  private Paint paint;
  private Paint thumbPaint;
  private Paint borderPaint;
  private float hue = 0f; // 0-360
  private float saturation = 1f; // 0-1
  private float lightness = 0.5f; // 0-1
  private int alpha = 255;
  private PointF thumbPos = new PointF();
  private OnColorChangedListener listener;
  private Bitmap spectrumBitmap;
  private Canvas bitmapCanvas;

  public interface OnColorChangedListener {
    void onColorChanged(int colorArgb);
  }

  public ColorSpectrumView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    paint = new Paint();
    thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    thumbPaint.setStyle(Paint.Style.FILL);
    thumbPaint.setColor(Color.WHITE);
    borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    borderPaint.setStyle(Paint.Style.STROKE);
    borderPaint.setStrokeWidth(2f);
    borderPaint.setColor(Color.DKGRAY);
  }

  public void setHue(float hue) {
    this.hue = hue % 360f;
    updateSpectrum();
    updateThumbPosition();
    notifyColor();
    invalidate();
  }

  public void setColor(int color) {
    float[] hsl = new float[3];
    ColorUtils.RGBToHSL(Color.red(color), Color.green(color), Color.blue(color), hsl);
    this.hue = hsl[0];
    this.saturation = hsl[1];
    this.lightness = hsl[2];
    this.alpha = Color.alpha(color);
    updateSpectrum();
    updateThumbPosition();
    notifyColor();
    invalidate();
  }

  public void setAlpha(int alpha) {
    this.alpha = alpha;
    notifyColor();
    // No need to redraw spectrum, just notify
  }

  public void setOnColorChangedListener(OnColorChangedListener listener) {
    this.listener = listener;
  }

  private void updateSpectrum() {
    int w = getWidth();
    int h = getHeight();
    if (w <= 0 || h <= 0) return;

    if (spectrumBitmap == null
        || spectrumBitmap.getWidth() != w
        || spectrumBitmap.getHeight() != h) {
      spectrumBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
      bitmapCanvas = new Canvas(spectrumBitmap);
    }

    // Draw saturation (left 0 to right 1) and lightness (top 0 to bottom 1)
    // We use two LinearGradients: first a white-to-color (saturation), then black-to-transparent
    // (lightness)
    float[] hsv = new float[] {hue, 1f, 1f};
    int hueColor = Color.HSVToColor(hsv);

    // Create a gradient from white to pure hue color (saturation gradient)
    LinearGradient satGradient =
        new LinearGradient(0, 0, w, 0, Color.WHITE, hueColor, Shader.TileMode.CLAMP);
    Paint satPaint = new Paint();
    satPaint.setShader(satGradient);
    bitmapCanvas.drawRect(0, 0, w, h, satPaint);

    // Overlay with black at bottom (lightness gradient)
    LinearGradient lightGradient =
        new LinearGradient(0, 0, 0, h, Color.TRANSPARENT, Color.BLACK, Shader.TileMode.CLAMP);
    Paint lightPaint = new Paint();
    lightPaint.setShader(lightGradient);
    bitmapCanvas.drawRect(0, 0, w, h, lightPaint);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    updateSpectrum();
    updateThumbPosition();
  }

  private void updateThumbPosition() {
    if (getWidth() == 0 || getHeight() == 0) return;
    // X from saturation (0-1 -> 0..width)
    thumbPos.x = saturation * getWidth();
    // Y from lightness (0-1 -> height..0) because lightness 0 = top (black), 1 = bottom (white)
    // In VS Code, lightness top is white, bottom is black. So lightness 1.0 -> top (0), 0.0 ->
    // bottom (height)
    thumbPos.y = (1f - lightness) * getHeight();
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_DOWN
        || event.getAction() == MotionEvent.ACTION_MOVE) {
      float x = event.getX();
      float y = event.getY();
      x = Math.max(0, Math.min(x, getWidth()));
      y = Math.max(0, Math.min(y, getHeight()));
      saturation = x / getWidth();
      lightness = 1f - (y / getHeight());
      updateThumbPosition();
      notifyColor();
      invalidate();
      return true;
    }
    return super.onTouchEvent(event);
  }

  private void notifyColor() {
    if (listener != null) {
      float[] hsv = new float[] {hue, saturation, lightness};
      int rgb = Color.HSVToColor(hsv);
      int argb = Color.argb(alpha, Color.red(rgb), Color.green(rgb), Color.blue(rgb));
      listener.onColorChanged(argb);
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (spectrumBitmap != null) {
      canvas.drawBitmap(spectrumBitmap, 0, 0, null);
    }
    // Draw thumb (circle with border)
    float radius = 12f;
    thumbPaint.setColor(Color.WHITE);
    canvas.drawCircle(thumbPos.x, thumbPos.y, radius, thumbPaint);
    canvas.drawCircle(thumbPos.x, thumbPos.y, radius, borderPaint);
  }
}
