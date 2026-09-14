package ir.hanzodev1375.ghostide.themeengine;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerSize;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

public class CircleImageView extends ShapeableImageView {

  private ValueAnimator shapeDrawableAnimator;

  public CircleImageView(Context context) {
    super(context);
    init();
  }

  public CircleImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    ShapeAppearanceModel shapeAppearanceModel =
        new ShapeAppearanceModel()
            .toBuilder().setAllCornerSizes((CornerSize) rect -> rect.height() / 2f).build();
    setBackground(new MaterialShapeDrawable(shapeAppearanceModel));
  }

  @Override
  public void setBackgroundColor(int color) {
    ((MaterialShapeDrawable) getBackground()).setFillColor(ColorStateList.valueOf(color));
  }

  public void setBackgroundColorRes(@ColorRes int color) {
    setBackgroundColor(ContextCompat.getColor(getContext(), color));
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, widthMeasureSpec);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
//    if (event.getAction() == MotionEvent.ACTION_DOWN) {
//      shapeDrawableAnimator = ValueAnimator.ofFloat(2f, 4f);
//      shapeDrawableAnimator.addUpdateListener(
//          animation -> {
//            float value = (float) animation.getAnimatedValue();
//            ((MaterialShapeDrawable) getBackground())
//                .setCornerSize((CornerSize) rect -> rect.height() / value);
//          });
//      shapeDrawableAnimator.start();
//    } else if (event.getAction() == MotionEvent.ACTION_UP
//        || event.getAction() == MotionEvent.ACTION_CANCEL) {
//      if (shapeDrawableAnimator != null) shapeDrawableAnimator.reverse();
//    }
    return super.onTouchEvent(event);
  }
}
