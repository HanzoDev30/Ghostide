package ir.hanzodev1375.ghostide.jgit.diff;

import java.util.List;

public interface SyntaxHighlighter {
  void highlight(String text, DiffLine.LineType type, String language, Callback callback);

  interface Callback {
    void onResult(List<HighlightSpan> spans);
  }

  class HighlightSpan {
    public int start, end, color;

    public HighlightSpan(int start, int end, int color) {
      this.start = start;
      this.end = end;
      this.color = color;
    }
  }
}
