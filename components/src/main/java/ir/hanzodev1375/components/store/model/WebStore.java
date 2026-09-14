package ir.hanzodev1375.components.store.model;

public class WebStore implements BaseStore {

  private String linkRaw;
  private String screen1, screen2, screen3, icon;
  private String name;
  private String zipFile;

  public WebStore(
      String linkRaw,
      String screen1,
      String screen2,
      String screen3,
      String name,
      String icon,
      String zipFile) {
    this.linkRaw = linkRaw;
    this.screen1 = screen1;
    this.screen2 = screen2;
    this.screen3 = screen3;
    this.name = name;
    this.icon = icon;
    this.zipFile = zipFile;
  }

  @Override
  public int id() {
    return 1;
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

  public String getScreen1() {
    return this.screen1;
  }

  public void setScreen1(String screen1) {
    this.screen1 = screen1;
  }

  public String getScreen2() {
    return this.screen2;
  }

  public void setScreen2(String screen2) {
    this.screen2 = screen2;
  }

  public String getScreen3() {
    return this.screen3;
  }

  public void setScreen3(String screen3) {
    this.screen3 = screen3;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getIcon() {
    return this.icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public String getZipFile() {
    return this.zipFile;
  }

  public void setZipFile(String zipFile) {
    this.zipFile = zipFile;
  }
}
