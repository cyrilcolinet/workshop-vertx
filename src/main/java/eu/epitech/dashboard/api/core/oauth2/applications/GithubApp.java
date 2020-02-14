package eu.epitech.dashboard.api.core.oauth2.applications;

import eu.epitech.dashboard.api.APIServer;
import eu.epitech.dashboard.api.core.oauth2.AbstractApp;
import eu.epitech.dashboard.api.core.oauth2.OAuth2Manager;
import eu.epitech.dashboard.api.core.oauth2.OAuthTypes;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.AccessToken;
import io.vertx.ext.auth.oauth2.providers.GithubAuth;
import io.vertx.ext.web.RoutingContext;

import java.util.UUID;

/**
 * GithubApplication
 * @see AbstractApp
 */
public class GithubApp extends AbstractApp {

    private static final String clientId = "aefcd9f261cc4ed98c4b";
    private static final String clientSecret = "591ad7ee9d84f54f0bbc132d94c0f6c0a6a603ed";

    /**
     * Constructor class
     * Create and stock auth
     */
    public GithubApp() {
        super(GithubAuth.create(APIServer.get().getVertx(), clientId, clientSecret));
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
        return "http://127.0.0.1:8080/auth/githubCallback";
    }

    /**
     * Handling callback
     * @param context RoutingContext
     */
    @Override
    public void handleCallback(RoutingContext context) {
        String stateUUID = context.request().getParam("state");
        String code = context.request().params().get("code");
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
                OAuth2Manager.OAuth2Fields fields = new OAuth2Manager.OAuth2Fields(OAuthTypes.GITHUB);
                fields.setAccessToken(accessToken.principal().getString("access_token"));
                fields.setTokenType(accessToken.principal().getString("token_type"));

                // Configure login field
                String login = userInfo.getString("login");
                fields.setAccountId(login);

                // Already logged in
                if (stateUUID != null && !stateUUID.isEmpty()) {
                    UUID accountUUID = UUID.fromString(context.request().params().get("code"));

                    // Add to current account and redirect
                    this.addOauthToAccount(accountUUID, fields, (ok) -> {
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

                // Configure fields
                String[] names = userInfo.getString("name").split(" ");

                // Set first name and last name for account creation
                fields.setFirstAndLastNames(names.length <= 1 ? login : names[0], names.length <= 1 ? "" : names[1]);

                // Create account with oauth access if not exists
                this.createOauthAccount(fields, login, (token) -> {
                    context.response().putHeader("Location", System.getenv("APP_URL") + "/auth/login?token=" + token)
                            .setStatusCode(302).end();
                });
            });
        });
    }

}
