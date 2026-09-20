package ir.hanzodev1375.ghostide.ai.chat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;

import ir.hanzodev1375.ghostide.ai.R;
import ir.hanzodev1375.ghostide.ai.model.ChatMessage;
import ir.theme.M3Theme;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final int ID_SELECT_ALL = 0x6001;
  private static final int ID_COPY = 0x6002;
  private static final int ID_CUT = 0x6003;

  /** Lets the Activity persist edits triggered by the bubble's Cut action. */
  public interface ChatMessageActionCallback {
    void onMessageEdited(ChatMessage message);
  }

  private final List<ChatMessage> messages;
  private final ChatMessageActionCallback actionCallback;
  private boolean animateNextBind;
  private RecyclerView attachedTo;

  private final RecyclerView.OnScrollListener scrollInvalidator =
      new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
          for (int i = 0; i < rv.getChildCount(); i++) {
            View child = rv.getChildAt(i);
            if (child instanceof ChatFadeView) {
              child.invalidate();
            }
          }
        }
      };

  public ChatAdapter(List<ChatMessage> messages) {
    this(messages, null);
  }

  public ChatAdapter(List<ChatMessage> messages, ChatMessageActionCallback actionCallback) {
    this.messages = messages;
    this.actionCallback = actionCallback;
  }

  @Override
  public void onAttachedToRecyclerView(@NonNull RecyclerView rv) {
    attachedTo = rv;
    rv.addOnScrollListener(scrollInvalidator);
  }

  @Override
  public void onDetachedFromRecyclerView(@NonNull RecyclerView rv) {
    rv.removeOnScrollListener(scrollInvalidator);
    attachedTo = null;
  }

  public void animateNextInsert() {
    animateNextBind = true;
  }

  @Override
  public int getItemViewType(int position) {
    return messages.get(position).getType();
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    switch (viewType) {
      case ChatMessage.TYPE_USER:
        return new UserViewHolder(inflater.inflate(R.layout.item_chat_user, parent, false));
      case ChatMessage.TYPE_AI:
        return new AiViewHolder(inflater.inflate(R.layout.item_chat_ai, parent, false));
      case ChatMessage.TYPE_LOADING:
        return new LoadingViewHolder(inflater.inflate(R.layout.item_chat_loading, parent, false));
      default:
        return new ErrorViewHolder(inflater.inflate(R.layout.item_chat_error, parent, false));
    }
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    ChatMessage msg = messages.get(position);

    if (holder instanceof UserViewHolder) {
      UserViewHolder userHolder = (UserViewHolder) holder;

      if (!TextUtils.isEmpty(msg.getContent())) {
        userHolder.tvMessage.setText(msg.getContent());
        userHolder.tvMessage.setVisibility(View.VISIBLE);
      } else {
        userHolder.tvMessage.setVisibility(View.GONE);
      }

      if (msg.getImageUri() != null && !msg.getImageUri().isEmpty()) {
        userHolder.ivImage.setVisibility(View.VISIBLE);
        Glide.with(userHolder.itemView.getContext())
            .load(msg.getImageUri())
            .centerCrop()
            .into(userHolder.ivImage);
      } else {
        userHolder.ivImage.setVisibility(View.GONE);
      }

    } else if (holder instanceof AiViewHolder) {
      AiViewHolder aiHolder = (AiViewHolder) holder;
      String content = msg.getContent() == null ? "" : msg.getContent();
      if (content.trim().isEmpty()) {
        aiHolder.tvMessage.setText("");
        aiHolder.tvMessage.setVisibility(View.GONE);
      } else {
        ChatMarkwon.setMarkdown(aiHolder.tvMessage, content);
        aiHolder.tvMessage.setVisibility(View.VISIBLE);
        aiHolder.tvMessage.setTextIsSelectable(true);
        aiHolder.setupSelectionActions(
            msg,
            actionCallback,
            () -> {
              int p = aiHolder.getBindingAdapterPosition();
              if (p != RecyclerView.NO_POSITION) {
                notifyItemChanged(p);
              }
            });
      }
      aiHolder.tvProvider.setText(msg.getProvider().toUpperCase());
    } else if (holder instanceof ErrorViewHolder) {
      ((ErrorViewHolder) holder).tvError.setText(msg.getContent());
    }
    M3Theme.listCard(holder.itemView);
    if (animateNextBind) {
      animateNextBind = false;
      animateIn(holder.itemView, holder.getBindingAdapterPosition());
    }
  }

  private void animateIn(View view, int position) {
    float offset = 24 * view.getResources().getDisplayMetrics().density;
    view.animate().cancel();
    view.setAlpha(0f);
    view.setTranslationY(offset);
    view.animate()
        .alpha(1f)
        .translationY(0f)
        .setDuration(280)
        .setInterpolator(new DecelerateInterpolator())
        .setStartDelay(Math.max(position, 0) * 40L)
        .start();
  }

  @Override
  public int getItemCount() {
    return messages.size();
  }

  static class UserViewHolder extends RecyclerView.ViewHolder {
    TextView tvMessage;
    ImageView ivImage;
    ChatFadeView chatFadeView;

    UserViewHolder(@NonNull View itemView) {
      super(itemView);
      tvMessage = itemView.findViewById(R.id.tv_message_user);
      ivImage = itemView.findViewById(R.id.iv_user_image);
      chatFadeView = itemView.findViewById(R.id.chat_fade_view);
      chatFadeView.setFadeHeightsDp(44, 36);
      chatFadeView.setTailSide(ChatFadeView.TAIL_RIGHT);
    }
  }

  static class AiViewHolder extends RecyclerView.ViewHolder {
    TextView tvMessage;
    TextView tvProvider;
    ChatFadeView chatFadeView;

    AiViewHolder(@NonNull View itemView) {
      super(itemView);
      tvMessage = itemView.findViewById(R.id.tv_message_ai);
      tvProvider = itemView.findViewById(R.id.tv_provider_label);
      chatFadeView = itemView.findViewById(R.id.chat_fade_view_ai);
      chatFadeView.setFadeHeightsDp(44, 36);
      chatFadeView.setTailSide(ChatFadeView.TAIL_LEFT);
    }

    void setupSelectionActions(
        ChatMessage message, ChatMessageActionCallback callback, Runnable rerender) {
      tvMessage.setCustomSelectionActionModeCallback(
          new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
              return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
              menu.clear();
              menu.add(Menu.NONE, ID_SELECT_ALL, 0, R.string.chat_select_all);
              menu.add(Menu.NONE, ID_COPY, 1, R.string.chat_copy);
              menu.add(Menu.NONE, ID_CUT, 2, R.string.chat_cut);
              return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
              CharSequence full = tvMessage.getText();
              String text = full != null ? full.toString() : "";
              if (item.getItemId() == ID_SELECT_ALL) {
                CharSequence all = tvMessage.getText();
                if (all instanceof Spannable) {
                  Selection.setSelection((Spannable) all, 0, all.length());
                }
                return true;
              }
              int start = Math.max(0, tvMessage.getSelectionStart());
              int end = Math.max(start, tvMessage.getSelectionEnd());
              String selected =
                  start < text.length() && start < end ? text.substring(start, end) : text;
              copyToClipboard(tvMessage.getContext(), selected);
              if (item.getItemId() == ID_CUT
                  && start < end
                  && start < text.length()
                  && end <= text.length()) {
                message.setContent(text.substring(0, start) + text.substring(end));
                if (callback != null) {
                  callback.onMessageEdited(message);
                }
                if (rerender != null) {
                  rerender.run();
                }
              }
              mode.finish();
              return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {}
          });
    }

    private void copyToClipboard(Context context, String text) {
      try {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
          cm.setPrimaryClip(ClipData.newPlainText("chat", text));
          Toast.makeText(context, R.string.chat_copied, Toast.LENGTH_SHORT).show();
        }
      } catch (RuntimeException ignored) {
      }
    }
  }

  static class LoadingViewHolder extends RecyclerView.ViewHolder {
    CircularProgressIndicator progressBar;

    LoadingViewHolder(@NonNull View itemView) {
      super(itemView);
      progressBar = itemView.findViewById(R.id.progress_loading);
    }
  }

  static class ErrorViewHolder extends RecyclerView.ViewHolder {
    TextView tvError;

    ErrorViewHolder(@NonNull View itemView) {
      super(itemView);
      tvError = itemView.findViewById(R.id.tv_error);
    }
  }
}
