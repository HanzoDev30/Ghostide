package ir.hanzodev1375.ghostide.codeeditors.langs.yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class State {
  public static final int STATE_NORMAL = 0;
  public static final int STATE_BLOCK_LITERAL = 1;
  public static final int STATE_BLOCK_FOLDED = 2;

  public int state = STATE_NORMAL;
  public int blockIndent = -1;
  public List<String> identifiers = null;

  public void addIdentifier(CharSequence id) {
    if (identifiers == null) identifiers = new ArrayList<>();
    identifiers.add(id.toString());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    State s = (State) o;
    return state == s.state && blockIndent == s.blockIndent;
  }

  @Override
  public int hashCode() {
    return Objects.hash(state, blockIndent);
  }
}
