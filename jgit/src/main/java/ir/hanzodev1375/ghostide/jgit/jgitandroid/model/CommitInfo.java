package ir.hanzodev1375.ghostide.jgit.jgitandroid.model;

import java.util.Objects;

public class CommitInfo {

  private final String hash;
  private final String shortHash;
  private final String message;
  private final String author;
  private final String email;
  private final long timestamp;

  public CommitInfo(
      String hash, String shortHash, String message, String author, String email, long timestamp) {
    this.hash = hash;
    this.shortHash = shortHash;
    this.message = message;
    this.author = author;
    this.email = email;
    this.timestamp = timestamp;
  }

  public String getHash() {
    return hash;
  }

  public String getShortHash() {
    return shortHash;
  }

  public String getMessage() {
    return message;
  }

  public String getAuthor() {
    return author;
  }

  public String getEmail() {
    return email;
  }

  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CommitInfo)) return false;
    var that = (CommitInfo) o;
    return timestamp == that.timestamp
        && Objects.equals(hash, that.hash)
        && Objects.equals(shortHash, that.shortHash)
        && Objects.equals(message, that.message)
        && Objects.equals(author, that.author)
        && Objects.equals(email, that.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hash, shortHash, message, author, email, timestamp);
  }

  @Override
  public String toString() {
    return "CommitInfo{hash='"
        + hash
        + "', shortHash='"
        + shortHash
        + "', message='"
        + message
        + "', author='"
        + author
        + "', email='"
        + email
        + "', timestamp="
        + timestamp
        + "}";
  }
}
