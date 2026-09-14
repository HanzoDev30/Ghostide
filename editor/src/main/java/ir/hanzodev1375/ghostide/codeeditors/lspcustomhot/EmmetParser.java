package ir.hanzodev1375.ghostide.codeeditors.lspcustomhot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Emmet expander — HTML + CSS. Ported for GhostIDE. */
public class EmmetParser {

  private static final Pattern PAT_ABBR =
      Pattern.compile("^[a-zA-Z0-9_.#*()+>^\\[\\]=\"{} $!:\\-]+$");
  private static final Pattern PAT_EMMET_PARSE =
      Pattern.compile(
          "^([a-zA-Z0-9_-]*)(#[a-zA-Z0-9_$\\-]+)?((?:\\.[a-zA-Z0-9_$\\-]+)*)(?:\\[([^\\]]+)\\])?(?:\\{([^}]*)\\})?(?:\\*([0-9]+))?$");
  private static final Pattern PAT_CSS_NUMERIC =
      Pattern.compile("^([a-z]+)(-?[0-9]+(?:-[0-9]+)*)([a-z%]*)$");

  private static final Map<String, String> CSS_ABBREVS = new HashMap<>();
  private static final Map<String, String> CSS_PROP_MAP = new HashMap<>();

  static {
    CSS_ABBREVS.put("d", "display: |;");
    CSS_ABBREVS.put("df", "display: flex;");
    CSS_ABBREVS.put("dif", "display: inline-flex;");
    CSS_ABBREVS.put("db", "display: block;");
    CSS_ABBREVS.put("dib", "display: inline-block;");
    CSS_ABBREVS.put("di", "display: inline;");
    CSS_ABBREVS.put("dn", "display: none;");
    CSS_ABBREVS.put("dg", "display: grid;");
    CSS_ABBREVS.put("dig", "display: inline-grid;");
    CSS_ABBREVS.put("dt", "display: table;");
    CSS_ABBREVS.put("fxd", "flex-direction: |;");
    CSS_ABBREVS.put("fxdr", "flex-direction: row;");
    CSS_ABBREVS.put("fxdc", "flex-direction: column;");
    CSS_ABBREVS.put("fxdrr", "flex-direction: row-reverse;");
    CSS_ABBREVS.put("fxdcr", "flex-direction: column-reverse;");
    CSS_ABBREVS.put("fxw", "flex-wrap: wrap;");
    CSS_ABBREVS.put("fxwn", "flex-wrap: nowrap;");
    CSS_ABBREVS.put("fxg", "flex-grow: |;");
    CSS_ABBREVS.put("fxs", "flex-shrink: |;");
    CSS_ABBREVS.put("fxb", "flex-basis: |;");
    CSS_ABBREVS.put("fx", "flex: |;");
    CSS_ABBREVS.put("jc", "justify-content: |;");
    CSS_ABBREVS.put("jcc", "justify-content: center;");
    CSS_ABBREVS.put("jcsb", "justify-content: space-between;");
    CSS_ABBREVS.put("jcsa", "justify-content: space-around;");
    CSS_ABBREVS.put("jcse", "justify-content: space-evenly;");
    CSS_ABBREVS.put("jcfs", "justify-content: flex-start;");
    CSS_ABBREVS.put("jcfe", "justify-content: flex-end;");
    CSS_ABBREVS.put("ai", "align-items: |;");
    CSS_ABBREVS.put("aic", "align-items: center;");
    CSS_ABBREVS.put("ais", "align-items: stretch;");
    CSS_ABBREVS.put("aifs", "align-items: flex-start;");
    CSS_ABBREVS.put("aife", "align-items: flex-end;");
    CSS_ABBREVS.put("aib", "align-items: baseline;");
    CSS_ABBREVS.put("as", "align-self: |;");
    CSS_ABBREVS.put("asc", "align-self: center;");
    CSS_ABBREVS.put("ac", "align-content: |;");
    CSS_ABBREVS.put("acc", "align-content: center;");
    CSS_ABBREVS.put("ji", "justify-items: |;");
    CSS_ABBREVS.put("js", "justify-self: |;");
    CSS_ABBREVS.put("pi", "place-items: center;");
    CSS_ABBREVS.put("pc", "place-content: center;");
    CSS_ABBREVS.put("gtc", "grid-template-columns: |;");
    CSS_ABBREVS.put("gtr", "grid-template-rows: |;");
    CSS_ABBREVS.put("gg", "grid-gap: |;");
    CSS_ABBREVS.put("gap", "gap: |;");
    CSS_ABBREVS.put("rg", "row-gap: |;");
    CSS_ABBREVS.put("cg", "column-gap: |;");
    CSS_ABBREVS.put("gc", "grid-column: |;");
    CSS_ABBREVS.put("gr", "grid-row: |;");
    CSS_ABBREVS.put("ga", "grid-area: |;");
    CSS_ABBREVS.put("gaf", "grid-auto-flow: |;");
    CSS_ABBREVS.put("gac", "grid-auto-columns: |;");
    CSS_ABBREVS.put("gar", "grid-auto-rows: |;");
    CSS_ABBREVS.put("pos", "position: |;");
    CSS_ABBREVS.put("poss", "position: static;");
    CSS_ABBREVS.put("posr", "position: relative;");
    CSS_ABBREVS.put("posa", "position: absolute;");
    CSS_ABBREVS.put("posf", "position: fixed;");
    CSS_ABBREVS.put("posst", "position: sticky;");
    CSS_ABBREVS.put("t", "top: |;");
    CSS_ABBREVS.put("r", "right: |;");
    CSS_ABBREVS.put("b", "bottom: |;");
    CSS_ABBREVS.put("l", "left: |;");
    CSS_ABBREVS.put("z", "z-index: |;");
    CSS_ABBREVS.put("inset", "inset: |;");
    CSS_ABBREVS.put("m", "margin: |;");
    CSS_ABBREVS.put("mt", "margin-top: |;");
    CSS_ABBREVS.put("mr", "margin-right: |;");
    CSS_ABBREVS.put("mb", "margin-bottom: |;");
    CSS_ABBREVS.put("ml", "margin-left: |;");
    CSS_ABBREVS.put("mx", "margin-inline: |;");
    CSS_ABBREVS.put("my", "margin-block: |;");
    CSS_ABBREVS.put("ma", "margin: auto;");
    CSS_ABBREVS.put("mxa", "margin-inline: auto;");
    CSS_ABBREVS.put("p", "padding: |;");
    CSS_ABBREVS.put("pt", "padding-top: |;");
    CSS_ABBREVS.put("pr", "padding-right: |;");
    CSS_ABBREVS.put("pb", "padding-bottom: |;");
    CSS_ABBREVS.put("pl", "padding-left: |;");
    CSS_ABBREVS.put("px", "padding-inline: |;");
    CSS_ABBREVS.put("py", "padding-block: |;");
    CSS_ABBREVS.put("w", "width: |;");
    CSS_ABBREVS.put("h", "height: |;");
    CSS_ABBREVS.put("maw", "max-width: |;");
    CSS_ABBREVS.put("mah", "max-height: |;");
    CSS_ABBREVS.put("miw", "min-width: |;");
    CSS_ABBREVS.put("mih", "min-height: |;");
    CSS_ABBREVS.put("w100", "width: 100%;");
    CSS_ABBREVS.put("h100", "height: 100%;");
    CSS_ABBREVS.put("ff", "font-family: |;");
    CSS_ABBREVS.put("fs", "font-size: |;");
    CSS_ABBREVS.put("fw", "font-weight: |;");
    CSS_ABBREVS.put("fwb", "font-weight: bold;");
    CSS_ABBREVS.put("fwn", "font-weight: normal;");
    CSS_ABBREVS.put("fsi", "font-style: italic;");
    CSS_ABBREVS.put("fsn", "font-style: normal;");
    CSS_ABBREVS.put("ta", "text-align: |;");
    CSS_ABBREVS.put("tac", "text-align: center;");
    CSS_ABBREVS.put("tal", "text-align: left;");
    CSS_ABBREVS.put("tar", "text-align: right;");
    CSS_ABBREVS.put("taj", "text-align: justify;");
    CSS_ABBREVS.put("td", "text-decoration: |;");
    CSS_ABBREVS.put("tdn", "text-decoration: none;");
    CSS_ABBREVS.put("tdu", "text-decoration: underline;");
    CSS_ABBREVS.put("tt", "text-transform: |;");
    CSS_ABBREVS.put("ttu", "text-transform: uppercase;");
    CSS_ABBREVS.put("ttl", "text-transform: lowercase;");
    CSS_ABBREVS.put("ttc", "text-transform: capitalize;");
    CSS_ABBREVS.put("lh", "line-height: |;");
    CSS_ABBREVS.put("ls", "letter-spacing: |;");
    CSS_ABBREVS.put("ws", "white-space: |;");
    CSS_ABBREVS.put("wsnw", "white-space: nowrap;");
    CSS_ABBREVS.put("wof", "word-break: break-all;");
    CSS_ABBREVS.put("tov", "text-overflow: ellipsis;");
    CSS_ABBREVS.put("bg", "background: |;");
    CSS_ABBREVS.put("bgc", "background-color: |;");
    CSS_ABBREVS.put("bgi", "background-image: url(|);");
    CSS_ABBREVS.put("bgp", "background-position: |;");
    CSS_ABBREVS.put("bgs", "background-size: |;");
    CSS_ABBREVS.put("bgsc", "background-size: cover;");
    CSS_ABBREVS.put("bgr", "background-repeat: |;");
    CSS_ABBREVS.put("bgrn", "background-repeat: no-repeat;");
    CSS_ABBREVS.put("bd", "border: |;");
    CSS_ABBREVS.put("bdn", "border: none;");
    CSS_ABBREVS.put("bds", "border: 1px solid |;");
    CSS_ABBREVS.put("bdt", "border-top: |;");
    CSS_ABBREVS.put("bdr", "border-right: |;");
    CSS_ABBREVS.put("bdb", "border-bottom: |;");
    CSS_ABBREVS.put("bdl", "border-left: |;");
    CSS_ABBREVS.put("bdrs", "border-radius: |;");
    CSS_ABBREVS.put("br", "border-radius: |;");
    CSS_ABBREVS.put("brc", "border-radius: 50%;");
    CSS_ABBREVS.put("bxsh", "box-shadow: |;");
    CSS_ABBREVS.put("bxshn", "box-shadow: none;");
    CSS_ABBREVS.put("ov", "overflow: |;");
    CSS_ABBREVS.put("ovh", "overflow: hidden;");
    CSS_ABBREVS.put("ova", "overflow: auto;");
    CSS_ABBREVS.put("ovs", "overflow: scroll;");
    CSS_ABBREVS.put("ovv", "overflow: visible;");
    CSS_ABBREVS.put("ovx", "overflow-x: |;");
    CSS_ABBREVS.put("ovy", "overflow-y: |;");
    CSS_ABBREVS.put("v", "visibility: |;");
    CSS_ABBREVS.put("vh", "visibility: hidden;");
    CSS_ABBREVS.put("vv", "visibility: visible;");
    CSS_ABBREVS.put("op", "opacity: |;");
    CSS_ABBREVS.put("cur", "cursor: |;");
    CSS_ABBREVS.put("curp", "cursor: pointer;");
    CSS_ABBREVS.put("curd", "cursor: default;");
    CSS_ABBREVS.put("pe", "pointer-events: |;");
    CSS_ABBREVS.put("pen", "pointer-events: none;");
    CSS_ABBREVS.put("us", "user-select: none;");
    CSS_ABBREVS.put("trs", "transition: |;");
    CSS_ABBREVS.put("trsa", "transition: all 0.3s ease;");
    CSS_ABBREVS.put("anim", "animation: |;");
    CSS_ABBREVS.put("animd", "animation-duration: |;");
    CSS_ABBREVS.put("tf", "transform: |;");
    CSS_ABBREVS.put("bxz", "box-sizing: border-box;");
    CSS_ABBREVS.put("ct", "content: \"|\"");
    CSS_ABBREVS.put("ol", "outline: |;");
    CSS_ABBREVS.put("oln", "outline: none;");
    CSS_ABBREVS.put("rsz", "resize: |;");
    CSS_ABBREVS.put("ap", "appearance: none;");
    CSS_ABBREVS.put("fil", "filter: |;");
    CSS_ABBREVS.put("obf", "object-fit: |;");
    CSS_ABBREVS.put("obfc", "object-fit: cover;");
    CSS_ABBREVS.put("obfct", "object-fit: contain;");
    CSS_ABBREVS.put("ar", "aspect-ratio: |;");
    CSS_ABBREVS.put("lis", "list-style: |;");
    CSS_ABBREVS.put("lisn", "list-style: none;");
    CSS_ABBREVS.put("c", "color: |;");
    CSS_ABBREVS.put("cen", "display: flex;\njustify-content: center;\nalign-items: center;");
    CSS_ABBREVS.put(
        "abscen", "position: absolute;\ntop: 50%;\nleft: 50%;\ntransform: translate(-50%, -50%);");
    CSS_ABBREVS.put("trun", "overflow: hidden;\ntext-overflow: ellipsis;\nwhite-space: nowrap;");

    CSS_PROP_MAP.put("m", "margin");
    CSS_PROP_MAP.put("mt", "margin-top");
    CSS_PROP_MAP.put("mr", "margin-right");
    CSS_PROP_MAP.put("mb", "margin-bottom");
    CSS_PROP_MAP.put("ml", "margin-left");
    CSS_PROP_MAP.put("mx", "margin-inline");
    CSS_PROP_MAP.put("my", "margin-block");
    CSS_PROP_MAP.put("p", "padding");
    CSS_PROP_MAP.put("pt", "padding-top");
    CSS_PROP_MAP.put("pr", "padding-right");
    CSS_PROP_MAP.put("pb", "padding-bottom");
    CSS_PROP_MAP.put("pl", "padding-left");
    CSS_PROP_MAP.put("px", "padding-inline");
    CSS_PROP_MAP.put("py", "padding-block");
    CSS_PROP_MAP.put("w", "width");
    CSS_PROP_MAP.put("h", "height");
    CSS_PROP_MAP.put("maw", "max-width");
    CSS_PROP_MAP.put("mah", "max-height");
    CSS_PROP_MAP.put("miw", "min-width");
    CSS_PROP_MAP.put("mih", "min-height");
    CSS_PROP_MAP.put("fs", "font-size");
    CSS_PROP_MAP.put("lh", "line-height");
    CSS_PROP_MAP.put("ls", "letter-spacing");
    CSS_PROP_MAP.put("br", "border-radius");
    CSS_PROP_MAP.put("bdrs", "border-radius");
    CSS_PROP_MAP.put("t", "top");
    CSS_PROP_MAP.put("r", "right");
    CSS_PROP_MAP.put("b", "bottom");
    CSS_PROP_MAP.put("l", "left");
    CSS_PROP_MAP.put("gap", "gap");
    CSS_PROP_MAP.put("rg", "row-gap");
    CSS_PROP_MAP.put("cg", "column-gap");
    CSS_PROP_MAP.put("op", "opacity");
    CSS_PROP_MAP.put("z", "z-index");
    CSS_PROP_MAP.put("fw", "font-weight");
  }

