package ir.hanzodev1375.ghostide.commands;

import android.content.Context;
import android.widget.Toast;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.activity.EditorActivity;
import ir.hanzodev1375.ghostide.codeeditors.IdeEditor;
import ir.hanzodev1375.ghostide.models.CommandItem;
import java.util.ArrayList;
import java.util.List;

public final class CommandPaletteRegistry {

  public static final String CMD_SAVE = "cmd.save";
  public static final String CMD_SAVE_ALL = "cmd.save_all";
  public static final String CMD_UNDO = "cmd.undo";
  public static final String CMD_REDO = "cmd.redo";
  public static final String CMD_FORMAT = "cmd.format";
  public static final String CMD_TOGGLE_COMMENT = "cmd.toggle_comment";
  public static final String CMD_GOTO_LINE = "cmd.goto_line";
  public static final String CMD_FILE_TREE = "cmd.file_tree";
  public static final String CMD_SEARCH = "cmd.search";
  public static final String CMD_DUPLICATE_LINE = "cmd.duplicate_line";
  public static final String CMD_SELECT_WORD = "cmd.select_word";
  public static final String CMD_CLOSE_TABS = "cmd.close_tabs";

  private CommandPaletteRegistry() {}

  public static List<CommandItem> buildCommands(Context context) {
    List<CommandItem> commands = new ArrayList<>();
    commands.add(new CommandItem(CMD_SAVE, context.getString(R.string.saveitemthis), "Ctrl+S", R.drawable.save, true));
    commands.add(new CommandItem(CMD_SAVE_ALL, context.getString(R.string.saveitemall), "Ctrl+Shift+S", R.drawable.save, true));
    commands.add(new CommandItem(CMD_UNDO, "Undo", "Ctrl+Z", R.drawable.outline_undo, true));
    commands.add(new CommandItem(CMD_REDO, "Redo", "Ctrl+Y", R.drawable.outline_redo, true));
    commands.add(new CommandItem(CMD_FORMAT, "Format Code", "Format the whole file", R.drawable.ic_edit, true));
    commands.add(new CommandItem(CMD_TOGGLE_COMMENT, "Toggle Comment", "Comment or uncomment the current line", R.drawable.ic_bug_report, true));
    commands.add(new CommandItem(CMD_GOTO_LINE, "Go to Line", "Navigate to a specific line", R.drawable.ic_arrow_forward, true));
    commands.add(new CommandItem(CMD_FILE_TREE, "Show File Tree", "Toggle the project file tree", R.drawable.round_account_tree, true));
    commands.add(new CommandItem(CMD_SEARCH, "Find in File", "Open the search panel", R.drawable.outline_search, true));
    commands.add(new CommandItem(CMD_DUPLICATE_LINE, "Duplicate Line", "Copy the current line below", R.drawable.ic_content_copy, true));
    commands.add(new CommandItem(CMD_SELECT_WORD, "Select Current Word", "Select the word under the cursor", R.drawable.outline_select_all, true));
    commands.add(new CommandItem(CMD_CLOSE_TABS, "Close All Tabs", "Close every open editor tab", R.drawable.ic_close, true));
    return commands;
  }

  public static void execute(EditorActivity activity, String commandId) {
    if (activity == null || commandId == null) {
      return;
    }
    switch (commandId) {
      case CMD_SAVE -> activity.saveCurrentTab();
      case CMD_SAVE_ALL -> activity.saveAllTabs();
      case CMD_UNDO -> {
        IdeEditor editor = activity.getEditor();
        if (editor != null) editor.undo();
      }
      case CMD_REDO -> {
        IdeEditor editor = activity.getEditor();
        if (editor != null) editor.redo();
      }
      case CMD_FORMAT -> {
        IdeEditor editor = activity.getEditor();
        if (editor != null) editor.formatCodeAsync();
      }
      case CMD_TOGGLE_COMMENT -> {
        IdeEditor editor = activity.getEditor();
        if (editor != null) editor.toggleCommentForCurrentLine();
      }
      case CMD_GOTO_LINE -> activity.showGotoLineDialog();
      case CMD_FILE_TREE -> activity.stepFileTree();
      case CMD_SEARCH -> activity.stepSearch();
      case CMD_DUPLICATE_LINE -> {
        IdeEditor editor = activity.getEditor();
        if (editor != null) editor.setDuplicateLine();
      }
      case CMD_SELECT_WORD -> {
        IdeEditor editor = activity.getEditor();
        if (editor != null) editor.setSelectCurrentWord();
      }
      case CMD_CLOSE_TABS -> activity.closeAllTabs();
      default -> Toast.makeText(activity, commandId, Toast.LENGTH_SHORT).show();
    }
  }
}