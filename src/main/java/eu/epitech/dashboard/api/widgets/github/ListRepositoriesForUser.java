package eu.epitech.dashboard.api.widgets.github;

import eu.epitech.dashboard.api.widgets.AbstractWidget;

public class ListRepositoriesForUser extends AbstractWidget {
    /**
     * AbstractWidget constructor.
     */
    public ListRepositoriesForUser() {
        super("list_repositories", "Lister les repos d'un utilisateur en particulier.");

        // Add params
        this.addParam(new Parameter("max", "int", "Nombre max de repo à afficher"));
        this.addParam(new Parameter("username", "string", "Nom d'utilisateur"));
    }

}
