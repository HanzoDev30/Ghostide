package ir.hanzodev1375.ghostide.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.text.InputType;
import android.widget.FrameLayout;
import ir.hanzodev1375.components.sheet.customitemsheet.ui.LiquidGlassDialogBuilderJava;
import ir.hanzodev1375.ghostide.utils.EditorFileStats;
import ir.hanzodev1375.ghostide.utils.EditorGitStatus;
import ir.hanzodev1375.ghostide.utils.UriFileImporter;
import ir.hanzodev1375.ghostide.utils.EditorGlassMenu;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import ir.theme.M3Theme;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.View;
import ir.hanzodev1375.components.views.GhostToast;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.google.android.material.sidesheet.SideSheetDialog;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.rosemoe.sora.lsp.editor.LspEditorStatus;
import ir.hanzodev1375.components.colors.ColorPickerBottomSheet;
import ir.hanzodev1375.filetreelib.widget.FileTreeView;
import ir.hanzodev1375.ghostide.codeeditors.langs.lsp.model.BreadcrumbItem;
import ir.hanzodev1375.ghostide.codeeditors.setting.PreferencesUtils;
import ir.hanzodev1375.ghostide.codeeditors.ui.model.OpenFileLocationEvent;
import ir.hanzodev1375.ghostide.customui.EditorStatusBar;
import ir.hanzodev1375.ghostide.customui.TabCustomView;
import ir.hanzodev1375.ghostide.fragments.MarkDownPreview;
import ir.hanzodev1375.ghostide.jgit.GitHubClient;
import ir.hanzodev1375.ghostide.jgit.GitHubProfileSheet;
import ir.hanzodev1375.ghostide.jgit.fragments.GitBottomSheetFragment;
import ir.hanzodev1375.ghostide.runer.CodeRuner;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.adapters.EditorPagerAdapter;
import ir.hanzodev1375.ghostide.adapters.ToolbarListAdapter;
import ir.hanzodev1375.ghostide.adapters.BreadcrumbAdapter;
import ir.hanzodev1375.ghostide.codeeditors.IdeEditor;
import io.github.rosemoe.sora.lang.completion.snippet.CodeSnippet;
import io.github.rosemoe.sora.lang.completion.snippet.parser.CodeSnippetParser;

import ir.hanzodev1375.ghostide.dialogs.CommandPaletteSheet;
import ir.hanzodev1375.ghostide.commands.CommandPaletteRegistry;
import ir.hanzodev1375.ghostide.dialogs.OutlineSheet;
import ir.hanzodev1375.ghostide.dialogs.SnippetManagerSheet;
import ir.hanzodev1375.ghostide.customui.GhostIdeEditorSearch;
import ir.hanzodev1375.ghostide.databinding.ActivityEditorBinding;
import ir.hanzodev1375.ghostide.fragments.EditorFragment;
import ir.hanzodev1375.ghostide.models.TabModel;
import ir.hanzodev1375.ghostide.models.ToolbarModel;
import ir.hanzodev1375.ghostide.adapters.EditorHostAdapter;
import ir.hanzodev1375.ghostide.adapters.CodeRunnerHostAdapter;
import ir.hanzodev1375.ghostide.ide.ui.api.EditorPanel;
import ir.hanzodev1375.ghostide.ide.ui.api.CodeRunnerHost;
import ir.hanzodev1375.ghostide.ide.ui.api.FileEvent;
import ir.hanzodev1375.ghostide.ide.ui.api.IdeEvents;
import ir.hanzodev1375.ghostide.ide.ui.api.IdeHostServices;
import ir.hanzodev1375.ghostide.plugin.PluginManager;
import ir.hanzodev1375.ghostide.plugin.PluginPanelHost;
import ir.hanzodev1375.ghostide.plugin.PluginPopupController;
import ir.hanzodev1375.ghostide.plugin.api.Disposable;
import ir.hanzodev1375.ghostide.plugin.api.GlobalRegistry;
import ir.theme.ThemeManager;
import ir.theme.ThemeUtils;
import android.view.ViewTreeObserver;
import ir.hanzodev1375.ghostide.splitlayout.EditorPaneFragment;
import ir.hanzodev1375.ghostide.splitlayout.SplitLayoutPopup;
import ir.hanzodev1375.ghostide.splitlayout.SplitPaneContainerLayout;
import ir.hanzodev1375.ghostide.refactor.rename.FileRenameNotifier;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import ir.hanzodev1375.components.sheet.customitemsheet.ui.DialogCompat;

