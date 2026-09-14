package ir.hanzodev1375.ghostide.ai.database;

import android.provider.BaseColumns;

public final class ChatContract {
  private ChatContract() {}

  public static class ChatEntry implements BaseColumns {
    public static final String TABLE_NAME = "chats";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CREATED_AT = "created_at";
  }

  public static class MessageEntry implements BaseColumns {
    public static final String TABLE_NAME = "messages";
    public static final String COLUMN_CHAT_ID = "chat_id";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_PROVIDER = "provider";
    public static final String COLUMN_IMAGE_URI = "image_uri";
    public static final String COLUMN_TIMESTAMP = "timestamp";
  }
}
