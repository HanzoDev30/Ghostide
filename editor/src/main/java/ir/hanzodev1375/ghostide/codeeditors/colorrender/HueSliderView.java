package ir.hanzodev1375.ghostide.codeeditors.colorrender;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;

public class HueSliderView extends View {

    private Paint thumbLinePaint;
    private Paint thumbArrowPaint;
    private Paint borderPaint;
    private float hue = 0f;
    private PointF thumbPos = new PointF();
    private OnHueChangedListener listener;
    private Bitmap hueBitmap;
    private Canvas bitmapCanvas;
    private Paint gradientPaint;

    public interface OnHueChangedListener {
        void onHueChanged(float hue);
    }

    public HueSliderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // خط نازک سفید برای نشان‌دهنده محل hue
        thumbLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbLinePaint.setStyle(Paint.Style.STROKE);
        thumbLinePaint.setStrokeWidth(2f);
        thumbLinePaint.setColor(Color.WHITE);

        // مثلث کوچک سمت راست
        thumbArrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumbArrowPaint.setStyle(Paint.Style.FILL);
        thumbArrowPaint.setColor(Color.WHITE);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1.5f);
        borderPaint.setColor(Color.DKGRAY);

        gradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void setHue(float hue) {
        this.hue = hue % 360f;
        updateThumbPosition();
        invalidate();
    }

    public void setOnHueChangedListener(OnHueChangedListener listener) {
        this.listener = listener;
    }

    private void updateHueBitmap() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        if (hueBitmap == null || hueBitmap.getWidth() != w || hueBitmap.getHeight() != h) {
            hueBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmapCanvas = new Canvas(hueBitmap);
        }

        // گرادیانت عمودی کامل (بالا قرمز، پایین دوباره قرمز)
        int[] colors = new int[361];
        float[] positions = new float[361];
        float[] hsv = new float[]{0f, 1f, 1f};
        for (int i = 0; i <= 360; i++) {
            hsv[0] = i;
            colors[i] = Color.HSVToColor(hsv);
            positions[i] = i / 360f;
        }

        LinearGradient gradient = new LinearGradient(
                0, 0, 0, h,
                colors, positions, Shader.TileMode.CLAMP
        );
        gradientPaint.setShader(gradient);
        bitmapCanvas.drawRect(0, 0, w, h, gradientPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateHueBitmap();
        updateThumbPosition();
    }

    private void updateThumbPosition() {
        if (getHeight() == 0) return;
        thumbPos.y = (hue / 360f) * getHeight();
        thumbPos.x = getWidth() / 2f;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            float y = event.getY();
            y = Math.max(0, Math.min(y, getHeight()));
            hue = (y / getHeight()) * 360f;
            updateThumbPosition();
            if (listener != null) listener.onHueChanged(hue);
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (hueBitmap != null) {
            canvas.drawBitmap(hueBitmap, 0, 0, null);
        }

        // خط افقی به پهنای نوار
        float centerX = getWidth() / 2f;
        float y = thumbPos.y;
        canvas.drawLine(0, y, getWidth(), y, thumbLinePaint);

        // مثلث کوچک سمت راست (فلش)
        float arrowSize = 8f;
        Path arrowPath = new Path();
        arrowPath.moveTo(getWidth() - 2f, y - arrowSize); // نقطه بالا
        arrowPath.lineTo(getWidth() + 6f, y);             // نوک مثلث (کمی بیرون‌زده)
        arrowPath.lineTo(getWidth() - 2f, y + arrowSize); // نقطه پایین
        arrowPath.close();
        canvas.drawPath(arrowPath, thumbArrowPaint);

        // دور مثلث رو هم خط باریک سیاه بکشیم تا معلوم باشه
        canvas.drawPath(arrowPath, borderPaint);
    }
}