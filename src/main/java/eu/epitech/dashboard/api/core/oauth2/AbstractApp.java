package eu.epitech.dashboard.api.core.oauth2;

import eu.epitech.dashboard.api.APIServer;
import eu.epitech.dashboard.api.core.database.MongoQueries;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.ext.web.RoutingContext;

import java.util.UUID;

/**
 * Application OAuth interface
 */
public abstract class AbstractApp {

    private OAuth2Auth auth;

    /**
     * Constructor
     * @param auth OAuthAuth
     */
    public AbstractApp(OAuth2Auth auth) {
        this.auth = auth;
    }

    /**
     * Generate token JWT
     * @param uuid UUID
     * @return String
     */
    protected String generateToken(UUID uuid) {
        JsonObject claims = new JsonObject().put("uuid", uuid.toString());
        return APIServer.get().getJwtAuthProvider().generateToken(claims, new JWTOptions());
    }

    /**
     * Get authenticator
     * @return OAuth2Auth
     */
    public OAuth2Auth getAuthenticator() {
        return auth;
    }

    /**
     * Get authorize url to perform authorization
     * @param uuid UUID of user
     * @return String
     */
    public abstract String getAuthorizeUrl(UUID uuid);

    /**
     * Set uuid param if not null
     * @param uuid UUid of the current logged in user
     * @return Empty string if uuid is null, query parameter with uuid otherwise
     */
    protected String getUUIDParam(UUID uuid) {
        if (uuid == null)
            return "";

        return uuid.toString();
    }

    /**
     * Get redirect URI
     * @return String
     */
    public abstract String getRedirectUri();

    /**
     * Handling callback
     * @param context RoutingContext
     */
    public abstract void handleCallback(RoutingContext context);

    /**
     * Adding oauth account to current logged in account
     * @param uuid UUID of current account
     * @param fields Fields (token ...) for the app
     * @param handler Handler callback
     */
    protected void addOauthToAccount(UUID uuid, OAuth2Manager.OAuth2Fields fields, Handler<Boolean> handler) {
        MongoQueries.addOauthToAccountWithUUID(uuid, fields);

        // Redirect to profile page
        handler.handle(true);
    }

    /**
     * Create account from oauth access
     * @param fields Fields in oauth control
     * @param login Login identifier unique
     * @param handler Context of the response
     */
    protected void createOauthAccount(OAuth2Manager.OAuth2Fields fields, String login, Handler<String> handler) {
        UUID uuid = MongoQueries.createUserWithOAuth(login, fields);
        String token = this.generateToken(uuid);

        // Redirect to login page with token
        handler.handle(token);
    }

    /**
     *Login only when oauth account already exists
     * @param type Oauth type
     * @param login Login id
     * @param handler Handle response with this handler
     */
    protected void loginWhenOAuthAlreadyExists(OAuthTypes type, String login, Handler<String> handler) {
        UUID accountUUID = MongoQueries.getAccountUUIDByOAuthAccountId(type, login);

        // If login is true, just login and return JWToken
        if (accountUUID != null) {
            String jwtToken = this.generateToken(accountUUID);
            handler.handle(jwtToken);
            return;
        }

        handler.handle(null);
    }

}
