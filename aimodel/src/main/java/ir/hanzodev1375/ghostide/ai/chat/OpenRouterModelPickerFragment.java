package ir.hanzodev1375.ghostide.ai.chat;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ir.hanzodev1375.components.sheet.BaseBlurBottomSheet;
import ir.hanzodev1375.components.sheet.customitemsheet.ui.DialogCompat;
import ir.hanzodev1375.components.views.GhostToast;
import ir.hanzodev1375.ghostide.ai.R;
import ir.hanzodev1375.ghostide.ai.model.OpencodeModelInfo;
import ir.hanzodev1375.ghostide.ai.network.OpenRouterModelsClient;
import ir.hanzodev1375.ghostide.ai.utils.AiPreferencesUtils;
import ir.theme.M3Theme;

/**
 * Shows every model OpenRouter offers (free, paid and currently-broken) fetched live from
 * openrouter.ai. The user picks whatever they want – GhostIDE does not filter them.
 */
public class OpenRouterModelPickerFragment extends BaseBlurBottomSheet {

  private static final int COLOR_FREE = 0xFF00C853;
  private static final int COLOR_PAID = 0xFFD50000;
  private static final int COLOR_UNAVAILABLE = 0xFFFFC107;

  public interface OnModelSelectedListener {
    void onModelSelected(String qualifiedId, String name);
  }

  private AiPreferencesUtils prefs;
  private OnModelSelectedListener listener;
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private final Handler mainHandler = new Handler(Looper.getMainLooper());

  private RecyclerView rvModels;
  private ProgressBar progress;
  private TextView emptyView;
  private ModelAdapter adapter;
  private List<OpencodeModelInfo> models = new ArrayList<>();

  public void setOnModelSelectedListener(OnModelSelectedListener listener) {
    this.listener = listener;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getContext() != null) {
      prefs = new AiPreferencesUtils(getContext());
    }
  }

  @Override
  protected void onContentReady(@Nullable ViewGroup contentContainer) {
    View view =
        LayoutInflater.from(contentContainer.getContext())
            .inflate(R.layout.bottom_sheet_opencode_models, contentContainer, false);
    contentContainer.addView(view);

    TextView title = view.findViewById(R.id.tv_models_title);
    title.setText("OpenRouter models");

    TextView subtitle = view.findViewById(R.id.tv_models_subtitle);
    subtitle.setText("Everything OpenRouter lists — free, paid or currently broken. Pick any.");

    rvModels = view.findViewById(R.id.rv_models);
    progress = view.findViewById(R.id.progress_models);
    emptyView = view.findViewById(R.id.empty_models);

    adapter = new ModelAdapter();
    rvModels.setLayoutManager(new LinearLayoutManager(getContext()));
    rvModels.setAdapter(adapter);
    rvModels.setVisibility(View.GONE);

    M3Theme.applyTopLevel(view);
    loadModels();
  }

  private void loadModels() {
    if (progress != null) progress.setVisibility(View.VISIBLE);
    executor.execute(
        () -> {
          try {
            List<OpencodeModelInfo> loaded = new OpenRouterModelsClient().getModels();
            mainHandler.post(
                () -> {
                  models.clear();
                  models.addAll(loaded);
                  if (progress != null) progress.setVisibility(View.GONE);
                  if (models.isEmpty()) {
                    emptyView.setText("No models returned by OpenRouter.");
                    emptyView.setVisibility(View.VISIBLE);
                    rvModels.setVisibility(View.GONE);
                  } else {
                    emptyView.setVisibility(View.GONE);
                    rvModels.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                  }
                });
          } catch (Exception e) {
            mainHandler.post(
                () -> {
                  if (progress != null) progress.setVisibility(View.GONE);
                  emptyView.setText("Failed to load models: " + e.getMessage());
                  emptyView.setVisibility(View.VISIBLE);
                });
          }
        });
  }

  private void onModelClicked(OpencodeModelInfo model) {
    if (model == null) {
      return;
    }
    prefs.setOpenRouterModel(model.qualifiedId());
    if (listener != null) {
      listener.onModelSelected(model.qualifiedId(), model.getName());
    }
    dismiss();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    executor.shutdownNow();
  }

  private class ModelAdapter extends RecyclerView.Adapter<ModelAdapter.ViewHolder> {

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      GlassChip chip = new GlassChip(parent.getContext());
      float density = parent.getResources().getDisplayMetrics().density;
      RecyclerView.LayoutParams lp =
          new RecyclerView.LayoutParams(
              RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
      lp.setMargins((int) (12 * density), 0, (int) (12 * density), (int) (6 * density));
      chip.setLayoutParams(lp);
      return new ViewHolder(chip);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      OpencodeModelInfo model = models.get(position);
      boolean topRounded = position == 0;
      boolean bottomRounded = position == models.size() - 1;
      holder.chip.setGroupPosition(topRounded, bottomRounded);

      String name = model.getName();
      holder.chip.setLabel(name != null && !name.isEmpty() ? name : model.getModelId());
      holder.chip.getLabelView().setTextColor(Color.WHITE);

      int dotColor =
          !model.isAvailable()
              ? COLOR_UNAVAILABLE
              : model.isFree() ? COLOR_FREE : COLOR_PAID;
      holder.chip.setStatusDot(dotColor);

      Integer primary = M3Theme.primary();
      if (prefs.getOpenRouterModel().equals(model.qualifiedId()) && primary != null) {
        holder.chip.tint(primary);
      } else {
        holder.chip.clearTint();
      }

      holder.chip.setOnClickListener(v -> onModelClicked(model));
    }

    @Override
    public int getItemCount() {
      return models.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
      final GlassChip chip;

      ViewHolder(@NonNull View itemView) {
        super(itemView);
        chip = (GlassChip) itemView;
      }
    }
  }
}