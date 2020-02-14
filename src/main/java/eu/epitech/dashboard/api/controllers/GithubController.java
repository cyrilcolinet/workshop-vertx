package eu.epitech.dashboard.api.controllers;

import eu.epitech.dashboard.api.core.services.GithubService;
import eu.epitech.dashboard.api.controllers.abstracts.AbstractController;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Github controller
 * This controller contains all routes about the github service
 * @see AbstractController
 */
public class GithubController extends AbstractController {

    private GithubService service;

    /**
     * Constructor
     */
    public GithubController() {
        super("github");

        // Cast abstract service to custom
        this.service = (GithubService) this.getBelongedService();
    }

    /**
     * Configure routes locally for constructor
     */
    @Override
    public Router configure(final Vertx vertx) {
        final Router subRouter = Router.router(vertx);

        // Body handler
        subRouter.route("/*").handler(BodyHandler.create());

        // Adding routes
        subRouter.get("/repositoriesList/:username/:max").handler(this::repositoriesList);
        subRouter.get("/countFollowers/:username").handler(this::getCountFollowers);
        subRouter.get("/listFollowers/:username/:max").handler(this::getFollowersList);
        return subRouter;
    }

    /**
     * Get repositories list
     * @param context RoutingContext
     */
    private void repositoriesList(RoutingContext context) {
        String uuid = context.user().principal().getString("uuid");
        int max = Integer.parseInt(context.request().getParam("max"));
        String username = context.request().getParam("username");

        // Get repo list
        this.service.getRepositoriesWithMaxValue(uuid, username, max, data -> {
            JsonObject response = new JsonObject();
            response.put("list", data);

            context.response()
                    .setStatusCode(200)
                    .putHeader("content-type", "application/json")
                    .end(Json.encodePrettily(response));
        });
    }

    /**
     * Get followers count
     * @param context RoutingContext
     */
    private void getCountFollowers(RoutingContext context) {
        String uuid = context.user().principal().getString("uuid");
        String username = context.request().getParam("username");

        // Get repo list
        this.service.getFollowersList(uuid, username, Integer.MAX_VALUE, data -> {
            JsonObject response = new JsonObject();
            response.put("count", data.size());

            context.response()
                    .setStatusCode(200)
                    .putHeader("content-type", "application/json")
                    .end(Json.encodePrettily(response));
        });
    }

    /**
     * Get followers count
     * @param context RoutingContext
     */
    private void getFollowersList(RoutingContext context) {
        String uuid = context.user().principal().getString("uuid");
        int max = Integer.parseInt(context.request().getParam("max"));
        String username = context.request().getParam("username");

        // Get repo list
        this.service.getFollowersList(uuid, username, max, data -> {
            JsonObject response = new JsonObject();
            response.put("list", data);

            context.response()
                    .setStatusCode(200)
                    .putHeader("content-type", "application/json")
                    .end(Json.encodePrettily(response));
        });
    }
}
