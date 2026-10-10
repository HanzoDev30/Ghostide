package ir.hanzodev1375.ghostide.codeeditors.colorscheme;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel;
import ir.theme.EditorTheme;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.eclipse.tm4e.core.registry.IThemeSource;

/**
 * ساخت تم TextMate فقط از روی تم شخصی (GhostTheme) بدون base theme.
 *
 * <p>ساختار JSON مطابق قالب VS Code است: کلید ریشه {@code tokenColors} برای موتور اسکوپ
 * (Theme.parseTheme) و کلید {@code colors} برای {@code TextMateColorScheme.applyVSCTheme} تا همه‌ی
 * رنگ‌های خوانده‌شده از آن فایل (پس‌زمینه، selection، خط جاری، شماره خط، راهنمای تورفتگی،
 * پیشنهادها، tooltip، find-match و ...) اعمال شوند.
 *
 * <p>قاعده ها قبل از نوشتن بر اساس عمق اسکوپ مرتب می شوند (والد قبل از فرزند). چون موتور tm4e رنگ
 * والد را هنگام ساختن node فرزند clone می کند، اگر فرزند زودتر ثبت شود رنگش پیش‌فرض می ماند و
 * توکن‌هایی مثل {@code punctuation.section.begin} بی‌رنگ می‌شوند.
 */
public final class GhostTextMateTheme {

  private GhostTextMateTheme() {}

