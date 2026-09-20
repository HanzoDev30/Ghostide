package ir.hanzodev1375.ghostide.settings;

import android.os.Environment;
import android.os.Process;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.blankj.utilcode.util.ThreadUtils;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import ir.hanzodev1375.components.TextInputDialogFragment;
import ir.hanzodev1375.components.sheet.SliderSheet;
import ir.hanzodev1375.components.sheet.customitemsheet.ui.DialogCompat;
import ir.hanzodev1375.components.utils.ComponentsPrefs;
import ir.hanzodev1375.components.views.GhostToast;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.activity.SettingActivity;
import ir.hanzodev1375.ghostide.adapters.ThemeFilesAdapter;
import ir.hanzodev1375.ghostide.appicon.AppIconChooserDialogBuilder;
import ir.hanzodev1375.ghostide.appicon.AppIconManager;
import ir.hanzodev1375.ghostide.codeeditors.setting.PreferencesUtils;
import ir.hanzodev1375.ghostide.jgit.GitHubClient;
import ir.hanzodev1375.ghostide.models.SettingItem;
import ir.hanzodev1375.ghostide.utils.FileUtil;
import ir.hanzodev1375.ghostide.utils.LocaleHelper;
import ir.theme.GhostTheme;
import ir.theme.M3Theme;
import ir.theme.ThemeBus;
import ir.theme.ThemeManager;
import ir.theme.ThemeMediaPath;
import ir.theme.WidgetTheme;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class AppSettingSection extends SettingSection {

  private static final int POS_BUFFER_SIZE = 0;
  private static final int POS_APP_ICON = 1;
  private static final int POS_LOAD_THEME = 2;
  private static final int POS_GITHUB_ACCOUNT = 3;
  private static final int POS_LANGUAGE = 4;
  private static final int POS_SHOW_BACKGROUND = 7;
  private static final int POS_ANIMATION_THRESHOLD = 9;
  private static final int POS_GRID_COUNT = 10;
  private static final int POS_GLASS_TINT = 13;

  private static final String THEMES_DIRECTORY = "ghostide/themes";

  private final PreferencesUtils prefs;

  public AppSettingSection(SettingActivity activity, PreferencesUtils prefs) {
    super(activity);
    this.prefs = prefs;
  }

  public void openGitHubAccount() {
    showGitHubAccountDialog();
  }

  @Override
  protected List<SettingItem> buildItems() {
    List<SettingItem> items = new ArrayList<>();
    String current =
        String.format(
            getString(R.string.current_value), (prefs.getCurrentBufferSize() / 1024) + " KB");
    items.add(
        textItem(
            R.string.pref_buffer_size, getString(R.string.pref_buffer_size_desc) + "\n" + current));
    items.add(
        textItem(
            R.string.pref_app_icon,
            getString(R.string.pref_app_icon_desc)
                + "\n"
                + getString(AppIconManager.getCurrentIcon(activity).labelRes)));
    items.add(textItem(R.string.pref_load_theme_file, R.string.pref_load_theme_file_desc));
    items.add(
        textItem(
            R.string.github_account,
            prefs.isGitHubLoggedIn()
                ? getString(R.string.github_account_logged_in, prefs.getGitHubUsername())
                : getString(R.string.github_account_not_logged_in)));
    String currentLang = LocaleHelper.LANGUAGE_NAMES[LocaleHelper.getSavedLanguageIndex(activity)];
    items.add(
        textItem(
            R.string.pref_language, getString(R.string.pref_language_desc) + "\n" + currentLang));
    items.add(
        switchItem(
            R.string.pref_show_tab_icon_title,
            R.string.pref_show_tab_icon_summary,
            prefs.getShowIconTab(),
            prefs::setShowIconTab));
    items.add(
        switchItem(
            R.string.grid_title, R.string.grid_subtitle, prefs.getGridMod(), prefs::setGridMod));
    items.add(
        new SettingItem(
            getString(R.string.pref_show_background),
            getString(R.string.pref_show_background_desc),
            prefs.isShowBackground(),
            0,
            isChecked -> {
              prefs.setShowBackground(isChecked);
              M3Theme.reloadMode();
              activity.refreshBackgroundUi();
              GhostTheme themeNow = new ThemeManager(activity).getTheme();
              ThemeBus.getInstance().notifyThemeChanged(themeNow, themeNow, false);
            }));
    items.add(
        switchItem(
            R.string.pref_parallax,
            R.string.pref_parallax_desc,
            prefs.isParallaxEnabled(),
            prefs::setParallaxEnabled));
    items.add(
        textItem(
            R.string.pref_animation_battery_threshold,
            getString(R.string.pref_animation_battery_threshold_desc)
                + "\n"
                + String.format(
                    getString(R.string.current_value),
                    prefs.getAnimationBatteryThreshold() + "%")));
    items.add(textItem(R.string.pref_grid_title, R.string.pref_grid_subtitle));
    items.add(
        switchItem(
            R.string.pref_show_hidden_files,
            R.string.pref_show_hidden_files_desc,
            prefs.isShowHiddenFiles(),
            prefs::setShowHiddenFiles));
    items.add(
        switchItem(
            R.string.terfrtitle,
            R.string.terfrsubtitle,
            prefs.isTerminalFragment(),
            prefs::setTerminalFragment));
    items.add(
        textItem(
            R.string.pref_glass_tint,
            getString(R.string.pref_glass_tint_desc)
                + "\n"
                + String.format(
                    getString(R.string.current_value),
                    String.format(Locale.US, "%.2f", getGlassTint()))));
    return items;
  }

  @Override
  public void onItemClick(int position) {
    switch (position) {
      case POS_BUFFER_SIZE:
        showBufferSizeDialog();
        break;
      case POS_APP_ICON:
        showAppIconDialog();
        break;
      case POS_LOAD_THEME:
        showLoadThemeDialog();
        break;
      case POS_GITHUB_ACCOUNT:
        showGitHubAccountDialog();
        break;
      case POS_LANGUAGE:
        showLanguageDialog();
        break;
      case POS_ANIMATION_THRESHOLD:
        showAnimationThresholdDialog();
        break;
      case POS_GRID_COUNT:
        showGridCountDialog();
        break;
      case POS_GLASS_TINT:
        showGlassTintDialog();
        break;
      default:
        break;
    }
  }

  // ---------------------------------------------------------------------
  // Dialogs
  // ---------------------------------------------------------------------

  private void showBufferSizeDialog() {
    int current = prefs.getCurrentBufferSize() / 1024;
    String[] sizes = {"2", "4", "6", "8", "10", "12", "16", "20"};
    String[] labels = {
      getString(R.string.buffer_size_2),
      getString(R.string.buffer_size_4),
      getString(R.string.buffer_size_6),
      getString(R.string.buffer_size_8),
      getString(R.string.buffer_size_10),
      getString(R.string.buffer_size_12),
      getString(R.string.buffer_size_16),
      getString(R.string.buffer_size_20)
    };
    int checked = 0;
    for (int i = 0; i < sizes.length; i++) {
      if (Integer.parseInt(sizes[i]) == current) checked = i;
    }
    showSingleChoiceDialog(
        R.string.pref_buffer_size,
        labels,
        checked,
        which -> {
          prefs.setBufferSize(sizes[which]);
          new DialogCompat(activity)
              .setTitle(R.string.restart_required)
              .setMessage(R.string.restart_message)
              .setPositiveButton(
                  R.string.restart_now, (d2, w) -> Process.killProcess(Process.myPid()))
              .setNegativeButton(R.string.later, null)
              .show();
        });
  }

  private void showAppIconDialog() {
    new AppIconChooserDialogBuilder(activity)
        .setTitle(R.string.pref_app_icon)
        .setPositiveButton(
            R.string.ok,
            icon -> {
              AppIconManager.applyIcon(activity, icon);
              updateDescriptionAt(
                  POS_APP_ICON,
                  getString(R.string.pref_app_icon_desc) + "\n" + getString(icon.labelRes));
            })
        .setNegativeButton(R.string.cancel)
        .create()
        .show();
  }

  private void showLanguageDialog() {
    int checkedIndex = LocaleHelper.getSavedLanguageIndex(activity);
    showSingleChoiceDialog(
        R.string.pref_language,
        LocaleHelper.LANGUAGE_NAMES,
        checkedIndex,
        which -> {
          String selectedCode = LocaleHelper.LANGUAGE_CODES[which];
          LocaleHelper.saveLanguage(activity, selectedCode);
          LocaleListCompat localeList =
              "default".equals(selectedCode)
                  ? LocaleListCompat.getEmptyLocaleList()
                  : LocaleListCompat.forLanguageTags(selectedCode);
          AppCompatDelegate.setApplicationLocales(localeList);
        });
  }

  private void showGridCountDialog() {
    int id = prefs.getGridSpanCount();
    int[] values = {2, 4, 6, 8};
    String[] labels = {
      getString(R.string.pref_grid, "2"),
      getString(R.string.pref_grid, "4"),
      getString(R.string.pref_grid, "6"),
      getString(R.string.pref_grid, "8")
    };
    int checked = 0;
    for (int i = 0; i < values.length; i++) {
      if (values[i] == id) {
        checked = i;
        break;
      }
    }
    showSingleChoiceDialog(
        R.string.pref_grid_title, labels, checked, which -> prefs.setGridSpanCount(values[which]));
  }

  private void showAnimationThresholdDialog() {
    SliderSheet slidersheet = new SliderSheet(activity);
    int current = prefs.getAnimationBatteryThreshold();
    var slider = slidersheet.getSlider();
    slider.setValueFrom(0f);
    slider.setValueTo(100f);
    slider.setStepSize(5f);
    slider.setValue(current);
    slidersheet.setLable(current + "%");
    slider.addOnChangeListener((s, value, fromUser) -> slidersheet.setLable((int) value + "%"));
    slidersheet.setButtonOk(
        v -> {
          prefs.setAnimationBatteryThreshold((int) slider.getValue());
          updateItemAt(
              POS_ANIMATION_THRESHOLD,
              new SettingItem(
                  getString(R.string.pref_animation_battery_threshold),
                  getString(R.string.pref_animation_battery_threshold_desc)
                      + "\n"
                      + String.format(
                          getString(R.string.current_value),
                          prefs.getAnimationBatteryThreshold() + "%"),
                  false,
                  0,
                  null));
          slidersheet.dismiss();
        },
        R.string.ok);
    slidersheet.setButtonNo(v -> slidersheet.dismiss(), R.string.no);
    slidersheet.show();
  }

  private float getGlassTint() {
    return new ComponentsPrefs(activity).getGlassTint();
  }

  private void showGlassTintDialog() {
    SliderSheet slidersheet = new SliderSheet(activity);
    ComponentsPrefs componentsPrefs = new ComponentsPrefs(activity);
    float current = componentsPrefs.getGlassTint();
    var slider = slidersheet.getSlider();
    slider.setValueFrom(0.1f);
    slider.setValueTo(1f);
    slider.setStepSize(0.01f);
    slider.setValue(current);
    slidersheet.setLable(String.format(Locale.US, "Tint: %.2f", current));
    slider.addOnChangeListener(
        (s, value, fromUser) ->
            slidersheet.setLable(String.format(Locale.US, "Tint: %.2f", value)));
    slidersheet.setButtonOk(
        v -> {
          componentsPrefs.setGlassTint(slider.getValue());
          updateItemAt(
              POS_GLASS_TINT,
              new SettingItem(
                  getString(R.string.pref_glass_tint),
                  getString(R.string.pref_glass_tint_desc)
                      + "\n"
                      + String.format(
                          getString(R.string.current_value),
                          String.format(Locale.US, "%.2f", slider.getValue())),
                  false,
                  0,
                  null));
          slidersheet.dismiss();
        },
        R.string.ok);
    slidersheet.setButtonNo(v -> slidersheet.dismiss(), R.string.no);
    slidersheet.show();
  }

  private void showGitHubAccountDialog() {
    if (prefs.isGitHubLoggedIn()) {
      new DialogCompat(activity)
          .setTitle(getString(R.string.github_account))
          .setMessage(getString(R.string.github_account_logged_in, prefs.getGitHubUsername()))
          .setPositiveButton(getString(R.string.ok), null)
          .setNegativeButton(
              getString(R.string.github_logout),
              (d, w) ->
                  ThreadUtils.runOnUiThread(
                      () -> {
                        new GitHubClient(activity).logout();
                        GhostToast.makeText(
                                activity, R.string.github_logout_success, GhostToast.LENGTH_SHORT)
                            .show();
                        updateItemAt(
                            POS_GITHUB_ACCOUNT,
                            new SettingItem(
                                getString(R.string.github_account),
                                getString(R.string.github_account_not_logged_in),
                                false,
                                0,
                                null));
                      }))
          .show();
    } else {
      TextInputDialogFragment.newInstance(
              getString(R.string.github_login_title), getString(R.string.github_login_hint), "")
          .setCallback(
              token ->
                  new GitHubClient(activity)
                      .login(
                          token,
                          new GitHubClient.GitHubLoginCallback() {
                            @Override
                            public void onSuccess(String name, String username, String avatarUrl) {
                              ThreadUtils.runOnUiThread(
                                  () -> {
                                    GhostToast.makeText(
                                            activity,
                                            getString(R.string.github_welcome, name),
                                            GhostToast.LENGTH_SHORT)
                                        .show();
                                    updateItemAt(
                                        POS_GITHUB_ACCOUNT,
                                        new SettingItem(
                                            getString(R.string.github_account),
                                            getString(R.string.github_account_logged_in, username),
                                            false,
                                            0,
                                            null));
                                  });
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                              ThreadUtils.runOnUiThread(
                                  () ->
                                      GhostToast.makeText(
                                              activity, errorMessage, GhostToast.LENGTH_SHORT)
                                          .show());
                            }
                          }))
          .show(activity.getSupportFragmentManager(), "github_token");
    }
  }

  // ---------------------------------------------------------------------
  // Theme file
  // ---------------------------------------------------------------------

  private void showLoadThemeDialog() {
    View v = activity.getLayoutInflater().inflate(R.layout.dialog_theme_picker, null, false);
    TextInputLayout input = v.findViewById(R.id.editor);
    input.setHint("/sdcard/GhostIDE/themes/draks.gth");
    input.getEditText().setText(!prefs.getAppThemeFile().isEmpty() ? prefs.getAppThemeFile() : "");

    RecyclerView list = v.findViewById(R.id.theme_list);
    list.setLayoutManager(new GridLayoutManager(activity, 3));

    ThemeFilesAdapter[] box = new ThemeFilesAdapter[1];
    ThemeFilesAdapter adapter =
        new ThemeFilesAdapter(
            scanThemeFiles(),
            file -> {
              previewTheme(input, file);
              box[0].setSelectedPath(file.getAbsolutePath());
              box[0].notifyDataSetChanged();
              M3Theme.reloadMode();
              activity.refreshBackgroundUi();
              GhostTheme themeNow = new ThemeManager(activity).getTheme();
              ThemeBus.getInstance().notifyThemeChanged(themeNow, themeNow, false);
            });
    box[0] = adapter;
    adapter.setSelectedPath(prefs.getAppThemeFile());
    adapter.setOnFileLongClickListener((file, holder) -> confirmDeleteThemeFile(file, adapter));
    list.setAdapter(adapter);

    DialogCompat dialogBuilder =
        new DialogCompat(activity)
            .setTitle(getString(R.string.theme_load_title))
            .setMessage(getString(R.string.theme_load_message))
            .setView(v)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null);
    var dialog = dialogBuilder.create();
    dialog.setOnDismissListener(d -> onThemeDialogDismissed());
    dialog.setOnShowListener(
        d ->
            dialog
                .getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v12 -> applyThemeFromInput(input, dialog)));
    dialog.show();
  }

  private List<File> scanThemeFiles() {
    List<File> themes = new ArrayList<>();
    File themeDir = new File(Environment.getExternalStorageDirectory(), THEMES_DIRECTORY);
    scanThemeFilesRecursive(themeDir, themes);
    Collections.sort(themes, (a, b) -> a.getAbsolutePath().compareTo(b.getAbsolutePath()));
    return themes;
  }

  private void scanThemeFilesRecursive(File dir, List<File> out) {
    File[] files = dir.listFiles();
    if (files == null) return;
    for (File f : files) {
      if (f.isDirectory()) {
        scanThemeFilesRecursive(f, out);
      } else if (f.isFile() && f.getName().toLowerCase(Locale.ROOT).endsWith(".gth")) {
        out.add(f);
      }
    }
  }

  private void confirmDeleteThemeFile(File file, ThemeFilesAdapter adapter) {
    new DialogCompat(activity)
        .setTitle(getString(R.string.theme_delete_title))
        .setMessage(getString(R.string.theme_delete_message, file.getName()))
        .setPositiveButton(
            R.string.action_delete,
            (d, w) -> {
              boolean removed = file.exists() && file.delete();
              if (removed) {
                String applied = prefs.getAppThemeFile();
                if (applied != null && applied.equals(file.getAbsolutePath())) {
                  prefs.setAppThemeFile("");
                }
                adapter.refresh(scanThemeFiles());
                GhostToast.makeText(
                        activity, getString(R.string.theme_delete_success), GhostToast.LENGTH_SHORT)
                    .show();
              } else {
                GhostToast.makeText(
                        activity, getString(R.string.theme_delete_failed), GhostToast.LENGTH_SHORT)
                    .show();
              }
            })
        .setNegativeButton(R.string.cancel, null)
        .show();
  }

  private void previewTheme(TextInputLayout input, File file) {
    try {
      String json = new String(FileUtil.readBytesCompat(file), StandardCharsets.UTF_8);
      GhostTheme theme = new Gson().fromJson(json, GhostTheme.class);
      if (theme == null) throw new Exception("Invalid theme format");
      WidgetTheme widget = theme.getWidget();
      if (widget != null) {
        String stored = widget.getImagepath();
        if (stored != null && !stored.isEmpty()) {
          widget.setImagepath(ThemeMediaPath.resolve(file.getAbsolutePath(), stored));
        }
      }
      maybeEnableBackground(theme);
      GhostTheme base = new ThemeManager(activity).getTheme();
      if (base == null) {
        base = theme;
      }
      M3Theme.setPreviewTheme(theme);
      ThemeBus.getInstance().notifyThemeChanged(base, theme, true);
      input.getEditText().setText(file.getAbsolutePath());
    } catch (Exception e) {
      GhostToast.makeText(
              activity,
              String.format(getString(R.string.theme_load_error), e.getMessage()),
              GhostToast.LENGTH_SHORT)
          .show();
    }
  }

  private void applyThemeFromInput(TextInputLayout input, AlertDialog dialog) {
    String path = input.getEditText().getText().toString().trim();
    if (path.isEmpty()) {
      new ThemeManager(activity).resetToDefault();
      dialog.dismiss();
      return;
    }
    if (!path.endsWith(".gth")) {
      GhostToast.makeText(
              activity, getString(R.string.theme_load_invalid_extension), GhostToast.LENGTH_SHORT)
          .show();
      return;
    }
    File file = new File(path);
    if (!file.exists()) {
      GhostToast.makeText(
              activity,
              String.format(getString(R.string.theme_load_file_not_found), path),
              GhostToast.LENGTH_SHORT)
          .show();
      return;
    }
    try {
      String json = new String(FileUtil.readBytesCompat(file), StandardCharsets.UTF_8);
      GhostTheme theme = new Gson().fromJson(json, GhostTheme.class);
      if (theme == null) throw new Exception("Invalid theme format");
      maybeEnableBackground(theme);
      new ThemeManager(activity).saveTheme(theme);
      prefs.setAppThemeFile(path);
      dialog.dismiss();
      GhostToast.makeText(activity, getString(R.string.theme_load_success), GhostToast.LENGTH_LONG)
          .show();
    } catch (Exception e) {
      GhostToast.makeText(
              activity,
              String.format(getString(R.string.theme_load_error), e.getMessage()),
              GhostToast.LENGTH_SHORT)
          .show();
    }
  }

  private void maybeEnableBackground(GhostTheme theme) {
    if (theme == null || theme.getWidget() == null) {
      return;
    }
    String imagePath = theme.getWidget().getImagepath();
    boolean hasImage = imagePath != null && !imagePath.isEmpty();
    if (!hasImage) {
      if (prefs.isShowBackground()) {
        prefs.setShowBackground(false);
        M3Theme.reloadMode();
        setCheckedAt(POS_SHOW_BACKGROUND, false);
      }
      return;
    }
    if (!prefs.isShowBackground()) {
      prefs.setShowBackground(true);
      M3Theme.reloadMode();
      setCheckedAt(POS_SHOW_BACKGROUND, true);
    } else {
      M3Theme.reloadMode();
    }
  }

  private void onThemeDialogDismissed() {
    GhostTheme real = new ThemeManager(activity).getTheme();
    M3Theme.setPreviewTheme(null);
    ThemeBus.getInstance().notifyThemeChanged(real, real, false);
  }
}
