package eu.epitech.dashboard.api.widgets.spotify;

import eu.epitech.dashboard.api.widgets.AbstractWidget;

public class GetUserPlaylistWidget extends AbstractWidget {
    public GetUserPlaylistWidget() {
        super("get_playlists", "Récupère les playlist de l'utilisateur choisi.");

        // Adding parameters
        this.addParam(new Parameter("spotify_id", "string", "Nom du compte Spotify"));
    }
}
