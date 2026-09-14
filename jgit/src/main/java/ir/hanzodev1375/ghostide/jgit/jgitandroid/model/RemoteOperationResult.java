package ir.hanzodev1375.ghostide.jgit.jgitandroid.model;

import java.util.Objects;

public class RemoteOperationResult {

  private final boolean success;
  private final String message;
  private final String operation;

  public RemoteOperationResult(boolean success, String message, String operation) {
    this.success = success;
    this.message = message;
    this.operation = operation;
  }

  public boolean isSuccess() {
    return success;
  }

  public String getMessage() {
    return message;
  }

  public String getOperation() {
    return operation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RemoteOperationResult)) return false;
    var that = (RemoteOperationResult) o;
    return success == that.success
        && Objects.equals(message, that.message)
        && Objects.equals(operation, that.operation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(success, message, operation);
  }

  @Override
  public String toString() {
    return "RemoteOperationResult{success="
        + success
        + ", message='"
        + message
        + "', operation='"
        + operation
        + "'}";
  }
}