  public static String expandHtml(String abbr) {
    if (abbr == null || abbr.trim().isEmpty()) return null;
    if (abbr.equals("!"))
      return "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n"
          + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
          + "    <title>Document</title>\n</head>\n<body>\n    |\n</body>\n</html>";
    if (!PAT_ABBR.matcher(abbr).matches()) return null;
    try {
      return parseEmmet(abbr);
    } catch (Exception e) {
      return null;
    }
  }

  public static String expandCss(String abbr) {
    if (abbr == null || abbr.isEmpty()) return null;
    String named = CSS_ABBREVS.get(abbr);
    if (named != null) return named;
    Matcher m = PAT_CSS_NUMERIC.matcher(abbr);
    if (m.matches()) {
      String prop = CSS_PROP_MAP.get(m.group(1));
      if (prop == null) return null;
      String numPart = m.group(2), unitSuffix = m.group(3);
      String unit;
      switch (unitSuffix) {
        case "p":
        case "%":
          unit = "%";
          break;
        case "e":
          unit = "em";
          break;
        case "r":
          unit = "rem";
          break;
        case "x":
          unit = "px";
          break;
        case "vh":
          unit = "vh";
          break;
        case "vw":
          unit = "vw";
          break;
        default:
          unit = unitSuffix.isEmpty() ? "px" : unitSuffix;
      }
      String[] parts = numPart.split("-");
      StringBuilder value = new StringBuilder();
      for (int i = 0; i < parts.length; i++) {
        if (parts[i].isEmpty()) continue;
        String val = parts[i];
        if (val.equals("0")
            || prop.equals("z-index")
            || prop.equals("opacity")
            || prop.equals("font-weight")
            || prop.equals("line-height")) value.append(val);
        else value.append(val).append(unit);
        if (i < parts.length - 1) value.append(" ");
      }
      return value.length() == 0 ? null : prop + ": " + value + ";";
    }
    return null;
  }

