package ir.hanzodev1375.ghostide.codeeditors.langs.gradle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class State {
  public int state = 0;
  public boolean hasBraces = false;
  public List<String> identifiers = null;

  public void addIdentifier(CharSequence idt) {
    if (identifiers == null) {
      identifiers = new ArrayList<>();
    }
    identifiers.add(idt.toString());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    State state1 = (State) o;
    return state == state1.state && hasBraces == state1.hasBraces;
  }

  @Override
  public int hashCode() {
    return Objects.hash(state, hasBraces);
  }
}
