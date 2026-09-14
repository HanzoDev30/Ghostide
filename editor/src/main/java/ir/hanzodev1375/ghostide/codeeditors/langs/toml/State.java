package ir.hanzodev1375.ghostide.codeeditors.langs.toml;

import java.util.Objects;

public class State {
  /** 0 = normal key-value line, 1 = inside inline table */
  public int state = 0;

  public boolean hasBraces = false;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    State s = (State) o;
    return state == s.state && hasBraces == s.hasBraces;
  }

  @Override
  public int hashCode() {
    return Objects.hash(state, hasBraces);
  }
}
