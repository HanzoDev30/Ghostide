package ir.hanzodev1375.ghostide.jgit.jgitandroid.model;

import java.util.Objects;

public class FetchResult {

  private final boolean success;
  private final String message;

  public FetchResult(boolean success, String message) {
    this.success = success;
    this.message = message;
  }

  public boolean isSuccess() {
    return success;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FetchResult)) return false;
    var that = (FetchResult) o;
    return success == that.success && java.util.Objects.equals(message, that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(success, message);
  }

  @Override
  public String toString() {
    return "FetchResult{success=" + success + ", message='" + message + "'}";
  }
}
