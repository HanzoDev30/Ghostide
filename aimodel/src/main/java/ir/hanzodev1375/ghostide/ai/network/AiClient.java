package ir.hanzodev1375.ghostide.ai.network;

import java.util.List;

import ir.hanzodev1375.ghostide.ai.model.AttachedFile;
import ir.hanzodev1375.ghostide.ai.model.ChatMessage;

public interface AiClient {

  interface Callback {
    void onSuccess(String response);

    void onError(String errorMessage);
  }

  /**
   * Send a message, optionally with attached files.
   *
   * @param history Conversation history (TYPE_USER / TYPE_AI only)
   * @param userMessage The user's text (may be empty when only files are attached)
   * @param attachments Files to include (images as vision, text files as inline content)
   * @param callback Called on the main thread when done
   */
  void sendMessage(
      List<ChatMessage> history,
      String userMessage,
      List<AttachedFile> attachments,
      Callback callback);

  String getProviderName();
}
