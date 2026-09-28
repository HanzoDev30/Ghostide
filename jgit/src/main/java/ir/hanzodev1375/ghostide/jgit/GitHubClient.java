package ir.hanzodev1375.ghostide.jgit;

import android.content.Context;
import ir.hanzodev1375.ghostide.codeeditors.setting.PreferencesUtils;
import ir.hanzodev1375.ghostide.jgit.model.UserStats;
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

  public interface UserStatsCallback {
    void onSuccess(UserStats stats);

    void onFailure(String errorMessage);
  }

  /** Upper bound on repo pages walked while summing stars (1000 repos). */
  private static final int MAX_STAR_PAGES = 10;

  private static final int REPOS_PER_PAGE = 100;

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
    enqueue(HttpUrl.parse(url), callback);
  }

  private void enqueue(HttpUrl url, Callback callback) {
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

  /**
   * Loads the counters shown on the profile header: issues opened by the user, and the stars
   * held by their repositories.
   *
   * <p>The two numbers need different endpoints. Search is the only one that answers "issues
   * opened by this user" directly; stars exist per repository only, so they are summed by
   * walking the repository list. Neither request depends on the other, so one failing still
   * reports the other instead of dropping the whole header.
   */
  public void loadUserStats(String username, UserStatsCallback callback) {
    if (username == null || username.isEmpty()) {
      callback.onFailure("Missing username");
      return;
    }
    StatsAccumulator accumulator = new StatsAccumulator(client, getToken(), callback);
    accumulator.loadIssues(username);
    accumulator.loadStars(username, 1, 0);
  }

  private static class StatsAccumulator {

    private final OkHttpClient client;
    private final String token;
    private final UserStatsCallback callback;
    private int issues = -1;
    private int stars = -1;
    private int pending = 2;
    private String error;

    StatsAccumulator(OkHttpClient client, String token, UserStatsCallback callback) {
      this.client = client;
      this.token = token;
      this.callback = callback;
    }

    void loadIssues(String username) {
      HttpUrl url =
          HttpUrl.parse("https://api.github.com/search/issues")
              .newBuilder()
              .addQueryParameter("q", "author:" + username + " type:issue")
              .addQueryParameter("per_page", "1")
              .build();
      request(
          url,
          new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
              issuesFailed("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
              if (!response.isSuccessful()) {
                issuesFailed("Error: " + response.code());
                return;
              }
              try (ResponseBody body = response.body()) {
                int total = new JSONObject(body != null ? body.string() : "{}")
                    .optInt("total_count", 0);
                issuesReady(total);
              } catch (Exception e) {
                issuesFailed("Parse error");
              }
            }
          });
    }

    void loadStars(String username, int page, int accumulated) {
      HttpUrl url =
          HttpUrl.parse("https://api.github.com/users/" + username + "/repos")
              .newBuilder()
              .addQueryParameter("per_page", String.valueOf(REPOS_PER_PAGE))
              .addQueryParameter("page", String.valueOf(page))
              .addQueryParameter("sort", "full_name")
              .build();
      request(
          url,
          new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
              starsFailed("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
              if (!response.isSuccessful()) {
                starsFailed("Error: " + response.code());
                return;
              }
              int total;
              int received;
              try (ResponseBody body = response.body()) {
                JSONArray repos = new JSONArray(body != null ? body.string() : "[]");
                received = repos.length();
                total = accumulated;
                for (int i = 0; i < received; i++) {
                  total += repos.optJSONObject(i).optInt("stargazers_count", 0);
                }
              } catch (Exception e) {
                starsFailed("Parse error");
                return;
              }
              // A short page means the last one was reached.
              if (received < REPOS_PER_PAGE || page >= MAX_STAR_PAGES) {
                starsReady(total);
              } else {
                loadStars(username, page + 1, total);
              }
            }
          });
    }

    private void request(HttpUrl url, Callback callback) {
      Request.Builder builder = new Request.Builder().url(url);
      if (token != null && !token.isEmpty()) {
        builder.addHeader("Authorization", "Bearer " + token);
      }
      builder.addHeader("Accept", "application/vnd.github+json");
      client.newCall(builder.build()).enqueue(callback);
    }

    private synchronized void issuesReady(int value) {
      issues = value;
      settle();
    }

    private synchronized void starsReady(int value) {
      stars = value;
      settle();
    }

    private synchronized void issuesFailed(String message) {
      if (error == null) {
        error = message;
      }
      settle();
    }

    private synchronized void starsFailed(String message) {
      if (error == null) {
        error = message;
      }
      settle();
    }

    private void settle() {
      pending--;
      if (pending > 0) {
        return;
      }
      if (issues < 0 && stars < 0) {
        callback.onFailure(error != null ? error : "Unavailable");
        return;
      }
      callback.onSuccess(new UserStats(Math.max(issues, 0), Math.max(stars, 0)));
    }
  }
}
