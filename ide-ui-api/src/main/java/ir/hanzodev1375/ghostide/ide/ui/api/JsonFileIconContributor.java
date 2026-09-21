package ir.hanzodev1375.ghostide.ide.ui.api;

import android.content.Context;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A ready-made {@link FileIconContributor} that loads icon mappings from a JSON file shipped in a
 * plugin's own assets. The file uses the same schema as the host's {@code data/file_icons.json}
 * but is <b>partial</b>: only the sections you declare are overridden, everything else is left to
 * the built-in set (or to other contributors). The VS Code spelling for the section names is also
 * accepted ({@code fileExtensions}, {@code fileNames}, {@code folderNames}, {@code
 * folderNamesExpanded}):
 *
 * <pre>{@code
 * {
 *   "asset_dir": "myicons",
 *   "extensions": { "hsi": "file_type_hsi" },
 *   "fileNames": { "makefile": "file_type_makefile" },
 *   "folderNames": { "components": "folder_type_components" },
 *   "defaults": {
 *     "file": "default_file",
 *     "folder": "default_folder",
 *     "root_folder": "default_root_folder"
 *   }
 * }
 * }</pre>
 *
 * <p>The simplest pack only needs an extension mapping, exactly like a VS Code override:
 *
 * <pre>{@code
 * {
 *   "extensions": { "hsi": "iconhsi" }
 * }
 * }</pre>
 *
 * <p>Extension keys may be written with or without the {@code *.} / {@code .} prefix, matching VS
 * Code's {@code files.associations}/{@code fileExtensions} conventions: {@code "*.hsi"}, {@code
 * ".hsi"} and {@code "hsi"} are all equivalent.
 *
 * <p>Icon names may reference either your own artwork placed under {@code asset_dir} in the plugin
 * assets, or any icon of the built-in {@code vscode_icons} set. Custom artwork may be {@code .svg},
 * {@code .png}, {@code .jpg}/{@code .jpeg} or {@code .webp}; it is extracted once into the plugin's
 * private {@code filesDir/ghost_icons/} folder at construction time and served back as {@code
 * file://} URIs (Glide can only open host assets through {@code android_asset}); unknown names fall
 * back to the built-in set untouched. An {@code asset_dir} of {@code "."} (or {@code "/"}) means the
 * plugin's assets root, so a pack can reuse an asset the plugin already ships — for example its own
 * {@code plugin.json} icon.
 *
 * <p>Usage inside {@code activate()}:
 *
 * <pre>{@code
 * Context pluginContext = context.getServices().require(IdeHostServices.PLUGIN_ANDROID_CONTEXT);
 * context.registerDisposable(context.getExtensions().register(
 *     PluginUiExtensionPoints.FILE_ICON_CONTRIBUTOR,
 *     new JsonFileIconContributor(pluginContext, "myicons.json")));
 * }</pre>
 *
 * <p>When several icon packs are installed, the newest installed one is consulted first (see {@code
 * GplPluginLoader}); the first non-null answer wins per path, so newer packs override older ones on
 * the keys they declare and fall back to older packs / the built-in set everywhere else.
 */
public final class JsonFileIconContributor implements FileIconContributor {

  private static final String EXTRACT_DIR = "ghost_icons";
  private static final String[] ARTWORK_EXTENSIONS = {".svg", ".png", ".jpg", ".jpeg", ".webp"};
  static final String TAG = "JsonFileIconContributor";
  private final JSONObject extensions;
  private final JSONObject filenames;
  private final JSONObject folders;
  private final String defaultFile;
  private final String defaultFolder;
  private final String defaultRootFolder;
  private final List<String> extKeysSorted;
  private final Map<String, String> resolved = new HashMap<>();

  /**
   * @param pluginContext scoped plugin context from {@code IdeHostServices.PLUGIN_ANDROID_CONTEXT}
   * @param jsonAssetPath path of the mapping JSON inside the plugin assets, e.g. {@code
   *     "myicons.json"}
   */
  public JsonFileIconContributor(Context pluginContext, String jsonAssetPath) {
    JSONObject root = readJson(pluginContext, jsonAssetPath);
    String assetDir = root == null ? "" : root.optString("asset_dir", "");
    JSONObject def = root == null ? null : root.optJSONObject("defaults");
    defaultFile = def == null ? "" : def.optString("file", "");
    defaultFolder = def == null ? "" : def.optString("folder", "");
    defaultRootFolder = def == null ? "" : def.optString("root_folder", "");

    extensions = extendKeys(merged(root, "extensions", "fileExtensions"));
    filenames = merged(root, "filenames", "fileNames");
    folders = merged(root, "folders", "folderNames", "folderNamesExpanded");

    List<String> keys = new ArrayList<>();
    if (extensions != null) {
      for (Iterator<String> it = extensions.keys(); it.hasNext(); ) keys.add(it.next());
      Collections.sort(keys, (a, b) -> b.length() - a.length());
    }
    extKeysSorted = keys;

    Set<String> custom = new HashSet<>();
    markCustom(extensions, custom);
    markCustom(filenames, custom);
    markCustom(folders, custom);
    addIfPresent(custom, defaultFile);
    addIfPresent(custom, defaultFolder);
    addIfPresent(custom, defaultRootFolder);

    File outDir = new File(new File(pluginContext.getFilesDir(), EXTRACT_DIR), safe(jsonAssetPath));
    extractArtwork(pluginContext, assetDir, bundledArtwork(pluginContext, assetDir, custom), outDir);
  }

  @Override
  public String getIcon(String filePath) {
    File file = new File(filePath);
    if (file.isDirectory()) {
      String dirName = file.getName();
      if (dirName.isEmpty()) return pick(defaultRootFolder);
      if (folders != null) {
        String hit = resolve(folders, dirName.toLowerCase());
        if (hit != null) return hit;
      }
      return pick(defaultFolder);
    }
    String lower = file.getName().toLowerCase();
    if (!lower.isEmpty()) {
      if (filenames != null && filenames.has(lower)) return resolve(filenames, lower);
      for (String key : extKeysSorted) {
        int cut = lower.length() - key.length();
        if (cut > 0 && lower.charAt(cut - 1) == '.' && lower.endsWith(key)) {
          return resolve(extensions, key);
        }
      }
    }
    return pick(defaultFile);
  }

  /** Returns the URI for custom artwork, or the bare name for built-in icons. */
  private String resolve(JSONObject section, String key) {
    String name = section.optString(key, "");
    if (name.isEmpty()) return null;
    String uri = resolved.get(name);
    return uri != null ? uri : name;
  }

  private String pick(String name) {
    if (name == null || name.isEmpty()) return null;
    String uri = resolved.get(name);
    return uri != null ? uri : name;
  }

  private static JSONObject readJson(Context context, String path) {
    try (InputStream is = context.getAssets().open(path)) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buf = new byte[8192];
      int n;
      while ((n = is.read(buf)) > 0) out.write(buf, 0, n);
      return new JSONObject(out.toString("UTF-8"));
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Merges the VS Code-style alias sections into the native one, native entries winning on key
   * conflicts: {@code fileExtensions} into {@code extensions}, {@code fileNames} into {@code
   * filenames}, {@code folderNames}/{@code folderNamesExpanded} into {@code folders}. Returns
   * {@code null} when no section is present so the contributor answers nothing for that category.
   */
  private static JSONObject merged(JSONObject root, String primaryKey, String... aliasKeys) {
    if (root == null) return null;
    JSONObject out = new JSONObject();
    if (aliasKeys != null) {
      for (String alias : aliasKeys) putAll(out, root.optJSONObject(alias));
    }
    putAll(out, root.optJSONObject(primaryKey));
    return out.length() == 0 ? null : out;
  }

  private static void putAll(JSONObject into, JSONObject from) {
    if (from == null) return;
    for (Iterator<String> it = from.keys(); it.hasNext(); ) {
      String key = it.next();
      try {
        into.put(key, from.opt(key));
      } catch (JSONException ignored) {
      }
    }
  }

  /** Normalizes extension keys like VS Code's {@code "*.hsi"} or {@code ".hsi"} to {@code "hsi"}. */
  private static JSONObject extendKeys(JSONObject sections) {
    if (sections == null) return null;
    JSONObject out = new JSONObject();
    for (Iterator<String> it = sections.keys(); it.hasNext(); ) {
      String rawKey = it.next();
      Object value = sections.opt(rawKey);
      String key = rawKey;
      while (key.startsWith("*.")) key = key.substring(2);
      if (key.startsWith(".")) key = key.substring(1);
      if (key.isEmpty()) continue;
      try {
        out.put(key.toLowerCase(Locale.ROOT), value);
      } catch (JSONException ignored) {
      }
    }
    return out.length() == 0 ? null : out;
  }

  private static void markCustom(JSONObject section, Set<String> into) {
    if (section == null) return;
    for (Iterator<String> it = section.keys(); it.hasNext(); ) {
      String name = section.optString(it.next(), "");
      addIfPresent(into, name);
    }
  }

  private static void addIfPresent(Set<String> into, String name) {
    if (name != null && !name.isEmpty()) into.add(name);
  }

  /**
   * Keeps only the names that actually exist as artwork in the plugin assets, keyed to the real file
   * name (so {@code tmlang.png} and {@code file_type_x.svg} both work). Supported formats are SVG,
   * PNG, JPG/JPEG and WebP. An {@code asset_dir} of {@code ""}, {@code "."} or {@code "/"} means the
   * assets root, which lets a pack reuse an asset the plugin already ships (e.g. its own icon).
   */
  private static Map<String, String> bundledArtwork(
      Context context, String assetDir, Set<String> names) {
    Map<String, String> bundled = new HashMap<>();
    if (names.isEmpty()) return bundled;
    try {
      for (String file : context.getAssets().list(listDir(assetDir))) {
        String lower = file.toLowerCase(Locale.ROOT);
        for (String ext : ARTWORK_EXTENSIONS) {
          if (lower.endsWith(ext)) {
            String name = file.substring(0, file.length() - ext.length());
            if (names.contains(name)) bundled.put(name, file);
            break;
          }
        }
      }
    } catch (Exception ignored) {
      Log.e(TAG, ignored.getLocalizedMessage());
    }
    return bundled;
  }

  private void extractArtwork(
      Context context, String assetDir, Map<String, String> artwork, File outDir) {
    if (artwork.isEmpty()) return;
    for (Map.Entry<String, String> entry : artwork.entrySet()) {
      String name = entry.getKey();
      String file = entry.getValue();
      String ext = file.substring(file.lastIndexOf('.'));
      File target = new File(outDir, safe(name) + ext);
      try (InputStream is = context.getAssets().open(assetPath(assetDir, file))) {
        outDir.mkdirs();
        try (FileOutputStream fos = new FileOutputStream(target)) {
          byte[] buf = new byte[8192];
          int n;
          while ((n = is.read(buf)) > 0) fos.write(buf, 0, n);
        }
        resolved.put(name, "file://" + target.getAbsolutePath());
      } catch (Exception ignored) {
        Log.e(TAG, ignored.getMessage());
      }
    }
  }

  /** Normalizes an {@code asset_dir} to an {@link android.content.res.AssetManager} folder. */
  private static String listDir(String assetDir) {
    if (assetDir == null) return "";
    String dir = assetDir.trim();
    if (dir.equals(".") || dir.equals("./") || dir.equals("/")) return "";
    return dir;
  }

  private static String assetPath(String assetDir, String file) {
    String dir = listDir(assetDir);
    return dir.isEmpty() ? file : dir + "/" + file;
  }

  private static String safe(String raw) {
    return raw.replaceAll("[^a-zA-Z0-9._-]", "_");
  }
}
