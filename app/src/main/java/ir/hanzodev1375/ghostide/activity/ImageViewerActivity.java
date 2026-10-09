package ir.hanzodev1375.ghostide.activity;

import android.app.WallpaperManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;
import androidx.viewpager2.widget.ViewPager2;
import ir.hanzodev1375.components.sheet.customitemsheet.ui.GlassCompat;
import ir.hanzodev1375.components.views.GhostToast;
import ir.hanzodev1375.ghostide.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import ir.hanzodev1375.ghostide.adapters.ImagePagerAdapter;
import ir.hanzodev1375.ghostide.databinding.ActivityImageViewerBinding;
import ir.hanzodev1375.ghostide.fragments.ImageViewerFragment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import jp.wasabeef.blurry.Blurry;
import ir.hanzodev1375.components.sheet.customitemsheet.ui.DialogCompat;

public class ImageViewerActivity extends BaseCompat implements ImageViewerFragment.Host {

  public static final String EXTRA_IMAGE_URIS = "extra_image_uris";
  public static final String EXTRA_CURRENT_INDEX = "extra_current_index";
  private ActivityImageViewerBinding bind;

  private ViewPager2 viewPager;
  private ImageView ivBlurBg;
  private TextView tvCounter;
  private LinearLayout topBar, bottomBar;
  private List<Uri> uriList = new ArrayList<>();
  private ImagePagerAdapter adapter;
  private final SparseArray<Integer> rotations = new SparseArray<>();
  private int iconTint = Color.WHITE;
  private ImageButton btnSettings,
      btnGallery,
      btnNext,
      btnInfo,
      btnRotate,
      btnZoom,
      btnSave,
      btnShare;

  private void setupGlassBackdrop(GlassCompat glass) {
    glass.setBackdropSource(bind.contentArea);
    glass.setEnableDynamicBackground(true);
    glass.setEnableChromaticAberration(true);
    glass.setEnableEdgeHighlight(true);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    bind = ActivityImageViewerBinding.inflate(getLayoutInflater());
    setContentView(bind.getRoot());
    enableEdgeToEdge();
    ivBlurBg = bind.ivBlurBg;
    viewPager = bind.viewPager;
    tvCounter = bind.tvCounter;
    topBar = bind.topBar;
    bottomBar = bind.bottomBar;
    setupGlassBackdrop(bind.btnSettingsg);
    setupGlassBackdrop(bind.btnGalleryg);
    setupGlassBackdrop(bind.btnNextg);
    setupGlassBackdrop(bind.btnInfog);
    setupGlassBackdrop(bind.btnRotateg);
    setupGlassBackdrop(bind.btnZoomg);
    setupGlassBackdrop(bind.btnSaveg);
    setupGlassBackdrop(bind.btnShareg);
    setupGlassBackdrop(bind.tvCounterg);
    btnSettings = bind.btnSettings;
    btnGallery = bind.btnGallery;
    btnNext = bind.btnNext;
    btnInfo = bind.btnInfo;
    btnRotate = bind.btnRotate;
    btnZoom = bind.btnZoom;
    btnSave = bind.btnSave;
    btnShare = bind.btnShare;
    Intent intent = getIntent();
    if (intent != null) {
      if (intent.hasExtra(EXTRA_IMAGE_URIS)) {
        Object extra = intent.getSerializableExtra(EXTRA_IMAGE_URIS);
        if (extra instanceof ArrayList) {
          ArrayList<String> strings = (ArrayList<String>) extra;
          if (strings != null) {
            for (String s : strings) {
              File file = new File(s);
              if (file.exists()) {
                uriList.add(Uri.fromFile(file));
              }
            }
          }
        } else if (extra instanceof String) {
          String s = (String) extra;
          File file = new File(s);
          if (file.exists()) {
            uriList.add(Uri.fromFile(file));
          }
        }
      } else if (intent.getData() != null) {
        uriList.add(intent.getData());
      }
    }

    if (uriList.isEmpty()) {
      GhostToast.makeText(this, "No image to show", GhostToast.LENGTH_SHORT).show();
      finish();
      return;
    }

    adapter = new ImagePagerAdapter(this, uriList);
    viewPager.setAdapter(adapter);
    viewPager.setOffscreenPageLimit(1);
    int startIndex = intent.getIntExtra(EXTRA_CURRENT_INDEX, 0);
    if (startIndex < 0 || startIndex >= uriList.size()) startIndex = 0;
    viewPager.setCurrentItem(startIndex, false);
    updateCounter(startIndex);
    loadDynamicColorsAndBlur(startIndex);

    viewPager.registerOnPageChangeCallback(
        new ViewPager2.OnPageChangeCallback() {
          @Override
          public void onPageSelected(int position) {
            updateCounter(position);
            applyZoomButtonTint(iconTint);
            loadDynamicColorsAndBlur(position);
          }
        });

    btnSettings.setOnClickListener(v -> showDisplayDialog());
    btnGallery.setOnClickListener(v -> showWallpaperOptionsDialog());
    btnNext.setOnClickListener(
        v -> {
          int next = viewPager.getCurrentItem() + 1;
          if (next < uriList.size()) {
            viewPager.setCurrentItem(next, true);
          } else {
            finish();
          }
        });
    btnInfo.setOnClickListener(v -> showImageInfo());
    btnRotate.setOnClickListener(
        v -> {
          ImageViewerFragment fragment = currentFragment();
          if (fragment != null) fragment.rotateClockwise();
        });
    btnZoom.setOnClickListener(
        v -> {
          ImageViewerFragment fragment = currentFragment();
          if (fragment != null) fragment.toggleZoom();
        });
    btnSave.setOnClickListener(
        v -> {
          try {
            saveCurrentImage();
          } catch (Exception err) {
            Log.e(getClass().getName(), String.valueOf(err.getMessage()));
          }
        });
    btnShare.setOnClickListener(v -> shareCurrentImage());
  }

