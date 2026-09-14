package ir.hanzodev1375.ghostide.plugin;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PluginConfigModel {
  public String id;
  public String name;
  public String version;
  public String mainClass;
  public String supportedFileTypes;
  public boolean hasSettings;
  public String apkFile;
  public boolean isEnabled;

  public static List<PluginConfigModel> loadFromFile(String configPath) {
    List<PluginConfigModel> list = new ArrayList<>();
    if (configPath == null || configPath.isEmpty()) return list;
    File configFile = new File(configPath);
    if (!configFile.exists()) {
      try {
        File parent = configFile.getParentFile();
        if (parent != null) parent.mkdirs();
        configFile.createNewFile();
        try (FileWriter writer = new FileWriter(configFile)) {
          writer.write("[]");
        }
        return list;
      } catch (Exception e) {
        e.printStackTrace();
        return list;
      }
    }
    try (InputStream is = new FileInputStream(configFile)) {
      byte[] data = new byte[is.available()];
      is.read(data);
      String json = new String(data);
      if (json.trim().isEmpty()) return list;
      JSONArray arr = new JSONArray(json);
      for (int i = 0; i < arr.length(); i++) {
        JSONObject obj = arr.getJSONObject(i);
        PluginConfigModel m = new PluginConfigModel();
        m.id = obj.getString("id");
        m.name = obj.getString("name");
        m.version = obj.getString("version");
        m.mainClass = obj.getString("mainClass");
        m.supportedFileTypes = obj.optString("supportedFileTypes", "");
        m.hasSettings = obj.optBoolean("hasSettings", false);
        m.apkFile = obj.getString("apkFile");
        m.isEnabled = obj.optBoolean("isEnabled", true);
        list.add(m);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return list;
  }
}
