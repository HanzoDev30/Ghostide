package ir.hanzodev1375.ghostide.ai.model;

public class OpencodeModelInfo {

  private final String providerId;
  private final String providerName;
  private final String modelId;
  private final String name;
  private final boolean free;
  private final boolean available;

  public OpencodeModelInfo(
      String providerId,
      String providerName,
      String modelId,
      String name,
      boolean free,
      boolean available) {
    this.providerId = providerId;
    this.providerName = providerName;
    this.modelId = modelId;
    this.name = name;
    this.free = free;
    this.available = available;
  }

  public String getProviderId() {
    return providerId;
  }

  public String getProviderName() {
    return providerName;
  }

  public String getModelId() {
    return modelId;
  }

  public String getName() {
    return name;
  }

  public boolean isFree() {
    return free;
  }

  public boolean isAvailable() {
    return available;
  }

  public String qualifiedId() {
    return providerId + "/" + modelId;
  }
}