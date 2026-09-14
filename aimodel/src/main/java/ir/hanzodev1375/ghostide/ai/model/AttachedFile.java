package ir.hanzodev1375.ghostide.ai.model;

import android.net.Uri;

public class AttachedFile {

  public static final int TYPE_IMAGE = 0;
  public static final int TYPE_TEXT = 1;

  private final Uri uri;
  private final String name;
  private final int fileType;
  private String textContent;
  private String base64Data;

  public AttachedFile(Uri uri, String name, int fileType) {
    this.uri = uri;
    this.name = name;
    this.fileType = fileType;
  }

  public Uri getUri() {
    return uri;
  }

  public String getName() {
    return name;
  }

  public int getFileType() {
    return fileType;
  }

  public String getTextContent() {
    return textContent;
  }

  public void setTextContent(String textContent) {
    this.textContent = textContent;
  }

  public String getBase64Data() {
    return base64Data;
  }

  public void setBase64Data(String base64Data) {
    this.base64Data = base64Data;
  }

  public boolean isImage() {
    return fileType == TYPE_IMAGE;
  }

  public String getMimeType() {
    return "*/*";
  }
}
