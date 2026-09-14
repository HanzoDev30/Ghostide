package ninja.coder.appuploader.main.appupdate.model;

import com.google.gson.annotations.SerializedName;

public class AppUpdateModel {
  @SerializedName("title")
  private String title;

  @SerializedName("massges")
  private String massges;

  @SerializedName("version")
  private String version;

  @SerializedName("link")
  private String link;

  @SerializedName("appname")
  private String appname;

  public String getTitle() {
    return this.title;
  }

  public String getMassges() {
    return this.massges;
  }

  public String getVersion() {
    return this.version;
  }

  public String getLink() {
    return this.link;
  }

  public String getAppname() {
    return this.appname;
  }
}
