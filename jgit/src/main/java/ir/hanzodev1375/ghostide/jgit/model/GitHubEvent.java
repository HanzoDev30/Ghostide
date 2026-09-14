package ir.hanzodev1375.ghostide.jgit.model;

import com.google.gson.annotations.SerializedName;

public class GitHubEvent {

  @SerializedName("type")
  private String type;

  @SerializedName("repo")
  private Repo repo;

  @SerializedName("created_at")
  private String createdAt;

  public String getType() {
    return type;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public String getRepoName() {
    return repo != null ? repo.name : "";
  }

  public String getTypeLabel() {
    if (type == null) return "Unknown";
    return switch (type) {
      case "PushEvent" -> "Pushed to";
      case "WatchEvent" -> "Starred";
      case "ForkEvent" -> "Forked";
      case "CreateEvent" -> "Created";
      case "IssuesEvent" -> "Issue on";
      case "PullRequestEvent" -> "Pull Request on";
      case "DeleteEvent" -> "Deleted from";
      default -> type.replace("Event", "");
    };
  }

  public static class Repo {
    @SerializedName("name")
    public String name;
  }
}
