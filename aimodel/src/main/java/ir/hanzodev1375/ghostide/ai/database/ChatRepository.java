package ir.hanzodev1375.ghostide.ai.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import ir.hanzodev1375.ghostide.ai.model.ChatMessage;

public class ChatRepository {

  private final ChatDatabaseHelper dbHelper;

  public ChatRepository(Context context) {
    dbHelper = new ChatDatabaseHelper(context);
  }

  public long createNewChat(String title, long createdAt) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(ChatContract.ChatEntry.COLUMN_TITLE, title);
    values.put(ChatContract.ChatEntry.COLUMN_CREATED_AT, createdAt);
    long id = db.insert(ChatContract.ChatEntry.TABLE_NAME, null, values);
    db.close();
    return id;
  }

  public void updateChatTitle(long chatId, String title) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(ChatContract.ChatEntry.COLUMN_TITLE, title);
    db.update(
        ChatContract.ChatEntry.TABLE_NAME,
        values,
        ChatContract.ChatEntry._ID + " = ?",
        new String[] {String.valueOf(chatId)});
    db.close();
  }

  public void saveMessage(long chatId, ChatMessage message) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(ChatContract.MessageEntry.COLUMN_CHAT_ID, chatId);
    values.put(ChatContract.MessageEntry.COLUMN_CONTENT, message.getContent());
    values.put(ChatContract.MessageEntry.COLUMN_TYPE, message.getType());
    values.put(ChatContract.MessageEntry.COLUMN_PROVIDER, message.getProvider());
    values.put(ChatContract.MessageEntry.COLUMN_IMAGE_URI, message.getImageUri());
    values.put(ChatContract.MessageEntry.COLUMN_TIMESTAMP, message.getTimestamp());
    db.insert(ChatContract.MessageEntry.TABLE_NAME, null, values);
    db.close();
  }

  public List<ChatMessage> getMessagesByChatId(long chatId) {
    List<ChatMessage> messages = new ArrayList<>();
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    String selection = ChatContract.MessageEntry.COLUMN_CHAT_ID + " = ?";
    String[] selectionArgs = {String.valueOf(chatId)};
    Cursor cursor =
        db.query(
            ChatContract.MessageEntry.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            ChatContract.MessageEntry.COLUMN_TIMESTAMP + " ASC");
    while (cursor.moveToNext()) {
      String content =
          cursor.getString(cursor.getColumnIndexOrThrow(ChatContract.MessageEntry.COLUMN_CONTENT));
      int type = cursor.getInt(cursor.getColumnIndexOrThrow(ChatContract.MessageEntry.COLUMN_TYPE));
      String provider =
          cursor.getString(cursor.getColumnIndexOrThrow(ChatContract.MessageEntry.COLUMN_PROVIDER));
      String imageUri =
          cursor.getString(
              cursor.getColumnIndexOrThrow(ChatContract.MessageEntry.COLUMN_IMAGE_URI));
      long timestamp =
          cursor.getLong(cursor.getColumnIndexOrThrow(ChatContract.MessageEntry.COLUMN_TIMESTAMP));
      ChatMessage msg = new ChatMessage(content, type, provider, imageUri);
      msg.setTimestamp(timestamp);
      messages.add(msg);
    }
    cursor.close();
    db.close();
    return messages;
  }

  public List<ChatItem> getAllChats() {
    List<ChatItem> chats = new ArrayList<>();
    SQLiteDatabase db = dbHelper.getReadableDatabase();
    Cursor cursor =
        db.query(
            ChatContract.ChatEntry.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            ChatContract.ChatEntry.COLUMN_CREATED_AT + " DESC");
    while (cursor.moveToNext()) {
      long id = cursor.getLong(cursor.getColumnIndexOrThrow(ChatContract.ChatEntry._ID));
      String title =
          cursor.getString(cursor.getColumnIndexOrThrow(ChatContract.ChatEntry.COLUMN_TITLE));
      long createdAt =
          cursor.getLong(cursor.getColumnIndexOrThrow(ChatContract.ChatEntry.COLUMN_CREATED_AT));
      chats.add(new ChatItem(id, title, createdAt));
    }
    cursor.close();
    db.close();
    return chats;
  }

  public void deleteChat(long chatId) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    db.delete(
        ChatContract.MessageEntry.TABLE_NAME,
        ChatContract.MessageEntry.COLUMN_CHAT_ID + " = ?",
        new String[] {String.valueOf(chatId)});
    db.delete(
        ChatContract.ChatEntry.TABLE_NAME,
        ChatContract.ChatEntry._ID + " = ?",
        new String[] {String.valueOf(chatId)});
    db.close();
  }

  public void deleteChats(List<Long> chatIds) {
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    for (long id : chatIds) {
      db.delete(
          ChatContract.MessageEntry.TABLE_NAME,
          ChatContract.MessageEntry.COLUMN_CHAT_ID + " = ?",
          new String[] {String.valueOf(id)});
      db.delete(
          ChatContract.ChatEntry.TABLE_NAME,
          ChatContract.ChatEntry._ID + " = ?",
          new String[] {String.valueOf(id)});
    }
    db.close();
  }

  public static class ChatItem {
    public final long id;
    public final String title;
    public final long createdAt;

    public ChatItem(long id, String title, long createdAt) {
      this.id = id;
      this.title = title;
      this.createdAt = createdAt;
    }
  }
}
