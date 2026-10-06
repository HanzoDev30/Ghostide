package ir.hanzodev1375.ghostide.ide.ui.api;

public enum PluginStateMod {
  SIDESHEET,
  DIALOGFRAGMENT,
  DIALOG,
  FRAGMENT,
  BOTTOMSHERTFRAGMENT,
  BOTTOMSHEETDIALOG,
  FLOATINGWINDOWS,
  ACTIVITY,
  POPUP_WINDOW,
  SNACKBAR,
  HEADLESS,
  NONE;

  public boolean needsFragmentManager() {
    switch (this) {
      case DIALOGFRAGMENT:
      case FRAGMENT:
      case BOTTOMSHERTFRAGMENT:
        return true;
      default:
        return false;
    }
  }

  public boolean isFloating() {
    switch (this) {
      case FLOATINGWINDOWS:
      case POPUP_WINDOW:
      case SNACKBAR:
        return true;
      default:
        return false;
    }
  }

  public boolean isModal() {
    switch (this) {
      case DIALOG:
      case DIALOGFRAGMENT:
      case BOTTOMSHEETDIALOG:
      case BOTTOMSHERTFRAGMENT:
      case SIDESHEET:
        return true;
      default:
        return false;
    }
  }

  public boolean hasUi() {
    return this != HEADLESS && this != NONE;
  }

  public boolean requiresActivity() {
    return this != HEADLESS && this != NONE;
  }
}