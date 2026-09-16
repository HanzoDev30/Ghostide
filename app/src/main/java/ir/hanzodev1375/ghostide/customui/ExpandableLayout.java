package ir.hanzodev1375.ghostide.customui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import ir.hanzodev1375.components.animators.AnimationManager;
import ir.hanzodev1375.ghostide.GhostIdeAppLoader;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.codeeditors.setting.PreferencesUtils;
import ir.theme.M3Theme;

public class ExpandableLayout extends LinearLayout {
  private static final int ANIM_DURATION = 200;

  private TextView titleView;
  private ImageView arrowIcon;
  private RecyclerView recyclerView;
  private boolean isExpanded = false;
  private PreferencesUtils appsetting;
  private MaterialCardView card;
  private ValueAnimator heightAnimator;

  public ExpandableLayout(@NonNull Context context) {
    super(context);
    init(context);
  }

  public ExpandableLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public ExpandableLayout(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(Context context) {
    setOrientation(VERTICAL);
    LayoutInflater.from(context).inflate(R.layout.layout_expandable, this, true);
    titleView = findViewById(R.id.expandable_title);
    arrowIcon = findViewById(R.id.expandable_arrow);
    recyclerView = findViewById(R.id.expandable_recycler);
    card = findViewById(R.id.cardEx);
    findViewById(R.id.expandable_header).setOnClickListener(v -> toggle());
    recyclerView.setVisibility(GONE);
    appsetting = new PreferencesUtils(context);
    stepCard();
  }

  public void setTitle(String title) {
    titleView.setText(title);
  }

  public RecyclerView getRecyclerView() {
    return recyclerView;
  }

  void stepCard() {
    var isBack = GhostIdeAppLoader.getInstance().getSetting().isShowBackground();
    var color = M3Theme.surface();
    var stroke = M3Theme.outline();
    card.setCardBackgroundColor(isBack ? ColorUtils.setAlphaComponent(color, 128) : color);
    card.setStrokeColor(isBack ? ColorUtils.setAlphaComponent(stroke, 128) : stroke);
    M3Theme.textView(titleView);
    M3Theme.imageView(arrowIcon);
  }

  public void toggle() {
    if (isExpanded) collapse();
    else expand();
  }

  private void stopRunningAnimation() {
    if (heightAnimator != null && heightAnimator.isRunning()) {
      heightAnimator.cancel();
    }
    heightAnimator = null;
    arrowIcon.animate().cancel();
  }

  public void expand() {
    if (isExpanded) return;
    isExpanded = true;
    stopRunningAnimation();

    if (!AnimationManager.getInstance(getContext()).areAnimationsEnabled()) {
      resetRecyclerViewHeight();
      recyclerView.setVisibility(View.VISIBLE);
      arrowIcon.setRotation(90f);
      return;
    }

    recyclerView.setVisibility(View.VISIBLE);
    recyclerView.post(
        () -> {
          if (!isExpanded) return;
          int targetHeight = recyclerView.getHeight();
          if (targetHeight <= 0) return;
          LinearLayout.LayoutParams lp =
              (LinearLayout.LayoutParams) recyclerView.getLayoutParams();
          lp.height = 0;
          recyclerView.setLayoutParams(lp);
          heightAnimator = ValueAnimator.ofInt(0, targetHeight);
          heightAnimator
              .setDuration(ANIM_DURATION)
              .setInterpolator(new DecelerateInterpolator());
          heightAnimator.addUpdateListener(
              a -> {
                lp.height = (int) a.getAnimatedValue();
                recyclerView.setLayoutParams(lp);
              });
          heightAnimator.start();
        });
    arrowIcon.animate().rotation(90f).setDuration(ANIM_DURATION).start();
  }

  public void collapse() {
    if (!isExpanded) return;
    isExpanded = false;
    stopRunningAnimation();

    if (!AnimationManager.getInstance(getContext()).areAnimationsEnabled()) {
      recyclerView.setVisibility(View.GONE);
      resetRecyclerViewHeight();
      arrowIcon.setRotation(0f);
      return;
    }

    LinearLayout.LayoutParams lp =
        (LinearLayout.LayoutParams) recyclerView.getLayoutParams();
    int currentHeight = recyclerView.getHeight();
    if (currentHeight <= 0) {
      recyclerView.setVisibility(View.GONE);
      resetRecyclerViewHeight();
      arrowIcon.setRotation(0f);
      return;
    }
    heightAnimator = ValueAnimator.ofInt(currentHeight, 0);
    heightAnimator.setDuration(ANIM_DURATION).setInterpolator(new DecelerateInterpolator());
    heightAnimator.addUpdateListener(
        a -> {
          lp.height = (int) a.getAnimatedValue();
          recyclerView.setLayoutParams(lp);
        });
    heightAnimator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            recyclerView.setVisibility(View.GONE);
            resetRecyclerViewHeight();
          }
        });
    heightAnimator.start();
    arrowIcon.animate().rotation(0f).setDuration(ANIM_DURATION).start();
  }

  private void resetRecyclerViewHeight() {
    LinearLayout.LayoutParams lp =
        (LinearLayout.LayoutParams) recyclerView.getLayoutParams();
    lp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
    recyclerView.setLayoutParams(lp);
  }

  public boolean isExpanded() {
    return isExpanded;
  }
}