  private static String parseEmmet(String abbr) {
    List<Node> roots = new ArrayList<>();
    Node current = null;
    int i = 0, len = abbr.length();
    while (i < len) {
      char c = abbr.charAt(i);
      char op = 0;
      if (c == '>' || c == '+' || c == '^') {
        op = c;
        i++;
        if (i >= len) return null;
      }
      if (i < len && abbr.charAt(i) == '(') {
        int closeIdx = findMatchingParen(abbr, i);
        if (closeIdx < 0) return null;
        String groupAbbr = abbr.substring(i + 1, closeIdx);
        i = closeIdx + 1;
        int mult = 1;
        if (i < len && abbr.charAt(i) == '*') {
          i++;
          int ns = i;
          while (i < len && Character.isDigit(abbr.charAt(i))) i++;
          if (i > ns) mult = Integer.parseInt(abbr.substring(ns, i));
        }
        String groupExpanded = parseEmmet(groupAbbr);
        if (groupExpanded == null) return null;
        for (int g = 0; g < mult; g++) {
          Node gn = new Node("__group__");
          gn.textContent = groupExpanded.replace("$", String.valueOf(g + 1));
          if (op == '>' && current != null) current.addChild(gn);
          else if (op == '+' && current != null && current.parent != null)
            current.parent.addChild(gn);
          else roots.add(gn);
          current = gn;
        }
        continue;
      }
      if (op == '^') {
        if (current != null && current.parent != null) {
          current = current.parent;
          if (current.parent != null) current = current.parent;
        }
        if (i < len && abbr.charAt(i) != '>' && abbr.charAt(i) != '+' && abbr.charAt(i) != '^') {
          /* parse next */
        } else continue;
      }
      int tokenStart = i;
      i = extractToken(abbr, i);
      if (i == tokenStart) return null;
      String token = abbr.substring(tokenStart, i);
      Node[] nodes = parseNode(token);
      if (nodes == null) return null;
      if (op == '>' && current != null) {
        for (Node n : nodes) current.addChild(n);
        current = nodes[nodes.length - 1];
      } else if (op == '+' && current != null) {
        Node parent = current.parent;
        if (parent != null) for (Node n : nodes) parent.addChild(n);
        else Collections.addAll(roots, nodes);
        current = nodes[nodes.length - 1];
      } else {
        Collections.addAll(roots, nodes);
        current = nodes[nodes.length - 1];
      }
    }
    if (roots.isEmpty()) return null;
    StringBuilder sb = new StringBuilder();
    for (int r = 0; r < roots.size(); r++) renderNode(roots.get(r), sb, 0, r == roots.size() - 1);
    String result = sb.toString();
    if (!result.contains("|")) {
      int fc = result.indexOf("></");
      result =
          fc != -1 ? result.substring(0, fc + 1) + "|" + result.substring(fc + 1) : result + "|";
    }
    return result;
  }

