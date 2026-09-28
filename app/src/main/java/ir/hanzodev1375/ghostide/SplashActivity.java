package ir.hanzodev1375.ghostide;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import ir.hanzodev1375.ghostide.activity.BaseCompat;
import ir.hanzodev1375.ghostide.activity.FileManagerActivity;
import ir.hanzodev1375.ghostide.onboarding.OnboardingActivity;
import ir.hanzodev1375.ghostide.onboarding.OnboardingPrefs;
import ir.hanzodev1375.ghostide.utils.ObjectUtil;
import ir.theme.M3Theme;

public class SplashActivity extends BaseCompat {

  private boolean started = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);

    ImageView logo = findViewById(R.id.logo);
    logo.setAlpha(0f);
    logo
        .animate()
        .alpha(1f)
        .setDuration(800)
        .withEndAction(() -> logo.postDelayed(this::startFileManager, 1200))
        .start();

    M3Theme.apply(findViewById(android.R.id.content));
  }

  private void startFileManager() {
    if (started) return;
    started = true;
    ImageView logo = findViewById(R.id.logo);
    Intent intent;
    if (OnboardingPrefs.shouldShow(this)) {
      intent = new Intent(this, OnboardingActivity.class);
    } else {
      intent = new Intent(this, FileManagerActivity.class);
    }
    startActivityWithSharedElement(intent, logo, ObjectUtil.TRANSITION_LOGO);
    finish();
  }

  @Override
  protected void onResume() {
    super.onResume();
    startFileManager();
  }
}
