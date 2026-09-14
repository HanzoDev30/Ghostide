package ir.hanzodev1375.ghostide.codeeditors.lspcustomhot;

import android.content.Context;
import io.github.rosemoe.sora.lang.completion.CompletionItemKind;
import ir.hanzodev1375.ghostide.codeeditors.colorrender.model.colorrepo.ColorNameRepository;
import ir.hanzodev1375.ghostide.codeeditors.lspcustomhot.model.CssSelect;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Css3Server {

  private Random RANDOM = new Random();
  private int COLORS_TO_GENERATE = 20;
  private final ColorNameRepository colorRepo;

  private String[] colorsCss = {
    "aliceblue",
    "antiquewhite",
    "aqua",
    "aquamarine",
    "azure",
    "beige",
    "bisque",
    "black",
    "blanchedalmond",
    "blue",
    "blueviolet",
    "brown",
    "burlywood",
    "cadetblue",
    "chartreuse",
    "chocolate",
    "coral",
    "cornflowerblue",
    "cornsilk",
    "crimson",
    "cyan",
    "darkblue",
    "darkcyan",
    "darkgoldenrod",
    "darkgray",
    "darkgreen",
    "darkkhaki",
    "darkmagenta",
    "darkolivegreen",
    "darkorange",
    "darkorchid",
    "darkred",
    "darksalmon",
    "darkseagreen",
    "darkslateblue",
    "darkslategray",
    "darkturquoise",
    "darkviolet",
    "deeppink",
    "deepskyblue",
    "dimgray",
    "dodgerblue",
    "firebrick",
    "floralwhite",
    "forestgreen",
    "fuchsia",
    "gainsboro",
    "ghostwhite",
    "gold",
    "goldenrod",
    "gray",
    "green",
    "greenyellow",
    "honeydew",
    "hotpink",
    "indianred",
    "indigo",
    "ivory",
    "khaki",
    "lavender",
    "lavenderblush",
    "lawngreen",
    "lemonchiffon",
    "lightblue",
    "lightcoral",
    "lightcyan",
    "lightgoldenrodyellow",
    "lightgray",
    "lightgreen",
    "lightpink",
    "lightsalmon",
    "lightseagreen",
    "lightskyblue",
    "lightslategray",
    "lightsteelblue",
    "lightyellow",
    "lime",
    "limegreen",
    "linen",
    "magenta",
    "maroon",
    "mediumaquamarine",
    "mediumblue",
    "mediumorchid",
    "mediumpurple",
    "mediumseagreen",
    "mediumslateblue",
    "mediumspringgreen",
    "mediumturquoise",
    "mediumvioletred",
    "midnightblue",
    "mintcream",
    "mistyrose",
    "moccasin",
    "navajowhite",
    "navy",
    "oldlace",
    "olive",
    "olivedrab",
    "orange",
    "orangered",
    "orchid",
    "palegoldenrod",
    "palegreen",
    "paleturquoise",
    "palevioletred",
    "papayawhip",
    "peachpuff",
    "peru",
    "pink",
    "plum",
    "powderblue",
    "purple",
    "rebeccapurple",
    "red",
    "rosybrown",
    "royalblue",
    "saddlebrown",
    "salmon",
    "sandybrown",
    "seagreen",
    "seashell",
    "sienna",
    "silver",
    "skyblue",
    "slateblue",
    "slategray",
    "snow",
    "springgreen",
    "steelblue",
    "tan",
    "teal",
    "thistle",
    "tomato",
    "turquoise",
    "violet",
    "wheat",
    "white",
    "whitesmoke",
    "yellow",
    "yellowgreen"
  };

  private String[] cssUnits = {
    "px", "em", "rem", "%", "vw", "vh", "vmin", "vmax", "in", "cm", "mm", "pt", "pc", "ch", "ex",
    "fr", "lh", "rlh"
  };

  private String[] types = {
    "text",
    "password",
    "email",
    "number",
    "tel",
    "url",
    "date",
    "time",
    "datetime-local",
    "color",
    "range",
    "checkbox",
    "radio",
    "file",
    "hidden",
    "search"
  };

  private String[] textAlignValues = {"left", "right", "center", "justify", "start", "end"};

  private KeyWordConst keyWordConst = new KeyWordConst();

  public Css3Server(Context context) {
    this.colorRepo = ColorNameRepository.getInstance(context);
  }

  /** برای backward compat — بدون context، color repo نداریم */
  public Css3Server() {
    this.colorRepo = null;
  }

  public List<CustomCompletionItem> getCompletions(String prefix) {
    if (prefix == null) prefix = "";
    List<CustomCompletionItem> list = new ArrayList<>();
    list.addAll(getCssColors(prefix));
    list.addAll(getTypeValues(prefix));
    list.addAll(keyWordConst.getCssFont(prefix));
    list.addAll(getMarginPadding(prefix, "margin"));
    list.addAll(getMarginPadding(prefix, "padding"));
    list.addAll(getBorder(prefix));
    list.addAll(getLayoutProperties(prefix));
    list.addAll(getCssFunctions(prefix));
    list.addAll(getRandomColors(prefix));
    list.addAll(getPseudoSelectors(prefix));
    return list;
  }

  private List<CustomCompletionItem> getPseudoSelectors(String prefix) {
    List<CustomCompletionItem> list = new ArrayList<>();

    // شبه‌المان‌ها (::)
    int doubleColon = prefix.lastIndexOf("::");
    if (doubleColon != -1) {
      String toReplace = prefix.substring(doubleColon); // شامل :: و هر کاراکتر بعد از آن
      String search = toReplace.substring(2).toLowerCase(); // حروف بعد از ::
      for (CssSelect sel : PseudoData.getPseudoElements()) {
        if (sel.getName().startsWith(search)) {
          String insertText = "::" + sel.getName();
          // کل toReplace (یعنی :: و هر چه تایپ شده) حذف و insertText جایگزین می‌شود
          list.add(
              new CustomCompletionItem(
                  sel.getName(), sel.getMarkdownDoc(), insertText, -1, toReplace));
        }
      }
      return list;
    }

    // شبه‌کلاس‌ها (:)
    int singleColon = prefix.lastIndexOf(':');
    if (singleColon != -1) {
      String toReplace = prefix.substring(singleColon); // شامل : و هر کاراکتر بعد از آن
      String search = toReplace.substring(1).toLowerCase(); // حروف بعد از :
      for (CssSelect sel : PseudoData.getPseudoClasses()) {
        if (sel.getName().startsWith(search)) {
          String insertText = ":" + sel.getName();
          list.add(
              new CustomCompletionItem(
                  sel.getName(), sel.getMarkdownDoc(), insertText, -1, toReplace));
        }
      }
    }
    return list;
  }

  private List<CustomCompletionItem> getCssColors(String prefix) {
    List<CustomCompletionItem> list = new ArrayList<>();
    String propertyName = detectColorProperty(prefix);
    if (!propertyName.isEmpty()) {
      String colorPrefix = extractValueAfterProperty(prefix, propertyName);
      for (String color : colorsCss) {
        if (color.startsWith(colorPrefix)) {
          String insertText = color + ";";
          // hex رو از repo بگیر برای desc
          String hexDesc = colorRepo != null ? colorRepo.getHexForName(color) : "CSS Color";
          if (hexDesc == null) hexDesc = "CSS Color";
          CustomCompletionItem item =
              new CustomCompletionItem(color, hexDesc, insertText, -1, colorPrefix);
          item.kind(CompletionItemKind.Color);
          list.add(item);
        }
      }
    }
    return list;
  }

  private String detectColorProperty(String prefix) {
    if (prefix.contains("color:") || prefix.contains("color: ")) return "color";
    if (prefix.startsWith("background-color:")) return "background-color";
    if (prefix.startsWith("border-color:")) return "border-color";
    if (prefix.startsWith("outline-color:")) return "outline-color";
    if (prefix.startsWith("text-decoration-color:")) return "text-decoration-color";
    if (prefix.startsWith("column-rule-color:")) return "column-rule-color";
    if (prefix.startsWith("caret-color:")) return "caret-color";
    if (prefix.startsWith("border-top-color:")) return "border-top-color";
    if (prefix.startsWith("border-right-color:")) return "border-right-color";
    if (prefix.startsWith("border-bottom-color:")) return "border-bottom-color";
    if (prefix.startsWith("border-left-color:")) return "border-left-color";
    return "";
  }

  private String extractValueAfterProperty(String prefix, String property) {
    int idx = prefix.indexOf(property + ":");
    if (idx == -1) return "";
    String after = prefix.substring(idx + property.length() + 1);
    return after.trim();
  }

  private List<CustomCompletionItem> getTypeValues(String prefix) {
    List<CustomCompletionItem> list = new ArrayList<>();
    if (prefix.startsWith("type=") || prefix.startsWith("type")) {
      String typePrefix =
          prefix.startsWith("type=")
              ? prefix.substring("type=".length())
              : prefix.substring("type".length());
      for (String type : types) {
        if (type.startsWith(typePrefix)) {
          String insertText = "\"" + type + "\"";
          list.add(new CustomCompletionItem(type, "Input type", insertText, -1, typePrefix));
        }
      }
      return list;
    }
    if (prefix.startsWith("text-align:")) {
      String valPrefix = prefix.substring("text-align:".length()).trim();
      for (String val : textAlignValues) {
        if (val.startsWith(valPrefix)) {
          String insertText = val + ";";
          list.add(new CustomCompletionItem(val, "Text align", insertText, -1, valPrefix));
        }
      }
    }
    return list;
  }

  private List<CustomCompletionItem> getMarginPadding(String prefix, String propName) {
    List<CustomCompletionItem> list = new ArrayList<>();
    String[] variants = {
      propName + ":",
      propName + "-top:",
      propName + "-right:",
      propName + "-bottom:",
      propName + "-left:"
    };
    for (String prop : variants) {
      if (prefix.startsWith(prop)) {
        String valuePart = prefix.substring(prop.length());
        Matcher m = Pattern.compile("^(\\d+(?:\\.\\d+)?)([a-z%]*)").matcher(valuePart);
        if (m.matches()) {
          String number = m.group(1);
          String partialUnit = m.group(2);
          boolean isCompleteUnit = false;
          for (String unit : cssUnits) {
            if (partialUnit.equals(unit)) {
              isCompleteUnit = true;
              break;
            }
          }
          if (!isCompleteUnit) {
            for (String unit : cssUnits) {
              if (unit.startsWith(partialUnit)) {
                String insertText = number + unit + ";";
                list.add(
                    new CustomCompletionItem(
                            number + unit, propName + " value", insertText, -1, valuePart)
                        .kind(CompletionItemKind.Unit));
              }
            }
          }
        } else if (valuePart.isEmpty() || valuePart.matches("\\s*")) {
          for (String unit : cssUnits) {
            String insertText = "0" + unit + ";";
            list.add(new CustomCompletionItem(insertText, propName + " zero"));
          }
          list.add(
              new CustomCompletionItem("auto;", propName + " auto").kind(CompletionItemKind.Unit));
        }
        break;
      }
    }
    return list;
  }

  private List<CustomCompletionItem> getBorder(String prefix) {
    List<CustomCompletionItem> list = new ArrayList<>();
    String[] borderProps = {
      "border:",
      "border-width:",
      "border-style:",
      "border-color:",
      "border-top:",
      "border-right:",
      "border-bottom:",
      "border-left:"
    };
    String[] borderStyles = {
      "solid", "dashed", "dotted", "double", "groove", "ridge", "inset", "outset", "none", "hidden"
    };
    for (String prop : borderProps) {
      if (prefix.startsWith(prop)) {
        String valuePart = prefix.substring(prop.length()).trim();
        if (prop.endsWith("color:")) {
          for (String color : colorsCss) {
            if (color.startsWith(valuePart)) {
              String insertText = color + ";";
              String hexDesc = colorRepo != null ? colorRepo.getHexForName(color) : "Border color";
              if (hexDesc == null) hexDesc = "Border color";
              CustomCompletionItem item =
                  new CustomCompletionItem(color, hexDesc, insertText, -1, valuePart);
              item.kind(CompletionItemKind.Color);
              list.add(item);
            }
          }
        } else if (prop.endsWith("style:")) {
          for (String style : borderStyles) {
            if (style.startsWith(valuePart)) {
              String insertText = style + ";";
              list.add(
                  new CustomCompletionItem(insertText, "Border style", insertText, -1, valuePart));
            }
          }
        } else if (prop.endsWith("width:")) {
          Matcher m = Pattern.compile("^(\\d+(?:\\.\\d+)?)([a-z%]*)").matcher(valuePart);
          if (m.matches()) {
            String number = m.group(1);
            String partialUnit = m.group(2);
            boolean isComplete = false;
            for (String unit : cssUnits) if (partialUnit.equals(unit)) isComplete = true;
            if (!isComplete) {
              for (String unit : cssUnits) {
                if (unit.startsWith(partialUnit)) {
                  String insertText = number + unit + ";";
                  list.add(
                      new CustomCompletionItem(
                              insertText, "Border width", insertText, -1, valuePart)
                          .kind(CompletionItemKind.Method));
                }
              }
            }
          } else {
            for (String unit : cssUnits) {
              String insertText = "1" + unit + ";";
              list.add(new CustomCompletionItem(insertText, "Border width"));
            }
            list.add(new CustomCompletionItem("thin;", "Border width"));
            list.add(new CustomCompletionItem("medium;", "Border width"));
            list.add(new CustomCompletionItem("thick;", "Border width"));
          }
        } else {
          String insertText = "1px solid black;";
          list.add(new CustomCompletionItem(insertText, "Border"));
          list.add(new CustomCompletionItem("none;", "Border"));
        }
        break;
      }
    }
    return list;
  }

  private List<CustomCompletionItem> getLayoutProperties(String prefix) {
    List<CustomCompletionItem> list = new ArrayList<>();
    if (prefix.startsWith("display:")) {
      String[] displays = {
        "block",
        "inline",
        "inline-block",
        "flex",
        "inline-flex",
        "grid",
        "inline-grid",
        "none",
        "table",
        "list-item"
      };
      String valPrefix = prefix.substring("display:".length()).trim();
      for (String d : displays) {
        if (d.startsWith(valPrefix)) {
          String insertText = d + ";";
          list.add(
              new CustomCompletionItem(insertText, "Display", insertText, -1, valPrefix)
                  .kind(CompletionItemKind.Variable));
        }
      }
    } else if (prefix.startsWith("position:")) {
      String[] positions = {"static", "relative", "absolute", "fixed", "sticky"};
      String valPrefix = prefix.substring("position:".length()).trim();
      for (String p : positions) {
        if (p.startsWith(valPrefix)) {
          String insertText = p + ";";
          list.add(
              new CustomCompletionItem(insertText, "Position", insertText, -1, valPrefix)
                  .kind(CompletionItemKind.Reference));
        }
      }
    } else if (prefix.startsWith("flex-direction:")) {
      String[] dirs = {"row", "row-reverse", "column", "column-reverse"};
      String valPrefix = prefix.substring("flex-direction:".length()).trim();
      for (String d : dirs) {
        if (d.startsWith(valPrefix)) {
          String insertText = d + ";";
          list.add(
              new CustomCompletionItem(insertText, "Flex direction", insertText, -1, valPrefix)
                  .kind(CompletionItemKind.Struct));
        }
      }
    } else if (prefix.startsWith("justify-content:")) {
      String[] vals = {
        "flex-start", "flex-end", "center", "space-between", "space-around", "space-evenly"
      };
      String valPrefix = prefix.substring("justify-content:".length()).trim();
      for (String v : vals) {
        if (v.startsWith(valPrefix)) {
          String insertText = v + ";";
          list.add(
              new CustomCompletionItem(insertText, "Justify content", insertText, -1, valPrefix)
                  .kind(CompletionItemKind.Constant));
        }
      }
    } else if (prefix.startsWith("align-items:")) {
      String[] vals = {"stretch", "flex-start", "flex-end", "center", "baseline"};
      String valPrefix = prefix.substring("align-items:".length()).trim();
      for (String v : vals) {
        if (v.startsWith(valPrefix)) {
          String insertText = v + ";";
          list.add(
              new CustomCompletionItem(insertText, "Align items", insertText, -1, valPrefix)
                  .kind(CompletionItemKind.Variable));
        }
      }
    } else if (prefix.startsWith("grid-template-columns:")) {
      list.add(new CustomCompletionItem("repeat(3, 1fr);", "CSS Grid"));
      list.add(new CustomCompletionItem("1fr 2fr 1fr;", "CSS Grid"));
    } else if (prefix.startsWith("grid-template-rows:")) {
      list.add(new CustomCompletionItem("auto 1fr auto;", "CSS Grid"));
    }
    return list;
  }

  private List<CustomCompletionItem> getCssFunctions(String prefix) {
    List<CustomCompletionItem> list = new ArrayList<>();
    if (prefix.contains("rgb(")) {
      if (prefix.endsWith("rgb(")) list.add(new CustomCompletionItem("0,0,0);", "RGB function"));
    }
    if (prefix.contains("rgba(")) {
      if (prefix.endsWith("rgba("))
        list.add(new CustomCompletionItem("0,0,0,0.5);", "RGBA function"));
    }
    if (prefix.contains("var(")) {
      if (prefix.endsWith("var("))
        list.add(new CustomCompletionItem("--my-color);", "CSS variable"));
    }
    if (prefix.contains("calc(") && prefix.endsWith("calc(")) {
      list.add(new CustomCompletionItem("100% - 20px);", "calc function"));
    }
    return list;
  }

  private List<CustomCompletionItem> getRandomColors(String prefix) {
    List<CustomCompletionItem> list = new ArrayList<>();
    if (prefix.endsWith("#f")) {
      List<String> fake = generateRandomColors(COLORS_TO_GENERATE);
      for (String color : fake) {
        CustomCompletionItem item =
            new CustomCompletionItem(color, color.toUpperCase(), color + ";", -1, "#f");
        item.kind(CompletionItemKind.Color);
        list.add(item);
      }
    }
    return list;
  }

  private List<String> generateRandomColors(int count) {
    List<String> colors = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      colors.add(String.format("#%06X", RANDOM.nextInt(0xFFFFFF + 1)));
    }
    return colors;
  }
}
