package ir.hanzodev1375.ghostide.adapters;

import android.animation.ObjectAnimator;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.codeeditors.IdeEditor;
import ir.hanzodev1375.ghostide.GhostIdeAppLoader;
import ir.hanzodev1375.ghostide.utils.FileUtil;
import ir.theme.ThemeManager;
import ir.theme.ThemeUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class SysmbolbarAdapter extends RecyclerView.Adapter<SysmbolbarAdapter.ViewHolder> {

  protected HashMap<String, Object> imap;
  private ArrayList<HashMap<String, Object>> data;
  private OnTabView tabview;
  private IdeEditor editor;

  public SysmbolbarAdapter(
      ArrayList<HashMap<String, Object>> data, OnTabView tabview, IdeEditor editor) {
    this.data = data;
    this.tabview = tabview;
    this.editor = editor;
    imap = new HashMap<>();
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.sysmbol_item, parent, false);
    return new ViewHolder(v);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    View view = holder.itemView;
    holder.bind(data.get(position));
  }

  @Override
  public int getItemCount() {
    return data.size();
  }

  public void clickAnimation(View view) {
    ScaleAnimation fadein =
        new ScaleAnimation(
            0.9f, 1f, 0.9f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.7f);
    fadein.setDuration(300);
    fadein.setFillAfter(true);
    view.startAnimation(fadein);
  }

  public interface OnTabView {
    public void TAB(String tab);

    public void POST(String post);
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    private TextView sysbarInsertId;

    public ViewHolder(View v) {
      super(v);
      clickAnimation(itemView);
      sysbarInsertId = v.findViewById(R.id.sysbarInsertId);
    }

    void bind(HashMap<String,Object> model) {
      var manager = new ThemeManager(sysbarInsertId.getContext());
      var themeUtils = new ThemeUtils(manager);
      themeUtils.applySymbolBarText(sysbarInsertId);
      if (model.containsKey("Tab")) {
        sysbarInsertId.setText(model.get("Tab").toString());
      } else {
        sysbarInsertId.setText(model.get("post").toString());
      }
      itemView.setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              if (model.containsKey("Tab")) {
                sysbarInsertId.setText(model.get("Tab").toString());
                tabview.TAB(sysbarInsertId.getText().toString());

              } else {
                sysbarInsertId.setText(model.get("post").toString());
                tabview.POST(sysbarInsertId.getText().toString());
                Log.e("POST", sysbarInsertId.getText().toString());
              }
            }
          });

      sysbarInsertId.setOnTouchListener(
          new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
              switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                  {
                    ObjectAnimator scaleX = new ObjectAnimator();
                    scaleX.setTarget(sysbarInsertId);
                    scaleX.setPropertyName("scaleX");
                    scaleX.setFloatValues(0.9f);
                    scaleX.setDuration(5);
                    scaleX.start();

                    ObjectAnimator scaleY = new ObjectAnimator();
                    scaleY.setTarget(sysbarInsertId);
                    scaleY.setPropertyName("scaleY");
                    scaleY.setFloatValues(0.9f);
                    scaleY.setDuration(5);
                    scaleY.start();
                    break;
                  }
                case MotionEvent.ACTION_UP:
                  {
                    ObjectAnimator scaleX = new ObjectAnimator();
                    scaleX.setTarget(sysbarInsertId);
                    scaleX.setPropertyName("scaleX");
                    scaleX.setFloatValues((float) 1);
                    scaleX.setDuration(5);
                    scaleX.start();

                    ObjectAnimator scaleY = new ObjectAnimator();
                    scaleY.setTarget(sysbarInsertId);
                    scaleY.setPropertyName("scaleY");
                    scaleY.setFloatValues((float) 1);
                    scaleY.setDuration(5);
                    scaleY.start();

                    break;
                  }
              }
              return false;
            }
          });
    }
  }
}
