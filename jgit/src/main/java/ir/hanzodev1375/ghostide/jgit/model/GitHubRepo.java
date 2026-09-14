package ir.hanzodev1375.ghostide.jgit.model;

import com.google.gson.annotations.SerializedName;

public class GitHubRepo {

  @SerializedName("name")
  private String name;

  @SerializedName("description")
  private String description;

  @SerializedName("stargazers_count")
  private int stars;

  @SerializedName("language")
  private String language;

  @SerializedName("html_url")
  private String url;

  @SerializedName("fork")
  private boolean fork;

  @SerializedName("private")
  private boolean isPrivate;

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public int getStars() {
    return stars;
  }

  public String getLanguage() {
    return language;
  }

  public String getUrl() {
    return url;
  }

  public boolean isFork() {
    return fork;
  }

  public boolean isPrivate() {
    return isPrivate;
  }
}
