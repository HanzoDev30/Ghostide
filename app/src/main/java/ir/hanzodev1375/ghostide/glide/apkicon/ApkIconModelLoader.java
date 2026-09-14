package ir.hanzodev1375.ghostide.glide.apkicon;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

public class ApkIconModelLoader implements ModelLoader<String, Drawable> {
  private final Context context;

  public ApkIconModelLoader(Context context) {
    this.context = context;
  }

  @Override
  public LoadData<Drawable> buildLoadData(
      @NonNull String model, int width, int height, @NonNull Options options) {
    return new LoadData<>(new ObjectKey(model), new ApkIconFetcher(model, context));
  }

  @Override
  public boolean handles(@NonNull String model) {
    return model.endsWith(".apk");
  }

  public static class Factory implements ModelLoaderFactory<String, Drawable> {
    private final Context context;

    public Factory(Context context) {
      this.context = context;
    }

    @NonNull
    @Override
    public ModelLoader<String, Drawable> build(@NonNull MultiModelLoaderFactory multiFactory) {
      return new ApkIconModelLoader(context);
    }

    @Override
    public void teardown() {}
  }
}
