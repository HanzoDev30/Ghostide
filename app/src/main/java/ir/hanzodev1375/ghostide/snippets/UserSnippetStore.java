package ir.hanzodev1375.ghostide.snippets;

import android.content.Context;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class UserSnippetStore {

  private static final Gson GSON = new Gson();

  private UserSnippetStore() {}

  public static File snippetsFile(Context context) {
    File dir = new File(context.getFilesDir(), "GhostIDE");
    if (!dir.exists()) {
      dir.mkdirs();
    }
    return new File(dir, "snippets.json");
  }

  public static List<SnippetEntry> load(Context context) {
    File file = snippetsFile(context);
    if (!file.exists()) {
      return new ArrayList<>();
    }
    try (FileReader reader = new FileReader(file)) {
      SnippetEntry[] entries = GSON.fromJson(reader, SnippetEntry[].class);
      List<SnippetEntry> list = new ArrayList<>();
      if (entries != null) {
        for (SnippetEntry entry : entries) {
          if (entry != null && entry.body != null && !entry.body.isEmpty()) {
            list.add(entry);
          }
        }
      }
      return list;
    } catch (IOException e) {
      return new ArrayList<>();
    }
  }

  public static void save(Context context, List<SnippetEntry> entries) {
    File file = snippetsFile(context);
    try (FileWriter writer = new FileWriter(file)) {
      GSON.toJson(entries == null ? new ArrayList<>() : entries, writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}