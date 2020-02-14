package eu.epitech.dashboard.api.controllers;

import eu.epitech.dashboard.api.APIServer;
import eu.epitech.dashboard.api.controllers.abstracts.AbstractController;
import eu.epitech.dashboard.api.controllers.objects.ErrorResponse;
import eu.epitech.dashboard.api.core.database.MongoQueries;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * UserController manage users
 * @see AbstractController
 */
public class UserController extends AbstractController {

    /**
     * Constructor
     */
    public UserController() {
        super(null);
    }

    /**
     * Configure routes for this controller
     * @param vertx Vertx object
     */
    @Override
    public Router configure(Vertx vertx) {
        final Router subRouter = Router.router(vertx);

        // Adding routes
        subRouter.get("/info").handler(this::infoAction);
        subRouter.get("/authorizeUrls").handler(this::listOAuthAuthorizeURLs);
        subRouter.route().handler(BodyHandler.create());
        subRouter.post("/addWidget").handler(this::configureNewWidget);
        subRouter.route().handler(BodyHandler.create());
        subRouter.post("/editWidget").handler(this::editWidgetByName);
        subRouter.get("/widgetInfo/:service_name").handler(this::getWidgetInfos);
        subRouter.get("/deleteWidget/:service_name").handler(this::deleteWidget);

        return subRouter;
    }

    /**
     * Get information about the user
     * @param context RoutingContext
     */
    private void infoAction(RoutingContext context) {
        String uuid = context.user().principal().getString("uuid");

        JsonObject response = MongoQueries.getAccountByUUID(UUID.fromString(uuid));

        // Get widgets list and add it ton object
        response.put("widgets", MongoQueries.getUserWidgets(UUID.fromString(uuid)));
        context.response()
                .setStatusCode(200)
                .putHeader("content-type", "application/json")
                .end(Json.encodePrettily(JsonObject.mapFrom(response)));
    }

    /**
     * List all authorize urls with uuid in GET parameter
     * @param context RoutingContext
     */
    private void listOAuthAuthorizeURLs(RoutingContext context) {
        JsonObject resp = new JsonObject();
        JsonObject urls = new JsonObject();

        // Get user uuid
        UUID uuid = UUID.fromString(context.user().principal().getString("uuid"));

        // Add all to list, and send it to response
        APIServer.get().getOAuth2Manager().getApps().forEach((key, value) ->
                urls.put(key, value.getAuthorizeUrl(uuid)));
        resp.put("list", urls);

        // Response with a list of oauth authorization URLs
        context.response()
                .setStatusCode(200)
                .putHeader("content-type", "application/json")
                .end(Json.encodePrettily(JsonObject.mapFrom(resp)));
    }

    /**
     * Configure new widget for user
     * @param context RoutingContext
     */
    private void configureNewWidget(RoutingContext context) {
        JsonObject resp = new JsonObject();
        JsonObject params = context.getBodyAsJson();
        String uuidString = context.user().principal().getString("uuid");

        // Check if params exists and are present
        if (!params.containsKey("service")) {
            ErrorResponse err = new ErrorResponse(400, "Service type is missing");

            context.response()
                    .setStatusCode(400)
                    .putHeader("content-type", "application/json")
                    .end(Json.encodePrettily(JsonObject.mapFrom(err)));
            return;
        }

        // Add params in a map
        Map<String, Object> paramsMap = new HashMap<>();
        params.getMap().forEach((key, value) -> {
            if (key.equalsIgnoreCase("service"))
                return;
            paramsMap.put(key, value);
        });

        // Perform request
        MongoQueries.addNewWidgetForUser(UUID.fromString(uuidString), params.getString("service"), paramsMap);

        resp.put("message", "Successfully added");
        context.response()
                .setStatusCode(200)
                .putHeader("content-type", "application/json")
                .end(Json.encodePrettily(JsonObject.mapFrom(resp)));
    }

    /**
     * Get widget information from database
     * @param context RoutingContext
     */
    private void getWidgetInfos(RoutingContext context) {
        String service = context.request().getParam("service_name");
        String uuidString = context.user().principal().getString("uuid");

        // Get result
        JsonObject widgetInfos = MongoQueries.getWidgetInfoByUUID(UUID.fromString(uuidString), UUID.fromString(service));
        if (widgetInfos == null)
            widgetInfos = new JsonObject().put("error", "Not found");

        // Return result
        context.response()
                .setStatusCode(200)
                .putHeader("content-type", "application/json")
                .end(Json.encodePrettily(JsonObject.mapFrom(widgetInfos)));
    }

    /**
     * Edit widget by their name
     * @param context RoutingContext
     */
    private void editWidgetByName(RoutingContext context) {
        JsonObject resp = new JsonObject();
        JsonObject params = context.getBodyAsJson();
        String uuidString = context.user().principal().getString("uuid");

        // Check if params exists and are present
        if (!params.containsKey("service")) {
            ErrorResponse err = new ErrorResponse(400, "Service type is missing");

            context.response()
                    .setStatusCode(400)
                    .putHeader("content-type", "application/json")
                    .end(Json.encodePrettily(JsonObject.mapFrom(err)));
            return;
        }

        // Add params in a map
        Map<String, Object> paramsMap = new HashMap<>();
        params.getMap().forEach((key, value) -> {
            if (key.equalsIgnoreCase("service"))
                return;
            paramsMap.put(key, value);
        });

        // Perform request
        MongoQueries.editWidgetByUUID(UUID.fromString(uuidString), UUID.fromString(params.getString("service")), paramsMap);

        resp.put("message", "Successfully edited");
        context.response()
                .setStatusCode(200)
                .putHeader("content-type", "application/json")
                .end(Json.encodePrettily(JsonObject.mapFrom(resp)));
    }

    /**
     * Delete widget by their service name
     * @param context RoutingContext
     */
    private void deleteWidget(RoutingContext context) {
        JsonObject resp = new JsonObject();
        String uuidString = context.user().principal().getString("uuid");
        String serviceName = context.request().getParam("service_name");

        // Delete widget
        MongoQueries.deleteWidgetByName(UUID.fromString(uuidString), UUID.fromString(serviceName));
        resp.put("message", "Successfully deleted");
        context.response()
                .setStatusCode(200)
                .putHeader("content-type", "application/json")
                .end(Json.encodePrettily(JsonObject.mapFrom(resp)));
    }

}