  private static int extractToken(String abbr, int start) {
    int i = start, len = abbr.length();
    while (i < len) {
      char c = abbr.charAt(i);
      if (c == '>' || c == '+' || c == '^' || c == '(') break;
      if (c == '[') {
        int cl = abbr.indexOf(']', i);
        if (cl < 0) return i;
        i = cl + 1;
      } else if (c == '{') {
        int cl = abbr.indexOf('}', i);
        if (cl < 0) return i;
        i = cl + 1;
      } else i++;
    }
    return i;
  }

  private static int findMatchingParen(String s, int openIdx) {
    int depth = 0;
    for (int i = openIdx; i < s.length(); i++) {
      if (s.charAt(i) == '(') depth++;
      else if (s.charAt(i) == ')') {
        depth--;
        if (depth == 0) return i;
      }
    }
    return -1;
  }

  private static Node[] parseNode(String str) {
    Matcher m = PAT_EMMET_PARSE.matcher(str);
    if (!m.matches()) return null;
    String tag = m.group(1), idStr = m.group(2), classesStr = m.group(3);
    String attrStr = m.group(4), textContent = m.group(5), multStr = m.group(6);
    if ((tag == null || tag.isEmpty())
        && (idStr != null || (classesStr != null && !classesStr.isEmpty()))) tag = "div";
    if (tag == null || tag.isEmpty()) return null;
    String id = (idStr != null && idStr.length() > 1) ? idStr.substring(1) : null;
    List<String> classes = new ArrayList<>();
    if (classesStr != null && !classesStr.isEmpty())
      for (String cl : classesStr.substring(1).split("\\.")) if (!cl.isEmpty()) classes.add(cl);
    int mult = 1;
    if (multStr != null && !multStr.isEmpty()) {
      mult = Integer.parseInt(multStr);
      if (mult > 100) mult = 100;
    }
    Node[] nodes = new Node[mult];
    for (int i = 0; i < mult; i++) {
      Node n = new Node(tag);
      n.id = id;
      if (id != null && mult > 1) n.id = id + (i + 1);
      n.classes = new ArrayList<>(classes);
      if (mult > 1)
        for (int j = 0; j < n.classes.size(); j++)
          n.classes.set(j, n.classes.get(j).replace("$", String.valueOf(i + 1)));
      if (attrStr != null) n.attributes = attrStr;
      if (textContent != null)
        n.textContent = mult > 1 ? textContent.replace("$", String.valueOf(i + 1)) : textContent;
      nodes[i] = n;
    }
    return nodes;
  }

