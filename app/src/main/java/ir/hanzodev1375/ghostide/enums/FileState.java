package ir.hanzodev1375.ghostide.enums;

import android.graphics.Color;

public enum FileState {
  CREATOR(0, Color.parseColor("#ff7010")),
  RENAME(1, Color.parseColor("#710170")),
  SERACH(3, Color.CYAN),
  REOMVED(2),
  NONE(3);
  int value, color;

  FileState(int value) {
    this.value = value;
  }

  FileState(int value, int color) {
    this.value = value;
    this.color = color;
  }

  public int getValue() {
    return this.value;
  }

  public int getColor() {
    return this.color;
  }
}
