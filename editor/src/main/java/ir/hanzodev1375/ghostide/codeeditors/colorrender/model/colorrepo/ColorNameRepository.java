package ir.hanzodev1375.ghostide.codeeditors.colorrender.model.colorrepo;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ir.hanzodev1375.ghostide.codeeditors.colorrender.model.ColorNameItem;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Pattern;

public class ColorNameRepository {
  private static ColorNameRepository instance;
  private final Map<String, String> nameToHex; // name -> "#RRGGBB"
  private final Pattern namePattern;

  private ColorNameRepository(Context context) {
    nameToHex = loadColors(context);
    namePattern = buildPattern();
  }

  public static synchronized ColorNameRepository getInstance(Context context) {
    if (instance == null) {
      instance = new ColorNameRepository(context.getApplicationContext());
    }
    return instance;
  }

  private Map<String, String> loadColors(Context context) {
    try (InputStreamReader reader =
        new InputStreamReader(context.getAssets().open("css/color.kj"))) {
      Type listType = new TypeToken<List<ColorNameItem>>() {}.getType();
      List<ColorNameItem> items = new Gson().fromJson(reader, listType);
      Map<String, String> map = new HashMap<>();
      for (var item : items) {
        map.put(item.name.toLowerCase(), item.desc);
      }
      return map;
    } catch (Exception e) {
      e.printStackTrace();
      return Collections.emptyMap();
    }
  }

  private Pattern buildPattern() {
    if (nameToHex.isEmpty()) return Pattern.compile("a^");
    List<String> names = new ArrayList<>(nameToHex.keySet());
    names.sort((a, b) -> Integer.compare(b.length(), a.length()));
    String regex = "\\b(" + String.join("|", names) + ")\\b";
    return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
  }

  public Map<String, String> getNameToHex() {
    return nameToHex;
  }

  public Pattern getNamePattern() {
    return namePattern;
  }

  public String getHexForName(String name) {
    return nameToHex.get(name.toLowerCase());
  }
}
