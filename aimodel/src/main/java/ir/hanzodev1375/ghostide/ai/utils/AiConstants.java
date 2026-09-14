package ir.hanzodev1375.ghostide.ai.utils;

public class AiConstants {

    public static class AiProvider {
        public static final String CLAUDE = "claude";
        public static final String CHATGPT = "chatgpt";
        public static final String DEEPSEEK = "deepseek";
        public static final String GEMINI = "gemini";
        public static final String OPENROUTER = "openrouter";

        private AiProvider() {
            throw new UnsupportedOperationException("Utility class");
        }
    }

    public static class SharedPreferenceKeys {
        public static final String KEY_AI_CLAUDE_API_KEY = "pref_ai_claude_api_key";
        public static final String KEY_AI_CHATGPT_API_KEY = "pref_ai_chatgpt_api_key";
        public static final String KEY_AI_DEEPSEEK_API_KEY = "pref_ai_deepseek_api_key";
        public static final String KEY_AI_GEMINI_API_KEY = "pref_ai_gemini_api_key";
        public static final String KEY_AI_OPENROUTER_API_KEY = "pref_ai_openrouter_api_key";

        public static final String KEY_AI_SELECTED_PROVIDER = "pref_ai_selected_provider";
        public static final String KEY_AI_CLAUDE_MODEL = "pref_ai_claude_model";
        public static final String KEY_AI_CHATGPT_MODEL = "pref_ai_chatgpt_model";
        public static final String KEY_AI_DEEPSEEK_MODEL = "pref_ai_deepseek_model";
        public static final String KEY_AI_GEMINI_MODEL = "pref_ai_gemini_model";
        public static final String KEY_AI_OPENROUTER_MODEL = "pref_ai_openrouter_model";

        private SharedPreferenceKeys() {
            throw new UnsupportedOperationException("Utility class");
        }
    }

    public static class ApiEndpoints {
        public static final String CLAUDE_BASE_URL = "https://api.anthropic.com/v1/messages";
        public static final String CHATGPT_BASE_URL = "https://api.openai.com/v1/chat/completions";
        public static final String DEEPSEEK_BASE_URL = "https://api.deepseek.com/v1/chat/completions";
        public static final String GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1/models/";
        public static final String OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1/chat/completions";

        private ApiEndpoints() {
            throw new UnsupportedOperationException("Utility class");
        }
    }

    public static class DefaultModels {
        public static final String CLAUDE_DEFAULT = "claude-3-5-sonnet-20241022";
        public static final String CHATGPT_DEFAULT = "gpt-4o";
        public static final String DEEPSEEK_DEFAULT = "deepseek-chat";
        public static final String GEMINI_DEFAULT = "gemini-1.5-flash";
        public static final String OPENROUTER_DEFAULT = "meta-llama/llama-3.1-8b-instruct:free";

        private DefaultModels() {
            throw new UnsupportedOperationException("Utility class");
        }
    }
}