  private ImageViewerFragment currentFragment() {
    Fragment fragment =
        getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
    return fragment instanceof ImageViewerFragment ? (ImageViewerFragment) fragment : null;
  }

  @Override
  public int getRotation(int position) {
    Integer degrees = rotations.get(position);
    return degrees == null ? 0 : degrees;
  }

  @Override
  public void onRotationChanged(int position, int degrees) {
    rotations.put(position, degrees);
  }

  @Override
  public void onZoomStateChanged(int position, boolean zoomed) {
    if (position != viewPager.getCurrentItem()) return;
    applyZoomButtonTint(zoomed ? Color.WHITE : iconTint);
  }

  private void enableEdgeToEdge() {
    WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
    getWindow().setNavigationBarColor(Color.TRANSPARENT);
    getWindow().setStatusBarColor(Color.TRANSPARENT);
    ViewCompat.setOnApplyWindowInsetsListener(
        bind.getRoot(),
        (v, insets) -> {
          Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
          topBar.setPadding(
              topBar.getPaddingLeft(),
              bars.top + dp(12),
              topBar.getPaddingRight(),
              topBar.getPaddingBottom());
          int bottomInset = Math.max(bars.bottom, ime.bottom);
          bottomBar.setPadding(
              bottomBar.getPaddingLeft(),
              bottomBar.getPaddingTop(),
              bottomBar.getPaddingRight(),
              bottomInset + dp(16));
          return insets;
        });
  }

  private int dp(int value) {
    return (int) (value * getResources().getDisplayMetrics().density);
  }

  private void updateCounter(int pos) {
    tvCounter.setText((pos + 1) + "·" + uriList.size());
  }

  private void loadDynamicColorsAndBlur(int position) {
    if (uriList.isEmpty()) return;
    Glide.with(this)
        .asBitmap()
        .load(uriList.get(position))
        .into(
            new CustomTarget<Bitmap>() {
              @Override
              public void onResourceReady(
                  @NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                ivBlurBg.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Blurry.with(ImageViewerActivity.this)
                    .radius(20)
                    .sampling(8)
                    .async()
                    .from(resource)
                    .into(ivBlurBg);
                Palette.from(resource)
                    .generate(
                        palette -> {
                          int defaultColor = Color.parseColor("#1E1E1E");
                          int vibrant = palette.getVibrantColor(defaultColor);
                          int darkVibrant = palette.getDarkVibrantColor(defaultColor);
                          int lightMuted = palette.getLightMutedColor(Color.WHITE);
                          int bgColor = vibrant != defaultColor ? vibrant : darkVibrant;
                          int tint = lightMuted;
                          if (Math.abs(Color.red(bgColor) - Color.red(tint)) < 50
                              && Math.abs(Color.green(bgColor) - Color.green(tint)) < 50
                              && Math.abs(Color.blue(bgColor) - Color.blue(tint)) < 50) {
                            tint = Color.WHITE;
                          }
                          applyIconTint(tint);
                        });
              }

              @Override
              public void onLoadCleared(@Nullable Drawable placeholder) {}
            });
  }

