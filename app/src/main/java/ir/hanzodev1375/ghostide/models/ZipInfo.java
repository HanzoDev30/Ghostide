package ir.hanzodev1375.ghostide.models;

public class ZipInfo {
  public final int fileCount;
  public final int dirCount;
  public final long totalUncompressed;
  public final long totalCompressed;
  public final int compressionRatio;
  public final boolean isEncrypted;

  public ZipInfo(
      int fileCount,
      int dirCount,
      long totalUncompressed,
      long totalCompressed,
      int compressionRatio,
      boolean isEncrypted) {
    this.fileCount = fileCount;
    this.dirCount = dirCount;
    this.totalUncompressed = totalUncompressed;
    this.totalCompressed = totalCompressed;
    this.compressionRatio = compressionRatio;
    this.isEncrypted = isEncrypted;
  }
}
