package ir.hanzodev1375.ghostide.terminal.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.termux.terminal.TerminalSession;
import ir.hanzodev1375.components.sheet.customitemsheet.ui.DialogCompat;
import ir.hanzodev1375.components.sheet.customitemsheet.ui.LiquidGlassDialogBuilderJava;
import ir.hanzodev1375.components.views.GhostToast;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.activity.BaseCompat;
import ir.hanzodev1375.ghostide.codeeditors.setting.PreferencesUtils;
import ir.hanzodev1375.ghostide.databinding.ActivityTerminalBinding;
import ir.hanzodev1375.ghostide.terminal.DebianBootstrap;
import ir.hanzodev1375.ghostide.terminal.DebianInstaller;
import ir.hanzodev1375.ghostide.terminal.TerminalBackupManager;
import ir.hanzodev1375.ghostide.terminal.TerminalInputDock;
import ir.hanzodev1375.ghostide.terminal.TerminalSessionFragment;
import ir.hanzodev1375.ghostide.terminal.TerminalTab;
import ir.hanzodev1375.ghostide.terminal.TerminalViewModel;
import ir.hanzodev1375.ghostide.utils.ObjectUtil;
import ir.hanzodev1375.ghostide.utils.StorageUtils;
import ir.theme.GhostTheme;
import ir.theme.M3Theme;
import ir.theme.ThemeManager;
import ir.theme.ThemeUtils;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TerminalActivity extends BaseCompat implements TerminalViewModel.SessionListener {

  public static final String EXTRA_WORKING_DIR = "working_dir";
  public static final String EXTRA_COMMAND = "command";
  private static final String LOG_TAG = "TerminalActivity";

  private ActivityTerminalBinding b;
  private TerminalViewModel viewModel;
  private TerminalInputDock inputDock;
  private SessionPagerAdapter pagerAdapter;
  private int defaultKeyBackgroundColor;
  private int defaultKeyTextColor;

  private View initOverlay;
  private TextView initOverlayStatus;
  private AlertDialog installDialog;
  private TextView installStatusText;
  private ProgressBar installProgressBar;
  private DebianInstaller.InstallListener installListener;

  private AlertDialog transferDialog;
  private TextView transferStatusText;
  private TextView transferPathText;
  private ProgressBar transferProgressBar;
  private boolean transferIsBackup;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    b = ActivityTerminalBinding.inflate(getLayoutInflater());
    setContentView(b.getRoot());
    M3Theme.apply(b.getRoot());
    viewModel = new ViewModelProvider(this).get(TerminalViewModel.class);
    viewModel.setActivityListener(this);
    setupToolbar();
    setupEdgeToEdgeInsets();
    setupViewPager();
    setupExtraKeys();
    applyJsonTheme();
    setupInputDock();
    setupBackHandler();
    setupBackgroundBlur();
    setupInitOverlay();
    maybeRequestNotificationPermission();
  }

  @Override
  protected void onStart() {
    super.onStart();
    viewModel.bindService();
    viewModel.setServiceListener();
    maybeStartInstall();
  }

  @Override
  protected void onStop() {
    viewModel.unbindService();
    DebianInstaller.detach(installListener);
    super.onStop();
  }

  private void maybeStartInstall() {
    if (DebianBootstrap.isInstalled(this)) return;
    if (!DebianInstaller.isInstalling()) {
      startDebianInstall();
    } else {
      attachToRunningInstall();
    }
  }

  private void maybeRequestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= 33) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
          != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(
            this, new String[] {Manifest.permission.POST_NOTIFICATIONS}, 4821);
      }
    }
  }

  // ─── ViewPager + TabLayout ───────────────────────────────────────────

  private void setupViewPager() {
    pagerAdapter = new SessionPagerAdapter(this);
    b.viewPager.setAdapter(pagerAdapter);
    b.viewPager.setOffscreenPageLimit(4);
    b.viewPager.setUserInputEnabled(false);
    TabLayoutMediator mediator =
        new TabLayoutMediator(b.tabLayout, b.viewPager, true, this::bindTab);
    mediator.attach();

    b.viewPager.registerOnPageChangeCallback(
        new ViewPager2.OnPageChangeCallback() {
          @Override
          public void onPageSelected(int position) {
            viewModel.switchToTab(position);
          }
        });

    b.tabLayout.addOnTabSelectedListener(
        new TabLayout.OnTabSelectedListener() {
          @Override
          public void onTabSelected(TabLayout.Tab tab) {}

          @Override
          public void onTabUnselected(TabLayout.Tab tab) {}

          @Override
          public void onTabReselected(TabLayout.Tab tab) {
            showTabMenu(tab.view, tab.getPosition());
          }
        });

    b.btnNewSession.setOnClickListener(this::showNewSessionMenu);
  }

  private void bindTab(TabLayout.Tab tab, int position) {
    List<TerminalTab> tabs = viewModel.getSessionList();
    tab.setText(tabs.get(position).getDisplayTitle());
    tab.view.setOnLongClickListener(
        v -> {
          confirmCloseTab(position);
          return true;
        });
  }

  private void refreshTabs() {
    pagerAdapter.notifyDataSetChanged();
    for (int i = 0; i < viewModel.getSessionList().size(); i++) {
      TabLayout.Tab tab = b.tabLayout.getTabAt(i);
      if (tab != null) tab.setText(viewModel.getSessionList().get(i).getDisplayTitle());
    }
  }

  private void confirmCloseTab(int position) {
    List<TerminalTab> tabs = viewModel.getSessionList();
    if (position < 0 || position >= tabs.size()) return;
    new DialogCompat(this)
        .setTitle(getString(R.string.terminal_close_tab))
        .setMessage(getString(R.string.terminal_close_tab_confirm))
        .setPositiveButton(
            getString(R.string.terminal_action_close),
            (dialog, which) -> viewModel.removeSession(position))
        .setNegativeButton(getString(R.string.terminal_action_cancel), null)
        .show();
  }

  private void showTabMenu(View anchor, int position) {
    List<String> items =
        Arrays.asList(
            getString(R.string.close),
            getString(R.string.closeother),
            getString(R.string.closeall));
    ObjectUtil.showGlassMenu(
        this,
        anchor,
        items,
        (index, title) -> {
          switch (index) {
            case 0 -> closeTab(position);
            case 1 -> closeOtherTabs(position);
            case 2 -> closeAllTabs();
          }
        });
  }

  private void closeTab(int position) {
    viewModel.removeSession(position);
  }

  private void closeOtherTabs(int keep) {
    for (int i = viewModel.getSessionList().size() - 1; i >= 0; i--) {
      if (i != keep) viewModel.removeSession(i);
    }
  }

  private void closeAllTabs() {
    for (int i = viewModel.getSessionList().size() - 1; i >= 0; i--) {
      viewModel.removeSession(i);
    }
  }

  // ─── SessionListener ─────────────────────────────────────────────────

  @Override
  public void onServiceConnected() {
    animateTerminalReveal();
    boolean runRequested = hasCommandExtra();
    if (viewModel.getSessionList().isEmpty() || runRequested) {
      // اگه سشنی موجود نیست، یا این اکتیویتی برای اجرای کد (EXTRA_COMMAND) اومده، همیشه یه
      // تبِ تازه باز کن — نه این که تبِ قبلی رو دوباره لود کنی. دستورِ کد رو هم
      // consumeCommandExtra() (بعد از onSessionAdded) روی همین تبِ تازه می‌نویسه.
      if (DebianBootstrap.isInstalled(this)) {
        addNewDebianSession();
      } else {
        addNewShellSession();
      }
    } else {
      int idx =
          viewModel.getCurrentTabIndex().getValue() != null
              ? viewModel.getCurrentTabIndex().getValue()
              : 0;
      viewModel.switchToTab(idx);
      refreshTabs();
      b.viewPager.setCurrentItem(idx, true);
    }
  }

  /** وقتی اکتیویتی دوباره به سرویس وصل می‌شه، ترمینال و تب‌ها با فیِد + اسلاید ظاهر می‌شن. */
  private void animateTerminalReveal() {
    boolean animate = getAnimationManager() != null && getAnimationManager().areAnimationsEnabled();
    animateIn(b.viewPager, animate);
    animateIn(b.tabLayout, animate);
  }

  private void animateIn(View v, boolean animate) {
    if (v == null) return;
    if (!animate) {
      v.setAlpha(1f);
      v.setTranslationY(0f);
      return;
    }
    v.setAlpha(0f);
    v.setTranslationY(18f);
    v.animate()
        .alpha(1f)
        .translationY(0f)
        .setDuration(300)
        .setInterpolator(new DecelerateInterpolator())
        .start();
  }

  @Override
  public void onSessionAdded(int index) {
    refreshTabs();
    b.viewPager.setCurrentItem(index, true);
    consumeCommandExtra();
  }

  @Override
  public void onSessionRemoved(boolean empty) {
    if (empty) {
      finish();
      return;
    }
    refreshTabs();
  }

  @Override
  public void onTitleChanged(int index) {
    TabLayout.Tab tab = b.tabLayout.getTabAt(index);
    if (tab != null) tab.setText(viewModel.getSessionList().get(index).getDisplayTitle());
  }

  @Override
  public void onServiceLost() {
    GhostToast.makeText(this, getString(R.string.terminal_service_lost), GhostToast.LENGTH_SHORT)
        .show();
    finish();
  }

  private void consumeCommandExtra() {
    String command = getIntent().getStringExtra(EXTRA_COMMAND);
    if (command == null || command.isEmpty()) return;
    getIntent().removeExtra(EXTRA_COMMAND);
    TerminalSession session = viewModel.getCurrentSession();
    if (session != null) viewModel.writeCommandWhenReady(session, command);
  }

  private boolean hasCommandExtra() {
    String command = getIntent().getStringExtra(EXTRA_COMMAND);
    return command != null && !command.isEmpty();
  }

  // ─── InputDock ───────────────────────────────────────────────────────

  private void setupInputDock() {
    inputDock =
        new TerminalInputDock(
            b.inputDock,
            b.dockPages,
            b.extraKeysScroll,
            b.commandInputRow,
            b.commandInput,
            b.commandInputLayout,
            b.dragHandle,
            b.handleChevron,
            this::currentSession);
    inputDock.attach();
    inputDock.attachKeyboardWatcher(getWindow().getDecorView());
  }

  public TerminalInputDock getInputDock() {
    return inputDock;
  }

  @Nullable
  private TerminalSession currentSession() {
    return viewModel.getCurrentSession();
  }

  @Nullable
  private TerminalSessionFragment getCurrentFragment() {
    int position = b.viewPager.getCurrentItem();
    List<TerminalTab> tabs = viewModel.getSessionList();
    if (position < 0 || position >= tabs.size()) return null;
    long itemId = tabs.get(position).id;
    String tag = "f" + itemId;
    Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
    return fragment instanceof TerminalSessionFragment ? (TerminalSessionFragment) fragment : null;
  }

  // ─── Edge to edge ────────────────────────────────────────────────────

  private void setupEdgeToEdgeInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(
        b.coordinator,
        (v, insets) -> {
          Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
          Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
          b.toolbar.setPadding(
              b.toolbar.getPaddingLeft(),
              systemBars.top,
              b.toolbar.getPaddingRight(),
              b.toolbar.getPaddingBottom());
          b.inputDock.setPadding(
              b.inputDock.getPaddingLeft(),
              b.inputDock.getPaddingTop(),
              b.inputDock.getPaddingRight(),
              Math.max(systemBars.bottom, ime.bottom));
          return insets;
        });
  }

  // ─── Toolbar ─────────────────────────────────────────────────────────

  private void setupToolbar() {
    setSupportActionBar(b.toolbar);
    if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    b.toolbar.setNavigationOnClickListener(v -> finish());
    b.btnMoreMenu.setOnClickListener(this::showMoreMenu);
    styleToolbarChrome();
  }

  private void styleToolbarChrome() {
    Integer onSurface = M3Theme.onSurface();
    if (onSurface == null) return;
    b.toolbar.setTitleTextColor(onSurface);
    b.toolbar.setSubtitleTextColor(onSurface);
    b.toolbar.setNavigationIconTint(onSurface);
    tintIcon(b.btnMoreMenu, onSurface);
    tintIcon(b.btnNewSession, onSurface);
  }

  private void tintIcon(ImageButton button, int color) {
    Drawable drawable = button.getDrawable();
    if (drawable == null) return;
    DrawableCompat.setTint(drawable.mutate(), color);
  }

  private void showMoreMenu(View anchor) {
    List<String> items = new ArrayList<>();
    items.add(
        getString(
            DebianBootstrap.isInstalled(this)
                ? R.string.terminal_remove_debian
                : R.string.terminal_install_debian));
    items.add(getString(R.string.terminal_backup_menu));
    items.add(getString(R.string.terminal_restore_menu));
    ObjectUtil.showGlassMenu(
        this,
        anchor,
        items,
        (index, title) -> {
          switch (index) {
            case 0 -> {
              if (DebianBootstrap.isInstalled(this)) {
                confirmAndRemoveDebian();
              } else {
                startDebianInstall();
              }
            }
            case 1 -> startTerminalBackup();
            case 2 -> showRestorePicker();
            default -> {
            }
          }
        });
  }

  private void showNewSessionMenu(View anchor) {
    if (!DebianBootstrap.isInstalled(this)) {
      GhostToast.makeText(
              this,
              getString(
                  R.string.terminal_debian_rootfs_not_found_fallback,
                  DebianBootstrap.getRootfsDir(this).getAbsolutePath()),
              GhostToast.LENGTH_LONG)
          .show();
      viewModel.addSession(null);
      return;
    }
    List<String> items = Arrays.asList("Shell", "Debian");
    ObjectUtil.showGlassMenu(
        this,
        anchor,
        items,
        (index, title) -> {
          if (index == 1) {
            addNewDebianSession();
          } else {
            String workingDir = getIntent().getStringExtra(EXTRA_WORKING_DIR);
            viewModel.addSession(workingDir);
          }
        });
  }

  // ─── Extra keys ──────────────────────────────────────────────────────

  private void setupExtraKeys() {
    int tonalBg = fallback(M3Theme.secondaryContainer(), fallback(M3Theme.secondary(), 0));
    int tonalFg = fallback(M3Theme.onSecondaryContainer(), fallback(M3Theme.onSecondary(), 0));
    defaultKeyBackgroundColor = tonalBg;
    defaultKeyTextColor = tonalFg;

    Button[] keys = {
      b.keyEsc, b.keyTab, b.keyCtrl, b.keyAlt,
      b.keyUp, b.keyDown, b.keyLeft, b.keyRight,
      b.keySlash, b.keyDash, b.keyPipe
    };
    for (Button key : keys) {
      key.setBackgroundTintList(ColorStateList.valueOf(tonalBg));
      key.setTextColor(tonalFg);
    }

    b.keyEsc.setOnClickListener(v -> sendKeyEvent(KeyEvent.KEYCODE_ESCAPE));
    b.keyTab.setOnClickListener(v -> sendKeyEvent(KeyEvent.KEYCODE_TAB));
    b.keyUp.setOnClickListener(v -> sendKeyEvent(KeyEvent.KEYCODE_DPAD_UP));
    b.keyDown.setOnClickListener(v -> sendKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN));
    b.keyLeft.setOnClickListener(v -> sendKeyEvent(KeyEvent.KEYCODE_DPAD_LEFT));
    b.keyRight.setOnClickListener(v -> sendKeyEvent(KeyEvent.KEYCODE_DPAD_RIGHT));
    b.keySlash.setOnClickListener(v -> typeText("/"));
    b.keyDash.setOnClickListener(v -> typeText("-"));
    b.keyPipe.setOnClickListener(v -> typeText("|"));
    b.keyCtrl.setOnClickListener(
        v -> {
          viewModel.toggleCtrl();
          updateModifierButtonStyle(b.keyCtrl, viewModel.isCtrlToggled());
        });
    b.keyAlt.setOnClickListener(
        v -> {
          viewModel.toggleAlt();
          updateModifierButtonStyle(b.keyAlt, viewModel.isAltToggled());
        });
  }

  private void updateModifierButtonStyle(Button button, boolean active) {
    if (active) {
      int bg = fallback(M3Theme.primary(), 0);
      int fg = fallback(M3Theme.onPrimary(), 0);
      button.setBackgroundTintList(ColorStateList.valueOf(bg));
      button.setTextColor(fg);
    } else {
      button.setBackgroundTintList(ColorStateList.valueOf(defaultKeyBackgroundColor));
      button.setTextColor(defaultKeyTextColor);
    }
  }

  private void sendKeyEvent(int keyCode) {
    TerminalSessionFragment fragment = getCurrentFragment();
    if (fragment != null) {
      long now = SystemClock.uptimeMillis();
      fragment.sendKeyEvent(keyCode);
    }
  }

  private void typeText(String text) {
    TerminalSession session = currentSession();
    if (session == null) return;
    byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
    session.write(bytes, 0, bytes.length);
  }

  // ─── Sessions ────────────────────────────────────────────────────────

  private void addNewDebianSession() {
    viewModel.addDebianSession();
  }

  private void addNewShellSession() {
    String workingDir = getIntent().getStringExtra(EXTRA_WORKING_DIR);
    viewModel.addSession(workingDir);
  }

  // ─── Theme ───────────────────────────────────────────────────────────

  private void setupBackgroundBlur() {
    b.inputDock.setElevation(0f);
    setupBackgroundBlur(b.backgroundIconTerminal, b.toolbar, b.tabsBar, b.inputDock);
  }

  private void applyJsonTheme() {
    boolean hasBackgroundImage = isBackgroundImageEnabled();

    if (hasBackgroundImage) {
      b.coordinator.setBackgroundColor(Color.TRANSPARENT);
    } else {
      Integer surfaceContainer = M3Theme.surfaceContainer();
      Integer surface = M3Theme.surface();
      Integer surfaceHigh = M3Theme.surfaceContainerHigh();
      if (surfaceContainer == null) surfaceContainer = surface;
      if (surfaceContainer != null) b.coordinator.setBackgroundColor(surfaceContainer);
      if (surfaceHigh != null) {
        applyViewColor(b.toolbar, surfaceHigh);
        if (b.tabsBar != null) applyViewColor(b.tabsBar, surfaceHigh);
        applyViewColor(b.inputDock, surfaceHigh);
      }
    }

    Integer onSurface = M3Theme.onSurface();
    Integer onSurfaceVariant = M3Theme.onSurfaceVariant();
    Integer outlineVariant = M3Theme.outlineVariant();
    Integer primary = M3Theme.primary();

    M3Theme.toolbar(b.toolbar);
    styleTerminalTabs();

    if (onSurface != null) b.handleChevron.setColorFilter(onSurface);
    if (primary != null) b.commandInputLayout.setEndIconTintList(ColorStateList.valueOf(primary));
    if (onSurfaceVariant != null) b.commandInput.setHintTextColor(onSurfaceVariant);
    if (outlineVariant != null) {
      applyViewColor(b.dividerTop, outlineVariant);
      applyViewColor(b.divExtra1, outlineVariant);
      applyViewColor(b.divExtra2, outlineVariant);
      applyViewColor(b.divExtra3, outlineVariant);
      applyViewColor(b.dragHandlePill, outlineVariant);
    }

    applyColorBackground();
  }

  @Override
  protected void applyOwnTheme(ThemeUtils themeUtils) {
    super.applyOwnTheme(themeUtils);
    if (b != null) {
      if (b.tabLayout != null) {
        styleTerminalTabs();
      }
      applyColorBackground();
      themeUtils.applyImageBackground(b.backgroundIconTerminal);
    }
  }

  /**
   * Matches the tabs with the rest of the M3 surface container system (like ThemeEditor), fading
   * alpha over the background image.
   */
  private void styleTerminalTabs() {
    Integer low = fallback(M3Theme.surfaceContainerLow(), M3Theme.surfaceContainer());
    if (low != null) {
      b.tabLayout.setBackgroundColor(0);
    }
    M3Theme.tabs(b.tabLayout);
  }
  private void applyColorBackground() {
    try {
      ThemeUtils themeUtils = new ThemeUtils(new ThemeManager(this));
      GhostTheme theme = themeUtils.getTheme();
      if (b == null || b.mainContent == null || theme == null || theme.getActivity() == null) {
        return;
      }
      String background = theme.getActivity().getBackground();
      if (background != null && !background.isEmpty()) {
        b.mainContent.setBackgroundColor(Color.parseColor(background));
      }
    } catch (Throwable ignored) {
    }
  }

  private boolean isBackgroundImageEnabled() {
    boolean showBg = new PreferencesUtils(this).isShowBackground();
    try {
      GhostTheme theme = new ThemeUtils(new ThemeManager(this)).getTheme();
      return showBg
          && theme != null
          && theme.getWidget() != null
          && theme.getWidget().getImagepath() != null
          && !theme.getWidget().getImagepath().isEmpty();
    } catch (Throwable ignored) {
      return false;
    }
  }

  private void applyViewColor(View view, int color) {
    if (view == null) return;
    Drawable background = view.getBackground();
    if (background != null) {
      background.mutate().setTint(color);
    } else {
      view.setBackgroundColor(color);
    }
  }

  // ─── Init overlay ────────────────────────────────────────────────────

  private void setupInitOverlay() {
    initOverlay = b.getRoot().findViewById(R.id.initOverlay);
    initOverlayStatus = b.getRoot().findViewById(R.id.initOverlayStatus);
    TextView versionText = b.getRoot().findViewById(R.id.initOverlayVersion);
    try {
      String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
      versionText.setText("v" + version);
    } catch (Exception e) {
      versionText.setText("v1.0");
    }
    viewModel.setInitOverlayShower(this::showInitOverlay);
    viewModel.setInitOverlayHider(this::hideInitOverlay);
  }

  private void showInitOverlay() {
    if (initOverlay == null) return;
    initOverlay.setVisibility(View.VISIBLE);
    initOverlay.setAlpha(0f);
    initOverlay
        .animate()
        .alpha(1f)
        .setDuration(400)
        .setInterpolator(new DecelerateInterpolator())
        .start();
    cycleInitStatus(0);
  }

  private void hideInitOverlay() {
    if (initOverlay == null) return;
    initOverlay
        .animate()
        .alpha(0f)
        .setDuration(500)
        .setInterpolator(new DecelerateInterpolator())
        .withEndAction(() -> initOverlay.setVisibility(View.GONE))
        .start();
  }

  private void cycleInitStatus(int step) {
    if (initOverlay == null || initOverlay.getVisibility() != View.VISIBLE) return;
    String[] messages = {
      "Setting up DNS...", "Installing Node.js...", "Installing Web LSP...", "Almost ready..."
    };
    if (step < messages.length) {
      initOverlayStatus.setText(messages[step]);
      b.getRoot().postDelayed(() -> cycleInitStatus(step + 1), 4000);
    }
  }

  // ─── Back ────────────────────────────────────────────────────────────

  private void setupBackHandler() {
    getOnBackPressedDispatcher()
        .addCallback(
            this,
            new OnBackPressedCallback(true) {
              @Override
              public void handleOnBackPressed() {
                if (inputDock != null && inputDock.isExpanded()) {
                  inputDock.collapse();
                  return;
                }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
              }
            });
  }

  // ─── Debian install ──────────────────────────────────────────────────

  private void confirmAndRemoveDebian() {
    new DialogCompat(this)
        .setTitle(getString(R.string.terminal_remove_debian))
        .setMessage(getString(R.string.terminal_confirm_remove_debian_message))
        .setPositiveButton(
            getString(R.string.terminal_action_remove),
            (dialog, which) ->
                DebianBootstrap.uninstall(
                    this,
                    () -> {
                      GhostToast.makeText(
                              this,
                              getString(R.string.terminal_debian_removed),
                              GhostToast.LENGTH_SHORT)
                          .show();
                    }))
        .setNegativeButton(getString(R.string.terminal_action_cancel), null)
        .show();
  }

  private void buildInstallDialogViews() {
    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    int pad = (int) (16 * getResources().getDisplayMetrics().density);
    layout.setPadding(pad, pad, pad, pad);

    installStatusText = new TextView(this);
    installStatusText.setText(getString(R.string.terminal_status_starting_download));

    installProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
    installProgressBar.setMax(100);
    installProgressBar.setIndeterminate(false);

    layout.addView(installStatusText);
    layout.addView(installProgressBar);

    installDialog =
        new DialogCompat(this)
            .setTitle(getString(R.string.terminal_install_debian))
            .setView(layout)
            .setCancelable(false)
            .setNegativeButton(
                getString(R.string.terminal_action_cancel),
                (dialog, which) -> {
                  DebianInstaller.cancelInstall();
                  GhostToast.makeText(
                          this,
                          getString(R.string.terminal_install_cancelled),
                          GhostToast.LENGTH_SHORT)
                      .show();
                })
            .create();
    installDialog.show();
  }

  private DebianInstaller.InstallListener getOrCreateInstallListener() {
    if (installListener != null) return installListener;
    installListener =
        new DebianInstaller.InstallListener() {
          @Override
          public void onDownloadProgress(int percent) {
            runOnUiThread(
                () -> {
                  installStatusText.setText(
                      getString(R.string.terminal_status_downloading, percent));
                  installProgressBar.setIndeterminate(false);
                  installProgressBar.setProgress(percent);
                });
          }

          @Override
          public void onExtractProgress(int extractedEntries) {
            runOnUiThread(
                () -> {
                  installStatusText.setText(
                      getString(R.string.terminal_status_extracting, extractedEntries));
                  installProgressBar.setIndeterminate(true);
                });
          }

          @Override
          public void onSuccess() {
            runOnUiThread(
                () -> {
                  if (installDialog != null) installDialog.dismiss();
                  GhostToast.makeText(
                          TerminalActivity.this,
                          getString(R.string.terminal_debian_installed_success),
                          GhostToast.LENGTH_LONG)
                      .show();
                  viewModel.bindService();
                  viewModel.setServiceListener();
                });
          }

          @Override
          public void onError(String message) {
            runOnUiThread(
                () -> {
                  if (installDialog != null) installDialog.dismiss();
                  GhostToast.makeText(TerminalActivity.this, message, GhostToast.LENGTH_LONG)
                      .show();
                });
          }
        };
    return installListener;
  }

  private void startDebianInstall() {
    buildInstallDialogViews();
    DebianInstaller.installDebian(this, getOrCreateInstallListener());
  }

  private void attachToRunningInstall() {
    buildInstallDialogViews();
    DebianInstaller.attach(getOrCreateInstallListener());
  }

  private void startTerminalBackup() {
    if (TerminalBackupManager.isRunning()) {
      GhostToast.makeText(this, R.string.terminal_backup_busy, GhostToast.LENGTH_SHORT).show();
      return;
    }
    showTransferProgress(true);
    TerminalBackupManager.backup(this, createTransferListener());
  }

  private void showRestorePicker() {
    if (TerminalBackupManager.isRunning()) {
      GhostToast.makeText(this, R.string.terminal_backup_busy, GhostToast.LENGTH_SHORT).show();
      return;
    }
    List<File> backups = TerminalBackupManager.listBackups(this);
    if (backups.isEmpty()) {
      GhostToast.makeText(this, R.string.terminal_backup_none, GhostToast.LENGTH_LONG).show();
      return;
    }
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    List<String> labels = new ArrayList<>();
    for (File file : backups) {
      labels.add(
          getString(
              R.string.terminal_backup_item,
              format.format(new Date(file.lastModified())),
              StorageUtils.formatSize(file.length())));
    }
    new LiquidGlassDialogBuilderJava(this)
        .setTitle(R.string.terminal_restore_pick)
        .setItems(
            labels.toArray(new String[0]),
            (dialog, which) -> confirmRestore(backups.get(which)))
        .setNegativeButton(R.string.terminal_action_cancel, null)
        .show();
  }

  private void confirmRestore(File archive) {
    new LiquidGlassDialogBuilderJava(this)
        .setTitle(R.string.terminal_restore_menu)
        .setMessage(getString(R.string.terminal_confirm_restore_message, archive.getName()))
        .setNegativeButton(R.string.terminal_action_cancel, null)
        .setPositiveButton(
            R.string.terminal_restore_confirm,
            (dialog, which) -> {
              viewModel.unbindService();
              showTransferProgress(false);
              TerminalBackupManager.restore(TerminalActivity.this, archive, createTransferListener());
            })
        .show();
  }

  private void showTransferProgress(boolean backup) {
    transferIsBackup = backup;
    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    int pad = (int) (16 * getResources().getDisplayMetrics().density);
    layout.setPadding(pad, pad, pad, pad);

    transferStatusText = new TextView(this);
    transferStatusText.setText(R.string.terminal_transfer_scanning);

    transferProgressBar =
        new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
    transferProgressBar.setMax(100);
    transferProgressBar.setIndeterminate(true);

    transferPathText = new TextView(this);
    transferPathText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
    transferPathText.setMaxLines(1);
    transferPathText.setEllipsize(TextUtils.TruncateAt.MIDDLE);

    layout.addView(transferStatusText);
    layout.addView(transferProgressBar);
    layout.addView(transferPathText);

    transferDialog =
        new LiquidGlassDialogBuilderJava(this)
            .setTitle(
                backup ? R.string.terminal_backup_title : R.string.terminal_restore_title)
            .setView(layout)
            .setCancelable(false)
            .setNegativeButton(
                R.string.terminal_action_cancel,
                (dialog, which) -> TerminalBackupManager.cancel())
            .create();
    transferDialog.show();
  }

  private TerminalBackupManager.ProgressListener createTransferListener() {
    return new TerminalBackupManager.ProgressListener() {
      @Override
      public void onProgress(
          long processedBytes, long totalBytes, int percent, String currentPath) {
        if (transferStatusText == null) {
          return;
        }
        if (percent < 0) {
          transferStatusText.setText(R.string.terminal_transfer_scanning);
          transferProgressBar.setIndeterminate(true);
        } else {
          transferProgressBar.setIndeterminate(false);
          transferProgressBar.setProgress(percent);
          transferStatusText.setText(
              getString(
                  transferIsBackup
                      ? R.string.terminal_backup_working
                      : R.string.terminal_restore_working,
                  StorageUtils.formatSize(processedBytes),
                  StorageUtils.formatSize(totalBytes),
                  percent));
        }
        if (currentPath != null) {
          transferPathText.setText(currentPath);
        }
      }

      @Override
      public void onSuccess(File archive, long sizeBytes) {
        if (transferDialog != null) {
          transferDialog.dismiss();
        }
        if (transferIsBackup) {
          GhostToast.makeText(
                  TerminalActivity.this,
                  getString(
                      R.string.terminal_backup_done,
                      StorageUtils.formatSize(sizeBytes),
                      archive.getAbsolutePath()),
                  GhostToast.LENGTH_LONG)
              .show();
        } else {
          viewModel.bindService();
          viewModel.setServiceListener();
          GhostToast.makeText(
                  TerminalActivity.this,
                  getString(R.string.terminal_restore_done, StorageUtils.formatSize(sizeBytes)),
                  GhostToast.LENGTH_LONG)
              .show();
        }
      }

      @Override
      public void onCancelled() {
        if (transferDialog != null) {
          transferDialog.dismiss();
        }
        if (!transferIsBackup) {
          viewModel.bindService();
          viewModel.setServiceListener();
        }
        GhostToast.makeText(
                TerminalActivity.this,
                transferIsBackup
                    ? R.string.terminal_backup_cancelled
                    : R.string.terminal_restore_cancelled,
                GhostToast.LENGTH_SHORT)
            .show();
      }

      @Override
      public void onError(String message, Throwable error) {
        if (transferDialog != null) {
          transferDialog.dismiss();
        }
        if (!transferIsBackup) {
          viewModel.bindService();
          viewModel.setServiceListener();
        }
        GhostToast.makeText(
                TerminalActivity.this,
                getString(
                    transferIsBackup
                        ? R.string.terminal_backup_failed
                        : R.string.terminal_restore_failed,
                    message),
                GhostToast.LENGTH_LONG)
            .show();
      }
    };
  }

  // ─── Pager adapter ───────────────────────────────────────────────────

  private class SessionPagerAdapter extends FragmentStateAdapter {

    SessionPagerAdapter(TerminalActivity activity) {
      super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
      TerminalTab tab = viewModel.getSessionList().get(position);
      return TerminalSessionFragment.newInstance(tab.id);
    }

    @Override
    public int getItemCount() {
      return viewModel.getSessionList().size();
    }

    @Override
    public long getItemId(int position) {
      TerminalTab tab = viewModel.getSessionList().get(position);
      return tab.id;
    }

    @Override
    public boolean containsItem(long itemId) {
      return viewModel.containsSessionId((int) itemId);
    }
  }

  private static int fallback(Integer value, int def) {
    return value != null ? value : def;
  }
}
