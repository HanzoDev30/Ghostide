package ir.hanzodev1375.ghostide.settings;

import com.google.gson.Gson;
import ir.hanzodev1375.components.views.GhostToast;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.activity.SettingActivity;
import ir.hanzodev1375.ghostide.codeeditors.IdeEditor;
import ir.hanzodev1375.ghostide.codeeditors.setting.PreferencesUtils;
import ir.hanzodev1375.ghostide.codeeditors.util.TranslateLanguages;
import ir.hanzodev1375.ghostide.dialogs.ShortcutSettingsDialog;
import ir.hanzodev1375.ghostide.models.CursorPack;
import ir.hanzodev1375.ghostide.models.SettingItem;
import ir.hanzodev1375.ghostide.utils.FileUtil;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class EditorSettingSection extends SettingSection {

  private static final int POS_TAB_SIZE = 17;
  private static final int POS_LINE_HEIGHT = 18;
  private static final int POS_CURSOR_BLINK = 19;
  private static final int POS_FONT = 21;
  private static final int POS_TRANSLATE_LANG = 22;
  private static final int POS_POWER_MODE_EFFECT = 25;
  private static final int POS_HANDLE_CURSOR = 31;
  private static final int POS_WHITESPACE_FLAGS = 32;
  private static final int POS_CURSOR_WIDTH = 33;
  private static final int POS_DIVIDER_WIDTH = 34;
  private static final int POS_DIVIDER_MARGIN = 35;
  private static final int POS_SHORTCUTS = 39;

  private final PreferencesUtils prefs;

  public EditorSettingSection(SettingActivity activity, PreferencesUtils prefs) {
    super(activity);
    this.prefs = prefs;
  }

  @Override
  protected List<SettingItem> buildItems() {
    List<SettingItem> items = new ArrayList<>();
    items.add(
        switchItem(
            R.string.pref_auto_save,
            R.string.pref_auto_save_desc,
            prefs.autoSaveFiles(),
            prefs::setAutoSave));
    items.add(
        switchItem(
            R.string.pref_auto_complete,
            R.string.pref_auto_complete_desc,
            prefs.enableAutoComplete(),
            prefs::setAutoComplete));
    items.add(
        switchItem(
            R.string.pref_auto_complete_animation,
            R.string.pref_auto_complete_animation_desc,
            prefs.enableAutoCompleteWindowAnimation(),
            prefs::setAutoCompleteWindowAnimation));
    items.add(
        switchItem(
            R.string.pref_auto_close_bracket,
            R.string.pref_auto_close_bracket_desc,
            prefs.enableBracketAutoClosing(),
            prefs::setBracketAutoClosing));
    items.add(
        switchItem(
            R.string.pref_bracket_highlight,
            R.string.pref_bracket_highlight_desc,
            prefs.enableBracketHighlight(),
            prefs::setBracketHighlight));
    items.add(
        switchItem(
            R.string.pref_line_numbers,
            R.string.pref_line_numbers_desc,
            prefs.enableLineNumbers(),
            prefs::setLineNumbers));
    items.add(
        switchItem(
            R.string.pref_pin_line_numbers,
            R.string.pref_pin_line_numbers_desc,
            prefs.pinLineNumber(),
            prefs::setPinLineNumber));
    items.add(
        switchItem(
            R.string.pref_word_wrap,
            R.string.pref_word_wrap_desc,
            prefs.useWordWrap(),
            prefs::setWordWrap));
    items.add(
        switchItem(
            R.string.pref_tab_indent,
            R.string.pref_tab_indent_desc,
            prefs.useTabIndentation(),
            prefs::setTabIndentation));
    items.add(
        switchItem(
            R.string.pref_font_ligatures,
            R.string.pref_font_ligatures_desc,
            prefs.useFontLigatures(),
            prefs::setFontLigatures));
    items.add(
        switchItem(
            R.string.pref_icu_library,
            R.string.pref_icu_library_desc,
            prefs.useICULibrary(),
            prefs::setICULibrary));
    items.add(
        switchItem(
            R.string.pref_magnifier,
            R.string.pref_magnifier_desc,
            prefs.enableMagnifier(),
            prefs::setMagnifier));
    items.add(
        switchItem(
            R.string.pref_sticky_scroll,
            R.string.pref_sticky_scroll_desc,
            prefs.enableStickyScroll(),
            prefs::setStickyScroll));
    items.add(
        switchItem(
            R.string.pref_scroll_bar,
            R.string.pref_scroll_bar_desc,
            prefs.enableScrollBar(),
            prefs::setScrollBar));
    items.add(
        switchItem(
            R.string.pref_hardware_acceleration,
            R.string.pref_hardware_acceleration_desc,
            prefs.enableHardWareAcceleration(),
            prefs::setHardwareAcceleration));
    items.add(
        switchItem(
            R.string.pref_delete_empty_line,
            R.string.pref_delete_empty_line_desc,
            prefs.enableDeleteEmptyLine(),
            prefs::setDeleteEmptyLine));
    items.add(
        switchItem(
            R.string.pref_delete_tab,
            R.string.pref_delete_tab_desc,
            prefs.enableDeleteTab(),
            prefs::setDeleteTab));
    items.add(textItem(R.string.pref_tab_size, R.string.pref_tab_size_desc));
    items.add(textItem(R.string.pref_line_height, R.string.pref_line_height_desc));
    items.add(textItem(R.string.pref_cursor_blink_period, R.string.pref_cursor_blink_period_desc));
    items.add(
        switchItem(
            R.string.pref_minimap,
            R.string.pref_minimap_dec,
            prefs.enableMiniMap(),
            prefs::setMiniMap));
    items.add(
        textItem(
            R.string.pref_font,
            getString(R.string.pref_font_desc)
                + "\n"
                + getFontDisplayName(prefs.getCurrentEditorFontName())));
    String currentLang = TranslateLanguages.getNameByCode(prefs.getTranslateTargetLang());
    items.add(
        textItem(
            R.string.pref_translate_target_lang,
            getString(R.string.pref_translate_target_lang_desc) + "\n" + currentLang));
    items.add(
        switchItem(
            R.string.setting_lineinfopaneltitle,
            R.string.setting_lineinfopanelsubtitle,
            prefs.getShowLineColPanel(),
            prefs::setShowLineColPanel));
    items.add(
        switchItem(
            R.string.pref_power_mode,
            R.string.pref_power_mode_desc,
            prefs.enablePowerMode(),
            prefs::setPowerMode));
    items.add(
        textItem(
            R.string.pref_power_mode_effect,
            getString(R.string.pref_power_mode_effect_desc)
                + "\n"
                + prefs.getPowerModeEffectType()));
    items.add(
        switchItem(
            R.string.pref_code_editor_block_line,
            R.string.pref_code_editor_block_line_desc,
            prefs.enableBlockLine(),
            prefs::setBlockLine));
    items.add(
        switchItem(
            R.string.backgroundzoomtitle,
            R.string.backgroundzoomsubtitle,
            prefs.isBackgroundZoomMod(),
            prefs::setBackgroundZoomMod));
    items.add(
        switchItem(
            R.string.tabcolortitle,
            R.string.tabcolorsubtitle,
            prefs.isTabLangColor(),
            prefs::setTabLangColor));
    items.add(
        switchItem(
            R.string.pref_ghost_text,
            R.string.pref_ghost_text_desc,
            prefs.enableGhostTextCompletion(),
            prefs::setGhostTextCompletion));
    String handleName = prefs.getCustomHandleCursorName();
    items.add(
        switchItem(
            R.string.pref_custom_handle,
            R.string.pref_custom_handle_desc,
            prefs.enableCustomHandle(),
            prefs::setCustomHandle));
    items.add(
        textItem(
            R.string.pref_handle_cursor, getString(R.string.pref_handle_cursor_desc, handleName)));
    items.add(
        new SettingItem(
            getString(R.string.pref_show_whitespace),
            getString(R.string.pref_show_whitespace_desc),
            anyWhitespaceFlag(),
            0,
            null));
    items.add(
        textItem(
            R.string.pref_cursor_width,
            getString(R.string.pref_cursor_width_desc)
                + "\n"
                + String.format(getString(R.string.current_value), formatDp(prefs.getCursorWidth()))));
    items.add(
        textItem(
            R.string.pref_divider_width,
            getString(R.string.pref_divider_width_desc)
                + "\n"
                + String.format(
                    getString(R.string.current_value), formatDp(prefs.getDividerWidth()))));
    items.add(
        textItem(
            R.string.pref_divider_margin,
            getString(R.string.pref_divider_margin_desc)
                + "\n"
                + String.format(
                    getString(R.string.current_value), formatDp(prefs.getDividerMargin()))));
    items.add(
        switchItem(
            R.string.pref_sticky_prefer_inner,
            R.string.pref_sticky_prefer_inner_desc,
            prefs.stickyScrollPreferInnerScope(),
            prefs::setStickyScrollPreferInnerScope));
    items.add(
        switchItem(
            R.string.pref_sticky_auto_collapse,
            R.string.pref_sticky_auto_collapse_desc,
            prefs.stickyScrollAutoCollapse(),
            prefs::setStickyScrollAutoCollapse));
    items.add(
        switchItem(
            R.string.pref_sticky_line_indicator,
            R.string.pref_sticky_line_indicator_desc,
            prefs.stickyLineIndicator(),
            prefs::setStickyLineIndicator));
    items.add(
        new SettingItem(
            getString(R.string.shortcuts_title),
            getString(R.string.shortcuts_desc),
            false,
            R.drawable.outline_keyboard,
            null));
    return items;
  }

  @Override
  public void onItemClick(int position) {
    switch (position) {
      case POS_TAB_SIZE:
        showTabSizeDialog();
        break;
      case POS_LINE_HEIGHT:
        showLineHeightDialog();
        break;
      case POS_CURSOR_BLINK:
        showCursorBlinkDialog();
        break;
      case POS_FONT:
        showFontDialog();
        break;
      case POS_TRANSLATE_LANG:
        showTranslateLanguageDialog();
        break;
      case POS_POWER_MODE_EFFECT:
        showPowerModeEffectDialog();
        break;
      case POS_HANDLE_CURSOR:
        showCustomHandleCursorDialog();
        break;
      case POS_WHITESPACE_FLAGS:
        showWhitespaceFlagsDialog();
        break;
      case POS_CURSOR_WIDTH:
        showCursorWidthDialog();
        break;
      case POS_DIVIDER_WIDTH:
        showDividerWidthDialog();
        break;
      case POS_DIVIDER_MARGIN:
        showDividerMarginDialog();
        break;
      case POS_SHORTCUTS:
        new ShortcutSettingsDialog(activity).showList();
        break;
      default:
        break;
    }
  }

  // ---------------------------------------------------------------------
  // Dialogs
  // ---------------------------------------------------------------------

  private void showTabSizeDialog() {
    int[] sizes = {2, 4, 6, 8};
    String[] labels = {
      getString(R.string.tab_size_2),
      getString(R.string.tab_size_4),
      getString(R.string.tab_size_6),
      getString(R.string.tab_size_8)
    };
    int current = prefs.getCodeEditorTabSize();
    int checked = 0;
    for (int i = 0; i < sizes.length; i++) {
      if (sizes[i] == current) checked = i;
    }
    showSingleChoiceDialog(
        R.string.pref_tab_size, labels, checked, which -> prefs.setCodeEditorTabSize(sizes[which]));
  }

  private void showLineHeightDialog() {
    String[] values = {"1", "2", "3", "4"};
    String[] labels = {
      getString(R.string.line_height_1),
      getString(R.string.line_height_2),
      getString(R.string.line_height_3),
      getString(R.string.line_height_4)
    };
    int checked = ((int) prefs.getCurrentEditorLineHeight()) - 1;
    if (checked < 0) checked = 1;
    showSingleChoiceDialog(
        R.string.pref_line_height, labels, checked, which -> prefs.setLineHeight(values[which]));
  }

  private void showCursorBlinkDialog() {
    showSliderDialog(
        R.string.pref_cursor_blink_period,
        200f,
        1000f,
        50f,
        prefs.getCursorBlinkPeriod(),
        value -> getString(R.string.cursor_blink_ms, (int) value.floatValue()),
        value -> prefs.setCursorBlinkPeriod((int) value.floatValue()));
  }

  private void showFontDialog() {
    final String[] fontKeys = {
      "jetbrains_mono_regular",
      "inconsolata_regular",
      "sourcecodepro_regular",
      "firacode_regular",
      "notosans_regular",
      "notosans_italic"
    };
    String[] fontNames = {
      getString(R.string.font_jetbrains_mono),
      getString(R.string.font_inconsolata),
      getString(R.string.font_source_code_pro),
      getString(R.string.font_fira_code),
      getString(R.string.font_noto_sans),
      getString(R.string.font_noto_sans_italic)
    };
    String currentFont = prefs.getCurrentEditorFontName();
    int checked = 0;
    for (int i = 0; i < fontKeys.length; i++) {
      if (fontKeys[i].equals(currentFont)) {
        checked = i;
        break;
      }
    }
    showSingleChoiceDialog(
        R.string.pref_font,
        fontNames,
        checked,
        which -> {
          prefs.setCurrentEditorFont(fontKeys[which]);
          updateDescriptionAt(
              POS_FONT,
              getString(R.string.pref_font_desc) + "\n" + fontNames[which]);
        });
  }

  private void showTranslateLanguageDialog() {
    String[] names = TranslateLanguages.NAMES;
    String[] codes = TranslateLanguages.CODES;
    String saved = prefs.getTranslateTargetLang();
    int checked = 0;
    for (int i = 0; i < codes.length; i++) {
      if (codes[i].equals(saved)) {
        checked = i;
        break;
      }
    }
    showSingleChoiceDialog(
        R.string.pref_translate_target_lang,
        names,
        checked,
        which -> {
          prefs.setTranslateTargetLang(codes[which]);
          updateDescriptionAt(
              POS_TRANSLATE_LANG,
              getString(R.string.pref_translate_target_lang_desc) + "\n" + names[which]);
        });
  }

  private void showCustomHandleCursorDialog() {
    List<String> names = scanCursorPackNames();
    if (names.isEmpty()) {
      GhostToast.makeText(activity, R.string.pref_handle_cursor_empty, GhostToast.LENGTH_SHORT)
          .show();
      return;
    }
    String current = prefs.getCustomHandleCursorName();
    int checked = 0;
    for (int i = 0; i < names.size(); i++) {
      if (names.get(i).equals(current)) {
        checked = i;
        break;
      }
    }
    showSingleChoiceDialog(
        R.string.pref_handle_cursor,
        names.toArray(new String[0]),
        checked,
        which -> {
          String selected = names.get(which);
          prefs.setCustomHandleCursorName(selected);
          updateDescriptionAt(POS_HANDLE_CURSOR, getString(R.string.pref_handle_cursor_desc, selected));
        });
  }

  private void showWhitespaceFlagsDialog() {
    IdeEditor.showNonPrintableFlagsDialog(activity, prefs, null);
  }

  private void showCursorWidthDialog() {
    showEditorSliderDialog(
        R.string.pref_cursor_width,
        R.string.pref_cursor_width_desc,
        POS_CURSOR_WIDTH,
        0.5f,
        5f,
        0.5f,
        prefs.getCursorWidth(),
        prefs::setCursorWidth);
  }

  private void showDividerWidthDialog() {
    showEditorSliderDialog(
        R.string.pref_divider_width,
        R.string.pref_divider_width_desc,
        POS_DIVIDER_WIDTH,
        0.5f,
        5f,
        0.5f,
        prefs.getDividerWidth(),
        prefs::setDividerWidth);
  }

  private void showDividerMarginDialog() {
    showEditorSliderDialog(
        R.string.pref_divider_margin,
        R.string.pref_divider_margin_desc,
        POS_DIVIDER_MARGIN,
        0f,
        10f,
        1f,
        prefs.getDividerMargin(),
        prefs::setDividerMargin);
  }

  private void showEditorSliderDialog(
      int titleRes,
      int descRes,
      int listPosition,
      float from,
      float to,
      float step,
      float current,
      java.util.function.Consumer<Float> onApply) {
    showSliderDialog(
        titleRes,
        from,
        to,
        step,
        current,
        EditorSettingSection::formatDp,
        value -> {
          onApply.accept(value);
          updateDescriptionAt(
              listPosition,
              getString(descRes)
                  + "\n"
                  + String.format(getString(R.string.current_value), formatDp(value)));
        });
  }

  private void showPowerModeEffectDialog() {
    // TODO: effect picker
  }

  // ---------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------

  private String getFontDisplayName(String fontKey) {
    switch (fontKey) {
      case "inconsolata_regular":
        return getString(R.string.font_inconsolata);
      case "sourcecodepro_regular":
        return getString(R.string.font_source_code_pro);
      case "firacode_regular":
        return getString(R.string.font_fira_code);
      case "notosans_regular":
        return getString(R.string.font_noto_sans);
      case "notosans_italic":
        return "Noto Sans Italic";
      default:
        return getString(R.string.font_jetbrains_mono);
    }
  }

  private boolean anyWhitespaceFlag() {
    return prefs.flagLeading()
        || prefs.flagInner()
        || prefs.flagTrailing()
        || prefs.flagEmptyLine()
        || prefs.flagLineBreaks()
        || prefs.flagInSelection()
        || prefs.flagTabSameAsSpace();
  }

  private List<String> scanCursorPackNames() {
    List<String> names = new ArrayList<>();
    File jsonFile = new File("/storage/emulated/0/ghostide/cursor/cursor.json");
    if (!jsonFile.exists()) return names;
    try {
      String json = new String(FileUtil.readBytesCompat(jsonFile), StandardCharsets.UTF_8);
      List<CursorPack> packs =
          new Gson()
              .fromJson(
                  json,
                  new com.google.gson.reflect.TypeToken<List<CursorPack>>() {}.getType());
      if (packs == null) return names;
      Set<String> added = new HashSet<>();
      for (CursorPack pack : packs) {
        String name = pack.getDisplayName();
        if (name != null && !name.isEmpty() && added.add(name)) {
          names.add(name);
        }
      }
    } catch (Exception ignored) {
    }
    Collections.sort(names);
    return names;
  }

  private static String formatDp(float dp) {
    return String.format(Locale.US, "%.1f dp", dp);
  }
}