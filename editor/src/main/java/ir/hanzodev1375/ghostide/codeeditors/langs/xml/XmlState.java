/**
 * Comment by ghost ide
 *
 * @author: Ninjacoder
 */
package ir.hanzodev1375.ghostide.codeeditors.langs.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class XmlState {

  public int state = 0;

  public boolean hasBraces = false;

  public boolean insideTag = false;

  public List<String> identifiers = null;

  public void addIdentifier(CharSequence idt) {
    if (identifiers == null) identifiers = new ArrayList<>();
    identifiers.add(idt.toString());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    XmlState that = (XmlState) o;
    return state == that.state && hasBraces == that.hasBraces && insideTag == that.insideTag;
  }

  @Override
  public int hashCode() {
    return Objects.hash(state, hasBraces, insideTag);
  }
}
