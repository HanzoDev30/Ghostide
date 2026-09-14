package ir.hanzodev1375.ghostide.codeeditors.util.formatter;

import java.util.regex.Pattern;

public class JsFormatter extends BaseFormatter {

  private static final Pattern MULTI_NL = Pattern.compile("\\n{3,}");
  private static final Pattern TRAILING_SP = Pattern.compile("[ \t]+\n");

  @Override
  public String format(String code) {
    if (code == null || code.isEmpty()) return "";
    code = code.replace("\r\n", "\n").replace("\r", "\n");
    String norm = normalise(code);
    String indented = reIndent(norm);
    indented = TRAILING_SP.matcher(indented).replaceAll("\n");
    indented = MULTI_NL.matcher(indented).replaceAll("\n\n");
    return indented.trim() + "\n";
  }

  private String normalise(String code) {
    StringBuilder out = new StringBuilder(code.length() + code.length() / 4);
    int len = code.length();
    boolean inLineComment = false;
    boolean inBlockComment = false;
    boolean inString = false;
    char stringChar = 0;
    int templateDepth = 0;

    for (int i = 0; i < len; i++) {
      char c = code.charAt(i);

      if (inBlockComment) {
        out.append(c);
        if (c == '*' && i + 1 < len && code.charAt(i + 1) == '/') {
          out.append('/');
          i++;
          out.append('\n');
          inBlockComment = false;
        }
        continue;
      }

      if (inLineComment) {
        out.append(c);
        if (c == '\n') inLineComment = false;
        continue;
      }

      if (inString) {
        out.append(c);
        if (c == '\\') {
          if (i + 1 < len) {
            out.append(code.charAt(++i));
          }
          continue;
        }
        if (c == stringChar) {
          if (stringChar == '`') templateDepth--;
          inString = false;
        }
        continue;
      }

      if (c == '/' && i + 1 < len) {
        if (code.charAt(i + 1) == '/') {
          inLineComment = true;
          out.append("//");
          i++;
          continue;
        }
        if (code.charAt(i + 1) == '*') {
          inBlockComment = true;
          out.append("/*");
          i++;
          continue;
        }
      }

      if (c == '"' || c == '\'' || c == '`') {
        inString = true;
        stringChar = c;
        if (c == '`') templateDepth++;
        out.append(c);
        continue;
      }

      if (c == '\t' || c == '\r') {
        out.append(' ');
        continue;
      }
      if (c == '\n') {

        if (out.length() > 0 && out.charAt(out.length() - 1) != '\n') out.append('\n');
        continue;
      }
      if (c == ' ' && out.length() > 0 && out.charAt(out.length() - 1) == ' ') continue;

      if (c == '{' || c == '[') {
        ensureSpace(out);
        out.append(c).append('\n');
        continue;
      }
      if (c == '}' || c == ']') {
        trimTrailingSpace(out);
        out.append('\n').append(c);

        int j = i + 1;
        while (j < len && (code.charAt(j) == ' ' || code.charAt(j) == '\t')) j++;
        char next = j < len ? code.charAt(j) : 0;
        if (next == ';' || next == ',' || next == ')' || next == ']' || next == '}') {

        } else if (next == '.' || next == '?') {

        } else {

          String rem = j < len ? code.substring(j) : "";
          if (rem.startsWith("else") || rem.startsWith("catch") || rem.startsWith("finally")) {
            out.append(' ');
          } else {
            out.append('\n');
          }
        }
        continue;
      }
      if (c == ';') {
        out.append(';').append('\n');
        continue;
      }

      if (c == ',') {
        out.append(',').append(' ');
        continue;
      }

      if (c == '=' && i + 1 < len && code.charAt(i + 1) == '>') {
        ensureSpace(out);
        out.append("=>");
        i++;
        ensureSpace(out);
        continue;
      }

      if ((c == '=' || c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c == '&'
              || c == '|' || c == '<' || c == '>' || c == '!')
          && i + 1 < len) {
        char next = code.charAt(i + 1);
        boolean compound =
            (next == '='
                || next == '>'
                || next == '+'
                || next == '-'
                || next == '&'
                || next == '|'
                || next == '?');

        if (out.length() > 0
            && out.charAt(out.length() - 1) != ' '
            && out.charAt(out.length() - 1) != '\n') out.append(' ');
        out.append(c);
        if (compound) {
          out.append(code.charAt(++i));
        }
        out.append(' ');
        continue;
      }
      out.append(c);
    }
    return out.toString();
  }

  private String reIndent(String norm) {
    StringBuilder out = new StringBuilder(norm.length());
    String[] lines = norm.split("\n", -1);
    int depth = 0;
    boolean lastWasBlank = false;
    boolean lastWasClose = false;

    for (int li = 0; li < lines.length; li++) {
      String line = lines[li].trim();
      if (line.isEmpty()) {
        if (!lastWasBlank && out.length() > 0) {
          out.append('\n');
          lastWasBlank = true;
        }
        continue;
      }

      boolean startsWithClose = line.charAt(0) == '}' || line.charAt(0) == ']';
      boolean endsWithOpen =
          line.charAt(line.length() - 1) == '{' || line.charAt(line.length() - 1) == '[';

      if (depth == 0
          && (line.startsWith("function ")
              || line.startsWith("class ")
              || line.startsWith("const ")
              || line.startsWith("let ")
              || line.startsWith("var ")
              || line.startsWith("export "))
          && out.length() > 0
          && !lastWasBlank) {
        out.append('\n');
      }

      if (startsWithClose) depth = Math.max(0, depth - 1);

      String pad = getIndentString(depth);

      out.append(pad).append(line).append('\n');
      lastWasBlank = false;

      if (endsWithOpen && !startsWithClose) depth++;
      else if (endsWithOpen && startsWithClose) {
        /* depth already decremented, now increment */
        depth++;
      }

      if (startsWithClose && depth == 0) {
        out.append('\n');
        lastWasBlank = true;
      }
      lastWasClose = startsWithClose;
    }
    return out.toString();
  }

  private void ensureSpace(StringBuilder sb) {
    if (sb.length() > 0) {
      char last = sb.charAt(sb.length() - 1);
      if (last != ' ' && last != '\n' && last != '(') sb.append(' ');
    }
  }

  private void trimTrailingSpace(StringBuilder sb) {
    while (sb.length() > 0 && sb.charAt(sb.length() - 1) == ' ') sb.deleteCharAt(sb.length() - 1);
  }
}
