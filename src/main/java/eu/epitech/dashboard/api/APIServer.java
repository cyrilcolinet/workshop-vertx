package eu.epitech.dashboard.api;

import eu.epitech.dashboard.api.controllers.*;
import eu.epitech.dashboard.api.core.database.MongoConnector;
import eu.epitech.dashboard.api.core.oauth2.OAuth2Manager;
import eu.epitech.dashboard.api.core.services.Services;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

/**
 * Dashboard API entry point class
 * This class is called when "vertx exec" command is executed or in start of jar
 *
 * Class in singleton, all useful core services (ex: database...) is instantiated
 * here and must be stopped with stop function override bellow
 * @see io.vertx.core.AbstractVerticle
 */
public class APIServer extends AbstractVerticle {

    private static APIServer instance;
    private static Logger logger;

    private HttpServer server;
    private Router router;

    private MongoConnector mongo;
    private Services services;
    private OAuth2Manager oAuth2Manager;
    private JWTAuth jwtAuthProvider;

    /**
     * Start vert.x server and waiting for back future
     * @param startFuture Start future of main launcher
     */
    @Override
    public void start(Future<Void> startFuture) {
        instance = this;
        logger = LoggerFactory.getLogger(APIServer.class);

        // Configure databases and libraries
        this.mongo = new MongoConnector();
        this.services = new Services();
        this.oAuth2Manager = new OAuth2Manager();

        // Configure and start server (IN LAST !!!)
        this.startHttpServer().setHandler(startFuture.completer());
    }

    /**
     * Stopping server properly
     * Kill all core connections
     */
    @Override
    public void stop() {
        this.server.close();
        this.mongo.connection().close();

        // Set instance to null
        instance = null;
        logger = null;
    }

    /**
     * Configure all routes in all services
     */
    private void configureRoutes() {
        // Set main class route
        this.router.mountSubRouter("/", new MainController().configure(vertx));
        this.router.mountSubRouter("/auth", new AuthController().configure(vertx));

        // Mount all controller sub router
        final Router apiRouter = Router.router(vertx);
        apiRouter.mountSubRouter("/user", new UserController().configure(vertx));
        apiRouter.mountSubRouter("/github", new GithubController().configure(vertx));
        apiRouter.mountSubRouter("/spotify", new SpotifyController().configure(vertx));

        // Configure api sub router
        this.router.mountSubRouter("/api", apiRouter);
    }

    /**
     * Start server when launcher of main vert.x server started
     * @return Future/Promise
     */
    private Future<Void> startHttpServer() {
        Future<Void> future = Future.future();

        // Configure all routes
        this.router = Router.router(vertx);

        // Handle router
        this.router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
        this.router.route("/*").handler(BodyHandler.create());
        this.router.route("/*").handler(CorsHandler.create(".*.")
            .allowedHeader("Origin")
            .allowedHeader("Content-Type")
            .allowedHeader("Accept")
            .allowedHeader("Authorization"));

        // Configure JavascriptWebToken authentication
        JWTAuthOptions config = new JWTAuthOptions()
                .setKeyStore(new KeyStoreOptions().setPath("keystore.jceks").setPassword("aME8Hi92n"));
        this.jwtAuthProvider = JWTAuth.create(vertx, config);

        // Configure JWTAuth handler provider
        this.router.route("/api/*").handler(JWTAuthHandler.create(this.jwtAuthProvider));

        // Configure sub routers and main routes
        this.configureRoutes();

        // Catch routes errors
        this.router.errorHandler(500, rc -> {
            logger().error("Handler failure");
            Throwable failure = rc.failure();
            if (failure != null) {
                failure.printStackTrace();
            }
        });

        // Configure server
        this.server = vertx.createHttpServer(new HttpServerOptions().setPort(8080).setHost("0.0.0.0"));
        this.server.requestHandler(this.router).listen(ar -> {
            // Error starting the HttpServer
            if (ar.succeeded()) future.complete();
            else future.fail(ar.cause());
        });

        // Return configured promise
        return future;
    }

    /**
     * Get mongo connector
     * Use this method to interact with database
     * @return MongoConnector
     */
    public MongoConnector getMongo() {
        return mongo;
    }

    /**
     * Get service configurator class
     * @return Services
     */
    public Services getServices() {
        return services;
    }

    /**
     * Get OAuth2Manager class
     * @return OAuth2Manager
     */
    public OAuth2Manager getOAuth2Manager() {
        return oAuth2Manager;
    }

    /**
     * Get JWTAuthProvider for authentication
     * @return JWTAuth
     */
    public JWTAuth getJwtAuthProvider() {
        return jwtAuthProvider;
    }

    /**
     * Get APIServer singleton instance
     * @return APIServer
     */
    public static APIServer get() {
        return instance;
    }

    /**
     * Return logger of the application
     * @return Logger
     */
    public static Logger logger() {
        return logger;
    }

    /**
     * Entry point on app started by --jar option
     * @param args Arguments passed has parameters
     */
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new APIServer());
    }

}