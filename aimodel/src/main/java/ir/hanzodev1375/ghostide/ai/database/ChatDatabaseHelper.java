package ir.hanzodev1375.ghostide.ai.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ChatDatabaseHelper extends SQLiteOpenHelper {

  private static final String DATABASE_NAME = "ghost_ai.db";
  private static final int DATABASE_VERSION = 1;

  private static final String SQL_CREATE_CHATS =
      "CREATE TABLE "
          + ChatContract.ChatEntry.TABLE_NAME
          + " ("
          + ChatContract.ChatEntry._ID
          + " INTEGER PRIMARY KEY AUTOINCREMENT, "
          + ChatContract.ChatEntry.COLUMN_TITLE
          + " TEXT, "
          + ChatContract.ChatEntry.COLUMN_CREATED_AT
          + " INTEGER)";

  private static final String SQL_CREATE_MESSAGES =
      "CREATE TABLE "
          + ChatContract.MessageEntry.TABLE_NAME
          + " ("
          + ChatContract.MessageEntry._ID
          + " INTEGER PRIMARY KEY AUTOINCREMENT, "
          + ChatContract.MessageEntry.COLUMN_CHAT_ID
          + " INTEGER, "
          + ChatContract.MessageEntry.COLUMN_CONTENT
          + " TEXT, "
          + ChatContract.MessageEntry.COLUMN_TYPE
          + " INTEGER, "
          + ChatContract.MessageEntry.COLUMN_PROVIDER
          + " TEXT, "
          + ChatContract.MessageEntry.COLUMN_IMAGE_URI
          + " TEXT, "
          + ChatContract.MessageEntry.COLUMN_TIMESTAMP
          + " INTEGER)";

  public ChatDatabaseHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(SQL_CREATE_CHATS);
    db.execSQL(SQL_CREATE_MESSAGES);
    db.execSQL(
        "CREATE INDEX IF NOT EXISTS idx_messages_chat_id ON "
            + ChatContract.MessageEntry.TABLE_NAME
            + "("
            + ChatContract.MessageEntry.COLUMN_CHAT_ID
            + ")");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS " + ChatContract.MessageEntry.TABLE_NAME);
    db.execSQL("DROP TABLE IF EXISTS " + ChatContract.ChatEntry.TABLE_NAME);
    onCreate(db);
  }
}
