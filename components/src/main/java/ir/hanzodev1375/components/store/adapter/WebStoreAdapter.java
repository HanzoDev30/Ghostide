package ir.hanzodev1375.components.store.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import ir.hanzodev1375.components.R;
import ir.hanzodev1375.components.store.model.WebStore;
import java.util.List;

public class WebStoreAdapter extends RecyclerView.Adapter<WebStoreAdapter.VH> {

  private List<WebStore> model;
  private OnClickItemListener click;

  public WebStoreAdapter(List<WebStore> model, OnClickItemListener click) {
    this.model = model;
    this.click = click;
  }

  public interface OnClickItemListener {
    void click(View v, int pos, WebStore model);
  }

  public void updateData(List<WebStore> newData) {
    this.model.clear();
    this.model.addAll(newData);
    notifyDataSetChanged();
  }

  static class VH extends RecyclerView.ViewHolder {
    private ImageView screenshot1, screenshot2, screenshot3, projectImage;
    private TextView projectTitle;

    public VH(View v) {
      super(v);
      screenshot1 = v.findViewById(R.id.screenshot1);
      screenshot2 = v.findViewById(R.id.screenshot2);
      screenshot3 = v.findViewById(R.id.screenshot3);
      projectImage = v.findViewById(R.id.projectImage);
      projectTitle = v.findViewById(R.id.projectTitle);
    }

    void bind(WebStore model) {
      pathGlide(screenshot1, model.getScreen1());
      pathGlide(screenshot2, model.getScreen2());
      pathGlide(screenshot3, model.getScreen3());
      pathGlide(projectImage, model.getIcon());
      projectTitle.setText(model.getName());
    }

    void pathGlide(ImageView v, String path) {
      Glide.with(v).load(path).into(v);
    }
  }

  @Override
  public VH onCreateViewHolder(ViewGroup arg0, int arg1) {
    return new VH(
        LayoutInflater.from(arg0.getContext()).inflate(R.layout.layout_webstore, arg0, false));
  }

  @Override
  public void onBindViewHolder(VH holder, int pos) {
    WebStore item = model.get(pos);
    holder.bind(item);
    holder.itemView.setOnClickListener(
        v -> {
          if (click != null) click.click(v, pos, item);
        });
  }

  @Override
  public int getItemCount() {
    return model.size();
  }
}
