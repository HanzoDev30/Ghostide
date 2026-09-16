package ir.hanzodev1375.ghostide.settings;

import ir.hanzodev1375.components.TextInputDialogFragment;
import ir.hanzodev1375.components.sheet.customitemsheet.ui.DialogCompat;
import ir.hanzodev1375.components.views.GhostToast;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.activity.SettingActivity;
import ir.hanzodev1375.ghostide.ai.utils.AiConstants;
import ir.hanzodev1375.ghostide.ai.utils.AiPreferencesUtils;
import ir.hanzodev1375.ghostide.models.SettingItem;
import java.util.ArrayList;
import java.util.List;

public class AiSettingSection extends SettingSection {

  private static final int POS_PROVIDER = 0;
  private static final int POS_CLAUDE = 1;
  private static final int POS_CHATGPT = 2;
  private static final int POS_DEEPSEEK = 3;
  private static final int POS_GEMINI = 4;
  private static final int POS_OPENROUTER = 5;
  private static final int POS_OPENCODE_SERVER = 6;
  private static final int POS_OPENCODE_PASSWORD = 7;

  private final AiPreferencesUtils aiPrefs;

  public AiSettingSection(SettingActivity activity, AiPreferencesUtils aiPrefs) {
    super(activity);
    this.aiPrefs = aiPrefs;
  }

  @Override
  protected List<SettingItem> buildItems() {
    List<SettingItem> items = new ArrayList<>();
    items.add(
        new SettingItem(
            getString(R.string.ai_provider),
            getString(R.string.ai_provider_desc, getProviderDisplay(aiPrefs.getSelectedProvider())),
            false,
            0,
            null));
    items.add(
        new SettingItem(
            getString(R.string.claude_api_key),
            aiPrefs.hasClaudeApiKey() ? "*********" : getString(R.string.not_set),
            false,
            0,
            null));
    items.add(
        new SettingItem(
            getString(R.string.chatgpt_api_key),
            aiPrefs.hasChatGptApiKey() ? "*********" : getString(R.string.not_set),
            false,
            0,
            null));
    items.add(
        new SettingItem(
            getString(R.string.deepseek_api_key),
            aiPrefs.hasDeepSeekApiKey() ? "*********" : getString(R.string.not_set),
            false,
            0,
            null));
    items.add(
        new SettingItem(
            getString(R.string.gemini_api_key),
            aiPrefs.hasGeminiApiKey() ? "*********" : getString(R.string.not_set),
            false,
            0,
            null));
    items.add(
        new SettingItem(
            getString(R.string.pref_openrouter_api_key),
            aiPrefs.hasOpenRouterApiKey() ? "*********" : getString(R.string.not_set),
            false,
            0,
            null));
    items.add(
        new SettingItem(
            getString(R.string.opencode_server),
            aiPrefs.getOpencodeUrl(),
            false,
            0,
            null));
    items.add(
        new SettingItem(
            getString(R.string.opencode_password),
            aiPrefs.getOpencodePassword().isEmpty()
                ? getString(R.string.not_set)
                : "*********",
            false,
            0,
            null));
    return items;
  }

  @Override
  public void onItemClick(int position) {
    switch (position) {
      case POS_PROVIDER:
        showProviderDialog();
        break;
      case POS_CLAUDE:
        showApiKeyDialog("claude", POS_CLAUDE);
        break;
      case POS_CHATGPT:
        showApiKeyDialog("chatgpt", POS_CHATGPT);
        break;
      case POS_DEEPSEEK:
        showApiKeyDialog("deepseek", POS_DEEPSEEK);
        break;
      case POS_GEMINI:
        showApiKeyDialog("gemini", POS_GEMINI);
        break;
      case POS_OPENROUTER:
        showApiKeyDialog("openrouter", POS_OPENROUTER);
        break;
      case POS_OPENCODE_SERVER:
        showOpencodeServerDialog();
        break;
      case POS_OPENCODE_PASSWORD:
        showOpencodePasswordDialog();
        break;
      default:
        break;
    }
  }

  // ---------------------------------------------------------------------
  // Dialogs
  // ---------------------------------------------------------------------

  private void showProviderDialog() {
    String[] providers = {
      "Claude", "ChatGPT", "DeepSeek", "Gemini", "OpenRouter", "OpenCode (Local)"
    };
    String[] values = {
      AiConstants.AiProvider.CLAUDE,
      AiConstants.AiProvider.CHATGPT,
      AiConstants.AiProvider.DEEPSEEK,
      AiConstants.AiProvider.GEMINI,
      AiConstants.AiProvider.OPENROUTER,
      AiConstants.AiProvider.OPENCODE
    };
    int checked = 0;
    String current = aiPrefs.getSelectedProvider();
    for (int i = 0; i < values.length; i++) {
      if (values[i].equals(current)) {
        checked = i;
        break;
      }
    }
    new DialogCompat(activity)
        .setTitle(R.string.ai_provider)
        .setSingleChoiceItems(
            providers,
            checked,
            (dialog, which) -> {
              aiPrefs.setSelectedProvider(values[which]);
              dialog.dismiss();
              updateDescriptionAt(POS_PROVIDER, getString(R.string.ai_provider_desc, providers[which]));
            })
        .setNegativeButton(R.string.cancel, null)
        .show();
  }

