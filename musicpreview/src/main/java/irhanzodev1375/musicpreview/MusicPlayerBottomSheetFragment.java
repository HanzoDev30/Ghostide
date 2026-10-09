package irhanzodev1375.musicpreview;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.animation.PathInterpolator;
import android.widget.SeekBar;
import java.io.File;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.transition.MaterialSharedAxis;
import irhanzodev1375.musicpreview.databinding.FragmentMusicPlayerBottomSheetBinding;
import ir.theme.M3Theme;
import java.util.Locale;
import java.util.Map;

public class MusicPlayerBottomSheetFragment extends BottomSheetDialogFragment {

  private static final String ARG_MUSIC_PATH = "music_path";
  private static final String ARG_SONG_NAME = "song_name";
  private static final String ARG_ARTIST_NAME = "artist_name";
  private static final int UPDATE_INTERVAL_MS = 100;
  private static final PathInterpolator M3_STANDARD = new PathInterpolator(0.2f, 0f, 0f, 1f);

  private FragmentMusicPlayerBottomSheetBinding binding;
  private MusicControl musicControl;
  private final Handler progressHandler = new Handler(Looper.getMainLooper());
  private boolean isUserSeeking = false;
  private SquigglyProgress squigglyProgress;
  private OnDismissListener onDismissCallback;
  private boolean introPlayed;

  public interface MusicControl {
    void play();
    void pause();
    boolean isPlaying();
    void seekTo(int position);
    int getCurrentPosition();
    int getDuration();
    Bitmap getAlbumArt();
    String getArtistName();
    String getMusicPath();
    void next();
    void previous();
    boolean hasNext();
    boolean hasPrevious();
    void setOnTrackChangedListener(OnTrackChangedListener listener);
  }

  public interface OnTrackChangedListener {
    void onTrackChanged(String path);
  }

  public interface OnDismissListener {
    void onSheetDismissed();
  }

  public static MusicPlayerBottomSheetFragment newInstance(
      String musicPath, String songName, String artistName) {
    MusicPlayerBottomSheetFragment fragment = new MusicPlayerBottomSheetFragment();
    Bundle args = new Bundle();
    args.putString(ARG_MUSIC_PATH, musicPath);
    args.putString(ARG_SONG_NAME, songName);
    args.putString(ARG_ARTIST_NAME, artistName);
    fragment.setArguments(args);
    return fragment;
  }

  public void setMusicControl(MusicControl control) {
    this.musicControl = control;
  }

