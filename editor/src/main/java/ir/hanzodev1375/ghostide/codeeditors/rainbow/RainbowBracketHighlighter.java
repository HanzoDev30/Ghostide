package ir.hanzodev1375.ghostide.codeeditors.rainbow;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import io.github.rosemoe.sora.event.ContentChangeEvent;
import io.github.rosemoe.sora.event.ScrollEvent;
import io.github.rosemoe.sora.lang.Language;
import io.github.rosemoe.sora.lang.styling.HighlightTextContainer;
import io.github.rosemoe.sora.lang.styling.color.EditorColor;
import io.github.rosemoe.sora.lang.EmptyLanguage;
import io.github.rosemoe.sora.widget.CodeEditor;
import ir.hanzodev1375.ghostide.codeeditors.colorscheme.GhostColorScheme;
import ir.hanzodev1375.ghostide.codeeditors.textmate.TextMateLanguages;
import ir.hanzodev1375.ghostide.codeeditors.textmate.TextMateScopeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * براکت های رنگین کمانی برای زبان های TextMate.
 *
 * <p>گرامر TextMate عمق تو در تویی را نمی داند، پس اینجا متن با {@link RainbowBracketScanner} اسکن
 * می شود (کامنت و رشته ها از language-configuration.json همان زبان رد می شوند) و فقط براکت های قابل
 * دیدن با {@link HighlightTextContainer} روی رنگ TextMate می نشینند. به Styles دست نمی زند، پس با
 * آنالایزر ناهمزمان TextMate تداخلی ندارد.
 *
 * <p>فایل های کوچک (تا ~۲۰۰ هزار کاراکتر) همزمان با هر تغییر اسکن می شوند تا رنگ هیچ وقت روی
 * کاراکتر اشتباه ننشیند؛ فایل های بزرگ تر debounce شده و در ترد پس زمینه اسکن می شوند.
 */
public final class RainbowBracketHighlighter {

  /**
   * تنها نقطه ای که واقعا {@code editor.setHighlightTexts} را صدا می زند (هماهنگی با چکر وابستگی).
   */
  public interface Host {
    void refreshOverlay(boolean fromScroll);
  }

  private static final int SYNC_LIMIT = 200_000;
  private static final int MAX_CHARS = 3_000_000;
  private static final long DEBOUNCE_MS = 150L;

  private static final int[] COLOR_IDS = {
    GhostColorScheme.BRACKET1,
    GhostColorScheme.BRACKET2,
    GhostColorScheme.BRACKET3,
    GhostColorScheme.BRACKET4,
    GhostColorScheme.BRACKET5,
    GhostColorScheme.BRACKET6
  };

  private final CodeEditor editor;
  private final Context context;
  private final Host host;
  private final Handler main = new Handler(Looper.getMainLooper());
  private final EditorColor noBackground = new EditorColor(GhostColorScheme.RAINBOW_NO_BG);
  private final EditorColor[] foregrounds = new EditorColor[COLOR_IDS.length];

  private ExecutorService executor;
  private boolean enabled = true;
  private boolean languageActive;
  private String filePath;
  private String pluginScope;
  private String pluginScopePath;
  private BracketSyntax syntax;
  private RainbowBracketScanner.Result data;
  private int generation;
  private int shownFirst = -1;
  private int shownLast = -1;

  private final Runnable asyncRescan = this::startAsyncScan;

  public RainbowBracketHighlighter(CodeEditor editor, Host host) {
    this.editor = editor;
    this.context = editor.getContext();
    this.host = host;
    for (int i = 0; i < COLOR_IDS.length; i++) {
      foregrounds[i] = new EditorColor(COLOR_IDS[i]);
    }
  }

  public void attach() {
    editor.subscribeEvent(
        ContentChangeEvent.class,
        (event, unsubscribe) -> {
          if (syntax != null) {
            rescan(true);
          }
        });
    editor.subscribeEvent(
        ScrollEvent.class,
        (event, unsubscribe) -> {
          if (data == null) {
            return;
          }
          if (editor.getFirstVisibleLine() == shownFirst
              && editor.getLastVisibleLine() == shownLast) {
            return;
          }
          host.refreshOverlay(true);
        });
  }

  public void release() {
    main.removeCallbacks(asyncRescan);
    generation++;
    if (executor != null) {
      executor.shutdownNow();
      executor = null;
    }
  }

  public void setEnabled(boolean enabled) {
    if (this.enabled == enabled) {
      return;
    }
    this.enabled = enabled;
    reloadSyntax();
    rescan(false);
  }

  public void setFilePath(String path) {
    this.filePath = path;
    reloadSyntax();
    rescan(false);
  }

