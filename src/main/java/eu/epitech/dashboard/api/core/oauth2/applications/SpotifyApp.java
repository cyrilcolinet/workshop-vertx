package eu.epitech.dashboard.api.core.oauth2.applications;

import eu.epitech.dashboard.api.APIServer;
import eu.epitech.dashboard.api.core.oauth2.AbstractApp;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2ClientOptions;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.web.RoutingContext;

import java.util.UUID;

public class SpotifyApp extends AbstractApp {

    private static final String clientId = "64fe66b6d7304560ad9aad0e465c7204";
    private static final String clientSecret = "8d9b49c6d7c4411b95c34a30f894e704";

    /**
     * Constructor class
     * Create and stock auth
     */
    public SpotifyApp() {
        super(OAuth2Auth.create(APIServer.get().getVertx(), new OAuth2ClientOptions(new HttpClientOptions())
                .setFlow(OAuth2FlowType.AUTH_CODE)
                .setClientID(null) // here
                .setClientSecret(null) // here
                .setSite("https://accounts.spotify.com")
                .setTokenPath("/api/token")
                .setAuthorizationPath("/authorize")
                .setUserInfoPath("https://api.spotify.com/v1/me")));
    }

    /**
     * Get authorize url to perform authorization
     * @return String
     */
    @Override
    public String getAuthorizeUrl(UUID uuid) {
        JsonObject params = new JsonObject();
        params.put("redirect_uri", this.getRedirectUri());
        params.put("state", this.getUUIDParam(uuid));

        return this.getAuthenticator().authorizeURL(params);
    }

    /**
     * Get redirect URI
     * @return String
     */
    @Override
    public String getRedirectUri() {
        return "http://127.0.0.1:8080/auth/spotifyCallback";
    }

    /**
     * Handling callback
     * @param context RoutingContext
     */
    @Override
    public void handleCallback(RoutingContext context) {
        // fill
    }

}
