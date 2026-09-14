package ir.hanzodev1375.ghostide.translator.model;

public class TranslationProgress {
  public final int current;
  public final int total;
  public final String currentKey;
  public final String languageFolder;

  public TranslationProgress(int current, int total, String currentKey, String languageFolder) {
    this.current = current;
    this.total = total;
    this.currentKey = currentKey;
    this.languageFolder = languageFolder;
  }

  public int getPercent() {
    return total == 0 ? 0 : (int) ((current / (float) total) * 100);
  }
}
