package ir.hanzodev1375.ghostide.plugin;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.sidesheet.SideSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import ir.hanzodev1375.components.sheet.BaseBlurBottomSheet;
import ir.hanzodev1375.components.sheet.BaseSheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import ir.hanzodev1375.ghostide.ide.ui.api.EditorPanel;
import ir.hanzodev1375.ghostide.ide.ui.api.Panel;
import ir.hanzodev1375.ghostide.ide.ui.api.PluginStateMod;
import ir.hanzodev1375.ghostide.ide.ui.api.PluginUiExtensionPoints;
import ir.hanzodev1375.ghostide.plugin.api.ExtensionPoint;
import ir.hanzodev1375.ghostide.plugin.api.GlobalRegistry;
import ir.theme.ThemeManager;
import ir.theme.ThemeUtils;
import ir.theme.WidgetTheme;

/**
 * Host for {@link Panel} contributions on any screen. Built over {@link
 * PluginUiExtensionPoints#EDITOR_PANEL} for the editor, or over {@link
 * PluginUiExtensionPoints#FILE_PANEL} via {@link #forFileManager(Activity)} for the file manager.
 * Reads everything registered at that point, exposes it as {@link #getPanels()}, and opens one
 * panel when {@link #showPanel(Panel)} is called. The {@link PluginStateMod} returned by {@link
 * Panel#getState()} decides how the panel is shown: side sheet (default), dialog, bottom sheet or a
 * (bottom sheet) dialog fragment. The returned {@link View} is created lazily and cached for the
 * lifetime of this host so a panel keeps its state between opens.
 */
public final class PluginPanelHost {

  private final Activity activity;
  private final ThemeUtils theme;
  private final Supplier<String> lastPathResolver;
  private final List<Panel> panels;
  private final Map<String, View> views = new HashMap<>();
  /** پنجره های شناور باز به ازای هر پنل (فقط داخل همین Activity). */
  private final Map<String, FloatingPanelWindow> floatingWindows = new HashMap<>();
  /** پاپ آپ های باز به ازای هر پنل. */
  private final Map<String, PopupWindow> popupWindows = new HashMap<>();

  public PluginPanelHost(Activity activity) {
    this(activity, null, PluginUiExtensionPoints.EDITOR_PANEL);
  }

  public PluginPanelHost(Activity activity, Supplier<String> lastPathResolver) {
    this(activity, lastPathResolver, PluginUiExtensionPoints.EDITOR_PANEL);
  }

  /** Host for {@link PluginUiExtensionPoints#FILE_PANEL} contributions inside the file manager. */
  public static PluginPanelHost forFileManager(Activity activity) {
    return new PluginPanelHost(activity, null, PluginUiExtensionPoints.FILE_PANEL);
  }

  private PluginPanelHost(
      Activity activity, Supplier<String> lastPathResolver, ExtensionPoint<? extends Panel> point) {
    this.activity = activity;
    this.lastPathResolver = lastPathResolver;
    this.theme = new ThemeUtils(new ThemeManager(activity));
    this.panels = wrapLastPath(collectPanels(point));
  }

  private static List<Panel> collectPanels(ExtensionPoint<? extends Panel> point) {
    List<Panel> found = new ArrayList<>();
    for (var registration : GlobalRegistry.extensions().registrations(point)) {
      found.add((Panel) registration.extension());
    }
    return found;
  }

  /**
   * First non-blank {@link EditorPanel#getLastPath()} among the registered panels, falling back
   * to {@code fallback} (e.g. the editor's current open file). Panels get the final say on the
   * path a {@code CodeRunnerHost.runCurrentFile()} should run.
   */
  public static String resolveLastPath(Supplier<String> fallback) {
    for (EditorPanel panel :
        GlobalRegistry.extensions().extensions(PluginUiExtensionPoints.EDITOR_PANEL)) {
      String path = panel.getLastPath();
      if (path != null && !path.isEmpty()) {
        return path;
      }
    }
    return fallback == null ? null : fallback.get();
  }

