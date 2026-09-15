package ir.hanzodev1375.ghostide.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import ir.hanzodev1375.components.sheet.BaseBlurBottomSheet;
import ir.hanzodev1375.ghostide.adapters.CommandAdapter;
import ir.hanzodev1375.ghostide.models.CommandItem;
import ir.hanzodev1375.ghostide.databinding.DialogSheetCommandsBinding;
import ir.theme.M3Theme;
import java.util.ArrayList;
import java.util.List;

public final class CommandPaletteSheet extends BaseBlurBottomSheet {

  public static final String TAG = "CommandPaletteSheet";

  public interface OnCommandListener {
    void onCommandSelected(CommandItem item);
  }

  private OnCommandListener commandListener;
  private List<CommandItem> commands = new ArrayList<>();
  private DialogSheetCommandsBinding binding;
  private CommandAdapter adapter;

  public static CommandPaletteSheet newInstance() {
    return new CommandPaletteSheet();
  }

  public void setCommands(List<CommandItem> commands) {
    this.commands = commands == null ? new ArrayList<>() : commands;
  }

  public void setOnCommandListener(OnCommandListener l) {
    this.commandListener = l;
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
    binding = DialogSheetCommandsBinding.inflate(getLayoutInflater(), contentContainer, false);
    contentContainer.addView(binding.getRoot());

    adapter = new CommandAdapter();
    adapter.setOnCommandClickListener(
        item -> {
          if (commandListener != null) {
            commandListener.onCommandSelected(item);
          }
          dismiss();
        });
    binding.rvCommands.setLayoutManager(new LinearLayoutManager(requireContext()));
    binding.rvCommands.setAdapter(adapter);
    adapter.submit(commands);

    M3Theme.applyTopLevel(binding.getRoot());
    binding.tvCommandsTitle.setTextColor(M3Theme.onSurface());
    binding.tvCommandsEmpty.setTextColor(M3Theme.onSurfaceVariant());
    binding.viewCommandsDivider.setBackgroundColor(M3Theme.outlineVariant());
    M3Theme.textInputLayout(binding.tlCommandSearch);
    M3Theme.text(binding.etCommandSearch);

    binding.etCommandSearch.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            adapter.filter(s == null ? "" : s.toString());
            boolean empty = adapter.getVisibleCount() == 0;
            binding.rvCommands.setVisibility(empty ? View.GONE : View.VISIBLE);
            binding.layoutCommandsEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
          }

          @Override
          public void afterTextChanged(Editable s) {}
        });

    binding.etCommandSearch.post(() -> binding.etCommandSearch.requestFocus());
  }
}