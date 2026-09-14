package ir.hanzodev1375.ghostide.jgit;

import android.content.Context;
import ir.hanzodev1375.ghostide.codeeditors.setting.PreferencesUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.*;


public class GitHubClient {
  
  public interface GitHubLoginCallback {
    void onSuccess(String name, String username, String avatarUrl);

    void onFailure(String errorMessage);
  }

  public interface GitHubRequestCallback {
    void onSuccess(JSONObject response);

    void onFailure(String errorMessage);
  }

  public interface GitHubArrayCallback {
    void onSuccess(JSONArray response);

    void onFailure(String errorMessage);
  }

  private final OkHttpClient client = new OkHttpClient();
  private final PreferencesUtils prefs;

  public GitHubClient(Context context) {
    this.prefs = new PreferencesUtils(context);
  }

  public void login(String token, GitHubLoginCallback callback) {
    Request request =
        new Request.Builder()
            .url("https://api.github.com/user")
            .addHeader("Authorization", "Bearer " + token)
            .addHeader("Accept", "application/vnd.github+json")
            .build();

    client
        .newCall(request)
        .enqueue(
            new Callback() {
              @Override
              public void onFailure(Call call, IOException e) {
                callback.onFailure("Network error: " + e.getMessage());
              }

              @Override
              public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                  try {
                    JSONObject json = new JSONObject(response.body().string());
                    String name = json.optString("name", json.optString("login"));
                    String username = json.optString("login");
                    String avatarUrl = json.optString("avatar_url");
                    prefs.setGitHubToken(token);
                    prefs.setGitHubName(name);
                    prefs.setGitHubUsername(username);
                    prefs.setGitHubAvatarUrl(avatarUrl);
                    callback.onSuccess(name, username, avatarUrl);
                  } catch (Exception e) {
                    callback.onFailure("Parse error");
                  }
                } else if (response.code() == 401) {
                  callback.onFailure("Invalid token");
                } else {
                  callback.onFailure("Error: " + response.code());
                }
              }
            });
  }

  public void logout() {
    prefs.clearGitHubAccount();
  }

  public boolean isLoggedIn() {
    return prefs.isGitHubLoggedIn();
  }

  public String getToken() {
    return prefs.getGitHubToken();
  }

  public String getName() {
    return prefs.getGitHubName();
  }

  public String getUsername() {
    return prefs.getGitHubUsername();
  }

  public String getAvatarUrl() {
    return prefs.getGitHubAvatarUrl();
  }

  public void get(String url, GitHubRequestCallback callback) {
    enqueue(
        url,
        new Callback() {
          @Override
          public void onFailure(Call call, IOException e) {
            callback.onFailure("Network error: " + e.getMessage());
          }

          @Override
          public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
              try {
                callback.onSuccess(new JSONObject(response.body().string()));
              } catch (Exception e) {
                callback.onFailure("Parse error");
              }
            } else {
              callback.onFailure("Error: " + response.code());
            }
          }
        });
  }

  public void getArray(String url, GitHubArrayCallback callback) {
    enqueue(
        url,
        new Callback() {
          @Override
          public void onFailure(Call call, IOException e) {
            callback.onFailure("Network error: " + e.getMessage());
          }

          @Override
          public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
              try {
                callback.onSuccess(new JSONArray(response.body().string()));
              } catch (Exception e) {
                callback.onFailure("Parse error");
              }
            } else {
              callback.onFailure("Error: " + response.code());
            }
          }
        });
  }

  private void enqueue(String url, Callback callback) {
    client
        .newCall(
            new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + prefs.getGitHubToken())
                .addHeader("Accept", "application/vnd.github+json")
                .build())
        .enqueue(callback);
  }

  public void getReceivedEvents(String username, GitHubArrayCallback callback) {
    enqueue(
        "https://api.github.com/users/" + username + "/received_events?per_page=30",
        new Callback() {
          @Override
          public void onFailure(Call call, IOException e) {
            callback.onFailure("Network error: " + e.getMessage());
          }

          @Override
          public void onResponse(Call call, Response response) throws IOException {
            if (response.isSuccessful()) {
              try {
                callback.onSuccess(new JSONArray(response.body().string()));
              } catch (Exception e) {
                callback.onFailure("Parse error");
              }
            } else {
              callback.onFailure("Error: " + response.code());
            }
          }
        });
  }
}
