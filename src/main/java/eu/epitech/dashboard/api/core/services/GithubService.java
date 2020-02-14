package eu.epitech.dashboard.api.core.services;

import eu.epitech.dashboard.api.core.services.abstracts.AbstractService;
import eu.epitech.dashboard.api.widgets.github.CountUsersFollowers;
import eu.epitech.dashboard.api.widgets.github.ListRepositoriesForUser;
import eu.epitech.dashboard.api.widgets.github.ListUserFollowers;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

/**
 * GithubService service
 * This class is used to perform request github api
 * @see AbstractService
 */
public class GithubService extends AbstractService {

    /**
     * WeatherService Constructor
     * All child widgets must be instantiated here with {@see #addWidget} method
     */
    public GithubService() {
        super("github");

        // Adding widgets
        this.addWidget(new ListRepositoriesForUser());
        this.addWidget(new CountUsersFollowers());
        this.addWidget(new ListUserFollowers());
    }

    /**
     * Get repositories as list
     * @param accountId UUID of account
     * @param username Github username
     * @param max Max count of repositories
     * @param handler Handle response
     */
    public void getRepositoriesWithMaxValue(String accountId, String username, int max, Handler<JsonArray> handler) {
        this.performOAuthRequest(UUID.fromString(accountId), "api.github.com", "/users/" + username + "/repos", res -> {
            JsonArray repos = new JsonArray();
            JsonArray result = new JsonArray();
            int index = 0;

            // Array ?
            if (res instanceof JsonArray)
                result = ((JsonArray) res);

            // Iterate in all repositories
            for (Object obj : result) {
                if (index >= max)
                    break;
                JsonObject object = JsonObject.mapFrom(obj);
                JsonObject repo = new JsonObject();
                repo.put("name", object.getString("name"));
                repo.put("description", object.getString("description"));
                repo.put("url", object.getString("html_url"));
                repo.put("updated_at", object.getString("updated_at"));
                repo.put("watchers_count", object.getInteger("watchers_count"));
                repo.put("stargazers_count", object.getInteger("stargazers_count"));

                // Iterate index and add to response
                index++;
                repos.add(repo);
            }

            // Return result
            handler.handle(repos);
        });
    }

    public void getFollowersList(String accountId, String username, int max, Handler<JsonArray> handler) {
        this.performOAuthRequest(UUID.fromString(accountId), "api.github.com", "/users/" + username + "/followers", res -> {
            JsonArray followers = new JsonArray();
            JsonArray result = new JsonArray();
            int index = 0;

            // Array ?
            if (res instanceof JsonArray)
                result = ((JsonArray) res);

            // Iterate in all repositories
            for (Object obj : result) {
                if (index >= max)
                    break;
                JsonObject object = JsonObject.mapFrom(obj);
                JsonObject repo = new JsonObject();
                repo.put("login", object.getString("login"));
                repo.put("avatar_url", object.getString("avatar_url"));
                repo.put("url", object.getString("html_url"));

                // Iterate index and add to response
                index++;
                followers.add(repo);
            }

            // Return result
            handler.handle(followers);
        });
    }
}