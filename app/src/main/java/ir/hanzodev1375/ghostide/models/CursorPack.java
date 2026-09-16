package ir.hanzodev1375.ghostide.models;

public record CursorPack(String cursorstart, String cursorend, String namecursorstart, String namecursorend) {

  public String getDisplayName() {
    return namecursorstart != null && !namecursorstart.isEmpty()
        ? namecursorstart
        : namecursorend;
  }
}