  /**
   * اسکوپ گرامر افزونه (LspServerDefinition.getGrammarScopeName) برای همین فایل؛ فقط وقتی به کار می
   * رود که مسیر فایل با {@code path} یکی باشد تا روی فایل بعدی نشت نکند.
   */
  public void setPluginScope(String scope, String path) {
    this.pluginScope = scope;
    this.pluginScopePath = path;
    reloadSyntax();
    rescan(false);
  }

  /**
   * از {@code IdeEditor.setEditorLanguage} صدا زده می شود. در فایل های دارای LSP زبان ادیتور یک
   * wrapper است (نه خود TextMateLanguage)، پس فقط «خالی نبودن» زبان را می سنجیم.
   */
  public void onLanguageChanged(Language language) {
    languageActive = language != null && !(language instanceof EmptyLanguage);
    reloadSyntax();
    rescan(false);
  }

  /** فقط رنگین کمانی را روی ادیتور اعمال می کند (وقتی چکر وابستگی فایل را در اختیار ندارد). */
  public void applyOnly() {
    HighlightTextContainer container = new HighlightTextContainer();
    contribute(container);
    editor.setHighlightTexts(container.isEmpty() ? null : container);
  }

  /** براکت های خطوط قابل دیدن را به {@code container} اضافه می کند. */
  public void contribute(HighlightTextContainer container) {
    RainbowBracketScanner.Result r = data;
    int first = Math.max(0, editor.getFirstVisibleLine());
    int last = editor.getLastVisibleLine();
    shownFirst = editor.getFirstVisibleLine();
    shownLast = last;
    if (r == null || r.size == 0) {
      return;
    }
    for (int i = r.firstIndexAtOrAfterLine(first); i < r.size && r.line[i] <= last; i++) {
      int line = r.line[i];
      int col = r.col[i];
      container.add(
          new HighlightTextContainer.HighlightText(
              line,
              col,
              line,
              col + 1,
              noBackground,
              foregrounds[r.depth[i] % foregrounds.length]));
    }
  }

  private void reloadSyntax() {
    // Html آنالایزر اختصاصی خودش را دارد که قبلا براکت ها را رنگی می کند؛ دوباره رنگ نمی زنیم.
    if (!enabled || !languageActive || TextMateLanguages.isHtml(filePath)) {
      syntax = null;
      return;
    }
    String scope = null;
    if (pluginScope != null
        && !pluginScope.isEmpty()
        && (filePath == null || filePath.equals(pluginScopePath))) {
      scope = pluginScope;
    }
    if (scope == null) {
      scope = TextMateScopeMap.scopeOf(filePath);
    }
    // اگر زبان فعال است ولی اسکوپش معلوم نیست (مثل setEditorLanguage مستقیم پلاگین برای .astro)
    // forScope(null) سینتکس عمومی را می دهد
    // respectDisabled=false تا برای زبان‌های در DISABLED هم GENERIC برگردانده شود (وقتی کاربر فعال کرده)
    syntax = BracketSyntaxLoader.forScope(context, scope, false);
  }

  private void rescan(boolean contentChanged) {
    main.removeCallbacks(asyncRescan);
    generation++;
    if (syntax == null) {
      data = null;
      host.refreshOverlay(false);
      return;
    }
    int length = editor.getText().length();
    if (length > MAX_CHARS) {
      data = null;
      host.refreshOverlay(false);
      return;
    }
    if (length <= SYNC_LIMIT) {
      data = RainbowBracketScanner.scan(editor.getText().toString(), syntax);
      host.refreshOverlay(false);
      return;
    }
    if (contentChanged) {
      // موقعیت های قدیمی بعد از ویرایش دیگر معتبر نیستند؛ تا اتمام اسکن رنگ پیش فرض
      data = null;
      host.refreshOverlay(false);
    }
    main.postDelayed(asyncRescan, DEBOUNCE_MS);
  }

  private void startAsyncScan() {
    final BracketSyntax syn = syntax;
    if (syn == null) {
      return;
    }
    final int gen = generation;
    final String snapshot = editor.getText().toString();
    if (executor == null) {
      executor =
          Executors.newSingleThreadExecutor(
              runnable -> {
                Thread thread = new Thread(runnable, "rainbow-bracket-scan");
                thread.setDaemon(true);
                return thread;
              });
    }
    executor.execute(
        () -> {
          RainbowBracketScanner.Result result = RainbowBracketScanner.scan(snapshot, syn);
          main.post(
              () -> {
                if (gen != generation) {
                  return;
                }
                data = result;
                host.refreshOverlay(false);
              });
        });
  }
}
