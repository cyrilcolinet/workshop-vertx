package eu.epitech.dashboard.api.controllers;

import eu.epitech.dashboard.api.controllers.abstracts.AbstractController;
import eu.epitech.dashboard.api.core.services.SpotifyService;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class SpotifyController extends AbstractController {

    private SpotifyService service;

    /**
     * Constructor
     */
    public SpotifyController() {
        super("spotify");

        // Cast abstract service to custom
        this.service = (SpotifyService) this.getBelongedService();
    }

    public Router configure(final Vertx vertx) {
        final Router subRouter = Router.router(vertx);

        // Body handler
        subRouter.route("/*").handler(BodyHandler.create());

        // Adding routes
        subRouter.get("/getPlaylists/:spotify_id").handler(this::getUserPlaylistAction);
        subRouter.get("/searchForPlaylist/:search").handler(this::getPlaylistFromSearchAction);
        return subRouter;
    }

    private void getUserPlaylistAction(RoutingContext context) {
        String SpotifyID = context.request().getParam("spotify_id");
        String uuid = context.user().principal().getString("uuid");

        this.service.getPlaylistList(SpotifyID, uuid,  data -> {
            JsonObject response = new JsonObject();
            response.put("playlists", data);
            context.response()
                    .setStatusCode(200)
                    .putHeader("content-type", "application/json")
                    .end(Json.encodePrettily(response));
        });
    }

    private void getPlaylistFromSearchAction(RoutingContext context) {
        String search = context.request().getParam("search");
        String uuid = context.user().principal().getString("uuid");
        this.service.getPlaylistFromSearch(search, uuid,  data -> {
            JsonObject response = new JsonObject();
            response.put("playlists", data);
            context.response()
                    .setStatusCode(200)
                    .putHeader("content-type", "application/json")
                    .end(Json.encodePrettily(response));
        });
    }
}
