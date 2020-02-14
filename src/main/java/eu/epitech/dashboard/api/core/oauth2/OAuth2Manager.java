package eu.epitech.dashboard.api.core.oauth2;

import eu.epitech.dashboard.api.core.oauth2.applications.GithubApp;
import eu.epitech.dashboard.api.core.oauth2.applications.SpotifyApp;
import io.vertx.ext.web.Router;

import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2Generator
 * It generates class object and return object or authentication URI
 */
public class OAuth2Manager {

    private Map<String, AbstractApp> app = new HashMap<>();

    /**
     * Class constructor
     * Implements and configure all OAuth applications
     */
    public OAuth2Manager() {
        this.configureApplications();
    }

    /**
     * Configure all available applications
     */
    private void configureApplications() {
        this.app.put("github", new GithubApp());
        this.app.put("spotify", new SpotifyApp());
    }

    /**
     * Configure all application callbacks
     * @param router Router
     */
    public void configureCallbackUrls(Router router) {
        this.getApps().forEach((name, app) -> {
            // Create route and perform request
            router.get("/" + name + "Callback").handler(app::handleCallback);
        });
    }

    /**
     * Get registered applications
     * @return Map
     */
    public Map<String, AbstractApp> getApps() {
        return app;
    }

    /**
     * OauthField for database and more
     */
    public static class OAuth2Fields {

        public OAuthTypes type;

        public String firstName;
        public String lastName;
        public String access_token;
        public String token_type;
        public String token_expires;
        public String account_id;

        public OAuth2Fields(OAuthTypes type) {
            this.type = type;
        }

        public void setFirstAndLastNames(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public void setAccessToken(String access_token) {
            this.access_token = access_token;
        }

        public void setTokenType(String token_type) {
            this.token_type = token_type;
        }

        public void setTokenExpires(String token_expires) {
            this.token_expires = token_expires;
        }

        public void setAccountId(String account_id) {
            this.account_id = account_id;
        }
    }
}
