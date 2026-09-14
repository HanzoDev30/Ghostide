package ir.hanzodev1375.ghostide.jgit.jgitandroid.model;

import java.util.Objects;

public class RemoteInfo {

  private final String name;
  private final String fetchUrl;
  private final String pushUrl;

  public RemoteInfo(String name, String fetchUrl, String pushUrl) {
    this.name = name;
    this.fetchUrl = fetchUrl;
    this.pushUrl = pushUrl;
  }

  public String getName() {
    return name;
  }

  public String getFetchUrl() {
    return fetchUrl;
  }

  public String getPushUrl() {
    return pushUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RemoteInfo)) return false;
    var that = (RemoteInfo) o;
    return Objects.equals(name, that.name)
        && Objects.equals(fetchUrl, that.fetchUrl)
        && Objects.equals(pushUrl, that.pushUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, fetchUrl, pushUrl);
  }

  @Override
  public String toString() {
    return "RemoteInfo{name='"
        + name
        + "', fetchUrl='"
        + fetchUrl
        + "', pushUrl='"
        + pushUrl
        + "'}";
  }
}
