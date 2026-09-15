package ir.hanzodev1375.ghostide.dialogs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import ir.hanzodev1375.components.sheet.BaseBlurBottomSheet;
import ir.hanzodev1375.components.sheet.customitemsheet.ui.DialogCompat;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.adapters.SnippetAdapter;
import ir.hanzodev1375.ghostide.databinding.DialogSheetSnippetsBinding;
import ir.hanzodev1375.ghostide.databinding.DialogSnippetEditBinding;
import ir.hanzodev1375.ghostide.snippets.SnippetEntry;
import ir.hanzodev1375.ghostide.snippets.UserSnippetStore;
import ir.theme.M3Theme;
import java.util.ArrayList;
import java.util.List;

public final class SnippetManagerSheet extends BaseBlurBottomSheet {

  public static final String TAG = "SnippetManagerSheet";

  public interface OnInsertSnippetListener {
    void onInsertSnippet(SnippetEntry entry);
  }

  private OnInsertSnippetListener insertListener;
  private DialogSheetSnippetsBinding binding;
  private SnippetAdapter adapter;
  private List<SnippetEntry> entries = new ArrayList<>();

  public void setOnInsertSnippetListener(OnInsertSnippetListener l) {
    this.insertListener = l;
  }

  @Override
  protected void onContentReady(@NonNull ViewGroup contentContainer) {
    binding = DialogSheetSnippetsBinding.inflate(getLayoutInflater(), contentContainer, false);
    contentContainer.addView(binding.getRoot());

    adapter = new SnippetAdapter();
    adapter.setOnSnippetActionListener(
        new SnippetAdapter.OnSnippetActionListener() {
          @Override
          public void onInsertSnippet(SnippetEntry entry) {
            if (insertListener != null) {
              insertListener.onInsertSnippet(entry);
            }
            dismiss();
          }

          @Override
          public void onMoreClick(SnippetEntry entry, View anchor) {
            showEntryActions(entry);
          }
        });
    binding.rvSnippets.setLayoutManager(new LinearLayoutManager(requireContext()));
    binding.rvSnippets.setAdapter(adapter);

    binding.tvSnippetsCount.setTextColor(M3Theme.onSurface());
    binding.tvSnippetsEmpty.setTextColor(M3Theme.onSurfaceVariant());
    binding.viewSnippetsDivider.setBackgroundColor(M3Theme.outlineVariant());
    binding.tvSnippetsEmpty.setText(getString(R.string.snippets_empty));
    binding.fabAddSnippet.setText(getString(R.string.snippets_new));

    binding.fabAddSnippet.setOnClickListener(v -> showEditDialog(null));

    M3Theme.applyTopLevel(binding.getRoot());

    reload();
  }

  private void reload() {
    entries = UserSnippetStore.load(requireContext());
    adapter.submit(entries);
    boolean empty = entries.isEmpty();
    binding.rvSnippets.setVisibility(empty ? View.GONE : View.VISIBLE);
    binding.layoutSnippetsEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
  }

  private void showEntryActions(SnippetEntry entry) {
    String[] options = {getString(R.string.snippets_edit_title), getString(R.string.delete)};
    new DialogCompat(requireContext())
        .setItems(
            options,
            (dialog, which) -> {
              if (which == 0) {
                showEditDialog(entry);
              } else if (which == 1) {
                confirmDelete(entry);
              }
            })
        .show();
  }

  private void confirmDelete(SnippetEntry entry) {
    new DialogCompat(requireContext())
        .setTitle(getString(R.string.snippets_delete_title))
        .setMessage(getString(R.string.snippets_delete_message))
        .setPositiveButton(
            getString(R.string.delete),
            (dialog, which) -> {
              entries.remove(entry);
              UserSnippetStore.save(requireContext(), entries);
              reload();
            })
        .setNegativeButton(getString(R.string.cancel), null)
        .show();
  }

  private void showEditDialog(SnippetEntry existing) {
    DialogSnippetEditBinding form =
        DialogSnippetEditBinding.inflate(LayoutInflater.from(requireContext()));
    M3Theme.textInputLayout(form.tlSnippetKey);
    M3Theme.textInputLayout(form.tlSnippetPrefix);
    M3Theme.textInputLayout(form.tlSnippetDesc);
    M3Theme.textInputLayout(form.tlSnippetScope);
    M3Theme.textInputLayout(form.tlSnippetBody);

    boolean isEdit = existing != null;
    if (isEdit) {
      form.etSnippetKey.setText(existing.key);
      form.etSnippetPrefix.setText(existing.prefix);
      form.etSnippetDesc.setText(existing.description);
      form.etSnippetScope.setText(existing.scope);
      form.etSnippetBody.setText(existing.body);
    }

    new DialogCompat(requireContext())
        .setTitle(
            isEdit ? getString(R.string.snippets_edit_title) : getString(R.string.snippets_add_title))
        .setView(form.getRoot())
        .setPositiveButton(
            getString(R.string.snippets_save),
            (dialog, which) -> {
              String key =
                  form.etSnippetKey.getText() == null
                      ? ""
                      : form.etSnippetKey.getText().toString().trim();
              String body =
                  form.etSnippetBody.getText() == null
                      ? ""
                      : form.etSnippetBody.getText().toString().trim();
              if (key.isEmpty()) {
                key = "snippet_" + (entries.size() + 1);
              }
              if (body.isEmpty()) {
                return;
              }
              String prefix =
                  form.etSnippetPrefix.getText() == null
                      ? ""
                      : form.etSnippetPrefix.getText().toString().trim();
              String description =
                  form.etSnippetDesc.getText() == null
                      ? ""
                      : form.etSnippetDesc.getText().toString().trim();
              String scope =
                  form.etSnippetScope.getText() == null
                      ? ""
                      : form.etSnippetScope.getText().toString().trim();
              if (isEdit) {
                existing.key = key;
                existing.prefix = prefix;
                existing.description = description;
                existing.scope = scope;
                existing.body = body;
              } else {
                entries.add(new SnippetEntry(key, prefix, description, body, scope));
              }
              UserSnippetStore.save(requireContext(), entries);
              reload();
            })
        .setNegativeButton(getString(R.string.cancel), null)
        .show();
  }
}