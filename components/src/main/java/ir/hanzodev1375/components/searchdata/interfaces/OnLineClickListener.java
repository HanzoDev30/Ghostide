package ir.hanzodev1375.components.searchdata.interfaces;

import ir.hanzodev1375.components.searchdata.model.FileSearchResult;

public interface OnLineClickListener {
  void onLineClick(String filePath, int lineNumber);

  void onFileClick(FileSearchResult result);
}
