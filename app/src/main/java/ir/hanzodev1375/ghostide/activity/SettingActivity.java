package ir.hanzodev1375.ghostide.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.blankj.utilcode.util.ThreadUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import ir.hanzodev1375.components.SearchLayout;
import ir.hanzodev1375.components.childern.ViewChilder;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.ai.utils.AiPreferencesUtils;
import ir.hanzodev1375.ghostide.codeeditors.setting.PreferencesUtils;
import ir.hanzodev1375.ghostide.customui.ExpandableLayout;
import ir.hanzodev1375.ghostide.settings.AiSettingSection;
import ir.hanzodev1375.ghostide.settings.AppSettingSection;
import ir.hanzodev1375.ghostide.settings.EditorSettingSection;
import ir.hanzodev1375.ghostide.settings.LspSettingSection;
import ir.hanzodev1375.ghostide.settings.SettingSection;
import ir.theme.M3Theme;

/**
 * Thin coordinator for the settings screen. Every section (Editor/App/AI/LSP) lives in
 * {@code ir.hanzodev1375.ghostide.settings} and owns its own rows, clicks and dialogs; this
 * activity only wires views, search and theme together.
 */
public class SettingActivity extends BaseCompat {

  private PreferencesUtils prefs;
  private AiPreferencesUtils aiPrefs;
  private EditorSettingSection editorSection;
  private AppSettingSection appSection;
  private AiSettingSection aiSection;
  private LspSettingSection lspSection;
  private ExpandableLayout expandEditor, expandApp, LspView, expandAi;
  private RecyclerView rvEditor, rvApp, rvLsp, rvAi;
  private SearchLayout ser;
  private boolean isSearchActive = false;
  private Runnable filterRunnable;
  private Runnable expandRunnable;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_setting);
    aiPrefs = new AiPreferencesUtils(this);
    prefs = new PreferencesUtils(this);
    setupBackgroundBlur();
    MaterialToolbar toolbar = findViewById(R.id.toolbar);
    ser = findViewById(R.id.searchitem);

    setSupportActionBar(toolbar);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setTitle(R.string.settings_title);
    }

    expandEditor = findViewById(R.id.expandEditor);
    expandApp = findViewById(R.id.expandApp);
    expandAi = findViewById(R.id.expandAi);
    LspView = findViewById(R.id.lspsetting);

    expandEditor.setTitle(getString(R.string.section_editor));
    expandApp.setTitle(getString(R.string.section_app));
    expandAi.setTitle(getString(R.string.ai_section_title));
    LspView.setTitle(getString(R.string.lsptitles));

    rvEditor = expandEditor.getRecyclerView();
    rvApp = expandApp.getRecyclerView();
    rvAi = expandAi.getRecyclerView();
    rvLsp = LspView.getRecyclerView();
    rvEditor.setLayoutManager(new LinearLayoutManager(this));
    rvApp.setLayoutManager(new LinearLayoutManager(this));
    rvAi.setLayoutManager(new LinearLayoutManager(this));
    rvLsp.setLayoutManager(new LinearLayoutManager(this));

    editorSection = new EditorSettingSection(this, prefs);
    appSection = new AppSettingSection(this, prefs);
    aiSection = new AiSettingSection(this, aiPrefs);
    lspSection = new LspSettingSection(this, prefs);

    rvEditor.setAdapter(editorSection.createAdapter());
    rvApp.setAdapter(appSection.createAdapter());
    rvAi.setAdapter(aiSection.createAdapter());
    rvLsp.setAdapter(lspSection.createAdapter());

    ser.show();
    ser.setIconClose(R.drawable.ic_close);
    ser.setIconSearch(R.drawable.outline_search);

    View root = findViewById(R.id.settingRoot);
    ViewCompat.setOnApplyWindowInsetsListener(
        root,
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
          findViewById(R.id.appbar).setPadding(0, systemBars.top, 0, 0);
          int bottomInset = Math.max(systemBars.bottom, ime.bottom);
          ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) ser.getLayoutParams();
          lp.bottomMargin = bottomInset + (int) (4 * getResources().getDisplayMetrics().density);
          ser.setLayoutParams(lp);
          NestedScrollView scrollView = findViewById(R.id.scrollView);
          scrollView.setPadding(
              0, 0, 0, bottomInset + (int) (80 * getResources().getDisplayMetrics().density));
          return insets;
        });

    // Prevents crash when DiffUtil updates lists while animations are running
    rvEditor.setItemAnimator(null);
    rvApp.setItemAnimator(null);
    rvAi.setItemAnimator(null);
    rvLsp.setItemAnimator(null);

    ser.setOnTextChangedListener(
        (item) -> {
          boolean hasText = item.length() > 0;

          if (hasText && !isSearchActive) {
            isSearchActive = true;
            ser.removeCallbacks(expandRunnable);
            expandRunnable =
                () -> {
                  if (!expandAi.isExpanded()) expandAi.expand();
                  if (!expandApp.isExpanded()) expandApp.expand();
                  if (!expandEditor.isExpanded()) expandEditor.expand();
                  if (!LspView.isExpanded()) LspView.expand();
                };
            ser.postDelayed(expandRunnable, 50);
          }

          ser.removeCallbacks(filterRunnable);
          filterRunnable =
              () -> {
                if (hasText) {
                  filterAll(item);
                } else {
                  resetAll();
                  if (expandAi.isExpanded()) expandAi.collapse();
                  if (expandApp.isExpanded()) expandApp.collapse();
                  if (expandEditor.isExpanded()) expandEditor.collapse();
                  if (LspView.isExpanded()) LspView.collapse();
                  isSearchActive = false;
                  ser.removeCallbacks(expandRunnable);
                }
              };
          ser.postDelayed(filterRunnable, 50);
        });

    if ("githublogin".equals(getIntent().getStringExtra("open_section"))) {
      ThreadUtils.runOnUiThreadDelayed(
          () -> {
            expandApp.expand();
            appSection.openGitHubAccount();
          },
          500);
    }

    M3Theme.applyTopLevel(root);
  }

  private void filterAll(String query) {
    editorSection.filter(query);
    appSection.filter(query);
    lspSection.filter(query);
    aiSection.filter(query);
  }

  private void resetAll() {
    for (SettingSection section : new SettingSection[] {editorSection, appSection, lspSection, aiSection}) {
      section.resetToFull();
    }
  }

  /** Lets settings sections re-run the background blur after a theme/background change. */
  public void refreshBackgroundUi() {
    setupBackgroundBlur();
  }

  private void setupBackgroundBlur() {
    View appbar = findViewById(R.id.appbar);
    View toolbar = findViewById(R.id.toolbar);
    View settingRoot = findViewById(R.id.settingRoot);
    View scrollView = findViewById(R.id.scrollView);
    View settingsContainer = findViewById(R.id.settings_container);
    ViewChilder backgroundIcon = findViewById(R.id.backgroundIconSetting);
    appbar.setBackgroundColor(0);
    toolbar.setBackgroundColor(0);
    if (settingsContainer == null || backgroundIcon == null) return;
    setupBackgroundBlur(backgroundIcon, settingRoot, appbar, scrollView);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) finish();
    return super.onOptionsItemSelected(item);
  }
}