  public void setOnDismissListener(OnDismissListener listener) {
    this.onDismissCallback = listener;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NORMAL, R.style.MusicPlayerBottomSheetTheme);
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentMusicPlayerBottomSheetBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    setupSheetStyle();
    setupSquigglyProgress();
    updateUI();
    setupListeners();
    if (musicControl != null) {
      musicControl.setOnTrackChangedListener(this::onTrackChanged);
    }
    startProgressUpdates();
    M3Theme.apply(binding.getRoot());
  }

  private void onTrackChanged(String path) {
    if (binding == null || musicControl == null) return;
    playTrackChangeAnimation(path != null ? new File(path).getName() : "");
  }

  private void playTrackChangeAnimation(String songName) {
    View content = binding.musicSheetContent;
    float density = getResources().getDisplayMetrics().density;
    content.animate().cancel();
    content
        .animate()
        .alpha(0f)
        .scaleX(0.94f)
        .scaleY(0.94f)
        .translationY(-6f * density)
        .setDuration(220)
        .setInterpolator(M3_STANDARD)
        .withEndAction(
            () -> {
              updateTrackContent(songName);
              content.setAlpha(0f);
              content.setScaleX(0.96f);
              content.setScaleY(0.96f);
              content.setTranslationY(8f * density);
              content
                  .animate()
                  .alpha(1f)
                  .scaleX(1f)
                  .scaleY(1f)
                  .translationY(0f)
                  .setDuration(420)
                  .setInterpolator(M3_STANDARD)
                  .start();
            })
        .start();
  }

  private void updateTrackContent(String songName) {
    String path = musicControl.getMusicPath();
    binding.musicSheetTitle.setText(
        songName == null || songName.isEmpty() ? path : songName);
    String artist = musicControl.getArtistName();
    binding.musicSheetArtist.setText(
        artist != null ? artist : getString(R.string.music_preview_no_artist));

    Bitmap cover = musicControl.getAlbumArt();
    if (cover != null) {
      binding.musicSheetCover.setImageBitmap(cover);
      applyPaletteFromBitmap(cover);
    } else {
      binding.musicSheetCover.setImageDrawable(new ColorDrawable(Color.CYAN));
    }

    binding.musicSheetSeekBar.setProgress(0);
    binding.musicSheetPosition.setText(formatTime(0));
    binding.musicSheetSeekBar.setMax(musicControl.getDuration());
    binding.musicSheetDuration.setText(formatTime(musicControl.getDuration()));
    updatePlayPauseState();
  }

  private void setupSheetStyle() {
    Dialog dialog = getDialog();
    if (dialog == null) return;
    BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
    View bottomSheet = bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
    if (bottomSheet != null) {
      BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
      behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
      behavior.setSkipCollapsed(true);
      behavior.setHideable(true);

      bottomSheet.setOutlineProvider(
          new ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline outline) {
              float cornerRadius =
                  getResources().getDimension(R.dimen.music_bottom_sheet_corner_radius);
              outline.setRoundRect(
                  0, 0, v.getWidth(), v.getHeight() + (int) cornerRadius, cornerRadius);
            }
          });
      bottomSheet.setClipToOutline(true);

      behavior.addBottomSheetCallback(
          new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
              if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                if (binding != null) {
                  binding.getRoot().setAlpha(1f);
                  binding.getRoot().setTranslationY(0f);
                }
                playIntro();
              } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                if (binding != null) {
                  binding.getRoot().setAlpha(0f);
                }
                if (musicControl != null && musicControl.isPlaying()) {
                  musicControl.pause();
                  if (squigglyProgress != null) {
                    squigglyProgress.setAnimate(false);
                  }
                }
              }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
              if (binding == null) return;
              float clamped = Math.max(0f, Math.min(1f, slideOffset));
              binding.getRoot().setAlpha(clamped);
              binding.getRoot().setTranslationY((1f - clamped) * 100f);
            }
          });
    }
  }

  private void playIntro() {
    if (introPlayed || binding == null) return;
    introPlayed = true;

    View[] cover = {binding.musicSheetCoverCard, binding.musicSheetTitle, binding.musicSheetArtist};
    View[] progress = {binding.musicSheetSeekBar, binding.musicSheetTimeRow};
    View[] controls = {binding.musicSheetControls};

    hide(cover);
    hide(progress);
    hide(controls);

    TransitionSet reveal = new TransitionSet();
    reveal.setOrdering(TransitionSet.ORDERING_SEQUENTIAL);
    reveal.addTransition(sharedAxis(cover));
    reveal.addTransition(sharedAxis(progress));
    reveal.addTransition(sharedAxis(controls));

    TransitionManager.beginDelayedTransition(binding.getRoot(), reveal);
    show(cover);
    show(progress);
    show(controls);
  }

  private MaterialSharedAxis sharedAxis(View[] targets) {
    MaterialSharedAxis axis = new MaterialSharedAxis(MaterialSharedAxis.Y, true);
    axis.setDuration(220);
    for (View target : targets) {
      axis.addTarget(target);
    }
    return axis;
  }

  private void hide(View[] views) {
    for (View view : views) {
      view.setVisibility(View.INVISIBLE);
    }
  }

  private void show(View[] views) {
    for (View view : views) {
      view.setVisibility(View.VISIBLE);
    }
  }

  private void setupSquigglyProgress() {
    squigglyProgress = new SquigglyProgress();
    squigglyProgress.waveLength = getResources().getDimension(R.dimen.music_squiggly_wavelength);
    squigglyProgress.lineAmplitude = getResources().getDimension(R.dimen.music_squiggly_amplitude);
    squigglyProgress.phaseSpeed = getResources().getDimension(R.dimen.music_squiggly_phase);
    squigglyProgress.setStrokeWidth(
        getResources().getDimension(R.dimen.music_squiggly_stroke_width));
    squigglyProgress.transitionEnabled = true;
    squigglyProgress.setAnimate(false);

    int primaryColor =
        (M3Theme.primary() != null
            ? M3Theme.primary()
            : MaterialColors.getColor(requireView(), R.attr.colorPrimary, Color.BLUE));
    squigglyProgress.setTint(primaryColor);

    binding.musicSheetSeekBar.setProgressDrawable(squigglyProgress);
  }

  private void updateUI() {
    if (musicControl == null) return;

    String artistName = musicControl.getArtistName();
    binding.musicSheetArtist.setText(
        artistName != null ? artistName : getString(R.string.music_preview_no_artist));

    String songName = getArguments() != null ? getArguments().getString(ARG_SONG_NAME, "") : "";
    binding.musicSheetTitle.setText(songName.isEmpty() ? musicControl.getMusicPath() : songName);

    Bitmap cover = musicControl.getAlbumArt();
    if (cover != null) {
      binding.musicSheetCover.setImageBitmap(cover);
      applyPaletteFromBitmap(cover);
    } else {
      binding.musicSheetCover.setImageDrawable(new ColorDrawable(Color.CYAN));
    }

    if (musicControl.isPlaying()) {
      binding.musicSheetPlayPause.startAnimation();
      if (squigglyProgress != null) squigglyProgress.setAnimate(true);
    }
  }

  private void setupListeners() {
    binding.musicSheetPlayPause.setOnClickListener(
        v -> {
          if (musicControl == null) return;
          if (musicControl.isPlaying()) {
            musicControl.pause();
            if (squigglyProgress != null) squigglyProgress.setAnimate(false);
          } else {
            musicControl.play();
            if (squigglyProgress != null) squigglyProgress.setAnimate(true);
          }
        });

    binding.musicSheetPrev.setOnClickListener(
        v -> {
          if (musicControl == null) return;
          if (musicControl.hasPrevious()) {
            musicControl.previous();
          } else {
            int target = musicControl.getCurrentPosition() - 10000;
            musicControl.seekTo(Math.max(target, 0));
          }
        });

    binding.musicSheetNext.setOnClickListener(
        v -> {
          if (musicControl == null) return;
          if (musicControl.hasNext()) {
            musicControl.next();
          } else {
            int target = musicControl.getCurrentPosition() + 10000;
            musicControl.seekTo(Math.min(target, musicControl.getDuration()));
          }
        });

    binding.musicSheetSeekBar.setOnSeekBarChangeListener(
        new SeekBar.OnSeekBarChangeListener() {
          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
              binding.musicSheetPosition.setText(formatTime(progress));
              if (squigglyProgress != null) {
                squigglyProgress.setAnimate(false);
              }
            }
          }

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {
            isUserSeeking = true;
            if (squigglyProgress != null) {
              squigglyProgress.setAnimate(false);
            }
          }

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {
            if (musicControl != null) {
              musicControl.seekTo(seekBar.getProgress());
            }
            isUserSeeking = false;
            if (squigglyProgress != null && musicControl != null) {
              squigglyProgress.setAnimate(musicControl.isPlaying());
            }
          }
        });
  }

  private final Runnable progressRunnable =
      new Runnable() {
        @Override
        public void run() {
          updateSeekBarProgress();
          if (musicControl != null && musicControl.isPlaying()) {
            progressHandler.postDelayed(this, UPDATE_INTERVAL_MS);
          }
        }
      };

  private void startProgressUpdates() {
    progressHandler.post(progressRunnable);
  }

  private void updateSeekBarProgress() {
    if (musicControl == null || binding == null) return;

    int duration = musicControl.getDuration();
    int position = musicControl.getCurrentPosition();

    binding.musicSheetSeekBar.setMax(duration);
    if (!isUserSeeking) {
      binding.musicSheetSeekBar.setProgress(position);
    }
    binding.musicSheetPosition.setText(formatTime(position));
    binding.musicSheetDuration.setText(formatTime(duration));

    updatePlayPauseState();
  }

  private void updatePlayPauseState() {
    if (binding == null || musicControl == null) return;
    boolean playing = musicControl.isPlaying();
    if (squigglyProgress != null) {
      squigglyProgress.setAnimate(playing && !isUserSeeking);
    }
  }

  private String formatTime(int millis) {
    int totalSeconds = millis / 1000;
    int minutes = totalSeconds / 60;
    int seconds = totalSeconds % 60;
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
  }

  @Override
  public void onStart() {
    super.onStart();
    progressHandler.post(progressRunnable);
  }

  @Override
  public void onStop() {
    super.onStop();
    progressHandler.removeCallbacks(progressRunnable);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    progressHandler.removeCallbacks(progressRunnable);
    binding = null;
  }

  @Override
  public void onDismiss(@NonNull DialogInterface dialog) {
    super.onDismiss(dialog);
    if (squigglyProgress != null) {
      squigglyProgress.setAnimate(false);
    }
    if (onDismissCallback != null) {
      onDismissCallback.onSheetDismissed();
    }
  }

  private void applyPaletteFromBitmap(Bitmap bitmap) {
    ColorPaletteUtils.generateFromBitmap(
        bitmap,
        (lightColors, darkColors) -> {
          boolean isDark = isNightMode();
          Map<String, Integer> palette = isDark ? darkColors : lightColors;
          if (palette != null && !palette.isEmpty() && binding != null) {
            requireView().post(() -> applyColorsFromPalette(palette));
          }
        });
  }

  private boolean isNightMode() {
    if (getContext() == null) return false;
    int nightModeFlags =
        getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
    return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
  }

  private void applyColorsFromPalette(Map<String, Integer> palette) {
    Integer surface = palette.get("surface");
    Integer onSurface = palette.get("onSurface");
    Integer primary = palette.get("primary");
    Integer onPrimary = palette.get("onPrimary");
    Integer tertiary = palette.get("tertiary");
    Integer onTertiary = palette.get("onTertiary");
    Integer outline = palette.get("outline");
    Integer surfaceContainer = palette.get("surfaceContainer");
    Integer onSurfaceContainer = palette.get("onSurfaceContainer");

    if (surface == null) surface = Color.parseColor(isNightMode() ? "#121212" : "#FFFBFE");
    if (onSurface == null) onSurface = isNightMode() ? Color.WHITE : Color.BLACK;
    if (primary == null) primary = Color.parseColor("#BB86FC");
    if (onPrimary == null) onPrimary = Color.TRANSPARENT;
    if (tertiary == null) tertiary = Color.YELLOW;
    if (onTertiary == null) onTertiary = Color.parseColor("#293810");
    if (outline == null) outline = Color.parseColor("#333333");
    if (surfaceContainer == null) surfaceContainer = surface;
    if (onSurfaceContainer == null) onSurfaceContainer = onSurface;

    binding.getRoot().setBackgroundColor(surfaceContainer);

    binding.musicSheetTitle.setTextColor(onSurface);
    binding.musicSheetArtist.setTextColor(onSurfaceContainer);
    binding.musicSheetPosition.setTextColor(onSurfaceContainer);
    binding.musicSheetDuration.setTextColor(onSurfaceContainer);

    binding.musicSheetSeekBar.setThumbTintList(
        android.content.res.ColorStateList.valueOf(primary));
    binding.musicSheetSeekBar.setProgressTintList(
        android.content.res.ColorStateList.valueOf(primary));

    if (squigglyProgress != null) {
      squigglyProgress.setTint(primary);
    }

    binding.musicSheetPrev.setColorFilter(tertiary);
    binding.musicSheetNext.setColorFilter(tertiary);

    binding.musicSheetPlayPause.setShapeColor(onPrimary != null ? onPrimary : Color.TRANSPARENT);
    binding.musicSheetPlayPause.setIconColor(primary);
  }
}
