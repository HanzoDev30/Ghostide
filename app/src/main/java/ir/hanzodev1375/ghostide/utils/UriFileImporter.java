package ir.hanzodev1375.ghostide.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import com.blankj.utilcode.util.FileIOUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public final class UriFileImporter {

  private UriFileImporter() {}

  public static String getRealPathFromUri(Context context, Uri uri) {
    if (uri == null) return null;
    if ("file".equals(uri.getScheme())) {
      return uri.getPath();
    }
    if ("content".equals(uri.getScheme())) {
      return copyFileFromContentUri(context, uri);
    }
    return null;
  }

  public static String copyFileFromContentUri(Context context, Uri uri) {
    String fileName = "temp_file_" + System.currentTimeMillis();
    try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
      if (cursor != null && cursor.moveToFirst()) {
        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        if (nameIndex != -1) fileName = cursor.getString(nameIndex);
      }
    } catch (Exception ignored) {
    }
    File tempDir = new File(context.getCacheDir(), "GhostIDE/temp");
    if (!tempDir.exists()) tempDir.mkdirs();
    File destFile = new File(tempDir, fileName);
    try (InputStream is = context.getContentResolver().openInputStream(uri);
        FileOutputStream os = new FileOutputStream(destFile)) {
      byte[] buffer = new byte[8192];
      int len;
      while ((len = is.read(buffer)) != -1) os.write(buffer, 0, len);
      return destFile.getAbsolutePath();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static String saveTextToCache(Context context, String text) {
    File dir = new File(context.getCacheDir(), "GhostIDE/temp");
    if (!dir.exists()) dir.mkdirs();
    String fileName = "shared_text_" + System.currentTimeMillis() + ".txt";
    File file = new File(dir, fileName);
    FileIOUtils.writeFileFromString(file.getAbsolutePath(), text);
    return file.getAbsolutePath();
  }
}