package ir.hanzodev1375.components.searchdata.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import ir.hanzodev1375.components.R;
import ir.hanzodev1375.components.databinding.BottomSheetExcludeSearchBinding;
import ir.hanzodev1375.components.sheet.BaseBlurBottomSheet;
import ir.hanzodev1375.components.utils.ComponentsPrefs;
import ir.theme.M3Theme;

public class ExcludedSearchSheet extends BaseBlurBottomSheet {

  public static final String TAG = "ExcludedSearchSheet";

  @Override
  public void onContentReady(ViewGroup contentContainer) {
    BottomSheetExcludeSearchBinding b =
        BottomSheetExcludeSearchBinding.inflate(getLayoutInflater(), contentContainer, false);
    contentContainer.addView(b.getRoot());
    M3Theme.applyTopLevel(b.getRoot());

    ComponentsPrefs prefs = new ComponentsPrefs(requireContext());
    b.etPatterns.setText(prefs.getExcludedFilesText());

    b.btnSave.setOnClickListener(
        v -> {
          prefs.setExcludedFilesText(b.etPatterns.getText().toString());
          Snackbar.make(b.getRoot(), R.string.search_exclude_saved, Snackbar.LENGTH_SHORT).show();
          dismiss();
        });

    b.btnReset.setOnClickListener(v -> b.etPatterns.setText(ComponentsPrefs.defaultExcludedFilesText()));
  }
}