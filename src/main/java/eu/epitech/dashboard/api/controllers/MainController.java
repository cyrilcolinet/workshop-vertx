package eu.epitech.dashboard.api.controllers;

import eu.epitech.dashboard.api.APIServer;
import eu.epitech.dashboard.api.core.services.abstracts.AbstractService;
import eu.epitech.dashboard.api.controllers.abstracts.AbstractController;
import eu.epitech.dashboard.api.widgets.AbstractWidget;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Map;

/**
 * DefaultRoute controller
 * This controller only contains /about.json route
 *
 * You need to create other controllers to manage other routes
 * @see AbstractController
 */
public class MainController extends AbstractController {

    /**
     * DefaultRoute constructor
     */
    public MainController() {
        super(null);
    }

    /**
     * Configure routes locally
     */
    @Override
    public Router configure(final Vertx vertx) {
        final Router subRouter = Router.router(vertx);

        // Add about UNIQUE route
        subRouter.route("/*").handler(BodyHandler.create());
        subRouter.get("/about.json").handler(this::aboutAction);

        return subRouter;
    }

    /**
     * About action
     * Answer to /
     * @param context Context
     */
    private void aboutAction(RoutingContext context) {
        // Configure about response json
        JsonObject about = new JsonObject();
        about.put("client", new JsonObject().put("host", context.request().remoteAddress().host()));

        // Configure service
        JsonObject server = new JsonObject();
        server.put("current_time", System.currentTimeMillis());

        // Get all services and all to server
        JsonArray services = new JsonArray();
        for (Map.Entry<String, AbstractService> entry : APIServer.get().getServices().all().entrySet()) {
            JsonObject serviceObj = new JsonObject();
            serviceObj.put("name", entry.getValue().getName());

            // Check if this service needs to be authenticated
            if (APIServer.get().getOAuth2Manager().getApps().containsKey(entry.getValue().getName().toLowerCase()))
                serviceObj.put("authentication_required", entry.getValue().getName().toLowerCase());

            // List all widgets and add it
            JsonArray widgets = new JsonArray();
            for (AbstractWidget widget : entry.getValue().getWidgets()) {
                JsonObject widgetObj = new JsonObject();
                widgetObj.put("name", widget.getName());
                widgetObj.put("description", widget.getDescription());

                // Add parameters
                JsonArray params = new JsonArray();
                for (AbstractWidget.Parameter param : widget.getParams()) {
                    JsonObject paramObj = new JsonObject();
                    paramObj.put("name", param.getName());
                    paramObj.put("description", param.getDescription());
                    paramObj.put("type", param.getType());

                    // Add to array
                    params.add(paramObj);
                }

                // Add widget to array
                widgetObj.put("params", params);
                widgets.add(widgetObj);
            }

            // Add widgets
            serviceObj.put("widgets", widgets);
            services.add(serviceObj);
        }

        // Linking objects and response
        server.put("services", services);
        about.put("server", server);
        context.response()
                .setStatusCode(200)
                .putHeader("content-type", "application/json")
                .end(Json.encodePrettily(about));
    }
}
