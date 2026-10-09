package irhanzodev1375.musicpreview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Gravity;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.google.android.material.color.MaterialColors;
import ir.theme.M3Theme;
import irhanzodev1375.musicpreview.databinding.MusicLayoutBinding;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MusicView extends FrameLayout implements MusicPlayerBottomSheetFragment.MusicControl {

  private static final int SEEK_STEP_MS = 10000;
  private static final long FADE_OUT_MS = 220L;
  private static final long FADE_IN_MS = 480L;
  private static final long END_FADE_WINDOW_MS = 1400L;

  private static final PathInterpolator M3_STANDARD =
      new PathInterpolator(0.2f, 0f, 0f, 1f);

  private MusicLayoutBinding bind;
  private Music music;
  private String musicPath;
  private MediaPlayerListener externalListener;
  private Runnable onMusicClickListener;
  private String songName = "";
  private final Handler mainHandler = new Handler(Looper.getMainLooper());

  private final List<String> playlist = new ArrayList<>();
  private int playlistIndex = -1;
  private MusicPlayerBottomSheetFragment.OnTrackChangedListener trackChangedListener;
  private ValueAnimator volumeAnimator;
  private boolean endFadeStarted;

  public MusicView(Context c) {
    super(c);
    init();
  }

  public MusicView(Context c, AttributeSet set) {
    super(c, set);
    init();
  }

  private void init() {
    bind = MusicLayoutBinding.inflate(LayoutInflater.from(getContext()), this, true);
    View rootMusicView = bind.getRoot();
    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER);
    rootMusicView.setLayoutParams(params);

    bind.play.setOnClickListener(v -> togglePlayback());
    bind.previous.setOnClickListener(v -> seekBackward());
    bind.next.setOnClickListener(v -> seekForward());
    rootMusicView.setOnClickListener(
        v -> {
          if (onMusicClickListener != null) {
            onMusicClickListener.run();
          }
        });
    stepBackground(rootMusicView);
    M3Theme.apply(this);
  }

  public void setOnMusicClickListener(Runnable listener) {
    this.onMusicClickListener = listener;
  }

  public void setSongName(String name) {
    this.songName = name;
  }

  public String getSongName() {
    return this.songName;
  }

  private void togglePlayback() {
    if (music == null) {
      return;
    }
    if (music.isPlaying()) {
      pause();
    } else {
      play();
    }
  }

  void stepBackground(View v) {
    var gd = new GradientDrawable();
    gd.setShape(GradientDrawable.RECTANGLE);
    gd.setColor(MaterialColors.getColor(v, R.attr.colorSurface));
    gd.setStroke(3, MaterialColors.getColor(v, R.attr.colorOutline));
    gd.setCornerRadius(25f);
    v.setBackground(gd);
  }

  private void seekBackward() {
    if (music == null) {
      return;
    }
    int target = music.getCurrentDuration() - SEEK_STEP_MS;
    music.seekTo(Math.max(target, 0));
  }

  private void seekForward() {
    if (music == null) {
      return;
    }
    int target = music.getCurrentDuration() + SEEK_STEP_MS;
    music.seekTo(Math.min(target, music.getDuration()));
  }

  private void updatePlayIcon(boolean isPlaying) {
    bind.play.setImageResource(
        isPlaying ? R.drawable.icon_pause_round : R.drawable.icon_play_arrow_round); //fix 
  }

  public String getMusicPath() {
    return this.musicPath;
  }

  public void setMusicPath(String musicPath) {
    loadTrack(musicPath, false);
  }

  public void setPlaylist(List<String> paths, int startIndex) {
    playlist.clear();
    if (paths != null) {
      playlist.addAll(paths);
    }
    if (playlist.isEmpty()) {
      playlistIndex = -1;
      return;
    }
    playlistIndex = Math.max(0, Math.min(startIndex, playlist.size() - 1));
    setMusicPath(playlist.get(playlistIndex));
  }

  public List<String> getPlaylist() {
    return new ArrayList<>(playlist);
  }

  public int getPlaylistIndex() {
    return playlistIndex;
  }

  public boolean hasNext() {
    return playlistIndex >= 0 && playlistIndex < playlist.size() - 1;
  }

  public boolean hasPrevious() {
    return playlistIndex > 0;
  }

  public void next() {
    if (!hasNext()) return;
    playlistIndex++;
    loadTrack(playlist.get(playlistIndex), true);
  }

  public void previous() {
    if (!hasPrevious()) return;
    playlistIndex--;
    loadTrack(playlist.get(playlistIndex), true);
  }

  public void setOnTrackChangedListener(MusicPlayerBottomSheetFragment.OnTrackChangedListener listener) {
    this.trackChangedListener = listener;
  }

  private void loadTrack(String musicPath, boolean autoPlay) {
    cancelVolumeAnimation();
    endFadeStarted = false;
    this.musicPath = musicPath;
    if (music != null) {
      music.release();
      music = null;
    }
    if (musicPath == null) {
      return;
    }
    music = new Music(getContext(), musicPath);
    music.setMediaPlayerListener(
        new MediaPlayerListener() {
          @Override
          public void isPlaying(int currentDuration) {
            checkEndFade(currentDuration);
            if (externalListener != null) {
              externalListener.isPlaying(currentDuration);
            }
          }

          @Override
          public void onPause() {
            updatePlayIcon(false);
            if (externalListener != null) {
              externalListener.onPause();
            }
          }

          @Override
          public void onStart() {
            updatePlayIcon(true);
            if (externalListener != null) {
              externalListener.onStart();
            }
          }

          @Override
          public void onComplete() {
            updatePlayIcon(false);
            if (externalListener != null) {
              externalListener.onComplete();
            }
            handleCompletion();
          }
        });
    if (musicPath.startsWith("http://") || musicPath.startsWith("https://")) {
      music.setUrlSource(musicPath);
    } else {
      music.setPathSource(new File(musicPath));
    }
    updatePlayIcon(false);
    try {
      bind.nameartist.setText(music.getNameArtist());
    } catch (Exception err) {
      bind.nameartist.setText("");
    }

    if (music.getImageBitmap() != null) {
      bind.musiccaver.setImageBitmap(music.getImageBitmap());
      applyPaletteFromBitmap(music.getImageBitmap());
    } else {
      bind.musiccaver.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    if (autoPlay) {
      play();
    }
    if (trackChangedListener != null) {
      trackChangedListener.onTrackChanged(musicPath);
    }
  }

  private void handleCompletion() {
    cancelVolumeAnimation();
    if (hasNext()) {
      playlistIndex++;
      loadTrack(playlist.get(playlistIndex), true);
    } else if (music != null) {
      music.setVolume(1f);
    }
  }

  private void checkEndFade(int currentDuration) {
    if (endFadeStarted || music == null || !music.isPlaying()) return;
    int duration = music.getDuration();
    if (duration <= 0) return;
    long remaining = duration - currentDuration;
    if (remaining > 0 && remaining <= END_FADE_WINDOW_MS) {
      endFadeStarted = true;
      cancelVolumeAnimation();
      volumeAnimator = ValueAnimator.ofFloat(1f, 0f);
      volumeAnimator.setDuration(Math.max(200L, remaining));
      volumeAnimator.setInterpolator(M3_STANDARD);
      volumeAnimator.addUpdateListener(
          a -> {
            if (music != null) music.setVolume((float) a.getAnimatedValue());
          });
      volumeAnimator.start();
    }
  }

  private void cancelVolumeAnimation() {
    if (volumeAnimator != null) {
      volumeAnimator.removeAllUpdateListeners();
      volumeAnimator.removeAllListeners();
      volumeAnimator.cancel();
      volumeAnimator = null;
    }
  }

  public void setMediaPlayerListener(MediaPlayerListener listener) {
    this.externalListener = listener;
  }

  public void release() {
    cancelVolumeAnimation();
    if (music != null) {
      music.release();
      music = null;
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    release();
  }

  @Override
  public void play() {
    if (music == null || music.isPlaying()) return;
    cancelVolumeAnimation();
    endFadeStarted = false;
    music.setVolume(0f);
    music.start();
    volumeAnimator = ValueAnimator.ofFloat(0f, 1f);
    volumeAnimator.setDuration(FADE_IN_MS);
    volumeAnimator.setInterpolator(M3_STANDARD);
    volumeAnimator.addUpdateListener(
        a -> {
          if (music != null) music.setVolume((float) a.getAnimatedValue());
        });
    volumeAnimator.start();
  }

  @Override
  public void pause() {
    if (music == null || !music.isPlaying()) return;
    cancelVolumeAnimation();
    Music target = music;
    volumeAnimator = ValueAnimator.ofFloat(1f, 0f);
    volumeAnimator.setDuration(FADE_OUT_MS);
    volumeAnimator.setInterpolator(M3_STANDARD);
    volumeAnimator.addUpdateListener(a -> target.setVolume((float) a.getAnimatedValue()));
    volumeAnimator.addListener(
        new AnimatorListenerAdapter() {
          private boolean cancelled;

          @Override
          public void onAnimationCancel(Animator animation) {
            cancelled = true;
          }

          @Override
          public void onAnimationEnd(Animator animation) {
            if (cancelled) return;
            target.pause();
            target.setVolume(1f);
          }
        });
    volumeAnimator.start();
  }

  @Override
  public boolean isPlaying() {
    return music != null && music.isPlaying();
  }

  @Override
  public void seekTo(int position) {
    if (music != null) music.seekTo(position);
  }

  @Override
  public int getCurrentPosition() {
    return music != null ? music.getCurrentDuration() : 0;
  }

  @Override
  public int getDuration() {
    return music != null ? music.getDuration() : 0;
  }

  @Override
  public Bitmap getAlbumArt() {
    return music != null ? music.getImageBitmap() : null;
  }

  @Override
  public String getArtistName() {
    if (music == null) return null;
    try {
      return music.getNameArtist();
    } catch (Exception e) {
      return null;
    }
  }

  private void applyPaletteFromBitmap(Bitmap bitmap) {
    ColorPaletteUtils.generateFromBitmap(
        bitmap,
        (lightColors, darkColors) -> {
          boolean isDark = isNightMode();
          Map<String, Integer> palette = isDark ? darkColors : lightColors;
          if (palette != null && !palette.isEmpty()) {
            mainHandler.post(() -> applyColorsFromPalette(palette));
          }
        });
  }

  private boolean isNightMode() {
    int nightModeFlags =
        getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
    return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
  }

  private void applyColorsFromPalette(Map<String, Integer> palette) {
    Integer surface = palette.get("surface");
    Integer onSurface = palette.get("onSurface");
    Integer primary = palette.get("primary");
    Integer outline = palette.get("outline");

    if (surface == null) surface = MaterialColors.getColor(this, R.attr.colorSurface, Color.DKGRAY);
    if (onSurface == null)
      onSurface = MaterialColors.getColor(this, R.attr.colorOnSurface, Color.WHITE);
    if (primary == null) primary = MaterialColors.getColor(this, R.attr.colorPrimary, Color.BLUE);
    if (outline == null) outline = MaterialColors.getColor(this, R.attr.colorOutline, Color.GRAY);

    View rootMusicView = bind.getRoot();
    var gd = new GradientDrawable();
    gd.setShape(GradientDrawable.RECTANGLE);
    gd.setColor(surface);
    gd.setStroke(3, outline);
    gd.setCornerRadius(25f);
    rootMusicView.setBackground(gd);

    bind.nameartist.setTextColor(onSurface);
    bind.play.setColorFilter(primary);
    bind.previous.setColorFilter(primary);
    bind.next.setColorFilter(primary);
  }
}