  private void showApiKeyDialog(String provider, int position) {
    String title = apiTitle(provider);
    String currentKey = apiKey(provider);
    TextInputDialogFragment.newInstance(
            title, "Enter API key", currentKey.isEmpty() ? null : currentKey)
        .setCallback(
            text -> {
              if (text.isEmpty()) {
                clearApiKey(provider);
                GhostToast.makeText(activity, R.string.key_cleared, GhostToast.LENGTH_SHORT).show();
                updateDescriptionAt(position, getString(R.string.not_set));
              } else {
                saveApiKey(provider, text);
                GhostToast.makeText(activity, R.string.key_saved, GhostToast.LENGTH_SHORT).show();
                updateDescriptionAt(position, "*********");
              }
            })
        .show(activity.getSupportFragmentManager(), "api_key_dialog");
  }

  private void showOpencodeServerDialog() {
    String current = aiPrefs.getOpencodeUrl();
    TextInputDialogFragment.newInstance(
            getString(R.string.opencode_server),
            getString(R.string.opencode_server_desc),
            current.isEmpty() ? null : current)
        .setCallback(
            text -> {
              if (text.isEmpty()) {
                aiPrefs.setOpencodeUrl("");
                aiPrefs.setOpencodePassword("");
                GhostToast.makeText(activity, R.string.key_cleared, GhostToast.LENGTH_SHORT).show();
              } else {
                aiPrefs.setOpencodeUrl(text.trim());
                GhostToast.makeText(activity, R.string.key_saved, GhostToast.LENGTH_SHORT).show();
              }
              updateDescriptionAt(POS_OPENCODE_SERVER, aiPrefs.getOpencodeUrl());
            })
        .show(activity.getSupportFragmentManager(), "opencode_server_dialog");
  }

  private void showOpencodePasswordDialog() {
    TextInputDialogFragment.newInstance(
            getString(R.string.opencode_password),
            getString(R.string.opencode_password_desc),
            null)
        .setCallback(
            text -> {
              aiPrefs.setOpencodePassword(text.trim());
              GhostToast.makeText(activity, R.string.key_saved, GhostToast.LENGTH_SHORT).show();
              updateDescriptionAt(
                  POS_OPENCODE_PASSWORD,
                  text.isEmpty()
                      ? getString(R.string.opencode_password_desc)
                      : "*********");
            })
        .show(activity.getSupportFragmentManager(), "opencode_password_dialog");
  }

  // ---------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------

  private String getProviderDisplay(String provider) {
    switch (provider) {
      case AiConstants.AiProvider.CLAUDE:
        return "Claude";
      case AiConstants.AiProvider.CHATGPT:
        return "ChatGPT";
      case AiConstants.AiProvider.DEEPSEEK:
        return "DeepSeek";
      case AiConstants.AiProvider.GEMINI:
        return "Gemini";
      case AiConstants.AiProvider.OPENROUTER:
        return "OpenRouter";
      case AiConstants.AiProvider.OPENCODE:
        return "OpenCode";
      default:
        return "";
    }
  }

  private String apiTitle(String provider) {
    switch (provider) {
      case "claude":
        return getString(R.string.claude_api_key);
      case "chatgpt":
        return getString(R.string.chatgpt_api_key);
      case "deepseek":
        return getString(R.string.deepseek_api_key);
      case "gemini":
        return getString(R.string.gemini_api_key);
      case "openrouter":
        return getString(R.string.pref_openrouter_api_key);
      default:
        return "";
    }
  }

  private String apiKey(String provider) {
    switch (provider) {
      case "claude":
        return aiPrefs.getClaudeApiKey();
      case "chatgpt":
        return aiPrefs.getChatGptApiKey();
      case "deepseek":
        return aiPrefs.getDeepSeekApiKey();
      case "gemini":
        return aiPrefs.getGeminiApiKey();
      case "openrouter":
        return aiPrefs.getOpenRouterApiKey();
      default:
        return "";
    }
  }

  private void saveApiKey(String provider, String text) {
    switch (provider) {
      case "claude":
        aiPrefs.setClaudeApiKey(text);
        break;
      case "chatgpt":
        aiPrefs.setChatGptApiKey(text);
        break;
      case "deepseek":
        aiPrefs.setDeepSeekApiKey(text);
        break;
      case "gemini":
        aiPrefs.setGeminiApiKey(text);
        break;
      case "openrouter":
        aiPrefs.setOpenRouterApiKey(text);
        break;
    }
  }

  private void clearApiKey(String provider) {
    switch (provider) {
      case "claude":
        aiPrefs.setClaudeApiKey("");
        break;
      case "chatgpt":
        aiPrefs.setChatGptApiKey("");
        break;
      case "deepseek":
        aiPrefs.setDeepSeekApiKey("");
        break;
      case "gemini":
        aiPrefs.setGeminiApiKey("");
        break;
      case "openrouter":
        aiPrefs.setOpenRouterApiKey("");
        break;
    }
  }
}