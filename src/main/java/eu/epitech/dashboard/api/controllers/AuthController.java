package eu.epitech.dashboard.api.controllers;

import eu.epitech.dashboard.api.APIServer;
import eu.epitech.dashboard.api.controllers.abstracts.AbstractController;
import eu.epitech.dashboard.api.core.database.MongoQueries;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jwt.JWTOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

/**
 * Authentication controller
 * @see AbstractController
 */
public class AuthController extends AbstractController {

    /**
     * Constructor
     */
    public AuthController() {
        super(null);
    }

    @Override
    public Router configure(Vertx vertx) {
        final Router subRouter = Router.router(vertx);

        // Adding routes
        subRouter.route().handler(BodyHandler.create());
        subRouter.post("/login").handler(this::loginAction);
        subRouter.route().handler(BodyHandler.create());
        subRouter.post("/register").handler(this::registerAction);

        // OAuthActions and callback register
        subRouter.get("/loginStatus").handler(this::listOAuthAuthorizeURLs);
        APIServer.get().getOAuth2Manager().configureCallbackUrls(subRouter);

        return subRouter;
    }

    /**
     * Login user with JWToken
     * @param context RoutingContext
     */
    private void loginAction(RoutingContext context) {
        JsonObject body = context.getBodyAsJson();

        // Check if data is correct
        if (!body.containsKey("username") || !body.containsKey("password")) {
            context.response()
                    .putHeader("Location", System.getenv("APP_URL") + "/auth/login?error=Remplissez tout les champs.")
                    .setStatusCode(302).end();
            return;
        }

        // Compress and hash password in MD5
        String hash;
        try {
            byte[] byteString = body.getString("password").getBytes(StandardCharsets.UTF_8);
            MessageDigest md = MessageDigest.getInstance("MD5");
            hash = Base64.getEncoder().encodeToString(md.digest(byteString));
        } catch (NoSuchAlgorithmException err) {
            context.response()
                    .putHeader("Location", System.getenv("APP_URL") + "/auth/login?error=" + err.toString())
                    .setStatusCode(302).end();
            return;
        }

        // Check password hash
        UUID uuid = MongoQueries.getAccountByUserAndHash(body.getString("username"), hash);
        if (uuid == null) {
            context.response()
                    .putHeader("Location", System.getenv("APP_URL") + "/auth/login?error=Aucun compte trouvé avec ces identifiants")
                    .setStatusCode(302).end();
            return;
        }

        // Valid password, generate token and send it to user
        JsonObject params = new JsonObject().put("uuid", uuid.toString());
        String token = APIServer.get().getJwtAuthProvider().generateToken(params, new JWTOptions());

        // Great !
        context.response()
                .putHeader("Location", System.getenv("APP_URL") + "/auth/login?token=" + token)
                .setStatusCode(302).end();
    }

    /**
     * Registering new user in mongodb database
     * @param context RoutingContext
     */
    private void registerAction(RoutingContext context) {
        JsonObject body = context.getBodyAsJson();

        // Check if data is correctly set
        if (!body.containsKey("first_name") || !body.containsKey("last_name") || !body.containsKey("password")) {
            context.response()
                    .putHeader("Location", System.getenv("APP_URL") + "/auth/register?error=Remplissez tout les champs.")
                    .setStatusCode(302).end();
            return;
        }

        // Hashing password
        String hashedPassword;
        try {
            byte[] byteString = body.getString("password").getBytes(StandardCharsets.UTF_8);
            MessageDigest md = MessageDigest.getInstance("MD5");
            hashedPassword = Base64.getEncoder().encodeToString(md.digest(byteString));
        } catch (NoSuchAlgorithmException err) {
            context.response()
                    .putHeader("Location", System.getenv("APP_URL") + "/auth/register?error=" + err.toString())
                    .setStatusCode(302).end();
            return;
        }

        // Create new user
        UUID uuid = MongoQueries.createNewUser(body.getString("first_name"), body.getString("last_name"), hashedPassword);
        if (uuid == null) {
            context.response()
                    .putHeader("Location", System.getenv("APP_URL") + "/auth/register?error=Le compte existe déjà")
                    .setStatusCode(302).end();
            return;
        }

        // Great !
        context.response()
                .putHeader("Location", System.getenv("APP_URL") + "/auth/login?success=Compte créé ! Connectez-vous maintenant.")
                .setStatusCode(302).end();
    }

    /**
     * Github login oauth
     * @param context RoutingContext
     */
    private void listOAuthAuthorizeURLs(RoutingContext context) {
        JsonObject resp = new JsonObject();
        JsonObject urls = new JsonObject();

        // Add all to list, and send it to response
        APIServer.get().getOAuth2Manager().getApps().forEach((key, value) ->
                urls.put(key, value.getAuthorizeUrl(null)));
        resp.put("list", urls);

        // Response with a list of oauth authorization URLs
        context.response()
                .setStatusCode(200)
                .putHeader("content-type", "application/json")
                .end(Json.encodePrettily(JsonObject.mapFrom(resp)));
    }

}
