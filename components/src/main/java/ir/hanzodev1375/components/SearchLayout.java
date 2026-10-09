package ir.hanzodev1375.components;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.example.liquidglass.GlassMaterial;
import com.google.android.material.transition.platform.MaterialSharedAxis;
import ir.hanzodev1375.components.sheet.customitemsheet.ui.GlassCompat;
import ir.hanzodev1375.components.utils.ComponentsPrefs;
import ir.theme.M3Theme;

@MainThread
public class SearchLayout extends FrameLayout {

  private EditText editText;
  private ImageButton clearButton;
  private ImageView searchIcon;
  private OnSearchListener onSearchListener;
  private OnTextChangedListener onTextChangedListener;
  ComponentsPrefs setting;

  public SearchLayout(@NonNull Context context) {
    super(context);
    init(context);
  }

  public SearchLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  private void init(Context context) {
    setting = new ComponentsPrefs(context);
    LayoutInflater.from(context).inflate(R.layout.search_layout, this, true);

    editText = findViewById(R.id.etSearch);
    clearButton = findViewById(R.id.btnClear);
    searchIcon = findViewById(R.id.ivSearchIcon);
    clearButton.setVisibility(View.INVISIBLE);
    clearButton.setAlpha(0f);

    setVisibility(GONE);
    setupListeners();
    applyGlassBackground();
    M3Theme.apply(this);
  }

  private void applyGlassBackground() {
    Activity activity = resolveActivity(getContext());
    if (activity == null) return;

    GlassCompat glass = new GlassCompat(activity);
    float density = getResources().getDisplayMetrics().density;
    glass.setCornerRadius(26f * density);
    glass.setRefractionHeight(48f);
    glass.setBevelWidth(8f);
    glass.setMaterial(GlassMaterial.REGULAR);
    glass.setDispersionStrength(0.1f);
    glass.setEnableDynamicBackground(false);
    glass.setEnableSensorHighlight(true);
    glass.setEnableAdaptiveTint(true);
    addView(
        glass,
        0,
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    attachBackdropSource(glass, activity);
  }

  private void attachBackdropSource(GlassCompat glass, Activity activity) {
    if (isAttachedToWindow()) {
      View backdrop = findBackdrop(activity);
      if (backdrop != null) {
        glass.setBackdropSource(backdrop);
        return;
      }
    }
    getViewTreeObserver()
        .addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
              @Override
              public void onGlobalLayout() {
                if (!isAttachedToWindow()) return;
                View backdrop = findBackdrop(activity);
                if (backdrop == null) return;
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                glass.setBackdropSource(backdrop);
              }
            });
  }

  /**
   * Resolves the view the glass panel samples.
   *
   * <p>It must be the page this bar lives on, never the Activity's content view. The library
   * re-draws the whole backdrop tree synchronously from {@code onDraw} and only hides glass views
   * that share the glass's parent, so a backdrop that contains the ViewPager2 re-draws every
   * sibling page — and each of those pages' own glass bars captures the window again, recursively.
   * With the store's offscreen page limit that turns into a visible stall, and the panel paints a
   * sample of where the page was a frame ago instead of what is behind it now.
   */
  private View findBackdrop(Activity activity) {
    View page = findPageRoot();
    if (page != null) {
      return page;
    }
    return activity.findViewById(android.R.id.content);
  }

  /**
   * The page this bar is displayed on: the direct child of the ViewPager2's internal RecyclerView
   * that contains this view, or null when the bar is not inside a ViewPager2.
   */
  @Nullable
  private View findPageRoot() {
    ViewGroup parent = getParent() instanceof ViewGroup ? (ViewGroup) getParent() : null;
    while (parent != null) {
      ViewParent grandParent = parent.getParent();
      if (grandParent instanceof RecyclerView && ((RecyclerView) grandParent).getParent() instanceof ViewPager2) {
        return parent;
      }
      if (!(grandParent instanceof ViewGroup)) {
        return null;
      }
      parent = (ViewGroup) grandParent;
    }
    return null;
  }

  private static Activity resolveActivity(Context context) {
    while (context instanceof ContextWrapper) {
      if (context instanceof Activity) {
        return (Activity) context;
      }
      context = ((ContextWrapper) context).getBaseContext();
    }
    return null;
  }

  private void setupListeners() {
    editText.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            String text = s.toString();
            showClearButton(!text.isEmpty());
            if (onTextChangedListener != null) {
              onTextChangedListener.onTextChanged(text);
            }
          }

          @Override
          public void afterTextChanged(Editable s) {}
        });

    clearButton.setOnClickListener(
        v -> {
          clear();
          requestFocusForEditText();
        });

    editText.setOnEditorActionListener(
        (v, actionId, event) -> {
          if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            performSearch();
            return true;
          }
          return false;
        });

    searchIcon.setOnClickListener(v -> performSearch());
  }

  private void showClearButton(boolean show) {
    clearButton.setAlpha(1f);
    clearButton.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
  }

  private void performSearch() {
    String query = editText.getText().toString();
    if (onSearchListener != null && !query.trim().isEmpty()) {
      onSearchListener.onSearch(query);
    }
  }

  // متدهای عمومی
  public void setOnSearchListener(OnSearchListener listener) {
    this.onSearchListener = listener;
  }

  public void setOnTextChangedListener(OnTextChangedListener listener) {
    this.onTextChangedListener = listener;
  }

  public String getQuery() {
    return editText.getText().toString();
  }

  public void setHint(CharSequence hint) {
    editText.setHint(hint);
  }

  public void setQuery(String query) {
    editText.setText(query);
    editText.setSelection(query.length());
  }

  public void clear() {
    editText.getText().clear();
  }

  public void requestFocusForEditText() {
    editText.requestFocus();
  }

  public interface OnSearchListener {
    void onSearch(String query);
  }

  public interface OnTextChangedListener {
    void onTextChanged(String text);
  }

  public void setIconClose(int icon) {
    if (icon == 0) {
      throw new IllegalArgumentException("icon res not found call setIconClose(#int.class)");
    } else {
      clearButton.setImageResource(icon);
    }
  }

  public void setIconSearch(int icon) {
    if (icon == 0) {
      throw new IllegalArgumentException("icon res not found call setIconSearch(#int.class)");
    } else searchIcon.setImageResource(icon);
  }

  public boolean isShow() {
    return getVisibility() == VISIBLE;
  }

  public void show() {
    var material = new MaterialSharedAxis(MaterialSharedAxis.Z, true);
    if (getParent() instanceof ViewGroup) {
      TransitionManager.beginDelayedTransition((ViewGroup) getParent(), material);
    }
    setVisibility(VISIBLE);
  }

  public void hide() {
    var material = new MaterialSharedAxis(MaterialSharedAxis.Z, false);
    if (getParent() instanceof ViewGroup) {
      TransitionManager.beginDelayedTransition((ViewGroup) getParent(), material);
    }
    setVisibility(GONE);
  }

}