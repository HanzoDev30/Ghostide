package ir.hanzodev1375.ghostide.ai.chat;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.ext.tasklist.TaskListPlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;

/** Shared Markwon instance used to render AI chat answers as markdown. */
public final class ChatMarkwon {

  private static volatile Markwon markwon;

  private ChatMarkwon() {
    throw new AssertionError("No instances.");
  }

  @NonNull
  private static Markwon get(@NonNull Context context) {
    if (markwon == null) {
      synchronized (ChatMarkwon.class) {
        if (markwon == null) {
          markwon =
              Markwon.builder(context.getApplicationContext())
                  .usePlugin(StrikethroughPlugin.create())
                  .usePlugin(LinkifyPlugin.create())
                  .usePlugin(TablePlugin.create(context))
                  .usePlugin(TaskListPlugin.create(context))
                  .usePlugin(HtmlPlugin.create())
                  .build();
        }
      }
    }
    return markwon;
  }

  public static void setMarkdown(@NonNull TextView textView, @NonNull String markdown) {
    get(textView.getContext()).setMarkdown(textView, markdown);
  }
}