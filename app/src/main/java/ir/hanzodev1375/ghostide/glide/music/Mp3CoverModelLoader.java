package ir.hanzodev1375.ghostide.glide.music;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoader.LoadData;
import com.bumptech.glide.signature.ObjectKey;

public class Mp3CoverModelLoader implements ModelLoader<String, Bitmap> {

  @Override
  public LoadData<Bitmap> buildLoadData(
      @NonNull String model, int width, int height, @NonNull Options options) {
    var key = new ObjectKey(model);
    return new LoadData<>(key, new Mp3CoverLoader(model));
  }

  @Override
  public boolean handles(@NonNull String model) {
    return true;
  }
}
