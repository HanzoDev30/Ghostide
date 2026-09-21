package ir.hanzodev1375.ghostide.codeeditors.colorscheme;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel;
import ir.theme.EditorTheme;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.eclipse.tm4e.core.registry.IThemeSource;

/** ساخت تم TextMate فقط از روی تم شخصی (GhostTheme) بدون base theme. */
public final class GhostTextMateTheme {

  private GhostTextMateTheme() {}

  /** json ی token های تم شخصی را به صورت byte برمی گرداند. */
  public static byte[] tokenColorsJson(EditorTheme theme) {
    JsonObject root = new JsonObject();
    root.addProperty("name", "ghost");
    JsonArray tokenColors = new JsonArray();
    addRule(tokenColors, "", theme.getTextNormal());
    addRule(tokenColors, "comment", theme.getComment());
    addRule(tokenColors, "string, constant, number", theme.getLiteral());
    addRule(tokenColors, "keyword.operator, punctuation.definition.operator", theme.getOperator());
    addRule(
        tokenColors,
        "keyword, keyword.control, storage.type, storage.modifier",
        theme.getKeyword());
    addRule(tokenColors, "variable, variable.other, variable.parameter", theme.getIdentifierVar());
    addRule(
        tokenColors,
        "entity.name.type, entity.other.inherited-class, support.type, support.class",
        theme.getIdentifierName());
    addRule(
        tokenColors,
        "entity.name.function, support.function, meta.function-call",
        theme.getFunctionName());
    addRule(
        tokenColors, "annotation, meta.annotation, storage.type.annotation", theme.getAnnotation());
    addRule(
        tokenColors, "meta.tag, entity.name.tag, punctuation.definition.tag", theme.getHtmlTag());
    addRule(tokenColors, "entity.other.attribute-name", theme.getAttributeName());
    addRule(
        tokenColors,
        "meta.attribute.value string, string.quoted.double, string.quoted.single",
        theme.getAttributeValue());
    root.add("tokenColors", tokenColors);
    // نام یکتا بر اساس محتوا تا تم های مختلف در ThemeRegistry با هم تداخل نکنند.
    String name = "ghost-" + Integer.toHexString(root.toString().hashCode());
    root.addProperty("name", name);
    return root.toString().getBytes(StandardCharsets.UTF_8);
  }

  /** token رنگ ها را به ThemeModel تبدیل می کند. */
  public static ThemeModel build(EditorTheme theme, boolean dark) {
    return build(theme, tokenColorsJson(theme), dark);
  }

  public static ThemeModel build(EditorTheme theme, byte[] json, boolean dark) {
    String name = readName(json);
    ThemeModel model =
        new ThemeModel(
            IThemeSource.fromInputStream(
                new ByteArrayInputStream(json), name + ".json", null),
            name);
    model.setDark(dark);
    return model;
  }

  private static String readName(byte[] json) {
    try {
      return JsonParser.parseString(new String(json, StandardCharsets.UTF_8))
          .getAsJsonObject()
          .get("name")
          .getAsString();
    } catch (Exception e) {
      return "ghost";
    }
  }

  private static void addRule(JsonArray tokenColors, String scope, String color) {
    if (color == null || color.isEmpty()) {
      return;
    }
    JsonObject rule = new JsonObject();
    rule.addProperty("scope", scope);
    JsonObject settings = new JsonObject();
    settings.addProperty("foreground", color);
    rule.add("settings", settings);
    tokenColors.add(rule);
  }
}
