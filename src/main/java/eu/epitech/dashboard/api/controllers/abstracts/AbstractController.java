package eu.epitech.dashboard.api.controllers.abstracts;

import eu.epitech.dashboard.api.APIServer;
import eu.epitech.dashboard.api.core.services.abstracts.AbstractService;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

/**
 * IRoute interface
 * All routes/controllers must be extended with this class
 */
public abstract class AbstractController {

    private AbstractService belongsToService;

    /**
     * Constructor
     * @param serviceName String
     */
    public AbstractController(String serviceName) {
        if (serviceName == null) {
            this.belongsToService = null;
            return;
        }
        this.belongsToService = APIServer.get().getServices().all().get(serviceName);
    }

    /**
     * Configure routes for this controller
     */
    public abstract Router configure(final Vertx vertx);

    /**
     * Get service controller belonged to
     * @return AbstractService
     */
    public AbstractService getBelongedService() {
        return belongsToService;
    }
}