  /** json ی token های تم شخصی را به صورت byte برمی گرداند. */
  public static byte[] tokenColorsJson(EditorTheme theme) {
    JsonObject root = new JsonObject();
    root.addProperty("name", "ghost");
    JsonArray tokenColors = new JsonArray();

    tokenColors.add(globalEntry(theme));

    List<Rule> rules = new ArrayList<>();
    add(
        rules,
        theme.getComment(),
        "comment, comment.line, comment.block, comment.documentation, comment.block.documentation,"
            + " comment.block.preprocessor, comment.line.number-sign, punctuation.definition.comment,"
            + " punctuation.definition.block.documentation");

    add(
        rules,
        theme.getLiteral(),
        "string, string.quoted, string.template, string.unquoted, string.other, string.interpolated,"
            + " string.escape, string.escape.backslash, constant, constant.numeric,"
            + " constant.numeric.integer, constant.numeric.float, constant.numeric.hex,"
            + " constant.numeric.octal, constant.character, constant.character.escape,"
            + " constant.character.entity, constant.other, constant.other.symbol, constant.other.key,"
            + " constant.language, support.constant, variable.language, variable.anonymous,"
            + " variable.self, punctuation.definition.string, punctuation.definition.string.begin,"
            + " punctuation.definition.string.end, punctuation.definition.escape,"
            + " punctuation.definition.character-class.regexp, punctuation.definition.group.regexp,"
            + " meta.property-value, support.constant.property-value, property-list.property-value,"
            + " entity.other.keyframe-offset, entity.other.keyframe-offset.percentage,"
            + " support.unicode-range, markup.raw, markup.inline.raw, markup.inserted,"
            + " markup.inserted.diff");

    add(
        rules,
        theme.getKeyword(),
        "keyword, keyword.control, keyword.other, keyword.other.unit, keyword.other.important,"
            + " keyword.operator.new, keyword.operator.delete, keyword.operator.static,"
            + " keyword.operator.this, keyword.operator.expression, keyword.operator.word,"
            + " keyword.operator.type, storage, storage.type, storage.modifier, storage.arrow,"
            + " support.keyword, string.regexp, markup.heading, markup.list, markup.link,"
            + " markup.underline.link,"
            + " meta.link, meta.image, entity.name.section, punctuation.definition.heading,"
            + " punctuation.section.embedded, punctuation.definition.template-expression");

    add(
        rules,
        theme.getOperator(),
        "keyword.operator, keyword.operator.logical, keyword.operator.relational,"
            + " keyword.operator.assignment, keyword.operator.comparison, keyword.operator.ternary,"
            + " keyword.operator.arithmetic, keyword.operator.spread, keyword.operator.increment,"
            + " keyword.operator.decrement, keyword.operator.bitwise, keyword.operator.accessor,"
            + " keyword.operator.concatenation, punctuation, punctuation.definition,"
            + " punctuation.definition.operator, punctuation.separator, punctuation.terminator,"
            + " punctuation.accessor, punctuation.definition.parameters,"
            + " punctuation.definition.array, punctuation.definition.block,"
            + " punctuation.definition.group, punctuation.definition.entity,"
            + " punctuation.definition.keyword, punctuation.definition.typeparameters,"
            + " punctuation.definition.comma, punctuation.definition.condition");

    add(
        rules,
        theme.getIdentifierVar(),
        "variable, variable.other, variable.parameter, variable.other.readwrite,"
            + " variable.other.constant, variable.other.property, variable.property,"
            + " variable.other.object.property, variable.object.property, variable.function,"
            + " support.variable, support.variable.property, support.other.variable,"
            + " meta.object-literal.key, operator.rest.parameters, variable.parameter.function");

    add(
        rules,
        theme.getIdentifierName(),
        "entity.name, entity.name.type, entity.name.class, entity.name.struct, entity.name.enum,"
            + " entity.name.interface, entity.name.module, entity.name.namespace,"
            + " entity.name.scope-resolution, entity.name.package, entity.other.inherited-class,"
            + " entity.other.namespace-prefix, entity.other.counter-name,"
            + " entity.other.counter-style-name, support.type, support.class,"
            + " support.type.primitive, support.type.property-name, meta.type.annotation,"
            + " meta.type.parameters, meta.return.type, meta.interface, type-declaration,"
            + " enum-declaration, quote.module");

    add(
        rules,
        theme.getFunctionName(),
        "entity.name.function, support.function, meta.function-call, meta.function-call.generic,"
            + " function-declaration, method-declaration, method-overload-declaration");

    add(
        rules,
        theme.getAnnotation(),
        "annotation, meta.annotation, meta.decorator, storage.type.annotation,"
            + " punctuation.definition.annotation, punctuation.definition.decorator,"
            + " variable.annotation, entity.name.function.decorator, entity.name.type.annotation,"
            + " support.type.annotation");

    add(
        rules,
        theme.getHtmlTag(),
        "meta.tag, entity.name.tag, entity.name.tag.block, entity.name.tag.inline,"
            + " entity.name.tag.structure, punctuation.definition.tag,"
            + " punctuation.definition.tag.begin, punctuation.definition.tag.end,"
            + " punctuation.definition.tag.html, support.function.html, meta.tag.sgml");

    add(
        rules,
        theme.getAttributeName(),
        "entity.other.attribute-name, entity.other.attribute-name.class,"
            + " entity.other.attribute-name.id, entity.other.attribute-name.pseudo-class,"
            + " entity.other.attribute-name.pseudo-element");

    add(
        rules,
        theme.getAttributeValue(),
        "meta.attribute.value string, meta.attribute.with-value string, string.quoted.double.html,"
            + " string.quoted.single.html, string.quoted.double.xml, string.quoted.single.xml,"
            + " support.attribute-value");

    add(
        rules,
        theme.getProblemError(),
        "invalid, invalid.illegal, invalid.deprecated, invalid.broken, markup.deleted,"
            + " markup.deleted.diff");

    add(rules, theme.getProblemWarning(), "markup.changed, markup.changed.diff");

    addFont(rules, "markup.bold", "bold");
    addFont(rules, "markup.italic", "italic");
    addFont(rules, "markup.underline", "underline");
    addFont(rules, "markup.strikethrough", "strikethrough");

    // والد قبل از فرزند؛ وگرنه رنگ clone شده پیش‌فرض می ماند (باگ رنگی نشدن punctuation).
    rules.sort(Comparator.comparingInt(rule -> rule.depth));
    for (Rule rule : rules) {
      tokenColors.add(rule.toJson());
    }

    root.add("tokenColors", tokenColors);
    root.add("colors", vscColors(theme));
    // نام یکتا بر اساس محتوا تا تم های مختلف در ThemeRegistry با هم تداخل نکنند.
    String name = "ghost-" + Integer.toHexString(root.toString().hashCode());
    root.addProperty("name", name);
    return root.toString().getBytes(StandardCharsets.UTF_8);
  }

  /** ورودی اولِ tokenColors؛ بدون scope تا رنگ پیش‌فرض و رنگ‌های سراسری TextMate را بدهد. */
  private static JsonObject globalEntry(EditorTheme theme) {
    JsonObject entry = new JsonObject();
    JsonObject settings = new JsonObject();
    put(settings, "foreground", theme.getTextNormal());
    put(settings, "background", theme.getWholeBackground());
    put(settings, "lineHighlight", theme.getCurrentLine());
    put(settings, "selection", theme.getSelectedTextBackground());
    put(settings, "caret", theme.getSelectionInsert());
    put(settings, "invisibles", theme.getNonPrintableChar());
    put(settings, "highlightedDelimitersForeground", theme.getHighlightedDelimitersForeground());
    put(settings, "completionWindowBackground", theme.getCompletionWndBackground());
    entry.add("settings", settings);
    return entry;
  }

