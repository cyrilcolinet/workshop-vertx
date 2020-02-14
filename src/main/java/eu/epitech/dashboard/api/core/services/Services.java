package eu.epitech.dashboard.api.core.services;

import eu.epitech.dashboard.api.core.services.abstracts.AbstractService;

import java.util.HashMap;
import java.util.Map;

/**
 * Configure all services in this class
 * All services must be added in list on the {@see Services#configureServices}
 */
public class Services {

    private Map<String, AbstractService> services = new HashMap<>();

    /**
     * Services constructor.
     */
    public Services() {
        this.configureServices();
    }

    /**
     * Instantiate and configure all services
     * Method called on startup
     */
    private void configureServices() {
        this.services.put("github", new GithubService());
        this.services.put("spotify", new SpotifyService());
    }

    /**
     * Get list of all services
     * @return Map
     */
    public Map<String, AbstractService> all() {
        return services;
    }
}