public class EditorActivity extends BaseCompat
    implements FileRenameNotifier.Listener, EditorGitStatus.Host {

  private ActivityEditorBinding binding;
  private EditorPagerAdapter adapter;
  private ThemeUtils theme;
  private List<TabModel> tabsList = new ArrayList<>();
  private SharedPreferences prefs;
  private Gson gson = new Gson();
  private static final String KEY_TABS = "path";
  private static final String KEY_POSITION = "positionTabs";
  private static final String KEY_SPLIT_ACTIVE = "splitActive";
  private static final String KEY_SPLIT_ROWS = "splitRows";
  private static final String KEY_SPLIT_COLS = "splitCols";
  private TabLayoutMediator tabMediator;
  private ToolbarListAdapter listAdapter;
  private boolean isShowSys = false;
  private List<ToolbarModel> toolbarModel = new ArrayList<>();
  private EditorGitStatus gitStatus;
  private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener;
  private SplitLayoutPopup splitLayoutPopup;
  private boolean isSplitViewActive = false;
  private PreferencesUtils settings;
  private int lastSplitRows = 1, lastSplitCols = 2;
  private EditorPaneFragment activePane = null;
  private PluginPopupController pluginPopupController;
  private static final long LSP_STATUS_POLL_INTERVAL_MS = 1500;
  private final Handler lspStatusHandler = new Handler(Looper.getMainLooper());
  private BreadcrumbAdapter breadcrumbAdapter;
  private Disposable editorHostRegistration;
  private Disposable codeRunnerHostRegistration;
  private PluginPanelHost pluginPanelHost;
  private final Runnable lspStatusPollRunnable =
      new Runnable() {
        @Override
        public void run() {
          refreshLspStatusIndicator();
          lspStatusHandler.postDelayed(this, LSP_STATUS_POLL_INTERVAL_MS);
        }
      };

  private final EditorPaneFragment.PaneActionListener paneActionListener =
      new EditorPaneFragment.PaneActionListener() {
        @Override
        public void onCloseTab(String filePath) {
          closeTabByPath(filePath);
        }

        @Override
        public void onCloseOthers(String filePath) {
          closeOtherTabsByPath(filePath);
        }

        @Override
        public void onCloseAll() {
          closeAllTabs();
        }

        @Override
        public void onTogglePin(String filePath) {
          togglePinByPath(filePath);
        }
      };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityEditorBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    prefs = getSharedPreferences("editor", MODE_PRIVATE);
    settings = new PreferencesUtils(this);
    ThemeManager manager = new ThemeManager(this);
    theme = new ThemeUtils(manager);
    EventBus.getDefault().register(this);
    gitStatus = new EditorGitStatus(this);
    setupViewPager();
    setupTabLayout();
    breadcrumbAdapter = new BreadcrumbAdapter();
    binding.rvBreadcrumb.setLayoutManager(
        new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    binding.rvBreadcrumb.setAdapter(breadcrumbAdapter);
    breadcrumbAdapter.setOnItemClickListener(
        item -> {
          IdeEditor current = getEditor();
          if (current != null) current.setSelection(item.getLine(), item.getColumn());
        });
    setupFAB();
    loadSavedTabs();
    updateLanguageStatus(binding.viewPager.getCurrentItem());
    PluginManager.init(this);
    String configPath = new File(getFilesDir(), "GhostIDE/plugins/config.json").getAbsolutePath();
    PluginManager.getInstance().loadPluginsFromConfig(configPath);
    editorHostRegistration =
        GlobalRegistry.services()
            .register(IdeHostServices.EDITOR_HOST, new EditorHostAdapter(this));
    codeRunnerHostRegistration =
        GlobalRegistry.services()
            .register(IdeHostServices.CODE_RUNNER_HOST, new CodeRunnerHostAdapter(this));
    applyThemeInternal(false);

    handleIncomingIntent(getIntent());

    String path = getIntent().getStringExtra("file_path");
    String name = getIntent().getStringExtra("file_name");
    if (path != null && name != null) {
      openFile(path, name);
    }
    pluginPanelHost = new PluginPanelHost(this, this::getCurrentFilePath);
    pluginPopupController = new PluginPopupController(this, pluginPanelHost);
    stepToolbar();
    theme.applyTabLayout(binding.tab, getCurrentFilePath());
    setupKeyboardListener();
    GitHubClient gitHub = new GitHubClient(this);
    if (gitHub.isLoggedIn()) {
      binding.userName.setText(gitHub.getName());
      Glide.with(this)
          .load(gitHub.getAvatarUrl())
          .circleCrop()
          .placeholder(R.drawable.user)
          .into(binding.userIcon);
    }

    binding.userIcon.setOnClickListener(
        v -> {
          if (gitHub.isLoggedIn()) {
            GitHubProfileSheet.newInstance().show(getSupportFragmentManager(), "github_profile");
          } else {
            new DialogCompat(v.getContext())
                .setTitle(getString(R.string.github_tokenerrortitle))
                .setMessage(getString(R.string.github_tokenerrormsg))
                .setPositiveButton(
                    getString(R.string.ok),
                    (c, e) -> {
                      Intent i = new Intent(getApplicationContext(), SettingActivity.class);
                      i.putExtra("open_section", "githublogin");
                      startActivity(i);
                    })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
          }
        });
    binding.symbolBarContainer.hide();
    binding.symbolBarContainer.bindEditor(this::getEditor);
    theme.applyEditorStatusBar(binding.editorStatusBar);

    ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), this::applyWindowInsets);
    gitStatus.refresh();
  }

  @Override
  protected void applyOwnTheme(ThemeUtils themeUtils) {
    if (binding == null) {
      return;
    }
    try {
      applyThemeInternal(true);
    } catch (Throwable ignored) {
    }
  }

  private void applyThemeInternal(boolean applyEditor) {
    theme.applyActivity(this);
    theme.applyView(binding.mainContent);
    theme.applyImageBackground(binding.backgroundicon);
    theme.applyFab(binding.fabineditor);
    theme.applyGhostIdeEditorSearch(binding.editorSearch);
    theme.applyTabLayout(binding.tab, getCurrentFilePath());
    theme.applyEditorStatusBar(binding.editorStatusBar);
    if (applyEditor) {
      IdeEditor editor = getEditor();
      if (editor != null) {
        theme.applyEditor(editor);
      }
    }
  }

  private WindowInsetsCompat applyWindowInsets(View v, WindowInsetsCompat insets) {
    int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
    int navBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
    int imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
    if (navBarHeight == 0) {
      navBarHeight = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
    }

    if (binding.mainContent != null) {
      binding.mainContent.setPadding(0, statusBarHeight, 0, navBarHeight);
    }

    int gapPx = dp(8);
    int editorStatusBarHeightPx = dp(36);
    boolean willShowEditorStatusBar = imeHeight <= 0 && !binding.editorSearch.isShowing;

    CoordinatorLayout.LayoutParams fabParams =
        (CoordinatorLayout.LayoutParams) binding.fabineditor.getLayoutParams();
    int newFabMargin = navBarHeight + dp(20) + 9;
    if (imeHeight > 0) newFabMargin += imeHeight;
    newFabMargin += willShowEditorStatusBar ? (editorStatusBarHeightPx + gapPx) : 0;
    fabParams.bottomMargin = newFabMargin;
    binding.fabineditor.setLayoutParams(fabParams);

    CoordinatorLayout.LayoutParams searchParams =
        (CoordinatorLayout.LayoutParams) binding.editorSearch.getLayoutParams();
    searchParams.bottomMargin = imeHeight > 0 ? imeHeight + gapPx : dp(16);
    binding.editorSearch.setLayoutParams(searchParams);

    CoordinatorLayout.LayoutParams symbolParams =
        (CoordinatorLayout.LayoutParams) binding.symbolBarContainer.getLayoutParams();
    symbolParams.bottomMargin = imeHeight > 0 ? imeHeight + gapPx : dp(16);
    binding.symbolBarContainer.setLayoutParams(symbolParams);

    CoordinatorLayout.LayoutParams statusBarParams =
        (CoordinatorLayout.LayoutParams) binding.editorStatusBar.getLayoutParams();
    statusBarParams.bottomMargin =
        imeHeight > 0 ? navBarHeight + imeHeight + gapPx : navBarHeight + dp(16);
    binding.editorStatusBar.setLayoutParams(statusBarParams);

    return insets;
  }

  private int dp(int value) {
    return (int)
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
  }

  @Override
  protected void onResume() {
    super.onResume();

    gitStatus.refresh();
    FileRenameNotifier.getInstance().addListener(this);
    lspStatusHandler.removeCallbacks(lspStatusPollRunnable);
    lspStatusHandler.post(lspStatusPollRunnable);
  }

  @Override
  protected void onPause() {
    super.onPause();
    FileRenameNotifier.getInstance().removeListener(this);
    lspStatusHandler.removeCallbacks(lspStatusPollRunnable);
  }

  @Override
  public void onFileRenamed(String oldPath, String newPath) {
    int index = indexOfTab(oldPath);
    if (index < 0) {
      return;
    }
    TabModel tab = tabsList.get(index);
    tab.updatePath(newPath, new File(newPath).getName());
    adapter.setTabs(new ArrayList<>(tabsList));
    TabLayout.Tab layoutTab = binding.tab.getTabAt(index);
    if (layoutTab != null && layoutTab.getCustomView() instanceof TabCustomView) {
      ((TabCustomView) layoutTab.getCustomView()).bind(tab);
    }
  }

  private int indexOfTab(String filePath) {
    for (int i = 0; i < tabsList.size(); i++) {
      if (tabsList.get(i).getFilePath().equals(filePath)) return i;
    }
    return -1;
  }

  private void closeTabByPath(String filePath) {
    int i = indexOfTab(filePath);
    if (i >= 0) closeTab(i);
  }

  private void closeOtherTabsByPath(String filePath) {
    int i = indexOfTab(filePath);
    if (i >= 0) closeOtherTabs(i);
  }

  private void togglePinByPath(String filePath) {
    int i = indexOfTab(filePath);
    if (i >= 0) togglePin(i);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    EventBus.getDefault().unregister(this);
    lspStatusHandler.removeCallbacks(lspStatusPollRunnable);
    if (keyboardLayoutListener != null) {
      getWindow()
          .getDecorView()
          .getViewTreeObserver()
          .removeOnGlobalLayoutListener(keyboardLayoutListener);
      keyboardLayoutListener = null;
    }
    if (gitStatus != null) {
      gitStatus.shutdown();
    }
    if (editorHostRegistration != null) {
      editorHostRegistration.dispose();
      editorHostRegistration = null;
    }
    if (codeRunnerHostRegistration != null) {
      codeRunnerHostRegistration.dispose();
      codeRunnerHostRegistration = null;
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    handleIncomingIntent(intent);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onOpenFileLocationEvent(OpenFileLocationEvent event) {
    openFileAtLocation(event.filePath, event.line, event.column);
  }

  private void openFileAtLocation(String filePath, int line, int column) {
    for (int i = 0; i < tabsList.size(); i++) {
      if (tabsList.get(i).getFilePath().equals(filePath)) {
        final int pos = i;
        switchToTab(pos);
        binding.viewPager.post(() -> jumpFragmentToLocation(pos, line, column));
        return;
      }
    }
    openFile(filePath, new File(filePath).getName(), true);
    binding.viewPager.postDelayed(
        () -> jumpFragmentToLocation(tabsList.size() - 1, line, column), 80);
  }

  /** بعد از اینکه fragment ساخته و محتوا لود شد، مکان‌نما رو به location دلخواه می‌بره. */
  private void jumpFragmentToLocation(int position, int line, int column) {
    if (adapter == null || position < 0 || position >= tabsList.size()) return;
    Fragment fragment = adapter.getFragmentAtPosition(position, this);
    if (fragment instanceof EditorFragment) {
      String fragPath =
          fragment.getArguments() != null ? fragment.getArguments().getString("file_path") : null;
      if (fragPath != null && fragPath.equals(tabsList.get(position).getFilePath())) {
        ((EditorFragment) fragment).jumpToLocation(line, column);
      }
    }
  }

  /** نمایش/مخفی کردن نوار جستجو (میان‌بر Ctrl+F). */
  public void showEditorSearch() {
    binding.editorSearch.showAndHide();
  }

  /** دیالوگ برو به خط (میان‌بر Ctrl+G) با ایجپوت شماره خط. */
  public void showGotoLineDialog() {
    if (adapter == null) return;
    int currentPos = binding.viewPager.getCurrentItem();
    Fragment current = adapter.getFragmentAtPosition(currentPos, this);
    if (!(current instanceof EditorFragment)) return;

    float density = getResources().getDisplayMetrics().density;
    TextInputLayout inputLayout = new TextInputLayout(this);
    inputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
    inputLayout.setHint(getString(R.string.editor_goto_line_hint));
    TextInputEditText input = new TextInputEditText(this);
    input.setInputType(InputType.TYPE_CLASS_NUMBER);
    M3Theme.textInputLayout(inputLayout);
    M3Theme.text(input);
    inputLayout.addView(input);

    FrameLayout wrap = new FrameLayout(this);
    int pad = (int) (16 * density);
    wrap.setPadding(pad, pad, pad, pad);
    wrap.addView(
        inputLayout,
        new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));

    new LiquidGlassDialogBuilderJava(this)
        .setTitle(R.string.editor_goto_line)
        .setView(wrap)
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(
            android.R.string.ok,
            (dialog, which) -> {
              String value = input.getText().toString().trim();
              if (!value.isEmpty()) {
                try {
                  int line = Integer.parseInt(value);
                  ((EditorFragment) current).jumpToLocation(line - 1, 0);
                } catch (NumberFormatException ignored) {
                }
              }
            })
        .show();
  }

  private void handleIncomingIntent(Intent intent) {
    if (intent == null) return;
    String directPath = intent.getStringExtra("open_file_direct");
    if (directPath != null && !directPath.isEmpty()) {
      openFileDirect(directPath);
      return;
    }
    String action = intent.getAction();
    if (Intent.ACTION_VIEW.equals(action) || Intent.ACTION_EDIT.equals(action)) {
      Uri uri = intent.getData();
      if (uri != null) {
        String path = UriFileImporter.getRealPathFromUri(this, uri);
        if (path != null && new File(path).exists()) {
          openFile(path);
        } else {
          GhostToast.makeText(this, "خطا: فایل معتبر نیست", GhostToast.LENGTH_SHORT).show();
        }
      }
    }
    if (Intent.ACTION_SEND.equals(action)
        && intent.getType() != null
        && "text/plain".equals(intent.getType())) {
      @SuppressWarnings("deprecation")
      Uri sharedUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
      if (sharedUri != null) {
        String path = UriFileImporter.getRealPathFromUri(this, sharedUri);
        if (path != null) openFile(path);
      } else {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) openFile(UriFileImporter.saveTextToCache(this, sharedText));
      }
    }
  }

  private void openFileDirect(String filePath) {
    File file = new File(filePath);
    if (!file.exists()) return;
    for (int i = 0; i < tabsList.size(); i++) {
      if (tabsList.get(i).getFilePath().equals(filePath)) {
        switchToTab(i);
        return;
      }
    }
    openFile(filePath, file.getName());
  }

  public void openFile(String filePath) {
    File file = new File(filePath);
    if (!file.exists()) return;
    openFile(filePath, file.getName());
  }

  public void stepFileTree() {
    String currentPath = getCurrentFilePath();
    if (currentPath == null) {
      GhostToast.makeText(this, "هیچ فایلی باز نیست", GhostToast.LENGTH_SHORT).show();
      return;
    }

    File currentFile = new File(currentPath);
    String rootPath = currentFile.isDirectory() ? currentPath : currentFile.getParent();

    if (rootPath == null) {
      GhostToast.makeText(this, "مسیر نامعتبر است", GhostToast.LENGTH_SHORT).show();
      return;
    }
    FileTreeView tree = new FileTreeView(this);
    tree.setNodePath(rootPath);
    tree.loadTree();

    tree.setClickNode(
        (v, c) -> {
          if (v != null && !v.isFolder()) {
            String filePath = v.getAbsolutePath();
            if (filePath != null) {
              openFile(filePath);
            }
          }
        });

    SideSheetDialog sideSheet = new SideSheetDialog(this);
    sideSheet.setContentView(tree);
    sideSheet.getWindow().setNavigationBarColor(0);
    theme.applySideSheetAndFileTree(sideSheet, tree);
    sideSheet.show();
  }

  private void setupKeyboardListener() {
    View rootView = getWindow().getDecorView();

    binding.symbolBarContainer.hide();
    binding.editorStatusBar.setVisibility(View.VISIBLE);
    keyboardLayoutListener =
        () -> {
          Rect r = new Rect();
          rootView.getWindowVisibleDisplayFrame(r);
          int screenHeight = rootView.getRootView().getHeight();
          int keypadHeight = screenHeight - r.bottom;
          if (binding.editorSearch.isShowing) {
            binding.symbolBarContainer.hide();
            binding.editorStatusBar.setVisibility(View.GONE);
            return;
          }
          if (!settings.isBackgroundZoomMod()) {
            if (keypadHeight > screenHeight * 0.15) {
              binding.backgroundicon.animate().scaleX(1.5f).scaleY(1.5f).setDuration(1000).start();
              binding.symbolBarContainer.show();
              // کیبورد بازه؛ استاتوس‌بار مخفی و نماد‌بار نمایش داده می‌شه
              binding.editorStatusBar.setVisibility(View.GONE);
            } else {
              binding.backgroundicon.animate().scaleX(1.0f).scaleY(1.0f).setDuration(1000).start();
              isShowSys = false;
              binding.symbolBarContainer.hide();
              // کیبورد بسته‌ست؛ استاتوس‌بار نمایش داده و نماد‌بار مخفی می‌شه
              binding.editorStatusBar.setVisibility(View.VISIBLE);
            }
          }
          if (keypadHeight > screenHeight * 0.15) {
            binding.symbolBarContainer.show();
            binding.editorStatusBar.setVisibility(View.GONE);
          } else {
            isShowSys = false;
            binding.symbolBarContainer.hide();
            binding.editorStatusBar.setVisibility(View.VISIBLE);
          }
        };
    rootView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);
  }

  void stepToolbar() {
    toolbarModel.clear();
    toolbarModel.add(new ToolbarModel(R.drawable.ic_git, "git", gitStatus.isGit()));
    toolbarModel.add(new ToolbarModel(R.drawable.ic_split_column, "Item Spilt!"));
    toolbarModel.add(new ToolbarModel(R.drawable.round_account_tree, "file tree"));
    toolbarModel.add(new ToolbarModel(R.drawable.outline_search, "search"));
    toolbarModel.add(new ToolbarModel(R.drawable.outline_undo, "undo"));
    toolbarModel.add(new ToolbarModel(R.drawable.outline_redo, "redo"));
    toolbarModel.add(new ToolbarModel(R.drawable.more_vert, "more"));
    toolbarModel.add(new ToolbarModel(R.drawable.ic_panel, "plugins"));
    if (listAdapter != null) {
      listAdapter.notifyDataSetChanged();
    } else {
      listAdapter =
          new ToolbarListAdapter(
              toolbarModel,
              (view, m, pos) -> {
                switch (pos) {
                  case 0 -> showGitBottomSheet();
                  case 1 -> toggleOrShowSplitPopup(view);
                  case 2 -> stepFileTree();
                  case 3 -> stepSearch();
                  case 4 -> {
                    if (getEditor().canUndo()) getEditor().undo();
                  }
                  case 5 -> {
                    if (getEditor().canRedo()) getEditor().redo();
                  }
                  case 6 -> setupMenuCalltoAction(view);
                  case 7 -> pluginPopupController.show(view);
                }
              },
              EditorActivity.this);
    }
    if (binding.rvtoolbar.getVisibility() != View.VISIBLE) {
      binding.rvtoolbar.setVisibility(View.VISIBLE);
    }
    binding.rvtoolbar.setLayoutManager(
        new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
    binding.rvtoolbar.setAdapter(listAdapter);
    if (listAdapter != null) listAdapter.notifyDataSetChanged();
    binding.rvtoolbar.post(
        () -> {
          if (listAdapter != null) listAdapter.notifyDataSetChanged();
          binding.rvtoolbar.requestLayout();
        });
  }

  private void openPluginPanelAt(int pos) {
    if (pluginPanelHost == null) {
      return;
    }
    int panelIndex = pos - 7;
    List<EditorPanel> panels = pluginPanelHost.getPanels();
    if (panelIndex >= 0 && panelIndex < panels.size()) {
      pluginPanelHost.showPanel(panels.get(panelIndex));
    }
  }

  public void stepSearch() {
    binding.editorSearch.bindEditor(this::getEditor);
    binding.editorSearch.setCallBack(
        new GhostIdeEditorSearch.onViewChange() {
          @Override
          public void onViewShow() {
            binding.fabineditor.hide();
            binding.symbolBarContainer.hide();
            binding.editorStatusBar.setVisibility(View.GONE);
          }

          @Override
          public void onViewHide() {
            binding.fabineditor.show();
          }
        });
    binding.editorSearch.showAndHide();
  }

  private void showGitBottomSheet() {
    String repoPath = gitStatus.findRepositoryPath();
    if (repoPath == null) {
      GhostToast.makeText(this, "هیچ مخزن گیتی در مسیر فایل جاری یافت نشد", GhostToast.LENGTH_LONG)
          .show();
      return;
    }
    GitBottomSheetFragment bottomSheet = GitBottomSheetFragment.newInstance(repoPath);
    bottomSheet.show(getSupportFragmentManager(), "git_bottom_sheet");
  }

  public void setTabDirty(String filePath, boolean dirty) {
    if (filePath == null) return;
    for (int i = 0; i < tabsList.size(); i++) {
      TabModel tab = tabsList.get(i);
      if (filePath.equals(tab.getFilePath())) {
        tab.setHasStar(dirty);
        TabLayout.Tab layoutTab = binding.tab.getTabAt(i);
        if (layoutTab != null && layoutTab.getCustomView() instanceof TabCustomView) {
          ((TabCustomView) layoutTab.getCustomView()).setHasStar(dirty);
        }
        if (i == binding.viewPager.getCurrentItem()) {
          binding.editorStatusBar.setDirty(dirty);
        }
        return;
      }
    }
  }

  public void setTabError(String filePath, boolean hasError) {
    if (filePath == null) return;
    for (int i = 0; i < tabsList.size(); i++) {
      TabModel tab = tabsList.get(i);
      if (filePath.equals(tab.getFilePath())) {
        tab.setHasError(hasError);
        TabLayout.Tab layoutTab = binding.tab.getTabAt(i);
        if (layoutTab != null && layoutTab.getCustomView() instanceof TabCustomView) {
          ((TabCustomView) layoutTab.getCustomView()).setHasErrors(hasError);
        }
        if (binding.splitPaneRoot != null) {
          binding.splitPaneRoot.notifyTabError(filePath, hasError);
        }
        return;
      }
    }
  }

  void setupMenuCalltoAction(View v) {
    List<EditorGlassMenu.GlassMenuItem> items = new ArrayList<>();
    items.add(new EditorGlassMenu.GlassMenuItem(getString(R.string.saveitemthis), R.drawable.save));
    items.add(new EditorGlassMenu.GlassMenuItem(getString(R.string.saveitemall), R.drawable.save));
    items.add(new EditorGlassMenu.GlassMenuItem(getString(R.string.command_palette_title), R.drawable.outline_search));
    items.add(new EditorGlassMenu.GlassMenuItem(getString(R.string.outline_title), R.drawable.round_account_tree));
    items.add(new EditorGlassMenu.GlassMenuItem(getString(R.string.snippets_title), R.drawable.ic_edit));
    items.add(
        new EditorGlassMenu.GlassMenuItem(
            getString(R.string.webcolor), R.drawable.outline_color_lens));
    EditorGlassMenu.showGlassMenu(
        this,
        v,
        items,
        (pos, title) -> {
          switch (pos) {
            case 0 -> saveCurrentTab();
            case 1 -> saveAllTabs();
            case 2 -> showCommandPaletteSheet();
            case 3 -> showOutlineSheet();
            case 4 -> showSnippetManagerSheet();
            case 5 -> {
              var colors = new ColorPickerBottomSheet();
              colors.show(getSupportFragmentManager(), "");
            }
          }
        });
  }

  private void showCommandPaletteSheet() {
    if (getEditor() == null) return;
    CommandPaletteSheet sheet = CommandPaletteSheet.newInstance();
    sheet.setCommands(CommandPaletteRegistry.buildCommands(this));
    sheet.setOnCommandListener(
        item -> {
          if (item.id() != null) {
            CommandPaletteRegistry.execute(this, item.id());
          }
        });
    sheet.show(getSupportFragmentManager(), CommandPaletteSheet.TAG);
  }

  private void showOutlineSheet() {
    String currentPath = getCurrentFilePath();
    if (currentPath == null || getEditor() == null) return;
    OutlineSheet sheet = OutlineSheet.newInstance(currentPath);
    sheet.setEditorSupplier(() -> getEditor());
    sheet.setOnSymbolClickListener(
        (line, column) -> {
          IdeEditor editor = getEditor();
          if (editor != null) {
            editor.gotoLine(line);
          }
        });
    sheet.show(getSupportFragmentManager(), OutlineSheet.TAG);
  }

  private void showSnippetManagerSheet() {
    if (getEditor() == null) return;
    SnippetManagerSheet sheet = new SnippetManagerSheet();
    sheet.setOnInsertSnippetListener(
        entry -> {
          IdeEditor editor = getEditor();
          if (editor == null) return;
          try {
            CodeSnippet snippet = CodeSnippetParser.parse(entry.body);
            editor.getSnippetController()
                .startSnippet(editor.getCursor().getLeft(), snippet, "");
          } catch (Exception ignored) {
            editor.commitText(entry.body);
          }
        });
    sheet.show(getSupportFragmentManager(), SnippetManagerSheet.TAG);
  }

  private void toggleOrShowSplitPopup(View anchor) {
    if (isSplitViewActive) {
      exitSplitView();
      return;
    }
    if (splitLayoutPopup == null) {
      splitLayoutPopup = new SplitLayoutPopup(this);
      splitLayoutPopup.setOnSplitChangeListener(
          new SplitLayoutPopup.OnSplitChangeListener() {
            @Override
            public void onApplySplit(int rows, int cols) {
              applySplitView(rows, cols);
            }

            @Override
            public void onExitSplit() {
              exitSplitView();
            }
          });
    }
    splitLayoutPopup.setCurrentState(isSplitViewActive, lastSplitRows, lastSplitCols);
    splitLayoutPopup.show(anchor);
  }

  private void applySplitView(int rows, int cols) {
    binding.splitPaneRoot.applySplit(rows, cols, tabsList);
    isSplitViewActive = binding.splitPaneRoot.isSplit();
    if (isSplitViewActive) {
      lastSplitRows = rows;
      lastSplitCols = cols;
    }
    saveSplitState();
  }

  private void exitSplitView() {
    binding.splitPaneRoot.exitSplit();
    isSplitViewActive = false;
    saveSplitState();
  }

  private void saveSplitState() {
    prefs
        .edit()
        .putBoolean(KEY_SPLIT_ACTIVE, isSplitViewActive)
        .putInt(KEY_SPLIT_ROWS, lastSplitRows)
        .putInt(KEY_SPLIT_COLS, lastSplitCols)
        .apply();
  }

  /**
   * اسپیلت رو از SharedPreferences برمیگردونه؛ فقط با خروج دستی کاربر (exitSplitView) بسته میشه، نه
   * با خروج/رفتن به activity دیگه یا کشته‌شدن پروسه.
   */
  private void restoreSplitState() {
    boolean wasSplitActive = prefs.getBoolean(KEY_SPLIT_ACTIVE, false);
    if (!wasSplitActive) return;
    int savedRows = prefs.getInt(KEY_SPLIT_ROWS, lastSplitRows);
    int savedCols = prefs.getInt(KEY_SPLIT_COLS, lastSplitCols);
    binding.splitPaneRoot.post(() -> applySplitView(savedRows, savedCols));
  }

  private void setupViewPager() {
    adapter = new EditorPagerAdapter(this, new ArrayList<>());
    binding.viewPager.setAdapter(adapter);
    binding.viewPager.setUserInputEnabled(false);
    binding.viewPager.setOffscreenPageLimit(RecyclerView.NO_POSITION);
  }

  private void setupTabLayout() {
    if (tabMediator != null) tabMediator.detach();
    tabMediator =
        new TabLayoutMediator(
            binding.tab,
            binding.viewPager,
            (tab, position) -> {
              if (position < tabsList.size()) {
                TabCustomView customView = new TabCustomView(this);
                customView.bind(tabsList.get(position));
                customView.setGitChanged(gitStatus.isFileGitChanged(tabsList.get(position).getFilePath()));
                tab.setCustomView(customView);
              }
            });
    tabMediator.attach();
    binding.tab.addOnTabSelectedListener(
        new TabLayout.OnTabSelectedListener() {
          @Override
          public void onTabSelected(TabLayout.Tab tab) {
            int position = tab.getPosition();
            if (binding.viewPager.getCurrentItem() != position)
              binding.viewPager.setCurrentItem(position, false);
            saveCurrentPosition(position);
            updateLanguageStatus(position);
            theme.applyTabLayout(binding.tab, getCurrentFilePath());
          }

          @Override
          public void onTabUnselected(TabLayout.Tab tab) {}

          @Override
          public void onTabReselected(TabLayout.Tab tab) {
            showPopupMenu(tab.view, tab.getPosition());
          }
        });
    binding.viewPager.registerOnPageChangeCallback(
        new ViewPager2.OnPageChangeCallback() {
          @Override
          public void onPageSelected(int position) {
            super.onPageSelected(position);
            TabLayout.Tab tab = binding.tab.getTabAt(position);
            if (tab != null && !tab.isSelected()) tab.select();
            saveCurrentPosition(position);
            updateLanguageStatus(position);
            theme.applyTabLayout(binding.tab, getCurrentFilePath());
          }
        });
  }

  private void loadSavedTabs() {
    String json = prefs.getString(KEY_TABS, "");
    if (!json.isEmpty()) {
      try {
        Type type = new TypeToken<List<TabModel>>() {}.getType();
        List<TabModel> saved = gson.fromJson(json, type);
        if (saved != null) tabsList = saved;
      } catch (Exception e) {
        tabsList = new ArrayList<>();
      }
    } else {
      tabsList = new ArrayList<>();
    }
    adapter.setTabs(new ArrayList<>(tabsList));
    if (binding.splitPaneRoot != null) binding.splitPaneRoot.notifyTabsChanged(tabsList);
    int savedPosition = 0;
    String posStr = prefs.getString(KEY_POSITION, "0");
    try {
      savedPosition = Integer.parseInt(posStr);
    } catch (NumberFormatException e) {
      savedPosition = 0;
    }
    if (!tabsList.isEmpty() && savedPosition >= 0 && savedPosition < tabsList.size()) {
      switchToTab(savedPosition);
      binding.tab.setScrollPosition(savedPosition, 0f, true);
    }
    binding.splitPaneRoot.initialize(this, paneActionListener);
    binding.splitPaneRoot.setOnActivePaneChangedListener(pane -> activePane = pane);
    restoreSplitState();
  }

  private void saveCurrentPosition(int position) {
    prefs.edit().putString(KEY_POSITION, String.valueOf(position)).apply();
  }

  private void switchToTab(int position) {
    if (adapter == null || adapter.getItemCount() == 0) return;
    int safe = Math.max(0, Math.min(position, adapter.getItemCount() - 1));
    binding.viewPager.post(
        () -> {
          if (adapter == null || adapter.getItemCount() == 0) return;
          int s = Math.max(0, Math.min(safe, adapter.getItemCount() - 1));
          binding.viewPager.setCurrentItem(s, false);
          saveCurrentPosition(s);
          updateLanguageStatus(s);
          TabLayout.Tab layoutTab = binding.tab.getTabAt(s);
          if (layoutTab != null && !layoutTab.isSelected()) layoutTab.select();
        });
  }

  private void saveTabs() {
    String json = gson.toJson(tabsList);
    prefs.edit().putString(KEY_TABS, json).apply();
  }

  private void openFile(String path, String name) {
    openFile(path, name, false);
  }

  private void openFile(String path, String name, boolean readOnly) {
    for (int i = 0; i < tabsList.size(); i++) {
      if (tabsList.get(i).getFilePath().equals(path)) {
        switchToTab(i);
        return;
      }
    }
    tabsList.add(new TabModel(path, name, readOnly));
    adapter.setTabs(new ArrayList<>(tabsList));
    if (binding.splitPaneRoot != null) binding.splitPaneRoot.notifyTabsChanged(tabsList);
    saveTabs();
    int newPos = tabsList.size() - 1;
    switchToTab(newPos);
    saveCurrentPosition(newPos);
    updateLanguageStatus(newPos);
    String ext = "";
    int dot = path.lastIndexOf('.');
    if (dot != -1) ext = path.substring(dot + 1);
    PluginManager.getInstance().setCurrentEditorActivity(this, getEditor(), path, ext);
    IdeEvents.post(FileEvent.opened(path));
    gitStatus.refresh();
  }

  private void updateLanguageStatus(int position) {
    if (tabsList == null || position < 0 || position >= tabsList.size()) return;
    String filePath = tabsList.get(position).getFilePath();
    String lang = EditorFileStats.getLanguageFromPath(filePath);
    binding.editorStatusBar.setLanguageText(lang.isEmpty() ? "Text" : lang);
    binding.editorStatusBar.setLinesText(EditorFileStats.formatFileStats(this, filePath));
    binding.editorStatusBar.setDirty(tabsList.get(position).getHasStar());
    refreshLspStatusIndicator();
  }

  /**
   * وضعیتِ اتصالِ LSP مربوط به تبِ الان دیده شده رو می‌خونه (از IdeEditor.getLspStatus()) و نقطه‌ی
   * گِردِ داخل editorStatusBar رو بر همون اساس رنگ/متنش رو آپدیت می‌کنه.
   *
   * <p>هم از updateLanguageStatus() (موقع تعویض تب) صدا زده می‌شه، هم از lspStatusPollRunnable (هر
   * ۱.۵ ثانیه، تا وضعیت‌هایی مثل CONNECTING → CONNECTED که وسط کار عوض می‌شن هم دیده بشن).
   */
  private void refreshLspStatusIndicator() {
    if (binding == null) return;
    IdeEditor editor = getEditor();
    if (editor == null) {
      binding.editorStatusBar.setStatusIndicator(EditorStatusBar.StatusIndicator.IDLE, "—");
      return;
    }
    LspEditorStatus status = editor.getLspStatus();
    if (status == null) {

      binding.editorStatusBar.setStatusIndicator(EditorStatusBar.StatusIndicator.IDLE, "—");
      return;
    }
    switch (status) {
      case CONNECTED:
        binding.editorStatusBar.setStatusIndicator(
            EditorStatusBar.StatusIndicator.CONNECTED, "Connected");
        break;
      case CONNECTING:
        binding.editorStatusBar.setStatusIndicator(
            EditorStatusBar.StatusIndicator.CONNECTING, "Connecting…");
        break;
      case DISCONNECTED:
        binding.editorStatusBar.setStatusIndicator(
            EditorStatusBar.StatusIndicator.ERROR, "Disconnected");
        break;
      case IDLE:
      default:
        binding.editorStatusBar.setStatusIndicator(EditorStatusBar.StatusIndicator.IDLE, "Idle");
        break;
    }
    refreshBreadcrumbs();
  }

  private void refreshBreadcrumbs() {
    if (binding == null || adapter == null) return;
    if (activePane != null) {
      breadcrumbAdapter.setItems(new ArrayList<>());
      return;
    }
    if (binding.viewPager == null || adapter.getItemCount() == 0) {
      breadcrumbAdapter.setItems(new ArrayList<>());
      return;
    }
    int currentPos = binding.viewPager.getCurrentItem();
    Fragment currentFragment = adapter.getFragmentAtPosition(currentPos, this);
    if (currentFragment instanceof EditorFragment) {
      ((EditorFragment) currentFragment).scheduleBreadcrumbRefresh();
    } else {
      breadcrumbAdapter.setItems(new ArrayList<>());
    }
  }

  public void showBreadcrumbs(EditorFragment source, List<BreadcrumbItem> items) {
    if (binding == null || activePane != null) return;
    if (getEditor() != source.getEditor()) return;
    breadcrumbAdapter.setItems(items);
  }

  private void closeTab(int position) {
    if (position >= 0 && position < tabsList.size()) {
      if (tabsList.get(position).isPinned()) return;
      IdeEvents.post(FileEvent.closed(tabsList.get(position).getFilePath()));
      tabsList.remove(position);
      adapter.setTabs(new ArrayList<>(tabsList));
      if (binding.splitPaneRoot != null) binding.splitPaneRoot.notifyTabsChanged(tabsList);
      saveTabs();
      if (tabsList.isEmpty()) {
        finish();
        return;
      }
      int newPos = Math.min(position, tabsList.size() - 1);
      switchToTab(newPos);
    }
  }

  private void closeOtherTabs(int position) {
    if (position < 0 || position >= tabsList.size()) return;
    TabModel current = tabsList.get(position);
    List<TabModel> newList = new ArrayList<>();
    newList.add(current);
    for (int i = 0; i < tabsList.size(); i++) {
      if (i != position && tabsList.get(i).isPinned()) newList.add(tabsList.get(i));
      else if (i != position) IdeEvents.post(FileEvent.closed(tabsList.get(i).getFilePath()));
    }
    tabsList = newList;
    adapter.setTabs(new ArrayList<>(tabsList));

    if (binding.splitPaneRoot != null) binding.splitPaneRoot.notifyTabsChanged(tabsList);
    saveTabs();
    switchToTab(0);
  }

  public void closeAllTabs() {
    List<TabModel> pinned = new ArrayList<>();
    for (TabModel tab : tabsList) {
      if (tab.isPinned()) pinned.add(tab);
      else IdeEvents.post(FileEvent.closed(tab.getFilePath()));
    }
    tabsList = pinned;
    adapter.setTabs(new ArrayList<>(tabsList));
    if (binding.splitPaneRoot != null) binding.splitPaneRoot.notifyTabsChanged(tabsList);
    saveTabs();
    if (tabsList.isEmpty()) finish();
    else {
      switchToTab(0);
    }
  }

  private void togglePin(int position) {
    if (position >= 0 && position < tabsList.size()) {
      TabModel tab = tabsList.get(position);
      tab.setPinned(!tab.isPinned());
      adapter.setTabs(new ArrayList<>(tabsList));
      if (binding.splitPaneRoot != null) binding.splitPaneRoot.notifyTabsChanged(tabsList);
      saveTabs();
      TabLayout.Tab layoutTab = binding.tab.getTabAt(position);
      if (layoutTab != null && layoutTab.getCustomView() instanceof TabCustomView) {
        ((TabCustomView) layoutTab.getCustomView()).bind(tab);
      }
    }
  }

  public String getCurrentFilePath() {
    if (activePane != null) {
      return activePane.getCurrentFilePath();
    }
    int currentPos = binding.viewPager.getCurrentItem();
    if (currentPos >= 0 && currentPos < tabsList.size())
      return tabsList.get(currentPos).getFilePath();
    return null;
  }

  @Override
  public List<TabModel> getTabs() {
    return tabsList;
  }

  @Override
  public TabLayout getTabLayout() {
    return binding.tab;
  }

  @Override
  public SplitPaneContainerLayout getSplitPaneRoot() {
    return binding.splitPaneRoot;
  }

  private void setupFAB() {
    binding.fabineditor.setOnClickListener(
        v -> {
          String currentFilePath = getCurrentFilePath();
          if (currentFilePath == null) return;
          if (currentFilePath.endsWith(".html")) {
            Intent intent = new Intent(EditorActivity.this, WebViewActivity.class);
            intent.putExtra("keyweb", currentFilePath);
            startActivity(intent);
          } else if (currentFilePath.endsWith(".md")) {
            var bl = new Bundle();
            bl.putString("md", currentFilePath);
            var mdview = new MarkDownPreview();
            mdview.setArguments(bl);
            mdview.show(getSupportFragmentManager(), "MarkDownPreview");
          } else {
            CodeRunnerHost runner = GlobalRegistry.services().get(IdeHostServices.CODE_RUNNER_HOST);
            if (runner != null) {
              runner.runFile(currentFilePath, settings.isTerminalFragment());
            } else {
              new CodeRuner(EditorActivity.this)
                  .bindof(currentFilePath, settings.isTerminalFragment());
            }
          }
        });
  }

  private void showPopupMenu(View anchor, int position) {
    List<EditorGlassMenu.GlassMenuItem> items = new ArrayList<>();
    items.add(new EditorGlassMenu.GlassMenuItem(getString(R.string.close)));
    items.add(new EditorGlassMenu.GlassMenuItem(getString(R.string.closeother)));
    items.add(new EditorGlassMenu.GlassMenuItem(getString(R.string.closeall)));
    items.add(new EditorGlassMenu.GlassMenuItem(getString(R.string.pin)));
    EditorGlassMenu.showGlassMenu(
        this,
        anchor,
        items,
        (pos, title) -> {
          switch (pos) {
            case 0 -> closeTab(position);
            case 1 -> closeOtherTabs(position);
            case 2 -> closeAllTabs();
            case 3 -> togglePin(position);
          }
        });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
      String path = data.getStringExtra("selected_file_path");
      String name = data.getStringExtra("selected_file_name");
      if (path != null) openFile(path, name);
    }
  }

  public void saveAllTabs() {
    if (adapter == null || adapter.getItemCount() == 0) {
      GhostToast.makeText(this, "هیچ فایلی باز نیست", GhostToast.LENGTH_SHORT).show();
      return;
    }
    int savedCount = 0;
    for (Fragment fragment : getSupportFragmentManager().getFragments()) {
      if (fragment instanceof EditorFragment) {
        ((EditorFragment) fragment).saveCurrentFile();
        savedCount++;
      }
    }
    List<String> activeFragPaths = new ArrayList<>();
    for (Fragment fragment : getSupportFragmentManager().getFragments()) {
      if (fragment instanceof EditorFragment && fragment.getArguments() != null) {
        String p = fragment.getArguments().getString("file_path");
        if (p != null) activeFragPaths.add(p);
      }
    }
    for (TabModel tab : tabsList) {
      if (!activeFragPaths.contains(tab.getFilePath())) {

        savedCount++;
      }
    }
    GhostToast.makeText(
            this, savedCount + getString(R.string.editorac_savefile), GhostToast.LENGTH_SHORT)
        .show();
    gitStatus.refresh();
  }

  public void saveCurrentTab() {
    if (binding.viewPager == null || adapter == null || adapter.getItemCount() == 0) {
      GhostToast.makeText(this, getString(R.string.editorac_notopenfile), GhostToast.LENGTH_SHORT)
          .show();
      return;
    }
    int currentPos = binding.viewPager.getCurrentItem();
    Fragment currentFragment = adapter.getFragmentAtPosition(currentPos, this);
    if (currentFragment instanceof EditorFragment) {
      ((EditorFragment) currentFragment).saveCurrentFile();
      GhostToast.makeText(this, getString(R.string.editorac_wassaved), GhostToast.LENGTH_SHORT)
          .show();
      gitStatus.refresh();
    } else {
      GhostToast.makeText(this, getString(R.string.editorac_errorfargment), GhostToast.LENGTH_SHORT)
          .show();
    }
  }

  public IdeEditor getEditor() {
    if (activePane != null) {
      return activePane.getEditor();
    }
    if (binding.viewPager == null || adapter == null || adapter.getItemCount() == 0) {
      return null;
    }
    int currentPos = binding.viewPager.getCurrentItem();
    Fragment currentFragment = adapter.getFragmentAtPosition(currentPos, this);
    if (currentFragment instanceof EditorFragment)
      return ((EditorFragment) currentFragment).getEditor();
    return null;
  }
}