  /** کلیدهای colors که TextMateColorScheme.applyVSCTheme می خواند. */
  private static JsonObject vscColors(EditorTheme t) {
    JsonObject c = new JsonObject();
    put(c, "editor.background", t.getWholeBackground());
    put(c, "editor.foreground", t.getTextNormal());
    put(c, "editorCursor.foreground", t.getSelectionInsert());
    put(c, "editor.selectionBackground", t.getSelectedTextBackground());
    put(c, "editorWhitespace.foreground", t.getNonPrintableChar());
    put(c, "editor.lineHighlightBackground", t.getCurrentLine());
    put(c, "editorLineNumber.foreground", t.getLineNumber());
    put(c, "editorLineNumber.activeForeground", t.getLineNumberCurrent());
    put(c, "highlightedDelimitersForeground", t.getHighlightedDelimitersForeground());
    put(c, "editorIndentGuide.background", t.getBlockLine());
    put(c, "editorIndentGuide.activeBackground", t.getBlockLineCurrent());
    put(c, "editorSuggestWidget.background", t.getCompletionWndBackground());
    put(c, "editorSuggestWidget.foreground", t.getCompletionWndTextPrimary());
    put(c, "editorSuggestWidget.selectedBackground", t.getCompletionWndItemCurrent());
    put(c, "editorSuggestWidget.highlightForeground", t.getCompletionWndTextMatched());
    put(c, "editor.wordHighlightBackground", t.getTextHighlightBackground());
    put(c, "editor.wordHighlightStrongBackground", t.getTextHighlightStrongBackground());
    put(c, "editor.findMatchBackground", t.getMatchedTextBackground());
    put(c, "tooltipBackground", t.getDiagnosticTooltipBackground());
    put(c, "tooltipBriefMessageColor", t.getDiagnosticTooltipBriefMsg());
    put(c, "tooltipDetailedMessageColor", t.getDiagnosticTooltipDetailedMsg());
    put(c, "tooltipActionColor", t.getDiagnosticTooltipAction());
    return c;
  }

  /** token رنگ ها را به ThemeModel تبدیل می کند. */
  public static ThemeModel build(EditorTheme theme, boolean dark) {
    return build(theme, tokenColorsJson(theme), dark);
  }

  public static ThemeModel build(EditorTheme theme, byte[] json, boolean dark) {
    String name = readName(json);
    ThemeModel model =
        new ThemeModel(
            IThemeSource.fromInputStream(new ByteArrayInputStream(json), name + ".json", null),
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

  private static void add(List<Rule> rules, String color, String scopes) {
    if (color == null || color.isEmpty()) {
      return;
    }
    for (String scope : scopes.split(",")) {
      scope = scope.trim();
      if (!scope.isEmpty()) {
        rules.add(new Rule(scope, color, null));
      }
    }
  }

  private static void addFont(List<Rule> rules, String scope, String fontStyle) {
    rules.add(new Rule(scope, null, fontStyle));
  }

  private static void put(JsonObject object, String key, String value) {
    if (value == null || value.isEmpty()) {
      return;
    }
    object.addProperty(key, value);
  }

  /** یک قاعده تم؛ depth = تعداد segment اسکوپ (برای مرتب سازی والد قبل از فرزند). */
  private static final class Rule {
    final int depth;
    final String scope;
    final String color;
    final String fontStyle;

    Rule(String scope, String color, String fontStyle) {
      this.scope = scope;
      this.color = color;
      this.fontStyle = fontStyle;
      this.depth = depthOf(scope);
    }

    JsonObject toJson() {
      JsonObject rule = new JsonObject();
      rule.addProperty("scope", scope);
      JsonObject settings = new JsonObject();
      if (color != null) {
        settings.addProperty("foreground", color);
      }
      if (fontStyle != null) {
        settings.addProperty("fontStyle", fontStyle);
      }
      rule.add("settings", settings);
      return rule;
    }

    private static int depthOf(String scope) {
      String last = scope.substring(scope.lastIndexOf(' ') + 1);
      int depth = 1;
      for (int i = 0; i < last.length(); i++) {
        if (last.charAt(i) == '.') {
          depth++;
        }
      }
      return depth;
    }
  }
}
