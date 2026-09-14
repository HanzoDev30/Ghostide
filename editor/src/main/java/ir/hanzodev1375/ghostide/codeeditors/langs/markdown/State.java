package ir.hanzodev1375.ghostide.codeeditors.langs.markdown;

import java.util.Objects;

/**
 * Per-line incremental state for Markdown.
 *
 * <p>Markdown is line-oriented: most block states carry over from one line to the next, so we only
 * need to track a handful of multi-line constructs.
 */
public class State {

  
  /** Normal / top-level parsing. */
  public static final int STATE_NORMAL = 0;

  /**
   * Inside a fenced code block (``` or ~~~). The exact fence character (backtick vs tilde) is
   * encoded in {@link #fenceChar}.
   */
  public static final int STATE_CODE_FENCE = 1;

  /** Inside an HTML block that ends with a blank line. */
  public static final int STATE_HTML_BLOCK = 2; 
  /** Current block state (one of the STATE_* constants above). */
  public int state = STATE_NORMAL;

  /**
   * For STATE_CODE_FENCE: the opening fence character ('`' or '~') and minimum fence length (≥ 3).
   */
  public char fenceChar = '`';
  public int fenceLen = 3;
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    State s = (State) o;
    return state == s.state && fenceChar == s.fenceChar && fenceLen == s.fenceLen;
  }

  @Override
  public int hashCode() {
    return Objects.hash(state, fenceChar, fenceLen);
  }
}
