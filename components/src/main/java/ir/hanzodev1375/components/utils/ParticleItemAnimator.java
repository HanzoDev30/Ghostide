package ir.hanzodev1375.components.utils;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import ir.hanzodev1375.components.effect.ParticleSmasher;
import ir.hanzodev1375.components.effect.SmashAnimator;
import ir.hanzodev1375.components.effect.Utils;
import java.util.HashMap;
import java.util.Map;

public class ParticleItemAnimator extends DefaultItemAnimator {

  private final Activity activity;
  private final Map<RecyclerView.ViewHolder, SmashAnimator> runningAnimations = new HashMap<>();

  public ParticleItemAnimator(Activity activity) {
    this.activity = activity;
  }

  @Override
  public boolean animateRemove(RecyclerView.ViewHolder holder) {
    if (runningAnimations.containsKey(holder)) {
      return super.animateRemove(holder);
    }

    View itemView = holder.itemView;
    ParticleSmasher smasher = new ParticleSmasher(activity);

    SmashAnimator animator =
        smasher
            .with(itemView)
            .setStyle(SmashAnimator.STYLE_EXPLOSION)
            .setDuration(800)
            .setStartDelay(100)
            .setParticleRadius(Utils.dp2Px(3))
            .addAnimatorListener(
                new SmashAnimator.OnAnimatorListener() {
                  @Override
                  public void onAnimatorEnd() {
                    runningAnimations.remove(holder);
                    dispatchRemoveFinished(holder);
                    if (smasher.getParent() != null) {
                      ((ViewGroup) smasher.getParent()).removeView(smasher);
                    }
                  }
                });

    runningAnimations.put(holder, animator);
    animator.start();
    return true;
  }

  @Override
  public boolean animateAdd(RecyclerView.ViewHolder holder) {
    // بدون انیمیشن ذره‌ای – فقط به RecyclerView بگوییم که افزودن تمام شد
    dispatchAddStarting(holder);
    dispatchAddFinished(holder);
    return true;
  }

  @Override
  public void endAnimation(RecyclerView.ViewHolder item) {
    SmashAnimator animator = runningAnimations.get(item);
    if (animator != null) {
      // در صورت نیاز متوقف کنید
    }
    super.endAnimation(item);
  }

  @Override
  public void endAnimations() {
    runningAnimations.clear();
    super.endAnimations();
  }

  @Override
  public boolean isRunning() {
    return !runningAnimations.isEmpty() || super.isRunning();
  }
}