  /** Wraps panels so an unimplemented {@code getLastPath()} falls back to this host's path. */
  private List<Panel> wrapLastPath(List<Panel> originals) {
    List<Panel> wrapped = new ArrayList<>(originals.size());
    for (Panel panel : originals) {
      wrapped.add(new LastPathPanel(panel));
    }
    return wrapped;
  }

  private final class LastPathPanel implements Panel {
    private final Panel delegate;

    LastPathPanel(Panel delegate) {
      this.delegate = delegate;
    }

    @Override
    public String getId() {
      return delegate.getId();
    }

    @Override
    public String getTitle() {
      return delegate.getTitle();
    }

    @Override
    public View createView() {
      return delegate.createView();
    }

    @Override
    public String getLastPath() {
      String path = delegate.getLastPath();
      if ((path == null || path.isEmpty()) && lastPathResolver != null) {
        return lastPathResolver.get();
      }
      return path;
    }

    @Override
    public PluginStateMod getState() {
      return delegate.getState();
    }

    @Override
    public void setState(PluginStateMod state) {
      delegate.setState(state);
    }
  }

  public List<Panel> getPanels() {
    return panels;
  }

  public boolean isEmpty() {
    return panels.isEmpty();
  }

  public void showPanel(Panel panel) {
    if (panel == null || activity.isFinishing()) {
      return;
    }

    PluginStateMod state = panel.getState() == null ? PluginStateMod.NONE : panel.getState();
    if (state == PluginStateMod.NONE) {
      return;
    }

    FloatingPanelWindow previousFloat = floatingWindows.remove(panel.getId());
    if (previousFloat != null) {
      previousFloat.dismiss();
    }

    PopupWindow previousPopup = popupWindows.remove(panel.getId());
    if (previousPopup != null) {
      previousPopup.dismiss();
    }

    if (state == PluginStateMod.HEADLESS) {
      runHeadless(panel);
      return;
    }

    if (state == PluginStateMod.SNACKBAR) {
      showSnackbar(panel);
      return;
    }

    View content = views.get(panel.getId());
    if (content == null) {
      content = createPanelView(panel);
      views.put(panel.getId(), content);
    } else if (content.getParent() instanceof ViewGroup) {
      ((ViewGroup) content.getParent()).removeView(content);
    }

    ViewGroup wrapper = buildWrapper(panel, content);

    switch (state) {
      case DIALOG:
        showDialog(wrapper);
        break;
      case DIALOGFRAGMENT:
        showDialogFragment(panel, wrapper);
        break;
      case FRAGMENT:
        showFragment(panel, wrapper);
        break;
      case BOTTOMSHERTFRAGMENT:
        showBottomSheetFragment(panel, wrapper);
        break;
      case BOTTOMSHEETDIALOG:
        showBottomSheetDialog(wrapper);
        break;
      case FLOATINGWINDOWS:
        showFloatingWindow(panel, content);
        break;
      case POPUP_WINDOW:
        showPopupWindow(panel, wrapper);
        break;
      case ACTIVITY:
        showActivity(panel, wrapper);
        break;
      case SIDESHEET:
      default:
        showSideSheet(wrapper);
        break;
    }
  }

  /**
   * پنجره ی شناور فقط روی همین Activity؛ نه نیاز به مجوز overlay داره نه از اپ خارج می شه. به جای
   * wrapper، مستقیم content رو می فرستیم چون خود پنجره نوار عنوان و آیکون های بستن/درگ رو داره.
   */
  private void showFloatingWindow(Panel panel, View content) {
    FloatingPanelWindow win = new FloatingPanelWindow(activity, panel.getTitle(), content);
    floatingWindows.put(panel.getId(), win);
    win.show();
  }

