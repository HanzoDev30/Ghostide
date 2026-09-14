package ir.hanzodev1375.ghostide.models;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ir.hanzodev1375.ghostide.enums.FileState;

public class FileManagerModel {
  private String path;
  private String name;
  private FileState state;
  private long lastModified;

  public FileManagerModel(String path, String name, FileState state, long lastModified) {
    this.path = path;
    this.name = name;
    this.state = state;
    this.lastModified = lastModified;
  }

  public String getPath() {
    return path;
  }

  public String getName() {
    return name;
  }

  public FileState getState() {
    return state;
  }

  public void setState(FileState state) {
    this.state = state;
  }

  public long getLastModified() {
    return lastModified;
  }

  public String getLastModifiedFormatted() {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    return sdf.format(new Date(lastModified));
  }

  public boolean isDirectory() {
    File file = new File(path);
    return file.exists() && file.isDirectory();
  }
}
