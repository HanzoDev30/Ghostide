package ir.hanzodev1375.ghostide.translator.model;

public class StringEntry {
  public final String name;
  public final String value;
  public final boolean translatable;

  public StringEntry(String name, String value, boolean translatable) {
    this.name = name;
    this.value = value;
    this.translatable = translatable;
  }
}
