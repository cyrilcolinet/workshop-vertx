package eu.epitech.dashboard.api.widgets.github;

import eu.epitech.dashboard.api.widgets.AbstractWidget;

public class ListUserFollowers extends AbstractWidget {
    /**
     * AbstractWidget constructor.
     */
    public ListUserFollowers() {
        super("list_followers", "Lister les followers d'un utilisateur");

        // Add params
        this.addParam(new Parameter("max", "int", "Nombre max de followers à afficher"));
        this.addParam(new Parameter("username", "string", "Nom d'utilisateur"));
    }

}
