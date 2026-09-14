package ir.hanzodev1375.ghostide.codeeditors.lspcustomhot;

import ir.hanzodev1375.ghostide.codeeditors.lspcustomhot.model.CssSelect;
import java.util.ArrayList;
import java.util.List;

public class PseudoData {

  public static List<CssSelect> getPseudoClasses() {
    List<CssSelect> list = new ArrayList<>();

    list.add(
        new CssSelect(
            "hover",
            "**:hover** - Applies when mouse hovers over the element.\n```css\na:hover { color: red; }\n```\n📘 MDN: :hover"));
    list.add(
        new CssSelect(
            "focus",
            "**:focus** - Element receives focus.\n```css\ninput:focus { border-color: blue; }\n```"));
    list.add(
        new CssSelect(
            "active",
            "**:active** - Moment of activation.\n```css\nbutton:active { transform: scale(0.98); }\n```"));
    list.add(
        new CssSelect(
            "visited",
            "**:visited** - Already visited link.\n```css\na:visited { color: purple; }\n```"));
    list.add(
        new CssSelect("link", "**:link** - Unvisited link.\n```css\na:link { color: blue; }\n```"));
    list.add(
        new CssSelect(
            "first-child",
            "**:first-child** - First child of its parent.\n```css\nli:first-child { font-weight: bold; }\n```"));
    list.add(new CssSelect("last-child", "**:last-child** - Last child of its parent."));
    list.add(
        new CssSelect(
            "nth-child",
            "**:nth-child()** - Nth child using formula.\n```css\nli:nth-child(odd) { background: #eee; }\n```"));
    list.add(new CssSelect("nth-of-type", "**:nth-of-type()** - Nth element of its type."));
    list.add(new CssSelect("first-of-type", "**:first-of-type** - First element of its type."));
    list.add(new CssSelect("last-of-type", "**:last-of-type** - Last element of its type."));
    list.add(new CssSelect("only-child", "**:only-child** - Only child of its parent."));
    list.add(new CssSelect("only-of-type", "**:only-of-type** - Only element of its type."));
    list.add(new CssSelect("empty", "**:empty** - Element with no children."));
    list.add(new CssSelect("root", "**:root** - Root of the document (<html>)."));
    list.add(
        new CssSelect(
            "not",
            "**:not()** - Negation pseudo‑class.\n```css\np:not(.special) { color: gray; }\n```"));
    list.add(new CssSelect("is", "**:is()** - Matches any selector in the list."));
    list.add(new CssSelect("where", "**:where()** - Same as :is() but zero specificity."));
    list.add(
        new CssSelect(
            "has",
            "**:has()** - Relational pseudo‑class.\n```css\ndiv:has(p) { background: yellow; }\n```"));
    list.add(new CssSelect("target", "**:target** - Element whose ID matches URL fragment."));
    list.add(new CssSelect("enabled", "**:enabled** - Enabled form element."));
    list.add(new CssSelect("disabled", "**:disabled** - Disabled form element."));
    list.add(new CssSelect("checked", "**:checked** - Checked radio/checkbox."));
    list.add(new CssSelect("indeterminate", "**:indeterminate** - Indeterminate state."));
    list.add(new CssSelect("valid", "**:valid** - Form element that passes validation."));
    list.add(new CssSelect("invalid", "**:invalid** - Form element that fails validation."));
    list.add(new CssSelect("in-range", "**:in-range** - Input value inside range limits."));
    list.add(
        new CssSelect("out-of-range", "**:out-of-range** - Input value outside range limits."));
    list.add(new CssSelect("required", "**:required** - Form element with 'required' attribute."));
    list.add(
        new CssSelect("optional", "**:optional** - Form element without 'required' attribute."));
    list.add(new CssSelect("read-only", "**:read-only** - Non‑editable element."));
    list.add(new CssSelect("read-write", "**:read-write** - Editable element."));
    list.add(
        new CssSelect(
            "placeholder-shown", "**:placeholder-shown** - Input where placeholder is visible."));
    list.add(new CssSelect("dir", "**:dir()** - Matches based on text direction (ltr/rtl)."));
    list.add(new CssSelect("lang", "**:lang()** - Matches based on language code."));

    return list;
  }

  public static List<CssSelect> getPseudoElements() {
    List<CssSelect> list = new ArrayList<>();

    list.add(
        new CssSelect(
            "before",
            "**::before** - Creates pseudo‑element as first child.\n```css\np::before { content: \"→ \"; }\n```\n📘 MDN: ::before"));
    list.add(
        new CssSelect(
            "after",
            "**::after** - Creates pseudo‑element as last child.\n```css\np::after { content: \" ←\"; }\n```"));
    list.add(
        new CssSelect(
            "first-line",
            "**::first-line** - Styles first line of block.\n```css\np::first-line { font-weight: bold; }\n```"));
    list.add(
        new CssSelect(
            "first-letter",
            "**::first-letter** - Styles first letter of block.\n```css\np::first-letter { font-size: 200%; }\n```"));
    list.add(
        new CssSelect(
            "selection",
            "**::selection** - Styles selected text.\n```css\n::selection { background: yellow; }\n```"));
    list.add(
        new CssSelect(
            "marker",
            "**::marker** - Styles list bullet/number.\n```css\nli::marker { color: red; }\n```"));
    list.add(
        new CssSelect(
            "placeholder",
            "**::placeholder** - Styles placeholder text.\n```css\ninput::placeholder { color: gray; }\n```"));
    list.add(new CssSelect("backdrop", "**::backdrop** - Styles fullscreen backdrop."));
    list.add(
        new CssSelect(
            "file-selector-button", "**::file-selector-button** - Styles file upload button."));
    list.add(new CssSelect("slotted", "**::slotted()** - Shadow DOM slotted content."));
    list.add(new CssSelect("spelling-error", "**::spelling-error** - Highlights spelling errors."));
    list.add(new CssSelect("grammar-error", "**::grammar-error** - Highlights grammar errors."));

    return list;
  }
}
