/**
 * Comment by ghost ide
 *
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.sass;

import java.util.Objects;

public class SassState {

  public boolean insideBlockComment = false;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SassState that = (SassState) o;
    return insideBlockComment == that.insideBlockComment;
  }

  @Override
  public int hashCode() {
    return Objects.hash(insideBlockComment);
  }
}
