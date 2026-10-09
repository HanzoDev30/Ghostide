package ir.hanzodev1375.ghostide.glide.gpl;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;

import java.io.InputStream;

/** Lets Glide accept a {@link GplIcon} and decode it as a bitmap through the normal pipeline. */
public final class GplIconLoader implements ModelLoader<GplIcon, InputStream> {

  @NonNull
  @Override
  public LoadData<InputStream> buildLoadData(
      @NonNull GplIcon model, int width, int height, @NonNull Options options) {
    return new LoadData<>(new ObjectKey(model), new GplIconFetcher(model));
  }

  @Override
  public boolean handles(@NonNull GplIcon model) {
    return true;
  }

  public static final class Factory implements ModelLoaderFactory<GplIcon, InputStream> {

    @NonNull
    @Override
    public ModelLoader<GplIcon, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
      return new GplIconLoader();
    }

    @Override
    public void teardown() {}
  }
}