  private void applyIconTint(int color) {
    iconTint = color;
    btnSettings.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    btnGallery.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    btnNext.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    btnInfo.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    btnRotate.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    btnZoom.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    btnSave.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    btnShare.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      btnSettings.setImageTintList(ColorStateList.valueOf(color));
      btnGallery.setImageTintList(ColorStateList.valueOf(color));
      btnNext.setImageTintList(ColorStateList.valueOf(color));
      btnInfo.setImageTintList(ColorStateList.valueOf(color));
      btnRotate.setImageTintList(ColorStateList.valueOf(color));
      btnZoom.setImageTintList(ColorStateList.valueOf(color));
      btnSave.setImageTintList(ColorStateList.valueOf(color));
      btnShare.setImageTintList(ColorStateList.valueOf(color));
    }

    tvCounter.setTextColor(color);
  }

  private void applyZoomButtonTint(int color) {
    btnZoom.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      btnZoom.setImageTintList(ColorStateList.valueOf(color));
    }
  }

  private void showDisplayDialog() {
    if (uriList.isEmpty()) return;
    CharSequence[] options =
        new CharSequence[] {
          getString(R.string.viewer_fit),
          getString(R.string.viewer_fill),
          getString(R.string.viewer_actual)
        };
    new DialogCompat(this)
        .setTitle(R.string.viewer_settings)
        .setItems(
            options,
            (dialog, which) -> {
              ImageViewerFragment fragment = currentFragment();
              if (fragment == null) return;
              switch (which) {
                case 0:
                  fragment.fitToScreen();
                  break;
                case 1:
                  fragment.fillScreen();
                  break;
                case 2:
                  fragment.showActualSize();
                  break;
              }
            })
        .setNegativeButton(R.string.cancel, null)
        .show();
  }

  private void showImageInfo() {
    if (uriList.isEmpty()) return;
    Uri uri = uriList.get(viewPager.getCurrentItem());
    File file = resolveFile(uri);
    if (file == null) {
      new DialogCompat(this)
          .setTitle(R.string.viewer_info)
          .setMessage(uri.toString())
          .setPositiveButton(android.R.string.ok, null)
          .show();
      return;
    }
    new Thread(
            () -> {
              String name = file.getName();
              String size = Formatter.formatFileSize(this, file.length());
              int width = 0;
              int height = 0;
              try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(file.getAbsolutePath(), options);
                width = options.outWidth;
                height = options.outHeight;
              } catch (Exception ignored) {
              }
              String resolution =
                  width > 0 && height > 0 ? width + "×" + height : "-";
              String modified =
                  new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
                      .format(new Date(file.lastModified()));
              String message =
                  getString(
                      R.string.viewer_info_message,
                      name,
                      size,
                      resolution,
                      file.getAbsolutePath(),
                      modified);
              runOnUiThread(
                  () ->
                      new DialogCompat(this)
                          .setTitle(R.string.viewer_info)
                          .setMessage(message)
                          .setPositiveButton(android.R.string.ok, null)
                          .show());
            })
        .start();
  }

  private File resolveFile(Uri uri) {
    if (uri == null) return null;
    if ("file".equalsIgnoreCase(uri.getScheme())) {
      String path = uri.getPath();
      if (path != null) {
        File file = new File(path);
        if (file.exists()) return file;
      }
    }
    return null;
  }

  private Bitmap rotateIfNeeded(Bitmap bitmap, int position) {
    int degrees = getRotation(position);
    if (degrees == 0 || bitmap == null) return bitmap;
    Matrix matrix = new Matrix();
    matrix.postRotate(degrees);
    Bitmap rotated =
        Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    if (rotated != bitmap) bitmap.recycle();
    return rotated;
  }

  private void saveCurrentImage() {
    if (uriList.isEmpty()) return;
    Uri imageUri = uriList.get(viewPager.getCurrentItem());
    int position = viewPager.getCurrentItem();
    new Thread(
            () -> {
              try {
                Bitmap bitmap = Glide.with(this).asBitmap().load(imageUri).submit().get();
                if (bitmap == null) return;
                bitmap = rotateIfNeeded(bitmap, position);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                  ContentValues values = new ContentValues();
                  values.put(
                      MediaStore.Images.Media.DISPLAY_NAME,
                      "img_" + System.currentTimeMillis() + ".jpg");
                  values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                  values.put(
                      MediaStore.Images.Media.RELATIVE_PATH,
                      Environment.DIRECTORY_PICTURES + "/ImageViewer");
                  Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                  Uri saveUri = getContentResolver().insert(collection, values);
                  if (saveUri != null) {
                    try (OutputStream oss = getContentResolver().openOutputStream(saveUri)) {
                      bitmap.compress(Bitmap.CompressFormat.JPEG, 90, oss);
                    }
                    runOnUiThread(
                        () ->
                            GhostToast.makeText(
                                    this, "Saved to Gallery", GhostToast.LENGTH_SHORT)
                                .show());
                  } else {
                    runOnUiThread(
                        () ->
                            GhostToast.makeText(this, "Save failed", GhostToast.LENGTH_SHORT)
                                .show());
                  }
                } else {
                  File dir =
                      new File(
                          Environment.getExternalStoragePublicDirectory(
                              Environment.DIRECTORY_PICTURES),
                          "ImageViewer");
                  if (!dir.exists() && !dir.mkdirs()) {
                    runOnUiThread(
                        () ->
                            GhostToast.makeText(this, "Save failed", GhostToast.LENGTH_SHORT)
                                .show());
                    return;
                  }
                  File outFile = new File(dir, "img_" + System.currentTimeMillis() + ".jpg");
                  try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                  }
                  runOnUiThread(
                      () ->
                          GhostToast.makeText(this, "Saved to Gallery", GhostToast.LENGTH_SHORT)
                              .show());
                }
                bitmap.recycle();
              } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(
                    () ->
                        GhostToast.makeText(this, "Error saving", GhostToast.LENGTH_SHORT)
                            .show());
              }
            })
        .start();
  }

  private void shareCurrentImage() {
    if (uriList.isEmpty()) return;
    Uri uri = uriList.get(viewPager.getCurrentItem());
    Uri shareUri = uri;
    File file = resolveFile(uri);
    if (file != null) {
      try {
        shareUri =
            FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
      } catch (Exception e) {
        e.printStackTrace();
        GhostToast.makeText(this, "Share failed", GhostToast.LENGTH_SHORT).show();
        return;
      }
    }
    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.setType("image/*");
    shareIntent.putExtra(Intent.EXTRA_STREAM, shareUri);
    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    startActivity(
        Intent.createChooser(shareIntent, getString(R.string.viewer_share_title)));
  }

  private void showWallpaperOptionsDialog() {
    String[] options = {
      getString(R.string.wallpaper_option_home),
      getString(R.string.wallpaper_option_lock),
      getString(R.string.wallpaper_option_both)
    };

    new DialogCompat(this)
        .setTitle(R.string.wallpaper_dialog_title)
        .setItems(
            options,
            (dialog, which) -> {
              switch (which) {
                case 0:
                  applyWallpaper(WallpaperManager.FLAG_SYSTEM);
                  break;
                case 1:
                  applyWallpaper(WallpaperManager.FLAG_LOCK);
                  break;
                case 2:
                  applyWallpaper(WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK);
                  break;
              }
            })
        .setNegativeButton(R.string.cancel, null)
        .show();
  }

  private void applyWallpaper(int flags) {
    if (uriList.isEmpty()) return;
    Uri imageUri = uriList.get(viewPager.getCurrentItem());
    int position = viewPager.getCurrentItem();

    new Thread(
            () -> {
              try {
                Bitmap bitmap = Glide.with(this).asBitmap().load(imageUri).submit().get();
                if (bitmap == null) return;
                bitmap = rotateIfNeeded(bitmap, position);

                WallpaperManager wm = WallpaperManager.getInstance(this);
                wm.setBitmap(bitmap, null, true, flags);
                bitmap.recycle();

                runOnUiThread(
                    () ->
                        GhostToast.makeText(this, R.string.wallpaper_set_success, GhostToast.LENGTH_SHORT)
                            .show());
              } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(
                    () ->
                        GhostToast.makeText(this, R.string.wallpaper_set_failed, GhostToast.LENGTH_SHORT)
                            .show());
              }
            })
        .start();
  }
}