  private static void renderNode(Node node, StringBuilder sb, int indent, boolean isLast) {
    String ind = "    ".repeat(indent);
    if ("__group__".equals(node.tag)) {
      if (node.textContent != null) {
        String[] lines = node.textContent.split("\n");
        for (int i = 0; i < lines.length; i++) {
          sb.append(ind).append(lines[i]);
          if (i < lines.length - 1 || !isLast) sb.append("\n");
        }
      }
      return;
    }
    sb.append(ind).append("<").append(node.tag);
    if (node.id != null) sb.append(" id=\"").append(node.id).append("\"");
    if (node.classes != null && !node.classes.isEmpty()) {
      sb.append(" class=\"");
      for (int i = 0; i < node.classes.size(); i++) {
        sb.append(node.classes.get(i));
        if (i < node.classes.size() - 1) sb.append(" ");
      }
      sb.append("\"");
    }
    if (node.attributes != null) sb.append(" ").append(node.attributes);
    boolean isVoid = isVoidElement(node.tag);
    if (isVoid) {
      sb.append(">");
    } else {
      sb.append(">");
      if (node.textContent != null)
        sb.append(node.textContent).append("</").append(node.tag).append(">");
      else if (node.children.isEmpty()) sb.append("</").append(node.tag).append(">");
      else {
        sb.append("\n");
        for (int i = 0; i < node.children.size(); i++)
          renderNode(node.children.get(i), sb, indent + 1, i == node.children.size() - 1);
        sb.append(ind).append("</").append(node.tag).append(">");
      }
    }
    if (!isLast) sb.append("\n");
  }

  private static boolean isVoidElement(String tag) {
    switch (tag) {
      case "img":
      case "input":
      case "br":
      case "hr":
      case "meta":
      case "link":
      case "area":
      case "base":
      case "col":
      case "embed":
      case "source":
      case "track":
      case "wbr":
        return true;
    }
    return false;
  }

  static class Node {
    String tag, id, attributes, textContent;
    List<String> classes;
    List<Node> children = new ArrayList<>();
    Node parent;

    Node(String t) {
      tag = t;
    }

    void addChild(Node c) {
      c.parent = this;
      children.add(c);
    }
  }
}
