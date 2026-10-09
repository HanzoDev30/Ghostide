package ir.hanzodev1375.ghostide.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import ir.hanzodev1375.components.sheet.BaseBlurBottomSheet;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.adapters.OpenFilesAdapter;
import ir.hanzodev1375.ghostide.databinding.DialogSheetOpenFilesBinding;
import ir.hanzodev1375.ghostide.models.TabModel;
import ir.theme.M3Theme;
import java.util.ArrayList;
import java.util.List;

public final class OpenFilesSheet extends BaseBlurBottomSheet {

  public static final String TAG = "OpenFilesSheet";

  public interface OnFileSelectedListener {
    void onFileSelected(TabModel tab);
  }

  private final List<TabModel> tabs = new ArrayList<>();
  private OnFileSelectedListener listener;
  private DialogSheetOpenFilesBinding binding;
  private OpenFilesAdapter adapter;

  public static OpenFilesSheet newInstance() {
    return new OpenFilesSheet();
  }

  public void setTabs(List<TabModel> source) {
    tabs.clear();
    if (source != null) {
      tabs.addAll(source);
    }
  }

  public void setOnFileSelectedListener(OnFileSelectedListener l) {
    this.listener = l;
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return super.onCreateView(inflater, container, savedInstanceState);
  }

  @Override
  protected void onContentReady(ViewGroup contentContainer) {
    binding = DialogSheetOpenFilesBinding.inflate(getLayoutInflater(), contentContainer, false);
    contentContainer.addView(binding.getRoot());

    adapter = new OpenFilesAdapter();
    adapter.setOnFileClickListener(
        position -> {
          TabModel tab = adapter.getVisibleItem(position);
          if (listener != null && tab != null) {
            listener.onFileSelected(tab);
          }
          dismiss();
        });
    binding.rvOpenFiles.setLayoutManager(new LinearLayoutManager(requireContext()));
    binding.rvOpenFiles.setAdapter(adapter);
    adapter.submit(tabs);

    M3Theme.applyTopLevel(binding.getRoot());
    binding.tvOpenFilesTitle.setTextColor(M3Theme.onSurface());
    binding.tvOpenFilesEmpty.setTextColor(M3Theme.onSurfaceVariant());
    binding.viewOpenFilesDivider.setBackgroundColor(M3Theme.outlineVariant());

    binding.searchOpenFiles.setVisibility(View.VISIBLE);
    binding.searchOpenFiles.setHint(getString(R.string.open_files_search_hint));
    M3Theme.apply(binding.searchOpenFiles);
    updateEmptyState(adapter.getVisibleCount() == 0);
    binding.searchOpenFiles.setOnTextChangedListener(
        text -> {
          adapter.filter(text);
          updateEmptyState(adapter.getVisibleCount() == 0);
        });
  }

  private void updateEmptyState(boolean empty) {
    if (binding == null) return;
    binding.rvOpenFiles.setVisibility(empty ? View.GONE : View.VISIBLE);
    binding.layoutOpenFilesEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
  }
}
