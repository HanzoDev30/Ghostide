package ir.hanzodev1375.ghostide.glide.music;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

public class Mp3CoverLoaderFactory implements ModelLoaderFactory<String, Bitmap> {
  @NonNull
  @Override
  public ModelLoader<String, Bitmap> build(@NonNull MultiModelLoaderFactory multiFactory) {
    return new Mp3CoverModelLoader();
  }

  @Override
  public void teardown() {
  }
}
