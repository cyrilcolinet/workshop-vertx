package eu.epitech.dashboard.api.core.oauth2.applications;

import eu.epitech.dashboard.api.APIServer;
import eu.epitech.dashboard.api.core.oauth2.OAuth2Manager;
import eu.epitech.dashboard.api.core.oauth2.OAuthTypes;
import eu.epitech.dashboard.api.core.oauth2.AbstractApp;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.AccessToken;
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
                .setClientID(clientId)
                .setClientSecret(clientSecret)
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
        String code = context.request().getParam("code");
        String stateUUID = context.request().getParam("state");
        if (code == null) {
            context.response()
                    .putHeader("Location", System.getenv("APP_URL") + "/auth/login?error=Token invalide")
                    .setStatusCode(302).end();
            return;
        }

        JsonObject tokenConfig = new JsonObject();
        tokenConfig.put("code", code);
        tokenConfig.put("redirect_uri", this.getRedirectUri());

        // Perform authentication
        this.getAuthenticator().authenticate(tokenConfig, res -> {
            if (res.failed()) {
                res.cause().printStackTrace();
                context.response()
                        .putHeader("Location", System.getenv("APP_URL") + "/auth/login?error=Erreur lors de la connexion")
                        .setStatusCode(302).end();
                return;
            }

            // Get user information from github page
            AccessToken accessToken = (AccessToken) res.result();
            accessToken.userInfo(info -> {
                JsonObject userInfo = info.result();
                OAuth2Manager.OAuth2Fields fields = new OAuth2Manager.OAuth2Fields(OAuthTypes.SPOTIFY);
                fields.setAccessToken(accessToken.principal().getString("access_token"));

                // Configure login fields
                String login = userInfo.getString("id");
                fields.setAccountId(login);

                // Already logged in
                if (stateUUID != null && !stateUUID.isEmpty()) {
                    UUID accountUUID = UUID.fromString(stateUUID);

                    // Add to current account and redirect
                    this.addOauthToAccount(accountUUID, fields, ok -> {
                        context.response().putHeader("Location", System.getenv("APP_URL") + "/admin/profile")
                                .setStatusCode(302).end();
                    });
                    return;
                }

                // Already exists in database, just login
                this.loginWhenOAuthAlreadyExists(fields.type, login, token -> {
                    if (token == null)
                        return;

                    context.response().putHeader("Location", System.getenv("APP_URL") + "/auth/login?token=" + token)
                            .setStatusCode(302).end();
                });

                // Configure display name fields for account creation
                String[] names = userInfo.getString("display_name").split(" ");

                // Set first name and last name in fields
                fields.setFirstAndLastNames(names.length <= 1 ? login : names[0], names.length <= 1 ? "" : names[1]);

                // Create account with oauth access
                this.createOauthAccount(fields, login, (token) -> {
                    context.response().putHeader("Location", System.getenv("APP_URL") + "/auth/login?token=" + token)
                            .setStatusCode(302).end();
                });
            });
        });
    }

}
