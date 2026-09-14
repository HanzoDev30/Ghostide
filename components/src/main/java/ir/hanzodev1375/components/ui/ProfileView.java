package ir.hanzodev1375.components.ui;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;

import jp.wasabeef.blurry.Blurry;

public class ProfileView {

  private final AppCompatActivity app;

  public ProfileView(AppCompatActivity app) {
    this.app = app;
  }

  public void bindImageView(View icons, String url , int errorIcon) {
    final View[] mainUiHolder = new View[1];
    icons.setOnLongClickListener(
        v -> {
          var ctx = v.getContext();
          var act = (Activity) ctx;
          var decorView = (ViewGroup) act.getWindow().getDecorView();
          var contentParent = (ViewGroup) act.findViewById(android.R.id.content);
          var mainUI = contentParent.getChildAt(0);
          mainUiHolder[0] = mainUI;

          var overlay = new FrameLayout(ctx);
          overlay.setLayoutParams(
              new FrameLayout.LayoutParams(
                  FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
          overlay.setClickable(true);

          var localBlurBg = new ImageView(ctx);
          localBlurBg.setLayoutParams(
              new FrameLayout.LayoutParams(
                  FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
          localBlurBg.setScaleType(ImageView.ScaleType.CENTER_CROP);
          overlay.addView(localBlurBg);

          var popup =
              new PopupWindow(
                  overlay,
                  ViewGroup.LayoutParams.MATCH_PARENT,
                  ViewGroup.LayoutParams.MATCH_PARENT);
          popup.setClippingEnabled(false);
          popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

          if (decorView != null) {
            try {
              Blurry.with(ctx)
                  .radius(25)
                  .sampling(4)
                  .color(Color.argb(70, 255, 255, 255))
                  .async()
                  .animate(300)
                  .capture(decorView)
                  .into(localBlurBg);

              if (mainUI != null) {
                mainUI.animate().scaleX(0.95f).scaleY(0.95f).setDuration(350).start();
              }
            } catch (Exception ignored) {
            }
          }

          var card = new CardView(ctx);
          var sizePx =
              (int)
                  android.util.TypedValue.applyDimension(
                      android.util.TypedValue.COMPLEX_UNIT_DIP,
                      250,
                      ctx.getResources().getDisplayMetrics());
          var cardParams = new FrameLayout.LayoutParams(sizePx, sizePx);
          cardParams.gravity = Gravity.CENTER;
          card.setLayoutParams(cardParams);
          card.setRadius(sizePx / 2f);
          card.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
          card.setScaleX(0.3f);
          card.setScaleY(0.3f);
          card.setAlpha(0f);
          card.setCardElevation(80f);

          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            card.setOutlineAmbientShadowColor(Color.parseColor("#FFFFFF"));
            card.setOutlineSpotShadowColor(Color.parseColor("#FFFFFF"));
          }

          var largeImg = new ImageView(ctx);
          largeImg.setLayoutParams(
              new ViewGroup.LayoutParams(
                  ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
          largeImg.setScaleType(ImageView.ScaleType.CENTER_CROP);
          card.addView(largeImg);
          overlay.addView(card);

          popup.showAtLocation(decorView, Gravity.CENTER, 0, 0);

          Glide.with(ctx.getApplicationContext()).load(url).error(errorIcon).into(largeImg);

          var overshoot = new OvershootInterpolator(1.8f);
          card.animate()
              .alpha(1f)
              .scaleX(1f)
              .scaleY(1f)
              .setDuration(400)
              .setInterpolator(overshoot)
              .start();
          overlay.setOnClickListener(
              view -> {
                if (mainUiHolder[0] != null) {
                  mainUiHolder[0].animate().scaleX(1f).scaleY(1f).setDuration(250).start();
                }

                overlay.animate().alpha(0f).setDuration(250).start();
                card.animate().scaleX(0.4f).scaleY(0.4f).alpha(0f).setDuration(250).start();
                new Handler(Looper.getMainLooper()).postDelayed(popup::dismiss, 250);
              });

          return true;
        });
  }
}
