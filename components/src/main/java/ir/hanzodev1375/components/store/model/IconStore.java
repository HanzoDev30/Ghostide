package ir.hanzodev1375.components.store.model;

public class IconStore implements BaseStore {

  private String linkRaw;
  private String iconName;

  public IconStore(String linkRaw, String iconName) {
    this.linkRaw = linkRaw;
    this.iconName = iconName;
  }

  @Override
  public int id() {
    return -1;
  }

  @Override
  public String tag() {
    return getClass().getSimpleName();
  }

  public String getLinkRaw() {
    return this.linkRaw;
  }

  public void setLinkRaw(String linkRaw) {
    this.linkRaw = linkRaw;
  }

  public String getIconName() {
    return this.iconName;
  }

  public void setIconName(String iconName) {
    this.iconName = iconName;
  }
}
