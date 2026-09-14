package ir.hanzodev1375.ghostide.utils;

import android.graphics.Bitmap;

public class BlurHelper {

  public static Bitmap blur(Bitmap source, float radius) {
    if (source == null) {
      return null;
    }

    int scaledWidth = Math.max(1, source.getWidth() / 4);
    int scaledHeight = Math.max(1, source.getHeight() / 4);

    Bitmap small = Bitmap.createScaledBitmap(
      source,
      scaledWidth,
      scaledHeight,
      true
    );

    Bitmap blurred = boxBlur(small, Math.max(1, (int) radius));

    return Bitmap.createScaledBitmap(
      blurred,
      source.getWidth(),
      source.getHeight(),
      true
    );
  }

  private static Bitmap boxBlur(Bitmap bitmap, int radius) {
    int w = bitmap.getWidth();
    int h = bitmap.getHeight();

    int[] pixels = new int[w * h];
    int[] output = new int[w * h];

    bitmap.getPixels(pixels, 0, w, 0, 0, w, h);

    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {

        long r = 0;
        long g = 0;
        long b = 0;
        long a = 0;
        int count = 0;

        for (int ky = -radius; ky <= radius; ky++) {
          int py = Math.max(0, Math.min(h - 1, y + ky));

          for (int kx = -radius; kx <= radius; kx++) {
            int px = Math.max(0, Math.min(w - 1, x + kx));

            int color = pixels[py * w + px];

            a += (color >>> 24);
            r += (color >> 16) & 0xff;
            g += (color >> 8) & 0xff;
            b += color & 0xff;

            count++;
          }
        }

        output[y * w + x] =
          ((int) (a / count) << 24) |
          ((int) (r / count) << 16) |
          ((int) (g / count) << 8) |
          (int) (b / count);
      }
    }

    Bitmap result = Bitmap.createBitmap(
      w,
      h,
      Bitmap.Config.ARGB_8888
    );

    result.setPixels(output, 0, w, 0, 0, w, h);

    return result;
  }
}