package eu.epitech.dashboard.api.widgets.github;

import eu.epitech.dashboard.api.widgets.AbstractWidget;

public class CountUsersFollowers extends AbstractWidget {
    /**
     * AbstractWidget constructor.
     */
    public CountUsersFollowers() {
        super("count_followers", "Affiche le nombre de followers sur Github du compte en paramètre");

        // Add params
        this.addParam(new Parameter("username", "string", "Nom d'utilisateur"));
    }

}