  private void showPopupWindow(Panel panel, ViewGroup wrapper) {
    PopupWindow popup = new PopupWindow(activity);
    popup.setContentView(wrapper);
    popup.setWidth(dp(280));
    popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
    popup.setFocusable(true);
    popup.setOutsideTouchable(true);
    popup.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
      popup.setElevation(dp(8));
    }
    View anchor = activity.findViewById(android.R.id.content);
    popup.showAtLocation(anchor, Gravity.CENTER, 0, 0);
    popupWindows.put(panel.getId(), popup);
  }

  private void showActivity(Panel panel, ViewGroup wrapper) {
    new Dialog(activity) {
      {
        setContentView(wrapper);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        styleDialogWindow(getWindow());
      }
    }.show();
  }

  private void showSnackbar(Panel panel) {
    View root = activity.findViewById(android.R.id.content);
    if (root == null) return;
    Snackbar.make(root, panel.getTitle(), Snackbar.LENGTH_LONG).show();
  }

  private void runHeadless(Panel panel) {
    try {
      panel.createView();
    } catch (Exception ignored) {
    }
  }

  /** برای بستن همه ی پنجره های شناور (مثلاً در onPause یا onDestroy اکتویتی). */
  public void dismissAllFloatingWindows() {
    for (FloatingPanelWindow win : floatingWindows.values()) {
      win.dismiss();
    }
    floatingWindows.clear();
    for (PopupWindow popup : popupWindows.values()) {
      popup.dismiss();
    }
    popupWindows.clear();
  }

  private void showSideSheet(ViewGroup wrapper) {
    SideSheetDialog sheet = new SideSheetDialog(activity);
    sheet.setContentView(wrapper);
    sheet.getWindow().setNavigationBarColor(Color.TRANSPARENT);
    theme.applySideSheet(sheet);
    sheet.show();
  }

  private void showBottomSheetDialog(ViewGroup wrapper) {
    BaseSheet sheet = new BaseSheet(activity);
    sheet.setContentView(wrapper);
    sheet.show();
  }

  private void showDialog(ViewGroup wrapper) {
    Dialog dialog = new Dialog(activity);
    dialog.setContentView(wrapper);
    dialog.getWindow().setNavigationBarColor(Color.TRANSPARENT);
    styleDialogWindow(dialog.getWindow());
    dialog.show();
  }

  private void showDialogFragment(Panel panel, ViewGroup wrapper) {
    FragmentManager fm = fragmentManager();
    if (fm == null) {
      showDialog(wrapper);
      return;
    }
    String tag = "plugin_panel_dialog_" + panel.getId();
    Fragment previous = fm.findFragmentByTag(tag);
    if (previous != null) {
      fm.beginTransaction().remove(previous).commitNowAllowingStateLoss();
    }
    new PanelDialogFragment(wrapper).show(fm, tag);
  }

  private void showBottomSheetFragment(Panel panel, ViewGroup wrapper) {
    FragmentManager fm = fragmentManager();
    if (fm == null) {
      showBottomSheetDialog(wrapper);
      return;
    }
    String tag = "plugin_panel_sheet_" + panel.getId();
    Fragment previous = fm.findFragmentByTag(tag);
    if (previous != null) {
      fm.beginTransaction().remove(previous).commitNowAllowingStateLoss();
    }
    new PanelBottomSheetFragment(wrapper).show(fm, tag);
  }

  private void showFragment(Panel panel, ViewGroup wrapper) {
    FragmentManager fm = fragmentManager();
    if (fm == null) {
      showDialog(wrapper);
      return;
    }
    String tag = "plugin_panel_fragment_" + panel.getId();
    Fragment previous = fm.findFragmentByTag(tag);
    if (previous != null) {
      fm.beginTransaction().remove(previous).commitNowAllowingStateLoss();
    }
    FragmentTransaction tx = fm.beginTransaction();
    tx.setCustomAnimations(
        android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in,
        android.R.anim.fade_out);
    tx.add(android.R.id.content, new PanelHostFragment(wrapper), tag);
    tx.commitAllowingStateLoss();
  }

  private FragmentManager fragmentManager() {
    if (activity instanceof FragmentActivity) {
      return ((FragmentActivity) activity).getSupportFragmentManager();
    }
    return null;
  }

  private ViewGroup buildWrapper(Panel panel, View content) {
    LinearLayout wrapper = new LinearLayout(activity);
    wrapper.setOrientation(LinearLayout.VERTICAL);

    LinearLayout headerBox = new LinearLayout(activity);
    headerBox.setOrientation(LinearLayout.VERTICAL);
    int pad = dp(16);
    headerBox.setPadding(pad, dp(12), pad, dp(12));

    TextView header = new TextView(activity);
    header.setText(panel.getTitle());
    header.setTextSize(15);
    header.setGravity(Gravity.CENTER_VERTICAL);
    header.setTypeface(header.getTypeface(), Typeface.BOLD);
    headerBox.addView(
        header,
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

    String lastPath = panel.getLastPath();
    if (lastPath != null && !lastPath.isEmpty()) {
      TextView path = new TextView(activity);
      path.setText(lastPath);
      path.setTextSize(12);
      path.setGravity(Gravity.CENTER_VERTICAL);
      headerBox.addView(
          path,
          new LinearLayout.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    styleHeader(headerBox);
    wrapper.addView(
        headerBox,
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

    View divider = new View(activity);
    divider.setBackgroundColor(0x223E4452);
    wrapper.addView(
        divider,
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));

    wrapper.addView(
        content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
    return wrapper;
  }

  private View createPanelView(Panel panel) {
    try {
      return panel.createView();
    } catch (Exception e) {
      TextView error = new TextView(activity);
      error.setPadding(dp(16), dp(16), dp(16), dp(16));
      error.setText("Plugin panel '" + panel.getId() + "' failed: " + e);
      return error;
    }
  }

  private void styleHeader(LinearLayout headerBox) {
    WidgetTheme widget = theme.getTheme() == null ? null : theme.getTheme().getWidget();
    if (widget == null) {
      return;
    }
    headerBox.setBackgroundColor(Color.TRANSPARENT);
    String text = widget.getMenutextcolor();
    if (text != null) {
      int color = Color.parseColor(text);
      for (int i = 0; i < headerBox.getChildCount(); i++) {
        if (headerBox.getChildAt(i) instanceof TextView) {
          ((TextView) headerBox.getChildAt(i)).setTextColor(color);
        }
      }
    }
  }

  private void styleDialogWindow(Window window) {
    var editor = theme.getTheme() == null ? null : theme.getTheme().getEditor();
    if (editor == null || editor.getCompletionWndBackground() == null) {
      return;
    }
    try {
      window
          .getDecorView()
          .setBackgroundTintList(
              ColorStateList.valueOf(Color.parseColor(editor.getCompletionWndBackground())));
    } catch (Exception ignored) {
    }
  }

  /** Reusable fragment that just hosts a panel view. */
  private static final class PanelHostFragment extends Fragment {
    private View content;

    public PanelHostFragment() {
      // Required for FragmentManager restoration.
    }

    PanelHostFragment(View content) {
      this.content = content;
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      return attach(content);
    }
  }

  /** Dialog-hosted panel. */
  private static final class PanelDialogFragment extends DialogFragment {
    private View content;

    public PanelDialogFragment() {
      // Required for FragmentManager restoration.
    }

    PanelDialogFragment(View content) {
      this.content = content;
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      return attach(content);
    }
  }

  /** Bottom sheet hosted panel with glass effect. */
  private static final class PanelBottomSheetFragment extends BaseBlurBottomSheet {
    private View content;

    public PanelBottomSheetFragment() {
      // Required for FragmentManager restoration.
    }

    PanelBottomSheetFragment(View content) {
      this.content = content;
    }

    @Override
    protected void onContentReady(ViewGroup contentContainer) {
      if (content != null) {
        if (content.getParent() instanceof ViewGroup) {
          ((ViewGroup) content.getParent()).removeView(content);
        }
        contentContainer.addView(content);
      }
    }
  }

  private static View attach(View content) {
    if (content == null) {
      return null;
    }
    if (content.getParent() instanceof ViewGroup) {
      ((ViewGroup) content.getParent()).removeView(content);
    }
    return content;
  }

  private int dp(int value) {
    float density = activity.getResources().getDisplayMetrics().density;
    return Math.round(value * density);
  }
}