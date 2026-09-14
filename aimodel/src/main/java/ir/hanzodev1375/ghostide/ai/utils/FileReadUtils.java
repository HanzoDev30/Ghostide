// FileReadUtils.java (با استفاده از Glide برای خواندن ایمن تصاویر)
package ir.hanzodev1375.ghostide.ai.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Base64;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.Target;

import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

public class FileReadUtils {

  private FileReadUtils() {}

  public static String readTextFile(Context context, Uri uri) throws Exception {
    ContentResolver cr = context.getContentResolver();
    try (InputStream is = cr.openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line).append('\n');
      }
      return sb.toString();
    }
  }

  public static String readImageAsBase64(Context context, Uri uri) throws Exception {
    Bitmap bitmap =
        Glide.with(context)
            .asBitmap()
            .load(uri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .override(1024, 1024)
            .centerCrop()
            .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
            .get();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, baos);
    byte[] bytes = baos.toByteArray();
    bitmap.recycle();
    return Base64.encodeToString(bytes, Base64.NO_WRAP);
  }

  public static String getMimeType(Context context, Uri uri) {
    ContentResolver cr = context.getContentResolver();
    String mime = cr.getType(uri);
    return mime != null ? mime : "application/octet-stream";
  }

  public static boolean isImageMime(String mimeType) {
    return mimeType != null && mimeType.startsWith("image/");
  }
}
