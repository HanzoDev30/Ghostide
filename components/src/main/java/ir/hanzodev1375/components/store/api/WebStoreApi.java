package ir.hanzodev1375.components.store.api;

import android.os.Handler;
import android.os.Looper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ir.hanzodev1375.components.store.model.WebStore;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebStoreApi {

  private static final String BASE_URL = "https://github.com/HanzoDev1375/ghostideswebstore/raw/refs/heads/main/config.json"; 
  private static OkHttpClient client = new OkHttpClient();
  private static Gson gson = new Gson();

  public interface Callbacks {
    void onSuccess(List<WebStore> stores);

    void onError(String message);
  }

  public static void fetchWebStores(Callbacks callback) {
    Request request = new Request.Builder().url(BASE_URL).get().build();

    client
        .newCall(request)
        .enqueue(
            new Callback() {
              @Override
              public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper())
                    .post(() -> callback.onError("خطای شبکه: " + e.getMessage()));
              }

              @Override
              public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                  new Handler(Looper.getMainLooper())
                      .post(() -> callback.onError("کد خطا: " + response.code()));
                  return;
                }
                try {
                  String json = response.body().string();
                  Type listType = new TypeToken<List<WebStore>>() {}.getType();
                  List<WebStore> stores = gson.fromJson(json, listType);
                  new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(stores));
                } catch (Exception e) {
                  new Handler(Looper.getMainLooper())
                      .post(() -> callback.onError("خطای ناشناخته: " + e.getMessage()));
                }
              }
            });
  }
}
