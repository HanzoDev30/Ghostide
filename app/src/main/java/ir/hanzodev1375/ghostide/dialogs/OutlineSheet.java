package ir.hanzodev1375.ghostide.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Supplier;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.blankj.utilcode.util.ThreadUtils;
import ir.hanzodev1375.components.sheet.BaseBlurBottomSheet;
import ir.hanzodev1375.ghostide.adapters.OutlineAdapter;
import ir.hanzodev1375.ghostide.codeeditors.IdeEditor;
import ir.hanzodev1375.ghostide.codeeditors.langs.lsp.LspRouter;
import ir.hanzodev1375.ghostide.codeeditors.langs.lsp.model.OutlineSymbol;
import ir.hanzodev1375.ghostide.databinding.DialogSheetOutlineBinding;
import ir.theme.M3Theme;
import java.io.File;
import java.util.List;

public final class OutlineSheet extends BaseBlurBottomSheet {

  public static final String TAG = "OutlineSheet";

  public interface OnSymbolClickListener {
    void onSymbolClick(int line, int column);
  }

  private String filePath;
  private String displayName;
  private Supplier<IdeEditor> editorSupplier;
  private OnSymbolClickListener symbolListener;
  private DialogSheetOutlineBinding binding;
  private OutlineAdapter adapter;

  public static OutlineSheet newInstance(String filePath) {
    OutlineSheet sheet = new OutlineSheet();
    Bundle args = new Bundle();
    args.putString("file_path", filePath);
    sheet.setArguments(args);
    return sheet;
  }

  public void setEditorSupplier(Supplier<IdeEditor> supplier) {
    this.editorSupplier = supplier;
  }

  public void setOnSymbolClickListener(OnSymbolClickListener l) {
    this.symbolListener = l;
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
    binding = DialogSheetOutlineBinding.inflate(getLayoutInflater(), contentContainer, false);
    contentContainer.addView(binding.getRoot());

    filePath = getArguments() != null ? getArguments().getString("file_path") : null;
    displayName = filePath == null ? "Outline" : new File(filePath).getName();

    adapter = new OutlineAdapter();
    adapter.setOnSymbolClickListener(
        symbol -> {
          if (symbolListener != null) {
            symbolListener.onSymbolClick(symbol.line, symbol.column);
          }
          dismiss();
        });
    binding.rvOutline.setLayoutManager(new LinearLayoutManager(requireContext()));
    binding.rvOutline.setAdapter(adapter);

    M3Theme.applyTopLevel(binding.getRoot());
    binding.tvOutlineTitle.setText(displayName);
    binding.tvOutlineTitle.setTextColor(M3Theme.onSurface());
    binding.tvOutlineEmpty.setTextColor(M3Theme.onSurfaceVariant());
    binding.viewOutlineDivider.setBackgroundColor(M3Theme.outlineVariant());

    loadOutline();
  }

  private void loadOutline() {
    final String currentPath = filePath;
    if (currentPath == null) {
      showEmpty();
      return;
    }
    binding.outlineProgress.setVisibility(View.VISIBLE);
    new Thread(
            () -> {
              IdeEditor editor = editorSupplier == null ? null : editorSupplier.get();
              var lspEditor = editor == null ? null : editor.getLspEditor();
              if (lspEditor == null) {
                ThreadUtils.runOnUiThread(this::showEmpty);
                return;
              }
              List<OutlineSymbol> symbols =
                  LspRouter.fetchOutline(lspEditor, currentPath);
              ThreadUtils.runOnUiThread(
                  () -> {
                    if (binding == null || !isAdded()) return;
                    binding.outlineProgress.setVisibility(View.GONE);
                    if (symbols == null || symbols.isEmpty()) {
                      showEmpty();
                      return;
                    }
                    adapter.submit(symbols);
                  });
            })
        .start();
  }

  private void showEmpty() {
    if (binding == null) return;
    binding.outlineProgress.setVisibility(View.GONE);
    binding.rvOutline.setVisibility(View.GONE);
    binding.layoutOutlineEmpty.setVisibility(View.VISIBLE);
  }
}