package ir.hanzodev1375.ghostide.plugin.gpl;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;

import ir.hanzodev1375.ghostide.R;

import java.io.File;
import java.lang.reflect.Method;

/**
 * Wraps the host context so a plugin's {@code getResources()}/{@code getAssets()} resolve
 * against its own {@code .gpl} package instead of the host's, using the same {@code
 * AssetManager.addAssetPath} reflection technique long relied on by Android plugin frameworks
 * such as RePlugin and VirtualAPK. {@code addAssetPath} is not a public API; if a future Android
 * version removes or blocks it, {@link #create} falls back to the host's own resources so a
 * plugin still loads, just without its custom layouts/drawables.
 */
final class GplPluginContextWrapper extends ContextWrapper {

  private static final String TAG = "GplPluginContext";

  private final ClassLoader classLoader;
  private final Resources resources;
  private final Resources.Theme theme;
  private LayoutInflater inflater;

  private GplPluginContextWrapper(
      Context base, ClassLoader classLoader, Resources resources, Resources.Theme theme) {
    super(base);
    this.classLoader = classLoader;
    this.resources = resources;
    this.theme = theme;
  }

  static GplPluginContextWrapper create(Context hostContext, File gplFile, ClassLoader classLoader) {
    Resources resources = tryLoadPluginResources(hostContext, gplFile);
    return new GplPluginContextWrapper(hostContext, classLoader, resources, buildTheme(hostContext));
  }

  /**
   * Builds the theme a plugin's views are inflated against. Without this the wrapped context has no
   * theme of its own and {@link #getTheme()} falls back to a framework {@code android.R.style}
   * theme, which makes every Material3 widget throw during theme enforcement. We copy the host's
   * Material3 theme so plugins can safely use Material components; a plugin that wants its own look
   * can still wrap this context in a {@code ContextThemeWrapper}.
   */
  private static Resources.Theme buildTheme(Context hostContext) {
    Resources hostResources = hostContext.getResources();
    Resources.Theme theme = hostResources.newTheme();
    Resources.Theme host = hostContext.getTheme();
    if (host != null) {
      theme.setTo(host);
    }
    theme.applyStyle(R.style.AppTheme, true);
    return theme;
  }

  private static Resources tryLoadPluginResources(Context hostContext, File gplFile) {
    try {
      AssetManager assetManager = AssetManager.class.getDeclaredConstructor().newInstance();
      Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
      Object result = addAssetPath.invoke(assetManager, gplFile.getAbsolutePath());
      if (result instanceof Integer cookie && cookie == 0) {
        throw new IllegalStateException("addAssetPath rejected " + gplFile);
      }
      Resources hostResources = hostContext.getResources();
      return new Resources(assetManager, hostResources.getDisplayMetrics(), hostResources.getConfiguration());
    } catch (ReflectiveOperationException | RuntimeException e) {
      Log.w(TAG, "Falling back to host resources for " + gplFile + ": " + e.getMessage());
      return hostContext.getResources();
    }
  }

  @Override
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  @Override
  public Resources getResources() {
    return resources;
  }

  @Override
  public Resources.Theme getTheme() {
    return theme;
  }

  @Override
  public AssetManager getAssets() {
    return resources.getAssets();
  }

  @Override
  public Object getSystemService(String name) {
    if (LAYOUT_INFLATER_SERVICE.equals(name)) {
      if (inflater == null) {
        inflater = LayoutInflater.from(getBaseContext()).cloneInContext(this);
      }
      return inflater;
    }
    if (CLIPBOARD_SERVICE.equals(name)) {
      return getBaseContext().getSystemService(name);
    }
    return super.getSystemService(name);
  }
}
