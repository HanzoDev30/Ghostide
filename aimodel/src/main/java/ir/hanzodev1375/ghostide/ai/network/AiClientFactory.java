package ir.hanzodev1375.ghostide.ai.network;

import android.content.Context;
import ir.hanzodev1375.ghostide.ai.utils.AiConstants;
import ir.hanzodev1375.ghostide.ai.utils.AiPreferencesUtils;

public class AiClientFactory {

  private AiClientFactory() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static AiClient create(Context context) {
    AiPreferencesUtils prefs = new AiPreferencesUtils(context);
    String provider = prefs.getSelectedProvider();
    return createForProvider(provider, prefs);
  }

  public static AiClient createForProvider(String provider, AiPreferencesUtils prefs) {
    switch (provider) {
      case AiConstants.AiProvider.CLAUDE:
        return new ClaudeClient(prefs.getClaudeApiKey(), prefs.getClaudeModel());
      case AiConstants.AiProvider.CHATGPT:
        return new OpenAiCompatibleClient(
            prefs.getChatGptApiKey(),
            prefs.getChatGptModel(),
            AiConstants.ApiEndpoints.CHATGPT_BASE_URL,
            AiConstants.AiProvider.CHATGPT);
      case AiConstants.AiProvider.DEEPSEEK:
        return new OpenAiCompatibleClient(
            prefs.getDeepSeekApiKey(),
            prefs.getDeepSeekModel(),
            AiConstants.ApiEndpoints.DEEPSEEK_BASE_URL,
            AiConstants.AiProvider.DEEPSEEK);
      case AiConstants.AiProvider.OPENROUTER:
        String openRouterModel = prefs.getOpenRouterModel();
        if (openRouterModel == null
            || openRouterModel.trim().isEmpty()
            || !openRouterModel.contains("/")) {
          openRouterModel = "qwen/qwen3.6-plus-preview:free";
        }
        String openRouterBaseUrl = AiConstants.ApiEndpoints.OPENROUTER_BASE_URL;
        if (!openRouterBaseUrl.endsWith("/chat/completions")) {
          if (openRouterBaseUrl.endsWith("/")) {
            openRouterBaseUrl = openRouterBaseUrl + "chat/completions";
          } else {
            openRouterBaseUrl = openRouterBaseUrl + "/chat/completions";
          }
        }
        return new OpenAiCompatibleClient(
            prefs.getOpenRouterApiKey(),
            openRouterModel,
            openRouterBaseUrl,
            AiConstants.AiProvider.OPENROUTER);
      case AiConstants.AiProvider.GEMINI:
        return new GeminiClient(prefs.getGeminiApiKey(), prefs.getGeminiModel());
      default:
        return new ClaudeClient(prefs.getClaudeApiKey(), prefs.getClaudeModel());
    }
  }
}
