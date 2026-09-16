package ir.hanzodev1375.ghostide.codeeditors.style;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import androidx.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.rosemoe.sora.widget.style.SelectionHandleStyle;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class BitmapHandleStyle implements SelectionHandleStyle {

  private static final String CURSOR_BASE = "/storage/emulated/0/ghostide/cursor";
  private static final float BITMAP_HEIGHT_RATIO = 1.8f;

  private final Paint paint;
  private Bitmap leftBitmap;
  private Bitmap rightBitmap;
  private int alpha = 255;
  private float scaleFactor = 1.0f;

  public BitmapHandleStyle(@NonNull Context context, String cursorName) {
    paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    loadFromJson(cursorName);
  }

  private void loadFromJson(String cursorName) {
    if (cursorName == null || cursorName.trim().isEmpty()) return;
    try {
      File jsonFile = new File(CURSOR_BASE, "cursor.json");
      if (!jsonFile.exists()) return;
      RandomAccessFile raf = new RandomAccessFile(jsonFile, "r");
      byte[] bytes = new byte[(int) raf.length()];
      raf.readFully(bytes);
      raf.close();
      String json = new String(bytes, StandardCharsets.UTF_8);
      List<CursorEntry> entries =
          new Gson()
              .fromJson(json, new TypeToken<List<CursorEntry>>() {}.getType());
      if (entries == null) return;
      for (CursorEntry entry : entries) {
        String name =
            entry.namecursorstart != null && !entry.namecursorstart.isEmpty()
                ? entry.namecursorstart
                : entry.namecursorend;
        if (cursorName.equals(name)) {
          leftBitmap = decodeFile(entry.cursorstart);
          rightBitmap = decodeFile(entry.cursorend);
          if (leftBitmap == null) leftBitmap = rightBitmap;
          if (rightBitmap == null) rightBitmap = leftBitmap;
          return;
        }
      }
    } catch (Exception ignored) {
    }
  }

  private static Bitmap decodeFile(String path) {
    if (path == null || path.isEmpty()) return null;
    File f = new File(path);
    if (!f.exists()) return null;
    Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
    if (bmp != null && !bmp.isRecycled()) return bmp;
    return null;
  }

  public boolean isAvailable() {
    return (leftBitmap != null && !leftBitmap.isRecycled())
        || (rightBitmap != null && !rightBitmap.isRecycled());
  }

  @Override
  public void draw(
      @NonNull Canvas canvas,
      int handleType,
      float x,
      float y,
      int rowHeight,
      int color,
      @NonNull HandleDescriptor descriptor) {
    Bitmap bmp;
    if (handleType == HANDLE_TYPE_LEFT || handleType == HANDLE_TYPE_RIGHT) {
      bmp = rightBitmap;
    } else {
      bmp = leftBitmap != null ? leftBitmap : rightBitmap;
    }
    if (bmp == null || bmp.isRecycled()) return;

    int bw = bmp.getWidth();
    int bh = bmp.getHeight();
    if (bw <= 0 || bh <= 0) return;

    float aspect = (float) bw / bh;
    int drawH = (int) (rowHeight * BITMAP_HEIGHT_RATIO * scaleFactor);
    int drawW = (int) (drawH * aspect);
    if (drawW <= 0 || drawH <= 0) return;

    int left = (int) (x - drawW / 2f);
    int top = (int) y;
    int right = left + drawW;
    int bottom = top + drawH;

    paint.setAlpha(alpha);
    canvas.drawBitmap(bmp, null, new RectF(left, top, right, bottom), paint);

    int alignment;
    if (handleType == HANDLE_TYPE_LEFT) {
      alignment = ALIGN_RIGHT;
    } else if (handleType == HANDLE_TYPE_RIGHT) {
      alignment = ALIGN_LEFT;
    } else {
      alignment = ALIGN_CENTER;
    }
    descriptor.set(left, top, right, bottom, alignment);
  }

  @Override
  public void setAlpha(int alpha) {
    this.alpha = alpha;
  }

  @Override
  public void setScale(float factor) {
    this.scaleFactor = factor;
  }

  private static class CursorEntry {
    String cursorstart;
    String cursorend;
    String namecursorstart;
    String namecursorend;
  }
}
