package eu.epitech.dashboard.api.core.services.abstracts;

import eu.epitech.dashboard.api.APIServer;
import eu.epitech.dashboard.api.core.database.MongoQueries;
import eu.epitech.dashboard.api.widgets.AbstractWidget;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Abstract class of service
 * All core services must be extended with this class
 */
public abstract class AbstractService {

    private String name;
    private List<AbstractWidget> widgets = new ArrayList<>();

    /**
     * Constructor of AbstractService
     * @param name The name of the service
     */
    public AbstractService(String name) {
        this.name = name;
    }

    /**
     * Get name of the service
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Get all instantiated widgets on this service
     * @return List
     */
    public List<AbstractWidget> getWidgets() {
        return widgets;
    }

    /**
     * Add widget to service
     * @param widget Widget
     */
    protected void addWidget(AbstractWidget widget) {
        this.widgets.add(widget);
    }

    /**
     * Performing OAuth request
     * @param accountId UUID id of account
     * @param host String hostname
     * @param request String request address
     * @param handler Handle request result
     */
    protected void performOAuthRequest(UUID accountId, String host, String request, Handler<Object> handler) {
        OAuth2Auth oauth = APIServer.get().getOAuth2Manager().getApps().get(this.name).getAuthenticator();
        if (oauth == null) {
            handler.handle(null);
            return;
        }

        // Get access token from database
        String accessToken = MongoQueries.getAccessTokenByAccountType(this.name, accountId);
        if (accessToken == null) {
            handler.handle(null);
            return;
        }

        WebClient client = WebClient.create(APIServer.get().getVertx());
        client.get(80, host, request)
                .putHeader("Authorization", "Bearer " + accessToken)
                .send(ar -> {
                    if (ar.failed()) {
                        ar.cause().printStackTrace();
                        handler.handle(null);
                        return;
                    }

                    HttpResponse<Buffer> response = ar.result();
                    try {
                        handler.handle(response.bodyAsJsonObject());
                    } catch (Exception err) {
                        // Try other
                        try {
                            handler.handle(response.bodyAsJsonArray());
                        } catch (Exception ignored2) {
                            handler.handle(null);
                        }
                    }
                });

    }
}
