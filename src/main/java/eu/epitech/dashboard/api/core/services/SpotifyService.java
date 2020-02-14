package eu.epitech.dashboard.api.core.services;

import eu.epitech.dashboard.api.APIServer;
import eu.epitech.dashboard.api.core.services.abstracts.AbstractService;
import eu.epitech.dashboard.api.widgets.spotify.GetUserPlaylistWidget;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

import java.util.UUID;

public class SpotifyService extends AbstractService {
    public WebClient cl;
    public float temp;

    /**
     * SpotifyService Constructor
     * All child widgets must be instantiated here with {@see #addWidget} method
     */
    public SpotifyService() {
        super("spotify");
        // Adding widgets
        this.addWidget(new GetUserPlaylistWidget());
    }

    public void getPlaylistList(String spotifyID, String AccountID, Handler<JsonArray> handler) {
        this.performOAuthRequest(UUID.fromString(AccountID), "api.spotify.com", "/v1/users/" + spotifyID + "/playlists", res -> {
            JsonObject result = new JsonObject();
            JsonArray response = new JsonArray();

            // Cast into object if is object
            if (res instanceof JsonObject)
                result = ((JsonObject) res);

            APIServer.logger().info(result);
            JsonArray array = result.getJsonArray("items");
            if (array != null) {
                array.forEach(itemField -> {
                    JsonObject item = ((JsonObject) itemField);
                    JsonObject tmp = new JsonObject()
                            .put("name", item.getString("name"))
                            .put("collaborative", item.getBoolean("collaborative"))
                            .put("uri", item.getString("uri"))
                            .put("picture", item.getJsonArray("images").getJsonObject(0).getString("url"));

                    // Add to response
                    response.add(tmp);
                });
            }

            // Handler callback and set result
            handler.handle(response);
        });
    }

    public void getPlaylistFromSearch(String search, String AccountID, Handler<JsonArray> handler) {
        this.performOAuthRequest(UUID.fromString(AccountID), "api.spotify.com", "/v1/browse/categories/" + search + "/playlists", res -> {
            JsonObject result = new JsonObject();
            JsonArray response = new JsonArray();

            // Cast into object if is object
            if (res instanceof JsonObject)
                result = ((JsonObject) res);

            JsonArray array = result.getJsonObject("playlists").getJsonArray("items");
            array.forEach(itemField -> {
                JsonObject item = ((JsonObject) itemField);
                JsonObject tmp = new JsonObject()
                        .put("name", item.getString("name"))
                        .put("url", item.getJsonObject("external_urls").getString("spotify"))
                        .put("picture", item.getJsonArray("images").getJsonObject(0).getString("url"));

                // Add to response
                response.add(tmp);
            });

            // Handle and set response
            handler.handle(response);
        });
    }
}
