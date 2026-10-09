package ir.hanzodev1375.ghostide.glide.gpl;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import ir.hanzodev1375.ghostide.plugin.gpl.GplManifest;
import ir.hanzodev1375.ghostide.plugin.gpl.GplManifestReader;

/** Streams the icon bytes stored in a {@code .gpl}'s {@code assets/} folder. */
public final class GplIconFetcher implements DataFetcher<InputStream> {

  private final GplIcon model;
  private InputStream stream;

  GplIconFetcher(GplIcon model) {
    this.model = model;
  }

  @NonNull
  @Override
  public Class<InputStream> getDataClass() {
    return InputStream.class;
  }

  @NonNull
  @Override
  public DataSource getDataSource() {
    return DataSource.LOCAL;
  }

  @Override
  public void loadData(
      @NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
    try {
      GplManifest manifest = GplManifestReader.read(model.file());
      byte[] bytes =
          manifest == null ? null : GplManifestReader.readIconBytes(model.file(), manifest);
      if (bytes == null) {
        throw new FileNotFoundException("No icon declared in " + model.file());
      }
      stream = new ByteArrayInputStream(bytes);
      callback.onDataReady(stream);
    } catch (IOException e) {
      callback.onLoadFailed(e);
    }
  }

  @Override
  public void cleanup() {
    if (stream != null) {
      try {
        stream.close();
      } catch (IOException ignored) {
      }
      stream = null;
    }
  }

  @Override
  public void cancel() {}
}
