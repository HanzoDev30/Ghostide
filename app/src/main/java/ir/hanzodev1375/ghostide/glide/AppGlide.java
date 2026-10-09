package ir.hanzodev1375.ghostide.glide;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.caverock.androidsvg.SVG;

import java.io.InputStream;

import ir.hanzodev1375.ghostide.glide.gpl.GplIcon;
import ir.hanzodev1375.ghostide.glide.gpl.GplIconLoader;
import ir.hanzodev1375.ghostide.glide.svg.SvgDecoder;

@GlideModule
public class AppGlide extends AppGlideModule {

  @Override
  public void registerComponents(
      @NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
    registry.append(InputStream.class, SVG.class, new SvgDecoder());
    registry.append(GplIcon.class, InputStream.class, new GplIconLoader.Factory());
  }
}
