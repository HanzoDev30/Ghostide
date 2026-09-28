package ir.hanzodev1375.ghostide.jgit.model;

/** Aggregate counters shown on the GitHub profile header. */
public class UserStats {

  public static final UserStats EMPTY = new UserStats(0, 0);

  private final int issues;
  private final int stars;

  public UserStats(int issues, int stars) {
    this.issues = issues;
    this.stars = stars;
  }

  public int getIssues() {
    return issues;
  }

  public int getStars() {
    return stars;
  }
}
