package ir.hanzodev1375.ghostide.jgit.jgitandroid.model;

import java.util.Objects;

public class PushResult {

  private final boolean success;
  private final String message;

  public PushResult(boolean success, String message) {
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
    if (!(o instanceof PushResult)) return false;
    var that = (PushResult) o;
    return success == that.success && Objects.equals(message, that.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(success, message);
  }

  @Override
  public String toString() {
    return "PushResult{success=" + success + ", message='" + message + "'}";
  }
}
