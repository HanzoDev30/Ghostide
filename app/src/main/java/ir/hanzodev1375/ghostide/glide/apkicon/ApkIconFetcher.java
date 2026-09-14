package ir.hanzodev1375.ghostide.glide.apkicon;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;

public class ApkIconFetcher implements DataFetcher<Drawable> {
  private final String model;
  private final Context context;

  public ApkIconFetcher(String model, Context context) {
    this.model = model;
    this.context = context;
  }

  @Override
  public void loadData(
      @NonNull Priority priority, @NonNull DataCallback<? super Drawable> callback) {
    try {
      String apkPath = model;
      PackageManager packageManager = context.getPackageManager();
      PackageInfo packageInfo = packageManager.getPackageArchiveInfo(apkPath, 0);
      if (packageInfo != null && packageInfo.applicationInfo != null) {
        packageInfo.applicationInfo.sourceDir = apkPath;
        packageInfo.applicationInfo.publicSourceDir = apkPath;
        Drawable icon = packageInfo.applicationInfo.loadIcon(packageManager);
        callback.onDataReady(icon);
      } else {
        callback.onLoadFailed(new Exception("Failed to load APK icon"));
      }
    } catch (Exception e) {
      callback.onLoadFailed(e);
    }
  }

  @Override
  public void cleanup() {}

  @Override
  public void cancel() {}

  @NonNull
  @Override
  public Class<Drawable> getDataClass() {
    return Drawable.class;
  }

  @NonNull
  @Override
  public DataSource getDataSource() {
    return DataSource.LOCAL;
  }
}
