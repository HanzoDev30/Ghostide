package ir.hanzodev1375.ghostide.settings;

import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.widget.LinearLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import ir.hanzodev1375.components.sheet.customitemsheet.ui.DialogCompat;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.activity.SettingActivity;
import ir.hanzodev1375.ghostide.codeeditors.setting.PreferencesUtils;
import ir.hanzodev1375.ghostide.models.SettingItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Toggles for every code runner and every built-in LSP server. Everything defaults to ON. The last
 * row lets the user define custom runners (extension + command).
 */
public class CodeRunnerSetting extends SettingSection {

    private static final int CUSTOM_ROW_INDEX = 28;

    private final PreferencesUtils prefs;

    public CodeRunnerSetting(SettingActivity activity, PreferencesUtils prefs) {
        super(activity);
        this.prefs = prefs;
    }

    @Override
    protected List<SettingItem> buildItems() {
        List<SettingItem> items = new ArrayList<>();

        items.add(
                switchItem(
                R.string.runner_master_title,
                R.string.runner_master_desc,
                prefs.isCodeRunnerEnabled(),
                prefs::setCodeRunnerEnabled));
        items.add(
                switchItem(
                R.string.runner_shell_title,
                R.string.runner_shell_desc,
                prefs.isRunnerEnabled(PreferencesUtils.KEY_RUNNER_SHELL),
                checked -> prefs.setRunnerEnabled(PreferencesUtils.KEY_RUNNER_SHELL, checked)));

        items.add(runnerItem(R.string.runner_c_title, PreferencesUtils.KEY_RUNNER_C));
        items.add(runnerItem(R.string.runner_cpp_title, PreferencesUtils.KEY_RUNNER_CPP));
        items.add(runnerItem(R.string.runner_python_title, PreferencesUtils.KEY_RUNNER_PYTHON));
        items.add(runnerItem(R.string.runner_go_title, PreferencesUtils.KEY_RUNNER_GO));
        items.add(runnerItem(R.string.runner_node_title, PreferencesUtils.KEY_RUNNER_NODE));
        items.add(runnerItem(R.string.runner_typescript_title, PreferencesUtils.KEY_RUNNER_TYPESCRIPT));
        items.add(runnerItem(R.string.runner_php_title, PreferencesUtils.KEY_RUNNER_PHP));
        items.add(runnerItem(R.string.runner_lua_title, PreferencesUtils.KEY_RUNNER_LUA));
        items.add(runnerItem(R.string.runner_java_title, PreferencesUtils.KEY_RUNNER_JAVA));
        items.add(runnerItem(R.string.runner_kotlin_title, PreferencesUtils.KEY_RUNNER_KOTLIN));
        items.add(runnerItem(R.string.runner_sass_title, PreferencesUtils.KEY_RUNNER_SASS));

        items.add(lspItem(R.string.lsp_extra_gth_title, "GthServer"));
        items.add(lspItem(R.string.lsp_extra_python_title, "PylspServer"));
        items.add(lspItem(R.string.lsp_extra_cpp_title, "ClangdServer"));
        items.add(lspItem(R.string.lsp_extra_go_title, "GoServer"));
        items.add(lspItem(R.string.lsp_extra_csharp_title, "CsharpServer"));
        items.add(lspItem(R.string.lsp_extra_ts_title, "TsServer"));
        items.add(lspItem(R.string.lsp_extra_vue_title, "VueServer"));
        items.add(lspItem(R.string.lsp_extra_html_title, "HtmlServer"));
        items.add(lspItem(R.string.lsp_extra_css_title, "CssServer"));
        items.add(lspItem(R.string.lsp_extra_sass_title, "SassServer"));
        items.add(lspItem(R.string.lsp_extra_php_title, "PhpServer"));
        items.add(lspItem(R.string.lsp_extra_json_title, "JsonServer"));
        items.add(lspItem(R.string.lsp_extra_markdown_title, "MarkdownServer"));
        items.add(lspItem(R.string.lsp_extra_ruby_title, "RubyServer"));
        items.add(lspItem(R.string.lsp_extra_emmet_title, "EmmetServer"));

        items.add(textItem(R.string.runner_custom_title, R.string.runner_custom_desc));
        return items;
    }

    private SettingItem runnerItem(int titleRes, String key) {
        return switchItem(titleRes, R.string.runner_code_desc, prefs.isRunnerEnabled(key),
        checked -> prefs.setRunnerEnabled(key, checked));
    }

    private SettingItem lspItem(int titleRes, String tag) {
        return switchItem(titleRes, R.string.runner_lsp_desc, prefs.isLspServerEnabled(tag),
        checked -> prefs.setLspServerEnabled(tag, checked));
    }

    @Override
    public void onItemClick(int position) {
        if (position == CUSTOM_ROW_INDEX) showCustomRunnersDialog();
    }

    private void showCustomRunnersDialog() {
        TextInputLayout input = new TextInputLayout(activity);
        input.setHint(getString(R.string.runner_custom_hint));

        TextInputEditText edit = new TextInputEditText(input.getContext());
        edit.setGravity(Gravity.TOP | Gravity.START);
        edit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        edit.setMinLines(8);
        edit.setTypeface(Typeface.MONOSPACE);
        edit.setText(prefs.getCustomRunnersText());
        input.addView(
                edit,
                new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        new DialogCompat(activity)
                .setTitle(R.string.runner_custom_title)
                .setMessage(R.string.runner_custom_desc)
                .setView(input)
                .setPositiveButton(
                        R.string.ok, (d, w) -> prefs.setCustomRunnersText(String.valueOf(edit.getText())))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}