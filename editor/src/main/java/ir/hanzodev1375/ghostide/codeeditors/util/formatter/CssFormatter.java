package ir.hanzodev1375.ghostide.codeeditors.util.formatter;

import java.util.regex.Pattern;

public class CssFormatter extends BaseFormatter {

  private static final Pattern MULTI_NL = Pattern.compile("\\n{3,}");

  @Override
  public String format(String code) {
    if (code == null || code.isEmpty()) return "";

    code = code.replace("\r\n", "\n").replace("\r", "\n");

    StringBuilder out = new StringBuilder(code.length() + code.length() / 4);
    int depth = 0;
    boolean inString = false;
    char stringChar = 0;
    boolean inComment = false;
    boolean lineStart = true;
    StringBuilder decl = new StringBuilder();
    boolean inValue = false;

    StringBuilder norm = new StringBuilder();
    int braceDepth = 0;
    for (int i = 0; i < code.length(); i++) {
      char c = code.charAt(i);

      if (inComment) {
        norm.append(c);
        if (c == '*' && i + 1 < code.length() && code.charAt(i + 1) == '/') {
          norm.append('/');
          i++;
          norm.append('\n');
          inComment = false;
        }
        continue;
      }
      if (c == '/' && i + 1 < code.length() && code.charAt(i + 1) == '*') {
        norm.append("/*");
        i++;
        inComment = true;
        continue;
      }

      if (inString) {
        norm.append(c);
        if (c == stringChar && (i == 0 || code.charAt(i - 1) != '\\')) inString = false;
        continue;
      }
      if (c == '"' || c == '\'') {
        inString = true;
        stringChar = c;
        norm.append(c);
        continue;
      }

      if (c == '\t' || c == '\r') {
        norm.append(' ');
        continue;
      }
      if (c == '\n') {

        if (norm.length() > 0 && norm.charAt(norm.length() - 1) != '\n') norm.append('\n');
        continue;
      }

      if (c == ' ' && norm.length() > 0 && norm.charAt(norm.length() - 1) == ' ') continue;

      if (c == '{') {
        while (norm.length() > 0 && norm.charAt(norm.length() - 1) == ' ') {
          norm.deleteCharAt(norm.length() - 1);
        }
        norm.append(" {\n");
        braceDepth++;
        continue;
      }
      if (c == '}') {
        norm.append("\n}\n\n");
        if (braceDepth > 0) braceDepth--;
        continue;
      }
      if (c == ';') {
        norm.append(";\n");
        continue;
      }
      if (c == ':') {

        if (i + 1 < code.length() && code.charAt(i + 1) == ':') {
          norm.append("::");
          i++;
          continue;
        }

        if (braceDepth > 0) {
          norm.append(": ");
          while (i + 1 < code.length() && code.charAt(i + 1) == ' ') i++;
        } else {
          norm.append(':');
        }
        continue;
      }
      if (c == ',') {

        norm.append(", ");
        continue;
      }
      norm.append(c);
    }

    String[] lines = norm.toString().split("\n", -1);
    boolean lastWasBlank = false;
    for (String rawLine : lines) {
      String line = rawLine.trim();
      if (line.isEmpty()) {
        if (!lastWasBlank && out.length() > 0) {
          out.append('\n');
          lastWasBlank = true;
        }
        continue;
      }
      lastWasBlank = false;

      if (line.equals("}")) {
        depth = Math.max(0, depth - 1);
        out.append(getIndentString(depth)).append('}').append('\n');
        continue;
      }

      if (line.endsWith("{")) {

        String selector = line.substring(0, line.length() - 1).trim();
        String[] selParts = selector.split(",");
        if (selParts.length > 1) {
          for (int s = 0; s < selParts.length; s++) {
            out.append(getIndentString(depth)).append(selParts[s].trim());
            if (s < selParts.length - 1) out.append(",\n");
          }
          out.append(" {\n");
        } else {
          out.append(getIndentString(depth)).append(line).append('\n');
        }
        depth++;
        continue;
      }

      if (line.endsWith(";") && depth > 0) {
        out.append(getIndentString(depth)).append(line).append('\n');
        continue;
      }

      if (line.startsWith("@") && line.endsWith("{")) {
        out.append(getIndentString(depth)).append(line).append('\n');
        depth++;
        continue;
      }

      out.append(getIndentString(depth)).append(line).append('\n');
    }

    String result = MULTI_NL.matcher(out.toString()).replaceAll("\n\n");
    return result.trim() + "\n";
  }
}
