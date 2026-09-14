package ir.hanzodev1375.ghostide.models;

public class SettingItem {
  private String title;
  private String description;
  private boolean isChecked;
  private int iconRes;
  private OnSwitchChangeListener listener;

  public SettingItem(String title, boolean isChecked, OnSwitchChangeListener listener) {
    this(title, null, isChecked, 0, listener);
  }

  public SettingItem(
      String title,
      String description,
      boolean isChecked,
      int iconRes,
      OnSwitchChangeListener listener) {
    this.title = title;
    this.description = description;
    this.isChecked = isChecked;
    this.iconRes = iconRes;
    this.listener = listener;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public boolean isChecked() {
    return isChecked;
  }

  public int getIconRes() {
    return iconRes;
  }

  public OnSwitchChangeListener getListener() {
    return listener;
  }

  public void setChecked(boolean checked) {
    this.isChecked = checked;
  }

  public interface OnSwitchChangeListener {
    void onCheckedChanged(boolean isChecked);
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setTitle(String title) {
    this.title = title;
  }
}
